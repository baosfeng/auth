package xyz.bsfeng.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.dao.RedisTokenDaoImpl;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.TokenDaoDefaultImpl;
import xyz.bsfeng.auth.filter.*;
import xyz.bsfeng.auth.utils.AuthCollectionUtils;
import xyz.bsfeng.auth.utils.AuthSpringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
			new LockFilter(),
			new AuthRefreshFilter()
	);
	public static Cache<Long, Set<String>> idCache = CacheBuilder.newBuilder().build();

	public static void setConfig(AuthConfig config) {
		authConfig = config;
	}

	public static void setTokenDao(TokenDao dao) {
		tokenDao = dao;
	}

	public static AuthConfig getConfig() {
		if (authConfig == null) {
			synchronized (TokenManager.class) {
				authConfig = AuthSpringUtils.getClass(AuthConfig.class);
			}
		}
		return authConfig;
	}

	public static TokenDao getTokenDao() {
		if (tokenDao == null) {
			synchronized (TokenManager.class) {
				try {
					tokenDao = AuthSpringUtils.getClass(RedisTokenDaoImpl.class);
				} catch (NoSuchBeanDefinitionException e) {
					tokenDao = AuthSpringUtils.getClass(TokenDaoDefaultImpl.class);
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

	public static void removeAll() {
		idCache.invalidateAll();
	}

	public static void removeById(Long id) {
		idCache.invalidate(id);
	}

	public static Set<String> listById(Long id) {
		Set<String> tokenSet = idCache.getIfPresent(id);
		if (AuthCollectionUtils.isEmpty(tokenSet)) {
			tokenSet = new HashSet<>();
			idCache.put(id, tokenSet);
		}
		return tokenSet;
	}
}
