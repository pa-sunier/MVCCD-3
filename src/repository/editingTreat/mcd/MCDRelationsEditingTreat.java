package repository.editingTreat.mcd;

import main.MVCCDElement;
import main.MVCCDManager;
import mcd.MCDRelation;
import repository.editingTreat.EditingTreat;
import utilities.window.editor.DialogEditor;
import utilities.window.editor.PanelInputContent;

import java.awt.*;
import java.util.ArrayList;

public class MCDRelationsEditingTreat extends EditingTreat {

    @Override
    protected ArrayList<String> checkCompliant(MVCCDElement mvccdElement) {
        ArrayList<String> resultat = new ArrayList<String>();
        return resultat;

    }

    @Override
    protected PanelInputContent getPanelInputContent(MVCCDElement element) {
        return null;
    }

    @Override
    protected DialogEditor getDialogEditor(Window owner, MVCCDElement parent, MVCCDElement element, String mode) {
        return null;
    }

    @Override
    protected String getPropertyTheElement() {
        return "the.container";
    }


    @Override
    protected void removeMVVCCDChildInRepository(MVCCDElement child) {
        MCDRelation mcdRelation = (MCDRelation) child;
        MVCCDManager.instance().removeMCDRelationAndDependantsInRepository(mcdRelation);
    }
}