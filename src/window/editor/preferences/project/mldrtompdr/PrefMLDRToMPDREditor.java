package window.editor.preferences.project.mldrtompdr;

import main.MVCCDElement;
import preferences.Preferences;
import preferences.PreferencesManager;
import repository.editingTreat.EditingTreat;
import utilities.window.editor.*;
import window.editor.preferences.project.PrefEditor;
import window.editor.preferences.project.PrefNavTree;
import window.editor.preferences.project.PrefProjectMenu;

import java.awt.*;

public class PrefMLDRToMPDREditor extends PrefEditor {


    //TODO-0 Il faut appuyer 2 * pour fermer la fenêtre!
    public PrefMLDRToMPDREditor(Window owner,
                                MVCCDElement parent,
                                Preferences preferences,
                                String mode,
                                EditingTreat editingTreat)  {
        super(owner, parent, preferences, mode, editingTreat);
    }

    @Override
    protected PanelButtons getButtonsCustom() {
        return new PrefMLDRToMPDRButtons(this);
    }

    @Override
    protected PanelInput getInputCustom() {
        return new PrefMLDToMPDRRInput(this);
    }

    @Override
    protected Object getNavNodeMenu() {
        return PrefProjectMenu.MLDRToMPDR;
    }

}