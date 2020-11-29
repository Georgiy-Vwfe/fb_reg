package com.sixhands.config;

import com.sixhands.repository.UserRepository;
import com.sixhands.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CompositeFilter;

import java.util.ArrayList;
import java.util.List;


//@EnableOAuth2Sso
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] PUBLIC_MATCHERS = {
            "/css/**",
            "/js/**",
            "/images/**",
            "/fonts/**",
            "/",
            "/login",
            "/registration",
            "/forget-password",
            "/admin-profile-project",
            "/activation/**",
            "/greeting",
            "/recovery-password",
            "/project",
            "/project-not-aproved",
            "/search"
    };

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private OAuth2ClientContext oauth2ClientContext;

    @Bean
    public DaoAuthenticationProvider myAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userService);
        return provider;
    }

/*    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }*/

    @Bean
    @ConfigurationProperties("facebook.client")
    public AuthorizationCodeResourceDetails facebook() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    @ConfigurationProperties("facebook.resource")
    public ResourceServerProperties facebookResource() {
        return new ResourceServerProperties();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(PUBLIC_MATCHERS)
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .and()
                .addFilterAt(ssoFilter(), BasicAuthenticationFilter.class)
                .logout()
                .permitAll();
    }

    private CompositeFilter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<OAuth2ClientAuthenticationProcessingFilter> filters = new ArrayList<>();

        filters.add(createSsoFilter("/facebook/login", facebook(), facebookResource()));

        filter.setFilters(filters);

        return filter;
    }

    private OAuth2ClientAuthenticationProcessingFilter createSsoFilter(String filterUrl, OAuth2ProtectedResourceDetails oAuthResourceDetails, ResourceServerProperties resourceServerProperties) {
        OAuth2ClientAuthenticationProcessingFilter oAuthFilter = new OAuth2ClientAuthenticationProcessingFilter(filterUrl);
        OAuth2RestTemplate oAuthTemplate = new OAuth2RestTemplate(oAuthResourceDetails, oauth2ClientContext);
        oAuthFilter.setRestTemplate(oAuthTemplate);
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(resourceServerProperties.getUserInfoUri(), oAuthResourceDetails.getClientId());
        tokenServices.setRestTemplate(oAuthTemplate);
        oAuthFilter.setTokenServices(tokenServices);

        return oAuthFilter;
    }
}
