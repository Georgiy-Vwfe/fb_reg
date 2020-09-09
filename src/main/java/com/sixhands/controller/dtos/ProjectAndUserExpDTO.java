package com.sixhands.controller.dtos;

import com.sixhands.domain.Project;
import com.sixhands.domain.UserProjectExp;

public class ProjectAndUserExpDTO {
    private Project project;
    private UserProjectExp projectExp;
    public ProjectAndUserExpDTO(){}
    public ProjectAndUserExpDTO(Project project, UserProjectExp projectExp){
        this.project = project;
        this.projectExp = projectExp;
    }
    //#region getters/setters
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public UserProjectExp getProjectExp() {
        return projectExp;
    }

    public void setProjectExp(UserProjectExp projectExp) {
        this.projectExp = projectExp;
    }
    //#endregion
}
