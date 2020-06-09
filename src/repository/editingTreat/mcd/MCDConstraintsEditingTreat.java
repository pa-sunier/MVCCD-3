package repository.editingTreat.mcd;

import main.MVCCDElement;
import mcd.MCDContAttributes;
import mcd.MCDContConstraints;
import repository.editingTreat.EditingTreat;
import utilities.window.editor.DialogEditor;
import utilities.window.editor.PanelInputContent;
import window.editor.attributes.AttributesEditor;
import window.editor.operation.constraint.constraints.ConstraintsEditor;

import java.awt.*;
import java.util.ArrayList;

public class MCDConstraintsEditingTreat extends EditingTreat {

    @Override
    protected PanelInputContent getPanelInputContent(MVCCDElement element) {
        return null;
    }

    @Override
    protected DialogEditor getDialogEditor(Window owner, MVCCDElement parent, MVCCDElement element, String mode) {
        return new ConstraintsEditor(owner , parent, (MCDContConstraints)element, mode,
                new MCDConstraintsEditingTreat());
    }

    @Override
    protected String getPropertyTheElement() {
        return null;
    }



    @Override
    protected ArrayList<String> checkCompliant(MVCCDElement mvccdElement) {
        ArrayList<String> resultat = new ArrayList<String>();
        return resultat;
    }
}
