package com.yizhu.auth.dao;

/**
 * @author bsfeng
 * @date 2021/8/7-9:08
 * @since 1.0
 */
public abstract class UserInfo {

	/** 用户id */
	protected Long id;

	public UserInfo() {
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id == null ? -1 : id;
	}
}
