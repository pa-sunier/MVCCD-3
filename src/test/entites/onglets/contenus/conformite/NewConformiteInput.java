package test.entites.onglets.contenus.conformite;

import test.entites.EntiteOnglets;
import utilities.window.editor.PanelInput;
import utilities.window.editor.PanelInputContent;

public class NewConformiteInput extends PanelInput {
    final String name = "Conformité";

    public NewConformiteInput(EntiteOnglets entiteOnglets) {
        super(entiteOnglets);
    }

    @Override
    protected PanelInputContent createInputContentCustom() {
        return new NewConformiteInputContent(this);
    }

    @Override
    public String getName() {
        return name;
    }
}