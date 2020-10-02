package com.sixhands.controller;

import com.sixhands.controller.dtos.ProjectDTO;
import com.sixhands.controller.dtos.UserAndExpDTO;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.misc.GenericUtils;
import com.sixhands.repository.ProjectRepository;
import com.sixhands.repository.UserRepository;
import com.sixhands.service.ProjectService;
import com.sixhands.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Controller
@RequestMapping("/admin/{token}")
public class AdminController {
    @Autowired
    private ProjectRepository projectRepo;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserRepository userRepo;
    @GetMapping
    public String adminIndex(){
        return "admin-index";
    }
    public AdminController(){
        exportedDir.mkdir();
    }

    //TODO: Refactor, too much code
    private String getProjectsCSV(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < projectRepo.count(); i++) {
            Project project = GenericUtils.initializeAndUnproxy( projectRepo.findAll().get(i) );
            UserAndExpDTO[] userAndExpDTO = GenericUtils.initializeAndUnproxy( projectService.projectExpByProject(project) );
            ProjectDTO projectDTO = new ProjectDTO();
            projectDTO.setProject(project);
            projectDTO.setMembers(userAndExpDTO);
            stringBuilder.append(projectDTO.toCsvDto().toString(i == 0)).append(i==projectRepo.count()-1?"":"\n");
        }
        return "sep=,\n"+stringBuilder;
    }
    private String getUsersCSV(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < userRepo.count(); i++) {
            User user = GenericUtils.initializeAndUnproxy( userRepo.findAll().get(i) );
            if(i==0) stringBuilder.append( String.join(",", user.toCSV().keySet()) ).append("\n");
            stringBuilder.append( String.join(",", user.toCSV().values()) ).append(i==userRepo.count()-1?"":"\n");
        }
        return "sep=,\n"+stringBuilder;
    }
    private File exportedDir = new File("6hands-exported");
    //TODO: Refactor, too much code
    @GetMapping("/csv/projects")
    public ResponseEntity<Resource> serveProjects() throws IOException {
        File csvFile = new File(exportedDir.getAbsolutePath()+File.separator+"projects.csv");
        Files.write(csvFile.toPath(),getProjectsCSV().getBytes(StandardCharsets.UTF_8));
        Resource file = new UrlResource(csvFile.toURI());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+file.getFilename()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
    }
    @GetMapping("/csv/users")
    public ResponseEntity<Resource> serveUsers() throws IOException {
        File csvFile = new File(exportedDir.getAbsolutePath()+File.separator+"users.csv");
        Files.write(csvFile.toPath(),getUsersCSV().getBytes(StandardCharsets.UTF_8));
        Resource file = new UrlResource(csvFile.toURI());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+file.getFilename()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
    }
}
