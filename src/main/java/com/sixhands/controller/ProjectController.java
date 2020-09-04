package com.sixhands.controller;

import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;
import com.sixhands.service.ProjectService;
import com.sixhands.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Controller
@RequestMapping("/project")
public class ProjectController {
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;

    private Map<Long,ProjectDTO> editedProjects = new HashMap<>();
    private User getCurUser(){
        return userService.loadUserByUsername(UserService.getCurrentUsername()
                .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User is unauthorized")));
    }

    @GetMapping("/create")
    public String createProject(Model model) {
        model.addAttribute("projectDTO",new ProjectDTO());
        return "save-project";
    }
    @PostMapping(value = "/save", params = {"action=add-member"})
    public String addMember(@ModelAttribute ProjectDTO projectDTO, Model model) {
        projectDTO.addNewMember();
        model.addAttribute("projectDTO",projectDTO);
        return "save-project";
    }
    @PostMapping(value = "/save", params = {"action=delete-member"})
    public String deleteMember(@ModelAttribute ProjectDTO projectDTO, Model model, @RequestParam Integer index){
        projectDTO.deleteMember(index);
        model.addAttribute("projectDTO",projectDTO);
        return "save-project";
    }
    @PostMapping(value = "/save", params = {"action=persist"})
    public String saveProject(@ModelAttribute ProjectDTO projectDTO){
        projectService.saveNewProject(projectDTO,getCurUser());
        return "redirect:/user/me";
    }

}
