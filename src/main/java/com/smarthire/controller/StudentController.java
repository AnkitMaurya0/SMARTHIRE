package com.smarthire.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.smarthire.model.Application;
import com.smarthire.model.Job;
import com.smarthire.model.User;
import com.smarthire.repository.ApplicationRepository;
import com.smarthire.service.ATSService;
import com.smarthire.service.JobService;
import com.smarthire.service.MessageService;
import com.smarthire.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;

/**
 * StudentController Class
 * Handles all Student related pages and actions.
 * Accessible only by users with role STUDENT.
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private JobService jobService; // get all jobs

    @Autowired
    private UserService userService; // get logged in student

    @Autowired
    private ApplicationRepository applicationRepo; // save and get applications

    @Autowired
    private ATSService atsService; // resume parsing and scoring

    @Autowired
    private MessageService messageService; // unread message count

    /**
     * Student Dashboard
     * Shows total applications, shortlisted count,
     * best ATS score and unread messages
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {

        User student = userService.findByEmail(auth.getName());
        List<Application> applications = applicationRepo.findByStudent(student);

        // Count shortlisted applications
        long totalShortlisted = applications.stream()
                .filter(a -> a.getStatus().equals("SHORTLISTED"))
                .count();

        // Get best ATS score
        double bestScore = applications.stream()
                .mapToDouble(Application::getAtsScore)
                .max()
                .orElse(0.0);

        // Round best score
        bestScore = Math.round(bestScore * 100.0) / 100.0;

        // Count unread messages
        long unreadMessages = messageService.getUnreadCount(student);

        model.addAttribute("student", student);
        model.addAttribute("totalApplied", applications.size());
        model.addAttribute("totalShortlisted", totalShortlisted);
        model.addAttribute("bestScore", bestScore);
        model.addAttribute("unreadMessages", unreadMessages);
        model.addAttribute("applications", applications);

        return "student/dashboard";
    }

    /**
     * Show all available jobs
     * Also handles search by keyword
     */
    @GetMapping("/jobs")
    public String jobs(Model model,
            @RequestParam(required = false) String search) {

        List<Job> jobs;

        if (search != null && !search.isEmpty()) {
            jobs = jobService.searchJobs(search);
        } else {
            jobs = jobService.getAllJobs();
        }

        model.addAttribute("jobs", jobs);
        model.addAttribute("search", search);

        return "student/jobs";
    }

    /**
     * Show apply job page for a specific job
     */
    @GetMapping("/apply/{jobId}")
    public String applyPage(@PathVariable Long jobId,
            Model model,
            Authentication auth) {

        User student = userService.findByEmail(auth.getName());
        Job job = jobService.getJobById(jobId);

        // Check if student already applied for this job
        boolean alreadyApplied = applicationRepo.existsByStudentAndJob(student, job);

        model.addAttribute("job", job);
        model.addAttribute("alreadyApplied", alreadyApplied);

        return "student/apply-job";
    }

    /**
     * Handle resume upload and job application
     * Parses resume, calculates ATS score and saves application
     */
    @PostMapping("/apply/{jobId}")
    public String applyJob(@PathVariable Long jobId,
            @RequestParam("resume") MultipartFile resume,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            Job job = jobService.getJobById(jobId);

            // Check duplicate application
            if (applicationRepo.existsByStudentAndJob(student, job)) {
                ra.addFlashAttribute("error",
                        "You have already applied for this job.");
                return "redirect:/student/jobs";
            }

            // Parse resume and extract text
            String resumeText = atsService.parseResume(resume);

            // Save resume file to uploads folder
            String filename = atsService.saveResumeFile(
                    resume, student.getId());

            // Calculate ATS score
            double score = atsService.calculateScore(
                    resumeText, job.getRequiredSkills());

            // Get matched and missing skills
            String matched = String.join(",",
                    atsService.getMatchedSkills(
                            resumeText, job.getRequiredSkills()));
            String missing = String.join(",",
                    atsService.getMissingSkills(
                            resumeText, job.getRequiredSkills()));

            // Create and save application
            Application application = new Application();
            application.setStudent(student);
            application.setJob(job);
            application.setResumePath(filename);
            application.setExtractedText(resumeText);
            application.setAtsScore(score);
            application.setMatchedSkills(matched);
            application.setMissingSkills(missing);
            application.setStatus("APPLIED");
            application.setAppliedDate(LocalDate.now());

            applicationRepo.save(application);

            // Send notification message to HR
            messageService.sendMessage(
                    student,
                    job.getHr(),
                    "Hello, I have applied for the position of " +
                            job.getTitle() +
                            ". My ATS Score is " + score + "%. " +
                            "Please review my application.",
                    null,
                    job);

            ra.addFlashAttribute("success",
                    "Application submitted! Your ATS Score: "
                            + score + "%");

        } catch (Exception e) {
            ra.addFlashAttribute("error",
                    "Something went wrong: " + e.getMessage());
        }

        return "redirect:/student/applied-jobs";
    }

    /**
     * Show all jobs applied by logged in student
     */
    @GetMapping("/applied-jobs")
    public String appliedJobs(Model model, Authentication auth) {

        User student = userService.findByEmail(auth.getName());
        List<Application> applications = applicationRepo.findByStudent(student);

        model.addAttribute("applications", applications);

        return "student/applied-jobs";
    }

    /**
     * Show ATS resume screening page
     * Pass all jobs for dropdown selection
     */
    @GetMapping("/ats-check")
    public String atsCheckPage(Model model) {
        List<Job> jobs = jobService.getAllJobs();
        model.addAttribute("jobs", jobs);
        return "student/ats-result";
    }

    /**
     * Handle ATS check form submission
     * Job select karke resume check karo
     */
    @PostMapping("/ats-check")
    public String atsCheck(@RequestParam("resume") MultipartFile resume,
            @RequestParam("jobId") Long jobId,
            Model model) {
        try {
            Job job = jobService.getJobById(jobId);
            String resumeText = atsService.parseResume(resume);
            double score = atsService.calculateScore(
                    resumeText, job.getRequiredSkills());
            List<String> matched = atsService.getMatchedSkills(
                    resumeText, job.getRequiredSkills());
            List<String> missing = atsService.getMissingSkills(
                    resumeText, job.getRequiredSkills());
            String label = atsService.getScoreLabel(score);

            model.addAttribute("score", score);
            model.addAttribute("matched", matched);
            model.addAttribute("missing", missing);
            model.addAttribute("label", label);
            model.addAttribute("job", job);

        } catch (Exception e) {
            model.addAttribute("error",
                    "Could not process resume: " + e.getMessage());
        }

        model.addAttribute("jobs", jobService.getAllJobs());
        return "student/ats-result";
    }

    /**
     * Download ATS Report as PDF
     * Generates detailed PDF report with score breakdown
     */
    @PostMapping("/ats-report")
    public void downloadAtsReport(
            @RequestParam("resume") MultipartFile resume,
            @RequestParam("jobId") Long jobId,
            Authentication auth,
            HttpServletResponse response) throws Exception {

        User student = userService.findByEmail(auth.getName());
        Job job = jobService.getJobById(jobId);

        String resumeText = atsService.parseResume(resume);
        double score = atsService.calculateScore(
                resumeText, job.getRequiredSkills());
        List<String> matched = atsService.getMatchedSkills(
                resumeText, job.getRequiredSkills());
        List<String> missing = atsService.getMissingSkills(
                resumeText, job.getRequiredSkills());
        String label = atsService.getScoreLabel(score);

        // PDF response setup
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=ATS_Report.pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Fonts
        Font titleFont = new Font(Font.FontFamily.HELVETICA,
                20, Font.BOLD);
        Font headFont = new Font(Font.FontFamily.HELVETICA,
                14, Font.BOLD);
        Font bodyFont = new Font(Font.FontFamily.HELVETICA,
                11, Font.NORMAL);
        Font greenFont = new Font(Font.FontFamily.HELVETICA,
                11, Font.BOLD, BaseColor.GREEN);
        Font redFont = new Font(Font.FontFamily.HELVETICA,
                11, Font.BOLD, BaseColor.RED);
        Font scoreFont = new Font(Font.FontFamily.HELVETICA,
                24, Font.BOLD);

        // Title
        Paragraph title = new Paragraph(
                "SmartHire - ATS Score Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        document.add(new Paragraph(
                "─────────────────────────────────────", bodyFont));

        // Student Info
        document.add(new Paragraph(" "));
        document.add(new Paragraph(
                "Student Name : " + student.getName(), bodyFont));
        document.add(new Paragraph(
                "College      : " + student.getCollege(), bodyFont));
        document.add(new Paragraph(
                "Degree       : " + student.getDegree(), bodyFont));
        document.add(new Paragraph(
                "Date         : " + LocalDate.now(), bodyFont));
        document.add(new Paragraph(" "));

        // Job Info
        document.add(new Paragraph(
                "─────────────────────────────────────", bodyFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(
                "Job Title       : " + job.getTitle(), bodyFont));
        document.add(new Paragraph(
                "Company         : " + job.getHr().getCompany(),
                bodyFont));
        document.add(new Paragraph(
                "Required Skills : " + job.getRequiredSkills(),
                bodyFont));
        document.add(new Paragraph(" "));

        // ATS Score
        document.add(new Paragraph(
                "─────────────────────────────────────", bodyFont));
        document.add(new Paragraph(" "));

        Paragraph scorePara = new Paragraph(
                "Overall ATS Score : " + score + "%", scoreFont);
        scorePara.setAlignment(Element.ALIGN_CENTER);
        document.add(scorePara);

        Paragraph labelPara = new Paragraph(label, headFont);
        labelPara.setAlignment(Element.ALIGN_CENTER);
        document.add(labelPara);
        document.add(new Paragraph(" "));

        // Matched Skills
        document.add(new Paragraph(
                "─────────────────────────────────────", bodyFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(
                "Matched Skills (" + matched.size() + ")", headFont));
        document.add(new Paragraph(" "));

        if (matched.isEmpty()) {
            document.add(new Paragraph(
                    "No skills matched.", bodyFont));
        } else {
            for (String skill : matched) {
                document.add(new Paragraph(
                        "   + " + skill, greenFont));
            }
        }
        document.add(new Paragraph(" "));

        // Missing Skills
        document.add(new Paragraph(
                "─────────────────────────────────────", bodyFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(
                "Missing Skills (" + missing.size() + ")", headFont));
        document.add(new Paragraph(" "));

        if (missing.isEmpty()) {
            document.add(new Paragraph(
                    "All skills matched. Perfect resume!", bodyFont));
        } else {
            for (String skill : missing) {
                document.add(new Paragraph(
                        "   - " + skill, redFont));
            }
        }
        document.add(new Paragraph(" "));

        // Improvement Tips
        if (!missing.isEmpty()) {
            document.add(new Paragraph(
                    "─────────────────────────────────────", bodyFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "How to Improve Your Score", headFont));
            document.add(new Paragraph(" "));
            for (String skill : missing) {
                document.add(new Paragraph(
                        "  -> Add '" + skill +
                                "' to your resume skills section.",
                        bodyFont));
            }
            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "  -> Add more project details and experience.",
                    bodyFont));
            document.add(new Paragraph(
                    "  -> Include certifications related to the job.",
                    bodyFont));
            document.add(new Paragraph(
                    "  -> Mention internships and achievements.",
                    bodyFont));
        }

        // Footer
        document.add(new Paragraph(" "));
        document.add(new Paragraph(
                "─────────────────────────────────────", bodyFont));
        Paragraph footer = new Paragraph(
                "Generated by SmartHire - AI Resume Screening Portal",
                bodyFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
    }
}