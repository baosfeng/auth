package com.yizhu.auth.exception;

import com.yizhu.auth.result.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class AuthExceptionHandler {

	private final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

	@ResponseBody
	@ExceptionHandler(AuthException.class)
	public Response<String> authExceptionHandler(AuthException e) {
		log.error("出现权限相关异常", e);
		return Response.error(e.getCode(), e.getMessage());
	}
}
