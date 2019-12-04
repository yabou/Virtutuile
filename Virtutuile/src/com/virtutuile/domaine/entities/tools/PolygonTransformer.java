package com.virtutuile.domaine.entities.tools;

import com.virtutuile.shared.NotNull;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Iterator;
import java.util.Vector;

public class PolygonTransformer {

    static Path awtPathToJavafx(java.awt.geom.Path2D path) {
        // Making a path with all PathElement
        Path ret = new Path();
        ret.setStrokeWidth(0.001);
        ret.setStrokeType(StrokeType.INSIDE);
        ret.setFill(Color.RED);

        double[] coords = new double[6];
        for (PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {
            switch (pi.currentSegment(coords)) {
                case PathIterator.SEG_CUBICTO:
                    ret.getElements().add(new CubicCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]));
                    break;
                case PathIterator.SEG_QUADTO:
                    ret.getElements().add(new QuadCurveTo(coords[0], coords[1], coords[2], coords[3]));
                    break;
                case PathIterator.SEG_MOVETO:
                    ret.getElements().add( new MoveTo(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_LINETO:
                    ret.getElements().add(new LineTo(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_CLOSE:
                    ret.getElements().add(new ClosePath());
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
        return ret;
    }

    static java.awt.geom.Path2D.Double javafxPathToAwt(Path path) {
        java.awt.geom.Path2D.Double ret = new java.awt.geom.Path2D.Double();

        Iterator<PathElement> it = path.getElements().iterator();
        if (!it.hasNext())
            return null;
        PathElement pe = it.next();
        ret.moveTo(((MoveTo) pe).getX(), ((MoveTo) pe).getY());
        for (; it.hasNext(); pe = it.next()) {
            if (pe instanceof LineTo)
                ret.lineTo(((LineTo) pe).getX(), ((LineTo) pe).getY());
            if (pe instanceof ClosePath)
                break;
        }
        ret.closePath();
        return ret;
    }

    static @NotNull Path2D.Double[] javafxPathsToAwt(Path path) {
        Vector<Path2D.Double> ret = new Vector<>(2);

        Iterator<PathElement> it = path.getElements().iterator();
        if (!it.hasNext())
            return null;
        Path2D.Double p = new Path2D.Double();
        for (PathElement pe = null; it.hasNext(); ) {
            pe = it.next();
            if (pe instanceof MoveTo)
                p.moveTo(((MoveTo) pe).getX(), ((MoveTo) pe).getY());
            else if (pe instanceof LineTo)
                p.lineTo(((LineTo) pe).getX(), ((LineTo) pe).getY());
            else if (pe instanceof ClosePath) {
                p.closePath();
                ret.add(p);
                p = new Path2D.Double();
            }
        }
        return ret.toArray(new Path2D.Double[0]);
    }

    static public Path2D.Double[] subtract(java.awt.geom.Path2D polygon, java.awt.geom.Path2D cuttingPattern, double cuttingInline) {
        Path poly = awtPathToJavafx(polygon);
        Path cut = awtPathToJavafx(cuttingPattern);

        Shape shape = Shape.intersect(poly, cut);
        return javafxPathsToAwt((Path) shape);
    }

    static public Path2D.Double[] merge(java.awt.geom.Path2D polygon1, java.awt.geom.Path2D polygon2) {
        Path poly1 = awtPathToJavafx(polygon1);
        Path poly2 = awtPathToJavafx(polygon2);

        return javafxPathsToAwt((Path) Shape.union(poly1, poly2));
    }
}
