package com.sixhands.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserAlreadyExistsException extends ServiceException {
    public UserAlreadyExistsException(String username){
        super("User "+username+" already exists");
    }
}
