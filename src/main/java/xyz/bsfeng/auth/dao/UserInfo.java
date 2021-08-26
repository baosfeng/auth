package xyz.bsfeng.auth.dao;

/**
 * 必须实现此接口,且用户的id必须全局唯一
 * 交给用户控制
 *
 * @author bsfeng
 * @date 2021/8/7-9:08
 * @since 1.0
 */
public abstract class UserInfo {

	public abstract Long getId();

	public abstract void setId(Long id);

	public String[] getRoles() {
		return new String[]{};
	}

	public void setRoles(String... roles) {}

	public String[] getAuths() {
		return new String[]{};
	}

	public void setAuths(String... auths) {

	}

	public Boolean getLock() {
		return false;
	}

	public void setLock(Boolean lock) {

	}

	public Long getLockTime() {
		return System.nanoTime();
	}

	public void setLockTime(Long lockTime) {

	}
}
