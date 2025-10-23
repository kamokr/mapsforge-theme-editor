package pl.lanteq.mapsforge.theme.editor.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.awt.event.MouseEvent;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Rotation;
import org.mapsforge.core.model.Tag;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.awt.util.AwtUtil;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.datastore.MapReadResult;
import org.mapsforge.map.datastore.PointOfInterest;
import org.mapsforge.map.datastore.Way;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.*;
import org.mapsforge.map.awt.view.MapView;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleLayer;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;

import pl.lanteq.mapsforge.theme.editor.model.RenderTheme;


public class MapPreviewPanel extends JPanel implements XmlRenderThemeMenuCallback {
    private static final GraphicFactory GRAPHIC_FACTORY = AwtGraphicFactory.INSTANCE;
    private MapView mapView;
    private TileRendererLayer tileRendererLayer;
    private MapDataStore mapDataStore;
    private MapDataPanel mapDataPanel;
    private JLabel statusLabel;
    private JLabel zoomLabel;

    public MapPreviewPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Initialize MapView
        mapView = new MapView();

        add(mapView, BorderLayout.CENTER);

        // Status bar
        zoomLabel = new JLabel();
        statusLabel = new JLabel("No theme applied");
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(zoomLabel);
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);

        mapView.getModel().mapViewPosition.addObserver(() -> {
            zoomLabel.setText("Zoom: " + mapView.getModel().mapViewPosition.getZoomLevel());
        });
    }

    public void setMapDataPanel(MapDataPanel panel) {
        this.mapDataPanel = panel;
    }


    @Override
    public Set<String> getCategories(XmlRenderThemeStyleMenu style) {
        String id = style.getDefaultValue();

        XmlRenderThemeStyleLayer baseLayer = style.getLayer(id);
        Set<String> result = baseLayer.getCategories();

        for(XmlRenderThemeStyleLayer overlay : baseLayer.getOverlays()) {
//            if(overlay.isEnabled())
//                result.addAll(overlay.getCategories());
            result.addAll(overlay.getCategories());
        }

        return result;
    }

    public void openMap(String path) throws FileNotFoundException {
        File mapFile = new File(path);

        TileCache tileCache = AwtUtil.createTileCache(
//                mapView.getModel().displayModel.getTileSize(),
                256,
                mapView.getModel().frameBufferModel.getOverdrawFactor(),
                2048,
                new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));

        mapDataStore = new MapFile(mapFile);
