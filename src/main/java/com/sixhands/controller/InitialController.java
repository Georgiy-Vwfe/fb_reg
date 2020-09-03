package com.sixhands.controller;

import com.sixhands.domain.Greeting;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    private UserService userService;

    @GetMapping("/current-user")
    public ResponseEntity<Map<String,Object>> getCurUser() {
        Map<String, Object> response = new HashMap<>();
        response.put("username",UserService.getCurrentUsername().get());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/forget-me")
    public ResponseEntity<Map<String, Object>> forgetMe(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        SecurityContextHolder.clearContext();
        Map<String, Object> resp = new HashMap<>();
        if(session != null) {
            session.invalidate();
            resp.put("message","success");
        }else resp.put("error","user is already logged in");
        return ResponseEntity.ok(resp);
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
    public String recoveryPassword(Model model){
        model.addAttribute("greeting", new Greeting());
        return "recovery-password";
    }
}
