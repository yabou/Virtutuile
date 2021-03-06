package com.virtutuile.domaine.entities;

import com.virtutuile.afficheur.Constants;
import com.virtutuile.afficheur.swing.events.MouseEventKind;
import com.virtutuile.domaine.UndoRedo;
import com.virtutuile.domaine.entities.surfaces.RectangularSurface;
import com.virtutuile.domaine.entities.surfaces.Surface;
import com.virtutuile.domaine.entities.surfaces.Tile;
import com.virtutuile.domaine.entities.tools.PolygonTransformer;
import com.virtutuile.shared.Pair;
import com.virtutuile.shared.UnorderedMap;
import com.virtutuile.shared.Vector2D;

import java.awt.*;
import java.awt.geom.*;
import java.io.Serializable;
import java.util.EventListener;
import java.util.Iterator;
import java.util.UUID;

public class Meta implements Serializable {

    private EventListener metaEventArgs;
    private UnorderedMap<UUID, Surface> surfaces;
    private UnorderedMap<String, Tile> typeOfTiles;
    private Unit unitSetted = Unit.Metric;

    private EditionAction doing;
    private Direction alignDirection;
    private Orientation stickOrientation;

    private transient Surface selectedSurface;
    private transient Surface hoveredSurface;
    private Surface lastAlignedSurface;
    private transient Tile hoveredTile;
    private boolean isSelectedSurfaceCanBeResized;
    private boolean shouldDisplayCuttedTiles = false;

    private Point2D clicked;
    private Point2D hover;
    private boolean mousePressed;
    private transient boolean isGridActivated;
    private Dimension canvasSize = new Dimension();

    private double zoomFactor = Constants.NORMAL_ZOOM;
    private Point2D.Double canvasPosition = new Point2D.Double();

    private transient  Double gridSize = 10d;

    private transient UndoRedo undoRedo;
    private MouseEventKind lastEvent;

    public Meta() {
        selectedSurface = null;
        hoveredSurface = null;
        lastAlignedSurface = null;
        hoveredTile = null;
        surfaces = new UnorderedMap<>();
        doing = EditionAction.Idle;
        alignDirection = Direction.Undefined;
        stickOrientation = Orientation.Undefined;
        clicked = null;
        hover = null;
        mousePressed = false;
        isGridActivated = false;
        typeOfTiles = new UnorderedMap<>();
        undoRedo = new UndoRedo(this);

        RectangularSurface.Builder b = RectangularSurface.getBuilder();
        b.placePoint(new Point2D.Double(10, 10));
        b.placePoint(new Point2D.Double(50, 50));
        Surface s = b.getSurface();
        surfaces.put(s.getId(), s);

        createNewTile(20, 10, Constants.DEFAULT_SHAPE_FILL_COLOR, "Small", false, 10);
        createNewTile(40, 20, Constants.DEFAULT_SHAPE_FILL_COLOR, "Medium", false, 10);
        createNewTile(80, 40, Constants.DEFAULT_SHAPE_FILL_COLOR, "Large", false, 10);

    }

    public boolean createNewTile(double width, double height, Color color, String name, boolean deletable, int packageSize, double minCut) {
        if (typeOfTiles.containsKey(name)) {
            return false;
        }
        if (color == null) {
            color = Constants.DEFAULT_SHAPE_FILL_COLOR;
        }

        if (unitSetted.equals(Unit.Imperial)) {
            width = inchToCentimeter(width);
            height = inchToCentimeter(height);
        }

        typeOfTiles.put(name, new Tile(width, height, color, name, deletable, packageSize, minCut));
        return true;
    }

    public boolean createNewTile(double width, double height, Color color, String name, boolean deletable, int packageSize) {
        return createNewTile(width, height, color, name, deletable, packageSize, 1);
    }

    public Surface getSelectedSurface() {
        return selectedSurface;
    }

    public void setSelectedSurface(Surface selectedSurface) {
        this.selectedSurface = selectedSurface;
    }

    public UnorderedMap<UUID, Surface> getSurfaces() {
        return surfaces;
    }

    public void setSurfaces(UnorderedMap<UUID, Surface> surfaces) {
        this.surfaces = surfaces;
        undoRedo.addUndo(this);
    }

    public EditionAction getDoing() {
        return doing;
    }

