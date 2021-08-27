package xyz.bsfeng.auth.config;

import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

/**
 * @author bsfeng
 * @date 2021/8/7-15:10
 * @since 1.0
 */
@Validated
public class AuthConfig {
	/** 是否启用验证 */
	private Boolean enable = true;
	/** token名称 */
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
	/** 在多人登录同一账号时，是否共用一个token (为true时所有登录共用一个token, 为false时每次登录新建一个token) */
	private Boolean globalShare = true;
	/** token的生成风格,全局唯一,支持random16,random32,random64,uuid,md5 */
	@NonNull
	private String tokenType = "random16";
	/** url放行白名单,支持通配符* */
	private String whiteUrlList = "";
	/** token放行白名单 */
	private String whiteTokenList = "";
	/** 超级管理员的角色名称 */
	private String adminRole = "administrator";
	/** 是否检查白名单url的token信息 */
	private Boolean checkWhiteUrlToken = false;
	/** 用于实现多种业务平台，多个业务平台使用不同的认证 */
	private String loginType = "login";

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

	public Boolean getGlobalShare() {
		return globalShare;
	}

	public void setGlobalShare(Boolean globalShare) {
		this.globalShare = globalShare;
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

	public Boolean getCheckWhiteUrlToken() {
		return checkWhiteUrlToken;
	}

	public void setCheckWhiteUrlToken(Boolean checkWhiteUrlToken) {
		this.checkWhiteUrlToken = checkWhiteUrlToken;
	}

	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
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
				", globalShare=" + globalShare +
				", tokenType='" + tokenType + '\'' +
				", whiteUrlList='" + whiteUrlList + '\'' +
				", whiteTokenList='" + whiteTokenList + '\'' +
				", adminRole='" + adminRole + '\'' +
				", checkWhiteUrlToken=" + checkWhiteUrlToken +
				", loginType='" + loginType + '\'' +
				'}';
	}
}
