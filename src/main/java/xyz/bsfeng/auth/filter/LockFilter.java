package xyz.bsfeng.auth.filter;

import org.springframework.core.annotation.Order;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.utils.AuthBooleanUtils;
import xyz.bsfeng.auth.utils.AuthTimeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

import static xyz.bsfeng.auth.constant.AuthConstant.*;

/**
 * @author bsfeng
 * @date 2021/12/8 11:00
 */
@Order
public class LockFilter implements AuthFilter {

	@Override
	public void doChain(@Nonnull HttpServletRequest request,
	                    @Nonnull HttpServletResponse response,
	                    @Nonnull AuthConfig authConfig,
	                    @Nullable Method method) {
		UserInfo userInfo = (UserInfo) request.getAttribute(USER_INFO);
		// 检查是否被封禁
		if (AuthBooleanUtils.isTrue(userInfo.getLock())) {
			long millis = System.currentTimeMillis();
			if (millis < userInfo.getLockTime()) {
				long lessTime = userInfo.getLockTime() - millis;
				throw new AuthException(ACCOUNT_LOCK_CODE, ACCOUNT_LOCK_MESSAGE + AuthTimeUtils.mill2Time(lessTime));
			}
			// 如果已经过了锁定时间,那么解封用户
			userInfo.setLock(false);
			request.setAttribute(USER_INFO, userInfo);
		}
	}
}
