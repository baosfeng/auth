package com.yizhu.auth.config;

import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

/**
 * @author bsfeng
 * @date 2021/8/7-15:10
 * @since 1.0
 */
@Validated
public class AuthConfig {

	/** token名称 */
	@NonNull
	private String tokenName = "every-bim-token";
	/** token名称保存的前缀 */
	@NonNull
	private String tokenPrefix = "user:login:";
	/** token有效期 */
	@NonNull
	private long timeout = 60 * 60 * 24 * 30;
	/** token的来源 */
	@NonNull
	private String readFrom = "header,url";
	/** 是否忽略大小写和横杠字符 */
	@NonNull
	private Boolean ignoreCamelCase = true;
	/** 是否自动续签 */
	@NonNull
	private Boolean autoRenew = true;
	/** token的生成风格,支持random16,random32,random64,uuid,md5,全局唯一 */
	@NonNull
	private String tokenType = "random16";
	/** url放行白名单,支持通配符* */
	private String whiteUrlList = "";
	/** token放行白名单 */
	private String whiteTokenList = "";

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

	public String getWhiteTokenList() {
		return whiteTokenList;
	}

	public void setWhiteTokenList(String whiteTokenList) {
		this.whiteTokenList = whiteTokenList;
	}
}
