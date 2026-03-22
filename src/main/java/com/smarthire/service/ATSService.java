package com.smarthire.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.*;

@Service
public class ATSService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${python.script.path}")
    private String pythonScriptPath;

    @Value("${python.executable}")
    private String pythonExecutable;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse resume file and extract plain text
     */
    public String parseResume(MultipartFile file) throws Exception {
        Tika tika = new Tika();
        tika.setMaxStringLength(100000);
        return tika.parseToString(file.getInputStream());
    }

    /**
     * Save uploaded resume file to uploads/resumes folder
     */
    public String saveResumeFile(MultipartFile file,
            Long studentId) throws Exception {
        String filename = "student_" + studentId + "_" +
                System.currentTimeMillis() + "_" +
                file.getOriginalFilename();
        Path dirPath = Paths.get(uploadDir);
        Files.createDirectories(dirPath);
        Path filePath = dirPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath,
                StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    /**
     * Calculate ATS score - calls Python ML script
     * Falls back to Java scoring if Python fails
     */
    @SuppressWarnings("unchecked")
    public double calculateScore(String resumeText,
            String requiredSkills) {
        try {
            // Call Python ML script
            Map<String, Object> result = callPythonScript(resumeText, requiredSkills);
            return ((Number) result.get("score")).doubleValue();

        } catch (Exception e) {
            System.err.println("Python failed, using Java: "
                    + e.getMessage());
            return javaScore(resumeText, requiredSkills);
        }
    }

    /**
     * Call Python ATS scorer script
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callPythonScript(
            String resumeText, String requiredSkills) throws Exception {

        // Clean text to avoid argument issues
        String cleanResume = resumeText
                .replace("\"", " ")
                .replace("'", " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();

        ProcessBuilder pb = new ProcessBuilder(
                pythonExecutable,
                pythonScriptPath,
                cleanResume,
                requiredSkills);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        String output = new String(
                process.getInputStream().readAllBytes());
        process.waitFor();

        if (output.trim().startsWith("{")) {
            return objectMapper.readValue(output.trim(), Map.class);
        }

        throw new RuntimeException("Python output invalid: " + output);
    }

    /**
     * Java fallback scoring
     */
    private double javaScore(String resumeText,
            String requiredSkills) {
        if (resumeText == null || requiredSkills == null)
            return 0;
        String[] skills = requiredSkills.toLowerCase().split(",");
        String resumeLower = resumeText.toLowerCase();
        int matched = 0;
        for (String skill : skills) {
            if (resumeLower.contains(skill.trim()))
                matched++;
        }
        double score = ((double) matched / skills.length) * 100;
        return Math.round(score * 100.0) / 100.0;
    }

    /**
     * Get matched skills
     */
    public List<String> getMatchedSkills(String resumeText,
            String requiredSkills) {
        String[] skills = requiredSkills.toLowerCase().split(",");
        String resumeLower = resumeText.toLowerCase();
        List<String> matched = new ArrayList<>();
        for (String skill : skills) {
            if (resumeLower.contains(skill.trim())) {
                matched.add(skill.trim());
            }
        }
        return matched;
    }

    /**
     * Get missing skills
     */
    public List<String> getMissingSkills(String resumeText,
            String requiredSkills) {
        String[] skills = requiredSkills.toLowerCase().split(",");
        String resumeLower = resumeText.toLowerCase();
        List<String> missing = new ArrayList<>();
        for (String skill : skills) {
            if (!resumeLower.contains(skill.trim())) {
                missing.add(skill.trim());
            }
        }
        return missing;
    }

    /**
     * Score label
     */
    public String getScoreLabel(double score) {
        if (score >= 75)
            return "Excellent Match";
        if (score >= 55)
            return "Good Match";
        if (score >= 35)
            return "Average Match";
        return "Low Match";
    }
}