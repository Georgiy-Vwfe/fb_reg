package com.sixhands.controller;

import com.sixhands.domain.Greeting;
import com.sixhands.domain.User;
import com.sixhands.repository.UserRepository;
import com.sixhands.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class RegistrationController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/register")
    public String greetingForm(Model model) {
        model.addAttribute("greeting", new Greeting());
        return "register";
    }

    @PostMapping("/register")
    public String greetingSubmit(@ModelAttribute Greeting greeting, Model model, User user) {
        Greeting userFromDB = userRepository.findByEmail(greeting.getId());
        if (userFromDB != null) {
           model.addAttribute("greeting", greeting);
           return "register";
        }
        String encoded = new BCryptPasswordEncoder().encode(greeting.getContent());

        user.setEmail(greeting.getId());
        user.setPassword(encoded);
        userRepository.save(user);
        return "admin-profile-project";
    }
    
    /*@PostMapping("/register")
    public String addUser(User user, Model model) {
        if (!userService.addUser(user)) {

            return "register";
        }

        return "redirect:/admin-profile-project";
    }*/

    @GetMapping("/activation/{code}")
    public String activate(Model model, @PathVariable String code) {
        boolean isActivated = userService.activateUser(code);

        if (isActivated) {
            model.addAttribute("message", "User activated");
        } else {
            model.addAttribute("message", "User is not activated");
        }
        return "admin-profile-project";
    }
}
