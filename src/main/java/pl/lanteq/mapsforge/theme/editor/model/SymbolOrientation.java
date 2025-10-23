package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum SymbolOrientation {
    @XmlEnumValue("auto") AUTO,
    @XmlEnumValue("auto_down") AUTO_DOWN,
    @XmlEnumValue("right") RIGHT,
    @XmlEnumValue("left") LEFT,
    @XmlEnumValue("up") UP,
    @XmlEnumValue("down") DOWN
}

