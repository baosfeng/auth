package xyz.bsfeng.auth.anno;

import xyz.bsfeng.auth.constant.SensitiveEnum;

import java.lang.annotation.*;

/**
 * @author bsfeng
 * @date 2021/8/26 14:21
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldSensitive {
	/**
	 * 指定的字段是否需要数据脱敏
	 *
	 * @return
	 */
	SensitiveEnum value();
}
