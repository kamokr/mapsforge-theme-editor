package pl.lanteq.mapsforge.theme.editor.map;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Rotation;
import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.datastore.MapReadResult;
import org.mapsforge.map.datastore.PointOfInterest;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.datastore.Way;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCaptureTileRendererLayer extends TileRendererLayer {
    private int tileSize;
    private final HashMap<Long, List<MapObject>> tileObjects;

    public DataCaptureTileRendererLayer(int tileSize, TileCache tileCache, MapDataStore mapDataStore,
                                        MapViewPosition mapViewPosition, GraphicFactory graphicFactory) {
        super(tileCache, mapDataStore, mapViewPosition, false, true, true, graphicFactory);
        tileSize = tileSize;
        this.tileObjects = new HashMap<>();
    }

    @Override
    public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint, Rotation rotation) {
        super.draw(boundingBox, zoomLevel, canvas, topLeftPoint, rotation);


        Tile tile = new Tile((int) (MercatorProjection.longitudeToTileX(boundingBox.minLongitude, zoomLevel)),
                (int) (MercatorProjection.latitudeToTileY(boundingBox.minLatitude, zoomLevel)),
                zoomLevel, tileSize);

        MapReadResult mapData = getMapDataStore().readMapData(tile);
        List<MapObject> objects = new ArrayList<>();


        for (PointOfInterest poi : mapData.pois) {
            objects.add(new MapObject("node", poi.tags,
                    new LatLong(poi.position.latitude, poi.position.longitude)));
        }

        for (Way way : mapData.ways) {
            objects.add(new MapObject("way", way.tags,
                    new LatLong(way.labelPosition.latitude, way.labelPosition.longitude)));
        }

        tileObjects.put((long) tile.hashCode(), objects);
    }

    public List<Object> getObjectsAt(LatLong position, byte zoomLevel) {
        List<Object> result = new ArrayList<>();
        Tile tile = new Tile((int) (MercatorProjection.longitudeToTileX(position.longitude, zoomLevel)),
                (int) (MercatorProjection.latitudeToTileY(position.latitude, zoomLevel)),
                zoomLevel, tileSize);

        List<MapObject> features = tileObjects.get(tile.hashCode());
        if (features != null) {
            for (Object feature : features) {
                LatLong featurePosition = getFeaturePosition(feature);
                if (featurePosition != null && calculateDistance(position, featurePosition) <= 10) {
                    result.add(feature);
                }
            }
        }
        return result;
    }

    private LatLong getFeaturePosition(Object feature) {
        if (feature instanceof Way) {
            return ((Way) feature).labelPosition;
        } else if (feature instanceof PointOfInterest) {
            return ((PointOfInterest) feature).position;
        }
        return null;
    }

    private double calculateDistance(LatLong p1, LatLong p2) {
        double R = 6371000;
        double lat1 = Math.toRadians(p1.latitude);
        double lat2 = Math.toRadians(p2.latitude);
        double dLat = Math.toRadians(p2.latitude - p1.latitude);
        double dLon = Math.toRadians(p2.longitude - p1.longitude);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }
}
