package com.sixhands.repository;

import com.sixhands.domain.Greeting;
import com.sixhands.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmail(String email);
    User findByActivationCode(String code);
}
