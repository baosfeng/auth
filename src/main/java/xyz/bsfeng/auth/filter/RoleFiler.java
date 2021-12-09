package xyz.bsfeng.auth.filter;

import xyz.bsfeng.auth.anno.PreAuthorize;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.utils.AuthBooleanUtils;
import xyz.bsfeng.auth.utils.AuthCollectionUtils;
import xyz.bsfeng.auth.utils.AuthStringUtils;
import xyz.bsfeng.auth.utils.TokenUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static xyz.bsfeng.auth.constant.AuthConstant.IS_ADMIN;

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
	                    @Nullable Method method) {
		if (method == null) return;
		PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
		if (annotation == null) return;
		if (AuthBooleanUtils.isTrue((Boolean) request.getAttribute(IS_ADMIN))) return;
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