    public void setDoing(EditionAction doing, boolean isDoing) {
        if (isDoing) {
            this.doing = doing;
        } else {
            this.doing = EditionAction.Idle;
        }
    }

    public Point2D getClicked() {
        return clicked;
    }

    public void setClicked(Point2D clicked) {
        this.clicked = clicked;
    }

    public Point2D getHover() {
        return hover;
    }

    public void setHover(Point2D hover) {
        this.hover = hover;
    }

    public boolean isMousePressed() {
        return mousePressed;
    }

    public void setMousePressed(boolean mousePressed) {
        this.mousePressed = mousePressed;
    }

    public Surface getHoveredSurface() {
        return hoveredSurface;
    }

    public void setHoveredSurface(Surface hoveredSurface) {
        this.hoveredSurface = hoveredSurface;
    }

    public boolean isGridActivated() {
        return isGridActivated;
    }

    public void changeGridStatus() {
        isGridActivated = !isGridActivated;
    }

    public void setCanvasSize(int width, int height) {
        canvasSize.width = width;
        canvasSize.height = height;
    }

    public void setCanvasSize(Dimension size) {
        canvasSize = size;
    }

    public Dimension getCanvasSize() {
        return canvasSize;
    }

    public boolean isSelectedSurfaceCanBeResized() {
        return isSelectedSurfaceCanBeResized;
    }

    public void setSelectedSurfaceCanBeResized(boolean selectedSurfaceCanBeResized) {
        isSelectedSurfaceCanBeResized = selectedSurfaceCanBeResized;
    }

    public Point2D pointToPoints2D(Point point) {
        return new Vector2D(point).add(canvasPosition).multiply(zoomFactor).toPoint2D();
    }

    public Point point2DToPoint(Point2D coordinates) {
        return new Vector2D(coordinates).divide(zoomFactor).subtract(canvasPosition).toPoint();
    }

    public Path2D.Double rawPathToGfxPath(Path2D path) {
        Path2D.Double ret = new Path2D.Double();

        double[] coords = new double[6];
        for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {
            int segType = pi.currentSegment(coords);
            Point pt = point2DToPoint(new Point2D.Double(coords[0], coords[1]));
            Point pt2, pt3;

            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    ret.moveTo(pt.x, pt.y);
                    break;
                case PathIterator.SEG_LINETO:
                    ret.lineTo(pt.x, pt.y);
                    break;
                case PathIterator.SEG_QUADTO:
                    pt2 = point2DToPoint(new Point2D.Double(coords[2], coords[3]));
                    ret.quadTo(pt.x, pt.y, pt2.x, pt2.y);
                    break;
                case PathIterator.SEG_CUBICTO:
                    pt2 = point2DToPoint(new Point2D.Double(coords[2], coords[3]));
                    pt3 = point2DToPoint(new Point2D.Double(coords[4], coords[5]));
                    ret.curveTo(pt.x, pt.y, pt2.x, pt2.y, pt3.x, pt3.y);
                    break;
                case PathIterator.SEG_CLOSE:
                    ret.closePath();
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }

        return ret;
    }

    public Point[] points2DToPoints(Point2D[] point2D) {
        Point[] points = new Point[point2D.length];

        for (int i = 0; i < point2D.length; ++i) {
            points[i] = point2DToPoint(point2D[i]);
        }
        return points;
    }

    public int[][] points2DToRawPoints(Point2D[] point2D) {
        int[][] ret = new int[2][point2D.length];

        for (int i = 0; i < point2D.length; ++i) {
            Point p = point2DToPoint(point2D[i]);
            ret[0][i] = p.x;
            ret[1][i] = p.y;
        }
        return ret;
    }

    public double pixelsToCentimeters(int pixels) {
        return (double) pixels * zoomFactor;
    }

    public int centimetersToPixels(double centimeters) {
        return (int) (centimeters / zoomFactor);
    }

    public Point2D.Double getCanvasPosition() {
        return canvasPosition;
    }

    public Meta setCanvasPosition(double posX, double posY) {
        return setCanvasPosition(new Point2D.Double(posX, posY));
    }

