package com.virtutuile.domaine.moteur;

import com.virtutuile.domaine.moteur.interfaces.IVEditorManager;
import com.virtutuile.domaine.moteur.managers.VPatternEditorManager;
import com.virtutuile.domaine.moteur.managers.VShapeEditorManager;
import com.virtutuile.domaine.systeme.components.VDrawableShape;
import com.virtutuile.domaine.systeme.singletons.VApplicationStatus;
import com.virtutuile.domaine.systeme.tools.UnorderedMap;
import com.virtutuile.domaine.systeme.units.VCoordinate;
import com.virtutuile.domaine.systeme.units.VProperties;

import java.awt.event.KeyEvent;
import java.util.List;

public class VEditorEngine {

    private VCoordinate _clicked = null;
    private VCoordinate _hover = null;

    public VEditorEngine() {super();}

    private UnorderedMap<VApplicationStatus.VActionManager, IVEditorManager> _managers = new UnorderedMap<>() {{
        put(VApplicationStatus.VActionManager.Pattern, new VPatternEditorManager());
        put(VApplicationStatus.VActionManager.Shape, new VShapeEditorManager());
    }};

    public void paint(VPainter graphics) {
        _managers.forEach((action, manager) -> {
            List<VDrawableShape> shapes = manager.getDrawableShapes();
            shapes.forEach(graphics::paint);
        });
    }

    public void mouseHover(VCoordinate coordinates){
        _hover = coordinates;
        _managers.forEach((action, manager) -> {
            manager.mouseHover(coordinates);
        });

    }

    public void mouseRelease(VCoordinate coordinate) {
        _hover = coordinate;
        _managers.forEach((action, manager) -> {
            manager.mouseRelease(coordinate);
        });
    }

    public void mouseLClick(VProperties properties) {
        VApplicationStatus.VActionManager manager = VApplicationStatus.getInstance().manager;
        this._managers.get(manager).mouseLClick(properties);
        this._clicked = properties.coordinates.get(0);
    }

    public void mouseRClick(VCoordinate coordinates) {

    }

    public void mouseDrag(VCoordinate coordinates) {
        VShapeEditorManager manager =  (VShapeEditorManager)_managers.get(VApplicationStatus.VActionManager.Shape);

        manager.mouseDrag(_hover, coordinates);
        _hover = coordinates;
    }

    public void keyEvent(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_BACK_SPACE || ke.getKeyCode() == KeyEvent.VK_DELETE) {

            VShapeEditorManager manager =  (VShapeEditorManager)_managers.get(VApplicationStatus.VActionManager.Shape);
            manager.deleteSelectedShape();

            //TODO VPatternEditorManager::resync -> Revérifie les liens entre les shapes et les patterns.
            ((VPatternEditorManager)_managers.get(VApplicationStatus.VActionManager.Pattern)).resync();
        }
    }
}
