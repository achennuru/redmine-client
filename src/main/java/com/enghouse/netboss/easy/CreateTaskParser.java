package com.enghouse.netboss.easy;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class CreateTaskParser {

	@Option(name = "-F", usage = "csv file path", required=false)
	private String file;

	@Option(name = "-P", usage = "Project name", required=false)
	private String project;

	@Option(name = "-s", usage = "Issue subject to be created", required=false)
	private String subject;

	@Option(name = "-p", usage = "Issue Parent subject", required=false)
	private String parent;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	public CreateTaskParser(final String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		parser.getProperties().withUsageWidth(80);
		try {
			// parse the arguments.
			parser.parseArgument(args);
			// you can parse additional arguments if you want.
			// parser.parseArgument("more","args");

			// after parsing arguments, you should check
			// if enough arguments are given.
			if (parser.getArguments().isEmpty())
				throw new CmdLineException(parser, "No argument is given");
		} catch (CmdLineException e) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			System.err.println("java CreateTask [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();
			System.exit(-1);
		}
	}

	public String getFile() {
		return file;
	}

	public String getProject() {
		return project;
	}

	public String getSubject() {
		return subject;
	}

	public String getParent() {
		return parent;
	}

	public List<String> getArguments() {
		return arguments;
	}

}
