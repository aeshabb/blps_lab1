package com.skillbox.enrollment.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final XmlUserDetailsService xmlUserDetailsService;

    public SecurityConfig(XmlUserDetailsService xmlUserDetailsService) {
        this.xmlUserDetailsService = xmlUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/applications").hasAuthority("APPLY_COURSE")
                .requestMatchers(HttpMethod.POST, "/api/applications/*/approve").hasAuthority("MANAGE_APPLICATIONS")
                .requestMatchers(HttpMethod.GET, "/api/applications/**").hasAnyAuthority("READ_COURSES", "MANAGE_APPLICATIONS")
                .requestMatchers(HttpMethod.GET, "/api/programs/**").hasAuthority("READ_COURSES")
                .requestMatchers(HttpMethod.POST, "/api/programs/**").hasAuthority("WRITE_COURSES")
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK))
                .failureHandler((req, res, exc) -> res.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK))
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(exc -> exc
                .authenticationEntryPoint((req, res, authExc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
            )
            .userDetailsService(xmlUserDetailsService);
            
        return http.build();
    }
}
