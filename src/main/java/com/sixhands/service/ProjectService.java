package com.sixhands.service;

import com.sixhands.controller.dtos.ProjectDTO;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;
import com.sixhands.exception.UserAlreadyExistsException;
import com.sixhands.misc.GenericUtils;
import com.sixhands.repository.ProjectRepository;
import com.sixhands.repository.UserProjectExpRepository;
import com.sixhands.repository.UserRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserProjectExpRepository userProjectExpRepo;
    @Autowired
    private UserService userService;

    //TODO: Throw error if project creator specified himself as a member
    public void saveNewProject(ProjectDTO projectDTO, User curUser){
        Project project = projectDTO.getProject();
        User[] members = projectDTO.getMembers();
        UserProjectExp userProjectExp = projectDTO.getProjectExp();

        project = projectRepo.save(project);
        final long projectId = project.getUuid();

        userProjectExp.setProject_uuid(projectId);
        userProjectExp.setUser_uuid(curUser.getUuid());
        userProjectExp.setProject_creator(true);

        List<UserProjectExp> memberExp = Arrays.stream(members)
                .filter(Objects::nonNull)
                .map((member)->{
                    UserProjectExp exp = new UserProjectExp();
                    exp.setProject_uuid(projectId);
                    exp.setRole(member.getAbout_user());
                    long memberId = -1;
                    try {
                        memberId = userService.loadUserByUsername(member.getEmail()).getUuid();
                    }catch (Exception e){
                        try { member = userService.registerUser(member.getEmail(),true); memberId = member.getUuid();}
                        catch (UserAlreadyExistsException ex) { ex.printStackTrace(); }
                    }
                    exp.setUser_uuid(memberId);
                    return exp;
                }).collect(Collectors.toList());

        boolean invalidMembersArePresent = memberExp.stream().map(UserProjectExp::getUser_uuid).anyMatch((id)->id==-1);
        if( invalidMembersArePresent ) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unable to register a project member");

        memberExp.add(userProjectExp);
        memberExp.forEach(userProjectExpRepo::save);
    }

    public ProjectDTO updateProjectDTOFromProject(Project project, User projectExpUser){
        ProjectDTO ret = new ProjectDTO();
        ret.setProject(project);
        UserProjectExp exp = projectExpByUser(project,projectExpUser)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"User is not a member of this project"));
        ret.setProjectExp( exp );
        //map UserProjectExp[] to User[], exclude project creator
        ret.setMembers(Arrays.stream(projectExpByProject(project))
                .filter((ue)-> !ue.getUser_project_exp_uuid().equals(exp.getUser_project_exp_uuid()))
                .map(this::projectExpToUser)
                .toArray(User[]::new));
        return ret;
    }
    public Project[] findProjectsByUser(User user, boolean onlyCreated){
        for (Object obj:new Object[]{user, user.getUuid()})
            Objects.requireNonNull(obj);
        Stream<Project> projectStream = projectRepo.findAll().stream();
        if(onlyCreated) projectStream = projectStream.filter( (p)-> projectExpByUser(p,user).map(UserProjectExp::isProject_creator).orElse(false) );
        return projectStream.toArray(Project[]::new);
    }
    public Optional<UserProjectExp> projectExpByUser(Project project, User user){
        for (Object obj:new Object[]{project, user, project.getUuid(), user.getUuid()})
            Objects.requireNonNull(obj);

        return Arrays.stream(projectExpByProject(project))
                .filter((ue)-> ue.getUser_uuid().equals(user.getUuid()))
                .findFirst();
    }
    public User projectExpToUser(UserProjectExp userProjectExp){
        for (Object obj:new Object[]{userProjectExp,userProjectExp.getUser_project_exp_uuid(),userProjectExp.getUser_uuid(),userProjectExp.getProject_uuid()})
            Objects.requireNonNull(obj);

        return userRepo.findById(userProjectExp.getUser_uuid())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unable to find project member"));
    }
    public UserProjectExp[] projectExpByProject(Project project){
        for (Object obj:new Object[]{project, project.getUuid()})
            Objects.requireNonNull(obj);

        return userProjectExpRepo.findAll().stream()
                .filter((pe)-> pe.getProject_uuid().equals(project.getUuid()))
                .toArray(UserProjectExp[]::new);
    }
    public Project projectByProjectExp(UserProjectExp projectExp){
        return projectRepo.getOne(projectExp.getProject_uuid());
    }
    public ProjectDTO updateProject(ProjectDTO projectDTO, boolean byCreator) {
        //TODO:
        // Check if project & projectExp is created by curUsers
        // Reuse createProject logic for members with updated emails
        // Edit member roles
        // Project creator can update all fields, members can only add new members and edit UserProjectExp custom_description
        Project reqProject = projectDTO.getProject();
        Project curProject = projectRepo.getOne(reqProject.getUuid());
        curProject = GenericUtils.initializeAndUnproxy(curProject);

        if(byCreator) curProject.setDescription(reqProject.getDescription());

        UserProjectExp reqUserProjectExp = projectDTO.getProjectExp();
        UserProjectExp curUserProjectExp = userProjectExpRepo.getOne(reqUserProjectExp.getUser_project_exp_uuid());
        curUserProjectExp = GenericUtils.initializeAndUnproxy(curUserProjectExp);

        if(!byCreator) curUserProjectExp.setCustom_description(reqProject.getDescription());

        User[] reqMembers = Arrays.stream(projectDTO.getMembers())
                .filter(Objects::nonNull)
                .toArray(User[]::new);
        User[] curMembers = Arrays.stream(reqMembers)
                .map((ru)->userRepo.getOne(ru.getUuid()))
                .toArray(User[]::new);
        //for (int i = 0; i < curMembers.length; i++)
        //    curMembers[i].safeAssignProperties(reqMembers[i]);

        //projectRepo.save(curProject.safeAssignProperties(reqProject));
        //userProjectExpRepo.save(curUserProjectExp.safeAssignProperties(reqUserProjectExp));

        Arrays.stream(curMembers).forEach(userRepo::save);
        userProjectExpRepo.save(curUserProjectExp);
        projectRepo.save(curProject);

        ProjectDTO ret = new ProjectDTO();
        ret.setProject(curProject);
        ret.setMembers(curMembers);
        ret.setProjectExp(curUserProjectExp);
        return ret;
    }
}
