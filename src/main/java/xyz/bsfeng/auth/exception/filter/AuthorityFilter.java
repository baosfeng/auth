package xyz.bsfeng.auth.exception.filter;

import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.anno.PreAuthorize;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.utils.CollectionUtils;
import xyz.bsfeng.auth.utils.StringUtils;
import xyz.bsfeng.auth.utils.TokenUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @date 2021/12/7 22:35
 * @since 1.0.0
 */
public class AuthorityFilter implements AuthFilter {

	@Override
	public void doChain(@Nonnull HttpServletRequest request,
	                    @Nonnull HttpServletResponse response,
	                    @Nonnull AuthConfig authConfig,
	                    @Nonnull Method method) {
		PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
		if (annotation == null) return;
		String[] auths = annotation.hasAuth();
		Set<String> authSet = Arrays.stream(auths).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
		if (CollectionUtils.isNotEmpty(authSet)) {
			String[] userAuths = TokenUtils.getUser().getAuths();
			if (userAuths == null) {
				throw new AuthException(AuthConstant.ACCOUNT_NO_AUTH_CODE, AuthConstant.ACCOUNT_NO_AUTH_MESSAGE);
			}
			userAuths = Arrays.stream(userAuths).filter(Objects::nonNull).toArray(String[]::new);
			if (userAuths.length == 0) {
				throw new AuthException(AuthConstant.ACCOUNT_NO_AUTH_CODE, AuthConstant.ACCOUNT_NO_AUTH_MESSAGE);
			}
			// 验证是否为超管
			boolean isAdmin = Arrays.stream(userAuths)
					.anyMatch(itm -> itm.equalsIgnoreCase(TokenManager.getConfig().getAdminRole()));
			if (isAdmin) return;
			// 只要拥有的权限中存在某一个指定的权限即可
			Set<String> authsSet = Arrays.stream(userAuths).collect(Collectors.toSet());
			for (String role : authSet) {
				if (authsSet.contains(role)) {
					return;
				}
			}
			throw new AuthException(AuthConstant.ACCOUNT_NO_AUTH_CODE, AuthConstant.ACCOUNT_NO_AUTH_MESSAGE);
		}
	}
}
