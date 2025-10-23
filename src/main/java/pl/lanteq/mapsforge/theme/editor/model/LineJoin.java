package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum LineJoin {
    @XmlEnumValue("miter") MITER,
    @XmlEnumValue("round") ROUND,
    @XmlEnumValue("bevel") BEVEL
}
