package com.example;

import xyz.bsfeng.auth.dao.UserInfo;

public class User extends UserInfo {

	private String mobilePhone;
	private Long id;
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

	@Override
	public String[] getAuths() {
		return auths;
	}

	@Override
	public void setAuths(String[] auths) {
		this.auths = auths;
	}

	@Override
	public Long getLockTime() {
		return lockTime;
	}

	@Override
	public void setLockTime(Long lockTime) {
		this.lockTime = lockTime;
	}

	@Override
	public Boolean getLock() {
		return lock;
	}

	@Override
	public void setLock(Boolean lock) {
		this.lock = lock;
	}
}
