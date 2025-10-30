package kamokr.mapsforge.theme.editor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class StartupNewProjectDialog extends JDialog {
    private static final String PROJECT_FILENAME = "editor.project";

    private Settings settings;
    private JTextField projectNameField;
    private JTextField mapFileField;
    private JTextField dirField;
    private JButton createButton;
    private JButton cancelButton;
    private JButton browseDirButton;
    private JButton browseMapButton;

    public class NewProjectDialogResult {
        public File projectFile = null;
        public File mapFile = null;
    }

    private NewProjectDialogResult result = null;

    public StartupNewProjectDialog(Frame parent, Settings settings) {
        super(parent, "Create New Project", true);
        this.settings = settings;
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(500, 260);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Project Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Project name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Project Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        projectNameField = new JTextField(20);
        formPanel.add(projectNameField, gbc);

        // Directory
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Project directory:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JPanel dirPanel = new JPanel(new BorderLayout(5, 0));
        dirField = new JTextField();
        dirField.setEditable(false);
        dirPanel.add(dirField, BorderLayout.CENTER);

        browseDirButton = new JButton("Browse");
        browseDirButton.addActionListener(this::browseDir);
        dirPanel.add(browseDirButton, BorderLayout.EAST);

        formPanel.add(dirPanel, gbc);

        // Map file
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Map File:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JPanel mapPanel = new JPanel(new BorderLayout(5, 0));
        mapFileField = new JTextField();
        mapFileField.setEditable(false);
        mapPanel.add(mapFileField, BorderLayout.CENTER);

        browseMapButton = new JButton("Browse");
        browseMapButton.addActionListener(this::browseMapFile);
        mapPanel.add(browseMapButton, BorderLayout.EAST);

        formPanel.add(mapPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        createButton = new JButton("Create");
        createButton.addActionListener(this::createProject);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Enable/disable create button based on input
        projectNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCreateButton(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCreateButton(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCreateButton(); }
        });

        updateCreateButton();
    }

    private void browseDir(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Map File");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            dirField.setText(selectedFile.getAbsolutePath());
            updateCreateButton();
        }
    }

    private void browseMapFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Mapsforge Map Files (*.map)", "map"));
        fileChooser.setDialogTitle("Select Map File");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            mapFileField.setText(selectedFile.getAbsolutePath());
            updateCreateButton();
        }
    }

    private void createProject(ActionEvent e) {
        String projectName = projectNameField.getText().trim();
        String mapFilePath = mapFileField.getText().trim();

        if (projectName.isEmpty() || mapFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate project name (should be a valid directory name)
        if (!isValidProjectName(projectName)) {
            JOptionPane.showMessageDialog(this,
                    "Project name contains invalid characters",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if map file exists
        File mapFile = new File(mapFilePath);
        if (!mapFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Selected map file does not exist",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create project directory
        File projectsDir = new File(settings.getProjectsDir());
        File projectDir = new File(projectsDir, projectName);

        if (projectDir.exists()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Project directory already exists. Overwrite?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION);

            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            if (!projectDir.mkdirs()) {
                JOptionPane.showMessageDialog(this,
                        "Could not create project directory: " + projectDir.getAbsolutePath(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        result = new NewProjectDialogResult();
        result.projectFile = new File(projectDir, PROJECT_FILENAME);
        result.mapFile = mapFile;

        dispose();
    }

    /**
     * Creates a new project with basic theme structure
     *
     * @param projectXmlFile The file where the project XML will be saved
     * @param mapFile The Mapsforge map file to use in editor
     * @throws IOException If there's an error creating the project files
     */
    public static void createEmptyProject(File projectXmlFile, File mapFile) throws IOException {

    }

    private boolean isValidProjectName(String name) {
        // Basic validation - should not contain path separators or other invalid characters
        return name != null &&
                !name.isEmpty() &&
                !name.contains("/") &&
                !name.contains("\\") &&
                !name.contains(":") &&
                !name.contains("*") &&
                !name.contains("?") &&
                !name.contains("\"") &&
                !name.contains("<") &&
                !name.contains(">") &&
                !name.contains("|");
    }

    private void updateCreateButton() {
        boolean enabled = !projectNameField.getText().trim().isEmpty() &&
                !mapFileField.getText().trim().isEmpty();
        createButton.setEnabled(enabled);
    }

    public NewProjectDialogResult getResult() {
        return result;
    }
}