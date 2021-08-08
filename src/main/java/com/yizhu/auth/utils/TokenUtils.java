package com.yizhu.auth.utils;

import com.yizhu.auth.config.AuthConfig;
import com.yizhu.auth.constant.AuthConstant;
import com.yizhu.auth.dao.TokenDao;
import com.yizhu.auth.dao.UserInfo;
import com.yizhu.auth.exception.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class TokenUtils {

	@Autowired
	private TokenDao tokenDao;
	@Autowired
	private AuthConfig authConfig;
	@Autowired
	private HttpServletRequest request;

	private String tokenName;
	private String[] readFrom;
	private Boolean ignoreCamelCase;

	@PostConstruct
	public void init() {
		tokenName = authConfig.getTokenName().replaceAll("-", "");
		readFrom = authConfig.getReadFrom().split(",");
		ignoreCamelCase = authConfig.getIgnoreCamelCase();
	}

	public void login(UserInfo userInfo) {
		tokenDao.setUserInfo(userInfo.getId() + "", userInfo);
	}

	public void logout(Long id) {
		tokenDao.deleteUserInfo(id + "");
	}

	public Long getId() {
		return getUser().getId();
	}

	public UserInfo getUser() {
		String userKey = getUserTokenValue(request);
		return tokenDao.getUserInfo(userKey);
	}
	public String getUserTokenValue(HttpServletRequest servletRequest) {
		String userKey = "";
		for (String from : readFrom) {
			switch (from) {
				case AuthConstant.READ_FROM_HEADER:
					if (!ignoreCamelCase) {
						return servletRequest.getHeader(tokenName);
					}
					Enumeration<String> headerNames = servletRequest.getHeaderNames();
					while (headerNames.hasMoreElements()) {
						String element = headerNames.nextElement().replaceAll("-", "").trim();
						if (element.equalsIgnoreCase(tokenName)) {
							return servletRequest.getHeader(element);
						}
					}
					break;
				case AuthConstant.READ_FROM_URL:
					if (!ignoreCamelCase) {
						return servletRequest.getParameter(tokenName);
					}
					Enumeration<String> parameterNames = servletRequest.getParameterNames();
					while (parameterNames.hasMoreElements()) {
						String element = parameterNames.nextElement();
						if (element.equalsIgnoreCase(tokenName)) {
							return servletRequest.getParameter(element);
						}
					}
					break;
				default:
					throw new AuthException(AuthConstant.TYPE_NOT_SUPPORT_CODE, AuthConstant.TYPE_NOT_SUPPORT_MESSAGE);
			}
		}
		if (StringUtils.isEmpty(userKey)) {
			throw new AuthException(AuthConstant.NOT_LOGIN_CODE, AuthConstant.NOT_LOGIN_MESSAGE);
		}
		return userKey;
	}

}
