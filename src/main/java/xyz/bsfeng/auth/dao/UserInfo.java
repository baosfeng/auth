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

	Boolean lock = false;
	Long lockTime = 0L;
	public abstract Long getId();

	public abstract void setId(Long id);

	public String[] getRoles() {
		return new String[]{};
	}

	public void setRoles(String... auths) {}

	public Boolean getLock() {
		return lock;
	}

	public void setLock(Boolean lock) {
		this.lock = lock;
	}

	public Long getLockTime() {
		return lockTime;
	}

	public void setLockTime(Long lockTime) {
		this.lockTime = lockTime;
	}
}
