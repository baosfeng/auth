package xyz.bsfeng.auth;

import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.dao.RedisTokenDaoImpl;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.TokenDaoDefaultImpl;
import xyz.bsfeng.auth.utils.SpringUtils;
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
