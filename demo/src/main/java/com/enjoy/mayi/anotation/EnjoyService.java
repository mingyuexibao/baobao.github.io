package com.enjoy.mayi.anotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)//作用范围
@Retention(RetentionPolicy.RUNTIME)//系统运行时
@Documented //javadoc
public @interface EnjoyService {

	String value() default "";

}
