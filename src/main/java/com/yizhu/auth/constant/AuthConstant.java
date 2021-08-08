package com.yizhu.auth.constant;

public interface AuthConstant {

	/** 从请求头的header中读取 */
	String READ_FROM_HEADER = "header";
	/** 从请求头的url中读取 */
	String READ_FROM_URL = "url";
	/** 读取方式不支持 */
	Integer TYPE_NOT_SUPPORT_CODE = 400;
	/** 读取方式不支持字符串 */
	String TYPE_NOT_SUPPORT_MESSAGE = "类型不受支持";
	/** token失效 */
	Integer NOT_LOGIN_CODE = 401;
	/** token失效 */
	String NOT_LOGIN_MESSAGE = "token失效或token过期,请重新登录!";
}
