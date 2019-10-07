package com.virtutuile.graphics.components.buttons;

import com.virtutuile.constants.UIConstants;
import com.virtutuile.graphics.wrap.ImageManipulator;
import com.virtutuile.graphics.wrap.VPanelEvents;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

public class VButton extends VPanelEvents implements MouseListener {
    private ImageIcon _icon;
    private JLabel _imgContainer;
    private JLabel _text;

    /**
     * Build a VButton. It's a combo of an icon and a text.
     *
     * @param name     The name of the button
     * @param fileicon The icon of the button
     */
    public VButton(String name, File fileicon) {
        this.setBackground(UIConstants.TOOLBAR_BACKGROUND);
        this.fixSize(UIConstants.BUTTON_SIZE);

        // Creating the label and style it
        _text = new JLabel(name, SwingConstants.CENTER);
        Border textMargin = new EmptyBorder(3, 10, 10, 10);
        _text.setBorder(new CompoundBorder(_text.getBorder(), textMargin));
        _text.setForeground(UIConstants.TOOLBAR_FONT_COLOR);

        // Creating the image and style it
        try {
            _icon = ImageManipulator.Resize(new ImageIcon(ImageIO.read(fileicon)), UIConstants.BUTTON_ICON_SIZE);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        _imgContainer = new JLabel(_icon);
        Border imgMargin = new EmptyBorder(10, 10, 3, 10);
        _imgContainer.setBorder(new CompoundBorder(_imgContainer.getBorder(), imgMargin));

        // Setting the layout of the button and add it's components
        this.setLayout(new BorderLayout());
        this.add(_imgContainer, BorderLayout.CENTER);
        this.add(_text, BorderLayout.SOUTH);
    }

    // region mouse button colors
    @Override
    public void mousePressed(MouseEvent me) {
        super.mousePressed(me);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        super.mouseReleased(me);
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        super.mouseEntered(me);
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent me) {
        super.mouseExited(me);
        repaint();
    }
    // endregion

    /**
     * Force the button to be this size
     *
     * @param size The desired size
     */
    public void fixSize(Dimension size) {
        this.setMaximumSize(size);
        this.setPreferredSize(size);
        this.setMinimumSize(size);
    }

    @Override
    public void paint(Graphics g) {
        if (this._isMouseActive || this._isClicked) {
            setBackground(UIConstants.BUTTON_ACTIVE_COLOR);
        } else if (this._isMouseOver) {
            setBackground(UIConstants.BUTTON_HOVER_COLOR);
        } else {
            setBackground(UIConstants.BUTTON_NORMAL_COLOR);
        }
        super.paint(g);
    }
}
