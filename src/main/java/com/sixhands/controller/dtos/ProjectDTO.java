package com.sixhands.controller.dtos;

import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;

import java.util.Arrays;

public class ProjectDTO {
    private Project project = new Project();
    private UserAndExpDTO member = new UserAndExpDTO();
    private UserAndExpDTO[] members = new UserAndExpDTO[10];

    public void addNewMember(){
        int index = Arrays.asList(members).indexOf(null);
        if(index == -1) return;
        members[index] = new UserAndExpDTO(new User(), new UserProjectExp());
        members[index].setAdded(true);
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

    public UserAndExpDTO[] getMembers(){
        return members;
    }

    public void setMembers(UserAndExpDTO[] members){
        this.members = members;
    }

    public UserAndExpDTO getMember() {
        return member;
    }

    public void setMember(UserAndExpDTO projectExp) {
        this.member = projectExp;
    }
}
