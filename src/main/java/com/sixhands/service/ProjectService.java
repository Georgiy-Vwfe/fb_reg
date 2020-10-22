package com.sixhands.service;

import com.sixhands.controller.dtos.ProjectDTO;
import com.sixhands.controller.dtos.UserAndExpDTO;
import com.sixhands.domain.Notification;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;
import com.sixhands.misc.GenericUtils;
import com.sixhands.repository.ProjectRepository;
import com.sixhands.repository.UserProjectExpRepository;
import com.sixhands.repository.UserRepository;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Consumer;
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

    private static Logger logger = Logger.getLogger(ProjectService.class);

    private UserAndExpDTO createOrUpdateProjectExp(UserAndExpDTO reqUserAndExp, Project project, Locale locale) { //Called for all project member on project creation/update
        Optional<UserAndExpDTO> oPersistedUserAndExp = Optional.empty();
        User reqCurUser = reqUserAndExp.getUser();
        UserProjectExp reqCurProjectExp = reqUserAndExp.getUserExp();
        try {
            Optional<User> oUser;
            if (!StringUtils.isEmpty(reqCurUser.getPassword()) && !StringUtils.isEmpty(reqCurUser.getEmail()) && reqCurUser.getUuid() != null)
                oUser = Optional.of(reqCurUser);
            else
                oUser = userService.findUserByUsername(reqCurUser.getEmail());

            User user;
            if (oUser.isPresent())
                user = oUser.get();
            else {
                User regUser = userService.registerUser(reqCurUser.getEmail(), true, locale);
                regUser.safeAssignProperties(reqCurUser);
                user = regUser;
            }

            reqUserAndExp.setUser(user);
            reqCurUser = user;
            oPersistedUserAndExp = userAndExpByUser(project, user);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Optional<UserAndExpDTO> projectCreator = findProjectCreator(project);
        if (!projectCreator.isPresent())
            logger.warn("Unable to send out notifications, can't find the project creator");

        UserProjectExp userExp;
        if (oPersistedUserAndExp.isPresent()) { //UserAndExp is persisted, update role and save
            userExp = oPersistedUserAndExp.get().getUserExp();
            userExp.setRole(reqCurProjectExp.getRole());
        } else { //UserAndExp is not persisted, set IDs and save
            userExp = reqCurProjectExp;
            userExp.setProject_uuid(project.getUuid());
            userExp.setUser_uuid(reqCurUser.getUuid());


            //Send out invite notifications
            projectCreator.ifPresent(new Consumer<UserAndExpDTO>() {
                @Override
                public void accept(UserAndExpDTO userAndExpDTO) {
                    Notification notification = new Notification.NotificationBuilder(userExp.getUser_uuid())
                            .buildProjectInvite(project, userAndExpDTO.getUser(), locale);
                    userService.sendUserNotification(
                            notification
                    );
                }
            });

        }

        userProjectExpRepo.save(userExp);
        userRepo.save(reqCurUser);
        return reqUserAndExp;
    }

    //TODO: Throw error if project creator specified himself as a member
    public void saveNewProject(ProjectDTO projectDTO, User curUser, Locale locale) {
        Project project = projectDTO.getProject();
        UserAndExpDTO[] members = projectDTO.getMembers();
        UserAndExpDTO creatorUserAndExp = projectDTO.getMember();
        UserProjectExp creatorProjectExp = creatorUserAndExp.getUserExp();

        project = projectRepo.save(project);
        final long projectId = project.getUuid();

        creatorProjectExp.setProject_uuid(projectId);
        creatorProjectExp.setUser_uuid(curUser.getUuid());
        creatorProjectExp.setProject_creator(true);
        userProjectExpRepo.save(creatorProjectExp);

        Project finalProject = project;
        List<UserAndExpDTO> memberExp = Arrays.stream(members)
                .filter(Objects::nonNull)
                .map((member) -> createOrUpdateProjectExp(member, finalProject, locale))
                .collect(Collectors.toList());

        memberExp.forEach((ueDTO) -> userProjectExpRepo.save(ueDTO.getUserExp()));
    }

    public ProjectDTO projectDTOFromProject(Project project, User projectExpUser) {
        ProjectDTO ret = new ProjectDTO();
        ret.setProject(project);
        UserAndExpDTO exp = userAndExpByUser(project, projectExpUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a member of this project"));
        ret.setMember(exp);
        //map UserProjectExp[] to User[], exclude project creator
        ret.setMembers(Arrays.stream(projectExpByProject(project))
                .filter((ue) -> !ue.getUserExp().getUuid().equals(exp.getUserExp().getUuid()))
                .toArray(UserAndExpDTO[]::new));
        return ret;
    }

    public void deleteProject(Project project) {
        deleteProject(project.getUuid());
    }

    public void deleteProject(Long uuid) {
        UserAndExpDTO[] userAndExpDTOS = this.projectExpByProject(uuid);
        for (UserAndExpDTO userAndExpDTO : userAndExpDTOS)
            userProjectExpRepo.deleteById(userAndExpDTO.getUserExp().getUuid());

        projectRepo.deleteById(uuid);
    }

    public ProjectDTO updateProject(ProjectDTO projectDTO, boolean byCreator, Locale locale) {
        //TODO:
        // throw exception if user created two members with same mail/another member with his mail
        // Reuse createProject logic for members with updated emails
        // Project creator can update all fields, members can only add new members and edit UserProjectExp
        Project reqProject = projectDTO.getProject();
        Project curProject = projectRepo.getOne(reqProject.getUuid());
        curProject = GenericUtils.initializeAndUnproxy(curProject);

        if (byCreator)
            curProject.setDescription(reqProject.getDescription());
        else
            curProject.setConfirmed(true);

        UserAndExpDTO reqUserAndExp = projectDTO.getMember();
        UserAndExpDTO curUserProjectExp = userAndExpByUser(curProject,
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
        if (!byCreator) {
            UserAndExpDTO creatorAndExp = findProjectCreator(reqProject).get();

            curUserProjectExp
                    .getUserExp()
                    .setCustom_description(reqProject.getDescription());
            if (!curUserProjectExp.getUserExp().isConfirmed())
                //Send out confirm notification
                userService.sendUserNotification(
                        new Notification.NotificationBuilder(creatorAndExp.getUser().getUuid())
                                .buildProjectConfirm(reqProject, curUserProjectExp.getUser(), locale)
                );
            curUserProjectExp
                    .getUserExp()
                    .setConfirmed(true);
            //Send out change notification
            userService.sendUserNotification(
                    new Notification.NotificationBuilder(creatorAndExp.getUser().getUuid())
                            .buildProjectChange(reqProject, curUserProjectExp.getUser(), locale)
            );
        }

        userProjectExpRepo.save(curUserProjectExp.getUserExp());
        UserAndExpDTO[] curMembers = Arrays.stream(projectDTO.getMembers())
                .filter(Objects::nonNull)
                .map((memDTO) -> createOrUpdateProjectExp(memDTO, projectDTO.getProject(), locale))
                .toArray(UserAndExpDTO[]::new);

        projectRepo.save(curProject);

        ProjectDTO ret = new ProjectDTO();
        ret.setProject(curProject);
        ret.setMembers(curMembers);
        ret.setMember(curUserProjectExp);
        return ret;
    }

    //#region methods for finding and filtering data
    //FIXME: TWO CALLS TO userAndExpByUser()
    public Project[] findProjectsByUser(User user, boolean onlyCreated) {
        for (Object obj : new Object[]{user, user.getUuid()})
            Objects.requireNonNull(obj);
        Stream<Project> projectStream = projectRepo.findAll().stream()
                .filter((p) -> userAndExpByUser(p, user).isPresent());
        if (onlyCreated)
            projectStream = projectStream.filter((p) -> userAndExpByUser(p, user).map((pe) -> pe.getUserExp().isProject_creator()).orElse(false));
        return projectStream.toArray(Project[]::new);
    }

    public Optional<UserAndExpDTO> userAndExpByUser(Project project, User user) {
        for (Object obj : new Object[]{project, user, project.getUuid(), user.getUuid()})
            Objects.requireNonNull(obj);

        return Arrays.stream(projectExpByProject(project))
                .filter((ue) -> ue.getUser().getUuid().equals(user.getUuid()))
                .findFirst();
    }

    public UserAndExpDTO[] projectExpByProject(Long uuid) {
        Objects.requireNonNull(uuid);

        return userProjectExpRepo
                .findAll()
                .stream()
                .filter((pe) -> pe.getProject_uuid().equals(uuid))
                .map((exp) -> new UserAndExpDTO(userRepo.getOne(exp.getUser_uuid()), exp))
                .toArray(UserAndExpDTO[]::new);
    }

    public UserAndExpDTO[] projectExpByProject(Project project) {
        return projectExpByProject(project.getUuid());
    }

    public Optional<UserAndExpDTO> findProjectCreator(Project project) {
        for (Object obj : new Object[]{project, project.getUuid()})
            Objects.requireNonNull(obj);
        Optional<UserProjectExp> exp = userProjectExpRepo
                .findAll()
                .stream()
                .filter((pe) -> pe.getProject_uuid().equals(project.getUuid()) && pe.isProject_creator())
                .findFirst();
        if (!exp.isPresent()) return Optional.empty();
        User user = userRepo.getOne(exp.get().getUser_uuid());
        return Optional.of(new UserAndExpDTO(user, exp.get()));
    }
    //#endregion
}
