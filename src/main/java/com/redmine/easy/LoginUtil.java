package com.redmine.easy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Properties;
import java.util.Scanner;

public class LoginUtil {
	private static final String TEMP_FILE = System.getProperty("java.io.tmpdir") + "/.timecollect";

	public static Properties getAuthentication(final boolean gui) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(TEMP_FILE)));
		} catch (Exception e) {

		}
		if(properties.getProperty("url") != null) {	
			System.setProperty("easy.url",properties.getProperty("url"));
		}
		if (properties.isEmpty() && !gui) {
			return askUserInput();
		}
		return properties;
	}

	private static Properties askUserInput() {
		Properties properties = new Properties();
		Scanner input = new Scanner(System.in);
		String url;
		String username;
		String password;
		System.out.println("url: ");
		url = input.next();
		System.out.println("username: ");
		username = input.next();
		System.out.println("password: ");
		password = input.next();
		properties.put("url", url);
		properties.put("user", username);
		properties.put("password", password);
		try {
			properties.store(new FileWriter(new File(TEMP_FILE)), "");
		} catch (Exception e) {

		}
		System.setProperty("easy.url",properties.getProperty("url"));
		return properties;
	}
}
