package com.yizhu.auth.utils;

import com.yizhu.auth.config.AuthConfig;
import com.yizhu.auth.constant.AuthConstant;
import com.yizhu.auth.dao.TokenDao;
import com.yizhu.auth.dao.UserInfo;
import com.yizhu.auth.exception.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.DigestUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class TokenUtils {

	@Autowired
	private TokenDao tokenDao;
	@Autowired
	private AuthConfig authConfig;

	private String tokenName;
	private String[] readFrom;
	private Boolean ignoreCamelCase;
	private List<String> whiteTokenList;

	@PostConstruct
	public void init() {
		tokenName = authConfig.getTokenName().replaceAll("-", "");
		readFrom = authConfig.getReadFrom().split(",");
		ignoreCamelCase = authConfig.getIgnoreCamelCase();
		whiteTokenList = Arrays.asList(authConfig.getWhiteTokenList().split(","));
	}

	public String login(UserInfo userInfo) {
		String token = getTokenKey();
		tokenDao.setUserInfo(token, userInfo, authConfig.getTimeout());
		return token;
	}

	public void logout() {
		tokenDao.deleteUserInfo(getToken());
	}

	public Long getId() {
		UserInfo user = getUser();
		if (user == null) {
			throw new AuthException(AuthConstant.NOT_LOGIN_CODE, AuthConstant.NOT_LOGIN_MESSAGE);
		}
		return user.getId();
	}

	public UserInfo getUser() {
		String userKey = getToken();
		// 如果为白名单token, 返回-1
		if (whiteTokenList.stream().anyMatch(itm -> itm.equalsIgnoreCase(userKey))) {
			return new UserInfo() {
				@Override
				public Long getId() {
					return -1L;
				}
			};
		}
		return tokenDao.getUserInfo(userKey);
	}

	public String getToken() {
		String userKey = "";
		HttpServletRequest servletRequest = SpringMVCUtil.getRequest();
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


	private String getTokenKey() {
		String tokenType = authConfig.getTokenType().toLowerCase();
		String token;
		switch (tokenType) {
			case AuthConstant.TYPE_RANDOM16:
				token = StringUtils.randomString(16);
				break;
			case AuthConstant.TYPE_RANDOM32:
				token = StringUtils.randomString(32);
				break;
			case AuthConstant.TYPE_RANDOM64:
				token = StringUtils.randomString(64);
				break;
			case AuthConstant.TYPE_MD5:
				token = DigestUtils.md5DigestAsHex(StringUtils.randomString(20).getBytes(StandardCharsets.UTF_8));
				break;
			case AuthConstant.TYPE_UUID:
				token = UUID.randomUUID().toString().replaceAll("-", "");
				break;
			default:
				throw new IllegalArgumentException("token类型不受支持");
		}
		while (tokenDao.getUserInfo(token) != null) {
			token = getTokenKey();
		}
		return token;
	}
}
