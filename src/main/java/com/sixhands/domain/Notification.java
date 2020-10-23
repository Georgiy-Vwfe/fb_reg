package com.sixhands.domain;

import com.sixhands.misc.GenericUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

@Entity
public class Notification {
    private Notification() {
    }

    /*private Notification(Long userId, String messageRU, String messageEN, String urlPath) {
        this.messageRU = messageRU;
        this.messageEN = messageEN;
        this.userUUID = userId;
        this.urlPath = urlPath;
    }*/

    private Notification(Long userId, String messageRU, String messageEN, String message, String urlPath) {
        this.messageRU = messageRU;
        this.messageEN = messageEN;
        this.message = message;
        this.userUUID = userId;
        this.urlPath = urlPath;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long uuid;

    private Long userUUID;

    private String messageRU;
    private String messageEN;

    private String message;

    private String urlPath;
    private Date timestamp = new Date();

/*    private Notification setDataFromTemplates(String urlPath, String ruTemplate, String enTemplate, Object... formatArgs) {
        this.urlPath = urlPath;

        messageEN = String.format(enTemplate, formatArgs);
        messageRU = String.format(ruTemplate, formatArgs);

        return this;
    }*/

    private Notification setDataFromTemplates(String urlPath, String template, Object... formatArgs) {
        this.urlPath = urlPath;
        message = String.format(template, formatArgs);
        return this;
    }

    public String formatTime() {
        return GenericUtils.formatDateToTHStr(timestamp);
    }

    public static class NotificationBuilder {
        private Notification notification = new Notification();
        private Function<User, String> getDisplayUsername = (u) -> u.getFirst_name() + " " + u.getLast_name();

        public NotificationBuilder(Long userId) {
            notification.setUserUUID(userId);
        }

        private boolean isEngLocale(Locale locale) {
            return locale.getDisplayLanguage().equals("английский");
        }

        public Notification buildProjectChangeLocaleSensitive(Project project, User changeByUser, Locale locale) {
            if (isEngLocale(locale)) {
                return notification.setDataFromTemplates(
                        "/user/" + changeByUser.getUuid(),
                        MessageTemplates.PROJECT_CHANGE_EN,
                        getDisplayUsername.apply(changeByUser),
                        project.getName()
                );
            } else {
                return notification.setDataFromTemplates(
                        "/user/" + changeByUser.getUuid(),
                        MessageTemplates.PROJECT_CHANGE_RU,
                        getDisplayUsername.apply(changeByUser),
                        project.getName()
                );
            }
        }

        public Notification buildProjectConfirmLocaleSensitive(Project project, User confirmByUser, Locale locale) {
            if (isEngLocale(locale)) {
                return notification.setDataFromTemplates(
                        "/user/" + confirmByUser.getUuid(),
                        MessageTemplates.PROJECT_CONFIRM_EN,
                        getDisplayUsername.apply(confirmByUser),
                        project.getName()
                );
            } else {
                return notification.setDataFromTemplates(
                        "/user/" + confirmByUser.getUuid(),
                        MessageTemplates.PROJECT_CONFIRM_RU,
                        getDisplayUsername.apply(confirmByUser),
                        project.getName()
                );
            }
        }

        public Notification buildProjectInviteLocaleSensitive(Project project, User projectCreator, Locale locale) {
            if (isEngLocale(locale)) {
                return notification.setDataFromTemplates(
                        "/user/me",
                        MessageTemplates.PROJECT_INVITE_EN,
                        getDisplayUsername.apply(projectCreator),
                        project.getName()
                );
            } else {
                return notification.setDataFromTemplates(
                        "/user/me",
                        MessageTemplates.PROJECT_INVITE_RU,
                        getDisplayUsername.apply(projectCreator),
                        project.getName()
                );
            }
        }

        public Notification buildProjectChange(Project project, User changeByUser) {
            return notification.setDataFromTemplates(
                    "/user/" + changeByUser.getUuid(),
                    MessageTemplates.PROJECT_CHANGE_RU,
                    MessageTemplates.PROJECT_CHANGE_EN,
                    getDisplayUsername.apply(changeByUser),
                    project.getName()
            );
        }

        public Notification buildProjectConfirm(Project project, User confirmByUser) {
            return notification.setDataFromTemplates(
                    "/user/" + confirmByUser.getUuid(),
                    MessageTemplates.PROJECT_CONFIRM_RU,
                    MessageTemplates.PROJECT_CONFIRM_EN,
                    getDisplayUsername.apply(confirmByUser),
                    project.getName()
            );
        }

        public Notification buildProjectInvite(Project project, User projectCreator) {
            return notification.setDataFromTemplates(
                    "/user/me",
                    MessageTemplates.PROJECT_INVITE_RU,
                    MessageTemplates.PROJECT_INVITE_EN,
                    getDisplayUsername.apply(projectCreator),
                    project.getName()
            );
        }

        private static class MessageTemplates {
            public static final String PROJECT_CHANGE_RU = "%s внес изменения в проект '%s'";
            public static final String PROJECT_CHANGE_EN = "%s has made some changes in the '%s' project";

            public static final String PROJECT_CONFIRM_RU = "%s подтвердил участие в проекте '%s'";
            public static final String PROJECT_CONFIRM_EN = "%s confirmed role in project '%s'";

            public static final String PROJECT_INVITE_RU = "%s пригласил вас в качестве участника в проекте '%s'";
            public static final String PROJECT_INVITE_EN = "%s sent you a request to confirm a project";
        }
    }

    //#region getters/setters
    public Long getUuid() {
        return uuid;
    }

    public void setUuid(Long uuid) {
        this.uuid = uuid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageRU() {
        return messageRU;
    }

    public void setMessageRU(String messageRU) {
        this.messageRU = messageRU;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public Long getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(Long userUUID) {
        this.userUUID = userUUID;
    }

    public String getMessageEN() {
        return messageEN;
    }

    public void setMessageEN(String messageEN) {
        this.messageEN = messageEN;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    //#endregion
}
