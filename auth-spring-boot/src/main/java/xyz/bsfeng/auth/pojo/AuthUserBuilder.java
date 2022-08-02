package xyz.bsfeng.auth.pojo;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author bsfeng
 * @date 2021/9/18 10:28
 */
public class AuthUserBuilder {

	private Long id;
	private String[] roles;
	private String[] auths;
	private Boolean lock;
	private Long lockTime;
	private Long expireTime = 0L;
	private String deviceId;

	public AuthUserBuilder() {
	}

	public AuthUserBuilder id(Long id) {
		this.id = id;
		return this;
	}

	public AuthUserBuilder roles(String[] roles) {
		this.roles = roles;
		return this;
	}

	public AuthUserBuilder roles(Collection<String> roles) {
		this.roles = roles.toArray(new String[0]);
		return this;
	}

	public AuthUserBuilder auths(String[] auths) {
		this.auths = auths;
		return this;
	}

	public AuthUserBuilder auths(Collection<String> auths) {
		this.auths = auths.toArray(new String[0]);
		return this;
	}

	public AuthUserBuilder lock(Boolean lock) {
		this.lock = lock;
		return this;
	}

	public AuthUserBuilder lockTime(Long lockTime) {
		this.lockTime = lockTime;
		return this;
	}
	public AuthUserBuilder expireTime(Long expireTime) {
		this.expireTime = expireTime;
		return this;
	}
	public AuthUserBuilder deviceId(String deviceId) {
		this.deviceId = deviceId;
		return this;
	}

	public AuthUser build() {
		if (id == null) throw new IllegalArgumentException("id不能为空");
		return new AuthUser(id, roles, auths, lock, lockTime, expireTime, deviceId);
	}

	public String toString() {
		return "AuthUserBuilder(id=" + this.id + ", roles=" + Arrays.deepToString(this.roles) + ", auths=" + Arrays.deepToString(this.auths) + ", lock=" + this.lock + ", lockTime=" + this.lockTime + ")";
	}
}
