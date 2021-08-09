package xyz.bsfeng.auth.dao;


import org.springframework.data.redis.core.RedisTemplate;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;

import java.util.concurrent.TimeUnit;

/**
 * @author bsfeng
 * @date 2021/8/7-10:01
 * @since 1.0
 */
public class RedisTokenDaoImpl implements TokenDao {

	private RedisTemplate<String, Object> redisTemplate;
	public RedisTokenDaoImpl(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	private static final String TOKEN_PREFIX = "user:login:";


	@Override
	public Object getUserInfo(String key) {
		return redisTemplate.opsForValue().get(getTokenKey(key));
	}

	@Override
	public void setUserInfo(String key, UserInfo userInfo, long timeout) {
		redisTemplate.opsForValue().set(getTokenKey(key), userInfo, timeout, TimeUnit.SECONDS);
	}

	@Override
	public void updateUserInfo(String key, UserInfo userInfo) {
		String tokenKey = getTokenKey(key);
		AuthConfig authConfig = TokenManager.getConfig();
		if (authConfig.getAutoRenew()) {
			setUserInfo(tokenKey, userInfo, authConfig.getTimeout());
			return;
		}
		long timeout = getTimeout(tokenKey);
		redisTemplate.opsForValue().set(tokenKey, userInfo, timeout, TimeUnit.SECONDS);
	}

	@Override
	public void deleteUserInfo(String key) {
		redisTemplate.delete(getTokenKey(key));
	}

	@Override
	public long getTimeout(String key) {
		Long expire = redisTemplate.getExpire(getTokenKey(key));
		return expire == null ? -2 : expire;
	}

	@Override
	public void updateTimeout(String key, long timeout) {
		redisTemplate.expire(getTokenKey(key), timeout, TimeUnit.SECONDS);
	}

	private String getTokenKey(String key) {
		if (key.startsWith(TOKEN_PREFIX)) {
			return key;
		}
		return TOKEN_PREFIX + key;
	}
}
