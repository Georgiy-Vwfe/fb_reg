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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

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
    private Long userId = 0L;

    @GetMapping("/me")
    public String getMeUser(Model model) {
        User curUser = userService.loadUserByUsername(UserService.getCurrentUsername().orElse(null));
        if (curUser == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not logged in");
        return "redirect:/user/" + curUser.getUuid();
    }

    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id, @RequestParam(defaultValue = "0", required = false) Integer edit, Model model, Locale locale) {
        userId = id;
        User user = userRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + id + " is not found"));
        boolean canEdit = false;
        User curUser = null;
        try {
            curUser = userService.loadUserByUsername(UserService.getCurrentUsername().orElse(null));
            canEdit = curUser.getUuid().equals(user.getUuid());
        } catch (Exception ignored) {
        }
        User finalCurUser = curUser;

        String currentUserImgPath = finalCurUser.getUser_img();
        if (currentUserImgPath == null || currentUserImgPath.equals("")) {
            finalCurUser.setUser_img("https://i.imgur.com/ahcplHm.png");
        }

        ProjectAndUserExpDTO[] projectAndExps = userService.getProjectExpForUser(user).stream()
                .map((ue) -> {
                    Project proj = projectRepo.getOne(ue.getProject_uuid());
                    int rating = userService.getRatingForProject(proj);
                    boolean likedByUser = finalCurUser != null && proj.getLikedUserIDs().contains(finalCurUser.getUuid());
                    return new ProjectAndUserExpDTO(proj, ue, rating, likedByUser);
                })
                .sorted((a, b) -> (int) b.getProject().getCreated().getTime() - (int) a.getProject().getCreated().getTime())
                .toArray(ProjectAndUserExpDTO[]::new);

        model.addAttribute("cur_user", finalCurUser);
        model.addAttribute("user", user);
        model.addAttribute("userData", userService.getProfileDtoForUser(user));
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("projects", projectAndExps);
        return edit == 1 ? "edit-user-profile" : "project-not-aproved";
    }

    @PutMapping
    public String updateUserData(@ModelAttribute User editUser) {
        userService.safeAssignPersist(editUser, userService.getCurUserOrThrow());
        return "redirect:/user/me";
    }

    @GetMapping("/contact-offer")
    public String contactOffer(Locale locale) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + userId + " is not found"));
        User curUser = userService.getCurUserOrThrow();
        try {
            if (!user.getEmail().equals(curUser.getEmail())) {
                userService.sendUserContactsMail(user, curUser, locale);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }
}