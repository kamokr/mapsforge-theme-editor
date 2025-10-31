package kamokr.mapsforge.theme.editor;

import javax.swing.*;
import java.awt.*;

public class MapDataPanel extends JPanel {
    private JList<String> mapDataList;
    private DefaultListModel<String> mapDataModel;

    public MapDataPanel() {
        mapDataModel = new DefaultListModel<>();
        mapDataList = new JList<>(mapDataModel);
        mapDataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mapDataList.setCellRenderer(new MapDataPanel.MapDataListRenderer());

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JScrollPane(mapDataList));

        mapDataModel.addElement("click on map to see<br>map data information here");
    }

    public void clear() {
        mapDataModel.clear();
    }

    public void addEntry(String entry) {
        // method to add a new entry to the list
        mapDataModel.addElement(entry);
    }

    private static class MapDataListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof String mapData) {
                setText("<html>" + mapData + "</html>");
            }

            return this;
        }
    }
}
