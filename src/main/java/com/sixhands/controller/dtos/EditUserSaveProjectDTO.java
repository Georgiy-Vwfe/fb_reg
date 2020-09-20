package com.sixhands.controller.dtos;

import com.sixhands.domain.User;

public class EditUserSaveProjectDTO {
    public EditUserSaveProjectDTO(User user, ProjectDTO projectDTO) {
        this.user = user;
        this.projectDTO = projectDTO;
    }
    public EditUserSaveProjectDTO(){}

    private User user = new User();
    private ProjectDTO projectDTO = new ProjectDTO();

    //#region getters/setters
    public ProjectDTO getProjectDTO() {
        return projectDTO;
    }

    public void setProjectDTO(ProjectDTO projectDTO) {
        this.projectDTO = projectDTO;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    //#endregion
}
