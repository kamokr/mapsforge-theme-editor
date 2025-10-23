package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum Position {
    @XmlEnumValue("auto") AUTO,
    @XmlEnumValue("center") CENTER,
    @XmlEnumValue("below") BELOW,
    @XmlEnumValue("below_left") BELOW_LEFT,
    @XmlEnumValue("below_right") BELOW_RIGHT,
    @XmlEnumValue("above") ABOVE,
    @XmlEnumValue("above_left") ABOVE_LEFT,
    @XmlEnumValue("above_right") ABOVE_RIGHT,
    @XmlEnumValue("left") LEFT,
    @XmlEnumValue("right") RIGHT
}
