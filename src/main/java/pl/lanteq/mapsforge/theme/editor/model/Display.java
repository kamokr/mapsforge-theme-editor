package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum Display {
    @XmlEnumValue("never") NEVER,
    @XmlEnumValue("ifspace") IFSPACE,
    @XmlEnumValue("order") ORDER,
    @XmlEnumValue("always") ALWAYS
}

