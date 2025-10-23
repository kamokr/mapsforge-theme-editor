package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum Element {
    @XmlEnumValue("node") NODE,
    @XmlEnumValue("way") WAY,
    @XmlEnumValue("any") ANY
}