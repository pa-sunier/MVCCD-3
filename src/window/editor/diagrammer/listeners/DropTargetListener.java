package window.editor.diagrammer.listeners;

import console.ViewLogsManager;
import diagram.Diagram;
import diagram.mpdr.MPDRDiagram;
import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import main.MVCCDManager;
import mdr.MDRRelationFK;
import mdr.MDRTable;
import mpdr.MPDRRelFKEnd;
import preferences.Preferences;
import window.editor.diagrammer.drawpanel.DrawPanel;
import window.editor.diagrammer.elements.interfaces.IShape;
import window.editor.diagrammer.elements.shapes.MDTableShape;
import window.editor.diagrammer.elements.shapes.MPDRProcedureContainerShape;
import window.editor.diagrammer.elements.shapes.MPDRSequenceShape;
import window.editor.diagrammer.elements.shapes.MPDRTriggerShape;
import window.editor.diagrammer.elements.shapes.SquaredShape;
import window.editor.diagrammer.elements.shapes.UMLPackage;
import window.editor.diagrammer.elements.shapes.classes.ClassShape;
import window.editor.diagrammer.elements.shapes.relations.DependencyLinkShape;
import window.editor.diagrammer.elements.shapes.relations.MPDRelationShape;
import window.editor.diagrammer.services.DiagrammerService;
import window.editor.diagrammer.utils.GridUtils;

public class DropTargetListener extends DropTargetAdapter {

  private final DrawPanel panel;
  private final ReferentielListener listener;


  public DropTargetListener(DrawPanel panel, ReferentielListener listener) {
    this.panel = panel;
    this.listener = listener;
    new DropTarget(panel, DnDConstants.ACTION_COPY, this, true, null);
  }

  @Override
  public void drop(DropTargetDropEvent event) {
    try {
      final Diagram currentDiagram = MVCCDManager.instance().getCurrentDiagram();
      final Point mouseClick = event.getLocation();
      DefaultMutableTreeNode node = listener.getNode();

      // On récupère le parent du noeud sur lequel l'utilisateur a cliqué
      DefaultMutableTreeNode fatherNode = (DefaultMutableTreeNode) node.getParent();
      // On récupère le grand-parent du noeud sur lequel l'utilisateur a cliqué
      String grandFatherNode = fatherNode.getParent().toString();

      if (currentDiagram instanceof MPDRDiagram) {
        // S'il s'agit d'une Table
        if (grandFatherNode.equals("MPDR_Oracle") && fatherNode.toString().equals("Tables")) {
          MDRTable mpdrNode = (MDRTable) node.getUserObject();
          this.paintTable(mpdrNode, node, event, fatherNode, mouseClick);
        }
        // S'il s'agit d'un élément TAPIS
        else if (grandFatherNode.equals("Tables") && node.toString().equals("TAPIs")) {
          this.paintTAPIs(event, fatherNode, mouseClick);
        } else {
          event.rejectDrop();
        }

        this.panel.revalidate();
        event.dropComplete(true);
      }
    } catch (Exception e) {
      ViewLogsManager.catchException(e,
          "Drop opéré sur un composant ne supportant pas la fonctionnalité");
    }

  }

