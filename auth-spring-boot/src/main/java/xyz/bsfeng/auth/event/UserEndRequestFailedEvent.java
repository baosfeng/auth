package xyz.bsfeng.auth.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author bsfeng
 * @date 2021/10/13 9:28
 */
public class UserEndRequestFailedEvent extends ApplicationEvent {
	private final HandlerMethod handlerMethod;
	private final HttpServletResponse response;
	private final Exception ex;

	/**
	 * 用户退出系统事件有错误发生
	 *
	 * @param request
	 */
	public UserEndRequestFailedEvent(HttpServletRequest request,
									 HttpServletResponse response,
	                                 HandlerMethod handlerMethod,
	                                 Exception ex) {
		super(request);
		this.handlerMethod = handlerMethod;
		this.response = response;
		this.ex = ex;
	}

	public HandlerMethod getHandlerMethod() {
		return handlerMethod;
	}

	public Exception getEx() {
		return ex;
	}

	public HttpServletResponse getResponse() {
		return response;
	}
}
