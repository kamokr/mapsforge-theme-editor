package kamokr.mapsforge.theme.editor;

import org.apache.ws.commons.schema.*;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

public class Model {
    private static final Logger logger = Logger.getLogger(Model.class.getName());

    private static final String XS_NS = "http://www.w3.org/2001/XMLSchema";
    private Map<String, ElementMeta> schema = new HashMap<>();
    private ElementBinding root;

    private File projectXmlFile;
    private File themeXmlFile;
    private Document document;

    static class SimpleTypeMeta {
        String base;
        String pattern;
        List<String> enumerations = new ArrayList<>();
    }

    public static class ElementMeta {
        String name;
        Map<String, AttributeMeta> attributes = new LinkedHashMap<>();
        Map<String, AttributeMeta> choices = new LinkedHashMap<>();
        public ElementMeta(String name) { this.name = name; }
    }

    public static class AttributeMeta {
        String name;
        String type;             // e.g. xs:string, xs:decimal
        String pattern;          // from <xs:pattern>
        String defaultValue;
        List<String> enumerations  = new ArrayList<>(); // from <xs:enumeration>
        boolean required;
    }

    public static class AttributeBinding {
        public final Element element;
        public final AttributeMeta meta;
        private String value; //

        public AttributeBinding(Element element, AttributeMeta meta) {
            if (element == null || meta == null) {
                throw new IllegalArgumentException("Element and meta cannot be null");
            }
            this.element = element;
            this.meta = meta;
            this.value = element.getAttributeValue(meta.name);
        }

        public void setValue(String newValue) {
            String standardizedValue = (newValue != null && newValue.isBlank()) ? null : newValue;
            this.value = standardizedValue;
            if (meta.defaultValue != null && meta.defaultValue.equals(standardizedValue)) {
                element.removeAttribute(meta.name); // omit redundant default
            } else if (standardizedValue == null) {
                element.removeAttribute(meta.name);
            } else {
                element.setAttribute(meta.name, standardizedValue);
            }
        }

        public String getValue() {
            return value;
        }
    }

    public static class ElementBinding {
        private final Map<String, ElementMeta> schema;
        private final Element element;
        private final ElementBinding parent;
        private final ElementMeta meta;
        private final Map<String, AttributeBinding> attributes = new LinkedHashMap<>();
        private final List<ElementBinding> children = new LinkedList<>();

        public ElementBinding(ElementBinding parent, Element element, Map<String, ElementMeta> schema) {
            this.parent = parent;
            this.element = element;
            this.schema = schema;
            this.meta = schema.get(element.getName());

            if(this.meta == null) {
                logger.severe("Element " + element.getName() + " not found in schema");
                return;
            }

            meta.attributes.forEach((name, attrMeta) -> {
                String value = element.getAttributeValue(name);
                attributes.put(name, new AttributeBinding(element, attrMeta));
            });

            element.getChildren().forEach((child) -> {
                children.add(new ElementBinding(this, child, schema));
            });
        }

        public Element getElement() {
            return element;
        }

        public String getName() {
            return element.getName();
        }

        public String getValue(String name) {
            if(!attributes.containsKey(name)) return null;
            return attributes.get(name).getValue();
        }

        public void setValue(String name, String newValue) {
            attributes.get(name).setValue(newValue);
        }

        public Map<String, AttributeBinding> getAttributes() {
            return attributes;
        }

        public List<ElementBinding> getChildren() {
            return children;
        }

        public ElementBinding addChild(String complexTypeName) {
            Element childElement = new Element(complexTypeName, element.getNamespace());
            element.addContent(childElement);
            ElementBinding b = new ElementBinding(this, childElement, schema);
            children.add(b);
            return b;
        }

        public void remove() {
            element.detach();
            parent.children.remove(this);
        }
    }

    public Model() {}

    public ElementBinding getRoot() {
        return root;
    }

    public ElementMeta getElementMeta(String name) {
        return schema.get(name);
    }

