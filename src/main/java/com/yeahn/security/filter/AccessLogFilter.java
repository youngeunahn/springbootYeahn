package com.yeahn.security.filter;

import com.yeahn.model.AccessLogVo;
import com.yeahn.model.UserAgentInfo;
import com.yeahn.security.service.LogService;
import com.yeahn.security.service.UserAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AccessLogFilter extends OncePerRequestFilter {

    private final LogService logService;
    private final UserAgentService userAgentService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String uri = request.getRequestURI();

        return uri.startsWith("/css")
                || uri.startsWith("/js")
                || uri.startsWith("/images")
                || uri.startsWith("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long endTime = System.currentTimeMillis();

            AccessLogVo log = new AccessLogVo();

            String userAgent = request.getHeader("User-Agent");
            UserAgentInfo uaInfo = userAgentService.parse(userAgent);

            log.setAccessUri(request.getRequestURI());
            log.setAccessMethod(request.getMethod());
            log.setAccessIp(request.getRemoteAddr());
            log.setAccessDevice(uaInfo.getDevice());
            log.setAccessBrowser(uaInfo.getBrowser());
            log.setAccessOs(uaInfo.getOs());
            log.setAccessSessionId(request.getSession().getId());
            log.setAccessReferrer(request.getHeader("Referer"));
            log.setAccessLanguage(request.getHeader("Accept-Language"));
            log.setUaOrigin(userAgent);
            log.setAccessStatusCode(String.valueOf(response.getStatus()));
            log.setResponseTime(endTime - startTime);

            logService.saveAccessLog(log);
        }
    }
}
