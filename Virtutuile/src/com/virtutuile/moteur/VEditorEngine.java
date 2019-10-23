package com.virtutuile.moteur;

import com.virtutuile.moteur.interfaces.IVEditorManager;
import com.virtutuile.moteur.managers.VPainterManager;
import com.virtutuile.moteur.managers.VPatternEditorManager;
import com.virtutuile.moteur.managers.VShapeEditorManager;
import com.virtutuile.systeme.components.VDrawableShape;
import com.virtutuile.systeme.interfaces.IVGraphics;
import com.virtutuile.systeme.singletons.VActionStatus;
import com.virtutuile.systeme.tools.UnorderedMap;
import com.virtutuile.systeme.units.VCoordinate;
import com.virtutuile.systeme.units.VProperties;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;

public class VEditorEngine {

    private VCoordinate _clicked = null;

    public VEditorEngine() {super();}

    private UnorderedMap<VActionStatus.VActionManager, IVEditorManager> _managers = new UnorderedMap<>() {{
        put(VActionStatus.VActionManager.Pattern, new VPatternEditorManager());
        put(VActionStatus.VActionManager.Shape, new VShapeEditorManager());
    }};

    public void paint(VPainter graphics) {
        _managers.forEach((action, manager) -> {
            List<VDrawableShape> shapes = manager.getDrawableShapes();
            shapes.forEach(graphics::paint);
        });
    }

    public void mouseHover(VCoordinate coordinates){
        _managers.forEach((action, manager) -> {
            manager.mouseHover(coordinates);
        });

    }

    public void mouseLClick(VProperties properties) {
        VActionStatus.VActionManager manager = VActionStatus.VActionStatus().manager;
        this._managers.get(manager).mouseLClick(properties);
        this._clicked = properties.coordinates.get(0);
    }

    public void mouseRClick(VCoordinate coordinates) {

    }

    public void mouseDrag(VCoordinate point) {
        VShapeEditorManager manager =  (VShapeEditorManager)_managers.get(VActionStatus.VActionManager.Shape);

        manager.moveShape(_clicked, point);
        _clicked = point;
    }

    public void keyEvent(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_BACK_SPACE || ke.getKeyCode() == KeyEvent.VK_DELETE) {

            VShapeEditorManager manager =  (VShapeEditorManager)_managers.get(VActionStatus.VActionManager.Shape);
            manager.deleteSelectedShape();

            //TODO -> Revérifie les liens entre les shapes et les patterns.
            ((VPatternEditorManager)_managers.get(VActionStatus.VActionManager.Pattern)).resync();
        }
    }
}
