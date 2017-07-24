package com.redmine.easy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import au.com.bytecode.opencsv.CSVReader;

public class CreateTask {
	public static void main(String[] args) {
		CreateTaskParser parser = new CreateTaskParser(args);
		RedmineConnector connector = new RedmineConnector();
		Properties properties = LoginUtil.getAuthentication(false);
		connector.connect(properties.getProperty("user"), properties.getProperty("password"));
		if (parser.getFile() != null) {
			processCsv(parser, connector);
		} else {
			connector.createTask(parser.getProject(), parser.getSubject(), parser.getParent());
		}
	}

	private static void processCsv(CreateTaskParser parser, RedmineConnector connector) {
		try (CSVReader csvParser = new CSVReader(new FileReader(new File(parser.getFile())))) {
			try {
				csvParser.readAll().stream().forEach(row -> connector.createTask(row[0], row[1], row[2]));
			} catch (IOException e) {
			}
		} catch (IOException e) {
			System.out.println("Unable to read csv: " + e.getMessage());
		}
	}
}
