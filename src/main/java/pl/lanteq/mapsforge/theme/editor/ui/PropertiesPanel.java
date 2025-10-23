package pl.lanteq.mapsforge.theme.editor.ui;

import pl.lanteq.mapsforge.theme.editor.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertiesPanel extends JPanel {
    private final Map<String, JComponent> inputFields;
    private Object selectedObject;
    private final JButton saveButton;
    private final JButton revertButton;
    private final JScrollPane scrollPane;
    private final JPanel contentPanel;
    private List<PropertiesPanel.PropertyChangeListener> listeners = new ArrayList<>();

    public PropertiesPanel() {
        setLayout(new BorderLayout());
        inputFields = new HashMap<>();

        // Create content panel for input fields
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        revertButton = new JButton("Revert");
        saveButton = new JButton("Save");

        revertButton.addActionListener(e -> revertChanges());
        saveButton.addActionListener(e -> saveChanges());

        buttonPanel.add(revertButton);
        buttonPanel.add(saveButton);

        // Add scroll pane for input fields
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(contentPanel, BorderLayout.NORTH);
        scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initially disable buttons
        setButtonsEnabled(false);
    }

    public void updateContent(Object selected) {
        selectedObject = selected;
        clearInputFields();

        if (selected instanceof RenderTheme) {
            createRenderThemeInputs((RenderTheme) selected);
        } else if (selected instanceof Rule) {
            createRuleInputs((Rule) selected);
        } else if (selected instanceof Area) {
            createAreaInputs((Area) selected);
        } else if (selected instanceof Line) {
            createLineInputs((Line) selected);
        }
        // Add more conditions for other types

        revalidate();
        repaint();
        setButtonsEnabled(true);
    }

    private void addCheckboxField(String key, String label, boolean value) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setPreferredSize(new Dimension(200, 25));
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(value);

        fieldPanel.add(fieldLabel, BorderLayout.WEST);
        fieldPanel.add(checkBox, BorderLayout.CENTER);

        inputFields.put(key, checkBox);
        contentPanel.add(fieldPanel, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    }

    private void addInputField(String key, String label, String value) {
        addInputField(key, label, value, null);
    }

    private void addInputField(String key, String label, String value, String regex) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setPreferredSize(new Dimension(200, 25));
        fieldPanel.add(fieldLabel, BorderLayout.WEST);

        if(regex == null) {
            JTextField field = new JTextField(value);
            field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            fieldPanel.add(field, BorderLayout.CENTER);
            inputFields.put(key, field);

        } else {
            RegexTextField field = new RegexTextField(value, regex);
            field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            fieldPanel.add(field, BorderLayout.CENTER);
            inputFields.put(key, field);

        }

        contentPanel.add(fieldPanel, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    }

    private void addSelectField(String key, String label, String[] options, String selectedOption) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setPreferredSize(new Dimension(200, 25));
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setSelectedItem(selectedOption);
        comboBox.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        fieldPanel.add(fieldLabel, BorderLayout.WEST);
        fieldPanel.add(comboBox, BorderLayout.CENTER);

        inputFields.put(key, comboBox);
        contentPanel.add(fieldPanel, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    }

    private void addSeparator() {
        contentPanel.add(new JSeparator(), new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 5, 2), 0, 0));
    }

    private void createRenderThemeInputs(RenderTheme theme) {
        addInputField("version", "Version", theme.getVersion());
        addInputField("map-background", "Map Background", theme.getMapBackground());
        addInputField("map-background-outside", "Map Background Outside", theme.getMapBackgroundOutside());
        addInputField("base-stroke-width", "Base Stroke Width", theme.getBaseStrokeWidth() != null ? theme.getBaseStrokeWidth().toString() : "");
        addInputField("base-text-size", "Base Text Size", theme.getBaseTextSize() != null ? theme.getBaseTextSize().toString() : "");
    }

    private void applyRenderThemeInputs(RenderTheme theme) {
        theme.setVersion(((JTextField) inputFields.get("version")).getText().strip());

        theme.setMapBackground(((JTextField) inputFields.get("map-background")).getText().strip());

        theme.setMapBackgroundOutside(((JTextField) inputFields.get("map-background-outside")).getText().strip());

        String baseStrokeWidthValue = ((JTextField) inputFields.get("base-stroke-width")).getText().strip();
        theme.setBaseStrokeWidth(baseStrokeWidthValue.isEmpty() ? null : Float.parseFloat(baseStrokeWidthValue));

        String baseTextSizeValue = ((JTextField) inputFields.get("base-text-size")).getText().strip();
        theme.setBaseTextSize(baseTextSizeValue.isEmpty() ? null : Float.parseFloat(baseTextSizeValue));
    }

    private void createRuleInputs(Rule rule) {
        addCheckboxField("enabled", "Enabled", rule.isEnabled());
        addSelectField(
                "element",
                "Element",
                new String[]{
                        String.valueOf(Element.NODE).toLowerCase(),
                        String.valueOf(Element.WAY).toLowerCase(),
                        String.valueOf(Element.ANY).toLowerCase()},
                rule.getElement().toString().toLowerCase());
        addInputField("key", "Key", rule.getKey());
        addInputField("value", "Value", rule.getValue());
        addInputField("category", "Category", rule.getCategory());
        addSelectField(
                "closed",
                "Closed (default: any)",
                new String[]{
                        "",
                        String.valueOf(Closed.YES).toLowerCase(),
                        String.valueOf(Closed.NO).toLowerCase(),
                        String.valueOf(Closed.ANY).toLowerCase()},
                rule.getClosed() != null ? rule.getClosed().toString().toLowerCase() : "");

        addInputField("zoom-min", "Zoom Min (0-127, default: 0)", rule.getZoomMin() != null ? rule.getZoomMin().toString() : "", "^(12[0-7]|1[01][0-9]|[1-9][0-9]?|[0-9])$");
        addInputField("zoom-max", "Zoom Max (0-127, default: 127)", rule.getZoomMax() != null ? rule.getZoomMax().toString() : "", "^(12[0-7]|1[01][0-9]|[1-9][0-9]?|[0-9])$");
    }

    private void applyRuleInputs(Rule rule) {
        rule.setEnabled(((JCheckBox) inputFields.get("enabled")).isSelected());

        String elementValue = (String) ((JComboBox<?>) inputFields.get("element")).getSelectedItem();
        rule.setElement(Element.valueOf(elementValue.toUpperCase()));

        rule.setKey(((JTextField) inputFields.get("key")).getText().strip());

        rule.setValue(((JTextField) inputFields.get("value")).getText().strip());

        String categoryValue = ((JTextField) inputFields.get("category")).getText().strip();
        rule.setCategory(categoryValue.isEmpty() ? null : categoryValue);

        String closedValue = (String) ((JComboBox<?>) inputFields.get("closed")).getSelectedItem();
        rule.setClosed(closedValue.isEmpty() ? null : Closed.valueOf(closedValue.toUpperCase()));

        String zoomMinValue = ((RegexTextField) inputFields.get("zoom-min")).getText().strip();
        rule.setZoomMin(zoomMinValue.isEmpty() ? null : Integer.parseInt(zoomMinValue));

        String zoomMaxValue = ((JTextField) inputFields.get("zoom-max")).getText().strip();
        rule.setZoomMax(zoomMaxValue.isEmpty() ? null : Integer.parseInt(zoomMaxValue));
    }

    private void createAreaInputs(Area area) {
        addCheckboxField("enabled", "Enabled", area.isEnabled());
        addInputField("category", "Category", area.getCategory());
        addInputField("src", "Src", area.getSrc());
        addInputField("symbol-width", "Symbol width", area.getSymbolWidth() != null ? area.getSymbolWidth().toString() : "");
        addInputField("symbol-height", "Symbol height", area.getSymbolHeight() != null ? area.getSymbolHeight().toString() : "");
        addInputField("symbol-percent", "Symbol percent", area.getSymbolPercent() != null ? area.getSymbolPercent().toString() : "");
        addInputField("fill", "Fill", area.getFill());
        addSelectField(
                "scale",
                "Scale",
                new String[]{
                        "",
                        String.valueOf(Scale.ALL).toLowerCase(),
                        String.valueOf(Scale.NONE).toLowerCase(),
                        String.valueOf(Scale.STROKE).toLowerCase()},
                area.getScale() != null ? area.getScale().toString().toLowerCase() : "");

        addInputField("stroke", "Stroke", area.getStroke());
        addInputField("stroke-width", "Stroke width", area.getStrokeWidth() != null ? area.getStrokeWidth().toString() : "");

        addSelectField(
                "text-transform",
                "Text transform (default: none)",
                new String[]{
                        "",
                        String.valueOf(TextTransform.NONE).toLowerCase(),
                        String.valueOf(TextTransform.UPPERCASE).toLowerCase(),
                        String.valueOf(TextTransform.LOWERCASE).toLowerCase(),
                        String.valueOf(TextTransform.CAPITALIZE).toLowerCase()},
                area.getTextTransform() != null ? area.getTextTransform().toString().toLowerCase() : ""
        );
    }

    private void applyAreaInputs(Area area) {
        area.setEnabled(((JCheckBox) inputFields.get("enabled")).isSelected());

        area.setCategory(((JTextField) inputFields.get("category")).getText());

        String srcValue = ((JTextField) inputFields.get("src")).getText().strip();
        area.setSrc(srcValue.isEmpty() ? null : srcValue);

        String symbolWidthValue = ((JTextField) inputFields.get("symbol-width")).getText();
        area.setSymbolWidth(symbolWidthValue.isEmpty() ? null : Integer.parseInt(symbolWidthValue));

        String symbolHeightValue = ((JTextField) inputFields.get("symbol-height")).getText();
        area.setSymbolHeight(symbolHeightValue.isEmpty() ? null : Integer.parseInt(symbolHeightValue));

        String symbolPercentValue = ((JTextField) inputFields.get("symbol-percent")).getText();
        area.setSymbolPercent(symbolPercentValue.isEmpty() ? null : Integer.parseInt(symbolPercentValue));

        area.setFill(((JTextField) inputFields.get("fill")).getText());

        String scaleValue = (String) ((JComboBox<?>) inputFields.get("scale")).getSelectedItem();
        area.setScale(scaleValue.isEmpty() ? null : Scale.valueOf(scaleValue.toUpperCase()));

        area.setStroke(((JTextField) inputFields.get("stroke")).getText());

        String strokeWidthValue = ((JTextField) inputFields.get("stroke-width")).getText();
        area.setStrokeWidth(strokeWidthValue.isEmpty() ? null : Float.parseFloat(strokeWidthValue));

        String textTransformValue = (String) ((JComboBox<?>) inputFields.get("text-transform")).getSelectedItem();
        area.setTextTransform(textTransformValue.isEmpty() ? null : TextTransform.valueOf(textTransformValue.toUpperCase()));
    }

    private void createLineInputs(Line line) {
        addCheckboxField("enabled", "Enabled", line.isEnabled());
        addInputField("category", "Category", line.getCategory());
        addInputField("src", "Src", line.getSrc());
        addInputField("symbol-width", "Symbol width", line.getSymbolWidth() != null ? line.getSymbolWidth().toString() : "");
        addInputField("symbol-height", "Symbol height", line.getSymbolHeight() != null ? line.getSymbolHeight().toString() : "");
        addInputField("symbol-percent", "Symbol percent", line.getSymbolPercent() != null ? line.getSymbolPercent().toString() : "");
        addInputField("dy", "Dy", line.getDy() != null ? line.getDy().toString() : "");
        addSelectField(
                "scale",
                "Scale (default: stroke)",
                new String[]{
                        "",
                        String.valueOf(Scale.ALL).toLowerCase(),
                        String.valueOf(Scale.NONE).toLowerCase(),
                        String.valueOf(Scale.STROKE).toLowerCase()},
                line.getScale() != null ? line.getScale().toString().toLowerCase() : "");
        addInputField("stroke", "Stroke (default: #000000)", line.getStroke(), "^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$");
        addInputField("stroke-width", "Stroke width (default: 0)", line.getStrokeWidth() != null ? line.getStrokeWidth().toString() : "");
        addInputField("stroke-dasharray", "Stroke dasharray", line.getStrokeDasharray() != null ? line.getStrokeDasharray() : "");
        addSelectField(
                "stroke-linecap",
                "Stroke linecap (default: round)",
                new String[]{
                        "",
                        String.valueOf(Cap.BUTT).toLowerCase(),
                        String.valueOf(Cap.ROUND).toLowerCase(),
                        String.valueOf(Cap.SQUARE).toLowerCase()},
                line.getStrokeLinecap() != null ? line.getStrokeLinecap().toString().toLowerCase() : "");
        addSelectField(
                "stroke-linejoin",
                "Stroke linejoin (default: round)",
                new String[]{
                        "",
                        String.valueOf(LineJoin.MITER).toLowerCase(),
                        String.valueOf(LineJoin.ROUND).toLowerCase(),
                        String.valueOf(LineJoin.BEVEL).toLowerCase()},
                line.getStrokeLinejoin() != null ? line.getStrokeLinejoin().toString().toLowerCase() : "");
        addSelectField(
                "curve",
                "Curve (default: no)",
                new String[]{
                        "",
                        String.valueOf(Curve.NO).toLowerCase(),
                        String.valueOf(Curve.CUBIC).toLowerCase()},
                line.getCurve() != null ? line.getCurve().toString().toLowerCase() : "");
    }

    private void applyLineInputs(Line line) {
        line.setEnabled(((JCheckBox) inputFields.get("enabled")).isSelected());

        line.setCategory(((JTextField) inputFields.get("category")).getText().strip());

        String srcValue = ((JTextField) inputFields.get("src")).getText().strip();
        line.setSrc(srcValue.isEmpty() ? null : srcValue);

        String symbolWidthValue = ((JTextField) inputFields.get("symbol-width")).getText().strip();
        line.setSymbolWidth(symbolWidthValue.isEmpty() ? null : Integer.parseInt(symbolWidthValue));

        String symbolHeightValue = ((JTextField) inputFields.get("symbol-height")).getText().strip();
        line.setSymbolHeight(symbolHeightValue.isEmpty() ? null : Integer.parseInt(symbolHeightValue));

        String symbolPercentValue = ((JTextField) inputFields.get("symbol-percent")).getText().strip();
        line.setSymbolPercent(symbolPercentValue.isEmpty() ? null : Integer.parseInt(symbolPercentValue));

        String dyValue = ((JTextField) inputFields.get("dy")).getText().strip();
        line.setDy(dyValue.isEmpty() ? null : Float.parseFloat(dyValue));

        String scaleValue = (String) ((JComboBox<?>) inputFields.get("scale")).getSelectedItem();
        line.setScale(scaleValue.isEmpty() ? null : Scale.valueOf(scaleValue.toUpperCase()));

        line.setStroke(((JTextField) inputFields.get("stroke")).getText());

        String strokeWidthValue = ((JTextField) inputFields.get("stroke-width")).getText().strip();
        line.setStrokeWidth(strokeWidthValue.isEmpty() ? null : Float.parseFloat(strokeWidthValue));

        String strokeDasharrayValue = ((JTextField) inputFields.get("stroke-dasharray")).getText().strip();
        line.setStrokeDasharray(strokeDasharrayValue.isEmpty() ? null : strokeDasharrayValue);

        String strokeLinecapValue = (String) ((JComboBox<?>) inputFields.get("stroke-linecap")).getSelectedItem();
        line.setStrokeLinecap(strokeLinecapValue.isEmpty() ? null : Cap.valueOf(strokeLinecapValue.toUpperCase()));

        String strokeLinejoinValue = (String) ((JComboBox<?>) inputFields.get("stroke-linejoin")).getSelectedItem();
        line.setStrokeLinejoin(strokeLinejoinValue.isEmpty() ? null : LineJoin.valueOf(strokeLinejoinValue.toUpperCase()));

        String curveValue = (String) ((JComboBox<?>) inputFields.get("curve")).getSelectedItem();
        line.setCurve(curveValue.isEmpty() ? null : Curve.valueOf(curveValue.toUpperCase()));
    }

    private void clearInputFields() {
        inputFields.clear();
        contentPanel.removeAll();
    }

    private void setButtonsEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
        revertButton.setEnabled(enabled);
    }

    private void revertChanges() {
        if (selectedObject != null) {
            updateContent(selectedObject);
        }
    }

    private void saveChanges() {
        if (selectedObject == null) {
            return;
        }

        if(selectedObject instanceof RenderTheme) {
            applyRenderThemeInputs((RenderTheme) selectedObject);
        } else if (selectedObject instanceof Rule) {
            applyRuleInputs((Rule) selectedObject);
        } else if (selectedObject instanceof Area) {
            applyAreaInputs((Area) selectedObject);
        } else if (selectedObject instanceof Line) {
            applyLineInputs((Line) selectedObject);
        }

        notifyPropertyChanged(selectedObject);
    }



    public void addPropertyChangeListener(PropertiesPanel.PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public interface PropertyChangeListener {
        void onPropertyChanged(Object updatedObject);
    }

    private void notifyPropertyChanged(Object updatedObject) {
        for (PropertiesPanel.PropertyChangeListener listener : listeners) {
            listener.onPropertyChanged(updatedObject);
        }
    }
}
