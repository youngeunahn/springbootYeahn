package com.yeahn.web;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yeahn.model.MenuConfig;
import com.yeahn.web.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
@Component
@RequiredArgsConstructor
public class WebInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private ConfigService configService;

	private static final Logger logger = LoggerFactory.getLogger(WebInterceptor.class);

	public void paramHeader(HttpServletRequest request) {
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = (String) paramNames.nextElement();
			logger.info("Header : {}", paramName);

			String[] paramValues = request.getParameterValues(paramName);
			if (paramValues.length == 1) {
				String paramValue = paramValues[0];
				if (paramValue.length() == 0)
					logger.info("Value : No Value");
				else
					logger.info("Value : {} ", paramValue);
			} else {
				for (int i = 0; i < paramValues.length; i++) {
					logger.info("Value : {}", paramValues[i]);
				}
			}
		}
	}

	
	@Override
	public boolean preHandle (HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		List<MenuConfig> MenuList = configService.getMenuList();
		if (modelAndView != null){
			for (MenuConfig menulist : MenuList){
				if (menulist.getMENU_LEVEL()==1)
					menulist.setMENU_LEVEL_VIEW(true);
				else if (menulist.getMENU_LEVEL()==2)
					menulist.setMENU_LEVEL_VIEW(false);
				menulist.setMENU_GROUP(menulist.getMENU_CODE().split("_")[0]);
			}
				modelAndView.addObject("MenuList", MenuList);

			MenuConfig MenuPage = configService.getMenuPage(request.getParameter("menuCode").toString());
			modelAndView.addObject("MenuPage", MenuPage);
		}
	}
 
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

	}
	
}