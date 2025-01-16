package window.editor.diagrammer.elements.shapes;


import preferences.Preferences;
import window.editor.diagrammer.elements.interfaces.UMLPackageIntegrableShapes;
import window.editor.diagrammer.elements.shapes.relations.RelationAnchorPointShape;
import window.editor.diagrammer.elements.shapes.relations.RelationShape;
import window.editor.diagrammer.listeners.UMLPackageListener;
import window.editor.diagrammer.services.DiagrammerService;
import window.editor.diagrammer.utils.GeometryUtils;

import java.awt.*;
import java.util.List;
import java.util.Objects;

import static preferences.Preferences.DIAGRAMMER_UMLPACKAGE_DEFAULT_BACKGROUND_COLOR;

public class UMLPackage extends SquaredShape {

  private final List<UMLPackageIntegrableShapes> tapisElements;
  private final String parentTableName;
  private final String name;

  public UMLPackage(String name, String parentTableName,
      List<UMLPackageIntegrableShapes> tapisElements) {
    super();
    this.name = name;
    this.parentTableName = parentTableName;
    this.tapisElements = tapisElements;
    tapisElements.forEach(e -> e.setParentUMLPackage(this));

    this.addListeners();
  }

  @Override
  protected void paintComponent(Graphics g) {
    int width = getWidth();
    int height = getHeight();

    int heightBigRectangle = height / 6;

    Graphics2D graphics = (Graphics2D) g;
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    graphics.setStroke(new BasicStroke(3));
    graphics.setColor(Color.white);
    graphics.fillRect(0, heightBigRectangle, width, height - heightBigRectangle);
    graphics.setColor(super.getBackground());
    graphics.drawRect(0, heightBigRectangle, width, height - heightBigRectangle);

    graphics.fillRect(0, 0, width / 2, height / 6);

    graphics.setColor(Color.black);
    graphics.setFont(Preferences.DIAGRAMMER_CLASS_FONT);
    Rectangle r = new Rectangle(0, 0, width / 2, height / 6);
    graphics.drawString("<<TAPIs>>", (int) ((int) r.getCenterX() / 1.5), (int) r.getCenterY());
  }


  @Override
  protected void defineBackgroundColor() {
    this.setBackground(DIAGRAMMER_UMLPACKAGE_DEFAULT_BACKGROUND_COLOR);
    this.setOpaque(false);
  }

  @Override
  protected void defineMinimumSize() {
    this.setMinimumSize(new Dimension((int) (Preferences.DIAGRAMMER_DEFAULT_CLASS_WIDTH * 2.2),
        (int) (Preferences.DIAGRAMMER_DEFAULT_CLASS_HEIGHT * 2.8)));
  }

  @Override
  protected void defineSize() {
    this.setSize(this.getMinimumSize());
  }

  @Override
  protected void defineSizeAtDefaultZoom() {

  }

  @Override
  public String getName() {
    return name;
  }

  public String getParentTableName() {
    return parentTableName;
  }

  private void addListeners() {
    UMLPackageListener listener = new UMLPackageListener();
    this.addMouseListener(listener);
    this.addMouseMotionListener(listener);
  }

  @Override
  public void drag(int differenceX, int differenceY) {
    Rectangle bounds = this.getBounds();
    bounds.translate(differenceX, differenceY);
    this.setBounds(bounds);

    for (RelationShape relation : DiagrammerService.getDrawPanel()
        .getRelationShapesBySquaredShape(this)) {

      RelationAnchorPointShape nearestPointAncrage = GeometryUtils.getNearestPointAncrage(this,
          relation);
      nearestPointAncrage.setLocationDifference(differenceX, differenceY);

    }

    this.repaint();
  }

  @Override
  protected void doDraw(Graphics graphics) {

  }

  public List<UMLPackageIntegrableShapes> getTapisElements() {
    return tapisElements;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UMLPackage that = (UMLPackage) o;
    return getParentTableName().equals(that.getParentTableName()) && getName().equals(
        that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getParentTableName(), getName());
  }
}
