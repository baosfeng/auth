package xyz.bsfeng.auth.filter;

import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.dao.RedisTokenDaoImpl;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.pojo.AuthUserBuilder;
import xyz.bsfeng.auth.utils.AuthBooleanUtils;
import xyz.bsfeng.auth.utils.AuthSpringUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;

import static xyz.bsfeng.auth.constant.AuthConstant.*;

/**
 * @author Administrator
 * @date 2021/12/7 22:19
 * @since 1.0.0
 */
public class IdentifyFilter implements AuthFilter {


	@Override
	public void doChain(@Nonnull HttpServletRequest request,
	                    @Nonnull HttpServletResponse response,
	                    @Nonnull AuthConfig authConfig,
	                    @Nonnull Method method) {
		if (AuthBooleanUtils.isTrue((Boolean) request.getAttribute(IS_WHITE_URL))) {
			request.setAttribute(USER_ID, WHITE_ID);
			request.setAttribute(USER_INFO, new AuthUserBuilder().id(WHITE_ID).build());
			return;
		}
		String token = (String) request.getAttribute(TOKEN_NAME);
		boolean anyMatch = Arrays.asList(authConfig.getWhiteTokenList().split(",")).contains(token);
		if (anyMatch) {
			request.setAttribute(USER_ID, WHITE_TOKEN_ID);
			request.setAttribute(USER_INFO, new AuthUserBuilder().id(WHITE_TOKEN_ID).build());
			return;
		}
		TokenDao tokenDao = AuthSpringUtils.getClass(RedisTokenDaoImpl.class);
		UserInfo userInfo = (UserInfo) tokenDao.getUserInfo(token);
		// 校验是否登录
		if (userInfo == null) {
			throw new AuthException(AuthConstant.NOT_LOGIN_CODE, AuthConstant.NOT_LOGIN_MESSAGE);
		}
		request.setAttribute(USER_INFO, userInfo);
		request.setAttribute(USER_ID, userInfo.getId());
	}
}
