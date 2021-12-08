package xyz.bsfeng.auth.filter;

import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.anno.PreAuthorize;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.utils.AuthCollectionUtils;
import xyz.bsfeng.auth.utils.AuthStringUtils;
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
 * @date 2021/12/7 22:31
 * @since 1.0.0
 */
public class RoleFiler implements AuthFilter {

	@Override
	public void doChain(@Nonnull HttpServletRequest request,
	                    @Nonnull HttpServletResponse response,
	                    @Nonnull AuthConfig authConfig,
	                    @Nonnull Method method) {
		PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
		if (annotation == null) return;
		String[] roles = annotation.hasRole();
		Set<String> roleSet = Arrays.stream(roles).filter(AuthStringUtils::isNotEmpty).collect(Collectors.toSet());
		if (AuthCollectionUtils.isNotEmpty(roleSet)) {
			String[] userRoles = TokenUtils.getUser().getRoles();
			if (userRoles == null) {
				throw new AuthException(AuthConstant.ACCOUNT_NO_ROLE_CODE, AuthConstant.ACCOUNT_NO_ROLE_MESSAGE);
			}
			userRoles = Arrays.stream(userRoles).filter(Objects::nonNull).toArray(String[]::new);
			if (userRoles.length == 0) {
				throw new AuthException(AuthConstant.ACCOUNT_NO_ROLE_CODE, AuthConstant.ACCOUNT_NO_ROLE_MESSAGE);
			}
			// 验证是否为超管
			boolean isAdmin = Arrays.stream(userRoles)
					.anyMatch(itm -> itm.equalsIgnoreCase(TokenManager.getConfig().getAdminRole()));
			if (isAdmin) {
				return;
			}
			// 只要拥有的权限中存在某一个指定的权限即可
			Set<String> rolesSet = Arrays.stream(userRoles).collect(Collectors.toSet());
			for (String role : roleSet) {
				if (rolesSet.contains(role)) {
					return;
				}
			}
			throw new AuthException(AuthConstant.ACCOUNT_NO_ROLE_CODE, AuthConstant.ACCOUNT_NO_ROLE_MESSAGE);
		}
	}
}
