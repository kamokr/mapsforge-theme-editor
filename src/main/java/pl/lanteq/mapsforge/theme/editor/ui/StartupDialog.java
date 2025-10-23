package pl.lanteq.mapsforge.theme.editor.ui;

import pl.lanteq.mapsforge.theme.editor.model.EditorProject;
import pl.lanteq.mapsforge.theme.editor.model.EditorSettings;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public class StartupDialog extends JDialog {
    private static final String SETTINGS_FILE = "mapsforge-theme-editor-settings.xml";
    private static final String PROJECT_FILE = "editor.project";

    private EditorSettings editorSettings;
    private JList<EditorProject> recentProjectsList;
    private DefaultListModel<EditorProject> listModel;
    private String selectedProjectPath = null;
    private boolean createNewProject = false;

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

        newProjectBtn.addActionListener(this::createNewProject);
        openProjectBtn.addActionListener(this::openProject);

        headerPanel.add(newProjectBtn);
        headerPanel.add(openProjectBtn);

        return headerPanel;
    }

    private JPanel createProjectsPanel() {
        JPanel projectsPanel = new JPanel(new BorderLayout());
        projectsPanel.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "Recent Projects"));

        listModel = new DefaultListModel<>();
        recentProjectsList = new JList<>(listModel);
        recentProjectsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentProjectsList.setCellRenderer(new ProjectListRenderer());

        // Double-click to open project
        recentProjectsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    EditorProject selected = recentProjectsList.getSelectedValue();
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
        NewProjectDialog newProjectDialog = new NewProjectDialog((Frame) getParent(), editorSettings);
        newProjectDialog.setVisible(true);

        if (newProjectDialog.getSelectedFile() != null) {
            File selectedFile = newProjectDialog.getSelectedFile();
            selectedProjectPath = selectedFile.getAbsolutePath();

            // Add to recent projects
            EditorProject project = new EditorProject();
            String projectName = selectedFile.getParentFile().getName();
            project.setName(projectName);
            project.setDir(selectedFile.getParent());

            editorSettings.addRecentProject(project);
            saveSettings(); // Save settings after adding new project to recent
            dispose();
        }
    }

    private void openProject(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Theme Editor Project (*.project)", "project"));
        fileChooser.setDialogTitle("Open Project");
        fileChooser.setCurrentDirectory(new File(editorSettings.getProjectsDir()));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedProjectPath = selectedFile.getAbsolutePath();

            // Add to recent projects
            EditorProject project = new EditorProject();
            String projectName = selectedFile.getParentFile().getName();
            project.setName(projectName);
            project.setDir(selectedFile.getParent());

            editorSettings.addRecentProject(project);
            saveSettings();

            dispose();
        }
    }

    private void openSelectedProject(EditorProject project) {
        String projectPath = project.getDir() + File.separator + PROJECT_FILE;
        File projectFile = new File(projectPath);

        if (projectFile.exists()) {
            selectedProjectPath = projectPath;

            // Update the project in recent list (bumps it to top)
            editorSettings.addRecentProject(project);
            saveSettings();

            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Project file not found: " + projectPath,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            // Remove from recent projects since it doesn't exist
            editorSettings.getRecentProjects().remove(project);
            saveSettings();
            refreshProjectsList();
        }
    }

    private void loadSettings() {
        try {
            File settingsFile = new File(SETTINGS_FILE);
            if (settingsFile.exists()) {
                editorSettings = EditorSettings.load(settingsFile);
            } else {
                editorSettings = new EditorSettings();
            }
        } catch (Exception e) {
            editorSettings = new EditorSettings();
            System.err.println("Error loading settings: " + e.getMessage());
        }
        refreshProjectsList();
    }

    private void saveSettings() {
        try {
            File settingsFile = new File(SETTINGS_FILE);
            editorSettings.save(settingsFile);
        } catch (Exception e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    private void refreshProjectsList() {
        listModel.clear();
        List<EditorProject> recentProjects = editorSettings.getRecentProjects();
        for (EditorProject project : recentProjects) {
            listModel.addElement(project);
        }
    }

    public String getSelectedProjectPath() {
        return selectedProjectPath;
    }

    private static class ProjectListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof EditorProject project) {
                setText("<html><b>"+project.getName() + "</b><br>" + project.getDir() + "</html>");
            }

            return this;
        }
    }
}