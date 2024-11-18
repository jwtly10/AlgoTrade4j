package dev.jwtly10.shared.tracking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {
    List<UserActionLog> findByUserId(Long userId);

    List<UserActionLog> findAllByOrderByIdDesc(Pageable pageable);

    @Query("SELECT u FROM UserActionLog u WHERE u.userId = :userId ORDER BY u.id DESC")
    Page<UserActionLog> findTopNByUserId(@Param("userId") Long userId, Pageable pageable);
}