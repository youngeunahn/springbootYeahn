package com.yeahn.security.handler;

import com.yeahn.model.LoginLogVo;
import com.yeahn.model.UserAgentInfo;
import com.yeahn.security.service.LogService;
import com.yeahn.security.service.UserAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.yeahn.common.CommonUtils.getIP;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final LogService logService;
    private final UserAgentService userAgentService;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        LoginLogVo vo = new LoginLogVo();

        String userAgent = request.getHeader("User-Agent");
        UserAgentInfo uaInfo = userAgentService.parse(userAgent);

        String userId = request.getParameter("userId");
        if (userId == null) userId = "";

        String failReason = "UNKNOWN";

        if (exception instanceof BadCredentialsException) {
            failReason = "BAD_PASSWORD";
        } else if (exception instanceof UsernameNotFoundException) {
            failReason = "USER_NOT_FOUND";
        } else if (exception instanceof LockedException) {
            failReason = "ACCOUNT_LOCKED";
        } else if (exception instanceof DisabledException) {
            failReason = "ACCOUNT_DISABLED";
        }

        vo.setUserId(userId);
        vo.setLoginSuccess("N");
        vo.setStatusCode(String.valueOf(response.getStatus()));
        vo.setFailReason(failReason);
        vo.setLoginReqMethod(request.getMethod());
        vo.setLoginReqIp(getIP(request));
        vo.setLoginReqDevice(uaInfo.getDevice());
        vo.setLoginReqBrowser(uaInfo.getBrowser());
        vo.setLoginReqLanguage(request.getHeader("Accept-Language"));
        vo.setLoginReqOs(uaInfo.getOs());
        vo.setLoginReqSessionId(request.getSession().getId());
        vo.setLoginReqReferrer(request.getHeader("Referer"));
        vo.setLoginReqUaOrigin(userAgent);

        logService.saveLoginLog(vo);

        response.sendRedirect("/login?error");
    }
}
