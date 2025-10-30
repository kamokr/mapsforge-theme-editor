package kamokr.mapsforge.theme.editor;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EditorAttributesPanel extends JPanel {
    private Model.ElementBinding element;
    private Map<String, JComponent> inputs = new HashMap<>();
    private final JButton saveButton;
    private final JButton revertButton;
    private final JScrollPane scrollPane;
    private final JPanel contentPanel;

    private List<ChangeListener> listeners = new ArrayList<>();

    public EditorAttributesPanel() {
        // initialize UI
        setLayout(new BorderLayout());
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        revertButton = new JButton("Revert");
        saveButton = new JButton("Save");
        revertButton.addActionListener(e -> revertChanges());
        saveButton.addActionListener(e -> applyChanges());
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

        setButtonsEnabled(false);
    }

    public Model.ElementBinding getElement() {
        return element;
    }

    private void setButtonsEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
        revertButton.setEnabled(enabled);
    }

    private void revertChanges() {
        if (element != null) setElement(element);
    }

    private void applyChanges() {
        if (element == null) return;

        for(Model.AttributeBinding b : element.getAttributes().values()) {
            JComponent input = inputs.get(b.meta.name);
            String value = null;
            if (input instanceof JTextField text) {
                value = text.getText();
            } else if (input instanceof JCheckBox checkbox) {
                value = String.valueOf(checkbox.isSelected());
            } else if (input instanceof JSpinner spinner) {
                value = String.valueOf(spinner.getValue());
            } else if (input instanceof JComboBox<?> combo) {
                value = combo.getSelectedItem() == null ? null : combo.getSelectedItem().toString();
            }
            if(value != null)
                b.setValue(value);
        }

        notifyChanged();
    }

    public void setElement(Model.ElementBinding element) {
        this.element = element;
        contentPanel.removeAll();
        inputs.clear();

        for (Model.AttributeBinding b : element.getAttributes().values()) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            JLabel label = new JLabel(b.meta.name);
            label.setPreferredSize(new Dimension(200, 25));

            JComponent input = buildInput(b);
            inputs.put(b.meta.name, input);

            panel.add(label, BorderLayout.WEST);
            panel.add(input);

//            addListener(input, name);

            contentPanel.add(panel, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1,
                    1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                    new Insets(2, 2, 2, 2), 0, 0));
        }

        revalidate();
        repaint();
        setButtonsEnabled(true);
    }

    private JComponent buildInput(Model.AttributeBinding b) {
        String value = b.getValue();
        if(value == null)
            value = "";

        if(!b.meta.enumerations.isEmpty()) {
            JComboBox<String> combo = new JComboBox<>(b.meta.enumerations.toArray(new String[0]));
            combo.setSelectedItem(value);
            return combo;
        }

        switch (b.meta.type) {
            case "boolean" -> {
                JCheckBox box = new JCheckBox();
                box.setSelected(value.isEmpty() ? Boolean.parseBoolean(b.meta.defaultValue) : Boolean.parseBoolean(value));
                return box;
            }
            case "integer", "int" -> {
                return new JSpinner(new SpinnerNumberModel(Integer.parseInt(value.isEmpty() ? "0" : value), null, null, 1));
            }
            case "positiveInteger" -> {
                return new JSpinner(new SpinnerNumberModel(Integer.parseInt(value.isEmpty() ? "0" : value), 0, null, 1));
            }
            case "unsignedByte" -> {
                return new JSpinner(new SpinnerNumberModel(Integer.parseInt(value.isEmpty() ? Objects.requireNonNull(b.meta.defaultValue) : value), 0, 127, 1));
            }
            default -> {
                return new JTextField(value, 20);
            }
        }
    }

//    private void addListener(JComponent input, String attrName) {
//        if (input instanceof JTextField field) {
//            field.addActionListener(e -> element.setAttribute(attrName, field.getText()));
//        } else if (input instanceof JCheckBox box) {
//            box.addActionListener(e -> element.setAttribute(attrName, Boolean.toString(box.isSelected())));
//        } else if (input instanceof JSpinner spinner) {
//            spinner.addChangeListener(e -> element.setAttribute(attrName, spinner.getValue().toString()));
//        }
//    }

    // Change listener =================================================================================================
    public interface ChangeListener {
        void onChanged();
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyChanged() {
        for(ChangeListener listener : listeners) {
            listener.onChanged();
        }
    }
}

