package xyz.bsfeng.auth.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.dao.TempUser;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.event.UserLoginEvent;
import xyz.bsfeng.auth.event.UserLogoutEvent;
import xyz.bsfeng.auth.event.UserTokenKickOutEvent;
import xyz.bsfeng.auth.event.UserTokenLockEvent;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.pojo.AuthUser;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static xyz.bsfeng.auth.constant.AuthConstant.*;

public class TokenUtils {

	private static final Logger log = LoggerFactory.getLogger(TokenUtils.class);

	private static TokenDao tokenDao;

	private static long timeout;
	private static String tokenType;
	private static String tokenPrefix;
	private static Boolean enable;
	private static Boolean isLog;

	private TokenUtils() {
	}

	public static void init() {
		AuthConfig authConfig = TokenManager.getConfig();
		tokenDao = TokenManager.getTokenDao();
		timeout = authConfig.getTimeout();
		tokenType = authConfig.getTokenType().toLowerCase();
		tokenPrefix = authConfig.getTokenPrefix();
		enable = authConfig.getEnable();
		isLog = authConfig.getLog();
	}

	/**
	 * 此方法用于实现多端登录,注意,如果使用了全局共享token,那么将不会派上用场,返回仍旧为原始token
	 * <p>
	 * 如果仅配置了不允许多端登录,但是没有配置loginModel,此方法依然无效
	 * 默认允许多端登录
	 *
	 * @param userInfo 待登录的用户信息
	 * @return 登录token
	 */
	public static String login(AuthUser userInfo) {
		if (AuthBooleanUtils.isFalse(enable)) {
			throw new AuthException(414, "权限框架未启动!");
		}
		if (userInfo.getId() == null) throw new AuthException(415, "用户id不能为空!");
		String token = getTokenKey();
		long expireTime = userInfo.getExpireTime();
		if (userInfo.getExpireTime() == null || userInfo.getExpireTime() == 0) {
			expireTime = timeout;
		}
		tokenDao.setUserInfo(token, userInfo, expireTime);
		if (isLog) log.info("登录成功,当前登录用户为:{}", userInfo);
		AuthSpringUtils.publishEvent(new UserLoginEvent(userInfo, token));
		return token;
	}

	/**
	 * 使用临时身份进行登录
	 *
	 * @param authUser   临时身份
	 * @param expireTime 过期时间
	 * @return 登录token
	 */
	public static String loginTemp(TempUser authUser, Long expireTime) {
		if (AuthBooleanUtils.isFalse(enable)) throw new AuthException(414, "权限框架未启动!");
		Long id = authUser.getId();
		if (id == null) authUser.setId(TEMP_ID);
		String token = getTokenKey();
		token = token.replace(tokenPrefix, "");
		token = tokenPrefix + TEMP_PREFIX + token;
		tokenDao.setUserInfo(token, authUser, expireTime);
		if (isLog) log.info("登录成功,当前登录用户为:{}", authUser);
		AuthSpringUtils.publishEvent(new UserLoginEvent(authUser, token));
		return token;
	}

	/**
	 * 用于二次校验身份
	 *
	 * @param authUser
	 */
	@SuppressWarnings("all")
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
	 * 默认退出的是当前账户的token,注意账户被封禁无法正常退出
	 */
	public static void logout() {
		String token = getToken();
		kickOut(token);
		AuthSpringUtils.publishEvent(new UserLogoutEvent(token));
	}

	/**
	 * 根据指定的token进行踢出用户
	 *
	 * @param token 当前正在使用的token
	 */
	public static void kickOut(String token) {
		if (AuthStringUtils.isEmpty(token)) {
			throw new AuthException(KICK_OUT_TOKEN_EMPTY_CODE, KICK_OUT_TOKEN_EMPTY_MESSAGE);
		}
		// 更新用户拥有的token集合
		UserInfo user = (UserInfo) AuthSpringMVCUtil.getRequest().getAttribute(USER_INFO);
		tokenDao.deleteUserInfo(token);
		if (isLog) log.debug("正在踢出{}的用户", token);
		AuthSpringUtils.publishEvent(new UserTokenKickOutEvent(user, token));
	}

	/**
	 * 根据指定的token进行封禁,如果已经封禁了,那么将会是本次封禁起效果
	 *
	 * @param token    用户token
	 * @param lockTime 封禁时间
	 */
	public static void lock(String token, long lockTime) {
		if (lockTime < 0) {
			throw new AuthException(LOCK_USER_TIME_VALID_CODE, LOCK_USER_TIME_VALID_MESSAGE);
		}
		UserInfo user = (UserInfo) tokenDao.getUserInfo(token);
		if (user == null) {
			if (isLog) log.warn("待封禁的用户token已过期");
			return;
		}
		user.setLock(true);
		user.setLockTime(lockTime * 1000 + System.currentTimeMillis());
		tokenDao.updateUserInfo(token, user);
		AuthSpringUtils.publishEvent(new UserTokenLockEvent(token, lockTime));
	}

	public static Long getId() {
		HttpServletRequest request = AuthSpringMVCUtil.getRequest();
		return (Long) request.getAttribute(USER_ID);
	}

	public static UserInfo getUser() {
		return getUserInfo();
	}


	public static String getToken() {
		HttpServletRequest servletRequest = AuthSpringMVCUtil.getRequest();
		Object attribute = servletRequest.getAttribute(TOKEN_NAME);
		return (String) attribute;
	}

	private static String getTokenKey() {
		String token;
		switch (tokenType) {
			case AuthConstant.TYPE_RANDOM16:
				token = AuthStringUtils.randomString(16);
				break;
			case AuthConstant.TYPE_RANDOM32:
				token = AuthStringUtils.randomString(32);
				break;
			case AuthConstant.TYPE_RANDOM64:
				token = AuthStringUtils.randomString(64);
				break;
			case AuthConstant.TYPE_MD5:
				token = DigestUtils.md5DigestAsHex(AuthStringUtils.randomString(20).getBytes(StandardCharsets.UTF_8));
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
		// 如果未设置权限框架
		if (AuthBooleanUtils.isFalse(enable)) {
			return new AuthUser(-3L);
		}
		HttpServletRequest request = AuthSpringMVCUtil.getRequest();
		return (UserInfo) request.getAttribute(USER_INFO);
	}

}
