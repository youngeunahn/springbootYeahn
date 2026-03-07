package com.yeahn.security.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

//    private final LoginHistoryService loginHistoryService;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        String username = request.getParameter("username");
        String ip = request.getRemoteAddr();

//        loginHistoryService.recordFailure(username, ip, exception.getMessage());

        response.sendRedirect("/login?error");
    }
}
