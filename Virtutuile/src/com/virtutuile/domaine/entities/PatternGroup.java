package com.virtutuile.domaine.entities;

import com.virtutuile.domaine.entities.patterns.Classic;
import com.virtutuile.domaine.entities.patterns.Offset;
import com.virtutuile.domaine.entities.patterns.Pattern;
import com.virtutuile.domaine.entities.surfaces.PrimarySurface;
import com.virtutuile.domaine.entities.surfaces.Surface;
import com.virtutuile.domaine.entities.surfaces.Tile;
import com.virtutuile.domaine.entities.tools.PolygonTransformer;
import com.virtutuile.shared.NotNull;
import com.virtutuile.shared.Vector2D;

import java.awt.geom.*;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

public class PatternGroup implements Serializable {

    private Pattern pattern = null;
    private Vector<Tile> tiles = new Vector<>();
    private double rotation;
    private boolean centered = false;
    private int cuttedTiles = 0;
    private Vector2D patternOrigin;
    private double shift = 0d;
    private boolean shiftDirection = false;

    public PatternGroup(String patternName, Surface surface, Tile tile) {
        if (surface.getTypeOfTile() == null) {
            surface.setTypeOfTile(tile);
        }
        switch (patternName) {
            case "Classic":
                pattern = new Classic(surface.getTypeOfTile());
                break;
            case "Offset":
                pattern = new Offset(surface.getTypeOfTile());
                break;
            default:
                break;
        }
        if (this.pattern != null) {
            Path2D.Double grout = surface.getPolygon();
            if (surface.getGrout().getThickness() > 0)
                grout = PolygonTransformer.flate(grout, surface.getGrout().getThickness());
            this.buildPattern(surface, grout);
            surface.setFillColor(surface.getGrout().getColor());
        }
    }

    public PatternGroup(PatternGroup patternGroup) {
    }

    public void recalcPattern(Surface surface) {
        tiles = new Vector<>();
        pattern.setTileType(surface.getTypeOfTile());

        Rectangle2D.Double bounds = surface.getBounds();
        AffineTransform at = new AffineTransform();
        double degrees = surface.getRotationDeg() + rotation;
        at.setToRotation((degrees * Math.PI / 180) * -1);

        surface.moveOf((-bounds.x - (bounds.getWidth() / 2)), ((-bounds.y - (bounds.getHeight() / 2))));
        surface.getPolygon().transform(at);

        Path2D.Double grout = surface.getPolygon();
        if (surface.getGrout().getThickness() > 0) {
            grout = PolygonTransformer.flate(grout, surface.getGrout().getThickness());
        }

        buildPattern(surface, grout);

        at = new AffineTransform();
        at.setToRotation(degrees * Math.PI / 180);
        for (Tile tile : tiles) {
            tile.getPolygon().transform(at);
            /*tile.moveOf((bounds.x + (bounds.getWidth() / 2)),((bounds.y + (bounds.getHeight() / 2))));*/
        }
        surface.getPolygon().transform(at);
        surface.moveOf((bounds.x + (bounds.getWidth() / 2)), ((bounds.y + (bounds.getHeight() / 2))));
    }

    public void changeTileType(Surface surface, Tile tile) {
        pattern.setTileType(tile);
        recalcPattern(surface);
    }

    private Surface transformToSurfaceWithoutSideGrout(Surface surface) {
        if (this.pattern != null) {
            Rectangle2D.Double bounds = surface.getBounds();
            double groutThickness = surface.getGrout().getThickness();
            Surface groutedSurface = new Surface(surface);

            AffineTransform af = new AffineTransform();

            af.translate((bounds.x + bounds.width / 2) * -1, (bounds.y + bounds.height / 2) * -1);

//            groutedSurface.resize(bounds.width - groutThickness, bounds.height - groutThickness);
            return groutedSurface;
        }
        return null;
    }

    private void buildPattern(@NotNull Surface surface, @NotNull Path2D.Double cutting) {
        final Vector<Tile> tiles = pattern.getTiles();
        final double grout = surface.getGrout().getThickness();

        final double tileH = tiles.get(0).getBounds().height;
        final double tileW = tiles.get(0).getBounds().width;

        // Pattern min - max
        final double patMinX = surface.getBounds().x;
        final double patMaxX = patMinX + surface.getBounds().width + tileW;
        final double patMinY = surface.getBounds().y;
        final double patMaxY = patMinY + surface.getBounds().height + tileH;

        // Defining shifting values
        final double shiftX = (pattern.isShiftable() && !shiftDirection) ? shift : 0;
        final double shiftY = (pattern.isShiftable() && shiftDirection) ? shift : 0;

        int cuttedTiles = 0;
        if (patternOrigin == null) {
            patternOrigin = new Vector2D(0, 0);
        }
        Vector2D origin = transformOrigin(patternOrigin, surface);
        int iY = -1;

        double x = origin.x + .5;
        double y = origin.y + .5;
        while (y <= patMaxY) {
            ++iY;
            int iX = -1;

            if (iY % 2 == 0)
                x -= shiftX;

            while (x <= patMaxX) {
                ++iX;
                boolean isCutted = false;
                double tempX = x;
                double tempY = y;

                if (iX % 2 == 0)
                    tempY -= shiftY;
                for (int i = 0; i < tiles.size(); ++i) {
                    Tile tile = tiles.get(i);
                    Tile newTile = tile.copy();
                    Rectangle2D.Double pos = newTile.getBounds();

                    newTile.moveOf(-pos.x, -pos.y);
                    newTile.moveOf(tempX + (pos.x * pos.width), tempY + (pos.y * pos.height));
                    if (tileWillBeCuted(newTile, surface.getPolygon())) {
                        isCutted = true;
                        ++cuttedTiles;
                    }
                    Path2D.Double[] cuttedSurface = PolygonTransformer.poopSubtract(newTile.getPolygon(), surface.getPolygon());
                    if (cuttedSurface != null && cuttedSurface.length != 0) {
                        if (cuttedSurface.length == 1 && PolygonTransformer.isContaining(surface.getPolygon(), cuttedSurface[0])) {
                            newTile.setPolygon(cuttedSurface[0]);
                            newTile.setCutted(isCutted);
                            searchImpossibleCut(newTile);
                            this.tiles.add(newTile);
                        } else {
                            for (Path2D.Double cut : cuttedSurface) {
                                if (!PolygonTransformer.isContaining(surface.getPolygon(), cut))
                                    continue;
                                newTile = tile.copy();
                                newTile.setPolygon(cut);
                                newTile.setCutted(isCutted);
                                searchImpossibleCut(newTile);
                                this.tiles.add(newTile);
                            }
                        }
                    } else if (isCutted) {
                        --cuttedTiles;
                    }
                    // Calculate if grout should be applied here or outside the loop
                    tempX += (pattern.getGroutXRules()[i] * grout);
                    tempY += (pattern.getGroutYRules()[i] * grout);
                }
                x += (tileW * pattern.getOffsetX());
                x += (grout * pattern.getOffsetX());
            }

            x = origin.x + .5d;
            y += (tileH * pattern.getOffsetY());
            y += (grout * pattern.getOffsetY());
        }
        this.cuttedTiles = cuttedTiles;
    }

