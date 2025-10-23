package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum Cap {
    @XmlEnumValue("butt") BUTT,
    @XmlEnumValue("round") ROUND,
    @XmlEnumValue("square") SQUARE
}