    public Meta setCanvasPosition(Point2D.Double canvasPosition) {
        this.canvasPosition = canvasPosition;
        return this;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public double getZoomFactorFront() {
        if (unitSetted.equals(Unit.Imperial)) {
            return centimeterToInch(zoomFactor);
        }
        return zoomFactor;
    }

    public Meta setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
        return this;
    }

    public void updateZoom(double zoom, Point cursor) {
        zoom = zoom * -1;
        double oldWidth = pixelsToCentimeters(getCanvasSize().width);
        double oldHeight = pixelsToCentimeters(getCanvasSize().height);
        double newCanvasSize = pixelsToCentimeters((int) ((double) getCanvasSize().width - (zoom * Constants.WHEEL_TICK_RATIO)));
        double zoomFactor = (newCanvasSize / oldWidth);
        Point2D.Double pos = getCanvasPosition();
        if (zoomFactor == 1) {
            return;
        }

        Path2D.Double rect = new Path2D.Double();

        rect.moveTo(pos.x, pos.y);
        rect.lineTo(pos.x + oldWidth, pos.y);
        rect.lineTo(pos.x + oldWidth, pos.y + oldHeight);
        rect.lineTo(pos.x, pos.y + oldHeight);
        rect.closePath();

        cursor.x -= getCanvasSize().width / 4;
        cursor.y -= getCanvasSize().height / 4;

        AffineTransform at = new AffineTransform();
        at.translate(cursor.x, cursor.y);
        at.scale(zoomFactor, zoomFactor);
        at.translate(-cursor.x, -cursor.y);
        rect.transform(at);

        setCanvasPosition(rect.getBounds2D().getX(), rect.getBounds2D().getY());
        setZoomFactor(getZoomFactor() * zoomFactor);
    }

    public Double[] getSelectedSurfaceProperties() {
        if (selectedSurface != null) {
            Double[] dimensions = new Double[4];
            dimensions[0] = selectedSurface.getBounds().width;
            dimensions[1] = selectedSurface.getBounds().height;
            dimensions[2] = selectedSurface.getBounds().x;
            dimensions[3] = selectedSurface.getBounds().y;

            if (unitSetted.equals(Unit.Imperial)) {
                Double[] dimensionsInch = new Double[4];
                int i = 0;
                for (Double dimension : dimensions) {
                    dimensionsInch[i] = centimeterToInch(dimension);
                    i++;
                }
                return dimensionsInch;
            }

            return dimensions;
        }
        return null;
    }

    public Double getSelectedSurfaceGroutThickness() {
        if (selectedSurface != null) {
            Double thickness = selectedSurface.getGrout().getThickness();

            if (unitSetted == Unit.Imperial) {
                thickness = centimeterToInch(thickness);
            }

            return thickness;
        } else {
            return null;
        }
    }

    public Double getGridSize() {
        return gridSize;
    }

    public Double getGridSizeFront() {
        Double ret = gridSize;
        if (unitSetted == Unit.Imperial) {
            ret = centimeterToInch(ret);
        }
        return ret;
    }

    public void setGridSize(Double gridSize) {
        if (unitSetted.equals(Unit.Imperial)) {
            gridSize = inchToCentimeter(gridSize);
        }
        this.gridSize = gridSize;
    }

    public Double[] getHoveredSurfaceDimension() {
        if (hoveredSurface != null) {
            Double[] dim = new Double[2];
            dim[0] = hoveredSurface.getBounds().width;
            dim[1] = hoveredSurface.getBounds().height;
            if (unitSetted == Unit.Imperial) {
                dim[0] = centimeterToInch(dim[0]);
                dim[1] = centimeterToInch(dim[1]);
                return dim;
            }
            return dim;
        }
        return null;
    }

    public void setSelectedSurfaceWidth(double value) {
        if (selectedSurface != null) {
            if (unitSetted.equals(Unit.Imperial)) {
                value = inchToCentimeter(value);
            }
            selectedSurface.setWidth(value);
        }
    }

    public void setSelectedSurfaceHeight(double value) {
        if (selectedSurface != null) {
            if (unitSetted.equals(Unit.Imperial)) {
                value = inchToCentimeter(value);
            }
            selectedSurface.setHeight(value);
        }
    }

    public void recalcPattern() {
        if (selectedSurface != null
                && selectedSurface.getPatternGroup() != null) {
            selectedSurface.getPatternGroup().recalcPattern(selectedSurface);
        }
    }

