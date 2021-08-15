package xyz.bsfeng.auth.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.anno.HasRole;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.utils.TokenUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthInterceptor implements HandlerInterceptor {

	private final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
	private List<String> whiteUrlList;
	private List<String> whiteTokenList;
	private Boolean autoRenew;
	private TokenDao tokenDao;
	private Boolean enable;


	public void init() {
		AuthConfig authConfig = TokenManager.getConfig();
		tokenDao = TokenManager.getTokenDao();

		whiteUrlList = Arrays.stream(authConfig.getWhiteUrlList().split(",")).collect(Collectors.toList());
		whiteUrlList.add("/favicon.ico");
		whiteUrlList.add("/error");
		whiteTokenList = Arrays.asList(authConfig.getWhiteTokenList().split(","));
		autoRenew = authConfig.getAutoRenew();
		enable = authConfig.getEnable();
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		log.info("正在访问{}", request.getRequestURI());
		if (!enable) {
			return true;
		}
		if (doWhiteUrl(request)) return true;
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		HasRole annotation = handlerMethod.getMethodAnnotation(HasRole.class);
		if (annotation != null) {
			checkPermission(annotation);
		}

		String token = TokenUtils.getToken();
		if (whiteTokenList.stream().anyMatch(itm -> itm.equalsIgnoreCase(token))) {
			return true;
		}

		UserInfo userInfo = (UserInfo) tokenDao.getUserInfo(token);
		if (userInfo == null) {
			throw new AuthException(AuthConstant.NOT_LOGIN_CODE, AuthConstant.NOT_LOGIN_MESSAGE);
		}
		if (autoRenew) {
			tokenDao.updateUserInfo(token, userInfo);
		}
		return true;
	}

	private void checkPermission(HasRole annotation) {
		String[] value = annotation.value();
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

	private boolean doWhiteUrl(HttpServletRequest request) {
		String uri = request.getRequestURI();
		for (String white : whiteUrlList) {
			if (white.endsWith("*")) {
				white = white.replace("*", "");
				if (uri.startsWith(white)) {
					return true;
				}
			}
			if (white.equalsIgnoreCase(uri)) {
				return true;
			}
		}
		return false;
	}


}
