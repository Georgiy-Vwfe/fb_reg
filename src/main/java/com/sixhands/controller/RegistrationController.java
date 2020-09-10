package com.sixhands.controller;

import com.sixhands.domain.User;
import com.sixhands.exception.UserAlreadyExistsException;
import com.sixhands.repository.UserRepository;
import com.sixhands.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Controller
public class RegistrationController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/registration")
    public String greetingForm(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@ModelAttribute User user, Model model, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) return "registration";
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            model.addAttribute("passNotEquals", "passwords isn't equals");
            return "registration";
        }

        String email = user.getEmail();
        String password = user.getPassword();
        try { userService.registerUser(email, password); }
        catch(UserAlreadyExistsException e){
            model.addAttribute("usernameError", "A user with the same name already exists.");
            return "redirect:/";
        }

        try {
            request.login(email, password);
        } catch (ServletException e) {
            // log.debug("Autologin fail", e);
        }
        return "redirect:/"
               //"admin-profile-project"
               ;
    }
    
    @GetMapping("/activation/{code}")
    public String activate(Model model, @PathVariable String code) {
        boolean isActivated = userService.activateUser(code);

        if (isActivated) {
            model.addAttribute("message", "User activated");
        } else {
            model.addAttribute("message", "User is not activated");
        }
        return "redirect:/"
               //"admin-profile-project"
               ;
    }
}
