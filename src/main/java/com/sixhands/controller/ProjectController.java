package com.sixhands.controller;

import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.repository.ProjectRepository;
import com.sixhands.repository.UserProjectExpRepository;
import com.sixhands.repository.UserRepository;
import com.sixhands.service.ProjectService;
import com.sixhands.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
    @Autowired
    private ProjectRepository projectRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserProjectExpRepository userProjectExpRepo;

    private Map<Long,ProjectDTO> editedProjects = new HashMap<>();
    private User getCurUser(){
        return userService.loadUserByUsername(UserService.getCurrentUsername()
                .orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User is unauthorized")));
    }
    @GetMapping("/{id}/edit")
    public String getEditProject(Model model, @PathVariable int id){
        User curUser = getCurUser();
        //Get all projects that are created by current user
        Project[] projects = projectService.findProjectsCreatedByUser(curUser);
        //leave project that matches {id} from request path
        projects = Arrays.stream(projects).filter((p)-> p.getUuid() == id).toArray(Project[]::new);
        if(projects.length == 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User did not create this project or project was not found");
        model.addAttribute("isEditing",true);
        model.addAttribute("projectDTO",projectService.updateProjectDTOFromProject(projects[0],curUser));
        return "save-project";
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
    @Transactional
    @PutMapping(value = "/save", params = {"action=persist"})
    public String updateProject(@ModelAttribute ProjectDTO projectDTO){
        //FIXME: Error when adding/removing members
        projectService.updateProject(projectDTO);
        return "redirect:/user/me";
    }
    @PostMapping(value = "/save", params = {"action=persist"})
    public String saveProject(@ModelAttribute ProjectDTO projectDTO){
        projectService.saveNewProject(projectDTO,getCurUser());
        return "redirect:/user/me";
    }
}
