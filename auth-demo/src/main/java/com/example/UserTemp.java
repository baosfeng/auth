package com.example;

import xyz.bsfeng.auth.dao.TempUser;

/**
 * @author bsfeng
 * @date 2021/8/11 18:30
 */
public class UserTemp extends TempUser {
	private Long id;

	@Override
	public boolean check(TempUser userTemp) {
		return userTemp.equals(this);
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