//        tileRendererLayer = new DataCaptureTileRendererLayer(
//                mapView.getModel().displayModel.getTileSize(),
//                tileCache,
//                mapDataStore,
//                mapView.getModel().mapViewPosition,
//                MapPreviewPanel.GRAPHIC_FACTORY);
        tileRendererLayer = new TileRendererLayer(
                tileCache,
                mapDataStore,
                mapView.getModel().mapViewPosition,
                MapPreviewPanel.GRAPHIC_FACTORY);



        mapView.getLayerManager().getLayers().add(tileRendererLayer);

        mapView.setCenter(new LatLong(52.1425551, 20.7170701));
        mapView.setZoomLevel((byte) 12);

        // add mouse listener for clicks
        mapView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Convert click coordinates to geo coordinates
                LatLong clickLatLong = mapView.getMapViewProjection().fromPixels(e.getX(), e.getY());
                Point clickXY = new Point(e.getX(), e.getY());
                getMapData(clickLatLong, clickXY);
            }
        });
    }

    public void applyTheme(String themePath) {
        tileRendererLayer.getTileCache().purge();
        try {
            File themeFile = new File(themePath);
            XmlRenderTheme renderTheme = new ExternalRenderTheme(themeFile);
//        XmlRenderTheme renderTheme = MapsforgeThemes.MOTORIDER;
            tileRendererLayer.setXmlRenderTheme(renderTheme);

            mapView.getLayerManager().redrawLayers();
            statusLabel.setText("Theme applied");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error while applying theme: " + e.getMessage());
        }
    }

    private void getMapData(LatLong tapLatLong, Point tapXY) {
        final int TOUCH_RADIUS = 32 / 2;

        // Read all labeled POI and ways for the area covered by the tiles under touch
        float touchRadius = TOUCH_RADIUS * this.mapView.getModel().displayModel.getScaleFactor();
        long mapSize = MercatorProjection.getMapSize(this.mapView.getModel().mapViewPosition.getZoomLevel(), this.mapView.getModel().displayModel.getTileSize());
        double pixelX = MercatorProjection.longitudeToPixelX(tapLatLong.longitude, mapSize);
        double pixelY = MercatorProjection.latitudeToPixelY(tapLatLong.latitude, mapSize);
        Point mapXY = new Point(pixelX, pixelY);
        int tileXMin = MercatorProjection.pixelXToTileX(pixelX - touchRadius, this.mapView.getModel().mapViewPosition.getZoomLevel(), this.mapView.getModel().displayModel.getTileSize());
        int tileXMax = MercatorProjection.pixelXToTileX(pixelX + touchRadius, this.mapView.getModel().mapViewPosition.getZoomLevel(), this.mapView.getModel().displayModel.getTileSize());
        int tileYMin = MercatorProjection.pixelYToTileY(pixelY - touchRadius, this.mapView.getModel().mapViewPosition.getZoomLevel(), this.mapView.getModel().displayModel.getTileSize());
        int tileYMax = MercatorProjection.pixelYToTileY(pixelY + touchRadius, this.mapView.getModel().mapViewPosition.getZoomLevel(), this.mapView.getModel().displayModel.getTileSize());
        Tile upperLeft = new Tile(tileXMin, tileYMin, this.mapView.getModel().mapViewPosition.getZoomLevel(), this.mapView.getModel().displayModel.getTileSize());
        Tile lowerRight = new Tile(tileXMax, tileYMax, this.mapView.getModel().mapViewPosition.getZoomLevel(), this.mapView.getModel().displayModel.getTileSize());
        MapReadResult mapReadResult = mapDataStore.readMapData(upperLeft, lowerRight);

        StringBuilder sb = new StringBuilder();

        // Filter POI
        sb.append("*** POI ***");
        for (PointOfInterest pointOfInterest : mapReadResult.pois) {
            Point layerXY = this.mapView.getMapViewProjection().toPixels(pointOfInterest.position);
            if (!Rotation.noRotation(this.mapView.getMapRotation()) && layerXY != null) {
                layerXY = this.mapView.getMapRotation().rotate(layerXY, true);
            }
            if (layerXY.distance(tapXY) > touchRadius) {
                continue;
            }
            sb.append("\n");
            List<Tag> tags = pointOfInterest.tags;
            for (int i = 0, n = tags.size(); i < n; i++) {
                Tag tag = tags.get(i);
                sb.append("\n").append(tag.key).append("=").append(tag.value);
            }
        }

        // Filter ways
        sb.append("\n\n").append("*** WAYS ***");
        for (Way way : mapReadResult.ways) {
            boolean cont = false;
            if(!LatLongUtils.isClosedWay(way.latLongs[0])) {
                Point nearestPoint = nearestPolygonPoint(way.latLongs, tapLatLong);
                if (nearestPoint == null || nearestPoint.distance(mapXY) > touchRadius) {
                    continue;
                }
            } else if(!LatLongUtils.contains(way.latLongs[0], tapLatLong)) {
                continue;
            }

            sb.append("\n");
            List<Tag> tags = way.tags;
            for (int i = 0, n = tags.size(); i < n; i++) {
                Tag tag = tags.get(i);
                sb.append("\n").append(tag.key).append("=").append(tag.value);
            }
        }

        System.out.println(sb.toString());
        mapDataPanel.setText(sb.toString());
    }

    private Point nearestPolygonPoint(LatLong[][] polygons, LatLong point) {
        Point nearestPoint = null;
        long mapSize = MercatorProjection.getMapSize(this.mapView.getModel().mapViewPosition.getZoomLevel(), this.mapView.getModel().displayModel.getTileSize());

        // iterate over each pair of LatLongs in the polygon
        for(LatLong[] polygon : polygons) {
            for (int i = 0; i < polygon.length; i++) {
                LatLong start = polygon[i];
                LatLong end = polygon[(i + 1) % polygon.length]; // wrap around
                Point startXY = new Point(
                        MercatorProjection.longitudeToPixelX(start.longitude, mapSize),
                        MercatorProjection.latitudeToPixelY(start.latitude, mapSize)
                );
                Point endXY = new Point(
                        MercatorProjection.longitudeToPixelX(end.longitude, mapSize),
                        MercatorProjection.latitudeToPixelY(end.latitude, mapSize)
                );
                Point pointXY = new Point(
                        MercatorProjection.longitudeToPixelX(point.longitude, mapSize),
                        MercatorProjection.latitudeToPixelY(point.latitude, mapSize)
                );
                Point candidate = nearestPointOnSegment(startXY, endXY, pointXY);
                if (nearestPoint == null || candidate.distance(pointXY) < nearestPoint.distance(pointXY)) {
                    nearestPoint = candidate;
                }
            }
        }
        return nearestPoint;
    }

    private Point nearestPointOnSegment(Point start, Point end, Point point) {
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        if (dx == 0 && dy == 0) {
            return start; // the segment is a point
        }

        double t = ((point.x - start.x) * dx + (point.y - start.y) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t)); // clamp t to the segment

        return new Point(start.x + t * dx, start.y + t * dy);
    }

}