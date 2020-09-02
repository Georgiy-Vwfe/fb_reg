package com.sixhands.repository;

import com.sixhands.domain.Greeting;
import com.sixhands.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    //User findByEmail(String email);
    Greeting findByEmail(String email);
    User findByActivationCode(String code);
}
