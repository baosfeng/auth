package xyz.bsfeng.auth.filter;

import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.utils.AuthBooleanUtils;
import xyz.bsfeng.auth.utils.AuthStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import xyz.bsfeng.auth.config.AuthConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * @author Administrator
 * @date 2021/12/7 21:58
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TokenFilter implements AuthFilter {

	private final Logger log = LoggerFactory.getLogger(TokenFilter.class);
	private Boolean ignoreCamelCase;

	@Override
	public void doChain(@Nonnull HttpServletRequest request,
	                    @Nonnull AuthConfig authConfig,
	                    @Nullable Method method) {
		if (AuthBooleanUtils.isTrue((Boolean) request.getAttribute(AuthConstant.IS_WHITE_URL))) return;
		String token = "";
		String tokenFrom = "";
		String currentTokenName = "";
		ignoreCamelCase = authConfig.getIgnoreCamelCase();
		String[] tokenNames = authConfig.getTokenName().split(",");
		for (String from : authConfig.getReadFrom().split(",")) {
			for (String tokenName : authConfig.getTokenName().split(",")) {
				tokenName = tokenName.replaceAll("-", "").trim();
				if (AuthStringUtils.isNotEmpty(token)) break;
				switch (from) {
					case AuthConstant.READ_FROM_HEADER:
						currentTokenName = tokenName;
						token = doReadFromHeader(token, request, tokenName);
						break;
					case AuthConstant.READ_FROM_URL:
						currentTokenName = tokenName;
						token = doReadFromUrl(token, request, tokenName);
						break;
					default:
						throw new AuthException(AuthConstant.TYPE_NOT_SUPPORT_CODE, AuthConstant.TYPE_NOT_SUPPORT_MESSAGE);
				}
			}
			if (AuthStringUtils.isNotEmpty(token)) {
				tokenFrom = from;
				break;
			}
		}
		if (AuthStringUtils.isEmpty(token)) {
			throw new AuthException(AuthConstant.TOKEN_EMPTY_CODE, "???????????????????????????" + Arrays.toString(tokenNames) + "??????,?????????token??????????????????");
		}
		if (authConfig.getLog()) log.debug("???{}????????????{}:{}", tokenFrom, currentTokenName, token);
		if (authConfig.getWhiteTokenList().contains(token)) {
			request.setAttribute(AuthConstant.IS_WHITE_TOKEN, true);
		}
		request.setAttribute(AuthConstant.TOKEN_NAME, token);
	}

	private String doReadFromHeader(String userKey, HttpServletRequest servletRequest, String tokenName) {
		if (!ignoreCamelCase) {
			return servletRequest.getHeader(tokenName);
		}
		Enumeration<String> headerNames = servletRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String originHeader = headerNames.nextElement();
			String element = originHeader.replaceAll("-", "").trim();
			if (element.equalsIgnoreCase(tokenName)) {
				userKey = servletRequest.getHeader(originHeader);
				break;
			}
		}
		return userKey;
	}

	private String doReadFromUrl(String userKey, HttpServletRequest servletRequest, String tokenName) {
		if (!ignoreCamelCase) {
			return servletRequest.getParameter(tokenName);
		}
		Enumeration<String> parameterNames = servletRequest.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String originParam = parameterNames.nextElement();
			String element = originParam.replaceAll("-", "").trim();
			if (element.equalsIgnoreCase(tokenName)) {
				userKey = servletRequest.getParameter(originParam);
				break;
			}
		}
		return userKey;
	}
}
