package com.example;

import xyz.bsfeng.auth.anno.FieldSensitive;
import xyz.bsfeng.auth.constant.SensitiveEnum;
import xyz.bsfeng.auth.dao.UserInfo;

public class User extends UserInfo {

	private String mobilePhone;
	private Long id;
	@FieldSensitive(SensitiveEnum.PASSWORD)
	private String password;
	private String[] auths;
	private Long lockTime;
	private Boolean lock;

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

	@Override
	public void setRoles(String... auths) {
		this.auths = auths;
	}

	@Override
	public String[] getRoles() {
		return auths;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
