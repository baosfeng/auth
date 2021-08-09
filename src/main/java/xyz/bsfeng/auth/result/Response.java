package xyz.bsfeng.auth.result;


/**
 * @author bsfeng
 * @date 2021/4/28 11:54
 */
public class Response<T> {
	/** 响应成功的返回码 */
	public static final Integer SUCCESS_CODE = 200;
	/** 响应失败的返回码 */
	public static final Integer ERROR_CODE = 400;
	/**  */
	public static final String ERROR_TOKEN_EMPTY = "failed";

	/**
	 * 返回码
	 */
	private Integer code;
	/**
	 * 返回描述
	 */
	private String message;

	private Response(Integer code, String message) {
		this.setCode(code);
		this.setMessage(message);
	}

	public static <T> Response<T> error(Integer code, String message) {
		return instance(code, message);
	}

	public static <T> Response<T> instance(Integer code, String message) {
		return new Response<>(code, message);
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
