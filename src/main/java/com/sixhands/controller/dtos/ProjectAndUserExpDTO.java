package com.sixhands.controller.dtos;

import com.sixhands.domain.Project;
import com.sixhands.domain.UserProjectExp;

public class ProjectAndUserExpDTO {
    private Project project;
    private UserProjectExp projectExp;
    private int rating = 0;
    private boolean liked_by_user;
    public ProjectAndUserExpDTO(){}
    public ProjectAndUserExpDTO(Project project, UserProjectExp projectExp,int rating,boolean liked_by_user){
        this.project = project;
        this.projectExp = projectExp;
        this.rating = rating;
        this.liked_by_user = liked_by_user;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean isLiked_by_user() {
        return liked_by_user;
    }

    public void setLiked_by_user(boolean liked_by_user) {
        this.liked_by_user = liked_by_user;
    }
    //#endregion
}
