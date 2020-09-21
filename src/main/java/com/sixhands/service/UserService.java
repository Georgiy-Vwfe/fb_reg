package com.sixhands.service;

import com.sixhands.SixHandsApplication;
import com.sixhands.controller.dtos.UserAndExpDTO;
import com.sixhands.controller.dtos.UserProfileDTO;
import com.sixhands.domain.Notification;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;
import com.sixhands.exception.UserAlreadyExistsException;
import com.sixhands.misc.GenericUtils;
import com.sixhands.repository.NotificationRepository;
import com.sixhands.repository.ProjectRepository;
import com.sixhands.repository.UserProjectExpRepository;
import com.sixhands.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private MailSender mailSender;
    @Autowired
    public UserProjectExpRepository userProjectExpRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private ProjectRepository projectRepo;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private NotificationRepository notificationRepo;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private Logger logger = Logger.getLogger(UserService.class.getName());

    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        UsernameNotFoundException notFoundException = new UsernameNotFoundException("Unable to find user " + username);
        if (StringUtils.isEmpty(username)) throw notFoundException;
        return userRepo.findByEmail(username).orElseThrow(() -> notFoundException);
    }

    public Optional<User> findUserByUsername(String username) {
        User ret = null;
        try {
            ret = loadUserByUsername(username);
        } catch (Exception ignored) {
        }
        return Optional.ofNullable(ret);
    }

    public Optional<User> findUserByResetToken(String resetToken) {
        return userRepo.findByResetToken(resetToken);
    }

    public Optional<User> findFirstUserByResetToken(String resetToken) {
        return userRepo.findFirstByResetToken(resetToken);
    }

    public User registerUser(String email) throws UserAlreadyExistsException {
        return registerUser(email, GenericUtils.randomAlphaNumString(8));
    }

    public User registerUser(String email, boolean isProjectMember) throws UserAlreadyExistsException {
        return registerUser(email, GenericUtils.randomAlphaNumString(8), isProjectMember);
    }

    public User registerUser(String email, String plainPassword) throws UserAlreadyExistsException {
        return registerUser(email, plainPassword, false);
    }

    public User registerUser(String email, @NotNull String plainPassword, boolean isProjectMember) throws UserAlreadyExistsException {
        if (plainPassword == null) return registerUser(email, isProjectMember);
        User user = null;
        try {
            user = loadUserByUsername(email);
        } catch (Exception ignored) {
        }
        if (user != null) throw new UserAlreadyExistsException(email);
        user = new User();
        if (SixHandsApplication.requireVerification())
            user.setActivationCode(UUID.randomUUID().toString());
        user.setRole("ROLE_USER");
        user.setPassword(passwordEncoder.encode(plainPassword));
        user.setEmail(email);
        user = userRepo.save(user);
        if (SixHandsApplication.requireVerification()) {
            if (isProjectMember) sendMemberVerificationMail(user, plainPassword);
            else sendVerificationMail(user);
        } else {
            logger.info("(disabled-mail-verification) Created user " + user.getUsername() + ", password: " + plainPassword);
        }
        return user;
    }

    public Optional<User> getCurUser() {
        Optional<String> username = getCurrentUsername();
        if (!username.isPresent()) return Optional.empty();
        return userRepo.findByEmail(getCurrentUsername().get());
    }

    public User getCurUserOrThrow() {
        return getCurUser().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authorized."));
    }

    public void sendUserNotification(Notification notification) {
        notificationRepo.save(notification);
    }

    public List<Notification> getUserNotifications(User user) {
        return notificationRepo
                .findAll()
                .stream()
                .filter((n) -> n.getUserUUID().equals(user.getUuid()))
                .sorted(Comparator.comparingLong(a -> a.getTimestamp().getTime()))
                .collect(Collectors.toList());
    }

    public static Optional<String> getCurrentUsername() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) return Optional.empty();

        String username = null;
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            username = springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }
        username = username == null || username.length() == 0 || username.equals("anonymousUser") ? null : username;
        return Optional.ofNullable(username);
    }

    public boolean activateUser(String code) {
        User user = userRepo.findByActivationCode(code);

        if (user == null) {
            return false;
        }

        user.setActivationCode(null);
        userRepo.save(user);

        return true;
    }

    @Value("${6hands.domain}")
    private String domain;

    public List<UserProjectExp> getProjectExpForUser(User user) {
        if (user == null || user.getUuid() == null) return new ArrayList<>();
        return userProjectExpRepo.findAll().stream()
                .filter((exp) -> exp.getUser_uuid().equals(user.getUuid()))
                .collect(Collectors.toList());
    }

    public UserProfileDTO getProfileDtoForUser(User user) {
        UserProfileDTO profileDTO = new UserProfileDTO(user);
        List<UserProjectExp> projectExps = getProjectExpForUser(user);
        for (UserProjectExp projectExp : projectExps) {
            Project project = projectRepo.getOne(projectExp.getProject_uuid());
            profileDTO
                    .addSkill(projectExp.getSkills(), projectExp, project)
                    .addTool(projectExp.getTools(), projectExp, project)
                    .addIndustry(project.getIndustry(), projectExp, project)
                    .addCompany(project.getCompany(), projectExp, project)
                    .addRole(projectExp.getRole(), projectExp, project)
                    .setRating(getRatingForUser(user));
        }

        return profileDTO;
    }

    public User safeAssignPersist(User from, User to) {
        return userRepo.save(to.safeAssignProperties(from));
    }

    //#region user-search
    public List<UserProfileDTO> searchUsersByName(List<UserProfileDTO> users, String name) {
        if (StringUtils.isEmpty(name)) return users;
        Predicate<UserProfileDTO> nameSearch = (profileDTO) -> {
            User u = profileDTO.getUser();
            return !StringUtils.isEmpty(u.getFirst_name()) &&
                    !StringUtils.isEmpty(u.getLast_name()) &&
                    (u.getFirst_name() + " " + u.getLast_name()).toLowerCase().contains(name.toLowerCase());
        };
        return users.stream().filter(nameSearch).collect(Collectors.toList());
    }

    public List<UserProfileDTO> searchUsersByProps(String skill, String company, String industry, String tool, String role) {
        Stream<UserProfileDTO> stream = userRepo.findAll().stream().map(this::getProfileDtoForUser);
        if (StringUtils.isEmpty(skill) && StringUtils.isEmpty(company) && StringUtils.isEmpty(industry) && StringUtils.isEmpty(tool) && StringUtils.isEmpty(role)) {
            return stream.collect(Collectors.toList());
        }
        stream = filterProp(stream, UserProfileDTO::getSkills, skill);
        stream = filterProp(stream, UserProfileDTO::getCompanies, company);
        stream = filterProp(stream, UserProfileDTO::getIndustries, industry);
        stream = filterProp(stream, UserProfileDTO::getTools, tool);
        stream = filterProp(stream, UserProfileDTO::getRoles, role);
        return stream.collect(Collectors.toList());
    }

    private Stream<UserProfileDTO> filterProp(Stream<UserProfileDTO> init, Function<UserProfileDTO, List<UserProfileDTO.UserProfilePropertyDTO>> propSupplier, String compareTo) {
        if (StringUtils.isEmpty(compareTo)) return init;

        return init.filter((profileDTO) ->
                propSupplier.apply(profileDTO)
                        .stream()
                        .anyMatch((p) -> p.getProperty().equalsIgnoreCase(compareTo))
        );
    }

    //#endregion
    //#region user-rating
    public int getRatingForUser(User user) {
        int rating = 0;

        if (user.getConfirmed_project()) rating++;

        List<UserProjectExp> projectExps = getProjectExpForUser(user);
        List<Project> projects = projectExps.stream()
                .map((ue) -> projectRepo.getOne(ue.getProject_uuid()))
                .filter(Project::isConfirmed)
                .collect(Collectors.toList());
        rating += projects.stream().mapToInt(this::getRatingForProject).sum();

        return rating;
    }

    public int getRatingForProject(Project project) {
        int rating = 0;

        UserAndExpDTO[] projectExps = projectService.projectExpByProject(project);

        if (project.isConfirmed()) rating++;
        //Add total amount of members that confirmed the project
        rating += Arrays.stream(projectExps)
                .mapToInt((exp) -> exp.getUserExp().isConfirmed() ? 1 : 0)
                .sum();
        //Filter out duplicate likedUserIDs, add total likes length
        rating += project.getLikedUserIDs().stream()
                .filter((id) -> project.getLikedUserIDs().indexOf(id) == project.getLikedUserIDs().lastIndexOf(id))
                .count();

        return rating;
    }

    //#endregion
    //#region mail-send
    private void sendMemberVerificationMail(User user, String plainPassword) {
        if (StringUtils.isEmpty(user.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User email is null or empty");
        String emailText = String.format(
                "Hello, %s! \n" +
                        "You are invited as a member of a project. \n" +
                        "Your password is %s\n" +
                        "Please, visit this link to verify your account: http://%s/activation/%s",
                user.getEmail(),
                plainPassword,
                domain,
                user.getActivationCode()
        );
        mailSender.sendEmail(user.getEmail(), "Activate your profile", emailText);
    }

    private void sendVerificationMail(User user) {
        if (StringUtils.isEmpty(user.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User email is null or empty");
        String emailText = String.format(
                "Hello, %s! \n" +
                        "Welcome to 6hands. Please, visit link: http://%s/activation/%s",
                user.getEmail(),
                domain,
                user.getActivationCode()
        );
        mailSender.sendEmail(user.getEmail(), "Activate your profile", emailText);
    }

    public boolean sendRecoverMail(User user, HttpServletRequest request) {

        String appUrl = request.getScheme() + "://" + request.getServerName() + ":8081";
        String emailText = String.format(
                "Hello, %s! \n" +
                        "To reset your password, click the link below:\n" + appUrl + "/recovery-password?token=%s",
                user.getEmail(),
                user.getResetToken());
//        domain
//        http://%s/recovery-password
        mailSender.sendEmail(user.getEmail(), "Password Reset Request", emailText);
        return true;
    }

    public void changeUserPassword(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepo.save(user);
    }

    public boolean isPasswordMatch(String encodedPassword, String rawPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void saveUser(User user) {
        userRepo.save(user);
    }

    //#endregion
}
