package com.yeahn.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeahn.common.dto.ResponseDto;
import com.yeahn.log.dto.LoginLogVo;
import com.yeahn.common.UserAgentInfo;
import com.yeahn.log.service.LogService;
import com.yeahn.common.UserAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.yeahn.common.CommonUtils.getIP;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final LogService logService;
    private final UserAgentService userAgentService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        LoginLogVo vo = new LoginLogVo();

        String userAgent = request.getHeader("User-Agent");
        UserAgentInfo uaInfo = userAgentService.parse(userAgent);

        vo.setUserId(authentication.getName());
        vo.setLoginSuccess("Y");
        vo.setStatusCode(String.valueOf(response.getStatus()));
        vo.setFailReason("");
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

        // API 요청인 경우 JSON 응답
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            objectMapper.writeValue(response.getWriter(), ResponseDto.success("로그인이 완료되었습니다.", authentication.getName()));
        } else {
            response.sendRedirect("/exercise/plan?menuCode=EXERCISE_0002");
        }
    }
}
