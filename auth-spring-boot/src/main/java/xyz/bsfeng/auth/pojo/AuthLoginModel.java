package xyz.bsfeng.auth.pojo;

import java.io.Serializable;

/**
 * @author bsfeng
 * @date 2021/9/1 16:43
 */
public class AuthLoginModel implements Serializable {
	/** 设备标识,用于支持是否允许同端互斥登录 */
	private String device;
	/** token有效时间 */
	private Long timeout;

	public AuthLoginModel() {
	}

	AuthLoginModel(String device, Long timeout) {
		this.device = device;
		this.timeout = timeout;
	}

	public String getDevice() {
		return device;
	}

	public Long getTimeout() {
		return timeout;
	}

	public AuthLoginModel setDevice(String device) {
		this.device = device;
		return this;
	}

	public AuthLoginModel setTimeout(Long timeout) {
		this.timeout = timeout;
		return this;
	}

	@Override
	public String toString() {
		return "AuthLoginModel{" +
				"device='" + device + '\'' +
				", timeout=" + timeout +
				'}';
	}
}
