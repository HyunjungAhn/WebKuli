package com.nhncorp.ntaf.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CellTestResult {
	int[] position();
	String[] testResult();
}
