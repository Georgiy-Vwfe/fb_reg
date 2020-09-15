package com.sixhands.controller.dtos;

import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;

public class UserAndExpDTO {
    private boolean added = false;
    private User user = new User();
    private UserProjectExp userExp = new UserProjectExp();
    public UserAndExpDTO(){}
    public UserAndExpDTO(User user, UserProjectExp userExp){
        this.user = user;
        this.userExp = userExp;
    }
    //#region getters/setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserProjectExp getUserExp() {
        return userExp;
    }

    public void setUserExp(UserProjectExp userExp) {
        this.userExp = userExp;
    }

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }
    //#endregion
}
