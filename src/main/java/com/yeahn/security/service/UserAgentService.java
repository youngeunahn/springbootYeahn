package com.yeahn.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yeahn.model.UserAgentInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserAgentService {

    private final Parser parser = new Parser();

    private final Cache<String, UserAgentInfo> cache =
            Caffeine.newBuilder()
                    .maximumSize(200)
                    .expireAfterWrite(1, TimeUnit.HOURS)
                    .build();

    public UserAgentInfo parse(String userAgent) {

        return cache.get(userAgent, ua -> {

            Client c = parser.parse(ua);

            String browser = c.userAgent.family;
            String os = c.os.family;
            String device = resolveDevice(userAgent);

            return new UserAgentInfo(browser, os, device);
        });
    }

    private String resolveDevice(String userAgent) {

        if (userAgent == null) {
            return "UNKNOWN";
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("mobile")) {
            return "MOBILE";
        }

        if (ua.contains("tablet") || ua.contains("ipad")) {
            return "TABLET";
        }

        return "PC";
    }
}
