package dev.jwtly10.backtestapi.repository.logging;

import dev.jwtly10.backtestapi.auth.model.UserLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {
}