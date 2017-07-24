package com.redmine.easy;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.OptionHandler;

public class LogTimeParser {
	@Option(name = "-p", usage = "profile path", required = false)
	private String profilePath;

	@Option(name = "-h", usage = "Number of hours worked", required = false)
	private String hours;

	@Option(name = "-c", usage = "comment", required = false)
	private String comment;

	@Option(name = "-a", usage = "Activity name", required = false)
	private String activity;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	private final CmdLineParser parser;

	public LogTimeParser(final String[] args) {
		parser = new CmdLineParser(this);
		parser.getProperties().withUsageWidth(80);
		try {
			// parse the arguments.
			parser.parseArgument(args);
		} catch (CmdLineException e) {

		}
	}

	public String getProfilePath() {
		return profilePath;
	}

	public String getHours() {
		return hours;
	}

	public String getComment() {
		return comment;
	}

	public String getActivity() {
		return activity;
	}

	public List<OptionHandler> getArguments() {
		return parser.getArguments();
	}

	public void printUsage() {
		parser.printUsage(System.out);
	}

}
