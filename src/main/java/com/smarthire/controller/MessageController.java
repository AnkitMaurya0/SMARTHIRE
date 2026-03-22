package com.smarthire.controller;

import com.smarthire.model.Job;
import com.smarthire.model.Message;
import com.smarthire.model.User;
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

@Controller
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @GetMapping("/inbox")
    public String inbox(Model model, Authentication auth) {
        User currentUser = userService.findByEmail(auth.getName());
        List<User> contacts = messageService.getContactList(currentUser);
        long unreadCount = messageService.getUnreadCount(currentUser);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("contacts", contacts);
        model.addAttribute("unreadCount", unreadCount);
        return "messages/inbox";
    }

    @GetMapping("/chat/{userId}")
    public String chat(@PathVariable Long userId,
            @RequestParam(required = false) Long jobId,
            Model model, Authentication auth) {
        User currentUser = userService.findByEmail(auth.getName());
        User otherUser = userService.findById(userId);
        if (otherUser == null)
            return "redirect:/messages/inbox";
        messageService.markAsRead(currentUser, otherUser);
        List<Message> messages = messageService.getConversation(currentUser, otherUser);
        Job job = null;
        if (jobId != null)
            job = jobService.getJobById(jobId);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherUser", otherUser);
        model.addAttribute("messages", messages);
        model.addAttribute("job", job);
        model.addAttribute("jobId", jobId);
        return "messages/chat";
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam Long receiverId,
            @RequestParam String content,
            @RequestParam(required = false) String meetLink,
            @RequestParam(required = false) Long jobId,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            User sender = userService.findByEmail(auth.getName());
            User receiver = userService.findById(receiverId);
            Job job = null;
            if (jobId != null)
                job = jobService.getJobById(jobId);
            messageService.sendMessage(sender, receiver, content, meetLink, job);
            ra.addFlashAttribute("success", "Message sent!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to send message.");
        }
        return "redirect:/messages/chat/" + receiverId +
                (jobId != null ? "?jobId=" + jobId : "");
    }
}