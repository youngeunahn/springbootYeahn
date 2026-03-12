package com.yeahn.security.handler;

import com.yeahn.model.LoginLogVo;
import com.yeahn.security.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final LogService logService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        LoginLogVo vo = new LoginLogVo();
        vo.setUserId(authentication.getName());
        vo.setLoginSuccess("Y");

        String ip = request.getRemoteAddr();

//        logService.saveLoginLog(username, ip);

        response.sendRedirect("/yetable/list");
    }
}
