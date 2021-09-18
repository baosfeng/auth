package xyz.bsfeng.auth.pojo;

import xyz.bsfeng.auth.dao.TempUser;
import xyz.bsfeng.auth.utils.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author bsfeng
 * @date 2021/8/27 16:16
 */
public class AuthTempUser extends TempUser {

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

	public void serRoles(Collection<String> roles) {
		if (CollectionUtils.isEmpty(roles)) return;
		this.roles = roles.toArray(new String[0]);
	}

	@Override
	public String[] getAuths() {
		return auths;
	}

	@Override
	public void setAuths(String... auths) {
		this.auths = auths;
	}

	public void setAuths(Collection<String> auths) {
		if (CollectionUtils.isEmpty(auths)) return;
		this.auths = auths.toArray(new String[0]);
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
