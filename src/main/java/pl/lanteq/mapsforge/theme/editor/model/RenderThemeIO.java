package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.*;
import org.eclipse.persistence.jaxb.MarshallerProperties;

import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Logger;

public class RenderThemeIO {
    private static final Logger logger = Logger.getLogger(RenderThemeIO.class.getName());

    public static RenderTheme loadProject(File file) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(RenderTheme.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        RenderTheme theme = (RenderTheme) unmarshaller.unmarshal(file);

        logger.info(MessageFormat.format("Parsed theme - version: {0}, rules count: {1}",
                theme.getVersion(),
                theme.getRules() != null ? theme.getRules().size() : "null"));

        return theme;
    }

    public static void saveProject(RenderTheme theme, File file) throws Exception {
        save(theme, file, true);
    }

    public static void saveTheme(RenderTheme theme, File file) throws Exception {
        save(theme, file, false);
    }

    private static void save(RenderTheme theme, File file, boolean asProject) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(RenderTheme.class);
        Marshaller marshaller = ctx.createMarshaller();

        RenderTheme.setMarshallingAsProject(asProject);
        Rule.setMarshallingAsProject(asProject);

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(theme, file);
    }
}
