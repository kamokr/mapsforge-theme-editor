package pl.lanteq.mapsforge.theme.editor.ui;

import javax.swing.*;

public class MapDataPanel extends JPanel {
    private final JTextArea mapDataTextArea;
    public MapDataPanel() {
        // create text area to display map data information
        mapDataTextArea = new JTextArea();
        mapDataTextArea.setEditable(false);
        mapDataTextArea.setText("click on map to see map data information here");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JScrollPane(mapDataTextArea));
    }

    public void setText(String text) {
        // method to update the text area content
        mapDataTextArea.setText(text);
    }
}
