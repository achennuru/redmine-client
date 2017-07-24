package com.redmine.easy;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import com.taskadapter.redmineapi.bean.TimeEntryFactory;

public class RedmineConnect {
	private static String userName = null;
	private static String password = null;

	RedmineManager mgr = null;
	public static void main(String[] args) throws RedmineException {
		String uri = "https://projects.enghouse.com";
		RedmineManager mgr = RedmineManagerFactory.createWithUserAuth(uri,
				userName == null ? userName = JOptionPane.showInputDialog("Enter User name") : userName,
				password == null ? password = JOptionPane.showInputDialog("Enter Password") : password);
		logTime(mgr);
	}

	private static JFrame frame = new JFrame("Input Dialog Example 3");

	private static int iterator = 0;

	private static String selectTask(List<Issue> issues) {
		String[] issuesArray = new String[issues.size()];
		iterator = 0;
		issues.stream().forEach(issue -> issuesArray[iterator++] = issue.getSubject());
		Arrays.sort(issuesArray, 0, issuesArray.length);
		String result = (String) JOptionPane.showInputDialog(frame, "Select Task?", "Task",
				JOptionPane.QUESTION_MESSAGE, null, issuesArray, issuesArray[0]);
		System.out.println(result);
		return result;
	}

	private static String selectActivity(List<TimeEntryActivity> activities) {
		String[] activitiesArray = new String[activities.size()];
		iterator = 0;
		activities.stream().forEach(activity -> activitiesArray[iterator++] = activity.getName());
		Arrays.sort(activitiesArray, 0, activitiesArray.length);
		return (String) JOptionPane.showInputDialog(frame, "Select Activity?", "Activity", JOptionPane.QUESTION_MESSAGE,
				null, activitiesArray, activitiesArray[0]);
	}

	private static String selectProject(List<Project> projects) {
		String[] projectsArray = new String[projects.size()];
		iterator = 0;
		projects.stream().forEach(project -> projectsArray[iterator++] = project.getName());
		Arrays.sort(projectsArray, 0, projectsArray.length);
		String name = (String) JOptionPane.showInputDialog(frame, "Project Name?", "Project",
				JOptionPane.QUESTION_MESSAGE, null, projectsArray, projectsArray[0]);
		return getProject(projects, name);
	}

	private static String getProject(List<Project> projects, String name) {
		for (Project project : projects) {
			if (project.getName().equals(name)) {
				return project.getIdentifier();
			}
		}
		return projects.get(0).getIdentifier();
	}

	private static void logTime(RedmineManager mgr) throws RedmineException {
		List<Project> projects = mgr.getProjectManager().getProjects();
		String projectId = selectProject(projects);
		Project project = mgr.getProjectManager().getProjectByKey(projectId);
		List<TimeEntryActivity> activities = mgr.getTimeEntryManager().getTimeEntryActivities();
		List<Issue> issues = mgr.getIssueManager().getIssues(project.getIdentifier(), null);
		TimeEntry timeEntry = TimeEntryFactory.create();
		timeEntry.setUserName(userName);
		timeEntry.setProjectId(project.getId());
		timeEntry.setProjectName(project.getName());
		timeEntry.setIssueId(getIssueId(issues, selectTask(issues)));
		timeEntry.setSpentOn(new Date());
		TimeEntryActivity activity = getActivity(activities, selectActivity(activities));
		timeEntry.setActivityId(activity.getId());
		timeEntry.setActivityName(activity.getName());
		timeEntry.setHours(Float.valueOf(JOptionPane.showInputDialog("Enter Number of hours")));
		timeEntry.setComment(JOptionPane.showInputDialog("Enter Comment"));
		System.out.println(timeEntry);
		// mgr.getTimeEntryManager().createTimeEntry(timeEntry);
	}

	private static TimeEntryActivity getActivity(List<TimeEntryActivity> activities, String string) {
		for (TimeEntryActivity issue : activities) {
			if (issue.getName().equals(string)) {
				return issue;
			}
		}
		return activities.get(0);
	}

	private static Integer getIssueId(List<Issue> issues, String string) {
		for (Issue issue : issues) {
			if (issue.getSubject().equals(string)) {
				return issue.getId();
			}
		}
		return issues.get(0).getId();
	}

}
