package org.dcache.restful.interceptor;

import javax.ws.rs.NameBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Compress annotation
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface Compress {}
