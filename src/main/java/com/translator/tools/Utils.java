package com.translator.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	public static String findFileName(String filePath) {
		String objectName;
		Pattern pattern = Pattern.compile("([^\\/.]+)\\.[^.]*$");
        Matcher matcher = pattern.matcher(filePath);
        matcher.find();
        objectName = matcher.group();
		return objectName;
	}
	
	public static Properties loadProperties(String propFileName) throws IOException {
		Properties prop = new Properties();
		FileInputStream ip = new FileInputStream(propFileName);
		prop.load(ip);
		return prop;
	}

}
