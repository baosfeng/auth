package xyz.bsfeng.auth.filter;

import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.utils.AuthStringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author bsfeng
 * @date 2021/9/28 11:45
 */
public class MyFilter implements Filter {

	private final Logger log = LoggerFactory.getLogger(MyFilter.class);
	@Autowired
	private AuthConfig authConfig;
	private static final AntPathMatcher MATCHER = new AntPathMatcher();
	@Value("${error.path:/error}")
	private String errorPath;
	private final Cache<String, Method> cache = TokenManager.cache;
	private final Cache<String, Method> urlMethodCache = TokenManager.urlMethodCache;
	private final ArrayList<AuthFilter> authFilters = TokenManager.getAuthFilters();

	public void init() {
		String join = Joiner.on(",").join(Lists.newArrayList("/favicon.ico", errorPath));
		if (AuthStringUtils.isNotEmpty(authConfig.getWhiteUrlList())) {
			String s = authConfig.getWhiteUrlList() + "," + join;
			authConfig.setWhiteUrlList(s);
		} else {
			authConfig.setWhiteUrlList(join);
		}
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
