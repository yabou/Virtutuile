package com.virtutuile.afficheur.panels.subEdition;

import com.virtutuile.afficheur.MainWindow;
import com.virtutuile.afficheur.inputs.Button;
import com.virtutuile.afficheur.inputs.TextInput;
import com.virtutuile.afficheur.inputs.UnitInput;
import com.virtutuile.afficheur.swing.Panel;
import com.virtutuile.afficheur.swing.events.InputEventKind;
import com.virtutuile.afficheur.swing.events.MouseEventKind;
import com.virtutuile.afficheur.tools.AssetLoader;
import com.virtutuile.afficheur.tools.ValidationsException;
import com.virtutuile.domaine.Controller;
import com.virtutuile.shared.UnorderedMap;

import javax.swing.*;
import java.util.function.BiConsumer;

public class SurfacePanel extends SubPanel {

    private UnorderedMap<DrawShapeButtonType, Button> addSurface = new UnorderedMap<>();
    private UnorderedMap<DrawShapeButtonType, Button> removeSurface = new UnorderedMap<>();
    private UnorderedMap<InputContextType, UnitInput> inputs = new UnorderedMap<>();

    public SurfacePanel(String name, MainWindow mainWindow) {
        super(name, mainWindow);
        setButtonsOnPanel();
        setEvents();
        persistLayout();
    }

    @Override
    protected void setEvents() {
        Button addRectangularSurface = addSurface.get(DrawShapeButtonType.AddRectangularSurface);

        addRectangularSurface.addMouseEventListener(MouseEventKind.MouseLClick, (event) -> {
            if (addRectangularSurface.isActive()) {
                mainWindow.getController().setDrawRectangularSurface(false);
                addRectangularSurface.setActive(false);
            } else {
                mainWindow.getController().setDrawRectangularSurface(true);
                addRectangularSurface.setActive(true);
            }
        });

        inputs.get(InputContextType.Width).addInputListener(InputEventKind.OnChange, (value, self) -> {
            mainWindow.getController().setSurfaceWidth(Double.parseDouble(value));
            mainWindow.getController().recalcPattern();
            mainWindow.repaint();
        });

        inputs.get(InputContextType.Height).addInputListener(InputEventKind.OnChange, (value, self) -> {
            mainWindow.getController().setSurfaceHeight(Double.parseDouble(value));
            mainWindow.getController().recalcPattern();
            mainWindow.repaint();
        });

    }

    protected void setButtonsOnPanel() {
        JPanel line = new Panel();
        line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
        setDrawShapesButtons(line);

        line = new Panel();
        line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
        setRemoveShapesButtons(line);

        line = new Panel();
        line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
        setInputsOnPanel(line);
    }

    private void setDrawShapesButtons(JPanel line) {
        addSurface.put(DrawShapeButtonType.AddRectangularSurface, new Button("Rectangle", AssetLoader.loadImage("/icons/shape-edit-add-square.png")));
        addSurface.put(DrawShapeButtonType.AddFreeSurface, new Button("Free", AssetLoader.loadImage("/icons/shape-edit-add-free.png")));

        addSurface.forEach((key, value) -> {
            line.add(value);
        });

        rows.add(line);
    }

    private void setRemoveShapesButtons(JPanel line) {
        removeSurface.put(DrawShapeButtonType.RemoveRectangularSurface, new Button("Rectangle", AssetLoader.loadImage("/icons/shape-edit-remove-square.png")));
        removeSurface.put(DrawShapeButtonType.RemoveFreeSurface, new Button("Free", AssetLoader.loadImage("/icons/shape-edit-remove-free.png")));

        removeSurface.forEach((key, value) -> {
            line.add(value);
        });
        rows.add(line);
    }

    private void setInputsOnPanel(JPanel line) {
        inputs.put(InputContextType.Width, new UnitInput("Width"));
        inputs.put(InputContextType.Height, new UnitInput("Height"));

        inputs.forEach((key, value) -> {
            line.add(value);
        });

        rows.add(line);
    }

    public void retrieveSurfaceDimensions() {
        Double[] selectedSurfaceDimensions = mainWindow.getController().getSelectedSurfaceDimensions();
        if (selectedSurfaceDimensions != null) {
            inputs.get(InputContextType.Width).setText(selectedSurfaceDimensions[0].toString());
            inputs.get(InputContextType.Height).setText(selectedSurfaceDimensions[1].toString());
        } else {
            setSurfaceDimensions(0.0,0.0);
        }
    }

    private void setSurfaceDimensions(Double width, Double height) {
        inputs.get(InputContextType.Width).setText(width.toString());
        inputs.get(InputContextType.Height).setText(height.toString());
    }

    public enum DrawShapeButtonType {
        AddRectangularSurface,
        AddFreeSurface,
        RemoveRectangularSurface,
        RemoveFreeSurface,
    }

    public enum InputContextType {
        Width,
        Height
    }
}
