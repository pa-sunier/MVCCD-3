package window.editor.diagrammer.listeners;

import console.ViewLogsManager;
import diagram.mpdr.MPDRDiagram;
import main.MVCCDManager;
import mpdr.oracle.MPDROracleTable;
import window.editor.diagrammer.drawpanel.DrawPanel;
import window.editor.diagrammer.elements.shapes.classes.ClassShape;
import window.editor.diagrammer.elements.shapes.classes.MDTableShape;
import window.editor.diagrammer.elements.shapes.classes.UMLPackage;
import window.editor.diagrammer.elements.shapes.relations.MPDAssociationShape;
import window.editor.diagrammer.elements.shapes.relations.RelationShapeDashed;
import window.editor.diagrammer.services.DiagrammerService;
import window.editor.diagrammer.utils.GridUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;

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
            final Point mouseClick = event.getLocation();
            DefaultMutableTreeNode node = listener.getNode();

            // On récupère le parent du noeud sur lequel l'utilisateur a cliqué
            DefaultMutableTreeNode fatherNode = (DefaultMutableTreeNode) node.getParent();
            // On récupère le grand-parent du noeud sur lequel l'utilisateur a cliqué
            String grandFatherNode = fatherNode.getParent().toString();

            if (MVCCDManager.instance().getCurrentDiagram() instanceof MPDRDiagram) {
                // S'il s'agit d'une Table
                if (grandFatherNode.equals("MPDR_Oracle") && fatherNode.toString().equals("Tables")) {
                    MPDROracleTable mpdrNode = (MPDROracleTable) node.getUserObject();
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
            ViewLogsManager.catchException(e, "Drop opéré sur un composant ne supportant pas la fonctionnalité");
        }

    }

    public void paintTAPIs(DropTargetDropEvent event, DefaultMutableTreeNode fatherNode, Point mouseClick) {
        UMLPackage shape = new UMLPackage(fatherNode.toString() + " TAPIs");

        // On contrôle que la shape ne soit pas déjà dans le diagramme
        if (!MVCCDManager.instance().getCurrentDiagram().getShapes().contains(shape)) {
            event.acceptDrop(DnDConstants.ACTION_COPY);

            shape.setLocation(GridUtils.alignToGrid(mouseClick.x, DiagrammerService.getDrawPanel().getGridSize()), GridUtils.alignToGrid(mouseClick.y, DiagrammerService.getDrawPanel().getGridSize()));
            MVCCDManager.instance().getCurrentDiagram().addShape(shape);
            DiagrammerService.getDrawPanel().addShape(shape);
        }

        // On contrôle si la table parent du TAPIs est présente dans le diagramme
        boolean tablePresent = MVCCDManager.instance().getCurrentDiagram().getMDTableShapeList().stream().anyMatch(e -> e.getEntity().getName().equals(fatherNode.toString()));
        if (tablePresent) {
            ClassShape tableSource = MVCCDManager.instance().getCurrentDiagram().getMDTableShapeList().stream().filter(e -> e.getEntity().getName().equals(fatherNode.toString())).findFirst().orElseThrow(RuntimeException::new);

            RelationShapeDashed tapisAssociationShape = new RelationShapeDashed(shape, tableSource, "<<specific-to>>");
            MVCCDManager.instance().getCurrentDiagram().addShape(tapisAssociationShape);
            DiagrammerService.getDrawPanel().addShape(tapisAssociationShape);
        }
    }

    public void paintTable(MPDROracleTable mpdrNode, DefaultMutableTreeNode node, DropTargetDropEvent event, DefaultMutableTreeNode nodeAllTables, Point mouseClick) {
        MDTableShape shape = new MDTableShape(mpdrNode);

        DefaultMutableTreeNode nodeRelationExtremite = (DefaultMutableTreeNode) node.getChildAt(2);

        // On contrôle que la shape ne soit pas déjà dans le diagramme
        if (!MVCCDManager.instance().getCurrentDiagram().getShapes().contains(shape)) {
            event.acceptDrop(DnDConstants.ACTION_COPY);

            shape.setLocation(GridUtils.alignToGrid(mouseClick.x, DiagrammerService.getDrawPanel().getGridSize()), GridUtils.alignToGrid(mouseClick.y, DiagrammerService.getDrawPanel().getGridSize()));
            MVCCDManager.instance().getCurrentDiagram().addShape(shape);
            DiagrammerService.getDrawPanel().addShape(shape);

            // Est-ce qu'il y a une asssociation liée s'il s'agit d'une Table ?
            if (MVCCDManager.instance().getCurrentDiagram().getMDTableShapeList().size() > 1) {
                rechercheAssociationsLiees(nodeRelationExtremite, nodeAllTables, node);
            }
        }
    }


    public void rechercheAssociationsLiees(DefaultMutableTreeNode nodeRelationExtremite, DefaultMutableTreeNode nodeAllTables, DefaultMutableTreeNode node) {

        for (int i = 0; i < nodeRelationExtremite.getChildCount(); i++) {
            String nameRelation = nodeRelationExtremite.getChildAt(i).toString();
            String nameRelationFormat = nameRelation.substring(nameRelation.indexOf("-") + 2);

            for (int ii = 0; ii < nodeAllTables.getChildCount(); ii++) {
                DefaultMutableTreeNode nodeControle = (DefaultMutableTreeNode) nodeAllTables.getChildAt(ii);
                String nodeControleNom = nodeAllTables.getChildAt(ii).toString();
                if (!nodeControleNom.equals(node.toString())) {
                    for (int iii = 0; iii < nodeControle.getChildAt(2).getChildCount(); iii++) {
                        String nameRelationRecherche = nodeControle.getChildAt(2).getChildAt(iii).toString();
                        String nameRelationRechercheFormat = nameRelationRecherche.substring(nameRelationRecherche.indexOf("-") + 2);
                        if (nameRelationFormat.equals(nameRelationRechercheFormat)) {
                            if (MVCCDManager.instance().getCurrentDiagram().getMDTableShapeList().stream().anyMatch(e -> e.getEntity().getName().equals(nodeControleNom))) {
                                MPDAssociationShape relation = new MPDAssociationShape(MVCCDManager.instance().getCurrentDiagram().getMDTableShapeList().stream().filter(e -> e.getEntity().getName().equals(node.toString())).findFirst().orElseThrow(RuntimeException::new), MVCCDManager.instance().getCurrentDiagram().getMDTableShapeList().stream().filter(e -> e.getEntity().getName().equals(nodeControleNom)).findFirst().orElseThrow(RuntimeException::new), nameRelationFormat);
                                MVCCDManager.instance().getCurrentDiagram().addShape(relation);
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
