package pl.lanteq.mapsforge.theme.editor.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RegexTextField extends JTextField {
    private String regexPattern;
    private Color validColor = Color.WHITE;
    private Color invalidColor = new Color(255, 200, 200); // Light red
    private boolean isValid = true;
    private String tooltipMessage = "Invalid format";

    public RegexTextField(String text, String regex) {
        this(text,20, regex);
    }

    public RegexTextField(int columns, String regex) {
        super(columns);
        this.regexPattern = regex;
        setupKeyListener();
    }

    public RegexTextField(String text, int columns, String regex) {
        super(text, columns);
        this.regexPattern = regex;
        setupKeyListener();
        validateText(); // Validate initial text
    }

    private void setupKeyListener() {
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validateText();
            }
        });
    }

    private void validateText() {
        String text = getText();

        // Allow empty field to be considered valid (change if needed)
        if (text.isEmpty()) {
            setValid(true);
            return;
        }

        if (regexPattern != null && !regexPattern.isEmpty()) {
            boolean matches = text.matches(regexPattern);
            setValid(matches);
        } else {
            setValid(true); // No regex pattern means always valid
        }
    }

    private void setValid(boolean valid) {
        this.isValid = valid;
        if (valid) {
            setBackground(validColor);
            setToolTipText(null);
        } else {
            setBackground(invalidColor);
            setToolTipText(tooltipMessage);
        }
    }

    // Public methods
    public boolean isValidInput() {
        return isValid;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public void setRegexPattern(String regexPattern) {
        this.regexPattern = regexPattern;
        validateText(); // Re-validate with new pattern
    }

    public Color getValidColor() {
        return validColor;
    }

    public void setValidColor(Color validColor) {
        this.validColor = validColor;
        if (isValid) {
            setBackground(validColor);
        }
    }

    public Color getInvalidColor() {
        return invalidColor;
    }

    public void setInvalidColor(Color invalidColor) {
        this.invalidColor = invalidColor;
        if (!isValid) {
            setBackground(invalidColor);
        }
    }

    public String getTooltipMessage() {
        return tooltipMessage;
    }

    public void setTooltipMessage(String tooltipMessage) {
        this.tooltipMessage = tooltipMessage;
        if (!isValid) {
            setToolTipText(tooltipMessage);
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        validateText(); // Validate when text is set programmatically
    }
}
