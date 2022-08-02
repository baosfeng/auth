package xyz.bsfeng.auth.filter;

import xyz.bsfeng.auth.exception.AuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.utils.AuthMessageUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class AuthFilterChain {

	private final Logger log = LoggerFactory.getLogger(AuthFilterChain.class);
	private final Map<String, Method> cache = TokenManager.cache;
	private final Map<String, Method> urlMethodCache = TokenManager.urlMethodCache;
	private static final AntPathMatcher MATCHER = new AntPathMatcher();
	private static final List<AuthFilter> authFilters = TokenManager.getAuthFilters();

	private int currentIndex;
	private final AuthConfig authConfig;

	public AuthFilterChain(AuthConfig authConfig) {
		this.authConfig = authConfig;
		currentIndex = 0;
	}

	public void doFilter(@NonNull HttpServletRequest request,
	                     @NonNull HttpServletResponse response) throws IOException {
		if (!authConfig.getEnable()) return;
		if (currentIndex < authFilters.size()) {
			AuthFilter authFilter = authFilters.get(currentIndex++);
			Method method = getMethod(request);
			if (method == null) return;
			try {
				authFilter.doChain(request, response, authConfig, method);
			} catch (AuthException e) {
				AuthMessageUtils.sendErrorMessage(response, e);
			}
		}
	}

	@Nullable
	private Method getMethod(@NonNull HttpServletRequest request) {
		String uri = request.getRequestURI();
		if (authConfig.getLog()) log.debug("正在访问{}", uri);
		Method me = cache.get(uri);
		if (me == null) {
			for (Map.Entry<String, Method> entry : urlMethodCache.entrySet()) {
				String key = entry.getKey();
				Method method = entry.getValue();
				if (MATCHER.match(key, uri)) {
					cache.put(uri, method);
					return method;
				}
			}
		}
		return null;
	}
}
