package xyz.bsfeng.auth.filter;

import org.springframework.core.annotation.Order;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static xyz.bsfeng.auth.constant.AuthConstant.*;

/**
 * @author bsfeng
 * @date 2021/12/8 8:59
 */
@Order
public class AuthRefreshFilter implements AuthFilter {

	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
			Runtime.getRuntime().availableProcessors(),
			0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>(),
			new ThreadFactory() {
				private final AtomicInteger atomicInteger = new AtomicInteger();
				@Override
				public Thread newThread(@Nonnull Runnable r) {
					String threadName = "auth_" + atomicInteger.getAndIncrement() + "";
					return new Thread(null, r, threadName, 0);
				}});

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
		TokenDao tokenDao = AuthSpringUtils.getClass(TokenDao.class);
		executor.submit(() -> tokenDao.updateUserInfo(token, (UserInfo) request.getAttribute(USER_INFO)));
	}
}
