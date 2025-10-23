package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum Closed {
    @XmlEnumValue("yes") YES,
    @XmlEnumValue("no") NO,
    @XmlEnumValue("any") ANY
}