    public void setGroutColor(Color color) {
        if (selectedSurface != null
                && selectedSurface.getGrout() != null) {
            selectedSurface.getGrout().setColor(color);
            if (selectedSurface.getPatternGroup() != null) {
                selectedSurface.setFillColor(color);
            }
        }
    }

    public void setGroutThickness(String value) {
        Double valueD = Double.parseDouble(value);

        if (unitSetted.equals(Unit.Imperial)) {
            valueD = inchToCentimeter(valueD);
        }

        if (selectedSurface != null
                && selectedSurface.getGrout() != null) {
            if (selectedSurface.getGrout().getThickness() != valueD) {
                selectedSurface.getGrout().setThickness(Double.parseDouble(value));
                if (selectedSurface.getPatternGroup() != null) {
                    selectedSurface.getPatternGroup().recalcPattern(selectedSurface); /*TODO*/
                }
            }
        }
    }

    public Double[] getTileDimension(String type) {
        Double[] dimensions = new Double[2];
        dimensions[0] = typeOfTiles.get(type).getBounds().getWidth();
        dimensions[1] = typeOfTiles.get(type).getBounds().getHeight();

        if (unitSetted.equals(Unit.Imperial)) {
            Double[] inched = new  Double[2];
            inched[0] = centimeterToInch(dimensions[0]);
            inched[1] = centimeterToInch(dimensions[1]);
            return inched;
        }

        return dimensions;
    }

    public void setWidthForTile(String value, String name) {
        Double converted = Double.parseDouble(value);

        if (unitSetted.equals(Unit.Imperial)) {
            converted = inchToCentimeter(Double.parseDouble(value));
        }

        typeOfTiles.get(name).setWidth(converted);
        surfaces.forEach((key, surface) -> {
            if (surface.getPatternGroup() != null
                    && surface.getTypeOfTile() != null
                    && surface.getTypeOfTile().getName().equals(name)) {
                surface.getPatternGroup().recalcPattern(surface);
            }
        });
    }

    public void setHeightForTile(String value, String name) {
        Double converted = Double.parseDouble(value);

        if (unitSetted.equals(Unit.Imperial)) {
            converted = inchToCentimeter(Double.parseDouble(value));
        }

        typeOfTiles.get(name).setHeight(converted);
        surfaces.forEach((key, surface) -> {
            if (surface.getPatternGroup() != null
                    && surface.getTypeOfTile() != null
                    && surface.getTypeOfTile().getName().equals(name)) {
                surface.getPatternGroup().recalcPattern(surface);
            }
        });
    }

    public void setMinimalCutForTile(String name, double value) {
        if (unitSetted.equals(Unit.Imperial)) {
            value = inchToCentimeter(value);
        }

        typeOfTiles.get(name).setMinimalCut(value);
        surfaces.forEach((key, surface) -> {
            if (surface.getPatternGroup() != null
                    && surface.getTypeOfTile() != null
                    && surface.getTypeOfTile().getName().equals(name)) {
                surface.getPatternGroup().recalcPattern(surface);
            }
        });
    }

    public double getMinimalCutSizeFor(String name) {
        return typeOfTiles.get(name).getMinimalCut();
    }

    public void renameTile(String newName, String oldName) {
        /*System.out.println("old name : " + oldName + ", new name: " + newName);
        typeOfTiles.get(oldName).setName(newName);
        Tile tmp = typeOfTiles.get(oldName).copy();
        typeOfTiles.remove(oldName);
        typeOfTiles.put(newName, tmp);*/
    }

    public String[] getTypeOfTiles() {
        String[] types = new String[typeOfTiles.size()];
        final int[] i = {0};
        typeOfTiles.forEach((key, value) -> {
            types[i[0]] = key;
            i[0]++;
        });
        return types;
    }

    public void setTypeOfTileOnSurface(String typeOfTile) {
        if (selectedSurface != null) {
            typeOfTiles.forEach((name, tile) -> {
                if (name.equals(typeOfTile)) {
                    selectedSurface.setTypeOfTile(tile);
                    if (selectedSurface.getPatternGroup() != null) {
                        selectedSurface.getPatternGroup().changeTileType(selectedSurface, tile);
                    }
                }
            });
        }
    }

