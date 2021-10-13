package xyz.bsfeng.auth.event;

import org.springframework.context.ApplicationEvent;
import xyz.bsfeng.auth.dao.UserInfo;
import xyz.bsfeng.auth.pojo.UserModel;

/**
 * @author bsfeng
 * @date 2021/10/13 9:26
 */
public class UserLoginEvent extends ApplicationEvent {

	private final UserModel userModel;

	/**
	 * 用户登录事件
	 *
	 * @param userInfo
	 * @param userModel
	 */
	public UserLoginEvent(UserInfo userInfo, UserModel userModel) {
		super(userInfo);
		this.userModel = userModel;
	}

	public UserModel getUserModel() {
		return userModel;
	}
}
