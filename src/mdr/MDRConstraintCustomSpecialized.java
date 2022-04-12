package mdr;

import constraints.Constraint;
import preferences.Preferences;
import preferences.PreferencesManager;
import project.ProjectElement;
import stereotypes.Stereotype;
import stereotypes.Stereotypes;
import stereotypes.StereotypesManager;

import java.util.ArrayList;

public abstract class MDRConstraintCustomSpecialized extends MDRConstraintCustom  {

    private  static final long serialVersionUID = 1000;



    public MDRConstraintCustomSpecialized(ProjectElement parent) {
        super(parent);
    }

    public MDRConstraintCustomSpecialized(ProjectElement parent, int id) {
        super(parent, id);
    }

    public  Stereotype getDefaultStereotype(){
        Stereotypes stereotypes = StereotypesManager.instance().stereotypes();
        Preferences preferences = PreferencesManager.instance().preferences();
        return stereotypes.getStereotypeByLienProg(MDRConstraintCustomSpecialized.class.getName(),
                preferences.STEREOTYPE_SPECIALIZED_LIENPROG);
    }


    @Override
    public ArrayList<Stereotype> getStereotypes() {
        // Les stéréotypes doivent être ajoutés en respectant l'ordre d'affichage
        ArrayList<Stereotype> resultat = new ArrayList<Stereotype>();

        Stereotypes stereotypes = StereotypesManager.instance().stereotypes();
        Preferences preferences = PreferencesManager.instance().preferences();

        resultat.add(getDefaultStereotype());
        return resultat;
    }


    @Override
    public ArrayList<Constraint> getConstraints() {
        return new ArrayList<Constraint>();
    }

}
