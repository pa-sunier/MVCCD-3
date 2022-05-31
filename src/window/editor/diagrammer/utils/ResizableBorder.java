package window.editor.diagrammer.utils;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public final class ResizableBorder implements Border, Serializable {

  private static final long serialVersionUID = 1000;
  private final int DISTANCE = 8;
  int[] locations = {SwingConstants.NORTH, SwingConstants.SOUTH, SwingConstants.WEST, SwingConstants.EAST};
  int[] cursors = {Cursor.N_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR, Cursor.E_RESIZE_CURSOR};
  private boolean isVisible = false;

  @Override
  public void paintBorder(Component component, Graphics g, int x, int y, int w, int h) {

  }

  @Override
  public Insets getBorderInsets(Component component) {
    return new Insets(this.DISTANCE, this.DISTANCE, this.DISTANCE, this.DISTANCE);
  }

  @Override
  public boolean isBorderOpaque() {
    return false;
  }

  private Rectangle getRectangle(int x, int y, int width, int height, int location) {
    switch (location) {
      case SwingConstants.NORTH:
        return new Rectangle(x, y, width, this.DISTANCE);
      case SwingConstants.SOUTH:
        return new Rectangle(x, y + height - this.DISTANCE, width, this.DISTANCE);
      case SwingConstants.WEST:
        return new Rectangle(x, y, this.DISTANCE, height);
      case SwingConstants.EAST:
        return new Rectangle(x + width - this.DISTANCE, y, this.DISTANCE, height);
      default:
        return new Rectangle();
    }
  }

  public boolean isOneRectangleHovered(MouseEvent e) {
    Component component = e.getComponent();
    final int width = component.getWidth();
    final int height = component.getHeight();
    for (int i = 0; i < this.locations.length; i++) {
      Rectangle rect = this.getRectangle(0, 0, width, height, this.locations[i]);
      if (rect.contains(e.getPoint())) {
        return true;
      }
    }
    return false;
  }

  public int getCursor(MouseEvent e) {
    Component component = e.getComponent();
    final int width = component.getWidth();
    final int height = component.getHeight();
    for (int i = 0; i < this.locations.length; i++) {
      Rectangle rect = this.getRectangle(0, 0, width, height, this.locations[i]);
      if (rect.contains(e.getPoint())) {
        return this.cursors[i];
      }
    }
    return Cursor.MOVE_CURSOR;
  }

  public boolean isVisible() {
    return this.isVisible;
  }

  public void setVisible(boolean visible) {
    this.isVisible = visible;
  }
}