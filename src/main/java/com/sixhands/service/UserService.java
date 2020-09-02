package com.sixhands.service;

import com.sixhands.domain.User;
import com.sixhands.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private MailSender mailSender;
    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //TODO написать логику поиска пользователя из БД
        return null;
    }

    public boolean addUser(User user) {
        //TODO присвоить найденного юзера или null
        User userFromDb = null;

        if (user != null) {
            return false;
        }

        //TODO выполнить первоначальные настройки пользователя (присвоение роли, сохранение и т.д)
        user.setActivationCode(UUID.randomUUID().toString());

        userRepo.save(user);

        if (!StringUtils.isEmpty(user.getEmail())) {
            String emailText = String.format(
                    "Hello, %s! \n" +
                            "Welcome to 6hands. Please, visit link: http://localhost:8081/activation/%s",
                    user.getEmail(),
                    user.getActivationCode()
            );
            mailSender.send(user.getEmail(), "Activate your profile", emailText);
        }

        return true;
    }

    public static Optional<String> getCurrentUsername() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            LOG.debug("no authentication in security context found");
            return Optional.empty();
        }

        String username = null;
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            username = springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }

        LOG.debug("found username '{}' in security context", username);

        return Optional.ofNullable(username);
    }

    public boolean recoverPassword(User user){
        if (!StringUtils.isEmpty(user.getEmail())) {
            String emailText = String.format(
                    "Hello, %s! \n" +
                            "You want to recover your password. Please, visit link: http://localhost:8081/recover_password",
                    user.getEmail()
            );
            mailSender.send(user.getEmail(), "Recover password", emailText);
        }
        return true;
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
}
