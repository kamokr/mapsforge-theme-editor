package kamokr.mapsforge.theme.editor;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "settings")
@XmlAccessorType(XmlAccessType.FIELD)
public class Settings {
    @XmlAttribute(name = "projects_dir")
    private String projectsDir = System.getProperty("user.home") + File.separator + "MapsforgeThemeEditorProjects";
    public String getProjectsDir() {
        return projectsDir;
    }
    public void setProjectsDir(String projectsDir) {
        this.projectsDir = projectsDir;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RecentProjectInfo {
        @XmlAttribute(name = "name")
        private String name;

        @XmlAttribute(name = "dir")
        private String dir;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }
    }

    @XmlElement(name = "project")
    private List<RecentProjectInfo> recentProjects = new ArrayList<>();

    public List<RecentProjectInfo> getRecentProjects() {
        return recentProjects;
    }

    public void addRecentProjectInfo(RecentProjectInfo project) {
        // Remove if already exists
        recentProjects.removeIf(p -> p.getDir().equals(project.getDir()) && p.getName().equals(project.getName()));
        // Add to the beginning
        recentProjects.add(0, project);
        // Limit to last 10 projects
        if (recentProjects.size() > 10) {
            recentProjects = recentProjects.subList(0, 10);
        }
    }

    public static Settings load(File file) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(Settings.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        return (Settings) unmarshaller.unmarshal(file);
    }

    public void save(File file) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(Settings.class);
        jakarta.xml.bind.Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(this, file);
    }
}
