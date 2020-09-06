package com.sixhands.repository;

import com.sixhands.domain.UserProjectExp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface UserProjectExpRepository extends JpaRepository<UserProjectExp, Long> {
}
