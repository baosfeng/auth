package xyz.bsfeng.auth.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.anno.FieldSensitive;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.constant.SensitiveEnum;
import xyz.bsfeng.auth.dao.TempUser;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.dao.UserModel;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.pojo.AuthLoginModel;
import xyz.bsfeng.auth.pojo.AuthUser;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static xyz.bsfeng.auth.constant.AuthConstant.*;

public class TokenUtils {

	private static final Logger log = LoggerFactory.getLogger(TokenUtils.class);

	private static TokenDao tokenDao;

	private static String[] tokenNames;
	private static String[] readFrom;
	private static Boolean ignoreCamelCase;
	private static List<String> whiteTokenList;
	private static List<String> whiteUrlList;
	private static long timeout;
	private static String tokenType;
	private static String tokenPrefix;
	private static Boolean checkWhiteUrlToken;
	private static Boolean allowSampleDevice;
	private static final Long ONE_DAY = 24 * 60 * 60 * 1000L;
	private static Boolean enable;
	private static final ConcurrentHashMap<Class<?>, List<Field>> FIELDS_MAP = new ConcurrentHashMap<>();

	private TokenUtils() {
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
		whiteUrlList = Arrays.stream(authConfig.getWhiteUrlList().split(",")).collect(Collectors.toList());
		whiteUrlList.add("/favicon.ico");
		whiteUrlList.add("/error");
		checkWhiteUrlToken = authConfig.getCheckWhiteUrlToken();
		allowSampleDevice = authConfig.getAllowSampleDeviceLogin();
		enable = authConfig.getEnable();
	}

	public static String login(UserInfo userInfo) {
		return login(userInfo, null);
	}

