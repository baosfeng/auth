package xyz.bsfeng.auth.pojo;

import xyz.bsfeng.auth.dao.UserInfo;

import java.util.Arrays;

/**
 * 对于部分不想使用用户信息还需要继承接口的来说，可以利用此接口进行一些省事操作
 *
 * @author bsfeng
 * @date 2021/8/27 16:14
 */
public class AuthUser extends UserInfo {

	private Long id;
	private String[] roles;
	private String[] auths;
	private Boolean lock = false;
	private Long lockTime = System.currentTimeMillis();

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

	@Override
	public String[] getRoles() {
		return roles;
	}

	@Override
	public void setRoles(String... roles) {
		this.roles = roles;
	}

	@Override
	public String[] getAuths() {
		return auths;
	}

	@Override
	public void setAuths(String... auths) {
		this.auths = auths;
	}

	@Override
	public Boolean getLock() {
		return lock;
	}

	@Override
	public void setLock(Boolean lock) {
		this.lock = lock;
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
	public String toString() {
		return "AuthUser{" +
				"id=" + id +
				", roles=" + Arrays.toString(roles) +
				", auths=" + Arrays.toString(auths) +
				", lock=" + lock +
				", lockTime=" + lockTime +
				'}';
	}
}
