package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum TextTransform {
    @XmlEnumValue("none") NONE,
    @XmlEnumValue("uppercase") UPPERCASE,
    @XmlEnumValue("lowercase") LOWERCASE,
    @XmlEnumValue("capitalize") CAPITALIZE
}
