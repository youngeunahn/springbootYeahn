package com.yeahn.security.config;

import com.yeahn.web.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableWebSecurity        //spring security 를 적용한다는 Annotation
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers( "/login", "/signUp", "/access_denied", "/resources/**", "/css/**", "/js/**").permitAll() // 로그인 권한은 누구나, resources파일도 모든권한
                // ADMIN 접근 허용
                //.antMatchers("/").hasRole("USER")
                //.antMatchers("/*").hasRole("ADMIN")
                .anyRequest().authenticated()            // 나머지 모든 요청은  인증이 필요
                .and()
            .formLogin()
                .usernameParameter("userId")
                .passwordParameter("password")
                .loginPage("/login")
                .loginProcessingUrl("/login_proc")
                .defaultSuccessUrl("/yetable/list")
                .failureUrl("/access_denied") // 인증에 실패했을 때 보여주는 화면 url, 로그인 form으로 파라미터값 error=true로 보낸다.
                .and()
            .csrf().disable();		//로그인 창
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
    }
}
