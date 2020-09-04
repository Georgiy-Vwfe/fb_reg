package com.sixhands.service;

import com.sixhands.controller.ProjectDTO;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;
import com.sixhands.repository.ProjectRepository;
import com.sixhands.repository.UserProjectExpRepository;
import com.sixhands.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    public void saveNewProject(ProjectDTO projectDTO, User curUser){
        Project project = projectDTO.getProject();
        User[] members = projectDTO.getMembers();
        UserProjectExp userProjectExp = projectDTO.getProjectExp();

        project = projectRepo.save(project);
        final long projectId = project.getUuid();

        userProjectExp.setProject_uuid(projectId);
        userProjectExp.setUser_uuid(curUser.getUuid());

        UserProjectExp[] memberExp = Arrays.stream(members).filter(Objects::nonNull).map((m)->{
            UserProjectExp exp = new UserProjectExp();
            exp.setProject_uuid(projectId);
            long memberId = -1;
            try {
                memberId = userService.loadUserByUsername(m.getEmail()).getUuid();
            }catch (Exception e){
                //TODO: add userService method that sends out mail invite & stores user in db
                //m = *userService#inviteUnregisteredProjectMember*;
                //memberId = m.getUuid();
            }
            exp.setUser_uuid(memberId);
            //TODO: get role from form, ?create temp field in User class
            exp.setRole("");
            return exp;
        }).toArray(UserProjectExp[]::new);

        List<UserProjectExp> allExps = new ArrayList<>(Arrays.asList(memberExp));
        allExps.add(userProjectExp);
        allExps.forEach(userProjectExpRepo::save);
    }
}
