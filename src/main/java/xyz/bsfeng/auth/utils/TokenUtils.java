package xyz.bsfeng.auth.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.dao.TempUser;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.pojo.AuthLoginModel;
import xyz.bsfeng.auth.pojo.AuthUser;
import xyz.bsfeng.auth.pojo.UserModel;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static xyz.bsfeng.auth.constant.AuthConstant.*;

public class TokenUtils {

	private static final Logger log = LoggerFactory.getLogger(TokenUtils.class);

	private static TokenDao tokenDao;

	private static String[] tokenNames;
	private static String[] readFrom;
	private static Boolean ignoreCamelCase;
	private static List<String> whiteTokenList;
	private static long timeout;
	private static String tokenType;
	private static String tokenPrefix;
	private static Boolean allowSampleDevice;
	private static final Long ONE_DAY = 24 * 60 * 60 * 1000L;
	private static Boolean enable;
	private static Boolean isLog;

	private static final Cache<String, UserInfo> userCache;
	private static final Cache<String, UserModel> idCache;

	private TokenUtils() {
	}

	static {
		userCache = CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.DAYS)
				.recordStats()
				.build();
		idCache = CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.DAYS)
				.recordStats()
				.build();
	}

	public static void init() {
		AuthConfig authConfig = TokenManager.getConfig();
		tokenDao = TokenManager.getTokenDao();

		String tokenName = authConfig.getTokenName();
		ignoreCamelCase = authConfig.getIgnoreCamelCase();
		if (ignoreCamelCase) {
			tokenName = authConfig.getTokenName().replaceAll("-", "");
		}
		tokenNames = tokenName.split(",");
		readFrom = authConfig.getReadFrom().split(",");
		whiteTokenList = Arrays.asList(authConfig.getWhiteTokenList().split(","));
		timeout = authConfig.getTimeout();
		tokenType = authConfig.getTokenType().toLowerCase();
		tokenPrefix = authConfig.getTokenPrefix();
		allowSampleDevice = authConfig.getAllowSampleDeviceLogin();
		enable = authConfig.getEnable();
		isLog = authConfig.getLog();
	}

	public static String login(AuthUser userInfo) {
		return login(userInfo, null);
	}

	/**
	 * 此方法用于实现多端登录,注意,如果使用了全局共享token,那么将不会派上用场,返回仍旧为原始token
	 *
	 * 如果仅配置了不允许多端登录,但是没有配置loginModel,此方法依然无效
	 * 默认允许多端登录
	 *
	 * @param userInfo   待登录的用户信息
	 * @param loginModel 用户的额外配置信息
	 * @return 登录token
	 */
	public static String login(AuthUser userInfo, AuthLoginModel loginModel) {
		if (BooleanUtils.isFalse(enable)) {
			throw new AuthException(414, "权限框架未启动!");
		}
		Long id = userInfo.getId();
		Map<String, UserModel> tokenInfoMap = tokenDao.getTokenInfoMapById(id);
		if (CollectionUtils.isNotEmpty(tokenInfoMap)) {
			// 处理账号封禁情况
			for (String token : tokenInfoMap.keySet()) {
				UserInfo user = userCache.getIfPresent(token);
				if (user == null) {
					user = (UserInfo) tokenDao.getUserInfo(token);
				}
				checkUser(user);
			}
		}
		// 处理全局共享token
		if (TokenManager.getConfig().getGlobalShare() && CollectionUtils.isNotEmpty(tokenInfoMap)) {
			return processGlobalShare(userInfo, tokenInfoMap);
		}
		String token = getTokenKey();
		tokenDao.setUserInfo(token, userInfo, timeout);
		if (CollectionUtils.isEmpty(tokenInfoMap)) {
			tokenInfoMap = new HashMap<>(4);
		}
		// 设置是否允许多端登录
		if (!allowSampleDevice && loginModel != null) {
			// 设置被挤下线的用户相关信息
			Iterator<UserModel> iterator = tokenInfoMap.values().stream()
					.filter(itm -> itm.getDevice().equals(loginModel.getDevice())).iterator();
			while (iterator.hasNext()) {
				UserModel userModel = iterator.next();
				userModel.setToken(token);
				userModel.setOfflineTime(System.currentTimeMillis());
			}
		}
		UserModel userModel = new UserModel()
				.setDevice(loginModel == null ? null : loginModel.getDevice())
				.setExpireTime(System.currentTimeMillis() + ONE_DAY);
		tokenInfoMap.put(token, userModel);
		tokenDao.setTokenInfoMapById(id, tokenInfoMap);
		if (isLog) log.info("登录成功,当前登录用户为:{}", userInfo);
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
		if (BooleanUtils.isFalse(enable)) {
			throw new AuthException(414, "权限框架未启动!");
		}
		Long id = authUser.getId();
		if (id == null) {
			authUser.setId(-2L);
		}
		String token;
		if (StringUtils.isNotEmpty(field)) {
			token = DigestUtils.md5DigestAsHex(field.getBytes(StandardCharsets.UTF_8));
		} else {
			token = getTokenKey();
			token = token.replace(tokenPrefix, "");
			token = tokenPrefix + TEMP_PREFIX + token;
		}
		tokenDao.setUserInfo(token, authUser, expireTime);
		if (isLog) log.info("登录成功,当前登录用户为:{}", authUser);
		return token;
	}

	/**
	 * 用于二次校验身份
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
		kickOut(getToken());
	}

	/**
	 * 踢出当前的所有登录，所有token全部失效,注意账户被封禁无法正常退出
	 */
	public static void kickOut() {
		Long id = getId();
		Map<String, UserModel> tokenInfoMap = tokenDao.getTokenInfoMapById(id);
		for (String token : tokenInfoMap.keySet()) {
			kickOut(token);
		}
		tokenDao.deleteTokenListById(id);
		if (isLog) log.info("正在踢出{}的用户所有token", id);
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
		// 更新用户拥有的token集合
		Object userInfo = tokenDao.getUserInfo(token);
		if (userInfo == null) {
			if (isLog) log.warn("用户已退出登录");
			return;
		}
		UserInfo user = (UserInfo) userInfo;
		checkUser(user);
		Long id = user.getId();
		Map<String, UserModel> tokenInfoMap = tokenDao.getTokenInfoMapById(id);
		if (CollectionUtils.isNotEmpty(tokenInfoMap)) {
			tokenInfoMap.remove(token);
			// 如果删除之后仅剩一个,那么删除
			if (CollectionUtils.isEmpty(tokenInfoMap)) {
				tokenDao.deleteTokenListById(id);
			}
		}
		tokenDao.deleteUserInfo(token);
		userCache.invalidate(token);
		if (isLog) log.debug("正在踢出{}的用户", token);
	}

	/**
	 * 封禁指定id的用户,且封禁所有token,如果已经封禁了,那么将会是本次封禁起效果
	 *
	 * @param id       用户id
	 * @param lockTime 封禁时间
	 */
	public static void lock(long id, long lockTime) {
		Map<String, UserModel> tokenInfoMap = tokenDao.getTokenInfoMapById(id);
		if (CollectionUtils.isEmpty(tokenInfoMap)) {
			// 未登录的用户将自动帮他进行登录
			if (TokenManager.getConfig().getKickOutIgnoreLogin()) {
				AuthUser authUser = new AuthUser(id);
				login(authUser, new AuthLoginModel().setTimeout(lockTime * 1000));
				lock(id, lockTime);
				return;
			}
		}
		Set<String> tokenList = tokenInfoMap.keySet();
		if (CollectionUtils.isNotEmpty(tokenList)) {
			for (String token : tokenList) {
				lock(token, lockTime);
			}
		}
		if (isLog) log.info("封禁id为{}时间{}秒", id, lockTime);
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
		userCache.put(token, user);
	}

	public static Long getId() {
		HttpServletRequest request = SpringMVCUtil.getRequest();
		Object id = request.getAttribute("userId");
		if (id != null) {
			return (Long) id;
		}
		UserInfo user = getUser();
		return user.getId();
	}

	public static UserInfo getUser() {
		return getUserInfo();
	}


	public static String getToken() {
		HttpServletRequest servletRequest = SpringMVCUtil.getRequest();
		Object attribute = servletRequest.getAttribute("token");
		if (attribute != null) return (String) attribute;
		String token = "";
		String tokenFrom = "";
		String currentTokenName = "";
		for (String from : readFrom) {
			for (String tokenName : tokenNames) {
				if (StringUtils.isNotEmpty(token)) break;
				switch (from) {
					case AuthConstant.READ_FROM_HEADER:
						currentTokenName = tokenName;
						token = doReadFromHeader(token, servletRequest, tokenName);
						break;
					case AuthConstant.READ_FROM_URL:
						currentTokenName = tokenName;
						token = doReadFromUrl(token, servletRequest, tokenName);
						break;
					default:
						throw new AuthException(AuthConstant.TYPE_NOT_SUPPORT_CODE, AuthConstant.TYPE_NOT_SUPPORT_MESSAGE);
				}
			}
			if (StringUtils.isNotEmpty(token)) {
				tokenFrom = from;
				break;
			}
		}
		if (StringUtils.isEmpty(token)) {
			throw new AuthException(AuthConstant.TOKEN_EMPTY_CODE, "无法从请求体中获得"+Arrays.toString(tokenNames)+"信息,请检查token名称是否正确");
		}
		if (isLog) log.debug("从{}中获取到{}:{}", tokenFrom, currentTokenName, token);
		servletRequest.setAttribute("token", token);
		return token;
	}

	private static String doReadFromUrl(String userKey, HttpServletRequest servletRequest, String tokenName) {
		if (!ignoreCamelCase) {
			return servletRequest.getParameter(tokenName);
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
		return userKey;
	}

	private static String doReadFromHeader(String userKey, HttpServletRequest servletRequest, String tokenName) {
		if (!ignoreCamelCase) {
			return servletRequest.getHeader(tokenName);
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
		// 如果未设置权限框架
		if (BooleanUtils.isFalse(enable)) {
			return new AuthUser(-3L);
		}
		HttpServletRequest request = SpringMVCUtil.getRequest();
		Object attribute = request.getAttribute("userInfo");
		if (attribute != null) {
			UserInfo userInfo = (UserInfo) attribute;
			if (BooleanUtils.isFalse(TokenManager.getConfig().getAllowSampleDeviceLogin())) {
				checkUser(userInfo);
			}
			return userInfo;
		}
		// 如果为白名单url, 返回-2
		boolean isWhiteUrl = (boolean) request.getAttribute("isWhiteUrl");
		if (isWhiteUrl) {
			return new AuthUser(-2L);
		}
		String token = getToken();
		// 如果为白名单token, 返回-1
		if (whiteTokenList.stream().anyMatch(itm -> itm.equalsIgnoreCase(token))) {
			return new AuthUser(-1L);
		}
		// 使用本地缓存进行快速的获取
		UserInfo info = userCache.getIfPresent(token);
		if (info != null) {
			checkUser(info);
			return info;
		}
		UserInfo userInfo = (UserInfo) tokenDao.getUserInfo(token);
		checkUser(userInfo);
		request.setAttribute("userInfo", userInfo);
		request.setAttribute("userId", userInfo.getId());
		// 非临时用户可放入
		if (!(userInfo instanceof TempUser)) {
			userCache.put(token, userInfo);
		}
		return userInfo;
	}

	private static void checkUser(UserInfo userInfo) {
		// 校验是否登录
		if (userInfo == null) {
			throw new AuthException(AuthConstant.NOT_LOGIN_CODE, AuthConstant.NOT_LOGIN_MESSAGE);
		}
		HttpServletRequest request = SpringMVCUtil.getRequest();
		Object obj = request.getAttribute("token");
		if (obj == null) return;
		String token = (String) obj;
		UserModel userModel = idCache.getIfPresent(token);
		if (userModel == null && !token.startsWith(AuthConstant.TEMP_PREFIX)) {
			userModel = tokenDao.getTokenInfoByToken(userInfo.getId(), token);
			idCache.put(token, userModel);
		}
		// 检查是否被封禁
		if (BooleanUtils.isTrue(userInfo.getLock())) {
			long millis = System.currentTimeMillis();
			if (millis < userInfo.getLockTime()) {
				long lessTime = userInfo.getLockTime() - millis;
				throw new AuthException(ACCOUNT_LOCK_CODE, ACCOUNT_LOCK_MESSAGE + TimeUtils.mill2Time(lessTime));
			}
			// 如果已经过了锁定时间,那么解封用户
			userInfo.setLock(false);
			tokenDao.updateUserInfo(token, userInfo);
			// 更新用户信息
			userCache.put(token, userInfo);
		}
		// 检查是否被挤下线
		if (userModel != null && userModel.getOfflineTime() != null) {
			throw new AuthException(413, "当前登录的用户在" + TimeUtils.longToTime(userModel.getOfflineTime()) + "被另一台设备挤下线!");
		}
	}

	private static String processGlobalShare(UserInfo userInfo, Map<String, UserModel> tokenInfoMap) {
		Iterator<String> iterator = tokenInfoMap.keySet().iterator();
		String token = iterator.next();
		// 检查相关用户信息是否需要更新
		UserInfo user = (UserInfo) tokenDao.getUserInfo(token);
		if (user == null) {
			tokenInfoMap.remove(token);
		}
		if (!userInfo.equals(user)) {
			tokenDao.updateUserInfo(token, userInfo);
		}
		long currentTime = System.currentTimeMillis();
		tokenInfoMap.forEach((key, value) -> value.setExpireTime(currentTime + ONE_DAY));
		tokenDao.setTokenInfoMapById(userInfo.getId(), tokenInfoMap);
		return token;
	}
}
