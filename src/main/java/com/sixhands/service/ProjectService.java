package com.sixhands.service;

import com.sixhands.controller.dtos.ProjectDTO;
import com.sixhands.controller.dtos.UserAndExpDTO;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;
import com.sixhands.misc.GenericUtils;
import com.sixhands.repository.ProjectRepository;
import com.sixhands.repository.UserProjectExpRepository;
import com.sixhands.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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

    private UserAndExpDTO createOrUpdateProjectExp(UserAndExpDTO reqUserAndExp, Project project){ //Called for all project member on project creation/update
        Optional<UserAndExpDTO> oUserAndExp = Optional.empty();
        try {
            Optional<User> oUser;
            if( !StringUtils.isEmpty(reqUserAndExp.getUser().getPassword()) && !StringUtils.isEmpty(reqUserAndExp.getUser().getEmail()) && reqUserAndExp.getUser().getUuid() != null )
                oUser = Optional.of(reqUserAndExp.getUser());
            else
                oUser = userService.findUserByUsername(reqUserAndExp.getUser().getEmail());

            User user;
            if(oUser.isPresent()) user = oUser.get();
            else{
                User regUser = userService.registerUser(reqUserAndExp.getUser().getEmail(),true);
                regUser.safeAssignProperties(reqUserAndExp.getUser());
                user = regUser;
            }


            reqUserAndExp.setUser(user);
            oUserAndExp = userAndExpByUser(project,user);
        }
        catch (Exception e) { e.printStackTrace(); return null; }

        UserProjectExp userExp;
        if(oUserAndExp.isPresent()){ //UserAndExp is persisted, update role and save
            userExp = oUserAndExp.get().getUserExp();
            userExp.setRole(reqUserAndExp.getUserExp().getRole());
        }else{ //UserAndExp is not persisted, set IDs and save
            userExp = reqUserAndExp.getUserExp();
            userExp.setProject_uuid(project.getUuid());
            userExp.setUser_uuid(reqUserAndExp.getUser().getUuid());
        }
        userProjectExpRepo.save(userExp);
        userRepo.save(reqUserAndExp.getUser());
        return reqUserAndExp;
    }

    //TODO: Throw error if project creator specified himself as a member
    public void saveNewProject(ProjectDTO projectDTO, User curUser){
        Project project = projectDTO.getProject();
        UserAndExpDTO[] members = projectDTO.getMembers();
        UserAndExpDTO creatorUserAndExp = projectDTO.getMember();
        UserProjectExp creatorProjectExp = creatorUserAndExp.getUserExp();

        project = projectRepo.save(project);
        final long projectId = project.getUuid();

        creatorProjectExp.setProject_uuid(projectId);
        creatorProjectExp.setUser_uuid(curUser.getUuid());
        creatorProjectExp.setProject_creator(true);

        Project finalProject = project;
        List<UserAndExpDTO> memberExp = Arrays.stream(members)
                .filter(Objects::nonNull)
                .map((member)-> createOrUpdateProjectExp(member, finalProject) )
                .collect(Collectors.toList());

        userProjectExpRepo.save(creatorProjectExp);
        memberExp.forEach((ueDTO)->userProjectExpRepo.save(ueDTO.getUserExp()));
    }

    public ProjectDTO projectDTOFromProject(Project project, User projectExpUser){
        ProjectDTO ret = new ProjectDTO();
        ret.setProject(project);
        UserAndExpDTO exp = userAndExpByUser(project,projectExpUser)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a member of this project"));
        ret.setMember( exp );
        //map UserProjectExp[] to User[], exclude project creator
        ret.setMembers(Arrays.stream(projectExpByProject(project))
                .filter((ue)-> !ue.getUserExp().getUuid().equals(exp.getUserExp().getUuid()))
                .toArray(UserAndExpDTO[]::new));
        return ret;
    }

    public ProjectDTO updateProject(ProjectDTO projectDTO, boolean byCreator) {
        //TODO:
        // throw exception if user created two members with same mail/another member with his mail
        // Reuse createProject logic for members with updated emails
        // Project creator can update all fields, members can only add new members and edit UserProjectExp
        Project reqProject = projectDTO.getProject();
        Project curProject = projectRepo.getOne(reqProject.getUuid());
        curProject = GenericUtils.initializeAndUnproxy(curProject);

        if(byCreator)
            curProject.setDescription(reqProject.getDescription());
        else
            curProject.setConfirmed(true);

        UserAndExpDTO reqUserAndExp = projectDTO.getMember();
        UserAndExpDTO curUserProjectExp = userAndExpByUser( curProject,
            userRepo.getOne(
                reqUserAndExp
                    .getUser()
                    .getUuid()
            )
        ).get();
        curUserProjectExp = GenericUtils.initializeAndUnproxy(curUserProjectExp);

        curUserProjectExp
                .getUserExp()
                .safeAssignProperties(reqUserAndExp.getUserExp());
        if(!byCreator){
            curUserProjectExp
                    .getUserExp()
                    .setCustom_description(reqProject.getDescription());
            curUserProjectExp
                    .getUserExp()
                    .setConfirmed(true);
        }

        UserAndExpDTO[] curMembers = Arrays.stream(projectDTO.getMembers())
                .filter(Objects::nonNull)
                .map((memDTO)-> createOrUpdateProjectExp(memDTO,projectDTO.getProject()))
                .toArray(UserAndExpDTO[]::new);

        userProjectExpRepo.save(curUserProjectExp.getUserExp());
        projectRepo.save(curProject);

        ProjectDTO ret = new ProjectDTO();
        ret.setProject(curProject);
        ret.setMembers(curMembers);
        ret.setMember(curUserProjectExp);
        return ret;
    }

    //#region methods for finding and filtering data
    //FIXME: TWO CALLS TO userAndExpByUser()
    public Project[] findProjectsByUser(User user, boolean onlyCreated){
        for (Object obj:new Object[]{user, user.getUuid()})
            Objects.requireNonNull(obj);
        Stream<Project> projectStream = projectRepo.findAll().stream()
                .filter((p)-> userAndExpByUser(p,user).isPresent());
        if(onlyCreated) projectStream = projectStream.filter( (p)-> userAndExpByUser(p,user).map((pe)->pe.getUserExp().isProject_creator()).orElse(false) );
        return projectStream.toArray(Project[]::new);
    }
    public Optional<UserAndExpDTO> userAndExpByUser(Project project, User user){
        for (Object obj:new Object[]{project, user, project.getUuid(), user.getUuid()})
            Objects.requireNonNull(obj);

        return Arrays.stream(projectExpByProject(project))
                .filter((ue)-> ue.getUser().getUuid().equals(user.getUuid()))
                .findFirst();
    }
    public UserAndExpDTO[] projectExpByProject(Project project){
        for (Object obj:new Object[]{project, project.getUuid()})
            Objects.requireNonNull(obj);

        return userProjectExpRepo
                .findAll()
                .stream()
                .filter((pe) -> pe.getProject_uuid().equals(project.getUuid()))
                .map((exp)->new UserAndExpDTO(userRepo.getOne(exp.getUser_uuid()),exp))
                .toArray(UserAndExpDTO[]::new);
    }
    //#endregion
}
