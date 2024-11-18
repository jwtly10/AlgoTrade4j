package dev.jwtly10.backtestapi.repository.logging;

import dev.jwtly10.backtestapi.auth.model.UserLoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {
    List<UserLoginLog> findByUserId(Long userId);

    List<UserLoginLog> findByUserIdOrderByIdDesc(Long userId);

    @Query(value = "SELECT u FROM UserLoginLog u WHERE u.userId = :userId ORDER BY u.id DESC")
    Page<UserLoginLog> findTopNByUserId(@Param("userId") Long userId, Pageable pageable);

}