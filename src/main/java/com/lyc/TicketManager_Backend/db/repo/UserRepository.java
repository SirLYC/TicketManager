package com.lyc.TicketManager_Backend.db.repo;

import com.lyc.TicketManager_Backend.db.bean.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    Optional<User> findByUsernameAndPasswordHash(String username, String passwordHash);
}
