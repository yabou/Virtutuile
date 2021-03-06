package com.virtutuile.afficheur.panels;

import com.virtutuile.afficheur.Constants;
import com.virtutuile.afficheur.MainWindow;
import com.virtutuile.afficheur.panels.subEdition.*;
import com.virtutuile.afficheur.swing.PanelEvents;
import com.virtutuile.domaine.entities.surfaces.Surface;
import com.virtutuile.shared.UnorderedMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditionPanel extends PanelEvents {

    private MainWindow mainWindow;
    private TileSettingsPanel tileSettingsPanel;
    private InfoPanel infoPanel;
    private PatternPanel patternPanel;
    private UnorderedMap<PanelType, SubPanel> subPanels;

    public EditionPanel(MainWindow mainWindow) {
        super();
        this.mainWindow = mainWindow;
        tileSettingsPanel = new TileSettingsPanel("Tile Settings", mainWindow);
        infoPanel = new InfoPanel("Information", mainWindow);
        patternPanel = new PatternPanel("Pattern", mainWindow);

        subPanels = new UnorderedMap<PanelType, SubPanel>() {{
            put(PanelType.Surface, new SurfacePanel("Surface", mainWindow));
            put(PanelType.AdvancedManagement, new ManagementPanel("Advanced Management", mainWindow));
            put(PanelType.Grout, new GroutPanel("Grout", mainWindow));
            put(PanelType.Tile, new TilePanel("Tile", mainWindow));
            put(PanelType.Pattern, patternPanel);
        }};
        subPanels.forEach((key, value) -> {
            add(value, key);
        });

        setOpaque(true);
        setName("Edition Panel");
        setBackground(Constants.EDITIONPANEL_BACKGROUND);
        setForeground(Constants.EDITIONPANEL_FONT_COLOR);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setPreferredSize(new Dimension(600, 2500));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    }

    public void surfaceGetSurfaceProperties() {
        SurfacePanel surface = (SurfacePanel) subPanels.get(PanelType.Surface);
        if (surface != null) {
            surface.retrieveSelectedSurfaceProperties();
        }
    }

    public void surfaceGetGroutThickness() {
        GroutPanel grout = (GroutPanel) subPanels.get(PanelType.Grout);
        if (grout != null) {
            grout.retrieveGroutThickness();
        }
    }

    public void removeAllSurfacePanels() {
        subPanels.forEach((key, value) -> {
            remove(value);
        });
    }

    public void addAllPanels() {
        subPanels.forEach((key, value) -> {
            add(value);
        });
    }

    public UnorderedMap<PanelType, SubPanel> getSubPanels() {
        return subPanels;
    }

    public void addTileSettingsPanel() {
        add(tileSettingsPanel);
    }

    public void removeTileSettingsPanel() {
        remove(tileSettingsPanel);
    }

    public void addInfoPanel() {
        add(infoPanel);
    }

    public void removeInfoPanel() {
        remove(infoPanel);
    }

    public TileSettingsPanel getTileSettingsPanel() {
        return tileSettingsPanel;
    }

    public PatternPanel getPatternPanel() {
        return patternPanel;
    }

    public InfoPanel getInfoPanel() {
        return infoPanel;
    }

    public TilePanel getTilePanel(){
        return (TilePanel) subPanels.get(PanelType.Tile);
    }

    public ManagementPanel getManagementPanel() {
        return (ManagementPanel) subPanels.get(PanelType.AdvancedManagement);
    }

    public GroutPanel getGroutPanel() {
        return (GroutPanel) subPanels.get(PanelType.Grout);
    }

    public void refreshGUI() {
        tileSettingsPanel.refreshGUI();
        infoPanel.refreshGUI();
        subPanels.forEach((type, panel) -> {
            panel.refreshGUI();
        });
    }

    public SurfacePanel getSurfacePanel() {
        return (SurfacePanel) subPanels.get(PanelType.Surface);
    }

    enum PanelType {
        Surface,
        AdvancedManagement,
        Pattern,
        Grout,
        Tile
    }

}
