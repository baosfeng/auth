package xyz.bsfeng.auth.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * @author bsfeng
 * @date 2021/10/13 9:28
 */
public class UserBeginRequestEvent extends ApplicationEvent {

	private final HandlerMethod handlerMethod;
	/**
	 * 用户进入系统事件
	 *
	 * @param request
	 */
	public UserBeginRequestEvent(HttpServletRequest request, HandlerMethod handlerMethod) {
		super(request);
		this.handlerMethod = handlerMethod;
	}

	public HandlerMethod getHandlerMethod() {
		return handlerMethod;
	}
}
