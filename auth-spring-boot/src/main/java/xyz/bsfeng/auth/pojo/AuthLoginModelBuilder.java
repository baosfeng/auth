package xyz.bsfeng.auth.pojo;

public class AuthLoginModelBuilder {

	private String device;
	private Long timeout;

	public AuthLoginModelBuilder() {
	}

	public AuthLoginModelBuilder device(String device) {
		this.device = device;
		return this;
	}

	public AuthLoginModelBuilder timeout(Long timeout) {
		this.timeout = timeout;
		return this;
	}

	public AuthLoginModel build() {
		if (timeout == null) throw new IllegalArgumentException("timeout不能为空");
		return new AuthLoginModel(device, timeout);
	}

	public String toString() {
		return "AuthLoginModel.AuthLoginModelBuilder(device=" + this.device + ", timeout=" + this.timeout + ")";
	}
}