    public void loadSchema(File xsdFile) throws IOException {
        Map<String, SimpleTypeMeta> simpleTypes = new HashMap<>();

        XmlSchemaCollection collection = new XmlSchemaCollection();
        XmlSchema x;

        try (InputStream inputStream = new FileInputStream(xsdFile)) {
            x = collection.read(new InputSource(inputStream));
        }

        // --- Phase 1: collect all simple types ---
        for (XmlSchemaObject obj : x.getSchemaTypes().values()) {
            if (obj instanceof XmlSchemaSimpleType st && st.getName() != null) {
                SimpleTypeMeta meta = new SimpleTypeMeta();
                if (st.getContent() instanceof XmlSchemaSimpleTypeRestriction restriction) {
                    meta.base = restriction.getBaseTypeName().getLocalPart();
                    for (XmlSchemaFacet facet : restriction.getFacets()) {
                        if (facet instanceof XmlSchemaPatternFacet pf) {
                            meta.pattern = pf.getValue().toString();
                        } else if (facet instanceof XmlSchemaEnumerationFacet ef) {
                            meta.enumerations.add(ef.getValue().toString());
                        }
                    }
                }
                simpleTypes.put(st.getName(), meta);
            }
        }

        // --- Phase 2: collect complex types ---
        for (XmlSchemaObject obj : x.getSchemaTypes().values()) {
            if (obj instanceof XmlSchemaComplexType ct && ct.getName() != null) {
                ElementMeta elementMeta = new ElementMeta(ct.getName());

                // Collect direct attributes of the complex type
                for (XmlSchemaAttributeOrGroupRef ref : ct.getAttributes()) {
                    if (ref instanceof XmlSchemaAttribute a) {
                        AttributeMeta attrMeta = new AttributeMeta();
                        attrMeta.name = a.getName();

                        // Resolve type reference
                        if (a.getSchemaTypeName() != null)
                            attrMeta.type = a.getSchemaTypeName().getLocalPart();

                        attrMeta.defaultValue = a.getDefaultValue();
                        attrMeta.required = a.getUse() == XmlSchemaUse.REQUIRED;

                        // If this attribute type refers to a known simple type
                        if (attrMeta.type != null && simpleTypes.containsKey(attrMeta.type)) {
                            SimpleTypeMeta s = simpleTypes.get(attrMeta.type);
                            attrMeta.pattern = s.pattern;
                            attrMeta.enumerations.addAll(s.enumerations);

//                            // add empty choice to automatically assign default value
//                            if(!attrMeta.required && !attrMeta.enumerations.isEmpty())
//                                attrMeta.enumerations.add(0, "");
                        }

                        elementMeta.attributes.put(a.getName(), attrMeta);
                    }
                }

                // Collect choices
                XmlSchemaParticle particle = ct.getParticle();
                if(particle instanceof XmlSchemaChoice choice) {
                    for (XmlSchemaChoiceMember item : choice.getItems()) {
                        if (item instanceof XmlSchemaElement element) {
                            AttributeMeta meta = new AttributeMeta();
                            meta.name = element.getName();
                            meta.type = element.getSchemaTypeName().getLocalPart();
                            elementMeta.choices.put(meta.name, meta);
                        }
                    }
                }

                schema.put(ct.getName(), elementMeta);
            }
        }
    }

    public void createProject(File xmlFile, File mapFile) {
        this.projectXmlFile = xmlFile;
        this.themeXmlFile = new File(xmlFile.getParentFile(), "theme.xml");

        Element rootElement = new Element("rendertheme");
        rootElement.setAttribute("version", "5");
        rootElement.setAttribute("editor-map", mapFile.getAbsolutePath());

        this.document = new Document(rootElement);
        this.root = new ElementBinding(null, this.document.getRootElement(), schema);
    }

