package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.*;

import java.text.MessageFormat;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "area", namespace = "http://mapsforge.org/renderTheme")
public class Area extends Instruction {
    @XmlAttribute(name = "cat")
    private String category;

    @XmlAttribute(name = "src")
    private String src;

    @XmlAttribute(name = "symbol-width")
    private Integer symbolWidth;

    @XmlAttribute(name = "symbol-height")
    private Integer symbolHeight;

    @XmlAttribute(name = "symbol-percent")
    private Integer symbolPercent;

    @XmlAttribute(name = "fill")
    private String fill = "#000000"; // color

    @XmlAttribute(name = "scale")
    private Scale scale = Scale.STROKE;

    @XmlAttribute(name = "stroke")
    private String stroke = "#00000000"; // transparent default

    @XmlAttribute(name = "stroke-width")
    private Float strokeWidth = 0f;

    @XmlAttribute(name = "text-transform")
    private TextTransform textTransform = TextTransform.NONE;

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

    public String getFill() { return fill; }
    public void setFill(String fill) { this.fill = fill; }

    public Scale getScale() { return scale; }
    public void setScale(Scale scale) { this.scale = scale; }

    public String getStroke() { return stroke; }
    public void setStroke(String stroke) { this.stroke = stroke; }

    public Float getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(Float strokeWidth) { this.strokeWidth = strokeWidth; }

    public TextTransform getTextTransform() { return textTransform; }
    public void setTextTransform(TextTransform textTransform) { this.textTransform = textTransform; }

    public String toString() {
        String content = MessageFormat.format(
                "<b>area</b> fill={0}, stroke={1}, stroke-width={2}",
                fill,
                stroke,
                strokeWidth);

        return super.formatString(content);
    }
}
