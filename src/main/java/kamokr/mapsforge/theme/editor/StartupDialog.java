package kamokr.mapsforge.theme.editor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public class StartupDialog extends JDialog {
    private static final String SETTINGS_FILENAME = "mapsforge-theme-editor-settings.xml";
    private static final String PROJECT_FILENAME = "editor.project";

    private Settings settings;
    private JList<Settings.RecentProjectInfo> recentProjectsList;
    private DefaultListModel<Settings.RecentProjectInfo> recentProjectListModel;
    private StartupDialogResult result = null;

    public class StartupDialogResult {
        public File projectFile = null;
        public File themeFile = null;
        public File mapFile = null;
        public String projectName = null;
        public boolean createNewProject = false;
    }

    public StartupDialog(Frame parent) {
        super(parent, "Mapsforge Theme Editor", true);
        initializeUI();
        loadSettings();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header with buttons
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Recent projects list
        JPanel projectsPanel = createProjectsPanel();
        mainPanel.add(projectsPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(),
                "Projects"));

        JButton newProjectBtn = new JButton("New Project");
        JButton openProjectBtn = new JButton("Open Project");
        JButton openThemeBtn = new JButton("Open Theme");

        newProjectBtn.addActionListener(this::createNewProject);
        openProjectBtn.addActionListener(this::openProject);
        openThemeBtn.addActionListener(this::openTheme);

        headerPanel.add(newProjectBtn);
        headerPanel.add(openProjectBtn);
        headerPanel.add(openThemeBtn);

        return headerPanel;
    }

    private JPanel createProjectsPanel() {
        JPanel projectsPanel = new JPanel(new BorderLayout());
        projectsPanel.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "Recent Projects"));

        recentProjectListModel = new DefaultListModel<>();
        recentProjectsList = new JList<>(recentProjectListModel);
        recentProjectsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentProjectsList.setCellRenderer(new ProjectListRenderer());

        // Double-click to open project
        recentProjectsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Settings.RecentProjectInfo selected = recentProjectsList.getSelectedValue();
                    if (selected != null) {
                        openSelectedProject(selected);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(recentProjectsList);
        projectsPanel.add(scrollPane, BorderLayout.CENTER);

        return projectsPanel;
    }

    private void createNewProject(ActionEvent e) {
        StartupNewProjectDialog startupNewProjectDialog = new StartupNewProjectDialog((Frame) getParent(), settings);
        startupNewProjectDialog.setVisible(true);

        StartupNewProjectDialog.NewProjectDialogResult newProjectDialogResult = startupNewProjectDialog.getResult();
        if (newProjectDialogResult != null) {
            result = new StartupDialogResult();
            result.projectFile = newProjectDialogResult.projectFile;
            result.mapFile = newProjectDialogResult.mapFile;
            result.createNewProject = true;

            // Add to recent projects
            Settings.RecentProjectInfo recentProjectInfo = new Settings.RecentProjectInfo();
            String projectName = result.projectFile.getParentFile().getName();
            recentProjectInfo.setName(projectName);
            recentProjectInfo.setDir(result.projectFile.getParent());
            settings.addRecentProjectInfo(recentProjectInfo);

            saveSettings(); // Save settings after adding new project to recent
            dispose();
        }
    }

    // Open project by selecting it from the file chooser
    private void openProject(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Theme Editor Project (*.project)", "project"));
        fileChooser.setDialogTitle("Open Project");

        int fileChooserResult = fileChooser.showOpenDialog(this);
        if (fileChooserResult == JFileChooser.APPROVE_OPTION) {
            result = new StartupDialogResult();
            result.projectFile = fileChooser.getSelectedFile();

            // Add to recent projects
            Settings.RecentProjectInfo recentProjectInfo = new Settings.RecentProjectInfo();
            String projectName = result.projectFile.getParentFile().getName();
            recentProjectInfo.setName(projectName);
            recentProjectInfo.setDir(result.projectFile.getParent());
            settings.addRecentProjectInfo(recentProjectInfo);

            saveSettings();
            dispose();
        }
    }

    private void openTheme(ActionEvent e) {
        StartupLoadThemeDialog loadThemeDialog = new StartupLoadThemeDialog((Frame) getParent(), settings);
        loadThemeDialog.setVisible(true);

        StartupLoadThemeDialog.LoadThemeDialogResult dialogResult = loadThemeDialog.getResult();

        if (dialogResult != null) {
            result = new StartupDialogResult();
            result.themeFile = dialogResult.themeFile;
            result.projectFile = new File(result.themeFile.getParentFile(), "editor.project");
            result.mapFile = dialogResult.mapFile;

            // Add to recent projects
            Settings.RecentProjectInfo recentProjectInfo = new Settings.RecentProjectInfo();
            String projectName = result.projectFile.getParentFile().getName();
            recentProjectInfo.setName(projectName);
            recentProjectInfo.setDir(result.projectFile.getParent());
            settings.addRecentProjectInfo(recentProjectInfo);

            saveSettings();
            dispose();
        }
    }

    // Open project after selecting one from the list of recent projects
    private void openSelectedProject(Settings.RecentProjectInfo project) {
        String projectPath = project.getDir() + File.separator + PROJECT_FILENAME;
        File projectFile = new File(projectPath);

        if (projectFile.exists()) {
            result = new StartupDialogResult();
            result.projectFile = projectFile;

            // Update the project in recent list (bumps it to top)
            settings.addRecentProjectInfo(project);
            saveSettings();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Project file not found: " + projectPath,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            // Remove from recent projects since it doesn't exist
            settings.getRecentProjects().remove(project);
            saveSettings();
            refreshProjectsList();
        }
    }

    private void loadSettings() {
        try {
            File settingsFile = new File(SETTINGS_FILENAME);
            if (settingsFile.exists()) {
                settings = Settings.load(settingsFile);
            } else {
                settings = new Settings();
            }
        } catch (Exception e) {
            System.err.println("Error loading settings: " + e.getMessage());
            settings = new Settings();
        }
        refreshProjectsList();
    }

    private void saveSettings() {
        try {
            File settingsFile = new File(SETTINGS_FILENAME);
            settings.save(settingsFile);
        } catch (Exception e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    private void refreshProjectsList() {
        recentProjectListModel.clear();
        List<Settings.RecentProjectInfo> recentProjects = settings.getRecentProjects();
        for (Settings.RecentProjectInfo project : recentProjects) {
            recentProjectListModel.addElement(project);
        }
    }

    public StartupDialogResult getResult() {
        return result;
    }

    private static class ProjectListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Settings.RecentProjectInfo project) {
                setText("<html><b>"+project.getName() + "</b><br>" + project.getDir() + "</html>");
            }

            return this;
        }
    }
}