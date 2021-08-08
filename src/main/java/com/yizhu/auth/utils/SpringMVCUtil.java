package com.yizhu.auth.utils;


import com.yizhu.auth.constant.AuthConstant;
import com.yizhu.auth.exception.AuthException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SpringMVC相关操作
 *
 * @author bsfeng
 */
public class SpringMVCUtil {

	/**
	 * 获取当前会话的 request
	 *
	 * @return request
	 */
	public static HttpServletRequest getRequest() {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (servletRequestAttributes == null) {
			throw new AuthException(AuthConstant.NOT_SUPPORT_CODE, AuthConstant.NOT_SUPPORT_MESSAGE);
		}
		return servletRequestAttributes.getRequest();
	}

	/**
	 * 获取当前会话的 response
	 *
	 * @return response
	 */
	public static HttpServletResponse getResponse() {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (servletRequestAttributes == null) {
			throw new AuthException(AuthConstant.NOT_SUPPORT_CODE, AuthConstant.NOT_SUPPORT_MESSAGE);
		}
		return servletRequestAttributes.getResponse();
	}

}