  public void paintTAPIs(DropTargetDropEvent event,
      DefaultMutableTreeNode fatherNode, Point mouseClick) {
    final Diagram currentDiagram = MVCCDManager.instance().getCurrentDiagram();
    UMLPackage shape = new UMLPackage(fatherNode.toString() + " TAPIs", fatherNode.toString(),
        List.of(new MPDRTriggerShape((MDRTable) fatherNode.getUserObject()),
            new MPDRProcedureContainerShape((MDRTable) fatherNode.getUserObject()),
            new MPDRSequenceShape((MDRTable) fatherNode.getUserObject())));

    // On contrôle que la shape ne soit pas déjà dans le diagramme et on l'ajoute au diagramme si tel n'est pas le cas
    if (!currentDiagram.getShapes().contains(shape)) {
      event.acceptDrop(DnDConstants.ACTION_COPY);

      shape.setLocation(
          GridUtils.alignToGrid(mouseClick.x, DiagrammerService.getDrawPanel().getGridSize()),
          GridUtils.alignToGrid(mouseClick.y, DiagrammerService.getDrawPanel().getGridSize()));
      currentDiagram.addShape(shape);
      DiagrammerService.getDrawPanel().addShape(shape);

      // On contrôle si la table parent du TAPIs est présente dans le diagramme
      this.checkDependencyLinkTAPIStoTable(fatherNode, shape);

      // Ajout des objets graphiques (Triggers, Séquence et Procedures) dans le UMLPackage
      AtomicInteger centerPositionX = new AtomicInteger(shape.getCenter().x);
      shape.getTapisElements().forEach(e -> {
        e.initUI();
        ((SquaredShape) e).setLocation(
            GridUtils.alignToGrid(centerPositionX.getAndAdd(-80),
                DiagrammerService.getDrawPanel().getGridSize()),
            GridUtils.alignToGrid(shape.getCenter().y,
                DiagrammerService.getDrawPanel().getGridSize()));
        currentDiagram.addShape((IShape) e);
        DiagrammerService.getDrawPanel().addShape((IShape) e);
        DrawPanel drawPanel = (DrawPanel) SwingUtilities.getAncestorNamed(
            Preferences.DIAGRAMMER_DRAW_PANEL_NAME, (Component) e);
        drawPanel.setLayer((Component) e, JLayeredPane.DRAG_LAYER);
      });
    }else {
      event.rejectDrop();
    }
  }

  public void checkDependencyLinkTAPIStoTable(DefaultMutableTreeNode fatherNode, UMLPackage shape) {
    final Diagram currentDiagram = MVCCDManager.instance().getCurrentDiagram();
    // On contrôle si la table parent du TAPIs est présente dans le diagramme
    boolean tablePresent = currentDiagram.getMDTableShapeList()
        .stream().anyMatch(e -> e.getEntity().getName().equals(fatherNode.toString()));
    if (tablePresent) {
      ClassShape tableSource = currentDiagram.getMDTableShapeList()
          .stream().filter(e -> e.getEntity().getName().equals(fatherNode.toString())).findFirst()
          .orElseThrow(RuntimeException::new);

      DependencyLinkShape tapisAssociationShape = new DependencyLinkShape(shape, tableSource,
          "<<specific-to>>");
      currentDiagram.addShape(tapisAssociationShape);
      DiagrammerService.getDrawPanel().addShape(tapisAssociationShape);
    }
  }

  public void paintTable(MDRTable mdrTable, DefaultMutableTreeNode node, DropTargetDropEvent event,
      DefaultMutableTreeNode nodeAllTables, Point mouseClick) {
    final Diagram currentDiagram = MVCCDManager.instance().getCurrentDiagram();
    MDTableShape shape = new MDTableShape(mdrTable);

    DefaultMutableTreeNode nodeRelationExtremite = (DefaultMutableTreeNode) node.getChildAt(2);

    // On contrôle que la shape ne soit pas déjà dans le diagramme et on l'ajoute au diagramme si tel n'est pas le cas
    if (!currentDiagram.getShapes().contains(shape)) {
      event.acceptDrop(DnDConstants.ACTION_COPY);

      shape.setLocation(
          GridUtils.alignToGrid(mouseClick.x, DiagrammerService.getDrawPanel().getGridSize()),
          GridUtils.alignToGrid(mouseClick.y, DiagrammerService.getDrawPanel().getGridSize()));
      currentDiagram.addShape(shape);
      DiagrammerService.getDrawPanel().addShape(shape);

      // Est-ce qu'il y a un lien à créer pour lier la table créée avec une autre table du diagramme ?
      if (currentDiagram.getMDTableShapeList().size() > 1) {
        this.paintTableLink(nodeRelationExtremite, nodeAllTables, node);
      }

      // Est-ce qu'il y a un lien à créer pour lier la table créée avec un package du diagramme ?
      boolean umlPackagePresent = currentDiagram.getUMLPackagesList()
          .stream().anyMatch(e -> e.getParentTableName().equals(mdrTable.getName()));

      if (umlPackagePresent) {
        this.checkDependencyLinkTableToTAPIS(mdrTable, shape);
      }
    }
  }

