package com.sixhands.controller;

import com.sixhands.domain.Project;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/project")
public class ProjectController {
    @GetMapping("/create")
    public String createProject(Model model) {
        model.addAttribute("project",new Project());
        return "save-project";
    }

    @PostMapping("/save")
    public String saveProject(@ModelAttribute Project project){
        //TODO: Save new or update existing project
        System.out.println("name: "+project.getName()+", description: "+project.getDescription());
        return "redirect:/user/me";
    }

}
