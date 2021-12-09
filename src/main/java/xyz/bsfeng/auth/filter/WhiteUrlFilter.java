package xyz.bsfeng.auth.filter;

import org.springframework.util.AntPathMatcher;
import xyz.bsfeng.auth.anno.IgnoreLogin;
import xyz.bsfeng.auth.anno.MustLogin;
import xyz.bsfeng.auth.config.AuthConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

import static xyz.bsfeng.auth.constant.AuthConstant.IS_WHITE_URL;

/**
 * @author Administrator
 * @date 2021/12/7 22:09
 * @since 1.0.0
 */
public class WhiteUrlFilter implements AuthFilter {

	private static final AntPathMatcher MATCHER = new AntPathMatcher();

	@Override
	public void doChain(@Nonnull HttpServletRequest request,
	                    @Nonnull HttpServletResponse response,
	                    @Nonnull AuthConfig authConfig,
	                    @Nullable Method method) {
		if (method != null) {
			if (method.isAnnotationPresent(MustLogin.class)) {
				request.setAttribute(IS_WHITE_URL, false);
				return;
			}
			if (method.isAnnotationPresent(IgnoreLogin.class)) {
				request.setAttribute(IS_WHITE_URL, true);
				return;
			}
		}
		String[] whiteUrlList = authConfig.getWhiteUrlList().split(",");
		String[] blackUrlList = authConfig.getBlackUrlList().split(",");
		String uri = request.getRequestURI();
		for (String url : blackUrlList) {
			boolean match = MATCHER.match(url, uri);
			if (match) {
				request.setAttribute(IS_WHITE_URL, false);
				return;
			}
		}
		for (String white : whiteUrlList) {
			boolean match = MATCHER.match(white, uri);
			if (match) {
				request.setAttribute(IS_WHITE_URL, true);
				return;
			}
		}
		request.setAttribute(IS_WHITE_URL, false);
	}
}