    public void loadProject(File xmlFile) throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();
        this.document = builder.build(xmlFile);
        this.projectXmlFile = xmlFile;
        this.themeXmlFile = new File(xmlFile.getParentFile(), "theme.xml");
        this.root = new ElementBinding(null, this.document.getRootElement(), schema);
    }

    public void loadTheme(File themeXmlFile, File mapFile) throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();
        this.document = builder.build(themeXmlFile);

        Element rootElement = this.document.getRootElement();
        rootElement.setAttribute("editor-map", mapFile.getAbsolutePath());

        this.projectXmlFile = new File(themeXmlFile.getParentFile(), "editor.project");
        this.themeXmlFile = new File(themeXmlFile.getParentFile(), "theme.xml");;
        this.root = new ElementBinding(null, this.document.getRootElement(), schema);
    }

    public File getThemeXmlFile() {
        return themeXmlFile;
    }

    public File getMapFile() {
        String mapFilePath = this.document.getRootElement().getAttributeValue("editor-map");
        return new File(mapFilePath);
    }

    public void saveProject() throws IOException {
        saveXml(document, projectXmlFile);
    }

    public void saveTheme() throws IOException {
        Document themeXmlDocument = createThemeFromProject(document);
        saveXml(themeXmlDocument, themeXmlFile);
    }

    public void rebuild(DefaultTreeModel treeModel) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();

        if (rootNode == null) {
            return;
        }

        rebuildElementFromNode(rootNode);
        this.root = new ElementBinding(null, this.document.getRootElement(), schema);
    }

    private Element rebuildElementFromNode(DefaultMutableTreeNode node) {
        ElementBinding eb = (ElementBinding) node.getUserObject();
        Element element = eb.element;
        element.removeContent();

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            Element childElement = rebuildElementFromNode(childNode);
            element.addContent(childElement);
        }
        return element;
    }

    /**
     * Creates a theme document from the project XML document
     *
     * @param projectXmlDocument The project XML document object used as the basis for generating the theme
     * @return A processed theme document object that is a clone of the original document with elements processed
     */
    private Document createThemeFromProject(Document projectXmlDocument) {
        Document processed = projectXmlDocument.clone();
        processElement(processed.getRootElement());
        return processed;
    }

    private void processElement(Element element) {
        // remove editor-specific attributes
        element.removeAttribute("editor-enabled");
        element.removeAttribute("editor-comment");
        element.removeAttribute("editor-map");

        // remove attributes not present in schema or if they have default values
        List<Attribute> attrsToRemove = new ArrayList<>();
        for (Attribute attr : element.getAttributes()) {
            ElementMeta elementMeta = schema.get(element.getName());
            if (elementMeta == null) continue;

            // remove attributes not present in schema
            AttributeMeta attrMeta = elementMeta.attributes.get(attr.getName());
            if (attrMeta == null) attrsToRemove.add(attr);

            // remove attribute if it is optional and has default value
            if (attrMeta != null && !attrMeta.required && attrMeta.defaultValue != null) {
                if (attr.getValue().equals(attrMeta.defaultValue)) {
                    attrsToRemove.add(attr);
                }
            }
        }
        for (Attribute attr : attrsToRemove) element.removeAttribute(attr);

        // skip saving disabled elements
        List<Element> toRemove = new ArrayList<>();
        for (Element child : element.getChildren()) {
            if ("false".equals(child.getAttributeValue("editor-enabled", "true"))) {
                toRemove.add(child);
            }
        }
        for (Element child : toRemove) child.detach();

        // process children
        for (Element child : element.getChildren()) processElement(child);
    }

    /**
     * Saves an XML document to the specified file
     *
     * @param xmlDocument The XML document object to be saved
     * @param xmlFile The target XML file
     * @throws IOException Thrown when an IO error occurs during the file saving process
     */
    private static void saveXml(Document xmlDocument, File xmlFile) throws IOException {
        java.io.FileOutputStream fos = new java.io.FileOutputStream(xmlFile);
        org.jdom2.output.XMLOutputter outputter = new org.jdom2.output.XMLOutputter();
        outputter.setFormat(org.jdom2.output.Format.getPrettyFormat());
        outputter.output(xmlDocument, fos);
    }
}
