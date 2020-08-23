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

    @GetMapping("/sign-in")
    public String signIn() {
        return "sign-in";
    }

    @GetMapping("/forget-password")
    public String forgetPassword() {
        return "forget-password";
    }

    @GetMapping("/admin-profile-project")
    public String adminProfileProject() {
        return "admin-profile-project";
    }

    @GetMapping("/recovery-password")
    public String recoveryPassword(){
        return "recovery-password";
    }
}
