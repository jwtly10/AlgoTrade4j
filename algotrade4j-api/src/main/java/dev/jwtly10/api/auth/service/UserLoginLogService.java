package dev.jwtly10.api.auth.service;

import dev.jwtly10.api.auth.model.UserLoginLog;
import dev.jwtly10.api.auth.repository.UserLoginLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserLoginLogService {

    private final UserLoginLogRepository userLoginLogRepository;

    @Autowired
    public UserLoginLogService(UserLoginLogRepository userLoginLogRepository) {
        this.userLoginLogRepository = userLoginLogRepository;
    }

    @Transactional
    public UserLoginLog logUserLogin(Long userId, String ipAddress) {
        UserLoginLog log = new UserLoginLog();
        log.setUserId(userId);
        log.setIpAddress(ipAddress);
        log.setLoginTime(LocalDateTime.now());
        return userLoginLogRepository.save(log);
    }
}