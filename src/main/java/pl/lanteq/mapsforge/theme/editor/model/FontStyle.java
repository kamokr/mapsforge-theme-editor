package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum FontStyle {
    @XmlEnumValue("bold") BOLD,
    @XmlEnumValue("bold_italic") BOLD_ITALIC,
    @XmlEnumValue("italic") ITALIC,
    @XmlEnumValue("normal") NORMAL
}
