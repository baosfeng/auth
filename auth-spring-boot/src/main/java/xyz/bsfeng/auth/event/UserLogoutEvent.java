package xyz.bsfeng.auth.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author bsfeng
 * @date 2021/10/13 9:26
 */
public class UserLogoutEvent extends ApplicationEvent {
	/**
	 * 用户退出事件
	 *
	 * @param token
	 */
	public UserLogoutEvent(String token) {
		super(token);
	}
}
