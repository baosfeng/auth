package xyz.bsfeng.auth.utils;

import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.exception.AuthException;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TokenUtils {

	private static TokenDao tokenDao;

	private static String tokenName;
	private static String[] readFrom;
	private static Boolean ignoreCamelCase;
	private static List<String> whiteTokenList;
	private static long timeout;
	private static String tokenType;
	private static String tokenPrefix;

	public static void init() {
		AuthConfig authConfig = TokenManager.getConfig();
		tokenDao = TokenManager.getTokenDao();

		tokenName = authConfig.getTokenName();
		ignoreCamelCase = authConfig.getIgnoreCamelCase();
		if (ignoreCamelCase) {
			tokenName = authConfig.getTokenName().replaceAll("-", "");
		}
		readFrom = authConfig.getReadFrom().split(",");
		whiteTokenList = Arrays.asList(authConfig.getWhiteTokenList().split(","));
		timeout = authConfig.getTimeout();
		tokenType = authConfig.getTokenType();
		tokenPrefix = authConfig.getTokenPrefix();
	}

	public static String login(UserInfo userInfo) {
		List<String> tokenList = tokenDao.getTokenListById(userInfo.getId());
		if (TokenManager.getConfig().getGlobalShare()) {
			if (!CollectionUtils.isEmpty(tokenList)) {
				return tokenList.get(0);
			}
		}
		String token = getTokenKey();
		tokenDao.setUserInfo(token, userInfo, timeout);
		if (tokenList == null) {
			tokenList = new ArrayList<>();
		}
		tokenList.add(token);
		tokenDao.setTokenListById(userInfo.getId(), tokenList);
		return token;
	}

	public static void logout() {
		tokenDao.deleteUserInfo(getToken());
	}

	public static Long getId() {
		UserInfo user = getUser();
		if (user == null) {
			throw new AuthException(AuthConstant.NOT_LOGIN_CODE, AuthConstant.NOT_LOGIN_MESSAGE);
		}
		return user.getId();
	}

	public static UserInfo getUser() {
		String userKey = getToken();
		// 如果为白名单token, 返回-1
		if (whiteTokenList.stream().anyMatch(itm -> itm.equalsIgnoreCase(userKey))) {
			return () -> -1L;
		}
		return (UserInfo) tokenDao.getUserInfo(userKey);
	}

	public static String getToken() {
		String userKey = "";
		HttpServletRequest servletRequest = SpringMVCUtil.getRequest();
		for (String from : readFrom) {
			if (StringUtils.isNotEmpty(userKey)) {
				break;
			}
			switch (from) {
				case AuthConstant.READ_FROM_HEADER:
					if (!ignoreCamelCase) {
						userKey = servletRequest.getHeader(tokenName);
					}
					Enumeration<String> headerNames = servletRequest.getHeaderNames();
					while (headerNames.hasMoreElements()) {
						String originHeader = headerNames.nextElement();
						String element = originHeader.replaceAll("-", "").trim();
						if (element.equalsIgnoreCase(tokenName)) {
							userKey = servletRequest.getHeader(originHeader);
							break;
						}
					}
					break;
				case AuthConstant.READ_FROM_URL:
					if (!ignoreCamelCase) {
						userKey =  servletRequest.getParameter(tokenName);
					}
					Enumeration<String> parameterNames = servletRequest.getParameterNames();
					while (parameterNames.hasMoreElements()) {
						String originParam = parameterNames.nextElement();
						String element = originParam.replaceAll("-", "").trim();
						if (element.equalsIgnoreCase(tokenName)) {
							userKey = servletRequest.getParameter(originParam);
							break;
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


	private static String getTokenKey() {
		tokenType = tokenType.toLowerCase();
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
		token = tokenPrefix + token;
		while (tokenDao.getUserInfo(token) != null) {
			token = getTokenKey();
		}
		return token;
	}
}
