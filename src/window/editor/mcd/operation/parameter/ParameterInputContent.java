package window.editor.mcd.operation.parameter;

import exceptions.CodeApplException;
import m.MElement;
import main.MVCCDElement;
import main.MVCCDElementConvert;
import main.MVCCDElementFactory;
import mcd.*;
import mcd.interfaces.IMCDParameter;
import mcd.services.MCDAssEndService;
import mcd.services.MCDParameterService;
import utilities.window.editor.PanelInputContent;
import utilities.window.scomponents.SComboBox;
import utilities.window.scomponents.SComponent;
import utilities.window.scomponents.services.SComboBoxService;
import utilities.window.services.PanelService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;

public class ParameterInputContent extends PanelInputContent {

    //TODO-1 Mettre une constante globale int = -1
    private int scopeForCheckInput = -1;

    private SComboBox<String> fieldTarget ;
    private JLabel labelTarget;

    private ArrayList<MVCCDElement> targets= new ArrayList<MVCCDElement>();


    public ParameterInputContent(ParameterInput parameterInput)     {

        super(parameterInput);
    }


    public ParameterInputContent(MVCCDElement element, int scopeForCheckInput)     {
        super(null);
        elementForCheckInput = element;
        this.scopeForCheckInput = scopeForCheckInput;
    }

    public void createContentCustom() {

        createTargets();

        labelTarget = new JLabel("Paramètres potentiels");
        fieldTarget = new SComboBox(this, labelTarget);
        fieldTarget.setCheckPreSave(true);
        fieldTarget.addItem(SComboBox.LINEWHITE);
        for (MVCCDElement targetPotentialChecked : targets){
            if (targetPotentialChecked instanceof IMCDParameter) {
                fieldTarget.addItem(((IMCDParameter)targetPotentialChecked).getNameTarget());
            }
        }

        fieldTarget.addItemListener(this);
        fieldTarget.addFocusListener(this);

        super.getSComponents().add(fieldTarget);

        createPanelMaster();
    }

    public void createTargets(){

        MCDOperation mcdOperation = (MCDOperation) getEditor().getMvccdElementParent();
        MCDEntity mcdEntity = (MCDEntity) mcdOperation.getParent().getParent();
        ArrayList<MVCCDElement> targetsPotential = new ArrayList<MVCCDElement>();

        if (getEditor().getScope() == ParameterEditor.NID){
            // Attributs
            targetsPotential = MVCCDElementConvert.to(
                    MCDParameterService.createTargetsAttributesNID(mcdEntity));
            //#MAJ 2021-05-19 Affinement MCDUnicity
            /*
            if (mcdOperation instanceof MCDNID) {
                if (((MCDNID) mcdOperation).isAbsolute()) {
                    // Associations identifiantes
                    //#MAJ 2021-05-18 ParameterInputContent (Inversion)
                    //targetsPotential.addAll(MCDParameterService.createTargetsAssEndsIdAndNN(mcdEntity));
                    //targetsPotential.addAll(mcdEntity.getAssEndsIdAndNNChild());
                    // Associations non identifiantes
                    targetsPotential.addAll(mcdEntity.getAssEndsIdAndNNChild());
                    targetsPotential.addAll(mcdEntity.getAssEndsNoIdAndNoNNChild());
                }
            }

             */

        }

        if (getEditor().getScope() == ParameterEditor.UNIQUE){
            //#MAJ 2021-05-21 Affinement MCDUnicity
            MCDUnique mcdUnique = (MCDUnique) mcdOperation ;
            if (((MCDUnique) mcdOperation).isAbsolute()){
                // Contrainte absolue

                // Attribut
                targetsPotential = MVCCDElementConvert.to(
                        MCDParameterService.createTargetsAttributesUnique(mcdEntity));

                // Extrémités d'associations
                //#MAJ 2021-05-30 NameTarget
                /*
                targetsPotential.addAll(mcdEntity.getAssEndsIdChild());
                targetsPotential.addAll(mcdEntity.getAssEndsNoIdChild());
                targetsPotential.addAll(mcdEntity.getAssEndsAssNNChild());

                 */
                targetsPotential.addAll(MCDAssEndService.getMCDAssEndsOpposites(mcdEntity.getMCDAssEndsIdChild()));
                targetsPotential.addAll(MCDAssEndService.getMCDAssEndsOpposites(mcdEntity.getMCDAssEndsNoIdChild()));
                targetsPotential.addAll(MCDAssEndService.getMCDAssEndsOpposites(mcdEntity.getMCDAssEndsNN()));
            } else {
                //Contrainte non absolue

                // Attribut
                targetsPotential = MVCCDElementConvert.to(
                        MCDParameterService.createTargetsAttributesOptionnalUnique(mcdEntity));

                // Extrémités d'associations
                targetsPotential.addAll(MCDAssEndService.getMCDAssEndsOpposites(mcdEntity.getMCDAssEndsNoIdOptionnalChild()));
            }
            //#MAJ 2021-05-21 Affinement MCDUnicity
            /*
            // Attributs
            targetsPotential = MVCCDElementConvert.to(
                    MCDParameterService.createTargetsAttributesUnique(mcdEntity));
            //#MAJ 2021-05-19 Affinement MCDUnicity
            targetsPotential.addAll(mcdEntity.getAssEndsNoIdAndNoNNChild());

            // Associations non identifiantes
            //#MAJ 2021-05-18 ParameterInputContent (Inversion)
            //targetsPotential.addAll(MCDParameterService.createTargetsAssEndsNoIdAndNoNN(mcdEntity));
            if (mcdOperation instanceof MCDUnique) {
                if (((MCDUnique) mcdOperation).isAbsolute()) {
                    // Associations identifiantes
                    //#MAJ 2021-05-18 ParameterInputContent (Inversion)
                    //targetsPotential.addAll(MCDParameterService.createTargetsAssEndsIdAndNN(mcdEntity));
                    targetsPotential.addAll(mcdEntity.getAssEndsIdAndNNChild());
                }
            }

             */
        }

        for (MVCCDElement mvccdElement : targetsPotential) {
            if (!MCDParameterService.existTargetInParameters(mcdOperation.getParameters(), (MElement) mvccdElement)) {
                targets.add(mvccdElement);
            }
        }
    }


