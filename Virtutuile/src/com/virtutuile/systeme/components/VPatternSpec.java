package com.virtutuile.systeme.components;

import com.virtutuile.systeme.units.VCoordinates;

import java.util.Vector;

public class VPatternSpec {
    private VPattern _pattern;
    private Vector<VTileSpec> _tiles;
    private float _rotation;
    private VCoordinates _coordinate;

    public VPattern pattern() {
        return this._pattern;
    }

    public void pattern(VPattern pattern) {
        this._pattern = pattern;
    }

    public Vector<VTileSpec> tiles() {
        return this._tiles;
    }

    public void tiles(Vector<VTileSpec> tiles) {
        this._tiles = tiles;
    }

    public float rotation() {
        return this._rotation;
    }

    public void rotation(float rotation) {
        this._rotation = rotation;
    }

    public VCoordinates coordinate() {
        return this._coordinate;
    }

    public void coordinate(VCoordinates coordinate) {
        this._coordinate = coordinate;
    }
}