package com.virtutuile.afficheur.panels;

import com.virtutuile.afficheur.inputs.Button;
import com.virtutuile.afficheur.swing.BorderedPanel;
import com.virtutuile.afficheur.swing.events.MouseEventKind;
import com.virtutuile.afficheur.tools.AssetLoader;
import com.virtutuile.domaine.Controller;
import com.virtutuile.shared.UnorderedMap;

import javax.swing.*;

public class BottomToolbar extends BorderedPanel {

    private Controller controller;
    private UnorderedMap<TargetButton, Button> buttons = new UnorderedMap<>();
    //private hoveredSurface

    public BottomToolbar(Controller controller) {
        super();
        this.controller = controller;

        sizingPolicy(SizingPolicy.ContentBox);

        Button button = new Button("Show Bounds");
        buttons.put(TargetButton.ShowBounds, button);
        buttons.put(TargetButton.MagneticGrid, new Button("Magnetic grid", AssetLoader.loadImage("/icons/magnetic-grid.png")));

        buttons.forEach((key, value) -> {
            add(value);
        });
        setEvent();
        add(Box.createHorizontalGlue());

//        JLabel label = setUpLabel();
//        if (label != null) {
//            add(label);
//            repaint();
//        }
        add(Box.createVerticalGlue());
    }

    private void setEvent() {
        Button buttonMG = buttons.get(TargetButton.MagneticGrid);

        buttonMG.addMouseEventListener(MouseEventKind.MouseLClick, (event) -> {
            controller.drawGrid();
            repaint();
        });
    }

    public Button getButton(TargetButton name) {
        return buttons.get(name);
    }

//    public void setSurfaceBounds() {
//        controller.getHoveredSurfaceBounds();
//    }

    public enum TargetButton {
        ShowBounds,
        MagneticGrid
    }

//    private JLabel setUpLabel() {
//        Surface hoveredSurface = controller.getHoveredSurface();
//        if (hoveredSurface != null) {
//            JLabel label = new JLabel("width", SwingConstants.CENTER);
//            return label;
//        }
//        return null;
//    }
}
