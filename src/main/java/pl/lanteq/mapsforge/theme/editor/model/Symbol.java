package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "symbol", namespace = "http://mapsforge.org/renderTheme")
public class Symbol extends Instruction {
    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "cat")
    private String category;

    @XmlAttribute(name = "display")
    private Display display = Display.IFSPACE;

    @XmlAttribute(name = "priority")
    private Integer priority = 0;

    @XmlAttribute(name = "src")
    private String src;

    @XmlAttribute(name = "symbol-width")
    private Integer symbolWidth;

    @XmlAttribute(name = "symbol-height")
    private Integer symbolHeight;

    @XmlAttribute(name = "symbol-percent")
    private Integer symbolPercent;

    @XmlAttribute(name = "position")
    private Position position = Position.CENTER;

    // ===== Getters & Setters =====
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSrc() { return src; }
    public void setSrc(String src) { this.src = src; }

    public Integer getSymbolWidth() { return symbolWidth; }
    public void setSymbolWidth(Integer symbolWidth) { this.symbolWidth = symbolWidth; }

    public Integer getSymbolHeight() { return symbolHeight; }
    public void setSymbolHeight(Integer symbolHeight) { this.symbolHeight = symbolHeight; }

    public Integer getSymbolPercent() { return symbolPercent; }
    public void setSymbolPercent(Integer symbolPercent) { this.symbolPercent = symbolPercent; }
}
