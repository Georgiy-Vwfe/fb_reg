package com.sixhands.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class InitialController {

    @GetMapping("/test")
    public String test(){
        return "test";
    }
}
