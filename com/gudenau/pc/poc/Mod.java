package com.gudenau.pc.poc;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented

/**
 * @author gudenau
 * @version 1
 * @since 1
 * */
public @interface Mod {
	/**
	 * The id of the mod
	 * */
	String id();
}
