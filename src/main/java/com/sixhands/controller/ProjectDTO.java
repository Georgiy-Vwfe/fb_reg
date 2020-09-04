package com.sixhands.controller;

import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;

import java.util.Arrays;

public class ProjectDTO {
    private Project project = new Project();
    private UserProjectExp projectExp = new UserProjectExp();
    private User[] members = new User[10];

    public void addNewMember(){
        int index = Arrays.asList(members).indexOf(null);
        if(index == -1) return;
        members[index] = new User();
    }

    public void deleteMember(int index){
        members[index] = null;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User[] getMembers(){
        return members;
    }

    public void setMembers(User[] members){
        this.members = members;
    }

    public UserProjectExp getProjectExp() {
        return projectExp;
    }

    public void setProjectExp(UserProjectExp projectExp) {
        this.projectExp = projectExp;
    }
}
