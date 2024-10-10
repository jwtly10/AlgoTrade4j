package dev.jwtly10.backtestapi.repository.logging;

import dev.jwtly10.backtestapi.auth.model.UserLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {
    List<UserLoginLog> findByUserId(Long userId);
}