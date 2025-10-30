package kamokr.mapsforge.theme.editor;

import org.apache.ws.commons.schema.*;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;

public class Schema {
    private XmlSchema schema;
    private final Map<String, XmlSchemaComplexType> complexTypes = new HashMap<>();
    private final Map<String, XmlSchemaSimpleType> simpleTypes = new HashMap<>();

    public Schema() {

    }

    public void load(File xsdFile) throws FileNotFoundException, IOException {
        XmlSchemaCollection collection = new XmlSchemaCollection();
        try(InputStream inputStream = new FileInputStream(xsdFile)) {
            this.schema = collection.read(new InputSource(inputStream));
        }

        indexTypes();
    }

    private void indexTypes() {
        for (XmlSchemaObject obj : schema.getSchemaTypes().values()) {
            if (obj instanceof XmlSchemaComplexType ct && ct.getName() != null) {
                complexTypes.put(ct.getName(), ct);
            } else if (obj instanceof XmlSchemaSimpleType st && st.getName() != null) {
                simpleTypes.put(st.getName(), st);
            }
        }
    }

    public XmlSchemaComplexType getComplexType(String name) {
        return complexTypes.get(name);
    }

    public XmlSchemaSimpleType getSimpleType(String name) {
        return simpleTypes.get(name);
    }

    /** Get list of attributes for given complexType name. */
    public List<XmlSchemaAttribute> getAttributesForType(String typeName) {
        XmlSchemaComplexType ct = getComplexType(typeName);
        if (ct == null) return List.of();

        List<XmlSchemaAttribute> attrs = new ArrayList<>();
        for (XmlSchemaAttributeOrGroupRef ref : ct.getAttributes()) {
            if (ref instanceof XmlSchemaAttribute a) {
                attrs.add(a);
            }
        }
        return attrs;
    }

    public XmlSchemaAttribute getAttribute(String typeName, String attrName) {
        XmlSchemaComplexType ct = getComplexType(typeName);
        if (ct == null) return null;

        for (XmlSchemaAttributeOrGroupRef ref : ct.getAttributes()) {
            if (ref instanceof XmlSchemaAttribute a && a.getName().equals(attrName)) {
                return a;
            }
        }
        return null;
    }

    public List<String> getEnumValues(XmlSchemaAttribute attr) {
        QName typeName = attr.getSchemaTypeName();
        XmlSchemaSimpleType simpleType = null;

        if (typeName != null) {
            simpleType = getSimpleType(typeName.getLocalPart());
        }

        if (simpleType == null) return null;

        if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction restriction) {
            List<String> values = new ArrayList<>();
            for (XmlSchemaFacet facet : restriction.getFacets()) {
                if (facet instanceof XmlSchemaEnumerationFacet ef) {
                    values.add(ef.getValue().toString());
                }
            }
            if(values.isEmpty())
                return null;

            return values;
        }
        return null;
    }

    public String getPattern(XmlSchemaAttribute attr) {
        QName typeName = attr.getSchemaTypeName();
        XmlSchemaSimpleType simpleType = null;

        if (typeName != null) {
            simpleType = getSimpleType(typeName.getLocalPart());
        }

        if (simpleType == null) return null;

        if (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction restriction) {
            for (XmlSchemaFacet facet : restriction.getFacets()) {
                if (facet instanceof XmlSchemaPatternFacet pf) {
                    return pf.getValue().toString();
                }
            }
        }
        return null;
    }
}

