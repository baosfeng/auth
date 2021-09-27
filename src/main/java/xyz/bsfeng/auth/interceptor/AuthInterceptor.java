package xyz.bsfeng.auth.interceptor;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.anno.IgnoreLogin;
import xyz.bsfeng.auth.anno.MustLogin;
import xyz.bsfeng.auth.anno.PreAuthorize;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.utils.SpringMVCUtil;
import xyz.bsfeng.auth.utils.StringUtils;
import xyz.bsfeng.auth.utils.TokenUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class AuthInterceptor implements HandlerInterceptor {

	private final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
	private Boolean autoRenew;
	private TokenDao tokenDao;
	private Boolean enable;
	@Autowired
	private ThreadPoolExecutor poolExecutor;
	private Boolean isLog;
	private static List<String> whiteUrlList;
	private static List<String> blackUrlList;
	private static final AntPathMatcher MATCHER = new AntPathMatcher();


	public void init() {
		AuthConfig authConfig = TokenManager.getConfig();
		tokenDao = TokenManager.getTokenDao();
		autoRenew = authConfig.getAutoRenew();
		enable = authConfig.getEnable();
		isLog = authConfig.getLog();
		String join = Joiner.on(",").join(Lists.newArrayList("/favicon.ico", "/error"));
		if (StringUtils.isNotEmpty(authConfig.getWhiteUrlList())) {
			String s = authConfig.getWhiteUrlList() + "," + join;
			authConfig.setWhiteUrlList(s);
		} else {
			authConfig.setWhiteUrlList(join);
		}
		whiteUrlList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(authConfig.getWhiteUrlList());
		blackUrlList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(authConfig.getBlackUrlList());
		Lists.newArrayList(whiteUrlList);
	}

	@Override
	@SuppressWarnings("all")
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (isLog) log.debug("正在访问{}", request.getRequestURI());
		if (!enable) {
			return true;
		}
		boolean isWhiteUrl = checkWhiteUrl(handler);
		request.setAttribute("isWhiteUrl", isWhiteUrl);
		if (isWhiteUrl) return true;
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			PreAuthorize annotation = handlerMethod.getMethodAnnotation(PreAuthorize.class);
			if (annotation != null) {
				checkRoles(annotation);
				checkAuths(annotation);
			}
			IgnoreLogin ignoreLogin = handlerMethod.getMethodAnnotation(IgnoreLogin.class);
			if (ignoreLogin != null) {
				request.setAttribute("isWhiteUrl", true);
				return true;
			}
		}

		UserInfo userInfo;
		String token = TokenUtils.getToken();
		request.setAttribute("token", token);
		try {
			userInfo = TokenUtils.getUser();
		} catch (AuthException e) {
			throw e;
		}
		if (userInfo.getId() <= 0) return true;
		if (autoRenew) {
			request.setAttribute("id", userInfo.getId());
			request.setAttribute("userInfo", userInfo);
			poolExecutor.submit(() -> {
				tokenDao.updateUserInfo(token, userInfo);
			});
		}
		return true;
	}

	private void checkAuths(PreAuthorize annotation) {
		String[] value = annotation.hasAuth();
		if (value.length == 0) {
			return;
		}
		String[] auths = TokenUtils.getUser().getRoles();
		if (auths == null) {
			throw new AuthException(AuthConstant.ACCOUNT_NO_AUTH_CODE, AuthConstant.ACCOUNT_NO_AUTH_MESSAGE);
		}
		// 验证是否为超管
		boolean isAdmin = Arrays.stream(auths)
				.anyMatch(itm -> itm.equalsIgnoreCase(TokenManager.getConfig().getAdminRole()));
		if (isAdmin) {
			return;
		}
		// 只要拥有的权限中存在某一个指定的权限即可
		Set<String> checkSet = Arrays.stream(value).collect(Collectors.toSet());
		Set<String> authsSet = Arrays.stream(auths).collect(Collectors.toSet());
		for (String role : checkSet) {
			if (authsSet.contains(role)) {
				return;
			}
		}
		throw new AuthException(AuthConstant.ACCOUNT_NO_AUTH_CODE, AuthConstant.ACCOUNT_NO_AUTH_MESSAGE);
	}

	private void checkRoles(PreAuthorize annotation) {
		String[] value = annotation.hasRole();
		if (value.length == 0) {
			return;
		}
		String[] roles = TokenUtils.getUser().getRoles();
		if (roles == null) {
			throw new AuthException(AuthConstant.ACCOUNT_NO_ANY_ROLE_CODE, AuthConstant.ACCOUNT_NO_ANY_ROLE_MESSAGE);
		}
		// 验证是否为超管
		boolean isAdmin = Arrays.stream(roles)
				.anyMatch(itm -> itm.equalsIgnoreCase(TokenManager.getConfig().getAdminRole()));
		if (isAdmin) {
			return;
		}
		// 只要拥有的权限中存在某一个指定的权限即可
		Set<String> checkSet = Arrays.stream(value).collect(Collectors.toSet());
		Set<String> rolesSet = Arrays.stream(roles).collect(Collectors.toSet());
		for (String role : checkSet) {
			if (rolesSet.contains(role)) {
				return;
			}
		}
		throw new AuthException(AuthConstant.ACCOUNT_NO_ROLE_CODE, AuthConstant.ACCOUNT_NO_ROLE_MESSAGE);
	}

	public static boolean checkWhiteUrl(Object handler) {
		HttpServletRequest request = SpringMVCUtil.getRequest();
		String uri = request.getRequestURI();
		for (String url : blackUrlList) {
			boolean match = MATCHER.match(url, uri);
			if (match) return false;
		}
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			MustLogin mustLogin = handlerMethod.getMethodAnnotation(MustLogin.class);
			if (mustLogin != null) return false;
		}
		for (String white : whiteUrlList) {
			boolean match = MATCHER.match(white, uri);
			if (match) return true;
		}
		return false;
	}
}
