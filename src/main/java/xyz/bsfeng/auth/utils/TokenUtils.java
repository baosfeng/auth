package xyz.bsfeng.auth.utils;

import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.dao.TempUser;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.exception.AuthException;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static xyz.bsfeng.auth.constant.AuthConstant.*;

public class TokenUtils {

	private static TokenDao tokenDao;

	private static String tokenName;
	private static String[] readFrom;
	private static Boolean ignoreCamelCase;
	private static List<String> whiteTokenList;
	private static long timeout;
	private static String tokenType;
	private static String tokenPrefix;

	public TokenUtils() {
	}

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
		tokenType = authConfig.getTokenType().toLowerCase();
		tokenPrefix = authConfig.getTokenPrefix();
	}

	public static String login(UserInfo userInfo) {
		Long id = userInfo.getId();
		if (id == null) {
			throw new IllegalArgumentException("id不能为空");
		}
		List<String> tokenList = tokenDao.getTokenListById(id);
		if (TokenManager.getConfig().getGlobalShare()) {
			if (!CollectionUtils.isEmpty(tokenList)) {
				String token = tokenList.get(0);
				// 检查相关用户信息是否需要更新
				UserInfo user = (UserInfo) tokenDao.getUserInfo(token);
				if (!userInfo.equals(user)) {
					tokenDao.updateUserInfo(token, userInfo);
				}
				return token;
			}
		}
		String token = getTokenKey();
		tokenDao.setUserInfo(token, userInfo, timeout);
		if (tokenList == null) {
			tokenList = new ArrayList<>();
		}
		tokenList.add(token);
		tokenDao.setTokenListById(id, tokenList);
		return token;
	}

	public static String loginTemp(TempUser authUser, Long expireTime) {
		return loginTemp(authUser, expireTime, null);
	}

	/**
	 * 使用临时身份进行登录
	 *
	 * @param authUser   临时身份
	 * @param expireTime 过期时间
	 * @param field      用于标记是否要为同一个资源生成同一份token,常用于资源缓存
	 * @return 登录token
	 */
	public static String loginTemp(TempUser authUser, Long expireTime, String field) {
		Long id = getId();
		if (id == null) {
			throw new IllegalArgumentException("id不能为空");
		}
		authUser.setId(id);
		String token;
		if (StringUtils.isNotEmpty(field)) {
			token = DigestUtils.md5DigestAsHex(field.getBytes(StandardCharsets.UTF_8));
		} else {
			token = getTokenKey();
		}
		tokenDao.setUserInfo(token, authUser, expireTime);
		return token;
	}

	public static void checkTempUser(TempUser authUser) {
		UserInfo user = getUser();
		if (user instanceof TempUser) {
			TempUser tempUser = (TempUser) (user);
			try {
				boolean check = tempUser.check(authUser);
				if (!check) {
					throw new AuthException(TEMP_TOKEN_VALID_CODE, TEMP_TOKEN_VALID_MESSAGE);
				}

				// 一般来说,临时身份校验完毕,身份的权限可使用信息都会被消耗一部分,因此要及时更新
				tokenDao.updateUserInfo(getToken(), tempUser);
			} catch (RuntimeException e) {
				throw e;
			}
		}
	}

	/**
	 * 默认退出的是当前账户的token
	 */
	public static void logout() {
		kickOut(getToken());
	}

	/**
	 * 踢出当前的所有登录，所有token全部失效
	 */
	public static void kickOut() {
		Long id = getId();
		List<String> tokenList = tokenDao.getTokenListById(id);
		for (String token : tokenList) {
			kickOut(token);
		}
		tokenDao.deleteTokenListById(id);
	}

	/**
	 * 根据指定的token进行踢出用户
	 *
	 * @param token 当前正在使用的token
	 */
	public static void kickOut(String token) {
		if (StringUtils.isEmpty(token)) {
			throw new AuthException(KICK_OUT_TOKEN_EMPTY_CODE, KICK_OUT_TOKEN_EMPTY_MESSAGE);
		}
		tokenDao.deleteUserInfo(token);
	}

	/**
	 * 封锁时间为秒
	 *
	 * @param lockTime 封禁的时间
	 */
	public static void lock(long lockTime) {
		if (lockTime < 0) {
			throw new AuthException(LOCK_USER_TIME_VALID_CODE, LOCK_USER_TIME_VALID_MESSAGE);
		}
		UserInfo user = getUserInfo();
		user.setLock(true);
		// 方便时间过期之后修改回正常未锁定的样子
		user.setLockTime(lockTime * 1000 + System.currentTimeMillis());
		tokenDao.updateUserInfo(getToken(), user);
	}

	public static Long getId() {
		UserInfo user = getUser();
		return user.getId();
	}

	public static UserInfo getUser() {
		String userKey = getToken();
		UserInfo userInfo = getUserInfo();
		if (userInfo.getLock()) {
			long millis = System.currentTimeMillis();
			if (millis < userInfo.getLockTime()) {
				long lessTime = userInfo.getLockTime() - millis;
				throw new AuthException(ACCOUNT_LOCK_CODE, ACCOUNT_LOCK_MESSAGE + TimeUtils.mill2Time(lessTime));
			}
			userInfo.setLock(false);
			tokenDao.updateUserInfo(userKey, userInfo);
		}
		return userInfo;
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
						userKey = servletRequest.getParameter(tokenName);
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
			throw new AuthException(AuthConstant.TOKEN_EMPTY_CODE, TOKEN_EMPTY_MESSAGE);
		}
		return userKey;
	}


	private static String getTokenKey() {
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

	/**
	 * 不提供对外使用，仅限工具类内部使用，不做账号是否过期校验
	 *
	 * @return 账户相关信息
	 */
	private static UserInfo getUserInfo() {
		String userKey = getToken();
		// 如果为白名单token, 返回-1
		if (whiteTokenList.stream().anyMatch(itm -> itm.equalsIgnoreCase(userKey))) {
			return new UserInfo() {
				@Override
				public Long getId() {
					return -1L;
				}

				@Override
				public void setId(Long id) {

				}

			};
		}
		UserInfo userInfo = (UserInfo) tokenDao.getUserInfo(userKey);
		if (userInfo == null) {
			throw new AuthException(AuthConstant.NOT_LOGIN_CODE, AuthConstant.NOT_LOGIN_MESSAGE);
		}
		return userInfo;
	}
}
