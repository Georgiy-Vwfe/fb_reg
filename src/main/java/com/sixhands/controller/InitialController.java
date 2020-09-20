package com.sixhands.controller;

import com.sixhands.domain.User;
import com.sixhands.repository.UserRepository;
import com.sixhands.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class InitialController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepo;

    private User tmpUser;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/")
    public String index(Model model) {
        if (UserService.getCurrentUsername().isPresent()) model.addAttribute("isAuthenticated", true);
        return "index";
    }

    //TODO: ?Display error for unverified users
    @GetMapping("/login")
    public String signIn() {
        return "login";
    }

    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurUser() {
        Map<String, Object> response = new HashMap<>();
        response.put("username", UserService.getCurrentUsername().get());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/forget-me")
    public ResponseEntity<Map<String, Object>> forgetMe(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        SecurityContextHolder.clearContext();
        Map<String, Object> resp = new HashMap<>();
        if (session != null) {
            session.invalidate();
            resp.put("message", "success");
        } else resp.put("error", "user is already logged in");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/project-not-aproved")
    public String projectNotAproved() {
        return "project-not-aproved";
    }

    @GetMapping("/forget-password")
    public String forgetPassword(Model model) {
        model.addAttribute("user", new User());
        return "forget-password";
    }

    @PostMapping("/forget-password")
    public String sendRecoverMail(@ModelAttribute User user) {
        this.tmpUser = user;
        userService.sendRecoverMail(user);
        System.out.println(user.toString());
        return "redirect:/";
    }

    @GetMapping("/admin-profile-project")
    public String adminProfileProject() {
        return "admin-profile-project";
    }

    @GetMapping("/recovery-password")
    public String recoveryPassword(Model model) {
        model.addAttribute("user", new User());
        return "recovery-password";
    }

    @PostMapping("/recovery-password")
    public String recoverPassword(@ModelAttribute User user, Model model, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "recovery-password";
        if (user.getPassword().equals(user.getConfirmPassword())) {
            model.addAttribute("passNotEquals", "passwords isn't equals");
            return "recovery-password";
        }

        String password = user.getPassword();
        user = userService.loadUserByUsername(tmpUser.getUsername());
        //user.safeAssignProperties(tmpUser);
        user.setPassword(userService.encodePassword(password));
        userRepo.save(user);
        return "redirect:/login";
    }

    @GetMapping("/test-import")
    public String testImport() {
        return "test-import";
    }
}
