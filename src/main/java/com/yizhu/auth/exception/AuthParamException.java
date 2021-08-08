package com.yizhu.auth.exception;

public class AuthParamException extends RuntimeException {

	private String field;

	public AuthParamException(String field) {
		this.field = field;
	}

	public String getField() {
		return field;
	}
}
