package pl.lanteq.mapsforge.theme.editor.ui;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

public class MainFrame extends JFrame implements ThemeEditorPanel.ThemeChangeListener {
    private static final Logger logger = Logger.getLogger(MainFrame.class.getName());

    private ThemeEditorPanel themeEditorPanel;
    private MapPreviewPanel mapPreviewPanel;
    private MapDataPanel mapDataPanel;
    private JSplitPane splitPane;
    private JSplitPane mapSplitPane;


    public MainFrame() {
        setGlobalFontSize(13f);
        initializeUI();

        if (!showStartupDialog()) {
            System.exit(0); // User closed the dialog without selecting anything
        }

        applyTheme();
    }

    private boolean showStartupDialog() {
        StartupDialog startupDialog = new StartupDialog(this);
        startupDialog.setVisible(true);

        if (startupDialog.getSelectedProjectPath() != null) {
            // Load existing project
            return loadProject(startupDialog.getSelectedProjectPath());
        }

        return false; // Dialog was closed without selection
    }

    private boolean loadProject(String projectPath) {
        try {
            themeEditorPanel.loadProject(projectPath);
            themeEditorPanel.saveTheme();

            String mapPath = themeEditorPanel.getProject().getMapFilename();

            try {
                mapPreviewPanel.openMap(mapPath);
                return true;
            } catch (FileNotFoundException e) {
                logger.severe("Map file not found: " + e.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Map file not found: " + mapPath,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            logger.severe("Error loading project: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error loading project: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private void applyTheme() {
        mapPreviewPanel.applyTheme(themeEditorPanel.getThemePath());
    }

    private void initializeUI() {
        setTitle("Mapsforge Theme Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        themeEditorPanel = new ThemeEditorPanel();
        mapPreviewPanel = new MapPreviewPanel();
        mapDataPanel = new MapDataPanel();
        mapPreviewPanel.setMapDataPanel(mapDataPanel);

//        String projectPath = "/home/kamil/Projects/IdeaProjects/MapsforgeThemeEditorProjects/Motorider/project.xml";

//        themeEditorPanel.loadProject(projectPath);
//        themeEditorPanel.saveTheme();

        // get directory from project path
//        String projectDir = projectPath.substring(0, projectPath.lastIndexOf('/'));
//        String mapPath = projectDir + "/" + themeEditorPanel.getProject().getMapFilename();
//
//        try {
//            mapPreviewPanel.openMap(mapPath);
//        } catch (FileNotFoundException e) {
//            logger.severe(e.getMessage());
//        }

        mapSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                mapPreviewPanel, mapDataPanel);
        mapSplitPane.setResizeWeight(0.9);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                themeEditorPanel, mapSplitPane);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.25);

        add(splitPane, BorderLayout.CENTER);

        themeEditorPanel.addThemeChangeListener(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            new MainFrame().setVisible(true);
        });
    }

    public static void setGlobalFontSize(float size) {
        UIManager.getLookAndFeelDefaults().keySet().stream()
                .filter(key -> key.toString().toLowerCase().contains("font"))
                .forEach(key -> {
                    Font font = UIManager.getFont(key);
                    if (font != null) {
                        UIManager.put(key, font.deriveFont(size));
                    }
                });
    }

    @Override
    public void onThemeChanged() {
        applyTheme();
    }
}