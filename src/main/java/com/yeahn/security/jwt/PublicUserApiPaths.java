package com.yeahn.security.jwt;

import jakarta.servlet.http.HttpServletRequest;

public final class PublicUserApiPaths {

    private PublicUserApiPaths() {
    }

    public static boolean matches(HttpServletRequest request) {
        return matches(request.getMethod(), request.getServletPath());
    }

    static boolean matches(String method, String path) {
        if ("POST".equalsIgnoreCase(method)) {
            return "/api/user/login".equals(path)
                    || "/api/user/signUp".equals(path);
        }

        if ("GET".equalsIgnoreCase(method)) {
            return "/api/user/check-id".equals(path)
                    || "/api/user/templates".equals(path)
                    || path.startsWith("/api/user/templates/");
        }

        return false;
    }
}
