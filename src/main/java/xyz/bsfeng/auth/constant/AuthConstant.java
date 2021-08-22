package xyz.bsfeng.auth.constant;

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
	/** 临时token过期 */
	Integer TEMP_TOKEN_VALID_CODE = 403;
	/** 临时token已过期 */
	String TEMP_TOKEN_VALID_MESSAGE = "临时token已过期";
	/** 当前账号没有访问改资源的权限 */
	Integer ACCOUNT_NO_ROLE_CODE = 404;
	/** 当前账号没有访问改资源的权限 */
	String ACCOUNT_NO_ROLE_MESSAGE = "当前账号没有访问该资源的角色信息！";
	/** 当前账号没有任何权限 */
	Integer ACCOUNT_NO_ANY_ROLE_CODE = 405;
	/** 当前账号没有任何权限 */
	String ACCOUNT_NO_ANY_ROLE_MESSAGE = "当前账号没有任何权限！";
	/** 踢出的用户token不能为空 */
	Integer KICK_OUT_TOKEN_EMPTY_CODE = 406;
	/** 踢出的用户token不能为空 */
	String KICK_OUT_TOKEN_EMPTY_MESSAGE = "踢出的用户token不能为空！";
	/** 踢出的用户token不能为空 */
	Integer LOCK_USER_TIME_VALID_CODE = 407;
	/** 封禁时间不能小于0 */
	String LOCK_USER_TIME_VALID_MESSAGE = "封禁时间不能小于0！";
	/** 该账号已被封禁，解封时间还有 */
	Integer ACCOUNT_LOCK_CODE = 408;
	/** 该账号已被封禁，解封时间还有 */
	String ACCOUNT_LOCK_MESSAGE = "该账号已被封禁，解封时间还有";
	/** 无法从请求体中获得token信息 */
	Integer TOKEN_EMPTY_CODE = 409;
	/** 无法从请求体中获得token信息 */
	String TOKEN_EMPTY_MESSAGE = "无法从请求体中获得token信息,请检查token名称是否正确";
	/** 当前账号没有访问该资源的权限 */
	Integer ACCOUNT_NO_AUTH_CODE = 410;
	/** 当前账号没有访问该资源的权限 */
	String ACCOUNT_NO_AUTH_MESSAGE = "当前账号没有访问该资源的权限！";
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
