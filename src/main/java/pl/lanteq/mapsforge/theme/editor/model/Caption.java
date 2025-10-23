package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.*;

import java.text.MessageFormat;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "caption", namespace = "http://mapsforge.org/renderTheme")
public class Caption extends Instruction {

    @XmlAttribute(name = "cat")
    private String category;

    @XmlAttribute(name = "priority")
    private Integer priority = 0;

    @XmlAttribute(name = "k", required = true)
    private String key;

    @XmlAttribute(name = "display")
    private Display display = Display.IFSPACE;

    @XmlAttribute(name = "dy")
    private Float dy = 0f;

    @XmlAttribute(name = "font-family")
    private FontFamily fontFamily = FontFamily.DEFAULT;

    @XmlAttribute(name = "font-style")
    private FontStyle fontStyle = FontStyle.NORMAL;

    @XmlAttribute(name = "font-size")
    private Float fontSize = 0f;

    @XmlAttribute(name = "fill")
    private String fill = "#000000";

    @XmlAttribute(name = "stroke")
    private String stroke = "#000000";

    @XmlAttribute(name = "stroke-width")
    private Float strokeWidth = 0f;

    @XmlAttribute(name = "position")
    private Position position = Position.AUTO;

    @XmlAttribute(name = "symbol-id")
    private String symbolId;

    @XmlAttribute(name = "text-transform")
    private TextTransform textTransform = TextTransform.NONE;

    @XmlAttribute(name = "text-wrap-width")
    private Integer textWrapWidth = 0;

    // ===== Getters & Setters =====
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public Display getDisplay() { return display; }
    public void setDisplay(Display display) { this.display = display; }

    public Float getDy() { return dy; }
    public void setDy(Float dy) { this.dy = dy; }

    public FontFamily getFontFamily() { return fontFamily; }
    public void setFontFamily(FontFamily fontFamily) { this.fontFamily = fontFamily; }

    public FontStyle getFontStyle() { return fontStyle; }
    public void setFontStyle(FontStyle fontStyle) { this.fontStyle = fontStyle; }

    public Float getFontSize() { return fontSize; }
    public void setFontSize(Float fontSize) { this.fontSize = fontSize; }

    public String getFill() { return fill; }
    public void setFill(String fill) { this.fill = fill; }

    public String getStroke() { return stroke; }
    public void setStroke(String stroke) { this.stroke = stroke; }

    public Float getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(Float strokeWidth) { this.strokeWidth = strokeWidth; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public String getSymbolId() { return symbolId; }
    public void setSymbolId(String symbolId) { this.symbolId = symbolId; }

    public TextTransform getTextTransform() { return textTransform; }
    public void setTextTransform(TextTransform textTransform) { this.textTransform = textTransform; }

    public Integer getTextWrapWidth() { return textWrapWidth; }
    public void setTextWrapWidth(Integer textWrapWidth) { this.textWrapWidth = textWrapWidth; }

    public String toString() {
        return MessageFormat.format("caption (font-family={0})", fontFamily);
    }
}
