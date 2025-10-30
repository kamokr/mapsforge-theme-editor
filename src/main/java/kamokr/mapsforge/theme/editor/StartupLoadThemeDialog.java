package kamokr.mapsforge.theme.editor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class StartupLoadThemeDialog extends JDialog {
    private static final String PROJECT_FILENAME = "editor.project";

    private Settings settings;
    private JTextField mapFileField;
    private JTextField themeFileField;
    private JButton loadButton;
    private JButton cancelButton;
    private JButton browseThemeButton;
    private JButton browseMapButton;

    public class LoadThemeDialogResult {
        public File themeFile = null;
        public File mapFile = null;
    }

    private LoadThemeDialogResult result = null;

    public StartupLoadThemeDialog(Frame parent, Settings settings) {
        super(parent, "Load Theme", true);
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

        // Directory
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Theme file:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JPanel themePanel = new JPanel(new BorderLayout(5, 0));
        themeFileField = new JTextField();
        themeFileField.setEditable(false);
        themePanel.add(themeFileField, BorderLayout.CENTER);

        browseThemeButton = new JButton("Browse");
        browseThemeButton.addActionListener(this::browseTheme);
        themePanel.add(browseThemeButton, BorderLayout.EAST);

        formPanel.add(themePanel, gbc);

        // Map file
        gbc.gridx = 0;
        gbc.gridy = 1;
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
        loadButton = new JButton("Load");
        loadButton.addActionListener(this::loadTheme);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(loadButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        updateCreateButton();
    }

    private void browseTheme(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("RenderTheme XML file (*.xml)", "xml"));
        fileChooser.setDialogTitle("Open theme");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            themeFileField.setText(selectedFile.getAbsolutePath());
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

    private void loadTheme(ActionEvent e) {
        String themeFilePath = themeFileField.getText().trim();
        String mapFilePath = mapFileField.getText().trim();

        if (themeFilePath.isEmpty() || mapFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if theme file exists
        File themeFile = new File(themeFilePath);
        if (!themeFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Selected theme file does not exist",
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


        result = new LoadThemeDialogResult();
        result.themeFile = themeFile;
        result.mapFile = mapFile;

        dispose();
    }

    private void updateCreateButton() {
        boolean enabled = !themeFileField.getText().trim().isEmpty() &&
                !mapFileField.getText().trim().isEmpty();
        loadButton.setEnabled(enabled);
    }

    public LoadThemeDialogResult getResult() {
        return result;
    }
}