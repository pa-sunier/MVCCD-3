package generatorsql.generator;

import generatorsql.MPDRGenerateSQLUtil;
import mpdr.MPDRColumn;
import mpdr.MPDRTable;
import preferences.Preferences;
import utilities.TemplateFile;

public abstract class MPDRGenerateSQLTable {

    public MPDRGenerateSQLTable() {
   }

    public String generateSQLDropTable(MPDRTable mpdrTable) {
        String generateSQLCode = "";
        generateSQLCode += TemplateFile.templateFileToString(getMPDRGenerateSQL().getTemplateDirDropDB(), Preferences.TEMPLATE_DROP_TABLE);
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValue(generateSQLCode,
                Preferences.MDR_TABLE_NAME_WORD, mpdrTable.getName());
        return generateSQLCode;
    }

    public String generateSQLCreateTable(MPDRTable mpdrTable) {
        String generateSQLCode = "";

        //Génération des tables
        generateSQLCode += TemplateFile.templateFileToString(getMPDRGenerateSQL().getTemplateDirCreateDB(), Preferences.TEMPLATE_CREATE_TABLE) ;
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValue(generateSQLCode,
                Preferences.MDR_TABLE_NAME_WORD, mpdrTable.getName());


        //Génération des colonnes
        String tabsApplicable = MPDRGenerateSQLUtil.tabsApplicable(generateSQLCode, Preferences.TEMPLATE_CREATE_TABLE_COLUMNS);
        String columnsInCreateTable = generateSQLCreateColumns(mpdrTable, tabsApplicable);
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValue(generateSQLCode, Preferences.TEMPLATE_CREATE_TABLE_COLUMNS, columnsInCreateTable);

        //Génération de la contrainte de PK
        //generateSQLCode += Preferences.SQL_SEPARATOR_ARGUMENTS + System.lineSeparator();
        MPDRGenerateSQLPK mpdrGenerateSQLPK = getMPDRGenerateSQLPK();
        String pkInCreateTable =mpdrGenerateSQLPK.generateSQLCreatePK(mpdrTable);
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValue(generateSQLCode, Preferences.TEMPLATE_CREATE_TABLE_PK, pkInCreateTable);

        generateSQLCode = MPDRGenerateSQLUtil.cleanSeparatorArguments(generateSQLCode);

        return generateSQLCode ;
    }


    public String generateSQLCreateColumns(MPDRTable mpdrTable, String tabsApplicable) {
        String generateSQLCode = "";
        // Avec nos règles de conformité, une table doit avoir au moins une colonne,

        boolean firstColumn = true;
        for (MPDRColumn mpdrColumn : mpdrTable.getMPDRColumns()) {
            if (!firstColumn) {
                generateSQLCode +=  System.lineSeparator() + tabsApplicable;
            }
            generateSQLCode += getMPDRGenerateSQLColumn().generateSQLCreateColumn(mpdrColumn);
            firstColumn = false;
        }
        return generateSQLCode;

    }


    protected abstract MPDRGenerateSQLPK getMPDRGenerateSQLPK();

    protected abstract MPDRGenerateSQLColumn getMPDRGenerateSQLColumn();

    public abstract MPDRGenerateSQL getMPDRGenerateSQL() ;

}
