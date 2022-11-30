package com.yeahn.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfigurer implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

	@Bean
	public WebInterceptor webInterceptor() {
		return new WebInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(webInterceptor())
				.addPathPatterns("", "/", "/**") //해당경로 접근전에 인터셉터가 가로첸다
				.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.map")
				.excludePathPatterns("/ajax/**/*", "/api/**/*");
	}

}