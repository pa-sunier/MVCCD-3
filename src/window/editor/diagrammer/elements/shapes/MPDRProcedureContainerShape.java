package window.editor.diagrammer.elements.shapes;


import mdr.MDRTable;
import mpdr.MPDRTable;
import mpdr.tapis.MPDRBoxPackages;
import mpdr.tapis.MPDRContTAPIs;
import preferences.Preferences;
import window.editor.diagrammer.elements.interfaces.UMLPackageIntegrableShapes;
import window.editor.diagrammer.elements.shapes.classes.ClassShape;

import java.awt.*;

public class MPDRProcedureContainerShape extends ClassShape implements
    UMLPackageIntegrableShapes {

  private UMLPackage parentUMLPackage = null;

  public MPDRProcedureContainerShape(MDRTable mdrTable) {
    super(mdrTable);
  }

  @Override
  protected void defineBackgroundColor() {
    //this.setBackground(DIAGRAMMER_MPDRPROCEDURECONTAINER_DEFAULT_BACKGROUND_COLOR);
    this.setOpaque(false);
  }


  @Override
  protected void setZoneEnTeteContent() {
    this.zoneEnTete.getElements().clear();

    MPDRTable mpdrTable = (MPDRTable) this.relatedRepositoryElement;
    String technoBD = mpdrTable.getStereotypesString().get(mpdrTable.getStereotypes().size() - 1);

    if (technoBD.equals("<<Oracle>>")) {
      this.zoneEnTete.addElement(
          "<<" + Preferences.STEREOTYPE_PACKAGES_NAME + ">>");
    }

    this.zoneEnTete.addElement(technoBD);
    this.zoneEnTete.addElement(mpdrTable.getNames().getName30());
    this.updateSizeAndMinimumSize();
  }

  @Override
  protected void setZoneProprietesContent() {
    this.zoneProprietes.getElements().clear();

    MPDRTable mpdrTable = (MPDRTable) this.relatedRepositoryElement;

    MPDRContTAPIs mpdrContTAPIs = mpdrTable.getMPDRContTAPIs();

    MPDRBoxPackages allPackagesString = mpdrContTAPIs.getMPDRBoxPackages();
    if (allPackagesString != null) {
      allPackagesString.getAllPackagesString().forEach(e ->
          this.zoneProprietes.addElement(e)
      );
    }

    if (this.zoneProprietes.isEmpty()) {
      this.zoneProprietes.addElement("TAPIs non générés (préférences)");
    }

    this.updateSizeAndMinimumSize();
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

    // Dessine une ligne séparatrice entre l'entête et les propriétés
    graphics2D.drawLine(0, this.getZoneMinHeight(this.zoneEnTete.getElements()), this.getWidth(),
        this.getZoneMinHeight(this.zoneEnTete.getElements()));
  }

  @Override
  protected void drawZoneProprietes(Graphics2D graphics2D) {
    this.setZoneProprietesContent();
    int y = this.getZoneMinHeight(this.zoneEnTete.getElements())
        + Preferences.DIAGRAMMER_CLASS_PADDING + graphics2D.getFontMetrics().getHeight();

    for (int i = 0; i < this.zoneProprietes.getElements().size(); i++) {
      if (i == 1) {
        this.setNameFont(graphics2D);
      } else {
        graphics2D.setFont(Preferences.DIAGRAMMER_CLASS_FONT);
      }
      int x = this.getCenterTextPositionX(this.zoneProprietes.getElements().get(i), graphics2D);
      graphics2D.drawString(this.zoneProprietes.getElements().get(i), x, y);
      y += graphics2D.getFontMetrics().getHeight();
    }
  }

  @Override
  protected void defineSize() {
    this.setSize(this.getMinimumSize());
  }

  @Override
  public void initUI() {
    this.defineMinimumSize();
    this.defineBackgroundColor();
    this.defineSize();
  }


  @Override
  protected void doDraw(Graphics graphics) {

  }

  @Override
  public void paintComponent(Graphics g) {
    int width = getWidth();
    int height = getHeight();

    Graphics2D graphics = (Graphics2D) g;
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    graphics.setColor(super.getBackground());

    graphics.fillRect(0, (int) (width * 0.10), width, height);
    graphics.fillRoundRect(0, 0, width, height, 40, 40);

    graphics.setColor(Color.black);
    this.drawZoneEnTete(graphics);
    this.drawZoneProprietes(graphics);
  }

  @Override
  protected String getLongestProperty() {
    return null;
  }

  @Override
  protected void setNameFont(Graphics2D graphics2D) {

  }

  @Override
  public String getXmlTagName() {
    return null;
  }

  @Override
  public void setLocationDifference(int differenceX, int differenceY) {
    super.setLocationDifference(differenceX, differenceY);
  }

  @Override
  public void setParentUMLPackage(UMLPackage parentUMLPackage) {
    this.parentUMLPackage = parentUMLPackage;
  }

  public UMLPackage getParentUMLPackage() {
    return parentUMLPackage;
  }
}
