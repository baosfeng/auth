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
	/** 获取request失败 */
	Integer NOT_SUPPORT_CODE = 402;
	/** 获取request失败 */
	String NOT_SUPPORT_MESSAGE = "非Web上下文无法获取Request";
	/** md5方式生成token */
	String TYPE_MD5 = "md5";
	/** 随机16位字符方式生成token */
	String TYPE_RANDOM16 = "random16";
	/** 随机32位字符方式生成token */
	String TYPE_RANDOM32 = "random32";
	/** 随机64位字符方式生成token */
	String TYPE_RANDOM64 = "random64";
	/** uuid方式生成token */
	String TYPE_UUID = "uuid";
}
