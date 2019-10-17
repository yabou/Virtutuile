package com.virtutuile.afficheur.panels.subpanel;

import com.virtutuile.afficheur.inputs.VMetricInputText;
import com.virtutuile.systeme.constants.UIConstants;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.Vector;

public class SettingsPanel extends SubPanel {
    private VMetricInputText _areaInput = new VMetricInputText("Area");
    private VMetricInputText _perimeterInput = new VMetricInputText("Perimeter");
    private VMetricInputText _width = new VMetricInputText("Width");
    private VMetricInputText _height = new VMetricInputText("Height");
    private Vector<JPanel> _lines = new Vector<>();

    public SettingsPanel(String name) {
        super(name);
        TitledBorder border = new TitledBorder(name);
        border.setTitleColor(UIConstants.EDITIONPANEL_FONT_COLOR);
        border.setTitleJustification(TitledBorder.LEFT);
        border.setTitlePosition(TitledBorder.TOP);
        this.setBorder(border);

        JPanel line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
        line.add(_areaInput);
        line.add(_perimeterInput);
        _lines.add(line);

        line = new JPanel();
        line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
        line.add(_width);
        line.add(_height);
        _lines.add(line);

        persistLayout();
    }

    private void persistLayout() {
        for (JPanel layout : _lines) {
            if (!isAncestorOf(layout)) {
                add(layout);
            }
        }
    }
}