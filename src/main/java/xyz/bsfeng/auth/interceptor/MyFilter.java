package xyz.bsfeng.auth.interceptor;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.filter.AuthFilter;
import xyz.bsfeng.auth.utils.BooleanUtils;
import xyz.bsfeng.auth.utils.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author bsfeng
 * @date 2021/9/28 11:45
 */
public class MyFilter implements Filter {

	private final Logger log = LoggerFactory.getLogger(MyFilter.class);
	@Autowired
	private AuthConfig authConfig;
	private static List<String> whiteUrlList;
	private static List<String> blackUrlList;
	private static TokenDao tokenDao;
	private static final AntPathMatcher MATCHER = new AntPathMatcher();
	@Autowired
	private ThreadPoolExecutor poolExecutor;

	private String token;
	@Value("${error.path:/error}")
	private String errorPath;
	final Cache<String, Method> cache = TokenManager.cache;
	Cache<String, Method> urlMethodCache = CacheBuilder.newBuilder().build();
	private final ArrayList<AuthFilter> authFilters = TokenManager.getAuthFilters();

	public void init() {
		tokenDao = TokenManager.getTokenDao();
		String join = Joiner.on(",").join(Lists.newArrayList("/favicon.ico", errorPath));
		if (StringUtils.isNotEmpty(authConfig.getWhiteUrlList())) {
			String s = authConfig.getWhiteUrlList() + "," + join;
			authConfig.setWhiteUrlList(s);
		} else {
			authConfig.setWhiteUrlList(join);
		}
		whiteUrlList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(authConfig.getWhiteUrlList());
		blackUrlList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(authConfig.getBlackUrlList());
		cache.asMap().forEach((k, v) -> {
			if (BooleanUtils.isFalse(k.contains("*"))) urlMethodCache.put(k, v);
			cache.invalidate(k);
		});
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (!authConfig.getEnable()) {
			chain.doFilter(request, response);
			return;
		}
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		HttpServletResponse servletResponse = (HttpServletResponse) response;
		String uri = servletRequest.getRequestURI();
		if (authConfig.getLog()) log.debug("正在访问{}", uri);
		Method me = urlMethodCache.getIfPresent(uri);
		if (me == null) {
			for (Map.Entry<String, Method> entry : cache.asMap().entrySet()) {
				String key = entry.getKey();
				Method method = entry.getValue();
				if (MATCHER.match(key,uri)) {
					me = method;
					break;
				}
			}
		}
		if (me == null) {
			chain.doFilter(request, response);
			return;
		}
		for (AuthFilter authFilter : authFilters) {
			authFilter.doChain(servletRequest, servletResponse, authConfig, me);
		}
		chain.doFilter(request, response);
	}

}
