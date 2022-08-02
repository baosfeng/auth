package xyz.bsfeng.auth;

import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.TokenDaoDefaultImpl;
import auth.filter.*;
import xyz.bsfeng.auth.utils.AuthSpringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.annotation.Order;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.dao.RedisTokenDaoImpl;
import xyz.bsfeng.auth.filter.*;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenManager {

	private static AuthConfig authConfig;
	private static TokenDao tokenDao;
	public static Map<String, Method> cache = Maps.newConcurrentMap();
	public static Map<String, Method> urlMethodCache = Maps.newConcurrentMap();
	private static List<AuthFilter> authFilters = Lists.newArrayList(
			new WhiteUrlFilter(),
			new TokenFilter(),
			new IdentifyFilter(),
			new AdminFilter(),
			new RoleFiler(),
			new AuthorityFilter(),
			new LockFilter(),
			new AuthRefreshFilter()
	);

	static {
		sortFilter();
	}

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

	public static List<AuthFilter> getAuthFilters() {
		return authFilters;
	}

	public static List<AuthFilter> addFilter(AuthFilter filter) {
		authFilters.add(filter);
		sortFilter();
		return authFilters;
	}

	private synchronized static void sortFilter() {
		authFilters = authFilters.stream()
				.sorted(Comparator.comparingInt(TokenManager::getValue))
				.collect(Collectors.toList());
	}

	private static int getValue(AuthFilter authFilter) {
		Order order = authFilter.getClass().getAnnotation(Order.class);
		return order == null ? Integer.MAX_VALUE : order.value();
	}

}
