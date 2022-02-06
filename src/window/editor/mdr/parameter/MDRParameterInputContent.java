package window.editor.mdr.parameter;

import main.MVCCDElement;
import mdr.MDRParameter;
import preferences.Preferences;
import utilities.window.scomponents.STextField;
import utilities.window.services.PanelService;
import window.editor.mdr.utilities.PanelInputContentIdMDR;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MDRParameterInputContent extends PanelInputContentIdMDR {

    protected JPanel panelTarget = new JPanel ();

    private JLabel labelTargetClass;
    private STextField fieldTargetClass;
    private JLabel labelTargetName;
    private STextField fieldTargetName;
    private JLabel labelValue;
    private STextField fieldValue;


    public MDRParameterInputContent(MDRParameterInput MDRParameterInput)     {
        super(MDRParameterInput);
    }

    public MDRParameterInputContent(MVCCDElement element)     {
        super(element);
    }

    @Override
    public void createContentCustom() {
        super.createContentCustom();
        labelTargetClass = new JLabel("Type : ");
        fieldTargetClass = new STextField(this, labelTargetClass);
        fieldTargetClass.setPreferredSize((new Dimension(200, Preferences.EDITOR_FIELD_HEIGHT)));

        labelTargetName = new JLabel("Nom : ");
        fieldTargetName = new STextField(this, labelTargetName);
        fieldTargetName.setPreferredSize((new Dimension(200, Preferences.EDITOR_FIELD_HEIGHT)));
        fieldTargetName.setToolTipText("Nom de la cible du paramètre ...");
        
        labelValue = new JLabel("Valeur: ");
        fieldValue = new STextField(this, labelValue);
        fieldValue.setPreferredSize((new Dimension(500, Preferences.EDITOR_FIELD_HEIGHT)));
        fieldValue.setToolTipText("Valeur du paramètre...");



        super.getSComponents().add(fieldTargetClass);
        super.getSComponents().add(fieldTargetName);
        super.getSComponents().add(fieldValue);

        createPanelMaster();
    }

    private void createPanelMaster() {
        GridBagConstraints gbc = PanelService.createGridBagConstraints(panelInputContentCustom);

        gbc.gridwidth = 2;

        super.createPanelId();
        panelInputContentCustom.add(panelId, gbc);


        gbc.gridx = 0;
        gbc.gridy++;
        createPanelTarget();
        panelInputContentCustom.add(panelTarget, gbc);


        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy++;
        panelInputContentCustom.add(labelValue, gbc);
        gbc.gridx++;
        panelInputContentCustom.add(fieldValue, gbc);

        this.add(panelInputContentCustom);
    }


    private void createPanelTarget() {
        Border border = BorderFactory.createLineBorder(Color.black);
        TitledBorder panelDataypeBorder = BorderFactory.createTitledBorder(border, "Cible du paramètre");
        panelTarget.setBorder(panelDataypeBorder);

        panelTarget.setLayout(new GridBagLayout());
        GridBagConstraints gbcDT = new GridBagConstraints();
        gbcDT.anchor = GridBagConstraints.NORTHWEST;
        gbcDT.insets = new Insets(10, 10, 0, 0);

        gbcDT.gridx = 0;
        gbcDT.gridy = 0;
        gbcDT.gridwidth = 1;
        gbcDT.gridheight = 1;

        panelTarget.add(fieldTargetClass, gbcDT);

        gbcDT.gridx++;
        panelTarget.add(fieldTargetName, gbcDT);

    }



    @Override
    public void loadDatas(MVCCDElement mvccdElementCrt) {
        MDRParameter mdrParameter = (MDRParameter) mvccdElementCrt;
        super.loadDatas(mdrParameter);

        String targetClass = "";
        String targetName = "";
        if (mdrParameter.getTarget() != null){
            targetClass = mdrParameter.getTarget().getClass().getName();
            targetName = mdrParameter.getTarget().getName();
        }
        fieldTargetClass.setText(targetClass);
        fieldTargetName.setText(targetName);
        if (mdrParameter.getValue() != null){
            fieldValue.setText(mdrParameter.getValue());
        }
    }



}
