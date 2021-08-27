package xyz.bsfeng.auth.pojo;

import xyz.bsfeng.auth.dao.TempUser;

/**
 * @author bsfeng
 * @date 2021/8/27 16:16
 */
public class AuthTempUser extends TempUser {

	private Long id;

	public AuthTempUser(Long id) {
		this.id = id;
	}

	public AuthTempUser() {
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
