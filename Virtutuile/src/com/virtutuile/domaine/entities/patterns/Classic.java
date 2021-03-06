package com.virtutuile.domaine.entities.patterns;

import com.virtutuile.domaine.entities.surfaces.Tile;

import java.awt.geom.Rectangle2D;

/**
 * @see Pattern For more informations about pattern configuration
 */
public class Classic extends Pattern {
    public Classic(Tile tile) {
        super("Classic");
        adjustXRules = new double[]{0};
        adjustYRules = new double[]{0};
        groutXRules = new double[]{0};
        groutYRules = new double[]{0};
        shiftable = true;
        offsetX = 1;
        offsetY = 1;
        if (tile != null) {
            tiles.add(new Tile(new Rectangle2D.Double(0,0, tile.getBounds().getWidth(), tile.getBounds().getHeight())));
            tiles.get(0).setFillColor(tile.getFillColor());
        } else {
            tiles.add(new Tile(new Rectangle2D.Double(0,0, defaultDimensions.getX(), defaultDimensions.getY())));
        }
    }

    @Override
    public void setTileType(Tile tile) {
        tiles.clear();
        tiles.add(tile.copy());
//        tiles.get(0).setFillColor(tile.getFillColor());
    }
}
