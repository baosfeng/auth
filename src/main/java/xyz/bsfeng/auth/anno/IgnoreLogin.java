package xyz.bsfeng.auth.anno;

import java.lang.annotation.*;

/**
 * 当前接口可不登录进行访问
 *
 * @author bsfeng
 * @date 2021/9/22 14:34
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreLogin {
}
