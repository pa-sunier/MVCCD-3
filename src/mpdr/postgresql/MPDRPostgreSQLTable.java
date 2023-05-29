package mpdr.postgresql;

import constraints.Constraint;
import constraints.Constraints;
import constraints.ConstraintsManager;
import exceptions.CodeApplException;
import main.MVCCDElementFactory;
import mldr.*;
import mldr.interfaces.IMLDRElement;
import mldr.interfaces.IMLDRSourceMPDRCConstraintSpecifc;
import mpdr.*;
import mpdr.interfaces.IMPDRConstraint;
import mpdr.postgresql.intefaces.IMPDRPostgreSQLElement;
import mpdr.tapis.*;
import mpdr.tapis.postgresql.MPDRPostgreSQLBoxProceduresOrFunctions;
import mpdr.tapis.postgresql.MPDRPostgreSQLBoxTriggers;
import mpdr.tapis.postgresql.MPDRPostgreSQLFunction;
import mpdr.tapis.postgresql.MPDRPostgreSQLTrigger;
import preferences.Preferences;
import preferences.PreferencesManager;
import project.ProjectElement;
import stereotypes.Stereotype;
import stereotypes.Stereotypes;
import stereotypes.StereotypesManager;

import java.util.ArrayList;

public class MPDRPostgreSQLTable extends MPDRTable implements IMPDRPostgreSQLElement {

    private  static final long serialVersionUID = 1000;

    public MPDRPostgreSQLTable(ProjectElement parent,  IMLDRElement mldrElementSource) {
        super(parent, mldrElementSource);
    }

    public MPDRPostgreSQLTable(ProjectElement parent,  IMLDRElement mldrElementSource, int id) {
        super(parent, mldrElementSource, id);
    }

    @Override
    public MPDRColumn createColumn(MLDRColumn mldrColumn) {
        MPDRPostgreSQLColumn newColumn = MVCCDElementFactory.instance().createMPDRPostgreSQLColumn(
                getMDRContColumns(),  mldrColumn);
        return newColumn;
    }

    @Override
    public MPDRColumnAudit createColumnAudit(MLDRConstraintCustomAudit mldrConstraintCustomAudit, Stereotype stereotype) {
        return null;
    }

    @Override
    public MPDRPK createPK(MLDRPK mldrPK)  {
        MPDRPostgreSQLPK newPK = MVCCDElementFactory.instance().createMPDRPostgreSQLPK(
            getMDRContConstraints(), mldrPK);
        return newPK;
    }

    @Override
    public MPDRFK createFK(MLDRFK mldrFK) {
        MPDRPostgreSQLFK newFK = MVCCDElementFactory.instance().createMPDRPostgreSQLFK(
                getMDRContConstraints(), mldrFK);
        return newFK;
    }

    @Override
    public MPDRIndex createIndex(MLDRFK mldrFK) {
        MPDRPostgreSQLIndex newIndex = MVCCDElementFactory.instance().createMPDRPostgreSQLIndex(
                getMDRContConstraints(), mldrFK);
        return newIndex;
    }

    @Override
    public MPDRUnique createUnique(MLDRUnique mldrUnique) {
        MPDRPostgreSQLUnique newUnique = MVCCDElementFactory.instance().createMPDRPostgreSQLUnique(
                getMDRContConstraints(), mldrUnique);
        return newUnique;
    }

    @Override
    public IMPDRConstraint createSpecialized(MLDRConstraintCustomSpecialized mldrSpecialized) {
        MPDRPostgreSQLConstraintCustomSpecialized newSpecialized = MVCCDElementFactory.instance().createMPDRPostgreSQLConstraintCustomSpecialized(
                getMDRContConstraints(), mldrSpecialized);
        return newSpecialized;
   }

    @Override
    public IMPDRConstraint createJnal(MLDRConstraintCustomJnal mldrJournal) {
        MPDRPostgreSQLConstraintCustomJnal newJnal = MVCCDElementFactory.instance().createMPDRPostgreSQLConstraintCustomJnal(
                getMDRContConstraints(), mldrJournal);
        return newJnal;
    }

    @Override
    public IMPDRConstraint createAudit(MLDRConstraintCustomAudit mldrAudit) {
        MPDRPostgreSQLConstraintCustomAudit newAudit = MVCCDElementFactory.instance().createMPDRPostgreSQLConstraintCustomAudit(
                getMDRContConstraints(), mldrAudit);
        return newAudit;
    }


