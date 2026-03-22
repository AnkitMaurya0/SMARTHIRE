package com.smarthire.controller;

import com.smarthire.model.Application;
import com.smarthire.model.Job;
import com.smarthire.model.User;
import com.smarthire.repository.ApplicationRepository;
import com.smarthire.service.JobService;
import com.smarthire.service.MessageService;
import com.smarthire.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

/**
 * HRController Class
 * Handles all HR related pages and actions.
 * Accessible only by users with role HR.
 */
@Controller
@RequestMapping("/hr")
public class HRController {

    @Autowired
    private JobService jobService; // job posting logic

    @Autowired
    private UserService userService; // get logged in HR

    @Autowired
    private ApplicationRepository applicationRepo; // get applications

    @Autowired
    private MessageService messageService; // unread message count

    /**
     * HR Dashboard
     * Shows total jobs, applications and unread messages
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {

        User hr = userService.findByEmail(auth.getName());
        List<Job> jobs = jobService.getJobsByHR(hr);

        // Count total applications across all HR jobs
        long totalApplications = 0;
        for (Job job : jobs) {
            totalApplications += applicationRepo.countByJob(job);
        }

        // Count unread messages
        long unreadMessages = messageService.getUnreadCount(hr);

        model.addAttribute("hr", hr);
        model.addAttribute("totalJobs", jobs.size());
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("unreadMessages", unreadMessages);
        model.addAttribute("jobs", jobs);

        return "hr/dashboard";
    }

    /**
     * Show Post Job form page
     */
    @GetMapping("/post-job")
    public String postJobPage(Model model) {
        model.addAttribute("job", new Job());
        return "hr/post-job";
    }

    /**
     * Handle Post Job form submission
     */
    @PostMapping("/post-job")
    public String postJob(@ModelAttribute Job job,
            Authentication auth,
            RedirectAttributes ra) {

        User hr = userService.findByEmail(auth.getName());
        job.setHr(hr);
        jobService.saveJob(job);

        ra.addFlashAttribute("success", "Job posted successfully!");
        return "redirect:/hr/view-jobs";
    }

    /**
     * Show all jobs posted by logged in HR
     */
    @GetMapping("/view-jobs")
    public String viewJobs(Model model, Authentication auth) {

        User hr = userService.findByEmail(auth.getName());
        List<Job> jobs = jobService.getJobsByHR(hr);

        model.addAttribute("jobs", jobs);

        return "hr/view-jobs";
    }

    /**
     * Show ranked candidates for a specific job
     * Candidates sorted by ATS score highest first
     */
    @GetMapping("/ranked-candidates/{jobId}")
    public String rankedCandidates(@PathVariable Long jobId,
            Model model,
            Authentication auth) {

        User hr = userService.findByEmail(auth.getName());
        Job job = jobService.getJobById(jobId);
        List<Application> applications = applicationRepo.findByJobOrderByAtsScoreDesc(job);

        // Unread messages count for nav
        long unreadMessages = messageService.getUnreadCount(hr);

        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("unreadMessages", unreadMessages);

        return "hr/ranked-candidates";
    }

    /**
     * Delete a job posting
     */
    @GetMapping("/delete-job/{jobId}")
    public String deleteJob(@PathVariable Long jobId,
            RedirectAttributes ra) {

        jobService.deleteJob(jobId);

        ra.addFlashAttribute("success", "Job deleted successfully!");
        return "redirect:/hr/view-jobs";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Long appId,
            @RequestParam String status,
            @RequestParam Long jobId,
            RedirectAttributes ra) {
        Application app = applicationRepo.findById(appId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setStatus(status);
        applicationRepo.save(app);
        ra.addFlashAttribute("success", "Status updated successfully!");
        return "redirect:/hr/ranked-candidates/" + jobId;
    }
}