	/**
	 * 此方法用于实现多端登录,注意,如果使用了全局共享token,那么将不会派上用场,返回仍旧为原始token
	 *
	 * @param userInfo   待登录的用户信息
	 * @param loginModel 用户的额外配置信息
	 * @return 登录token
	 */
	public static String login(UserInfo userInfo, AuthLoginModel loginModel) {
		if (BooleanUtils.isFalse(enable)) {
			throw new AuthException(414, "权限框架未启动!");
		}
		Long id = userInfo.getId();
		if (id == null) {
			throw new IllegalArgumentException("id不能为空");
		}
		Map<String, UserModel> tokenInfoMap = tokenDao.getTokenInfoMapById(id);
		// 处理全局共享token
		if (TokenManager.getConfig().getGlobalShare() && CollectionUtils.isNotEmpty(tokenInfoMap)) {
			return processGlobalShare(userInfo, tokenInfoMap);
		}
		if (CollectionUtils.isNotEmpty(tokenInfoMap)) {
			// 处理账号封禁情况
			for (String token : tokenInfoMap.keySet()) {
				UserInfo user = (UserInfo) tokenDao.getUserInfo(token);
				if (user.getLock()) {
					throw new AuthException(412, "账户已被封禁!");
				}
			}
		}
		String token = getTokenKey();
		processSensitive(userInfo);
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
			log.warn("用户已退出登录");
			return;
		}
		UserInfo user = (UserInfo) userInfo;
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
	}

	/**
	 * 封禁指定id的用户,且封禁所有token
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
	}

	/**
	 * 根据指定的token进行封禁
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
			log.warn("待封禁的token已过期");
			return;
		}
		user.setLock(true);
		user.setLockTime(lockTime * 1000 + System.currentTimeMillis());
		tokenDao.updateUserInfo(token, user);
	}

	public static Long getId() {
		HttpServletRequest request = SpringMVCUtil.getRequest();
		Object id = request.getAttribute("id");
		if (id != null) {
			return (Long) id;
		}
		UserInfo user = getUser();
		return user.getId();
	}

	public static UserInfo getUser() {
		UserInfo userInfo = getUserInfo();
		if (userInfo.getLock() != null && userInfo.getLock()) {
			long millis = System.currentTimeMillis();
			if (millis < userInfo.getLockTime()) {
				long lessTime = userInfo.getLockTime() - millis;
				throw new AuthException(ACCOUNT_LOCK_CODE, ACCOUNT_LOCK_MESSAGE + TimeUtils.mill2Time(lessTime));
			}
			userInfo.setLock(false);
			String userKey = getToken();
			tokenDao.updateUserInfo(userKey, userInfo);
		}
		return userInfo;
	}


	public static String getToken() {
		String userKey = "";
		HttpServletRequest servletRequest = SpringMVCUtil.getRequest();
		for (String from : readFrom) {
			for (String tokenName : tokenNames) {
				if (StringUtils.isNotEmpty(userKey)) {
					break;
				}
				switch (from) {
					case AuthConstant.READ_FROM_HEADER:
						userKey = doReadFromHeader(userKey, servletRequest, tokenName);
						break;
					case AuthConstant.READ_FROM_URL:
						userKey = doReadFromUrl(userKey, servletRequest, tokenName);
						break;
					default:
						throw new AuthException(AuthConstant.TYPE_NOT_SUPPORT_CODE, AuthConstant.TYPE_NOT_SUPPORT_MESSAGE);
				}
			}
		}
		if (StringUtils.isEmpty(userKey)) {
			throw new AuthException(AuthConstant.TOKEN_EMPTY_CODE, TOKEN_EMPTY_MESSAGE);
		}
		return userKey;
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
		HttpServletRequest request = SpringMVCUtil.getRequest();
		Object attribute = request.getAttribute("userInfo");
		if (attribute != null) {
			return (UserInfo) attribute;
		}
		if (BooleanUtils.isFalse(enable)) {
			return new UserInfo() {
				@Override
				public Long getId() {
					return -3L;
				}

				@Override
				public void setId(Long id) {

				}
			};
		}
		// 如果为白名单url, 返回-2
		boolean isWhiteUrl = checkWhiteUrl();
		if (isWhiteUrl && !checkWhiteUrlToken) {
			return new UserInfo() {
				@Override
				public Long getId() {
					return -2L;
				}

				@Override
				public void setId(Long id) {

				}
			};
		}
		// 当检测到白名单的token为空时，返回-2
		if (isWhiteUrl) {
			try {
				getToken();
			} catch (AuthException e) {
				return new UserInfo() {
					@Override
					public Long getId() {
						return -2L;
					}

					@Override
					public void setId(Long id) {

					}
				};
			}
		}
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
		UserModel userModel = tokenDao.getTokenInfoByToken(userInfo.getId(), userKey);
		if (userModel != null && userModel.getOfflineTime() != null) {
			throw new AuthException(413, "当前登录的用户在" + TimeUtils.longToTime(userModel.getOfflineTime()) + "被另一台设备挤下线!");
		}
		return userInfo;
	}

	public static boolean checkWhiteUrl() {
		HttpServletRequest request = SpringMVCUtil.getRequest();
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

	private static void processSensitive(UserInfo userInfo) {
		List<Field> fieldList = FIELDS_MAP.get(userInfo.getClass());
		if (CollectionUtils.isEmpty(fieldList)) {
			HashSet<Field> fields = ReflectUtils.getFields(userInfo.getClass());
			fieldList = fields.stream()
					.filter(itm -> itm.getAnnotation(FieldSensitive.class) != null).collect(Collectors.toList());
			FIELDS_MAP.put(userInfo.getClass(), fieldList);
		}
		if (CollectionUtils.isEmpty(fieldList)) {
			return;
		}
		for (Field field : fieldList) {
			String className = field.getType().getSimpleName();
			if (!className.endsWith("String")) {
				throw new RuntimeException(className + "类型不受支持");
			}
			field.setAccessible(true);
			FieldSensitive annotation = field.getAnnotation(FieldSensitive.class);
			SensitiveEnum sensitiveEnum = annotation.value();
			try {
				Method method = ReflectUtils.getMethodByField(userInfo.getClass(), field);
				String value = (String) method.invoke(userInfo);
				switch (sensitiveEnum) {
					case PASSWORD:
						field.set(userInfo, null);
						break;
					case ID_CARD:
						String mixStr = StringUtils.mixStr(value.length() - 10);
						value = value.substring(0, 6) + mixStr + value.substring(value.length() - 4);
						field.set(userInfo, value);
						break;
					case EMAIL:
						mixStr = StringUtils.mixStr(value.indexOf("@") - 4);
						value = value.substring(0, 4) + mixStr + value.substring(value.indexOf("@"));
						field.set(userInfo, value);
						break;
					case BANK_CARD:
						mixStr = StringUtils.mixStr(value.length() - 8);
						value = value.substring(0, 4) + mixStr + value.substring(value.length() - 4);
						field.set(userInfo, value);
						break;
					default:
						break;
				}
			} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	private static String processGlobalShare(UserInfo userInfo, Map<String, UserModel> tokenInfoMap) {
		Iterator<String> iterator = tokenInfoMap.keySet().iterator();
		String token = iterator.next();
		// 检查相关用户信息是否需要更新
		UserInfo user = (UserInfo) tokenDao.getUserInfo(token);
		if (BooleanUtils.isTrue(user.getLock())) {
			throw new AuthException(412, "账户已被封禁!");
		}
		if (!userInfo.equals(user)) {
			processSensitive(userInfo);
			tokenDao.updateUserInfo(token, userInfo);
		}
		long currentTime = System.currentTimeMillis();
		tokenInfoMap.forEach((key, value) -> value.setExpireTime(currentTime + ONE_DAY));
		tokenDao.setTokenInfoMapById(user.getId(), tokenInfoMap);
		return token;
	}
}
