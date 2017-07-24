package com.redmine.easy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import com.github.cjwizard.APageFactory;
import com.github.cjwizard.StackWizardSettings;
import com.github.cjwizard.WizardContainer;
import com.github.cjwizard.WizardListener;
import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import com.taskadapter.redmineapi.RedmineAuthenticationException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;

public class TimeCollectWizard extends JDialog {
	private class WizardFactory extends APageFactory {

		// To keep things simple, we'll just create an array of wizard pages:
		private final WizardPage[] pages = { new WizardPage("Login", "Login Page") {
			// this is an instance initializer -- it's a constructor for
			// an anonymous class. WizardPages don't need to be anonymous,
			// of course. It just makes the demo fit in one file if we do it
			// this way:
			{
				Properties properties = LoginUtil.getAuthentication(true);
				setBackground(Color.WHITE);
				ImageIcon icon = new ImageIcon(this.getClass().getClassLoader().getResource("easy.png"));
				JLabel thumb = new JLabel();
				thumb.setIcon(icon);

				add(thumb);
				JTextField userField = new JTextField(30);
				userField.setName("userField");
				add(new JLabel("UserName"));
				add(userField);

				JPasswordField passwordField = new JPasswordField(30);
				passwordField.setName("passwordField");
				userField.setPreferredSize(new Dimension(50, 20));
				add(new JLabel("Password"));
				add(passwordField);
				if (properties.getProperty("user") != null) {
					userField.setText(properties.getProperty("user"));
				}
				if (properties.getProperty("password") != null) {
					passwordField.setText(properties.getProperty("password"));
				}
			}

			public void rendering(List<WizardPage> path, WizardSettings settings) {
				try {
					super.rendering(path, settings);
				} catch (Exception e) {

				}
			}

			@Override
			public void updateSettings(WizardSettings settings) {
				super.updateSettings(settings);
				startLoadingData();
				new Thread() {
					public void run() {
						try {
							projects.clear();
							projectModel.clear();
							List<Project> projectList = redmineConnector.getProjects((String) settings.get("userField"),
									new String((String) settings.get("passwordField")));
							projectList.sort(new Comparator<Project>() {
								@Override
								public int compare(Project o1, Project o2) {
									return o1.getName().compareTo(o2.getName());
								}
							});
							projectList.stream().forEach(project -> {
								projects.put(project.getName(), project);
								projectModel.addElement(project.getName());
							});
						} catch (RedmineAuthenticationException auth) {
							JOptionPane.showMessageDialog(TimeCollectWizard.this, "Invalid User name/Password.",
									"Login Failure", JOptionPane.ERROR_MESSAGE);
							failedLoadingData();
							throw new RuntimeException(auth);
						} catch (RedmineException e) {
							JOptionPane.showMessageDialog(TimeCollectWizard.this, e.getMessage(), "Login Failure",
									JOptionPane.ERROR_MESSAGE);
							failedLoadingData();
							throw new RuntimeException(e);
						} catch (Exception e) {
							failedLoadingData();
						}
						saveLogin(settings);
						finishedLoadingData();
					}
				}.start();
			}
		}, new WizardPage("Project", "Project Page") {
			{
				setBackground(Color.WHITE);
				JList<String> box = new JList<String>(projectModel);
				box.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
				box.setVisibleRowCount(10);
				box.setName("projectField");
				JScrollPane scroll = new JScrollPane(box, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				scroll.setPreferredSize(new Dimension(300, 400));
				add(scroll);
			}

			public void rendering(List<WizardPage> path, WizardSettings settings) {
				super.rendering(path, settings);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.github.cjwizard.WizardPage#updateSettings(com.github.cjwizard
			 * .WizardSettings)
			 */
			@Override
			public void updateSettings(WizardSettings settings) {
				super.updateSettings(settings);
				try {
					String projectName = (String) ((Object[]) settings.get("projectField"))[0];
					selectedProject = projects.get(projectName);
					settings.put("project", selectedProject);
					tasks.clear();
					taskModel.clear();

					List<Issue> tasksArray = new ArrayList<Issue>(redmineConnector.getTasks(selectedProject));
					tasksArray.sort(new Comparator<Issue>() {
						@Override
						public int compare(Issue o1, Issue o2) {
							return o1.getSubject().compareTo(o2.getSubject());
						}
					});
					tasksArray.stream().forEach(task -> {
						tasks.put(task.getSubject(), task);
						taskModel.addElement(task.getSubject());
					});
				} catch (RedmineException e) {
				} catch (Exception e) {

				}
			}

		}, new WizardPage("Task", "Task Page") {
			{
				setBackground(Color.WHITE);
				JList<String> box = new JList<String>(taskModel);
				box.setName("taskField");
				box.setVisibleRowCount(10);
				JScrollPane scroll = new JScrollPane(box, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scroll.setPreferredSize(new Dimension(300, 400));
				add(scroll);
			}

			public void rendering(List<WizardPage> path, WizardSettings settings) {
				super.rendering(path, settings);
			}

			@Override
			public void updateSettings(WizardSettings settings) {
				super.updateSettings(settings);
				try {
					settings.put("issue", tasks.get((String) ((Object[]) settings.get("taskField"))[0]));
					activities.clear();
					activitiesModel.clear();
					List<TimeEntryActivity> activitiesArray = new ArrayList<>(redmineConnector.getActivities());
					activitiesArray.sort(new Comparator<TimeEntryActivity>() {
						@Override
						public int compare(TimeEntryActivity o1, TimeEntryActivity o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});
					activitiesArray.stream().forEach(activity -> {
						activities.put(activity.getName(), activity);
						activitiesModel.addElement(activity.getName());
					});
				} catch (RedmineException e) {
					e.printStackTrace();
				} catch (Exception e) {

				}
			}

		}, new WizardPage("Activity", "Activity Page") {
			{
				setBackground(Color.WHITE);
				JList<String> box = new JList<String>(activitiesModel);
				box.setName("activityField");
				box.setVisibleRowCount(10);
				JScrollPane scroll = new JScrollPane(box, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scroll.setPreferredSize(new Dimension(300, 400));
				add(scroll);
			}

			public void rendering(List<WizardPage> path, WizardSettings settings) {
				try {
					super.rendering(path, settings);
				} catch (Exception e) {

				}
			}

			@Override
			public void updateSettings(WizardSettings settings) {
				super.updateSettings(settings);
				try {
					settings.put("activity", activities.get((String) ((Object[]) settings.get("activityField"))[0]));
				} catch (Exception e) {

				}
			}

		}, new WizardPage("Finalize", "Finalize Page") {
			{
				setBackground(Color.WHITE);
				JTextField hours = new JTextField(30);
				hours.setName("hours");
				JTextField comment = new JTextField(30);
				comment.setName("comment");
				hours.setPreferredSize(new Dimension(150, 20));
				comment.setPreferredSize(new Dimension(150, 20));
				add(new JLabel("Hours spent"));
				add(hours);
				add(new JLabel("Comment"));
				add(comment);
			}

			/**
			 * This is the last page in the wizard, so we will enable the finish
			 * button and disable the "Next >" button just before the page is
			 * displayed:
			 */
			public void rendering(List<WizardPage> path, WizardSettings settings) {
				super.rendering(path, settings);
				setFinishEnabled(true);
				setNextEnabled(false);
			}

		}, new WizardPage("Loading", "Please wait...") {
			{
				setLayout(new BorderLayout());
				setBackground(Color.WHITE);
				JLabel label = new JLabel();
				ImageIcon icon = new ImageIcon(this.getClass().getClassLoader().getResource("loading.gif"));

				label.setIcon(icon);
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setVerticalAlignment(JLabel.CENTER);
				add(label, BorderLayout.CENTER);
			}
		} };

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.github.cjwizard.PageFactory#createPage(java.util.List,
		 * com.github.cjwizard.WizardSettings)
		 */
		@Override
		public WizardPage createPage(List<WizardPage> path, WizardSettings settings) {
			// Get the next page to display. The path is the list of all wizard
			// pages that the user has proceeded through from the start of the
			// wizard, so we can easily see which step the user is on by taking
			// the length of the path. This makes it trivial to return the next
			// WizardPage:
			if (loadingData) {
				return pages[pages.length - 1];
			} else {
				if (path.size() > 0 && path.get(path.size() - 1).getTitle().equals("Loading")) {
					path.remove(path.size() - 1);
				}
				return pages[path.size()];
			}
			// if we wanted to, we could use the WizardSettings object like a
			// Map<String, Object> to change the flow of the wizard pages.
			// In fact, we can do arbitrarily complex computation to determine
			// the next wizard page.
		}

	}

	private static final String TEMP_FILE = System.getProperty("java.io.tmpdir") + "/.timecollect";

	private static void cli(LogTimeParser argumentParser, TimeCollectWizard timeCollect) {
		if (argumentParser.getProfilePath() == null) {
			argumentParser.printUsage();
			System.exit(-1);
		}
		String profilePath = argumentParser.getProfilePath();
		String hours = argumentParser.getHours();
		String comment = argumentParser.getComment();
		String activity = argumentParser.getActivity();
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(profilePath));
		} catch (Exception e) {
			System.err.println("Failed to load profile: " + e.getMessage());
			System.exit(-1);
		}
		properties.put("hours", hours);
		properties.put("comment", comment);

		Properties authenticationProperties = LoginUtil.getAuthentication(false);
		timeCollect.redmineConnector.connect(authenticationProperties.getProperty("user"),
				authenticationProperties.getProperty("password"));
		if (activity != null) {
			try {
				final List<TimeEntryActivity> activities = new ArrayList<>(
						timeCollect.redmineConnector.getActivities());
				TimeEntryActivity resolved = null;
				for (TimeEntryActivity activityEntry : activities) {
					if (activityEntry.getName().equals(activity)) {
						resolved = activityEntry;
						break;
					}
				}
				if (resolved == null) {
					System.err.println("Activity " + activity + " does not exist.");
					System.exit(-1);
				}
				properties.put("activity.id", resolved.getId() + "");
				properties.put("activity.name", resolved.getName());
			} catch (RedmineException e) {
				System.err.println("Activity " + activity + " does not exist.");
				System.exit(-1);
			}
		}
		try {
			properties.put("user", authenticationProperties.getProperty("user"));
			timeCollect.redmineConnector.logTime(properties);
			System.exit(0);
		} catch (RedmineException e) {
			System.err.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		LogTimeParser argumentParser = new LogTimeParser(args);
		TimeCollectWizard timeCollect = new TimeCollectWizard();
		if (argumentParser.getArguments().size() > 0) {
			cli(argumentParser, timeCollect);
		}
		timeCollect.pack();
		timeCollect.setVisible(true);
	}

	private final Map<String, TimeEntryActivity> activities = new HashMap<>();

	private final DefaultListModel<String> activitiesModel = new DefaultListModel<String>();
	private boolean loadingData = false;
	private final DefaultListModel<String> projectModel = new DefaultListModel<String>();
	private final Map<String, Project> projects = new HashMap<String, Project>();
	private final RedmineConnector redmineConnector = new RedmineConnector();
	private Project selectedProject = null;
	private final DefaultListModel<String> taskModel = new DefaultListModel<String>();
	private final Map<String, Issue> tasks = new HashMap<>();

	private final WizardContainer wc;

	public TimeCollectWizard() {
		// first, build the wizard. The TestFactory defines the
		// wizard content and behavior.
		wc = new WizardContainer(new WizardFactory(), new TitledPageTemplate(), new StackWizardSettings());

		// do you want to store previously visited path and repeat it if you hit
		// back
		// and then go forward a second time?
		// this options makes sense if you have a conditional path where
		// depending on choice of a page
		// you can visit one of two other pages.
		wc.setForgetTraversedPath(true);

		// add a wizard listener to update the dialog titles and notify the
		// surrounding application of the state of the wizard:
		wc.addWizardListener(new WizardListener() {
			@Override
			public void onCanceled(List<WizardPage> path, WizardSettings settings) {
				TimeCollectWizard.this.dispose();
			}

			@Override
			public void onFinished(List<WizardPage> path, WizardSettings settings) {
				try {
					redmineConnector.logTime(settings);
				} catch (RedmineException e) {
					e.printStackTrace();
				}
				TimeCollectWizard.this.dispose();
			}

			@Override
			public void onPageChanged(WizardPage newPage, List<WizardPage> path) {
				// Set the dialog title to match the description of the new
				// page:
				TimeCollectWizard.this.setTitle(newPage.getDescription());
			}
		});
		setBackground(Color.WHITE);

		// Set up the standard bookkeeping stuff for a dialog, and
		// add the wizard to the JDialog:
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.getContentPane().add(wc);
		this.setResizable(false);
		this.pack();
		this.setPreferredSize(new Dimension(400, 500));
	}

	private void failedLoadingData() {
		this.loadingData = false;
		wc.prev();
	}

	private void finishedLoadingData() {
		this.loadingData = false;
		wc.next();
	}

	private void saveLogin(WizardSettings settings) {
		try {
			Properties properties = new Properties();
			properties.put("user", (String) settings.get("userField"));
			properties.put("password", (String) settings.get("passwordField"));
			properties.store(new FileWriter(new File(TEMP_FILE)), "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startLoadingData() {
		this.loadingData = true;
	}

}
