package com.yeahn.security.config;

import com.yeahn.security.handler.LoginFailureHandler;
import com.yeahn.security.handler.LoginSuccessHandler;
import com.yeahn.web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
        .csrf().disable()
        .authorizeRequests()
            .antMatchers("/login", "/signUp", "/css/**", "/js/**").permitAll()
            .antMatchers("/*").hasRole("ADMIN")   // ADMIN만 접근 가능
            .anyRequest().authenticated()
            .and()
        .formLogin()
            .loginPage("/login")              // 로그인 페이지 GET
            .loginProcessingUrl("/login")    // 로그인 처리 POST
            .usernameParameter("userId")     // 파라미터명 변경
            .passwordParameter("password")
            .successHandler(loginSuccessHandler)
            .failureHandler(loginFailureHandler)
            .permitAll();
    }

    /**
     * 로그인 인증 처리 메소드
     * @param auth
     * @throws Exception
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        //https://velog.io/@limsubin/Spring-Boot-spring-security-%EB%A1%9C-%EB%A1%9C%EA%B7%B8%EC%9D%B8%EC%9D%84-%EA%B5%AC%ED%98%84%ED%95%98%EC%9E%90
        auth.userDetailsService(userService).passwordEncoder(new BCryptPasswordEncoder());
//        auth.userDetailsService(userService).passwordEncoder(NoOpPasswordEncoder.getInstance());
    }
}