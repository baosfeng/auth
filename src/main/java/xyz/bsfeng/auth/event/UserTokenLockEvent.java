package xyz.bsfeng.auth.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author bsfeng
 * @date 2021/10/13 10:24
 */
public class UserTokenLockEvent extends ApplicationEvent {
	private final Long lockTime;

	public UserTokenLockEvent(String token, Long lockTime) {
		super(token);
		this.lockTime = lockTime;
	}

	public Long getLockTime() {
		return lockTime;
	}
}
