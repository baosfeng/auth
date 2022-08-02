package xyz.bsfeng.auth.pojo;

import java.util.Arrays;

public class AuthTempUserBuilder {
	private Long id;
	private String[] roles;
	private String[] auths;
	private Boolean lock;
	private Long lockTime;

	public AuthTempUserBuilder() {
	}

	public AuthTempUserBuilder id(Long id) {
		this.id = id;
		return this;
	}

	public AuthTempUserBuilder roles(String[] roles) {
		this.roles = roles;
		return this;
	}

	public AuthTempUserBuilder auths(String[] auths) {
		this.auths = auths;
		return this;
	}

	public AuthTempUserBuilder lock(Boolean lock) {
		this.lock = lock;
		return this;
	}

	public AuthTempUserBuilder lockTime(Long lockTime) {
		this.lockTime = lockTime;
		return this;
	}

	public AuthTempUser build() {
		if (id == null) throw new IllegalArgumentException("id不能为空");
		return new AuthTempUser(id, roles, auths, lock, lockTime);
	}

	public String toString() {
		return "AuthTempUser.AuthTempUserBuilder(id=" + this.id + ", roles=" + Arrays.deepToString(this.roles) + ", auths=" + Arrays.deepToString(this.auths) + ", lock=" + this.lock + ", lockTime=" + this.lockTime + ")";
	}
}