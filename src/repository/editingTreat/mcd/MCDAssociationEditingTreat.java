package repository.editingTreat.mcd;

import main.MVCCDElement;
import main.MVCCDManager;
import mcd.*;
import mcd.services.MCDAssociationService;
import messages.MessagesBuilder;
import repository.editingTreat.EditingTreat;
import utilities.window.editor.DialogEditor;
import project.ProjectService;
import utilities.window.DialogMessage;
import utilities.window.editor.PanelInputContent;
import utilities.window.scomponents.services.SComboBoxService;
import window.editor.entity.EntityInputContent;
import window.editor.relation.association.AssociationEditor;
import window.editor.relation.association.AssociationInputContent;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.ArrayList;

public class MCDAssociationEditingTreat extends MCDRelationEditingTreat {


    public  MCDAssociation treatNew(Window owner,
                                    MVCCDElement parent) {

        MCDAssociation mcdAssociationNew = (MCDAssociation) super.treatNew( owner, parent);

        if (mcdAssociationNew != null) {
           addRelEndsInRepository(mcdAssociationNew);
        }
        return mcdAssociationNew;
    }

    public  MCDAssociation treatNew(Window owner,
                                    MCDContRelations parent,
                                    MCDEntity entityFrom,
                                    MCDEntity entityTo,
                                    MCDAssociationNature nature,
                                    boolean initFrozen) {

        DialogEditor fen = getDialogEditor(owner, parent, null, DialogEditor.NEW);
        AssociationInputContent content = (AssociationInputContent) fen.getInput().getInputContent();

        if (entityFrom  != null){
            SComboBoxService.selectByText(content.getFieldFromEntity(), entityFrom.getNamePath(content.getModePathName()));
            if (initFrozen)  {
                content.getFieldFromEntity().setReadOnly(true);
            }
        }
        if (entityTo  != null) {
            SComboBoxService.selectByText(content.getFieldToEntity(), entityTo.getNamePath(content.getModePathName()));
            if (initFrozen)  {
                content.getFieldToEntity().setReadOnly(true);
            }
        }
        if (nature  != null) {
            SComboBoxService.selectByText(content.getFieldNature(), nature.getText());
            if (initFrozen)  {
                content.getFieldNature().setReadOnly(true);
            }
        }

        fen.setVisible(true);
        MCDAssociation mcdAssociationNew = (MCDAssociation)  fen.getMvccdElementNew();

        if (mcdAssociationNew != null) {
            addRelEndsInRepository(mcdAssociationNew);
        }
        return mcdAssociationNew;
    }

/*
    public static void treatCompliant(Window window,
                                      MCDAssociation element) {

        if (MCDAssociationService.check(element).size() == 0){
            String message = MessagesBuilder.getMessagesProperty ("dialog.check.association.ok", new String[] {element.getName()});
            DialogMessage.showOk(window, message);
        } else {
            String message = MessagesBuilder.getMessagesProperty ("dialog.check.association.error", new String[] {element.getName()});
            if (DialogMessage.showConfirmYesNo_Yes(window, message) == JOptionPane.YES_OPTION){
                AssociationEditor fen = new AssociationEditor(window, (MCDContRelations) element.getParent(), element, DialogEditor.UPDATE);
                fen.setVisible(true);
            }
        }
    }
*/

       @Override
    protected ArrayList<String> checkCompliant(MVCCDElement mvccdElement) {
        ArrayList<String> resultat = new ArrayList<String>();
        return resultat;
    }

    @Override
    protected PanelInputContent getPanelInputContent(MVCCDElement element) {
        return new AssociationInputContent(element);
    }

    @Override
    protected DialogEditor getDialogEditor(Window owner,
                                           MVCCDElement parent,
                                           MVCCDElement element,
                                           String mode) {
        return new AssociationEditor(owner, (MCDContRelations) parent, (MCDAssociation) element,
                mode, new MCDAssociationEditingTreat());
    }

    @Override
    protected String getPropertyTheElement() {
        return "the.association";
    }


}
