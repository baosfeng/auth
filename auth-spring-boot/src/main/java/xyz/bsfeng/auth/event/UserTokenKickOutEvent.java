package xyz.bsfeng.auth.event;

import org.springframework.context.ApplicationEvent;
import xyz.bsfeng.auth.dao.UserInfo;

/**
 * @author bsfeng
 * @date 2021/10/13 9:26
 */
public class UserTokenKickOutEvent extends ApplicationEvent {

	private final String token;

	/**
	 * 用户token被踢出事件
	 *
	 * @param userInfo
	 * @param token
	 */
	public UserTokenKickOutEvent(UserInfo userInfo, String token) {
		super(userInfo);
		this.token = token;
	}

	public String getToken() {
		return token;
	}
}
