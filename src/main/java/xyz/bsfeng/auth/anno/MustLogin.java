package xyz.bsfeng.auth.anno;

import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

/**
 * @author bsfeng
 * @date 2021/9/27 8:57
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Order
public @interface MustLogin {
}
