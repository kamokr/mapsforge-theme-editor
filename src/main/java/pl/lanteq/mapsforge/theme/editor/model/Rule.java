package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.FIELD)
public class Rule extends MarshalledObject{
    @XmlAttribute(name = "e")
    private Element element = Element.ANY;

    @XmlAttribute(name = "k")
    private String key = "*";

    @XmlAttribute(name = "v")
    private String value = "*";

    @XmlAttribute(name = "cat")
    private String category;

    @XmlAttribute(name = "closed")
    private Closed closed;

    @XmlAttribute(name = "zoom-min")
    private Integer zoomMin;

    @XmlAttribute(name = "zoom-max")
    private Integer zoomMax;

//    public void setEnabledRules(List<Rule> rules) {
//        this.rules = rules;
//    }

    @XmlTransient
    private List<MarshalledObject> children = new ArrayList<MarshalledObject>();

    @XmlElements({
            @XmlElement(name = "rule", type = Rule.class),
            @XmlElement(name = "line", type = Line.class),
            @XmlElement(name = "area", type = Area.class),
            @XmlElement(name = "symbol", type = Symbol.class),
            @XmlElement(name = "caption", type = Caption.class)
    })
    public List<MarshalledObject> getEnabledChildren() {
        if (children == null) return null;
        List<MarshalledObject> tmp = new ArrayList<>();
        for(MarshalledObject o : this.children) {
            if (o instanceof Rule r) {
                if (r.isEnabled() || getMarshallingAsProject()) {
                    tmp.add(r);
                }
            } else if (o instanceof Instruction i) {
                if (i.isEnabled() || getMarshallingAsProject()) {
                    tmp.add(i);
                }
            }
        }
        return tmp;
    }
    public void setEnabledChildren(List<MarshalledObject> c) {
        this.children = new ArrayList<>(c);
    }


    public Element getElement() { return this.element; }
    public void setElement(Element element) { this.element = element; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Closed getClosed() { return closed; }
    public void setClosed(Closed closed) { this.closed = closed; }

    public Integer getZoomMin() { return zoomMin; }
    public void setZoomMin(Integer zoomMin) { this.zoomMin = zoomMin; }

    public Integer getZoomMax() { return zoomMax; }
    public void setZoomMax(Integer zoomMax) { this.zoomMax = zoomMax; }

    public List<MarshalledObject> getChildren() { return children; }
    public void clearChildren() { this.children.clear(); }
    public void addChild(MarshalledObject child) { this.children.add(child); }
    public void removeChild(MarshalledObject child) { this.children.remove(child); }

    public String toString() {
        String content = MessageFormat.format("<b>{0}</b> {1} = {2}",
                element.toString().toLowerCase(), key.replace("|", " | "), value.replace("|", " | "));

        return super.formatString(content);
    }
}
