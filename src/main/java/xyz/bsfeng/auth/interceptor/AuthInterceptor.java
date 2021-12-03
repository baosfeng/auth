package xyz.bsfeng.auth.interceptor;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.anno.PreAuthorize;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.event.UserBeginRequestEvent;
import xyz.bsfeng.auth.event.UserEndRequestFailedEvent;
import xyz.bsfeng.auth.event.UserEndRequestSuccessEvent;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.utils.*;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthInterceptor implements HandlerInterceptor {
	AuthConfig authConfig = TokenManager.getConfig();


	@Override
	public boolean preHandle(@Nonnull HttpServletRequest request,
	                         @Nonnull HttpServletResponse response,
	                         @Nonnull Object handler) throws IOException {
		if (BooleanUtils.isFalse(authConfig.getEnable())) return true;
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			SpringUtils.publishEvent(new UserBeginRequestEvent(request, handlerMethod));
			PreAuthorize annotation = handlerMethod.getMethodAnnotation(PreAuthorize.class);
			if (annotation != null) {
				try {
					String[] role = annotation.hasRole();
					Set<String> roleSet = Arrays.stream(role).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
					if (CollectionUtils.isNotEmpty(roleSet)) {
						checkRoles(roleSet);
					}
					String[] auths = annotation.hasAuth();
					Set<String> authSet = Arrays.stream(auths).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
					if (CollectionUtils.isNotEmpty(authSet)) {
						checkAuths(authSet);
					}
				} catch (AuthException e) {
					MessageUtils.sendErrorMessage(response, e);
					throw e;
				}
			}
		}
		return true;
	}

	@Override
	public void afterCompletion(@Nonnull HttpServletRequest request,
	                            @Nonnull HttpServletResponse response,
	                            @Nonnull Object handler,
	                            Exception ex) throws Exception {
		if (handler instanceof HandlerMethod) {
			if (ex == null) {
				SpringUtils.publishEvent(new UserEndRequestSuccessEvent(request, response, (HandlerMethod) handler));
			} else {
				SpringUtils.publishEvent(new UserEndRequestFailedEvent(request, response, (HandlerMethod) handler, ex));
			}
		}
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}

	private void checkAuths(Set<String> authSet) {
		String[] auths = TokenUtils.getUser().getAuths();
		if (auths == null) {
			throw new AuthException(AuthConstant.ACCOUNT_NO_AUTH_CODE, AuthConstant.ACCOUNT_NO_AUTH_MESSAGE);
		}
		auths = Arrays.stream(auths).filter(Objects::nonNull).toArray(String[]::new);
		if (auths.length == 0) {
			throw new AuthException(AuthConstant.ACCOUNT_NO_AUTH_CODE, AuthConstant.ACCOUNT_NO_AUTH_MESSAGE);
		}
		// 验证是否为超管
		boolean isAdmin = Arrays.stream(auths)
				.anyMatch(itm -> itm.equalsIgnoreCase(TokenManager.getConfig().getAdminRole()));
		if (isAdmin) return;
		// 只要拥有的权限中存在某一个指定的权限即可
		Set<String> authsSet = Arrays.stream(auths).collect(Collectors.toSet());
		for (String role : authSet) {
			if (authsSet.contains(role)) {
				return;
			}
		}
		throw new AuthException(AuthConstant.ACCOUNT_NO_AUTH_CODE, AuthConstant.ACCOUNT_NO_AUTH_MESSAGE);
	}

	private void checkRoles(Set<String> roleSet) {
		String[] roles = TokenUtils.getUser().getRoles();
		if (roles == null) {
			throw new AuthException(AuthConstant.ACCOUNT_NO_ROLE_CODE, AuthConstant.ACCOUNT_NO_ROLE_MESSAGE);
		}
		roles = Arrays.stream(roles).filter(Objects::nonNull).toArray(String[]::new);
		if (roles.length == 0) {
			throw new AuthException(AuthConstant.ACCOUNT_NO_ROLE_CODE, AuthConstant.ACCOUNT_NO_ROLE_MESSAGE);
		}
		// 验证是否为超管
		boolean isAdmin = Arrays.stream(roles)
				.anyMatch(itm -> itm.equalsIgnoreCase(TokenManager.getConfig().getAdminRole()));
		if (isAdmin) {
			return;
		}
		// 只要拥有的权限中存在某一个指定的权限即可
		Set<String> rolesSet = Arrays.stream(roles).collect(Collectors.toSet());
		for (String role : roleSet) {
			if (rolesSet.contains(role)) {
				return;
			}
		}
		throw new AuthException(AuthConstant.ACCOUNT_NO_ROLE_CODE, AuthConstant.ACCOUNT_NO_ROLE_MESSAGE);
	}

}
