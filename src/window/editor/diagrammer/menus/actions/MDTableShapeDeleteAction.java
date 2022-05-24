package window.editor.diagrammer.menus.actions;

import main.MVCCDManager;
import window.editor.diagrammer.elements.interfaces.IShape;
import window.editor.diagrammer.elements.shapes.classes.MCDEntityShape;
import window.editor.diagrammer.elements.shapes.classes.MDTableShape;
import window.editor.diagrammer.services.DiagrammerService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.List;

public class MDTableShapeDeleteAction extends AbstractAction implements Serializable {

    private MDTableShape shape;

    public MDTableShapeDeleteAction(String name, Icon icon, MDTableShape shape) {
        super(name, icon);
        this.shape = shape;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        delete();
    }

    private void delete() {
        DiagrammerService.getDrawPanel().deleteShape(shape);
        List<IShape> allShapes = DiagrammerService.getDrawPanel().getShapes();
        MVCCDManager.instance().getCurrentDiagram().getShapes().clear();
        MVCCDManager.instance().getCurrentDiagram().getShapes().addAll(allShapes);
    }
}