package com.sixhands.controller;

import com.sixhands.controller.dtos.ProjectAndUserExpDTO;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.repository.ProjectRepository;
import com.sixhands.repository.UserRepository;
import com.sixhands.service.ProjectService;
import com.sixhands.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private ProjectRepository projectRepo;
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
        ProjectAndUserExpDTO[] projectExps = userService.getProjectExpForUser(user).stream()
                .map((ue)->new ProjectAndUserExpDTO(projectRepo.getOne(ue.getProject_uuid()),ue))
                .toArray(ProjectAndUserExpDTO[]::new);
        model.addAttribute("user",user);
        model.addAttribute("userData", userService.getProfileDtoForUser(user).toString() );
        model.addAttribute("canEdit",canEdit);
        model.addAttribute("projects",projectExps);
        return edit == 1 ? "edit-user-profile" : "user-profile";
    }
    @PutMapping
    public String updateUserData(@ModelAttribute User editUser){

        User curUser = userService.loadUserByUsername( UserService.getCurrentUsername().orElseThrow(()->new RequestRejectedException("User is not logged in")) );

        curUser.safeAssignProperties(editUser);
        userRepo.save(curUser);

        return "redirect:/user/me";
    }


}