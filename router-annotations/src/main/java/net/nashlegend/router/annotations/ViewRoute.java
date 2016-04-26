package net.nashlegend.router.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target(TYPE)
@Retention(CLASS)
public @interface ViewRoute {
    String[] value();

    String[] intExtra() default "";

    String[] longExtra() default "";

    String[] boolExtra() default "";

    String[] shortExtra() default "";

    String[] floatExtra() default "";

    String[] doubleExtra() default "";

    String[] stringExtra() default "";

    String[] required() default "";

    String[] transfer() default "";
}
