package xyz.bsfeng.auth.config;

import xyz.bsfeng.auth.utils.AuthStringUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

/**
 * @author bsfeng
 * @date 2021/8/7-15:10
 * @since 1.0
 */
@Validated
public class AuthConfig implements InitializingBean {

	/** 是否启用验证 */
	private Boolean enable = true;
	/** token名称,如果使用逗号进行分割，表示可从多种方式读取 */
	@NonNull
	private String tokenName = "token";
	/** token名称保存的前缀 */
	@NonNull
	private String tokenPrefix = "";
	/** token有效期 */
	@NonNull
	private long timeout = 60 * 60 * 24;
	/** token的来源 */
	@NonNull
	private String readFrom = "header,url";
	/** 是否忽略大小写和横杠字符 */
	@NonNull
	private Boolean ignoreCamelCase = true;
	/** 是否自动续签 */
	@NonNull
	private Boolean autoRenew = true;
	/** token的生成风格,全局唯一,支持random16,random32,random64,uuid,md5 */
	@NonNull
	private String tokenType = "random16";
	/** url放行白名单,支持通配符* */
	private String whiteUrlList = "";
	/** url黑名单,支持通配符,优先级高于白名单,推荐使用注解@MustLogin * */
	private String blackUrlList = "";
	/** token放行白名单,推荐使用注解@IgnoreLogin */
	private String whiteTokenList = "";
	/** 超级管理员的角色名称 */
	private String adminRole = "administrator";
	/** 用于实现多种业务平台，多个业务平台使用不同的认证 */
	private String loginType = "login";
	/** 临时身份登录后缀 */
	private String tempSuffix = "temp";
	/** 是否允许同端登录 */
	private Boolean isAllowSampleDeviceLogin = true;
	/** 踢人时,是否忽略待踢除的人未登录,未登录将自动登录 */
	private Boolean kickOutIgnoreLogin = true;
	/** 是否需要打开日志输出 */
	private Boolean isLog = false;
	/** 是否设置白名单token为超管 */
	private Boolean whiteTokenAsAdmin = true;

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	@NonNull
	public String getTokenName() {
		return tokenName;
	}

	public void setTokenName(@NonNull String tokenName) {
		this.tokenName = tokenName;
	}

	@NonNull
	public String getTokenPrefix() {
		return tokenPrefix;
	}

	public void setTokenPrefix(@NonNull String tokenPrefix) {
		this.tokenPrefix = tokenPrefix;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@NonNull
	public String getReadFrom() {
		return readFrom;
	}

	public void setReadFrom(@NonNull String readFrom) {
		this.readFrom = readFrom;
	}

	@NonNull
	public Boolean getIgnoreCamelCase() {
		return ignoreCamelCase;
	}

	public void setIgnoreCamelCase(@NonNull Boolean ignoreCamelCase) {
		this.ignoreCamelCase = ignoreCamelCase;
	}

	@NonNull
	public Boolean getAutoRenew() {
		return autoRenew;
	}

	public void setAutoRenew(@NonNull Boolean autoRenew) {
		this.autoRenew = autoRenew;
	}

	@NonNull
	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(@NonNull String tokenType) {
		this.tokenType = tokenType;
	}

	public String getWhiteUrlList() {
		return whiteUrlList;
	}

	public void setWhiteUrlList(String whiteUrlList) {
		this.whiteUrlList = whiteUrlList;
	}

	public String getBlackUrlList() {
		return blackUrlList;
	}

	public void setBlackUrlList(String blackUrlList) {
		this.blackUrlList = blackUrlList;
	}

	public String getWhiteTokenList() {
		return whiteTokenList;
	}

	public void setWhiteTokenList(String whiteTokenList) {
		this.whiteTokenList = whiteTokenList;
	}

	public String getAdminRole() {
		return adminRole;
	}

	public void setAdminRole(String adminRole) {
		this.adminRole = adminRole;
	}

	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}

	public String getTempSuffix() {
		return tempSuffix;
	}

	public void setTempSuffix(String tempSuffix) {
		this.tempSuffix = tempSuffix;
	}

	public Boolean getAllowSampleDeviceLogin() {
		return isAllowSampleDeviceLogin;
	}

	public void setAllowSampleDeviceLogin(Boolean allowSampleDeviceLogin) {
		isAllowSampleDeviceLogin = allowSampleDeviceLogin;
	}

	public Boolean getKickOutIgnoreLogin() {
		return kickOutIgnoreLogin;
	}

	public void setKickOutIgnoreLogin(Boolean kickOutIgnoreLogin) {
		this.kickOutIgnoreLogin = kickOutIgnoreLogin;
	}

	public Boolean getLog() {
		return isLog;
	}

	public void setLog(Boolean log) {
		isLog = log;
	}

	public Boolean getWhiteTokenAsAdmin() {
		return whiteTokenAsAdmin;
	}

	public void setWhiteTokenAsAdmin(Boolean whiteTokenAsAdmin) {
		this.whiteTokenAsAdmin = whiteTokenAsAdmin;
	}

	@Override
	public String toString() {
		return "AuthConfig{" +
				"enable=" + enable +
				", tokenName='" + tokenName + '\'' +
				", tokenPrefix='" + tokenPrefix + '\'' +
				", timeout=" + timeout +
				", readFrom='" + readFrom + '\'' +
				", ignoreCamelCase=" + ignoreCamelCase +
				", autoRenew=" + autoRenew +
				", tokenType='" + tokenType + '\'' +
				", whiteUrlList='" + whiteUrlList + '\'' +
				", blackUrlList='" + blackUrlList + '\'' +
				", whiteTokenList='" + whiteTokenList + '\'' +
				", adminRole='" + adminRole + '\'' +
				", loginType='" + loginType + '\'' +
				", tempSuffix='" + tempSuffix + '\'' +
				", isAllowSampleDeviceLogin=" + isAllowSampleDeviceLogin +
				", kickOutIgnoreLogin=" + kickOutIgnoreLogin +
				", isLog=" + isLog +
				", whiteTokenAsAdmin=" + whiteTokenAsAdmin +
				'}';
	}

	@Value("${error.path:/error}")
	private String errorPath;

	@Override
	public void afterPropertiesSet() throws Exception {
		String join = Joiner.on(",").join(Lists.newArrayList("/favicon.ico", errorPath));
		if (AuthStringUtils.isNotEmpty(getWhiteUrlList())) {
			String s = getWhiteUrlList() + "," + join;
			setWhiteUrlList(s);
		} else {
			setWhiteUrlList(join);
		}
	}
}
