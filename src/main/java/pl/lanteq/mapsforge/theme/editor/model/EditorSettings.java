package pl.lanteq.mapsforge.theme.editor.model;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "settings")
@XmlAccessorType(XmlAccessType.FIELD)
public class EditorSettings {
    @XmlAttribute(name = "projects_dir")
    private String projectsDir = System.getProperty("user.home") + File.separator + "MapsforgeThemeEditorProjects";
    public String getProjectsDir() {
        return projectsDir;
    }
    public void setProjectsDir(String projectsDir) {
        this.projectsDir = projectsDir;
    }

    @XmlElement(name = "project")
    private List<EditorProject> recentProjects = new ArrayList<>();

    public List<EditorProject> getRecentProjects() {
        return recentProjects;
    }

    public void addRecentProject(EditorProject project) {
        // Remove if already exists
        recentProjects.removeIf(p -> p.getDir().equals(project.getDir()) && p.getName().equals(project.getName()));
        // Add to the beginning
        recentProjects.add(0, project);
        // Limit to last 10 projects
        if (recentProjects.size() > 10) {
            recentProjects = recentProjects.subList(0, 10);
        }
    }

    public static EditorSettings load(File file) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(EditorSettings.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        return (EditorSettings) unmarshaller.unmarshal(file);
    }

    public void save(File file) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(EditorSettings.class);
        jakarta.xml.bind.Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(this, file);
    }
}




