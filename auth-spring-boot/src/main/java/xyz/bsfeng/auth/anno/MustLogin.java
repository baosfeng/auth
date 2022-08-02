package xyz.bsfeng.auth.anno;

import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

/**
 * 使用在类上表示当前类的所有接口全部必须进行登录访问
 *
 * @author bsfeng
 * @date 2021/9/27 8:57
 */
@Order
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MustLogin {
}