    @Override
    public MPDRCheckSpecific createCheckSpecific(IMLDRSourceMPDRCConstraintSpecifc imldrSourceMPDRCConstraintSpecifc) {
        MPDRPostgreSQLCheckSpecific newCheck = MVCCDElementFactory.instance().createMPDRPostgreSQLCheckSpecific(
                getMDRContConstraints(), imldrSourceMPDRCConstraintSpecifc);
        return newCheck;
    }


    @Override
    public MPDRBoxTriggers createBoxTriggers(MLDRTable mldrTable) {
        MPDRPostgreSQLBoxTriggers mpdrPostgreSQLTriggers = MVCCDElementFactory.instance().createMPDRPostgreSQLBoxTriggers(
                getMPDRContTAPIs(), mldrTable);
        return mpdrPostgreSQLTriggers;
    }

    @Override
    public MPDRTrigger createTrigger(MPDRTriggerType mpdrTriggerType, IMLDRElement imldrElement) {
        MPDRPostgreSQLBoxTriggers mpdrPostgreSQLBoxTriggers = (MPDRPostgreSQLBoxTriggers) getMPDRBoxTriggers();
        if (mpdrPostgreSQLBoxTriggers != null){
            MPDRPostgreSQLTrigger mpdrPostgreSQLTrigger = MVCCDElementFactory.instance().createMPDRPostgreSQLTrigger(
                    mpdrPostgreSQLBoxTriggers, imldrElement);
            return mpdrPostgreSQLTrigger;
        } else {
            throw new CodeApplException("La boîte Triggers doit exister avant de créer un trigger");
        }
    }

    /*
    @Override
    public MPDRTrigger createTrigger(MPDRTriggerType mpdrTriggerType, MLDRTable mldrTable) {
        MPDRPostgreSQLBoxTriggers mpdrPostgreSQLBoxTriggers = (MPDRPostgreSQLBoxTriggers) getMPDRBoxTriggers();
        if (mpdrPostgreSQLBoxTriggers != null){
            MPDRPostgreSQLTrigger mpdrPostgreSQLTrigger = MVCCDElementFactory.instance().createMPDRPostgreSQLTrigger(
                    mpdrPostgreSQLBoxTriggers, mldrTable);
            return mpdrPostgreSQLTrigger;
        } else {
            throw new CodeApplException("La boîte Triggers doit exister avant de créer un trigger");
        }
    }

     */


    @Override
    public MPDRView createView(MLDRConstraintCustomSpecialized mldrSpecialized) {
        return null;
    }

    @Override
    public MPDRBoxProceduresOrFunctions createBoxProceduresOrFunctions(MLDRTable mldrTable) {
        MPDRBoxProceduresOrFunctions mpdrPostgreSQLBoxProceduresOrFunctions = MVCCDElementFactory.instance().createMPDRPostgreSQLBoxProceduresOrFunctions(
                getMPDRContTAPIs(), mldrTable);
        return mpdrPostgreSQLBoxProceduresOrFunctions;
    }


    @Override
    public MPDRFunction createFunction(MPDRFunctionType type, MLDRTable mldrTable) {
        MPDRPostgreSQLBoxProceduresOrFunctions mpdrPostgreSQLBoxProceduresOrFunctions = (MPDRPostgreSQLBoxProceduresOrFunctions) getMPDRBoxProceduresOrFunctions();
        if (mpdrPostgreSQLBoxProceduresOrFunctions != null){
            MPDRPostgreSQLFunction mpdrPostgreSQLFunction = MVCCDElementFactory.instance().createMPDRPostgreSQLFunction(
                    mpdrPostgreSQLBoxProceduresOrFunctions, mldrTable);
            return mpdrPostgreSQLFunction;
        } else {
            throw new CodeApplException("La boîte Procédures/Functions doit exister avant de créer un trigger");
        }

    }

    @Override
    public MPDRTableJnal createTableJnal(MLDRConstraintCustomJnal mldrConstraintCustomJnal) {
        MPDRTableJnal newTableJnal = MVCCDElementFactory.instance().createMPDROracleTableJnal(
                getMPDRContTAPIs(), mldrConstraintCustomJnal);
        return newTableJnal;
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

}
