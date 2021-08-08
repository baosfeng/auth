package com.yizhu.auth.dao;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author bsfeng
 * @date 2021/8/7-10:01
 * @since 1.0
 */
public class RedisTokenDaoImpl implements TokenDao {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public UserInfo getUserInfo(String key) {
		return null;
	}

	@Override
	public void setUserInfo(String key, UserInfo userInfo) {

	}

	@Override
	public void setUserInfo(String key, UserInfo userInfo, long timeout) {

	}

	@Override
	public void updateUserInfo(String key, UserInfo userInfo) {

	}

	@Override
	public void deleteUserInfo(String key) {

	}

	@Override
	public long getTimeout(String key) {
		return 0;
	}

	@Override
	public void updateTimeout(String key, long timeout) {

	}
}
