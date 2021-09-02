package xyz.bsfeng.auth.dao;

/**
 * @author bsfeng
 * @date 2021/9/1 16:57
 */
public class UserModel {
	/** token过期时间 */
	private Long expireTime;
	/** 设备标识 */
	private String device;
	/** 被挤下线的时间 */
	private Long offlineTime;
	/** 被哪个token挤下线 */
	private String token;

	public Long getExpireTime() {
		return expireTime;
	}

	public UserModel setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
		return this;
	}

	public String getDevice() {
		return device;
	}

	public UserModel setDevice(String device) {
		this.device = device;
		return this;
	}

	public Long getOfflineTime() {
		return offlineTime;
	}

	public UserModel setOfflineTime(Long offlineTime) {
		this.offlineTime = offlineTime;
		return this;
	}

	public String getToken() {
		return token;
	}

	public UserModel setToken(String token) {
		this.token = token;
		return this;
	}
}
