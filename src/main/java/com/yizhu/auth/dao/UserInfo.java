package com.yizhu.auth.dao;

/**
 * @author bsfeng
 * @date 2021/8/7-9:08
 * @since 1.0
 */
public interface UserInfo {
	Long id = null;

	default Long getId() {
		return id == null ? -1 : id;
	}
}
