package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ Line.class, Area.class, Symbol.class, Caption.class })
public abstract class Instruction extends MarshalledObject {
}