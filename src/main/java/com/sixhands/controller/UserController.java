package com.sixhands.controller;

import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;
import com.sixhands.repository.UserRepository;
import com.sixhands.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepo;
    @GetMapping("/me")
    public String getMeUser(Model model){
        User curUser = userService.loadUserByUsername(UserService.getCurrentUsername().orElse(null));
        if(curUser == null) return "";
        return "redirect:/user/"+curUser.getUuid();
    }
    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id, @RequestParam(defaultValue = "0",required = false) Integer edit, Model model){
        User user = userRepo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User with id "+id+" is not found"));
        boolean canEdit = false;
        try {
            User curUser = userService.loadUserByUsername(UserService.getCurrentUsername().orElse(null));
            canEdit = curUser.getUuid().equals(user.getUuid());
        }catch (Exception ignored){}
        UserProjectExp[] projectExps = userService.getProjectExpForUser(user).toArray(new UserProjectExp[0]);
        model.addAttribute("user",user);
        model.addAttribute("canEdit",canEdit);
        model.addAttribute("projects",projectExps);
        return edit == 1 ? "edit-user-profile" : "user-profile";
    }
    @PutMapping
    public String updateUserData(@ModelAttribute User editUser){

        User curUser = userService.loadUserByUsername( UserService.getCurrentUsername().orElseThrow(()->new RequestRejectedException("User is not logged in")) );

        curUser.setCountry(editUser.getCountry());
        curUser.setCity(editUser.getCity());

        curUser.setDate_of_birth(editUser.getDate_of_birth());

        curUser.setFirst_name(editUser.getFirst_name());
        curUser.setLast_name(editUser.getLast_name());

        curUser.setSocial_networks(editUser.getSocial_networks());
        curUser.setUser_img(editUser.getUser_img());
        curUser.setAbout_user(editUser.getAbout_user());

        curUser.setEmail(editUser.getEmail());
        curUser.setPhone_number(editUser.getPhone_number());
        //TODO: Parse&validate date of birth
        curUser.setDate_of_birth(editUser.getDate_of_birth());

        //curUser.setPassword(editUser.getPassword());

        userRepo.save(curUser);

        return "redirect:/user/me";
    }
}