  public void checkDependencyLinkTableToTAPIS(MDRTable mdrTable, MDTableShape shape) {
    final Diagram currentDiagram = MVCCDManager.instance().getCurrentDiagram();
    UMLPackage umlPackage = currentDiagram.getUMLPackagesList()
        .stream().filter(e -> e.getParentTableName().equals(mdrTable.getName())).findFirst()
        .orElseThrow(RuntimeException::new);

    DependencyLinkShape tapisAssociationShape = new DependencyLinkShape(umlPackage,
        shape, "<<specific-to>>");
    currentDiagram.addShape(tapisAssociationShape);
    DiagrammerService.getDrawPanel().addShape(tapisAssociationShape);
  }


  public void paintTableLink(DefaultMutableTreeNode nodeRelationExtremite,
      DefaultMutableTreeNode nodeAllTables, DefaultMutableTreeNode node) {

    final Diagram currentDiagram = MVCCDManager.instance().getCurrentDiagram();

    for (int i = 0; i < nodeRelationExtremite.getChildCount(); i++) {
      DefaultMutableTreeNode referentielNode = (DefaultMutableTreeNode) nodeRelationExtremite.getChildAt(
          i);
      MPDRRelFKEnd userObjectNode = (MPDRRelFKEnd) referentielNode.getUserObject();
      MDRRelationFK mdrRelationFK = userObjectNode.getMDRRelationFK();

      String nameRelation = nodeRelationExtremite.getChildAt(i).toString();
      String nameRelationFormat = nameRelation.substring(nameRelation.indexOf("-") + 2);

      for (int ii = 0; ii < nodeAllTables.getChildCount(); ii++) {
        DefaultMutableTreeNode nodeControle = (DefaultMutableTreeNode) nodeAllTables.getChildAt(ii);
        String nodeControleNom = nodeAllTables.getChildAt(ii).toString();

        if (!nodeControleNom.equals(node.toString())) {

          for (int iii = 0; iii < nodeControle.getChildAt(2).getChildCount(); iii++) {
            String nameRelationRecherche = nodeControle.getChildAt(2).getChildAt(iii).toString();
            String nameRelationRechercheFormat = nameRelationRecherche.substring(
                nameRelationRecherche.indexOf("-") + 2);

            if (nameRelationFormat.equals(nameRelationRechercheFormat)) {
              List<MDTableShape> mdTableShapes = currentDiagram.getMDTableShapeList();

              if (mdTableShapes.stream()
                  .anyMatch(e -> e.getEntity().getName().equals(nodeControleNom))) {

                MDTableShape table1 = mdTableShapes.stream()
                    .filter(e -> e.getEntity().getName().equals(node.toString())).findFirst()
                    .orElseThrow(RuntimeException::new);

                MDTableShape table2 = mdTableShapes.stream()
                    .filter(e -> e.getEntity().getName().equals(nodeControleNom)).findFirst()
                    .orElseThrow(RuntimeException::new);

                MPDRelationShape relation;

                if (mdrRelationFK.getA().getmElement().toString().equals(table1.getName())) {
                  relation = new MPDRelationShape(table2, table1, mdrRelationFK);
                } else {
                  relation = new MPDRelationShape(table1, table2, mdrRelationFK);
                }

                currentDiagram.addShape(relation);
                DiagrammerService.getDrawPanel().addShape(relation);
                DiagrammerService.getDrawPanel().repaint();
              }
            }
          }
        }
      }
    }
  }

}
