package com.yeahn.config;


import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.yeahn.web.WebInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
public class WebMvcConfigurer implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

	@Bean
	public WebInterceptor webInterceptor() {
		return new WebInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(webInterceptor())
				.addPathPatterns("/**") //해당경로 접근전에 인터셉터가 가로첸다
				.excludePathPatterns("/**/*.css", "/**/*.js");
	}

}