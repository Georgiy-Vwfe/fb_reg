package com.sixhands.controller;

import com.sixhands.domain.Notification;
import com.sixhands.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class NotificationsAdvice {
    @Autowired
    private UserService userService;

    @ModelAttribute("notifications")
    public List<Notification> getNotifications(){
        return userService.getCurUser()
                .map(curUser -> userService.getUserNotifications(curUser))
                .orElse(null);
    }
}
