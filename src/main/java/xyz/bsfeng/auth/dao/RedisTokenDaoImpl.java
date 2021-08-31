package xyz.bsfeng.auth.dao;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.exception.AuthException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static xyz.bsfeng.auth.constant.AuthConstant.OBJECT_SERIALIZER_CODE;
import static xyz.bsfeng.auth.constant.AuthConstant.OBJECT_SERIALIZER_MESSAGE;

/**
 * @author bsfeng
 * @date 2021/8/7-10:01
 * @since 1.0
 */
public class RedisTokenDaoImpl implements TokenDao {

	private final RedisTemplate<String, Object> redisTemplate;
	private static String TOKEN_PREFIX = "user:";
	private static String TEMP_TOKEN_SUFFIX = ":";
	private static String tokenPrefix;

	public RedisTokenDaoImpl(RedisTemplate<String, Object> redisTemplate, AuthConfig authConfig) {
		this.redisTemplate = redisTemplate;
		String loginType = authConfig.getLoginType();
		if (!loginType.endsWith(":")) {
			loginType += ":";
		}
		TOKEN_PREFIX += loginType;
		TEMP_TOKEN_SUFFIX = authConfig.getTempSuffix() + TEMP_TOKEN_SUFFIX;
		tokenPrefix = authConfig.getTokenPrefix();
	}

	@Override
	public Object getUserInfo(String key) {
		try {
			return redisTemplate.opsForValue().get(getTokenKey(key));
		} catch (SerializationException e) {
			throw new AuthException(OBJECT_SERIALIZER_CODE, OBJECT_SERIALIZER_MESSAGE);
		}
	}

	@Override
	public void setUserInfo(String key, UserInfo userInfo, long timeout) {
		redisTemplate.opsForValue().set(getTokenKey(key), userInfo, timeout, TimeUnit.SECONDS);
	}

	@Override
	public void updateUserInfo(String key, UserInfo userInfo) {
		String tokenKey = getTokenKey(key);
		// 更新临时身份用户信息
		if (userInfo instanceof TempUser) {
			long timeout = getTimeout(tokenKey);
			if (0 < timeout) {
				redisTemplate.opsForValue().set(tokenKey, userInfo, timeout, TimeUnit.SECONDS);
			}
			return;
		}
		// 防止出现封禁时间小于key过期时间
		AuthConfig authConfig = TokenManager.getConfig();
		long lockTime = (userInfo.getLockTime() - System.currentTimeMillis()) / 1000;
		if (authConfig.getAutoRenew()) {
			long maxTimeout = Math.max(authConfig.getTimeout(), lockTime);
			setUserInfo(tokenKey, userInfo, maxTimeout);
			return;
		}
		long expireTime = getTimeout(tokenKey);
		long maxTimeout = Math.max(expireTime, lockTime);
		if (0 < maxTimeout) {
			redisTemplate.opsForValue().set(tokenKey, userInfo, maxTimeout, TimeUnit.SECONDS);
		}
	}

	@Override
	public void deleteUserInfo(String key) {
		redisTemplate.delete(getTokenKey(key));
	}

	@Override
	public long getTimeout(String key) {
		Long expire = redisTemplate.getExpire(getTokenKey(key), TimeUnit.SECONDS);
		return expire == null ? -2 : expire;
	}

	@Override
	public void updateTimeout(String key, long timeout) {
		redisTemplate.expire(getTokenKey(key), timeout, TimeUnit.SECONDS);
	}

	@Override
	public List<String> getTokenListById(Long id) {
		// 一个id对应多个token
		String idKey = getTokenKey(id + "");
		Object o = redisTemplate.opsForValue().get(idKey);
		if (o == null) {
			return null;
		}
		return Arrays.stream(o.toString().split(",")).collect(Collectors.toList());
	}

	@Override
	public void setTokenListById(Long id, List<String> tokenList) {
		HashSet<String> tokenStringSet = new HashSet<>(tokenList);
		String joinList = String.join(",", tokenStringSet);
		redisTemplate.opsForValue().set(getTokenKey(id + ""), joinList, TokenManager.getConfig().getTimeout(), TimeUnit.SECONDS);
	}

	@Override
	public void deleteTokenListById(Long id) {
		String idKey = getTokenKey(id + "");
		redisTemplate.delete(idKey);
	}

	private String getTokenKey(String key) {
		if (key.startsWith(TOKEN_PREFIX)) {
			return key;
		}
		if (key.startsWith(tokenPrefix + AuthConstant.TEMP_PREFIX)) {
			return TOKEN_PREFIX + TEMP_TOKEN_SUFFIX + key;
		}
		return TOKEN_PREFIX + key;
	}
}