    public boolean isSurfaceSelected() {
        if (selectedSurface != null) {
            return true;
        }
        return false;
    }

    public void setTypeOfTileColor(String typeOfTile, Color color) {
        /*System.out.println("tuile: " + typeOfTile + ", color: " + color.toString());*/
        if (typeOfTiles != null && typeOfTiles.containsKey(typeOfTile)) {
            typeOfTiles.get(typeOfTile).setFillColor(color);
            typeOfTiles.get(typeOfTile).setColor(color);
        }

        assert typeOfTiles != null;
        typeOfTiles.forEach((name, tile) -> {
            if (name.equals(typeOfTile)) {
                surfaces.forEach((surfaceName, surface) -> {
                    if (surface.getPatternGroup() != null) {
                        surface.getPatternGroup().recalcPattern(surface);
                    }
                });
            }
        });
    }

    public Point2D updatePosToMagnetic(Point point) {
        Point2D.Double mousePosCM = (Point2D.Double) pointToPoints2D(point);
        Point2D.Double screen = (Point2D.Double) updateCoordsToMagnetic(canvasPosition);

        hover = updateCoordsToMagnetic((Point2D.Double) hover);
        hover = new Point2D.Double(hover.getX() + canvasPosition.x - screen.x, hover.getY() + canvasPosition.y - screen.y);

        Point2D.Double ret = (Point2D.Double) updateCoordsToMagnetic(mousePosCM);

        if (ret.x % gridSize > gridSize / 5) {
            ret.x += canvasPosition.x - screen.x;
        }
        if (ret.y % gridSize > gridSize / 5) {
            ret.y += canvasPosition.y - screen.y;
        }

        return ret;
    }

    private Point2D updateCoordsToMagnetic(Point2D.Double point) {
        if (point.x % gridSize <= gridSize / 2) {
            if (point.x % gridSize < gridSize / 7)
                point.x -= point.x % gridSize;
        } else {
            if (point.x % gridSize > gridSize / 7)
                point.x += gridSize - point.x % gridSize;
        }

        if (point.y % gridSize <= gridSize / 2) {
            if (point.y % gridSize < gridSize / 7)
                point.y -= point.y % gridSize;
        } else {
            if (point.y % gridSize > gridSize / 7)
                point.y += gridSize - point.y % gridSize;
        }

        return point;
    }

    public Surface updateSurfacePosToMagneticPos() {
        Surface surf = selectedSurface;

        Rectangle2D.Double bounds = surf.getBounds();

        Point2D newBounds = updateCoordsToMagnetic(new Point2D.Double(bounds.x, bounds.y));

        if (newBounds.getX() % gridSize > gridSize / 5 && newBounds.getY() % gridSize > gridSize / 5)
            selectedSurface.move(new Point2D.Double(bounds.x, bounds.y), newBounds,selectedSurface.getNext());
        return surf;
    }

    public void setPatternStartPosition(String name) {
        if (selectedSurface != null
                && selectedSurface.getPatternGroup() != null) {
            if (name.equals("Center")) {
                Rectangle2D.Double bounds = selectedSurface.getBounds();
                selectedSurface.getPatternGroup().setOrigin(bounds.width / 2, bounds.height / 2);
            } else {
                selectedSurface.getPatternGroup().setOrigin(0, 0);
            }
            selectedSurface.getPatternGroup().recalcPattern(selectedSurface);
        }
    }

    public void setSelectedSurfaceLongitude(Double longitude) {
        if (selectedSurface != null) {
            Rectangle2D.Double b = selectedSurface.getBounds();
            if (unitSetted.equals(Unit.Imperial)) {
                longitude = inchToCentimeter(longitude);
            }
            selectedSurface.move(new Point2D.Double(b.x, b.y), new Point2D.Double(longitude, b.y),selectedSurface.getNext());
        }
    }

    public void setSelectedSurfaceLatitude(Double latitude) {
        if (selectedSurface != null) {
            Rectangle2D.Double b = selectedSurface.getBounds();
            if (unitSetted.equals(Unit.Imperial)) {
                latitude = inchToCentimeter(latitude);
            }
            selectedSurface.move(new Point2D.Double(b.x, b.y), new Point2D.Double(b.x, latitude),selectedSurface.getNext());
        }
    }

