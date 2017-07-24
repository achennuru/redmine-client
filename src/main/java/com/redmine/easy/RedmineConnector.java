package com.redmine.easy;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.github.cjwizard.WizardSettings;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import com.taskadapter.redmineapi.bean.TimeEntryFactory;

public class RedmineConnector {
	private RedmineManager mgr = null;

	private static final String URI = "<<Redmine URL>>";

	private static final String PROFILE_FILE = System.getProperty("java.io.tmpdir") + "/.timecollect.profile";

	private List<Project> cachedProjects = new ArrayList<>();

	public List<Project> getProjects(String user, String password) throws RedmineException {
		connect(user, password);
		return mgr.getProjectManager().getProjects();
	}

	public Collection<? extends Issue> getTasks(Project project) throws RedmineException {
		return mgr.getIssueManager().getIssues(project.getIdentifier(), null);
	}

	public Collection<? extends TimeEntryActivity> getActivities() throws RedmineException {
		return mgr.getTimeEntryManager().getTimeEntryActivities();
	}

	public void createTask(String project, String subject, String parent) {
		Issue issueToCreate = IssueFactory.create(getProjectId(project), subject);
		if (parent != null) {
			issueToCreate.setParentId(getIssueId(project, parent));
		}
		Issue issue = null;
		try {
			issue = mgr.getIssueManager().createIssue(issueToCreate);
		} catch (RedmineException e) {
			System.out.println("Cannot create task ");
		}
		if (issue != null) {
			System.out.println("Issue created: " + issue.getSubject() + " (" + issue.getId() + ")");
		} else {
			System.out.println("Failed to create issue " + issue + " in project " + project + " with parent " + parent);
		}
	}

	public Integer getProjectId(final String project) {
		try {
			if (cachedProjects.isEmpty()) {
				cachedProjects.addAll(mgr.getProjectManager().getProjects());
			}
			return cachedProjects.stream().filter(projectEntry -> projectEntry.getName().equals(project)).findFirst()
					.get().getId();
		} catch (Exception e) {
			System.out.println("Cannot find Project: " + project);
		}
		return null;
	}

	public Integer getIssueId(final String project, final String issue) {
		try {
			return mgr.getIssueManager().getIssuesBySummary(getProjectId(project) + "", issue).iterator().next()
					.getId();
		} catch (RedmineException e) {
			System.out.println("Cannot find issue " + issue + " in project " + project);
		}
		return null;
	}

	public void logTime(WizardSettings settings) throws RedmineException {
		TimeEntry timeEntry = TimeEntryFactory.create();
		timeEntry.setUserName((String) settings.get("userField"));
		timeEntry.setProjectId(((Project) settings.get("project")).getId());
		timeEntry.setProjectName(((Project) settings.get("project")).getName());
		timeEntry.setIssueId(((Issue) settings.get("issue")).getId());
		timeEntry.setSpentOn(new Date());
		timeEntry.setActivityId(((TimeEntryActivity) settings.get("activity")).getId());
		timeEntry.setActivityName(((TimeEntryActivity) settings.get("activity")).getName());
		timeEntry.setHours(Float.valueOf((String) settings.get("hours")));
		timeEntry.setComment((String) settings.get("comment"));
		System.out.println(timeEntry);
		mgr.getTimeEntryManager().createTimeEntry(timeEntry);
		saveProfile(timeEntry);
	}

	private void saveProfile(TimeEntry timeEntry) {
		Properties properties = new Properties();
		properties.put("project.id", timeEntry.getProjectId() + "");
		properties.put("project.name", timeEntry.getProjectName());
		properties.put("issue.id", timeEntry.getIssueId() + "");
		properties.put("activity.id", timeEntry.getActivityId() + "");
		properties.put("activity.name", timeEntry.getActivityName());
		try {
			properties.store(new FileWriter(new File(PROFILE_FILE)), "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void connect(String user, String password) {
		mgr = RedmineManagerFactory.createWithUserAuth(URI, user, password);
	}

	public void logTime(Properties properties) throws RedmineException {
		TimeEntry timeEntry = TimeEntryFactory.create();
		timeEntry.setUserName(properties.getProperty("user"));
		timeEntry.setProjectId(Integer.parseInt(properties.getProperty("project.id")));
		timeEntry.setProjectName(properties.getProperty("project.name"));
		timeEntry.setIssueId(Integer.parseInt(properties.getProperty("issue.id")));
		timeEntry.setSpentOn(new Date());
		timeEntry.setActivityId(Integer.parseInt(properties.getProperty("activity.id")));
		timeEntry.setActivityName(properties.getProperty("activity.name"));
		timeEntry.setHours(Float.valueOf(properties.getProperty("hours")));
		timeEntry.setComment(properties.getProperty("comment", ""));
		System.out.println(timeEntry);
		mgr.getTimeEntryManager().createTimeEntry(timeEntry);
	}

}
