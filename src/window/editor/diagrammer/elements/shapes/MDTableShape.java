package window.editor.diagrammer.elements.shapes;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import mdr.MDRTable;
import mpdr.oracle.MPDROracleTable;
import preferences.Preferences;
import window.editor.diagrammer.elements.shapes.classes.ClassShape;
import window.editor.diagrammer.listeners.MDTableShapeListener;

public class MDTableShape extends ClassShape implements Serializable {
    private final String name;
    private final Color COLOR = Color.decode("#A0F0CF");
    private boolean initialized = false;

    public MDTableShape(MDRTable mdrTable) {
        super(mdrTable, true);
        this.name = mdrTable.getName();
        this.addListeners();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(COLOR);

        graphics.fillRoundRect(0, 0, width, height, 20, 20);

        graphics.setColor(Color.black);

        this.drawZoneEnTete(graphics);
        this.drawZoneProprietes(graphics);
        this.drawZoneOperations(graphics);
        this.initialized = true;
    }

    @Override
    protected void drawZoneEnTete(Graphics2D graphics2D) {
        this.setZoneEnTeteContent();
        int y = Preferences.DIAGRAMMER_CLASS_PADDING + graphics2D.getFontMetrics().getHeight();
        for (int i = 0; i < this.zoneEnTete.getElements().size(); i++) {
            if (i == 1) {
                this.setNameFont(graphics2D);
            } else {
                graphics2D.setFont(Preferences.DIAGRAMMER_CLASS_FONT);
            }
            int x = this.getCenterTextPositionX(this.zoneEnTete.getElements().get(i), graphics2D);
            graphics2D.drawString(this.zoneEnTete.getElements().get(i), x, y);
            y += graphics2D.getFontMetrics().getHeight();
        }
    }

    @Override
    protected void drawZoneProprietes(Graphics2D graphics2D) {
        this.setZoneProprietesContent();
        int y = this.getZoneMinHeight(this.zoneEnTete.getElements()) + Preferences.DIAGRAMMER_CLASS_PADDING + graphics2D.getFontMetrics().getHeight();
        this.drawElements(graphics2D, this.zoneProprietes.getElements(), y);
    }


    private void drawZoneOperations(Graphics2D graphics2D) {
        this.setZoneOperationsContent();
        int y = this.getZoneMinHeight(this.zoneProprietes.getElements()) + this.getZoneMinHeight(this.zoneEnTete.getElements()) + Preferences.DIAGRAMMER_CLASS_PADDING + graphics2D.getFontMetrics().getHeight();
        this.drawElements(graphics2D, this.zoneOperations.getElements(), y);
        this.drawBorders(graphics2D);
    }

    private void drawBorders(Graphics2D graphics2D) {
        graphics2D.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 20, 20);
        graphics2D.drawLine(0, this.getZoneMinHeight(this.zoneEnTete.getElements()), this.getWidth(), this.getZoneMinHeight(this.zoneEnTete.getElements()));

