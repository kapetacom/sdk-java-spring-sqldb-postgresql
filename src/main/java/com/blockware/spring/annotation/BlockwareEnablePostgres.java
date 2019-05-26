package com.blockware.spring.annotation;

import com.blockware.spring.postgres.PostgresConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({
    PostgresConfiguration.class
})
public @interface BlockwareEnablePostgres {
}
