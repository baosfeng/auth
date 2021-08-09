package com.example;

import xyz.bsfeng.auth.dao.UserInfo;

public class User implements UserInfo {

	private String mobilePhone;
	private Long id;

	public User() {
	}

	public User(Long id) {
		this.id = id;
	}

	public User(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	@Override
	public Long getId() {
		return id;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
