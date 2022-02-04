package window.editor.diagrammer.listeners;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import window.editor.diagrammer.elements.shapes.classes.MCDEntityShape;
import window.editor.diagrammer.menus.EntityShapeMenu;
import window.editor.diagrammer.services.DiagrammerService;

public class MCDEntityShapeListener extends MouseAdapter {

  @Override
  public void mouseClicked(MouseEvent e) {
    super.mouseClicked(e);
    if (SwingUtilities.isRightMouseButton(e)) {
      this.showMenu(e);
    }
  }

  private void showMenu(MouseEvent event) {
    final MCDEntityShape shape = (MCDEntityShape) event.getSource();
    final Point converted = SwingUtilities.convertPoint(shape, event.getPoint(), DiagrammerService.getDrawPanel());
    final EntityShapeMenu menu = new EntityShapeMenu(shape);

    menu.show(DiagrammerService.getDrawPanel(), converted.x, converted.y);
  }
}