    public boolean deleteTile(String selectedTile) {
        if (typeOfTiles.containsKey(selectedTile)
                && typeOfTiles.get(selectedTile).isDeletable()) {
            typeOfTiles.remove(selectedTile);
            return true;
        }
        return false;
    }

    public Integer[] getSurfaceTileProperties() {
        return getSurfaceTileProperties(selectedSurface);
    }

    public int[] getAllSurfaceTileProperties() {
        int[] result = new int[]{0, 0};

        if (surfaces.size() != 0) {
            Iterator<Pair<UUID, Surface>> iterator = surfaces.iterator();
            for (Pair<UUID, Surface> pair = iterator.next(); iterator.hasNext(); pair = iterator.next()) {
                Integer[] resCase = getSurfaceTileProperties(pair.getValue());
                if (resCase != null) {
                    result[0] += resCase[0];
                    result[1] += resCase[1];
                }
            }
        }
        return result;
    }

    private Integer[] getSurfaceTileProperties(Surface surface) {
        Integer[] result = new Integer[2];
        if (surface != null
                && surface.getPatternGroup() != null) {
            result[0] = surface.getPatternGroup().getTiles().size();
            result[1] = surface.getPatternGroup().getCuttedTiles();
        }
        return result;
    }

    public Integer getUsedPackageOnSurface() {
        if (selectedSurface != null
                && selectedSurface.getPatternGroup() != null
                && selectedSurface.getTypeOfTile() != null) {
            double resDouble =  (double)selectedSurface.getPatternGroup().getTiles().size() / (double)selectedSurface.getTypeOfTile().getPackageSize();
            return (int) Math.ceil(resDouble);
        }
        return 0;
    }

    public Integer getUsedPackageFor(String tileType) {
        int res = 0;
        double resDouble = 0;

        /*if (surfaces.size() != 0) {
            Iterator<Pair<UUID, Surface>> iterator = surfaces.iterator();
            for (Pair<UUID, Surface> pair = iterator.next(); iterator.hasNext(); pair = iterator.next()) {
                if (pair.getValue().getTypeOfTile().getName().equals(tileType)
                        && pair.getValue().getPatternGroup() != null) {
                    resDouble += (double) pair.getValue().getPatternGroup().getTiles().size() / (double) pair.getValue().getTypeOfTile().getPackageSize();
                }
            }
            res = (int) Math.ceil(resDouble);
        }
        return res;*/
        if (surfaces != null
                && surfaces.size() != 0
                && typeOfTiles.containsKey(tileType)) {
            Iterator<Pair<UUID, Surface>> iterator = surfaces.iterator();
            do {
                Pair<UUID, Surface> pair = iterator.next();
                if (pair.getValue().getTypeOfTile() != null && pair.getValue().getTypeOfTile().getName().equals(tileType)
                        && pair.getValue().getPatternGroup() != null) {
                    resDouble += (double) pair.getValue().getPatternGroup().getTiles().size() / (double) pair.getValue().getTypeOfTile().getPackageSize();
                }
            } while (iterator.hasNext());
            res = (int) Math.ceil(resDouble);
        }
        return res;
    }

    public Integer[] getTotalTileFor(String tileName) {
        //getSurfaceTileProperties

        Integer[] result = new Integer[]{0, 0};

        if (surfaces != null
                && surfaces.size() != 0
                && typeOfTiles.containsKey(tileName)) {
            Iterator<Pair<UUID, Surface>> iterator = surfaces.iterator();
            do {
                Pair<UUID, Surface> pair = iterator.next();
                if (pair.getValue().getTypeOfTile() != null && pair.getValue().getTypeOfTile().getName().equals(tileName)
                        && pair.getValue().getPatternGroup() != null) {
                    result[0] += pair.getValue().getPatternGroup().getTiles().size();
                    result[1] += pair.getValue().getPatternGroup().getCuttedTiles();
                }
            } while (iterator.hasNext());
        }
        return result;
    }

    public boolean displayCuttedTiles() {
        return shouldDisplayCuttedTiles;
    }

    public void displayCuttedTiles(boolean value) {
        shouldDisplayCuttedTiles = value;
    }

