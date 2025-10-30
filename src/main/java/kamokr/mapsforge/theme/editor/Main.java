package kamokr.mapsforge.theme.editor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

public class Main extends JFrame implements Editor.EditorChangeListener {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static Editor editor;
    private MapView mapView;
    private MapDataPanel mapDataPanel;
    private JSplitPane splitPane;
    private JSplitPane mapSplitPane;
    private static final File SCHEMA_FILE = Util.getResourceAsFile("renderTheme.editor.xsd");

    public Main() {
        setTitle("Mapsforge Theme Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setGlobalFontSize(13f);

        try {
            editor = new Editor(SCHEMA_FILE);
        } catch (IOException e) {
            logger.severe("Error loading schema: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error loading schema: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }


        mapView = new MapView();
        mapDataPanel = new MapDataPanel();
        mapView.setMapDataPanel(mapDataPanel);

        mapSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                mapView, mapDataPanel);
        mapSplitPane.setResizeWeight(0.8);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                editor, mapSplitPane);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.25);

        add(splitPane, BorderLayout.CENTER);

        editor.addChangeListener(this);

        if (!showStartupDialog()) {
            System.exit(0); // User closed the dialog without selecting anything
        }
    }

    private void applyTheme() {
        mapView.applyTheme(editor.getThemeFile());
    }

    private static void setGlobalFontSize(float size) {
        UIManager.getLookAndFeelDefaults().keySet().stream()
                .filter(key -> key.toString().toLowerCase().contains("font"))
                .forEach(key -> {
                    Font font = UIManager.getFont(key);
                    if (font != null) {
                        UIManager.put(key, font.deriveFont(size));
                    }
                });
    }

    private boolean showStartupDialog() {
        StartupDialog startupDialog = new StartupDialog(this);
        startupDialog.setVisible(true);

        StartupDialog.StartupDialogResult result = startupDialog.getResult();
        if (result == null) return false;

        if (result.createNewProject) {
            try {
                editor.createProject(result.projectFile, result.mapFile);
            } catch (IOException e) {
                logger.severe("Error creating project: " + e.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Error creating project: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        try {
            editor.loadProject(result.projectFile);
            mapView.openMap(editor.getMapFile());
            applyTheme();
            return true;

        } catch (Exception e) {
            logger.severe("Error loading project: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error loading project: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            new Main().setVisible(true);
        });
    }

    @Override
    public void onEditorChange() {
        applyTheme();
    }
}
