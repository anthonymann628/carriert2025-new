package com.agilegeodata.carriertrack.android.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * form validation utilities
 *
 */
public class FormValidation{

	public static boolean requiredField(String fieldStr, int length){
		if(fieldStr == null){
			return false;
		}
		else{
			return fieldStr.length() >= length;
		}
	}

	public static boolean validateStringByRule(String string, String rule){
		Pattern p = Pattern.compile(rule);

		//=== Match the given string with the pattern
		Matcher m = p.matcher(string);

		//=== check whether match is found
		boolean matchFound = m.matches();
		return matchFound;

	}
}
