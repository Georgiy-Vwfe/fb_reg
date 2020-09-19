package com.sixhands.controller;

import com.sixhands.controller.dtos.EditUserSaveProjectDTO;
import com.sixhands.controller.dtos.ProjectAndUserExpDTO;
import com.sixhands.controller.dtos.ProjectDTO;
import com.sixhands.domain.Notification;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.repository.ProjectRepository;
import com.sixhands.repository.UserRepository;
import com.sixhands.service.ProjectService;
import com.sixhands.service.SheetService;
import com.sixhands.service.UserService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

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
        if(curUser == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User is not logged in");
        return "redirect:/user/"+curUser.getUuid();
    }

    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id, @RequestParam(defaultValue = "0",required = false) Integer edit, Model model){
        User user = userRepo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id "+id+" is not found"));
        boolean canEdit = false;
        User curUser = null;
        try {
            curUser = userService.loadUserByUsername(UserService.getCurrentUsername().orElse(null));
            canEdit = curUser.getUuid().equals(user.getUuid());
        }catch (Exception ignored){}
        User finalCurUser = curUser;
        ProjectAndUserExpDTO[] projectAndExps = userService.getProjectExpForUser(user).stream()
                .map((ue)-> {
                    Project proj = projectRepo.getOne(ue.getProject_uuid());
                    int rating = userService.getRatingForProject(proj);
                    boolean likedByUser = finalCurUser != null && proj.getLikedUserIDs().contains(finalCurUser.getUuid());
                    return new ProjectAndUserExpDTO(proj, ue, rating, likedByUser);
                })
                .sorted((a,b) -> (int)b.getProject().getCreated().getTime()-(int)a.getProject().getCreated().getTime() )
                .toArray(ProjectAndUserExpDTO[]::new);

        model.addAttribute("user", user);
        model.addAttribute("userData", userService.getProfileDtoForUser(user) );
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("projects", projectAndExps);
        return edit == 1 ? "edit-user-profile" : "project-not-aproved";
    }
    @PutMapping
    public String updateUserData(@ModelAttribute User editUser){
        userService.safeAssignPersist(editUser,userService.getCurUserOrThrow());
        return "redirect:/user/me";
    }
}