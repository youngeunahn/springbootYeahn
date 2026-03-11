package com.yeahn.security.filter;

import com.yeahn.model.AccessLogVo;
import com.yeahn.security.service.LogService;
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
        AccessLogVo log = new AccessLogVo();
        String uri = request.getRequestURI();

        log.setAccessMethod(request.getMethod());
        log.setAccessIp(request.getRemoteAddr());
        log.setAccessSessionId(request.getSession().getId());
        log.setAccessUri(uri);

        logService.saveAccessLog(log);

        filterChain.doFilter(request, response);
    }
}
