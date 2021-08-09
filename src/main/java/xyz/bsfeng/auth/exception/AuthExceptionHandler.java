package xyz.bsfeng.auth.exception;

import xyz.bsfeng.auth.result.Response;
import xyz.bsfeng.auth.utils.SpringMVCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class AuthExceptionHandler {

	private final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

	@ResponseBody
	@ExceptionHandler(AuthException.class)
	public Response<String> authExceptionHandler(AuthException e) {
		log.error("出现权限相关异常", e);
		HttpServletResponse response = SpringMVCUtil.getResponse();
		response.setStatus(403);
		return Response.error(e.getCode(), e.getMessage());
	}
}
