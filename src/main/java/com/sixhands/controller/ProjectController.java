package com.sixhands.controller;

import com.sixhands.controller.dtos.ProjectDTO;
import com.sixhands.controller.dtos.UserAndExpDTO;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;
import com.sixhands.repository.ProjectRepository;
import com.sixhands.repository.UserProjectExpRepository;
import com.sixhands.repository.UserRepository;
import com.sixhands.service.ProjectService;
import com.sixhands.service.SheetService;
import com.sixhands.service.UserService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
    @Autowired
    private SheetService sheetService;

    private Map<Long, ProjectDTO> editedProjects = new HashMap<>();

/*    private User getCurUser() {
        return userService.loadUserByUsername(UserService.getCurrentUsername()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is unauthorized")));
    }*/

    @GetMapping(value = "/{id}/edit", params = {"as=creator"})
    public String getEditProjectCreator(Model model, @PathVariable int id) {
        User curUser = userService.getCurUserOrThrow();
        //Get all projects that are created by current user
        Project[] projects = projectService.findProjectsByUser(curUser, true);
        //leave project that matches {id} from request path
        projects = Arrays.stream(projects).filter((p) -> p.getUuid() == id).toArray(Project[]::new);
        if (projects.length == 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User did not create this project or project was not found");
        ProjectDTO projectDTO = projectService.projectDTOFromProject(projects[0], curUser);
        model.addAttribute("isEditing", true);
        model.addAttribute("projectDTO", projectDTO);
        model.addAttribute("user", curUser);
        model.addAttribute("cur_user", curUser);
        return "save-project";
    }

    @GetMapping(value = "/{id}/like")
    public String likeProject(@PathVariable int id, HttpServletRequest request) {
        User curUser = userService.getCurUserOrThrow();
        //Get all projects that are created by current user
        Project project = projectRepo.getOne((long) id);
        project.likeByUser(curUser);
        projectRepo.save(project);
        try {
            //https://stackoverflow.com/a/1525689
            URL referer = new URL(request.getHeader("Referer"));
            return "redirect:" + referer;
        } catch (MalformedURLException e) {
            return "redirect:/";
        }
    }

    @GetMapping(value = "/{id}/edit", params = {"as=member"})
    public String getEditProjectMember(Model model, @PathVariable int id) {
        User curUser = userService.getCurUserOrThrow();
        //Get all projects that are created by current user
        Project[] projects = projectService.findProjectsByUser(curUser, false);
        //leave project that matches {id} from request path
        projects = Arrays.stream(projects).filter((p) -> p.getUuid() == id).toArray(Project[]::new);
        if (projects.length == 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a member of this project or project was not found");
        ProjectDTO projectDTO = projectService.projectDTOFromProject(projects[0], curUser);
        if (projectDTO.getMember().getUserExp().isProject_creator())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User cannot edit this project as a member");
        model.addAttribute("isEditing", true);
        model.addAttribute("projectDTO", projectDTO);
        return "save-project";
    }

    @DeleteMapping("/{id}/delete")
    public String deleteProject(@PathVariable int id, HttpServletRequest request) {
        User curUser = userService.getCurUserOrThrow();
        //Get all projects that are created by current user
        Project[] projects = projectService.findProjectsByUser(curUser, true);
        //leave project that matches {id} from request path
        projects = Arrays.stream(projects).filter((p) -> p.getUuid() == id).toArray(Project[]::new);
        if (projects.length == 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User did not create this project or project was not found");

        Project project = projects[0];
        //if (project.isConfirmed())
            //throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unable to delete confirmed project");
            projectService.deleteProject(project);
        try {
            //https://stackoverflow.com/a/1525689
            URL referer = new URL(request.getHeader("Referer"));
            return "redirect:" + referer;
        } catch (MalformedURLException e) {
            return "redirect:/";
        }
    }

    @GetMapping("/create")
    public String createProject(Model model) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.addNewMember();
        User curUser = userService.getCurUserOrThrow();
        model.addAttribute("user", curUser);
        model.addAttribute("cur_user", curUser);
        model.addAttribute("projectDTO", projectDTO);
        model.addAttribute("isEditing", false);
        model.addAttribute("roleEnum", UserProjectExp.Role.values());
        model.addAttribute("industryEnum", UserProjectExp.Industry.values());
        return "save-project";
    }

    @RequestMapping(value = "/save", params = {"action=add-member"}, method = {RequestMethod.PUT, RequestMethod.POST})
    public String addMember(@ModelAttribute ProjectDTO projectDTO, Model model, HttpServletRequest request) {
        projectDTO.addNewMember();
        model.addAttribute("projectDTO", projectDTO);
        model.addAttribute("isEditing", request.getMethod().equalsIgnoreCase("PUT"));
        return "save-project";
    }

    @RequestMapping(value = "/save", params = {"action=delete-member"}, method = {RequestMethod.PUT, RequestMethod.POST})
    public String deleteMember(@ModelAttribute ProjectDTO projectDTO, Model model, @RequestParam Integer index, HttpServletRequest request) {
        projectDTO.deleteMember(index);
        model.addAttribute("projectDTO", projectDTO);
        model.addAttribute("isEditing", request.getMethod().equalsIgnoreCase("PUT"));
        return "save-project";
    }

    @Transactional
    @PutMapping(value = "/save", params = {"action=persist"})
    public String updateProject(@ModelAttribute ProjectDTO projectDTO, Locale locale) {
        //FIXME: Error when adding/removing members
        User curUser = userService.getCurUserOrThrow();
        Project curProject = projectRepo.getOne(projectDTO.getProject().getUuid());
        Optional<UserAndExpDTO> projectExp = projectService.userAndExpByUser(curProject, curUser);
        if (!projectExp.isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a member of this project");
        projectService.updateProject(projectDTO, projectExp.get().getUserExp().isProject_creator(), locale);
        return "redirect:/user/me";
    }

    @PostMapping(value = "/save", params = {"action=persist"})
    public String saveProject(@ModelAttribute ProjectDTO projectDTO) {
        projectService.saveNewProject(projectDTO, userService.getCurUserOrThrow(), Locale.getDefault());
        return "redirect:/user/me";
    }

    //TODO: Import all fields - https://imgur.com/1zenJ1p.jpg, assign first and surname to new users
    @PostMapping("/import")
    @Transactional
    public String importProjects(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        if (multipartFile == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is null");

        XSSFWorkbook xssfWorkbook = null;

        try (InputStream input = multipartFile.getInputStream()) {
            try {
                xssfWorkbook = new XSSFWorkbook(input);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type (xlsx is required)");
            }
        }

        List<ProjectDTO> imported = sheetService.parseSheet(xssfWorkbook.getSheetAt(0));

        imported.forEach(this::saveProject);
        //System.out.println( new JSONArray( imported ).toString(2) );

        return "redirect:/user/me";
    }
}
