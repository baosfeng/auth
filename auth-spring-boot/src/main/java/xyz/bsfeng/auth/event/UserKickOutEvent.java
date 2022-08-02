package xyz.bsfeng.auth.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author bsfeng
 * @date 2021/10/13 9:26
 */
public class UserKickOutEvent extends ApplicationEvent {

	/**
	 * 用户被踢出事件
	 *
	 * @param id
	 */
	public UserKickOutEvent(Long id) {
		super(id);
	}

}
