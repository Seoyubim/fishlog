package com.fishlog.fishlog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "redirect:/logs";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "auth/signup";
    }

    @GetMapping("/logs")
    public String logList() {
        return "fishlogs/list";
    }

    @GetMapping("/logs/new")
    public String logCreate() {
        return "fishlogs/create";
    }

    @GetMapping("/logs/{id}")
    public String logDetail(@PathVariable Long id, Model model) {
        model.addAttribute("logId", id);
        return "fishlogs/detail";
    }

    @GetMapping("/stats")
    public String stats() {
        return "stats/annual";
    }
}
