package com.yizhu.auth.dao;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yizhu.auth.TokenManager;
import com.yizhu.auth.config.AuthConfig;
import com.yizhu.auth.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author bsfeng
 * @date 2021/8/7-10:01
 * @since 1.0
 */
public class RedisTokenDaoImpl implements TokenDao {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	private static final String tokenPrefix = "user:login:";

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Logger log = LoggerFactory.getLogger(RedisTokenDaoImpl.class);

	@Override
	public UserInfo getUserInfo(String key) {
		String json = stringRedisTemplate.opsForValue().get(getTokenKey(key));
		if (StringUtils.isEmpty(json)) {
			return null;
		}
		try {
			return objectMapper.readValue(json, UserInfo.class);
		} catch (JsonProcessingException e) {
			log.error("获取对象出现异常", e);
		}
		return null;
	}

	@Override
	public void setUserInfo(String key, UserInfo userInfo, long timeout) {
		try {
			String json = objectMapper.writeValueAsString(userInfo);
			stringRedisTemplate.opsForValue().set(getTokenKey(key), json, timeout, TimeUnit.SECONDS);
		} catch (JsonProcessingException e) {
			log.error("保存转换对象出现异常", e);
		}
	}

	@Override
	public void updateUserInfo(String key, UserInfo userInfo) {
		String tokenKey = getTokenKey(key);
		AuthConfig authConfig = TokenManager.getConfig();
		if (authConfig.getAutoRenew()) {
			setUserInfo(tokenKey, userInfo, authConfig.getTimeout());
			return;
		}
		try {
			String json = objectMapper.writeValueAsString(userInfo);
			long timeout = getTimeout(tokenKey);
			stringRedisTemplate.opsForValue().set(tokenKey, json, timeout, TimeUnit.SECONDS);
		} catch (JsonProcessingException e) {
			log.error("保存转换对象出现异常", e);
		}
	}

	@Override
	public void deleteUserInfo(String key) {
		stringRedisTemplate.delete(getTokenKey(key));
	}

	@Override
	public long getTimeout(String key) {
		Long expire = stringRedisTemplate.getExpire(getTokenKey(key));
		return expire == null ? -2 : expire;
	}

	@Override
	public void updateTimeout(String key, long timeout) {
		stringRedisTemplate.expire(getTokenKey(key), timeout, TimeUnit.SECONDS);
	}

	private String getTokenKey(String key) {
		if (key.startsWith(tokenPrefix)) {
			return key;
		}
		return tokenPrefix + key;
	}
}