    public void deleteThisTile(String tileName) {
        if (surfaces != null
                && surfaces.size() != 0) {
            Iterator<Pair<UUID, Surface>> iterator = surfaces.iterator();
            do {
                Pair<UUID, Surface> pair = iterator.next();
                if (pair.getValue().getTypeOfTile() != null && pair.getValue().getTypeOfTile().getName().equals(tileName)) {
                    pair.getValue().setPatternGroup(null);
                    pair.getValue().setTypeOfTile(getDefaultTile());
                    pair.getValue().setFillColor(pair.getValue().getSettedColor());
                }
            } while (iterator.hasNext());
        }
    }

    public Tile getDefaultTile() {
        if (typeOfTiles.size() != 0) {
            Iterator<Pair<String, Tile>> iterator = typeOfTiles.iterator();
            for (Pair<String, Tile> pair = iterator.next(); iterator.hasNext(); pair = iterator.next()) {
                return pair.getValue();
            }
        }
        return null;
    }

    public int getPackageSizeFor(String tileName) {
        if (typeOfTiles.get(tileName) != null) {
            return typeOfTiles.get(tileName).getPackageSize();
        }
        return 0;
    }

    public void setPackageSizeFor(String tileName, int packageSize) {
        if (typeOfTiles.get(tileName) != null) {
            typeOfTiles.get(tileName).setPackageSize(packageSize);
        }
    }

    public Tile getHoveredTile() {
        return hoveredTile;
    }

    public void setHoveredTile(Tile tile) {
        this.hoveredTile = tile;
    }

    public Surface getSurfaceIntersected(Surface surface) {
        Surface found = null;
        Iterator<Pair<UUID, Surface>> iterator = surfaces.iterator();
        do {
            Pair<UUID, Surface> pair = iterator.next();
            if (surface.getId() != pair.getValue().getId()
                    && surface.containsOrIntersect(pair.getValue())) {
                found = pair.getValue();
            }
        } while (iterator.hasNext());
        return found;
    }

    public void makeSurfaceHole() {
        Surface mainSurface;
        doing = EditionAction.Idle;
        if (selectedSurface != null) {
            /*selectedSurface.setHole(true);*/
            mainSurface = getSurfaceIntersected(selectedSurface);
            if (mainSurface != null
                    && mainSurface.contains(selectedSurface)) {
                mainSurface.setPolygon(PolygonTransformer.hardSubtract(mainSurface.getPolygon(), selectedSurface.getPolygon()));
                selectedSurface.setSelected(false);
                surfaces.remove(selectedSurface.getId());
                selectedSurface = null;
                recalcPattern();
            }
        }
    }

    public boolean setAlignAction(String name) {
        if (name != null) {
            switch (name) {
                case "Align Top":
                    alignDirection = Direction.Top;
                    setDoing(EditionAction.Align, true);
                    return true;
                case "Align Bottom":
                    alignDirection = Direction.Bottom;
                    setDoing(EditionAction.Align, true);
                    return true;
                case "Align Left":
                    alignDirection = Direction.Left;
                    setDoing(EditionAction.Align, true);
                    return true;
                case "Align Right":
                    alignDirection = Direction.Right;
                    setDoing(EditionAction.Align, true);
                    return true;
                case "Centered Horizontal":
                    alignDirection = Direction.CenteredHorizontal;
                    setDoing(EditionAction.Align, true);
                    return true;
                case "Centered Vertical":
                    alignDirection = Direction.CenteredVertical;
                    setDoing(EditionAction.Align, true);
                    return true;
                default:
                    alignDirection = Direction.Undefined;
                    return false;

            }
        } else {
            doing = EditionAction.Idle;
            alignDirection = Direction.Undefined;
        }
        return false;
    }

    public Direction getAlignDirection() {
        return alignDirection;
    }

    public void setAlignDirection(Direction alignDirection) {
        this.alignDirection = alignDirection;
    }

    public Surface getLastAlignedSurface() {
        return lastAlignedSurface;
    }

    public void setLastAlignedSurface(Surface lastAlignedSurface) {
        this.lastAlignedSurface = lastAlignedSurface;
    }

