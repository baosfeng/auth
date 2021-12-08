package xyz.bsfeng.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.dao.RedisTokenDaoImpl;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.TokenDaoDefaultImpl;
import xyz.bsfeng.auth.exception.filter.*;
import xyz.bsfeng.auth.utils.SpringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TokenManager {

	private static AuthConfig authConfig;

	private static TokenDao tokenDao;

	public static Cache<String, Method> cache = CacheBuilder.newBuilder().build();
	public static Cache<String, Method> urlMethodCache = CacheBuilder.newBuilder().build();
	private static final ArrayList<AuthFilter> authFilters = Lists.newArrayList(
			new WhiteUrlFilter(),
			new TokenFilter(),
			new IdentifyFilter(),
			new RoleFiler(),
			new AuthorityFilter(),
			new AuthRefreshFilter()
	);

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

	public static ArrayList<AuthFilter> getAuthFilters() {
		return authFilters;
	}

	public static List<AuthFilter> addFilter(AuthFilter filter) {
		authFilters.add(filter);
		return authFilters;
	}
}
