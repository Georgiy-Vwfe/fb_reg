package com.sixhands.controller;

import com.sixhands.controller.dtos.EditUserSaveProjectDTO;
import com.sixhands.controller.dtos.ProjectDTO;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.service.ProjectService;
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
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;

    @GetMapping("/")
    public String index(Model model) {
        if(UserService.getCurrentUsername().isPresent()) model.addAttribute("isAuthenticated",true);
        return "index";
    }
    //TODO: ?Display error for unverified users
    @GetMapping("/login")
    public String signIn() {
        return "login";
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
    @GetMapping("/project-not-aproved")
    public String projectNotAproved() {
        return "project-not-aproved";
    }

    @GetMapping("/forget-password")
    public String forgetPassword() {
        return "forget-password";
    }

    @GetMapping("/recovery-password")
    public String recoveryPassword(Model model){
        model.addAttribute("user",new User());
        return "recovery-password";
    }

    @GetMapping("/test-import")
    public String testImport(){
        return "test-import";
    }

    @GetMapping("/edit-user-save-project")
    public String adminProfileProject(Model model) {
        model.addAttribute("editUserSaveProjectDTO",new EditUserSaveProjectDTO(userService.getCurUserOrThrow(),new ProjectDTO()));
        model.addAttribute("isEditing",false);
        return "edit-user-save-project";
    }
    @RequestMapping(value = "/edit-user-save-project", params = {"action=add-member"}, method = {RequestMethod.PUT,RequestMethod.POST})
    public String addMember(@ModelAttribute EditUserSaveProjectDTO dto, Model model, HttpServletRequest request) {
        dto.getProjectDTO().addNewMember();
        model.addAttribute("editUserSaveProjectDTO",dto);
        model.addAttribute("isEditing",false);
        return "edit-user-save-project";
    }
    @RequestMapping(value = "/edit-user-save-project", params = {"action=delete-member"}, method = {RequestMethod.PUT,RequestMethod.POST})
    public String deleteMember(@ModelAttribute EditUserSaveProjectDTO dto, Model model, @RequestParam Integer index, HttpServletRequest request){
        dto.getProjectDTO().deleteMember(index);
        model.addAttribute("editUserSaveProjectDTO",dto);
        model.addAttribute("isEditing",false);
        return "edit-user-save-project";
    }
    @PutMapping("/edit-user-save-project")
    public String persistEditUserSaveProjectForms(@ModelAttribute EditUserSaveProjectDTO dto){
        User curUser = userService.getCurUserOrThrow();
        projectService.saveNewProject(dto.getProjectDTO(),curUser);
        userService.safeAssignPersist(dto.getUser(),curUser);
        return "redirect:/user/me";
    }
}
