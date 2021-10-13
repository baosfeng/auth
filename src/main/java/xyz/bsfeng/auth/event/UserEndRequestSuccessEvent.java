package xyz.bsfeng.auth.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author bsfeng
 * @date 2021/10/13 9:28
 */
public class UserEndRequestSuccessEvent extends ApplicationEvent {
	private final HandlerMethod handlerMethod;
	private final HttpServletResponse response;

	/**
	 * 用户退出系统事件,无错误发生
	 *
	 * @param request
	 */
	public UserEndRequestSuccessEvent(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
		super(request);
		this.handlerMethod = handlerMethod;
		this.response = response;
	}

	public HandlerMethod getHandlerMethod() {
		return handlerMethod;
	}

	public HttpServletResponse getResponse() {
		return response;
	}
}
