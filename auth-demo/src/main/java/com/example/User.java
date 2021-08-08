package com.example;

import com.yizhu.auth.dao.UserInfo;

public class User extends UserInfo {

	private String mobilePhone;

	public User(Long id) {
		this.id = id;
	}

	public User(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}
}
