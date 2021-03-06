package com.virtutuile.afficheur.inputs;

import com.virtutuile.afficheur.swing.Label;
import com.virtutuile.afficheur.tools.ValidationsException;
import com.virtutuile.domaine.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

public class UnitInput extends TextInput {

    int valueInt;
    Double valueDouble;
    Label unitLabel = new Label();
    Constants.Units unit;

    /*public UnitInput(String label) {
        this(label, true);
    }

    public UnitInput(String label, boolean horizontalFlex) {
        super(label, horizontalFlex, null);
        setUnit(Constants.Units.Centimeter);
        unitLabel.setPreferredSize(new Dimension(50, unitLabel.getPreferredSize().height));
        unitLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fieldBorder.add(unitLabel, BorderLayout.EAST);
        revalidate();
        repaint();
    }*/

    public UnitInput(String label, boolean horizontalFlex, String validator) {
        super(label, horizontalFlex, validator);
        setUnit(Constants.Units.Centimeter);
        unitLabel.setPreferredSize(new Dimension(50, unitLabel.getPreferredSize().height));
        unitLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fieldBorder.add(unitLabel, BorderLayout.EAST);
        revalidate();
        repaint();
    }

    public int getValue() {
        return valueInt;
    }

    public UnitInput setValue(int value) {
        this.valueInt = value;
        field.setText(String.valueOf(this.valueInt));
        repaint();
        return this;
    }

    public UnitInput setValue(Double value) {

        this.valueDouble = value;
        field.setText(String.valueOf(this.valueDouble));
        repaint();
        return this;
    }

    public Constants.Units getUnit() {
        return unit;
    }

    public void setUnitLabel(String unitLabel) {
        this.unitLabel.setText(unitLabel);
    }

    public UnitInput setUnit(Constants.Units unit) {
        this.unit = unit;
        this.unitLabel.setText(Constants.UnitLabels.get(unit));
        repaint();
        return this;
    }

    public void switchUnit(String unit) {
        if ("Imperial".equals(unit)) {
            if (!unitLabel.getText().equals("cm") && !unitLabel.getText().equals("in")) {
                setUnitLabel("in to 0");
            } else {
                setUnitLabel("in");
            }
        } else {
            if (!unitLabel.getText().equals("in") && !unitLabel.getText().equals("cm")) {
                setUnitLabel("cm to 0");
            } else {
                setUnitLabel("cm");
            }
        }
    }

}
