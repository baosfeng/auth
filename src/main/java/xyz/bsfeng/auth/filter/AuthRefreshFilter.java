package xyz.bsfeng.auth.filter;

import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.utils.AuthBooleanUtils;
import xyz.bsfeng.auth.utils.AuthSpringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

import static xyz.bsfeng.auth.constant.AuthConstant.*;

/**
 * @author bsfeng
 * @date 2021/12/8 8:59
 */
public class AuthRefreshFilter implements AuthFilter {

	@Override
	public void doChain(@Nonnull HttpServletRequest request,
	                    @Nonnull HttpServletResponse response,
	                    @Nonnull AuthConfig authConfig,
	                    @Nullable Method method) {
		if (AuthBooleanUtils.isFalse(authConfig.getAutoRenew())) return;
		String token = (String) request.getAttribute(TOKEN_NAME);
		Long userId = (Long) request.getAttribute(USER_ID);
		if (userId <= 0) return;
		// 执行异步更新
		ThreadPoolExecutor executor = AuthSpringUtils.getClass(ThreadPoolExecutor.class);
		TokenDao tokenDao = AuthSpringUtils.getClass(TokenDao.class);
		executor.submit(() -> tokenDao.updateUserInfo(token, (UserInfo) request.getAttribute(USER_INFO)));
	}
}
