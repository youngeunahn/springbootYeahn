package com.yeahn.security.config;

import com.yeahn.security.handler.LoginFailureHandler;
import com.yeahn.security.handler.LoginSuccessHandler;
import com.yeahn.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
            .antMatchers("/login", "/signUp", "/signUp/checkId", "/css/**", "/js/**").permitAll()
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
        auth.userDetailsService(userService).passwordEncoder(new BCryptPasswordEncoder());
//        auth.userDetailsService(userService).passwordEncoder(NoOpPasswordEncoder.getInstance());
    }
}