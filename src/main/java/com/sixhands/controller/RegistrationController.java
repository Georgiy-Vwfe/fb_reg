package com.sixhands.controller;

import com.sixhands.domain.User;
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
    // @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;


    @GetMapping("/registration")
    public String greetingForm(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@ModelAttribute User user, Model model,
                          BindingResult bindingResult,
                          HttpServletRequest request) {

        String username = user.getUsername();
        String password = user.getPassword();
        Optional<User> userFromDB = userRepository.findByEmail(user.getEmail());
        if (bindingResult.hasErrors()) {
            return "registration";

        } else if (!user.getPassword().equals(user.getConfirmPassword())) {
            model.addAttribute("passNotEquals", "passwords isn't equals");
            return "registration";
        } else if (userFromDB.isPresent()) {
            model.addAttribute("usernameError", "A user with the same name already exists.");
            return "registration";
        } else {
            String encoded = new BCryptPasswordEncoder().encode(user.getPassword());
            user.setPassword(encoded);
            userRepository.save(user);
        }

        try {
            request.login(username, password);
        } catch (ServletException e) {
            // log.debug("Autologin fail", e);
        }
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
