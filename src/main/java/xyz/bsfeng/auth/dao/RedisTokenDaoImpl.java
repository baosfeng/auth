package xyz.bsfeng.auth.dao;


import org.springframework.data.redis.core.RedisTemplate;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.pojo.UserModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
		return redisTemplate.opsForValue().get(getTokenKey(key));
	}

	@Override
	public void setUserInfo(String key, UserInfo userInfo, long timeout) {
		if (timeout == -1) {
			redisTemplate.opsForValue().set(getTokenKey(key), userInfo);
			return;
		}
		if (timeout <= 0) {
			return;
		}
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
		long lockTime = (userInfo.getLockTime() == null ? 0 : userInfo.getLockTime() - System.currentTimeMillis()) / 1000;
		// 配置自动刷新key有效时间
		if (authConfig.getAutoRenew()) {
			long maxTimeout = Math.max(authConfig.getTimeout(), lockTime);
			setUserInfo(tokenKey, userInfo, maxTimeout);
			// 更新用户拥有的token配置信息
			String idKey = getTokenKey(userInfo.getId() + "");
			redisTemplate.expire(idKey, maxTimeout, TimeUnit.SECONDS);
			return;
		}
		// 不自动刷新key的有效时间,选择有效时间和封禁时间中最大的一个
		long expireTime = getTimeout(tokenKey);
		long maxTimeout = Math.max(expireTime, lockTime);
		if (0 < maxTimeout) {
			redisTemplate.opsForValue().set(tokenKey, userInfo, maxTimeout, TimeUnit.SECONDS);
			// 更新用户拥有的token配置信息
			String idKey = getTokenKey(userInfo.getId() + "");
			redisTemplate.expire(idKey, maxTimeout, TimeUnit.SECONDS);
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
	public Map<String, UserModel> getTokenInfoMapById(Long id) {
		// 一个id对应多个token
		String idKey = getTokenKey(id + "");
		Map<Object, Object> entries = redisTemplate.opsForHash().entries(idKey);
		Map<String, UserModel> newMap = new HashMap<>();
		long currentTime = System.currentTimeMillis();
		// 删除过期的key
		for (Map.Entry<Object, Object> entry : entries.entrySet()) {
			UserModel userModel = (UserModel) entry.getValue();
			if (currentTime < userModel.getExpireTime()) {
				newMap.put((String) entry.getKey(), userModel);
			}
		}
		return newMap;
	}

	@Override
	public void setTokenInfoMapById(Long id, Map<String, UserModel> tokenMap) {
		String idKey = getTokenKey(id + "");
		Map<String, UserModel> newMap = new HashMap<>();
		long currentTime = System.currentTimeMillis();
		// 删除过期的key
		for (Map.Entry<String, UserModel> entry : tokenMap.entrySet()) {
			UserModel userModel = entry.getValue();
			if (currentTime < userModel.getExpireTime()) {
				newMap.put(entry.getKey(), userModel);
			}
		}
		redisTemplate.opsForHash().putAll(idKey, newMap);
		redisTemplate.expire(idKey, TokenManager.getConfig().getTimeout(), TimeUnit.SECONDS);
	}

	@Override
	public UserModel getTokenInfoByToken(Long id, String token) {
		String idKey = getTokenKey(id + "");
		return (UserModel) redisTemplate.opsForHash().get(idKey, token);
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
