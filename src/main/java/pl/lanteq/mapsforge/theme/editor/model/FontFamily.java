package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum FontFamily {
    @XmlEnumValue("default") DEFAULT,
    @XmlEnumValue("serif") SERIF,
    @XmlEnumValue("sans_serif") SANS_SERIF,
    @XmlEnumValue("monospace") MONOSPACE
}
