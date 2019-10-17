package com.virtutuile.systeme.components;

import java.awt.*;

public class VShape {
    private Rectangle _bounds;
    private boolean _hole;
    private Polygon _polygon;
    private Color _borderColor;
    private Color _fillColor;

    public VShape(Rectangle bounds, boolean hole, Polygon polygon) {
        this._bounds = bounds;
        this._hole = hole;
        this._polygon = polygon;
    }

    public Rectangle bounds() {
        return this._bounds;
    }

    public void bounds(Rectangle bounds) {
        this._bounds = bounds;
    }

    public boolean isHole() {
        return this._hole;
    }

    public void hole(boolean hole) {
        this._hole = hole;
    }

    public Polygon polygon() {
        return this._polygon;
    }

    public void polygon(Polygon polygon) {
        this._polygon = polygon;
    }

    public Color borderColor() {
        return this._borderColor;
    }

    public void borderColor(Color borderColor) {
        this._borderColor = borderColor;
    }

    public Color fillColor() {
        return this._fillColor;
    }

    public void fillColor(Color fillColor) {
        this._fillColor = fillColor;
    }
}