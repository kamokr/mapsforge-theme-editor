package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.*;

import java.text.MessageFormat;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "line", namespace = "http://mapsforge.org/renderTheme")
public class Line extends Instruction {

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

    @XmlAttribute(name = "dy")
    private Float dy = 0f;

    @XmlAttribute(name = "scale")
    private Scale scale = Scale.STROKE;

    @XmlAttribute(name = "stroke")
    private String stroke = "#000000"; // color type (#RRGGBB or #AARRGGBB)

    @XmlAttribute(name = "stroke-width")
    private Float strokeWidth = 0f;

    @XmlAttribute(name = "stroke-dasharray")
    private String strokeDasharray;

    @XmlAttribute(name = "stroke-linecap")
    private Cap strokeLinecap = Cap.ROUND;

    @XmlAttribute(name = "stroke-linejoin")
    private LineJoin strokeLinejoin = LineJoin.ROUND;

    @XmlAttribute(name = "curve")
    private Curve curve = Curve.NO;

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

    public Float getDy() { return dy; }
    public void setDy(Float dy) { this.dy = dy; }

    public Scale getScale() { return scale; }
    public void setScale(Scale scale) { this.scale = scale; }

    public String getStroke() { return stroke; }
    public void setStroke(String stroke) { this.stroke = stroke; }

    public Float getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(Float strokeWidth) { this.strokeWidth = strokeWidth; }

    public String getStrokeDasharray() { return strokeDasharray; }
    public void setStrokeDasharray(String strokeDasharray) { this.strokeDasharray = strokeDasharray; }

    public Cap getStrokeLinecap() { return strokeLinecap; }
    public void setStrokeLinecap(Cap strokeLinecap) { this.strokeLinecap = strokeLinecap; }

    public LineJoin getStrokeLinejoin() { return strokeLinejoin; }
    public void setStrokeLinejoin(LineJoin strokeLinejoin) { this.strokeLinejoin = strokeLinejoin; }

    public Curve getCurve() { return curve; }
    public void setCurve(Curve curve) { this.curve = curve; }

    public String toString() {
        String content = MessageFormat.format("<b>line</b> stroke={0}, stroke-width={1}", stroke, strokeWidth);
        return formatString(content);
    }
}