    private void searchImpossibleCut(Tile tile) {
        double[] seg = new double[6];
        boolean stop = false;
        Vector2D start = new Vector2D();
        Vector2D a = new Vector2D();
        Vector2D b = new Vector2D();

        for (PathIterator pi = tile.getPolygon().getPathIterator(null); !pi.isDone() && !stop; pi.next()) {
            switch (pi.currentSegment(seg)) {
                case PathIterator.SEG_MOVETO:
                    start.x = seg[0];
                    start.y = seg[1];
                    b = start.copy();
                    break;
                case PathIterator.SEG_LINETO:
                    b.x = seg[0];
                    b.y = seg[1];

                    if (Math.abs(b.copy().subtract(a).magnitude()) <= tile.getMinimalCut()) {
                        stop = true;
                        tile.setImpossibleCut(true);
                    }
                    break;
                default:
                    break;
            }
            a = b.copy();
        }
    }

    private boolean tileWillBeCuted(PrimarySurface tile, Path2D.Double surface) {
        Point2D[] vertices = tile.getVertices();

        for (Point2D vertice : vertices) {
            if (!surface.contains(vertice))
                return true;
        }
        return false;
    }

    private Vector2D transformOrigin(Vector2D origin, Surface surface) {
        Rectangle2D.Double patBounds = getPatternBounds();
        Rectangle2D.Double surBounds = surface.getBounds();
        final double grout = surface.getGrout().getThickness();
        Vector2D ret = new Vector2D(origin);

        ret.x += surBounds.x;
        ret.y += surBounds.y;
        ret.x -= patBounds.width / 2;
        ret.y -= patBounds.height / 2;
        while (ret.x > surBounds.x) {
            ret.x -= grout * 2 + patBounds.width * 2;
        }
        while (ret.y > surBounds.y) {
            ret.y -= grout * 2 + patBounds.height * 2;
        }

        return ret;
    }

    private Rectangle2D.Double getPatternBounds() {
        Rectangle2D.Double ret = new Rectangle2D.Double();
        Iterator<Tile> it = pattern.getTiles().iterator();

        if (!it.hasNext()) {
            return ret;
        }

        Tile tile = it.next();
        ret = tile.getBounds();

        while (it.hasNext()) {
            tile = it.next();
            Rectangle2D.Double bounds = tile.getBounds();
            final double tx = bounds.x * bounds.width;
            final double ty = bounds.y * bounds.height;
            final double tw = bounds.x * bounds.width + bounds.width;
            final double th = bounds.y * bounds.height + bounds.height;

            if (ret.x > bounds.x)
                ret.x = bounds.x;
            if (ret.y > bounds.y)
                ret.y = bounds.y;
            if (ret.x + ret.width > bounds.x + bounds.width)
                ret.width += (bounds.x + bounds.width) - (ret.x + ret.width);
            if (ret.y + ret.height > bounds.y + bounds.height)
                ret.height += (bounds.y + bounds.height) - (ret.y + ret.height);
        }

        return ret;
    }

    public PatternGroup copy() {
        return new PatternGroup(this);
    }

    public Vector<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(Vector<Tile> tiles) {
        this.tiles = tiles;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    public void setOrigin(double x, double y) {
        if (patternOrigin == null) {
            patternOrigin = new Vector2D(0, 0);
        }

        if (!Double.isNaN(x))
            patternOrigin.x = x;
        if (!Double.isNaN(y))
            patternOrigin.y = y;
    }

    public int getCuttedTiles() {
        return cuttedTiles;
    }

    public Vector2D getOrigin() {
        return patternOrigin;
    }

    public void moveOrigin(double x, double y) {
        if (!Double.isNaN(x))
            patternOrigin.x += x;
        if (!Double.isNaN(y))
            patternOrigin.y += y;
    }

    public double getShift() {
        return shift;
    }

    public void setShift(double shift) {
        this.shift = shift;
    }

    public boolean getShiftDirection() {
        return shiftDirection;
    }

    public void setShiftDirection(boolean shiftDirection) {
        this.shiftDirection = shiftDirection;
    }
}
