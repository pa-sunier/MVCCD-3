package window.editor.diagrammer.elements.shapes;


import mdr.MDRTable;
import mpdr.MPDRTable;
import preferences.Preferences;
import window.editor.diagrammer.elements.shapes.classes.ClassShape;
import window.editor.diagrammer.listeners.MDTableShapeListener;
import window.editor.diagrammer.utils.UIUtils;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

import static preferences.Preferences.DIAGRAMMER_MDTABLE_DEFAULT_BACKGROUND_COLOR;

public class MDTableShape extends ClassShape implements Serializable {

  private final String name;

  public MDTableShape(MDRTable mdrTable) {
    super(mdrTable);
    this.name = mdrTable.getName();
    this.addListeners();
    this.updateSizeAndMinimumSize();
  }

  @Override
  protected void paintComponent(Graphics g) {
    int width = getWidth();
    int height = getHeight();

    Graphics2D graphics = (Graphics2D) g;
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    graphics.setColor(super.getBackground());

    graphics.fillRoundRect(0, 0, width, height, 20, 20);

    graphics.setColor(Color.black);
    this.drawZoneEnTete(graphics);
    this.drawZoneProprietes(graphics);
    this.drawZoneOperations(graphics);
  }

  @Override
  protected void drawZoneEnTete(Graphics2D graphics2D) {
    int y = getYSize(graphics2D);
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

  private int getYSize(Graphics2D graphics2D) {
    return Preferences.DIAGRAMMER_CLASS_PADDING + graphics2D.getFontMetrics().getHeight();
  }

  @Override
  protected void drawZoneProprietes(Graphics2D graphics2D) {
    int y =
        this.getZoneMinHeight(this.zoneEnTete.getElements()) + getYSize(graphics2D);
    this.drawElements(graphics2D, this.zoneProprietes.getElements(), y, UIUtils.getShapeFont());
  }

  private void drawZoneOperations(Graphics2D graphics2D) {
    this.setZoneOperationsContent();
    int y = this.getZoneMinHeight(this.zoneProprietes.getElements()) + this.getZoneMinHeight(
        this.zoneEnTete.getElements()) + getYSize(graphics2D);
    this.drawElements(graphics2D, this.zoneOperations.getElements(), y, UIUtils.getShapeFont());
    this.drawBorders(graphics2D);
  }


  private void drawBorders(Graphics2D graphics2D) {
    graphics2D.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 20, 20);
    graphics2D.drawLine(0, this.getZoneMinHeight(this.zoneEnTete.getElements()), this.getWidth(),
        this.getZoneMinHeight(this.zoneEnTete.getElements()));

    int height = this.getZoneMinHeight(this.zoneProprietes.getElements()) + this.getZoneMinHeight(
        this.zoneEnTete.getElements());
    graphics2D.drawLine(0, height, this.getWidth(), height);
  }

  @Override
  public void refreshInformations() {
    setZoneEnTeteContent();
    setZoneProprietesContent();
    setZoneOperationsContent();
    repaint();
  }


  @Override
  protected void defineBackgroundColor() {
    this.setBackground(DIAGRAMMER_MDTABLE_DEFAULT_BACKGROUND_COLOR);
  }

  @Override
  protected void defineSize() {
    this.setSize(this.getMinimumSize());
  }

  private void addListeners() {
    MDTableShapeListener listener = new MDTableShapeListener();
    this.addMouseListener(listener);
    this.addMouseMotionListener(listener);
  }

  @Override
  protected void doDraw(Graphics graphics) {

  }

  public MDRTable getTable() {
    return (MDRTable) this.getRelatedRepositoryElement();
  }

  @Override
  protected void setZoneEnTeteContent() {
    this.zoneEnTete.getElements().clear();

    MPDRTable mpdrTable = (MPDRTable) this.relatedRepositoryElement;
    mpdrTable.getStereotypesString().forEach(
        e -> this.zoneEnTete.addElement(e)
    );
    this.zoneEnTete.addElement(mpdrTable.getName());
  }

  private void setZoneOperationsContent() {
    this.zoneOperations.getElements().clear();

    MPDRTable mpdrTable = (MPDRTable) this.relatedRepositoryElement;

    String columnsCommaSeparated = mpdrTable.getMPDRPK().getMDRColumnsString();

    zoneOperations.addElement(
        mpdrTable.getMPDRColumnPKProper().getStereotypesInLine() + " " + mpdrTable.getMPDRPK()
            .getName() + "(" + columnsCommaSeparated + ")"
    );

    mpdrTable.getMPDRFKsString().forEach(
        e -> zoneOperations.addElement(e)
    );

    mpdrTable.getMPDRIndexesString().forEach(e ->
        zoneOperations.addElement(e)
    );

    mpdrTable.getMPDRChecksString().forEach(
        e -> zoneOperations.addElement(e)
    );
  }

  @Override
  protected void setZoneProprietesContent() {
    this.zoneProprietes.getElements().clear();

    MPDRTable mpdrTable = (MPDRTable) this.relatedRepositoryElement;

    mpdrTable.getMPDRColumnsSortDefaultString().forEach(
        e -> zoneProprietes.addElement(e)
    );
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
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
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

  public int getID() {
    return super.getId();
  }

}