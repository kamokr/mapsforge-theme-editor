package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;

public class MarshalledObject {
    @XmlTransient
    private boolean enabled = true;
    @XmlAttribute(name = "editor-enabled")
    public Boolean getEnabled() {
        return getMarshallingAsProject() ? enabled : null;
    }
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    @XmlTransient
    private static boolean marshalAsProject = false;
    public static void setMarshallingAsProject(boolean value) {
        marshalAsProject = value;
    }
    public static boolean getMarshallingAsProject() {
        return marshalAsProject;
    }

    public String formatString(String text) {
        if (enabled) {
            return "<html><tt>" + text + "</tt></html>";
        }
        return "<html><strike><tt>" + text + "</tt></strike></html>";
    }
}
