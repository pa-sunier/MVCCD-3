package window.editor.diagrammer.menus.actions;

import java.awt.event.ActionEvent;
import java.io.Serializable;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import main.MVCCDManager;
import mcd.MCDEntity;
import repository.editingTreat.mcd.MCDEntityEditingTreat;
import window.editor.diagrammer.elements.shapes.classes.mcd.MCDEntityShape;

public class MCDEntityShapeEditAction extends AbstractAction implements Serializable {

  private static final long serialVersionUID = 1000;
  private final MCDEntityShape shape;

  public MCDEntityShapeEditAction(String name, Icon icon, MCDEntityShape shape) {
    super(name, icon);
    this.shape = shape;
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    if (this.shape.getEntity() != null) {
      this.edit();
    } else {
      this.create();
    }

    // Affiche les diverses informations de l'entité
    this.shape.refreshInformations();
  }

  private void edit() {
    MVCCDManager manager = MVCCDManager.instance();
    MCDEntityEditingTreat mcdEntityEditingTreat = new MCDEntityEditingTreat();
    mcdEntityEditingTreat.treatUpdate(manager.getMvccdWindow(), this.shape.getEntity());
  }

  private void create() {
    MVCCDManager manager = MVCCDManager.instance();
    MCDEntityEditingTreat mcdEntityEditingTreat = new MCDEntityEditingTreat();
    MCDEntity entity = (MCDEntity) mcdEntityEditingTreat.treatNew(manager.getMvccdWindow(), manager.getProject().getMCDContModels().getEntities());

    this.shape.setEntity(entity);
  }

}