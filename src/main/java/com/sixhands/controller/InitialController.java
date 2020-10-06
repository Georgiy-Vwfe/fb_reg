package com.sixhands.controller;

import com.sixhands.controller.dtos.EditUserSaveProjectDTO;
import com.sixhands.controller.dtos.ProjectDTO;
import com.sixhands.controller.dtos.UserProfileDTO;
import com.sixhands.domain.User;
import com.sixhands.service.ProjectService;
import com.sixhands.service.UserService;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class InitialController {
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/")
    public String index(Model model) {
        if (UserService.getCurrentUsername().isPresent()) model.addAttribute("isAuthenticated", true);
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
        if (session != null) {
            session.invalidate();
            resp.put("message", "success");
        } else resp.put("error", "user is already logged in");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/project-not-aproved")
    public String projectNotAproved() {
        return "project-not-aproved";
    }

    @GetMapping("/forget-password")
    public ModelAndView forgetPassword() {
        return new ModelAndView("forget-password");
    }

    //FIXME: Messages are not displayed
    @PostMapping("/forget-password")
    public ModelAndView sendRecoverMail(ModelAndView modelAndView, @RequestParam("email") String userEmail, HttpServletRequest request) {
        Optional<User> optional = userService.findUserByUsername(userEmail);

        if (!optional.isPresent()) {
            modelAndView.addObject("errorMessage", "We didn't find an account for that e-mail address.");
        } else {
            User user = optional.get();
            user.setResetToken(UUID.randomUUID().toString());

            userService.saveUser(user);
            if (userService.sendRecoverMail(user, request)) {
                modelAndView.addObject("successMessage", "A password reset link has been sent to " + userEmail);
            }
        }

        modelAndView.setViewName("forget-password");
        return modelAndView;
    }

    @GetMapping("/recovery-password")
    public ModelAndView recoveryPassword(ModelAndView modelAndView, @RequestParam String token) {

        Optional<User> user = userService.findUserByResetToken(token);
        System.out.println(user.isPresent());
//        modelAndView.addObject("user", new User());
        if (user.isPresent()) {
            modelAndView.addObject("resetToken", token);
        } else {
            modelAndView.addObject("errorMessage", "Oops! This is an invalid password reset link.");
        }

        modelAndView.setViewName("recovery-password");
        return modelAndView;
    }

    @PostMapping("/recovery-password")
    public ModelAndView recoverPassword(ModelAndView modelAndView, @RequestParam Map<String, String> requestParams,  RedirectAttributes redir) {
        System.out.println("recover password call");
        Optional<User> user = userService.findFirstUserByResetToken(requestParams.get("token"));
        System.out.println(user.isPresent());
        System.out.println(requestParams);
        if (user.isPresent()) {
            User resetUser = user.get();

            resetUser.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));
            resetUser.setResetToken(null);
            userService.saveUser(resetUser);

            redir.addFlashAttribute("successMessage", "You have successfully reset your password. You may now login.");

            modelAndView.setViewName("redirect:login");
            return modelAndView;
        } else {
            modelAndView.addObject("errorMessage", "Oops! This is an invalid password reset link.");
            modelAndView.setViewName("recovery-password");
        }
        return modelAndView;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ModelAndView handleMissingParams(MissingServletRequestParameterException ex) {
        return new ModelAndView("redirect:login");
    }

    @GetMapping("/admin-profile-project")
    public String adminProfileProject() {
        return "admin-profile-project";
    }

    @GetMapping("/test-import")
    public String testImport() {
        return "test-import";
    }

    @GetMapping("/search")
    public String search(Model model,
                         @RequestParam(required = false) String industry,
                         @RequestParam(required = false) String role,
                         @RequestParam(required = false) String name) {
        List<UserProfileDTO> users = userService.searchUsersByProps(null, null, industry, null, role);
        users = userService.searchUsersByName(users, name);
        model.addAttribute("profileDTOs", users);
        return "search";
    }

    @GetMapping("/admin-token")
    public String adminPanelByToken() {
        return "admin-token-request";
    }

    //#region edit-user/save-project
    @GetMapping("/edit-user-save-project")
    public String adminProfileProject(Model model) {
        model.addAttribute("editUserSaveProjectDTO", new EditUserSaveProjectDTO(userService.getCurUserOrThrow(), new ProjectDTO()));
        model.addAttribute("isEditing", false);
        return "edit-user-save-project";
    }

    @RequestMapping(value = "/edit-user-save-project", params = {"action=add-member"}, method = {RequestMethod.PUT, RequestMethod.POST})
    public String addMember(@ModelAttribute EditUserSaveProjectDTO dto, Model model, HttpServletRequest request) {
        dto.getProjectDTO().addNewMember();
        model.addAttribute("editUserSaveProjectDTO", dto);
        model.addAttribute("isEditing", false);
        return "edit-user-save-project";
    }

    @RequestMapping(value = "/edit-user-save-project", params = {"action=delete-member"}, method = {RequestMethod.PUT, RequestMethod.POST})
    public String deleteMember(@ModelAttribute EditUserSaveProjectDTO dto, Model model, @RequestParam Integer index, HttpServletRequest request) {
        dto.getProjectDTO().deleteMember(index);
        model.addAttribute("editUserSaveProjectDTO", dto);
        model.addAttribute("isEditing", false);
        return "edit-user-save-project";
    }

    @PutMapping("/edit-user-save-project")
    public String persistEditUserSaveProjectForms(@ModelAttribute EditUserSaveProjectDTO dto) {
        User curUser = userService.getCurUserOrThrow();
        if (!StringUtils.isEmpty(dto.getProjectDTO().getProject().getName()))
            projectService.saveNewProject(dto.getProjectDTO(), curUser);
        userService.safeAssignPersist(dto.getUser(), curUser);
        return "redirect:/user/me";
    }
    //#endregion
}
