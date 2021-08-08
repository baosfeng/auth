package com.yizhu.auth.interceptor;

import com.yizhu.auth.config.AuthConfig;
import com.yizhu.auth.constant.AuthConstant;
import com.yizhu.auth.dao.TokenDao;
import com.yizhu.auth.dao.UserInfo;
import com.yizhu.auth.exception.AuthException;
import com.yizhu.auth.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AuthInterceptor implements HandlerInterceptor {

	@Autowired
	private AuthConfig authConfig;
	@Autowired
	private TokenDao tokenDao;
	@Autowired
	private TokenUtils tokenUtils;

	private List<String> whiteUrlList;
	private List<String> whiteTokenList;

	@PostConstruct
	public void init() {
		whiteUrlList = Arrays.stream(authConfig.getWhiteUrlList().split(",")).collect(Collectors.toList());
		whiteUrlList.add("/favicon.ico");
		whiteTokenList = Arrays.asList(authConfig.getWhiteTokenList().split(","));
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (doWhiteUrl(request, response)) return true;

		String userKey = tokenUtils.getUserTokenValue(request);
		if (whiteTokenList.stream().anyMatch(itm -> itm.equalsIgnoreCase(userKey))) {
			return true;
		}

		UserInfo userInfo = tokenDao.getUserInfo(userKey);
		if (userInfo == null) {
			throw new AuthException(AuthConstant.NOT_LOGIN_CODE, AuthConstant.NOT_LOGIN_MESSAGE);
		}
		return true;
	}

	private boolean doWhiteUrl(HttpServletRequest request, HttpServletResponse response) {
		String uri = request.getRequestURI();
		for (String white : whiteUrlList) {
			if (white.endsWith("*")) {
				white = white.replace("*", "");
				if (uri.startsWith(white)) {
					return true;
				}
			}
			if (white.equalsIgnoreCase(uri)) {
				return true;
			}
		}
		return false;
	}


}
