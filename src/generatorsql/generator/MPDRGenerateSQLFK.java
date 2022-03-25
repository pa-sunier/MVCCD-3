package generatorsql.generator;

import generatorsql.MPDRGenerateSQLUtil;
import mpdr.MPDRFK;
import mpdr.MPDRPK;
import preferences.Preferences;

public abstract class MPDRGenerateSQLFK {


    public MPDRGenerateSQLFK() {
    }

    public String generateSQLFK(MPDRFK mpdrfk) {
        String generateSQLCode =  MPDRGenerateSQLUtil.template(getMPDRGenerateSQL().getTemplateDirAlterDB(),
                Preferences.TEMPLATES_ALTER_TABLE_FK,
                getMPDRGenerateSQL().mpdrModel);
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValueWithSpecific(generateSQLCode,
                Preferences.MDR_TABLE_NAME_CHILD_WORD, mpdrfk.getParent().getParent().getName());
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValueWithSpecific(generateSQLCode, Preferences.MDR_FK_NAME_WORD, mpdrfk.getName());
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValueWithSpecific(generateSQLCode, Preferences.MDR_FK_COLUMNS_WORD, mpdrfk.getParametersNameAsStr());

        //Contraite PK de référence
        MPDRPK reference = (MPDRPK) mpdrfk.getMdrPK();
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValueWithSpecific(generateSQLCode,
                Preferences.MDR_TABLE_NAME_PARENT_WORD, reference.getMDRTableAccueil().getName());
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValueWithSpecific(generateSQLCode, Preferences.MDR_FK_COLUMNS_REF_WORD, mpdrfk.getMDRColumnsRefPKNameAsParamStr());

        return generateSQLCode;
    }

    public abstract MPDRGenerateSQL getMPDRGenerateSQL() ;

}
