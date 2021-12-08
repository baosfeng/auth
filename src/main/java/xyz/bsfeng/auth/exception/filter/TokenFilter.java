package xyz.bsfeng.auth.exception.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.bsfeng.auth.config.AuthConfig;
import xyz.bsfeng.auth.constant.AuthConstant;
import xyz.bsfeng.auth.exception.AuthException;
import xyz.bsfeng.auth.utils.BooleanUtils;
import xyz.bsfeng.auth.utils.StringUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;

import static xyz.bsfeng.auth.constant.AuthConstant.*;

/**
 * @author Administrator
 * @date 2021/12/7 21:58
 * @since 1.0.0
 */
public class TokenFilter implements AuthFilter {

	private final Logger log = LoggerFactory.getLogger(TokenFilter.class);
	private Boolean ignoreCamelCase;

	@Override
	public void doChain(@Nonnull HttpServletRequest request,
	                    @Nonnull HttpServletResponse response,
	                    @Nonnull AuthConfig authConfig,
	                    @Nonnull Method method) {
		if (BooleanUtils.isTrue((Boolean) request.getAttribute(IS_WHITE_URL))) return;
		String token = "";
		String tokenFrom = "";
		String currentTokenName = "";
		ignoreCamelCase = authConfig.getIgnoreCamelCase();
		String[] tokenNames = authConfig.getTokenName().split(",");
		for (String from : authConfig.getReadFrom().split(",")) {
			for (String tokenName : authConfig.getTokenName().split(",")) {
				if (StringUtils.isNotEmpty(token)) break;
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
			if (StringUtils.isNotEmpty(token)) {
				tokenFrom = from;
				break;
			}
		}
		if (StringUtils.isEmpty(token)) {
			throw new AuthException(AuthConstant.TOKEN_EMPTY_CODE, "无法从请求体中获得" + Arrays.toString(tokenNames) + "信息,请检查token名称是否正确");
		}
		if (authConfig.getLog()) log.debug("从{}中获取到{}:{}", tokenFrom, currentTokenName, token);
		request.setAttribute(TOKEN_NAME, token);
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
