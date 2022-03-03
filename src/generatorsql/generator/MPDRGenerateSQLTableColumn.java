package generatorsql.generator;

import datatypes.MDDatatypeService;
import generatorsql.MPDRGenerateSQLUtil;
import mpdr.MPDRColumn;
import org.apache.commons.lang.StringUtils;
import preferences.Preferences;

public abstract class MPDRGenerateSQLTableColumn {


    public MPDRGenerateSQLTableColumn() {
    }

    public String generateSQLCreateTableColumn(MPDRColumn mpdrColumn) {
        // Nom
        String  generateSQLCode =  MPDRGenerateSQLUtil.template(getMPDRGenerateSQL().getTemplateDirCreateDB(),
                Preferences.TEMPLATE_CREATE_TABLE_COLUMNS,
                getMPDRGenerateSQL().mpdrModel) + Preferences.SQL_MARKER_SEPARATOR_ARGUMENTS;;

        generateSQLCode = getMPDRGenerateSQL().replaceKeyValueWithSpecific(generateSQLCode, Preferences.MDR_COLUMN_NAME_WORD, mpdrColumn.getName());

        // Datatype
        String datatypeName = MDDatatypeService.convertMPDRLienProgToName(getMPDRGenerateSQL().mpdrModel.getDb(), mpdrColumn.getDatatypeLienProg());
        String datatypeSizeScale = generateDatatypeSizeScale(mpdrColumn);
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValueWithSpecific(generateSQLCode, Preferences.MDR_COLUMN_DATATYPE_WORD, datatypeName + datatypeSizeScale);


        //Options
        String codeOption = "";

        // Options identité
        boolean c1 = pkGenerateIdentity();
        boolean c2 = mpdrColumn.isPkNotFk();
        boolean c3 = mpdrColumn.getMPDRTableAccueil().isIndependant();

        boolean identityColumn = c1 && c2 && c3;
        if (identityColumn){
            codeOption =  MPDRGenerateSQLUtil.template(getMPDRGenerateSQL().getTemplateDirOptionsDB(),
                    Preferences.TEMPLATE_OPTION_COLUMN_IDENTITY,
                    getMPDRGenerateSQL().mpdrModel);
        }
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValueWithSpecific(generateSQLCode, Preferences.TEMPLATE_OPTION_COLUMN_IDENTITY, codeOption);

        // Options not null
        codeOption = "";
        if (mpdrColumn.isMandatory()  && (! identityColumn)) {
            codeOption =  MPDRGenerateSQLUtil.template(getMPDRGenerateSQL().getTemplateDirOptionsDB(),
                    Preferences.TEMPLATE_OPTION_COLUMN_NOTNULL,
                    getMPDRGenerateSQL().mpdrModel);
        }
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValueWithSpecific(generateSQLCode, Preferences.TEMPLATE_OPTION_COLUMN_NOTNULL, codeOption);


        // Option default value
        codeOption = "";
        if (StringUtils.isNotEmpty(mpdrColumn.getInitValue())  && (! identityColumn)) {
            codeOption =  MPDRGenerateSQLUtil.template(getMPDRGenerateSQL().getTemplateDirOptionsDB(),
                Preferences.TEMPLATE_OPTION_COLUMN_DEFAULTVALUE,
                getMPDRGenerateSQL().mpdrModel);
            //TODO-0 Vérfier que mpdrColumn.getInitValue() rendre une valeur conforme au mpdr cible
            codeOption = getMPDRGenerateSQL().replaceKeyValueWithSpecific(codeOption, Preferences.MDR_DEFAULT_VALUE_WORD, mpdrColumn.getInitValue());

        }
        generateSQLCode = getMPDRGenerateSQL().replaceKeyValueWithSpecific(generateSQLCode, Preferences.TEMPLATE_OPTION_COLUMN_DEFAULTVALUE, codeOption);

        return generateSQLCode;
    }

    protected abstract boolean pkGenerateIdentity();

    public abstract MPDRGenerateSQL getMPDRGenerateSQL() ;

    protected abstract String generateDatatypeSizeScale(MPDRColumn mpdrColumn);
}