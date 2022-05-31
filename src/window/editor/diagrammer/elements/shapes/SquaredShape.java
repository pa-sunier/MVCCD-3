package window.editor.diagrammer.elements.shapes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import window.editor.diagrammer.elements.interfaces.IResizable;
import window.editor.diagrammer.elements.interfaces.IShape;
import window.editor.diagrammer.elements.shapes.relations.RelationAnchorPointShape;
import window.editor.diagrammer.elements.shapes.relations.RelationShape;
import window.editor.diagrammer.listeners.SquaredShapeListener;
import window.editor.diagrammer.services.DiagrammerService;
import window.editor.diagrammer.utils.GeometryUtils;
import window.editor.diagrammer.utils.GridUtils;
import window.editor.diagrammer.utils.IDManager;

public abstract class SquaredShape extends JPanel implements IShape, IResizable, Serializable {

  private static final long serialVersionUID = 1000;
  protected int id;
  protected boolean isFocused = false;
  protected boolean isResizing = false;
  protected Dimension sizeAtDefaultZoom;

  public SquaredShape(int id) {
    this();
    this.id = id;
  }

  public SquaredShape() {
    super();
    this.id = IDManager.generateId();
    this.addListeners();
    this.initUI();
  }

  protected abstract void defineBackgroundColor();
  protected abstract void defineMinimumSize();
  protected abstract void defineSize();

  private void initUI() {
    this.defineMinimumSize();
    this.defineBackgroundColor();
    this.defineSize();
    this.defineSizeAtDefaultZoom();
  }

  protected abstract void defineSizeAtDefaultZoom();

  private void addListeners() {
    SquaredShapeListener listener = new SquaredShapeListener(this);
    this.addMouseListener(listener);
    this.addMouseMotionListener(listener);
  }

  protected abstract void doDraw(Graphics graphics);

  @Override
  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    this.setBorder(BorderFactory.createLineBorder(Color.BLACK, this.isFocused || this.isResizing ? 0 : 0));
    this.doDraw(graphics);
  }

  @Override
  public void resize(Rectangle newBounds) {
    this.setBounds(newBounds.x, newBounds.y, newBounds.width, newBounds.height);
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public Point getCenter() {
    return new Point((int) this.getBounds().getCenterX(), (int) this.getBounds().getCenterY());
  }

  @Override
  public void zoom(int fromFactor, int toFactor) {

    int newXPosition = this.getX() * toFactor / fromFactor;
    int newYPosition = this.getY() * toFactor / fromFactor;
    int newWidth = this.getBounds().width * toFactor / fromFactor;
    int newHeight = this.getBounds().height * toFactor / fromFactor;

    this.setSize(GridUtils.alignToGrid(newWidth, toFactor), GridUtils.alignToGrid(newHeight, toFactor));

    this.setLocation(GridUtils.alignToGrid(newXPosition, toFactor), GridUtils.alignToGrid(newYPosition, toFactor));
  }

  @Override
  public void drag(int differenceX, int differenceY) {
    List<RelationShape> linkedRelations = DiagrammerService.getDrawPanel().getRelationShapesByClSquaredShape(this);
    for (RelationShape relation : linkedRelations) {
      RelationAnchorPointShape nearestPointAncrage = GeometryUtils.getNearestPointAncrage(this, relation);
      nearestPointAncrage.drag(nearestPointAncrage.x + differenceX, nearestPointAncrage.y + differenceY);
    }
    this.setLocation(this.getX() + differenceX, this.getY() + differenceY);
  }

  @Override
  public boolean isFocused() {
    return this.isFocused;
  }

  @Override
  public void setFocused(boolean selected) {
    this.isFocused = selected;
    this.repaint();
  }

  @Override
  public String toString() {
    return "SquaredShape{" + "id=" + this.id + '}';
  }

  public void setResizing(boolean resizing) {
    this.isResizing = resizing;
  }
}