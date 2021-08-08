package com.yizhu.auth;

import com.yizhu.auth.config.AuthConfig;
import com.yizhu.auth.dao.RedisTokenDaoImpl;
import com.yizhu.auth.dao.TokenDao;
import com.yizhu.auth.dao.TokenDaoDefaultImpl;
import com.yizhu.auth.utils.SpringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class TokenManager {

	private static AuthConfig authConfig;

	private static TokenDao tokenDao;

	public static void setConfig(AuthConfig config) {
		authConfig = config;
	}

	public static void setTokenDao(TokenDao dao) {
		tokenDao = dao;
	}

	public static AuthConfig getConfig() {
		if (authConfig == null) {
			synchronized (TokenManager.class) {
				authConfig = SpringUtils.getClass(AuthConfig.class);
			}
		}
		return authConfig;
	}

	public static TokenDao getTokenDao() {
		if (tokenDao == null) {
			synchronized (TokenManager.class) {
				try {
					tokenDao = SpringUtils.getClass(RedisTokenDaoImpl.class);
				} catch (NoSuchBeanDefinitionException e) {
					tokenDao = SpringUtils.getClass(TokenDaoDefaultImpl.class);
				}
			}
		}
		return tokenDao;
	}
}
