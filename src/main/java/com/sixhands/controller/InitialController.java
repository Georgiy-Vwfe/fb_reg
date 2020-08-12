package com.sixhands.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class InitialController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/register")
    public String register() {
        return "register";
    }

    @RequestMapping("/sign-in")
    public String signIn() {
        return "sign-in";
    }

    @RequestMapping("/forget-password")
    public String forgetPassword() {
        return "forget-password";
    }
    @RequestMapping("/admin-profile-project")
    public String adminProfileProject(){
        return "admin-profile-project";
    }
}
