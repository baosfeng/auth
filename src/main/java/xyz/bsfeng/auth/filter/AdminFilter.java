package xyz.bsfeng.auth.filter;

import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.utils.AuthBooleanUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import static xyz.bsfeng.auth.constant.AuthConstant.*;

/**
 * @author bsfeng
 * @date 2021/12/9 9:02
 */
public class AdminFilter implements AuthFilter {
	@Override
	public void doChain(@Nonnull HttpServletRequest request,
	                    @Nonnull HttpServletResponse response,
	                    @Nonnull AuthConfig authConfig,
	                    @Nullable Method method) {
		Boolean isWhiteToken = (Boolean) request.getAttribute(IS_WHITE_TOKEN);
		if (AuthBooleanUtils.isTrue(isWhiteToken) && authConfig.getWhiteTokenAsAdmin()) {
			request.setAttribute(IS_ADMIN, true);
			return;
		}
		UserInfo userInfo = (UserInfo) request.getAttribute(USER_INFO);
		String[] auths = userInfo.getAuths();
		if (auths != null && auths.length != 0) {
			// 验证是否为超管
			boolean isAdmin = Arrays.stream(auths).filter(Objects::nonNull)
					.anyMatch(itm -> itm.equalsIgnoreCase(TokenManager.getConfig().getAdminRole()));
			request.setAttribute(IS_ADMIN, isAdmin);
			return;
		}
		String[] roles = userInfo.getRoles();
		if (roles != null && roles.length != 0) {
			// 验证是否为超管
			boolean isAdmin = Arrays.stream(roles).filter(Objects::nonNull)
					.anyMatch(itm -> itm.equalsIgnoreCase(TokenManager.getConfig().getAdminRole()));
			request.setAttribute(IS_ADMIN, isAdmin);
			return;
		}
		request.setAttribute(IS_ADMIN, false);
	}
}