        int height = this.getZoneMinHeight(this.zoneProprietes.getElements()) + this.getZoneMinHeight(this.zoneEnTete.getElements());
        graphics2D.drawLine(0, height, this.getWidth(), height);
    }

    @Override
    public void refreshInformations() {
        setZoneEnTeteContent();
        setZoneProprietesContent();
        setZoneOperationsContent();
        repaint();
    }


    private int getCenterTextPositionX(String element, Graphics2D graphics2D) {
        return this.getWidth() / 2 - graphics2D.getFontMetrics().stringWidth(element) / 2;
    }

    private void drawElements(Graphics2D graphics2D, List<String> elements, int y) {
        graphics2D.setFont(Preferences.DIAGRAMMER_CLASS_FONT);
        final int x = Preferences.DIAGRAMMER_CLASS_PADDING;
        for (String element : elements) {
            graphics2D.drawString(element, x, y);
            y += graphics2D.getFontMetrics().getHeight();
        }
    }

    private int getZoneMinHeight(List<String> elements) {
        final FontMetrics fontMetrics = this.getFontMetrics(Preferences.DIAGRAMMER_CLASS_FONT);
        int minHeight = Preferences.DIAGRAMMER_CLASS_PADDING * 2;
        minHeight += fontMetrics.getHeight() * elements.size();
        return minHeight;
    }

    @Override
    protected void defineBackgroundColor() {
        this.setBackgroundColor();
    }

    @Override
    protected void defineSize() {

    }

    private void addListeners() {
        MDTableShapeListener listener = new MDTableShapeListener();
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);
    }

    @Override
    protected void doDraw(Graphics graphics) {

    }

    public MDRTable getEntity() {
        return (MDRTable) this.getRelatedRepositoryElement();
    }

    @Override
    protected void setZoneEnTeteContent() {
        this.zoneEnTete.getElements().clear();

        MPDROracleTable mpdrTable = (MPDROracleTable) this.relatedRepositoryElement;
        mpdrTable.getStereotypes().forEach(
                e -> this.zoneEnTete.addElement("<<" + e.toString() + ">>")
        );
        this.zoneEnTete.addElement(mpdrTable.getName());


        if (!initialized) {
            updateSizeAndMinimumSize();
        }
    }

    private void setZoneOperationsContent() {
        this.zoneOperations.getElements().clear();

        MPDROracleTable mpdrTable = (MPDROracleTable) this.relatedRepositoryElement;

        String columnsCommaSeparated = mpdrTable.getMPDRPK().getMDRColumns().stream()
                .map(e -> e.toString().toLowerCase())
                .collect(Collectors.joining(","));

        zoneOperations.addElement(
                mpdrTable.getMPDRColumnPKProper().getStereotypesInLine() + " " + mpdrTable.getMPDRPK().getName() + "(" + columnsCommaSeparated + ")"
        );


        mpdrTable.getMPDRFKs().forEach(
                e -> zoneOperations.addElement(e.getStereotypesInLine() + " " + e.getName() + "(" +
                        (e.getMDRColumns().stream()
                                .map(ee -> ee.toString().toLowerCase())
                                .collect(Collectors.joining(","))
                        )
                        + ")"
                )
        );


        mpdrTable.getMPDRUniques().forEach(
                e -> zoneOperations.addElement(e.getStereotypesInLine() + " " + e.getName() + "(" +
                        (e.getMDRColumns().stream()
                                .map(ee -> ee.toString().toLowerCase())
                                .collect(Collectors.joining(","))
                        )
                        + ")"
                )
        );

        mpdrTable.getMPDRChecks().forEach(
                e -> zoneOperations.addElement(e.getStereotypesInLine() + " " + e.getName() + "(" +
                        (e.getMldrElementSource().getName())
                        + ")"
                )
        );

        if (!initialized) {
            updateSizeAndMinimumSize();
        }
    }

    @Override
    protected void setZoneProprietesContent() {
        this.zoneProprietes.getElements().clear();

        MPDROracleTable mpdrTable = (MPDROracleTable) this.relatedRepositoryElement;

        mpdrTable.getMPDRColumnsSortDefault().forEach(
                e -> zoneProprietes.addElement(
                        e.getStereotypesInLine() + " "
                                + e.getMldrElementSource().getName() + " : "
                                + e.getDatatypeLienProg()
                                + "(" + e.getSize() + ") "
                                + "{" + e.getDatatypeConstraint() + "}")
        );


        if (!initialized) {
            updateSizeAndMinimumSize();
        }
    }

    @Override
    protected void setBackgroundColor() {
        this.setBackground(new Color(255, 255, 255));
    }

    @Override
    protected String getLongestProperty() {
        return null;
    }

    @Override
    protected void setNameFont(Graphics2D graphics2D) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MDTableShape that = (MDTableShape) o;
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String getXmlTagName() {
        return null;
    }


    @Override
    public String getName() {
        return name;
    }
}