package xyz.bsfeng.auth.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author bsfeng
 * @date 2021/10/13 10:24
 */
public class UserLockEvent extends ApplicationEvent {
	private final Long lockTime;

	public UserLockEvent(Long id, Long lockTime) {
		super(id);
		this.lockTime = lockTime;
	}

	public Long getLockTime() {
		return lockTime;
	}
}
