package dev.jwtly10.shared.tracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {
    List<UserActionLog> findByUserId(Long userId);
}