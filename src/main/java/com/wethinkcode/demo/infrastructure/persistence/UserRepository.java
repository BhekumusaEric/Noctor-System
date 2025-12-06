package com.wethinkcode.demo.infrastructure.persistence;

import com.wethinkcode.demo.domain.shared.User;
import com.wethinkcode.demo.domain.shared.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRole(UserRole role);
    List<User> findByStatus(String status);
}
