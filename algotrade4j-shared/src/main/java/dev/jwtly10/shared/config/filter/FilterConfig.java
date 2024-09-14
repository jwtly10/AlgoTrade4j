package dev.jwtly10.shared.config.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<UserActionLoggingFilter> loggingFilterRegistration() {
        FilterRegistrationBean<UserActionLoggingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new UserActionLoggingFilter());
        registrationBean.addUrlPatterns("/*");
        // Make sure it runs after all the spring security filters
        registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);

        return registrationBean;
    }
}