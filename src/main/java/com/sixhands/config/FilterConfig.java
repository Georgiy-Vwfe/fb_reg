package com.sixhands.config;

import com.sixhands.config.filter.AdminFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class FilterConfig {
    @Value("${6hands.admin-token}")
    private String correctToken;

    @Bean
    public FilterRegistrationBean<AdminFilter> adminFilter(){
        FilterRegistrationBean<AdminFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new AdminFilter(correctToken));
        registrationBean.setUrlPatterns(Collections.singletonList( "/admin/*" ));

        return registrationBean;
    }
}
