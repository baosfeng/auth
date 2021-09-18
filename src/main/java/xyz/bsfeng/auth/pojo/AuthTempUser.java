package xyz.bsfeng.auth.pojo;

import xyz.bsfeng.auth.dao.TempUser;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author bsfeng
 * @date 2021/8/27 16:16
 */
public class AuthTempUser extends TempUser implements Serializable {

	private Long id;

	private String[] roles;
	private String[] auths;
	private Boolean lock = false;
	private Long lockTime = System.currentTimeMillis();

	public AuthTempUser(Long id) {
		this.id = id;
	}

	private AuthTempUser() {
	}

	public AuthTempUser(Long id, String[] roles, String[] auths, Boolean lock, Long lockTime) {
		this.id = id;
		this.roles = roles;
		this.auths = auths;
		this.lock = lock;
		this.lockTime = lockTime;
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
		return "AuthTempUser{" +
				"id=" + id +
				", roles=" + Arrays.toString(roles) +
				", auths=" + Arrays.toString(auths) +
				", lock=" + lock +
				", lockTime=" + lockTime +
				'}';
	}
}
