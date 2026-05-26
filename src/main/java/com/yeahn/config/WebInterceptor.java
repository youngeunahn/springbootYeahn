package com.yeahn.config;

import java.util.Enumeration;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.yeahn.menu.service.MenuService;
import com.yeahn.menu.dto.MenuConfig;
import com.yeahn.auth.dto.UserVo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
@RequiredArgsConstructor
public class WebInterceptor implements HandlerInterceptor {

	@Autowired
	private MenuService menuService;

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
		String requestURI = request.getRequestURI();

		logger.info("requestURI:::::::"+requestURI );
		if(requestURI.indexOf("/error") > -1){
		}else{
			List<MenuConfig> MenuList = menuService.getMenuList("menuList");
			if (modelAndView != null){
				for (MenuConfig menulist : MenuList){
					if (menulist.getMENU_LEVEL()==1)
						menulist.setMENU_LEVEL_VIEW(true);
					else if (menulist.getMENU_LEVEL()==2)
						menulist.setMENU_LEVEL_VIEW(false);
					menulist.setMENU_GROUP(menulist.getMENU_CODE().split("_")[0]);
				}
				modelAndView.addObject("MenuList", MenuList);
				addLoginUser(modelAndView);

				MenuConfig MenuPage = new MenuConfig();
				if(request.getParameter("menuCode") == null){
					MenuPage = menuService.getMenuPage("ROOT");
				} else{
					MenuPage = menuService.getMenuPage(request.getParameter("menuCode"));
				}
				modelAndView.addObject("MenuPage", MenuPage);
			}
		}
	}
 
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

	}

	private void addLoginUser(ModelAndView modelAndView) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return;
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof UserVo) {
			UserVo userVo = (UserVo) principal;
			String userName = userVo.getDisplayName();
			if (userName == null || userName.trim().isEmpty()) {
				userName = userVo.getUserId();
			}
			modelAndView.addObject("LoginUserName", userName);
		}
	}
	
}