    public boolean setStickAction(String name) {
        if (name != null) {
            switch (name) {
                case "Horizontal Stick":
                    stickOrientation = Orientation.Horizontal;
                    setDoing(EditionAction.Stick, true);
                    return true;
                case "Vertical Stick":
                    stickOrientation = Orientation.Vertical;
                    setDoing(EditionAction.Stick, true);
                    return true;
                default:
                    stickOrientation = Orientation.Undefined;
                    return false;

            }
        } else {
            doing = EditionAction.Idle;
            alignDirection = Direction.Undefined;
        }
        return false;
    }

    public Orientation getStickOrientation() {
        return stickOrientation;
    }

    public void setStickOrientation(Orientation stickOrientation) {
        this.stickOrientation = stickOrientation;
    }

    public void setMeta(Meta metaCpy) {
        doing = EditionAction.Idle;
        alignDirection = metaCpy.alignDirection;
        stickOrientation = metaCpy.stickOrientation;
        lastAlignedSurface = metaCpy.lastAlignedSurface;
        isSelectedSurfaceCanBeResized = metaCpy.isSelectedSurfaceCanBeResized;
        shouldDisplayCuttedTiles = metaCpy.shouldDisplayCuttedTiles;
        clicked = metaCpy.clicked;
        hover = metaCpy.hover;
        mousePressed = metaCpy.mousePressed;
        isGridActivated = metaCpy.isGridActivated;
        canvasSize = metaCpy.canvasSize;
        zoomFactor = metaCpy.zoomFactor;
        canvasPosition = metaCpy.canvasPosition;
        surfaces = metaCpy.surfaces;
        typeOfTiles = metaCpy.typeOfTiles;
        unitSetted = metaCpy.unitSetted;
    }

    public void setDoing(EditionAction action) {
        doing = action;
    }

    public Double getSurfaceRotation() {
        if (selectedSurface != null) {
            return selectedSurface.getRotationRadian();
        }
        return 0.0;
    }

    public Double getSurfaceRotationDeg() {
        if (selectedSurface != null) {
            return selectedSurface.getRotationDeg();
        }
        return 0.0;
    }

    public double getSelectedSurfacePatternRotation() {
        if (selectedSurface != null && selectedSurface.getPatternGroup() != null) {
            return selectedSurface.getPatternGroup().getRotation();
        }
        return 0.0;
    }

    public void setUnit(String unit) {
        switch (unit) {
            case "Metric":
                unitSetted = Unit.Metric;
                break;
            case "Imperial":
                unitSetted = Unit.Imperial;
                break;
            default:
                break;
        }
    }

    public String getUnitSetted() {
        switch (unitSetted) {
            case Metric:
                return "Metric";
            case Imperial:
                return "Imperial";
            default:
                return null;
        }
    }

    public void switchUnit() {
        switch (unitSetted) {
            case Metric:
                unitSetted = Unit.Imperial;
                break;
            case Imperial:
                unitSetted = Unit.Metric;
                break;
        }
    }

    public Double centimeterToInch(Double centimeter) {
        return com.virtutuile.domaine.Constants.Convert(centimeter, com.virtutuile.domaine.Constants.Units.Centimeter, com.virtutuile.domaine.Constants.Units.Inches);
    }

    public Double inchToCentimeter(Double inch) {
        return com.virtutuile.domaine.Constants.Convert(inch, com.virtutuile.domaine.Constants.Units.Inches, com.virtutuile.domaine.Constants.Units.Centimeter);
    }

    public void undo() {
        Meta newItem = undoRedo.undo();
        if (newItem != null) {
            setMeta(newItem);
        }
    }

    public void redo() {
        Meta newItem = undoRedo.redo();

        if (newItem != null) {
            setMeta(newItem);
        }
    }

    public void addToUndo() {
        undoRedo.addUndo(this);
    }

    public void setLastEvent(MouseEventKind event) {
        lastEvent = event;
    }

    public MouseEventKind getLastEvent() {
        return lastEvent;
    }

    public enum EditionAction {
        Idle,
        CreatingRectangularSurface,
        CreatingFreeSurface,
        Align,
        Stick
    }

    public enum Direction {
        Top,
        Bottom,
        Left,
        Right,
        CenteredHorizontal,
        CenteredVertical,
        Undefined,
    }

    public enum Orientation {
        Horizontal,
        Vertical,
        Undefined
    }

    public enum Unit {
        Metric,
        Imperial,
    }
}
