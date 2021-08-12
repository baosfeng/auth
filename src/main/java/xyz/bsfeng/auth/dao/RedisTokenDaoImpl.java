package xyz.bsfeng.auth.dao;


import org.springframework.data.redis.core.RedisTemplate;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author bsfeng
 * @date 2021/8/7-10:01
 * @since 1.0
 */
public class RedisTokenDaoImpl implements TokenDao {

	private final RedisTemplate<String, Object> redisTemplate;
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
		// 更新临时身份用户信息
		if (userInfo instanceof TempUser) {
			long timeout = getTimeout(tokenKey);
			redisTemplate.opsForValue().set(tokenKey, userInfo, timeout, TimeUnit.SECONDS);
			return;
		}
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
		Long expire = redisTemplate.getExpire(getTokenKey(key), TimeUnit.SECONDS);
		return expire == null ? 0 : expire;
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
		ArrayList<String> tokenStringList = new ArrayList<>();
		long maxExpireTime = 0;
		for (String token : tokenList) {
			Long expire = redisTemplate.getExpire(getTokenKey(token));
			if (expire != null && expire != -2) {
				tokenStringList.add(token);
				maxExpireTime = Math.max(maxExpireTime, expire);
			}
		}
		String joinList = String.join(",", tokenStringList);
		redisTemplate.opsForValue().set(getTokenKey(id + ""), joinList, maxExpireTime, TimeUnit.SECONDS);
	}

	private String getTokenKey(String key) {
		if (TOKEN_PREFIX.startsWith(key)) {
			return key;
		}
		return TOKEN_PREFIX + key;
	}
}
