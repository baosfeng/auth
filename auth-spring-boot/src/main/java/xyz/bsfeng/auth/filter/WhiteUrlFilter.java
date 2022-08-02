package xyz.bsfeng.auth.filter;

import xyz.bsfeng.auth.anno.IgnoreLogin;
import xyz.bsfeng.auth.anno.MustLogin;
import xyz.bsfeng.auth.constant.AuthConstant;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.AntPathMatcher;
import xyz.bsfeng.auth.config.AuthConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Administrator
 * @date 2021/12/7 22:09
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WhiteUrlFilter implements AuthFilter {

	private static final AntPathMatcher MATCHER = new AntPathMatcher();

	@Override
	public void doChain(@Nonnull HttpServletRequest request,
	                    @Nonnull AuthConfig authConfig,
	                    @Nullable Method method) {
		if (method != null) {
			if (method.isAnnotationPresent(MustLogin.class)) {
				request.setAttribute(AuthConstant.IS_WHITE_URL, false);
				return;
			}
			if (method.isAnnotationPresent(IgnoreLogin.class)) {
				request.setAttribute(AuthConstant.IS_WHITE_URL, true);
				return;
			}
		}
		List<String> whiteUrlList = authConfig.getWhiteUrlList();
		List<String> blackUrlList = authConfig.getBlackUrlList();
		String uri = request.getRequestURI();
		for (String url : blackUrlList) {
			boolean match = MATCHER.match(url, uri);
			if (match) {
				request.setAttribute(AuthConstant.IS_WHITE_URL, false);
				return;
			}
		}
		for (String white : whiteUrlList) {
			boolean match = MATCHER.match(white, uri);
			if (match) {
				request.setAttribute(AuthConstant.IS_WHITE_URL, true);
				return;
			}
		}
		request.setAttribute(AuthConstant.IS_WHITE_URL, false);
	}
}
