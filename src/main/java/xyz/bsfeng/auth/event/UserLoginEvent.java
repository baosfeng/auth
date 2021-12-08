package xyz.bsfeng.auth.event;

import org.springframework.context.ApplicationEvent;
import xyz.bsfeng.auth.dao.UserInfo;

/**
 * @author bsfeng
 * @date 2021/10/13 9:26
 */
public class UserLoginEvent extends ApplicationEvent {

	/**
	 * 用户登录事件
	 *
	 * @param userInfo
	 */
	public UserLoginEvent(UserInfo userInfo) {
		super(userInfo);
	}

}
