package com.virtutuile.afficheur.swing.panels;


import javax.swing.event.MouseInputListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Vector;
import java.util.function.Consumer;

public class VBorderedEventPanel extends VBorderedPanel implements MouseInputListener, MouseMotionListener, KeyListener {

    protected boolean _isClicked = false;
    protected boolean _isMouseActive = false;
    protected boolean _isMouseHover = false;

    HashMap<MouseEventKind, Vector<Consumer<MouseEvent>>> _mouseEvents = new HashMap<>();
    HashMap<KeyboardEventKind, Vector<Consumer<KeyEvent>>> _keyboardEvents = new HashMap<>();

    public VBorderedEventPanel() {
        super();
        _border.addMouseListener(this);
        _border.addKeyListener(this);
        _border.addMouseMotionListener(this);
    }

    public boolean isActive() {
        return this._isMouseActive;
    }

    public void setActive(boolean isActive) {
        this._isMouseActive = isActive;
    }

    /**
     * Adds a listener to a specific event.
     *
     * @param event The event to add
     * @param cb    The event to call
     */
    public void addMouseEventListener(MouseEventKind event, Consumer<MouseEvent> cb) {

        if (_mouseEvents.containsKey(event)) {
            for (Vector<Consumer<MouseEvent>> pair : _mouseEvents.values()) {
                pair.add(cb);
            }
        } else {
            Vector<Consumer<MouseEvent>> vector = new Vector<>();

            vector.add(cb);
            _mouseEvents.put(event, vector);
        }
    }

    /**
     * Adds a listener to a specific event.
     *
     * @param event The event to add
     * @param cb    The event to call
     */
    public void addKeyboardEventListener(KeyboardEventKind event, Consumer<KeyEvent> cb) {

        if (_keyboardEvents.containsKey(event)) {
            for (Vector<Consumer<KeyEvent>> pair : _keyboardEvents.values()) {
                pair.add(cb);
            }
        } else {
            Vector<Consumer<KeyEvent>> vector = new Vector<>();

            vector.add(cb);
            _keyboardEvents.put(event, vector);
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
        _isClicked = true;
        if (me.getButton() == MouseEvent.BUTTON1) {
            invokeMouseEvent(MouseEventKind.MouseLClick, me);
        } else if (me.getButton() == MouseEvent.BUTTON2) {
            invokeMouseEvent(MouseEventKind.MouseRClick, me);
        }
        invokeMouseEvent(MouseEventKind.MousePress, me);
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        _isClicked = false;
        invokeMouseEvent(MouseEventKind.MouseRelease, me);
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        invokeMouseEvent(MouseEventKind.MouseEnter, me);
        _isMouseHover = true;
    }

    @Override
    public void mouseExited(MouseEvent me) {
        invokeMouseEvent(MouseEventKind.MouseLeave, me);
        _isMouseHover = false;
    }

    private void invokeMouseEvent(MouseEventKind mek, MouseEvent me) {
        if (_mouseEvents.containsKey(mek)) {
            for (Consumer<MouseEvent> cb : _mouseEvents.get(mek)) {
                cb.accept(me);
            }
        }
    }

    private void invokeKeyboardEvent(KeyboardEventKind kek, KeyEvent ke) {
        if (_keyboardEvents.containsKey(kek)) {
            for (Consumer<KeyEvent> cb : _keyboardEvents.get(kek)) {
                cb.accept(ke);
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        invokeMouseEvent(MouseEventKind.MouseDrag, me);
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        invokeMouseEvent(MouseEventKind.MouseMove, me);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        invokeKeyboardEvent(KeyboardEventKind.KeyTyped, e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        invokeKeyboardEvent(KeyboardEventKind.KeyPressed, e);

    }

    @Override
    public void keyReleased(KeyEvent e) {
        invokeKeyboardEvent(KeyboardEventKind.KeyReleased, e);

    }
}
