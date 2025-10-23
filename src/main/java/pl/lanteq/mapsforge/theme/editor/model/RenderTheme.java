package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "rendertheme")
@XmlAccessorType(XmlAccessType.FIELD)
public class RenderTheme extends MarshalledObject {
    @XmlTransient
    private String mapFilename = "";
    @XmlAttribute(name = "editor-map-file")
    public String getMarshallMapFilename() {
        return getMarshallingAsProject() ? mapFilename : null;
    }
    public void setMarshallMapFilename(String mapFilename) {
        this.mapFilename = mapFilename;
    }
    public void setMapFilename(String mapFilename) {
        this.mapFilename = mapFilename;
    }
    public String getMapFilename() {
        return mapFilename;
    }

    @XmlAttribute
    private String version = "5";

    @XmlAttribute(name = "map-background")
    private String mapBackground = "#F8F8F8";

    @XmlAttribute(name = "map-background-outside")
    private String mapBackgroundOutside = "#E0E0E0";

    @XmlAttribute(name = "base-stroke-width")
    private Float baseStrokeWidth;

    @XmlAttribute(name = "base-text-size")
    private Float baseTextSize;

    @XmlTransient
    private List<Rule> rules = new ArrayList<Rule>();
    @XmlElement(name = "rule")
    public List<Rule> getEnabledRules() {
        if (rules == null) return null;
        List<Rule> tmp = new ArrayList<Rule>();
        for(Rule r : this.rules) {
            if (r.isEnabled() || getMarshallingAsProject()) {
                tmp.add(r);
            }
        }
        return tmp;
    }

    public void setEnabledRules(List<Rule> rules) {
        this.rules = rules;
    }

    // Getters and setters
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getMapBackground() { return mapBackground; }
    public void setMapBackground(String mapBackground) { this.mapBackground = mapBackground; }

    public String getMapBackgroundOutside() { return mapBackgroundOutside; }
    public void setMapBackgroundOutside(String mapBackgroundOutside) { this.mapBackgroundOutside = mapBackgroundOutside; }

    public Float getBaseStrokeWidth() { return baseStrokeWidth; }
    public void setBaseStrokeWidth(Float baseStrokeWidth) { this.baseStrokeWidth = baseStrokeWidth; }

    public Float getBaseTextSize() { return baseTextSize; }
    public void setBaseTextSize(Float baseTextSize) { this.baseTextSize = baseTextSize; }

    public List<Rule> getRules() { return rules; }
    public void setRules(List<Rule> rules) { this.rules = rules; }

    public void addRule(Rule rule) { this.rules.add(rule); }
    public void removeRule(Rule rule) { this.rules.remove(rule); }

    public String toString() {
        return formatString("<b>rendertheme</b>");
    }

    public void saveProject(File file) throws Exception {
        save(file, true);
    }

    public void saveTheme(File file) throws Exception {
        save(file, false);
    }

    private void save(File file, boolean asProject) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(RenderTheme.class);
        Marshaller marshaller = ctx.createMarshaller();

        RenderTheme.setMarshallingAsProject(asProject);
        Rule.setMarshallingAsProject(asProject);

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(this, file);
    }
}