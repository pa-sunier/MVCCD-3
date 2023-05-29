package mpdr.tapis.postgresql;

import constraints.Constraint;
import constraints.Constraints;
import constraints.ConstraintsManager;
import main.MVCCDElementFactory;
import mldr.interfaces.IMLDRElement;
import mpdr.MPDRColumn;
import mpdr.MPDRConstraintCustomJnal;
import mpdr.tapis.MPDRColumnJnal;
import mpdr.tapis.MPDRTableJnal;
import preferences.Preferences;
import preferences.PreferencesManager;
import project.ProjectElement;
import stereotypes.Stereotype;
import stereotypes.Stereotypes;
import stereotypes.StereotypesManager;

import java.util.ArrayList;

public class MPDRPostgreSQLTableJnal extends MPDRTableJnal {


    public MPDRPostgreSQLTableJnal(ProjectElement parent, IMLDRElement mldrElementSource) {
        super(parent, mldrElementSource);
    }

    @Override
    public ArrayList<Stereotype> getStereotypes() {
        // Les stéréotypes doivent être ajoutés en respectant l'ordre d'affichage
        ArrayList<Stereotype> resultat = super.getStereotypes();

        Stereotypes stereotypes = StereotypesManager.instance().stereotypes();
        Preferences preferences = PreferencesManager.instance().preferences();

        resultat.add(stereotypes.getStereotypeByLienProg(this.getClass().getName(),
                preferences.STEREOTYPE_POSTGRESQL_LIENPROG));

        return resultat;
    }


    @Override
    public ArrayList<Constraint> getConstraints() {
        ArrayList<Constraint> resultat = super.getConstraints();

        Constraints constraints = ConstraintsManager.instance().constraints();
        Preferences preferences = PreferencesManager.instance().preferences();

        return resultat;
    }

    @Override
    public MPDRColumnJnal createColumnJnalTech(MPDRConstraintCustomJnal mpdrConstraintCustomJnal,
                                               Stereotype stereotype) {

        MPDRPostgreSQLColumnJnalTech newColumn = MVCCDElementFactory.instance().createMPDRPostgreSQLColumnJnalTech(
                getMDRContColumns(), mpdrConstraintCustomJnal, stereotype);

        return newColumn;
    }

    @Override
    public MPDRColumnJnal createColumnJnalDatas(MPDRColumn mpdrColumnSource) {

        MPDRPostgreSQLColumnJnalDatas newColumn = MVCCDElementFactory.instance().createMPDRPostgreSQLColumnJnalDatas(
                getMDRContColumns(), mpdrColumnSource);

        return newColumn;
    }

}
