package xyz.bsfeng.auth.exception.filter;

import xyz.bsfeng.auth.config.AuthConfig;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author Administrator
 * @date 2021/12/7 21:57
 * @since 1.0.0
 */
public interface AuthFilter {

	/**
	 * 拦截器链
	 * @param request
	 * @param response
	 * @param authConfig
	 * @param method
	 */
	void doChain(@Nonnull HttpServletRequest request,
	             @Nonnull HttpServletResponse response,
	             @Nonnull AuthConfig authConfig,
	             @Nonnull Method method);
}
