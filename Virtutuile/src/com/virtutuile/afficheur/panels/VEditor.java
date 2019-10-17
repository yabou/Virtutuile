package com.virtutuile.afficheur.panels;

import com.virtutuile.afficheur.swing.panels.MouseEventKind;
import com.virtutuile.afficheur.swing.panels.VPanelEvents;
import com.virtutuile.moteur.VEditorEngine;
import com.virtutuile.systeme.constants.UIConstants;

import java.awt.*;

public class VEditor extends VPanelEvents {
    Point p;
    private VEditorEngine _editorEngine;

    public VEditor(VEditorEngine editorEngine) {
        this.setBackground(UIConstants.DRAW_BACKGROUND);
        this.setName("Toolbar");
        this.setBorder(null);
        this._editorEngine = editorEngine;

        addEventListener(MouseEventKind.MousePress, (me) -> {
            /*editorEngine.mouseLClick(me.getPoint());*/
            repaint();
        });
        ;

        addEventListener(MouseEventKind.MouseMove, (me) -> {
            /*editorEngine.setMouse(me.getPoint());*/
            repaint();
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        /*this._editorEngine.paint(g);*/
    }
}