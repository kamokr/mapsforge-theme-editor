package pl.lanteq.mapsforge.theme.editor.map;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Tag;

import java.util.List;

public class MapObject {
    private String type;
    private List<Tag> tags;
    private LatLong position;

    public MapObject(String type, List<Tag> tags, LatLong position) {
        this.type = type;
        this.tags = tags;
        this.position = position;
    }

    public String getType() { return type; }
    public List<Tag> getTags() { return tags; }
    public LatLong getPosition() { return position; }
}