package xyz.bsfeng.auth.interceptor;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import xyz.bsfeng.auth.TokenManager;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.dao.TokenDao;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.utils.StringUtils;
import xyz.bsfeng.auth.utils.TokenUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static xyz.bsfeng.auth.utils.MessageUtils.sendErrorMessage;

/**
 * @author bsfeng
 * @date 2021/9/28 11:45
 */
public class AuthFilter implements Filter {

	private final Logger log = LoggerFactory.getLogger(AuthFilter.class);
	@Autowired
	private AuthConfig authConfig;
	private static List<String> whiteUrlList;
	private static List<String> blackUrlList;
	private static TokenDao tokenDao;
	private static final AntPathMatcher MATCHER = new AntPathMatcher();
	@Autowired
	private ThreadPoolExecutor poolExecutor;

	private String token;
	@Value("${error.path:/error}")
	private String errorPath;

	public void init() {
		tokenDao = TokenManager.getTokenDao();
		String join = Joiner.on(",").join(Lists.newArrayList("/favicon.ico", errorPath));
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
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (!authConfig.getEnable()) {
			chain.doFilter(request, response);
			return;
		}
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		HttpServletResponse servletResponse = (HttpServletResponse) response;
		if (authConfig.getLog()) log.debug("正在访问{}", servletRequest.getRequestURI());
		boolean isWhiteUrl = checkWhiteUrl(servletRequest);

		if (isWhiteUrl) {
			try {
				chain.doFilter(request, response);
				return;
			} catch (AuthException e) {
				sendErrorMessage(servletResponse, e);
				return;
			}
		}
		UserInfo userInfo;
		try {
			token = TokenUtils.getToken();
			userInfo = TokenUtils.getUser();
			request.setAttribute("userInfo", userInfo);
			request.setAttribute("userId", userInfo.getId());
			if (authConfig.getAutoRenew()) {
				poolExecutor.submit(() -> tokenDao.updateUserInfo(token, userInfo));
			}
		} catch (AuthException e) {
			sendErrorMessage(servletResponse, e);
			return;
		}
		chain.doFilter(request, response);
	}

	public static boolean checkWhiteUrl(HttpServletRequest request) {
		String uri = request.getRequestURI();
		for (String url : blackUrlList) {
			boolean match = MATCHER.match(url, uri);
			if (match) {
				request.setAttribute("isWhiteUrl", false);
				return false;
			}
		}
		for (String white : whiteUrlList) {
			boolean match = MATCHER.match(white, uri);
			if (match) {
				request.setAttribute("isWhiteUrl", true);
				return true;
			}
		}
		request.setAttribute("isWhiteUrl", false);
		return false;
	}
}