    private void createPanelMaster() {
        GridBagConstraints gbc = PanelService.createGridBagConstraints(panelInputContentCustom);

        panelInputContentCustom.add(labelTarget, gbc);
        gbc.gridx = 1;
        panelInputContentCustom.add(fieldTarget, gbc);
    }





    protected boolean changeField(DocumentEvent e) {
        boolean ok = true ;


        SComponent sComponent = null;
        Document doc = e.getDocument();

        return ok;
    }

    @Override
    protected void changeFieldSelected(ItemEvent e) {
        Object source = e.getSource();

        if (source instanceof SComponent){
            treatField( (SComponent) source);
        }

    }


    @Override
    protected void changeFieldDeSelected(ItemEvent e) {
        Object source = e.getSource();

        if (source instanceof SComponent){
            treatField( (SComponent) source);
        }

    }





    @Override
    public void focusGained(FocusEvent focusEvent) {
        super.focusGained(focusEvent);


        Object source = focusEvent.getSource();

        if (source instanceof SComponent) {
            treatField((SComponent) source);
        }
    }



    @Override
    public void focusLost(FocusEvent focusEvent) {

    }

    @Override
    public void loadSimulationChange(MVCCDElement mvccdElementCrt) {

    }

    private boolean treatField(SComponent sComponent) {

        return checkDatas(sComponent);
    }

    @Override
    public boolean checkDatasPreSave(SComponent sComponent) {
        boolean ok = super.checkDatasPreSave(sComponent);
        boolean notBatch = panelInput != null;
        boolean unitaire;

        unitaire = notBatch && (sComponent == fieldTarget);
        ok = checkTarget(unitaire) && ok ;


        super.setPreSaveOk(ok);
        return ok;
    }



    public boolean checkDatas(SComponent sComponent){
        boolean ok = super.checkDatas(sComponent);
        return ok;
    }


    @Override
    protected void enabledContent() {

    }



    private boolean checkTarget(boolean unitaire) {
        IMCDParameter iMCDParameterTarget = getTargetByNameTarget((String) fieldTarget.getSelectedItem());
        
        return super.checkInput(fieldTarget, unitaire,
                MCDParameterService.checkTarget(
                        iMCDParameterTarget,
                        (String) fieldTarget.getSelectedItem(),
                        getScope()));
    }




    @Override
    protected void initDatas() {
        MCDParameter forInitParameter = MVCCDElementFactory.instance().createMCDParameter(
                (MCDConstraint) getEditor().getMvccdElementParent());
        loadDatas(forInitParameter);
        forInitParameter.removeInParent();
        forInitParameter = null;
     }

    @Override
    public void loadDatas(MVCCDElement mvccdElement) {
        MCDParameter mcdParameter =(MCDParameter) mvccdElement;

        if (mcdParameter.getTarget() != null) {
            SComboBoxService.selectByText(fieldTarget, mcdParameter.getTarget().getName());
        } else {
            fieldTarget.setSelectedEmpty();
        }
    }


    @Override
    public void saveDatas(MVCCDElement mvccdElement) {
        MCDParameter mcdParameter =(MCDParameter) mvccdElement;

        if (fieldTarget.checkIfUpdated()) {
            String nameTreeTarget = (String) fieldTarget.getSelectedItem();
            IMCDParameter iMCDParameterTarget = getTargetByNameTarget(nameTreeTarget);
            mcdParameter.setTarget(iMCDParameterTarget);

            if (iMCDParameterTarget != null) {
                mcdParameter.setTarget(iMCDParameterTarget);
            } else {
                throw new CodeApplException("Aucune cible de paramètre n'a été trouvée pour le nom: " + nameTreeTarget);
            }
        }
    }

    private IMCDParameter getTargetByName(String nameTarget) {
        for (MVCCDElement target : targets) {
            if (target instanceof IMCDParameter) {
                IMCDParameter iMCDParameterTarget = (IMCDParameter) target;
                if (iMCDParameterTarget.getName().equals(nameTarget)) {
                    return iMCDParameterTarget;
                }
            }
        }
        return null;
    }

    private IMCDParameter getTargetByNameTarget(String nameTarget) {
        for (MVCCDElement target : targets) {
            if (target instanceof IMCDParameter) {
                IMCDParameter iMCDParameterTarget = (IMCDParameter) target;
                if (iMCDParameterTarget.getNameTarget().equals(nameTarget)) {
                    return iMCDParameterTarget;
                }
            }
        }
        return null;
    }

    private int getScope() {
        if (scopeForCheckInput == -1) {
            return ((ParameterEditor) getEditor()).getScope();
        } else {
            return scopeForCheckInput;
        }
    }

}
