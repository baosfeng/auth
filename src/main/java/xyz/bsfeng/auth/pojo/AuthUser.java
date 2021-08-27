package xyz.bsfeng.auth.pojo;

import xyz.bsfeng.auth.dao.UserInfo;

/**
 * 对于部分不想使用用户信息还需要继承接口的来说，可以利用此接口进行一些省事操作
 *
 * @author bsfeng
 * @date 2021/8/27 16:14
 */
public class AuthUser extends UserInfo {

	private Long id;

	public AuthUser() {
	}

	public AuthUser(Long id) {
		this.id = id;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
}
