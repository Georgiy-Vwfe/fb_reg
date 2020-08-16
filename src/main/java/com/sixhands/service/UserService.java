package com.sixhands.service;

import com.sixhands.domain.User;
import com.sixhands.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class UserService implements UserDetailsService {

    @Autowired
    private MailSender mailSender;

    UserRepository userRepo;

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

    public boolean activateUser(String code) {

        User user = userRepo.findByActivationCode(code);

        if(user == null) {
            return false;
        }

        user.setActivationCode(null);

        userRepo.save(user);

        return true;
    }
}
