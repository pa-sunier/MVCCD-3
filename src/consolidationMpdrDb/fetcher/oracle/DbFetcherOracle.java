package consolidationMpdrDb.fetcher.oracle;

import consolidationMpdrDb.fetcher.DbFetcher;
import connections.ConConnection;
import main.MVCCDElementFactory;
import mldr.MLDRParameter;
import mpdr.*;
import mpdr.oracle.*;
import mpdr.oracle.interfaces.IMPDROracleElement;
import mpdr.tapis.oracle.MPDROraclePackage;
import mpdr.tapis.oracle.MPDROracleTrigger;
import preferences.Preferences;
import preferences.PreferencesManager;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

public class DbFetcherOracle extends DbFetcher {

    private final Connection connection;
    private final ConConnection conConnection;
    private Map<String, String> triggersBody = new HashMap<>();
    private Map<String, String> packagesSpec = new HashMap<>();
    private DatabaseMetaData databaseMetaData;
    private String schemaDB;
    private String databaseName;
    private MPDROracleModel dbModel;
    private List<String> sequencesNotInTable = new ArrayList<>();
    private List<String> packagesNotInTable = new ArrayList<>();
    private List<String> triggersNotInTable = new ArrayList<>();
    private Preferences pref = PreferencesManager.instance().getProjectPref();

    public DbFetcherOracle(ConConnection conConnection, Connection connection) throws SQLException {
        this.connection = connection;
        this.conConnection = conConnection;
        this.databaseMetaData = this.connection.getMetaData();
        this.databaseName = this.conConnection.getDbName();
        this.schemaDB = this.conConnection.getUserName();
        this.dbModel = MVCCDElementFactory.instance().createMPDRModelOracle(null);
        this.dbModel.setName(Preferences.FETCHER_ORACLE_MPDR_NAME);
    }

    public void fetch() throws SQLException {
        fetchTables();
        fetchTriggers(dbModel.getMPDRTables());
        fetchSequences(dbModel.getMPDRTables());
        fetchPackages(dbModel.getMPDRTables());
        //fetchIndex(); //Non implémenté dans ce projet - Vincent
    }


    private void fetchTables() throws SQLException {
        String[] types = {Preferences.FETCHER_ORACLE_TYPE_TABLE};
        try (ResultSet rsTables = databaseMetaData.getTables(databaseName, schemaDB, "%", types)) {
            while (rsTables.next()) {
                MPDROracleTable dbTable = MVCCDElementFactory.instance()
                        .createMPDROracleTable(this.dbModel.getMPDRContTables(), null);
                dbTable.setName(rsTables.getString(Preferences.FETCHER_ORACLE_TABLE_NAME));
                //On crée un box pour les packages et triggers pour éviter d'avoir un nullpointeur lors des comparaisons quand on boucle sur les box
                MVCCDElementFactory.instance().createMPDROracleBoxPackages(dbTable.getMPDRContTAPIs(), null);
                MVCCDElementFactory.instance().createMPDROracleBoxTriggers(dbTable.getMPDRContTAPIs(), null);
                fetchColumns(dbTable);
                fetchPk(dbTable);
                fetchUnique(dbTable);
                fetchCheck(dbTable);
                fetchFk(dbTable);
            }
        }
    }

    private void fetchColumns(MPDROracleTable dbTable) throws SQLException {
        try (ResultSet rsColumns = this.databaseMetaData.getColumns(this.databaseName, this.schemaDB, dbTable.getName(), null)) {
            while (rsColumns.next()) {
                MPDROracleColumn dbColumn = MVCCDElementFactory.instance().createMPDROracleColumn(dbTable.getMDRContColumns(), null);
                dbColumn.setName(rsColumns.getString(Preferences.FETCHER_ORACLE_COLUMN_NAME));
                dbColumn.setDatatypeLienProg(rsColumns.getString(Preferences.FETCHER_ORACLE_TYPE_NAME));
                dbColumn.setMandatory(!rsColumns.getBoolean(Preferences.FETCHER_ORACLE_NULLABLE)); //Si nullable -> non obligatoire
                dbColumn.setSize(rsColumns.getInt(Preferences.FETCHER_ORACLE_COLUMN_SIZE));
                dbColumn.setScale(rsColumns.getInt(Preferences.FETCHER_ORACLE_DECIMAL_DIGITS));
                dbColumn.setInitValue(rsColumns.getString(Preferences.FETCHER_ORACLE_COLUMN_DEF));
                dbColumn.setOrder(rsColumns.getInt(Preferences.FETCHER_ORACLE_ORDINAL_POSITION));
            }
        }
    }


    private void fetchPk(MPDROracleTable dbTable) throws SQLException {
        MPDROraclePK dbPK = null;
        try (ResultSet rsPk = this.databaseMetaData.getPrimaryKeys(this.databaseName, this.schemaDB, dbTable.getName())) {
            while (rsPk.next()) {
                if (dbPK == null) {
                    dbPK = MVCCDElementFactory.instance().createMPDROraclePK(dbTable.getMDRContConstraints(), null);
                    dbPK.setName(rsPk.getString(Preferences.FETCHER_ORACLE_PK_NAME));
                }
                for (MPDRColumn dbColumn : dbTable.getMPDRColumns()) {
                    if (dbColumn.getName().equals(rsPk.getString(Preferences.FETCHER_ORACLE_COLUMN_NAME))) {
                        MPDRParameter mpdrParameter = MVCCDElementFactory.instance().createMPDROracleParameter(dbPK, (MLDRParameter) null);
                        //Ajout de la méthode getId() dans la classe "ProjectElement"
                        mpdrParameter.setTargetId(dbColumn.getId());
                        mpdrParameter.setName(dbColumn.getName());
                        dbColumn.setPk(true);
                    }
                }
            }
        }
    }

    private void fetchUnique(MPDROracleTable dbTable) throws SQLException {
        MPDROracleUnique dbUnique = null;
        //On ne récupère que les contraintes Unique
        String requeteSQL = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONSTRAINTS;
        PreparedStatement pStmtUnique = connection.prepareStatement(requeteSQL);
        pStmtUnique.setString(1, Preferences.FETCHER_ORACLE_CONSTRAINT_TYPE_UNIQUE);
        pStmtUnique.setString(2, dbTable.getName());
        ResultSet rsCurseurUnique = pStmtUnique.executeQuery();
        while (rsCurseurUnique.next()) {
            //Si le nom de la contrainte n'existe pas, on en crée une
            if (!dbTable.getMPDRUniques().contains(rsCurseurUnique.getString((Preferences.FETCHER_ORACLE_CONSTRAINT_NAME)))) {
                dbUnique = MVCCDElementFactory.instance().createMPDROracleUnique(dbTable.getMDRContConstraints(), null);
                dbUnique.setName(rsCurseurUnique.getString(Preferences.FETCHER_ORACLE_CONSTRAINT_NAME));
            }
        }
        pStmtUnique.close();
        rsCurseurUnique.close();
        //On récupère toutes les contraintes et les colonnes auquelles elles sont rattachées
        String requeteSQLUserConsColumns = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONS_COLUMNS;
        PreparedStatement pStmtUserConsColumns = connection.prepareStatement(requeteSQLUserConsColumns);
        pStmtUserConsColumns.setString(1, dbTable.getName());
        ResultSet rsCurseurUserConsColumns = pStmtUserConsColumns.executeQuery();
        while (rsCurseurUserConsColumns.next()) {
            String constraintName = rsCurseurUserConsColumns.getString(Preferences.FETCHER_ORACLE_CONSTRAINT_NAME);
            String columnName = rsCurseurUserConsColumns.getString(Preferences.FETCHER_ORACLE_COLUMN_NAME);
            for (MPDRUnique dbUniqueConst : dbTable.getMPDRUniques()) {
                if (dbUniqueConst instanceof MPDROracleUnique) {
                    //si la position est null, il s'agit d'une contrainte qui doit être mise que sur une seule colonne (par exemple NOT NULL)
                    if (constraintName.equals(dbUniqueConst.getName())) {
                        MPDRParameter dbParameter = MVCCDElementFactory.instance().createMPDROracleParameter((IMPDROracleElement) dbUniqueConst, (MLDRParameter) null);
                        for (MPDRColumn dbColumn : dbTable.getMPDRColumns()) {
                            if (dbColumn.getName().equals(columnName)) {
                                dbParameter.setTargetId(dbColumn.getId());
                            }
                        }
                    }
                }
            }
        }
        pStmtUserConsColumns.close();
        rsCurseurUserConsColumns.close();
    }

    private void fetchCheck(MPDRTable dbTable) throws SQLException {
        MPDROracleCheckSpecific dbCheckSpecific;
        MPDROracleParameter dbParameter;
        StringBuilder requeteSQL = new StringBuilder();
        //Utilisation de la colonne search_condition_vc (introduit à la version 12c) qui est de type varchar car search_condition est de type long
        //Lors de la création d'une contrainte sans nom, la DB en crée une avec le nom qui commence par SYS_
        requeteSQL.append(Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONSTRAINTS);
        requeteSQL.append(Preferences.FETCHER_ORACLE_AND);
        requeteSQL.append(Preferences.FETCHER_ORACLE_REQUETE_SEARCH_CONDITION_VC);
        PreparedStatement pStmtChecks = connection.prepareStatement(requeteSQL.toString());
        pStmtChecks.setString(1, Preferences.FETCHER_ORACLE_CONSTRAINT_TYPE_CHECK);
        pStmtChecks.setString(2, dbTable.getName());
        ResultSet rsCursorChecks = pStmtChecks.executeQuery();
        while (rsCursorChecks.next()) {
            String constraintName = rsCursorChecks.getString(Preferences.FETCHER_ORACLE_CONSTRAINT_NAME);
            String condition = rsCursorChecks.getString(Preferences.FETCHER_ORACLE_SEARCH_CONDITION_VC);
            dbCheckSpecific = MVCCDElementFactory.instance().createMPDROracleCheckSpecific(dbTable.getMDRContConstraints(), null);
            dbCheckSpecific.setName(constraintName);
            dbParameter = (MPDROracleParameter) MVCCDElementFactory.instance().createMPDROracleParameter(dbCheckSpecific, (MLDRParameter) null);
            dbParameter.setValue(condition);
        }
        pStmtChecks.close();
        rsCursorChecks.close();
    }

    private void fetchFk(MPDRTable dbTable) throws SQLException {
        MPDROracleFK dbFK = null;
        String requeteSQL = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONSTRAINTS;
        PreparedStatement pStmtFks = connection.prepareStatement(requeteSQL);
        pStmtFks.setString(1, Preferences.FETCHER_ORACLE_CONSTRAINT_TYPE_FK);
        pStmtFks.setString(2, dbTable.getName());
        ResultSet rsCursorFks = pStmtFks.executeQuery();
        while (rsCursorFks.next()) {
            if (!dbTable.getMPDRFKs().contains(rsCursorFks.getString((Preferences.FETCHER_ORACLE_CONSTRAINT_NAME)))) {
                dbFK = MVCCDElementFactory.instance().createMPDROracleFK(dbTable.getMDRContConstraints(), null);
                dbFK.setName(rsCursorFks.getString(Preferences.FETCHER_ORACLE_CONSTRAINT_NAME));
                if (rsCursorFks.getString("DELETE_RULE").equals("CASCADE")) {
                    dbFK.setDeleteCascade(true);
                }
            }

            String requeteSQLUserConsColumns = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONS_COLUMNS;
            PreparedStatement pStmtUserConsColumns = connection.prepareStatement(requeteSQLUserConsColumns);
            pStmtUserConsColumns.setString(1, dbTable.getName());
            ResultSet rsCurseurUserConsColumns = pStmtUserConsColumns.executeQuery();
            while (rsCurseurUserConsColumns.next()) {
                String constraintName = rsCurseurUserConsColumns.getString(Preferences.FETCHER_ORACLE_CONSTRAINT_NAME);
                String columnName = rsCurseurUserConsColumns.getString(Preferences.FETCHER_ORACLE_COLUMN_NAME);
                for (MPDRColumn dbColumn : dbTable.getMPDRColumns()) {
                    if (constraintName.equals(dbFK.getName())) {
                        if (dbColumn.getName().equals(rsCurseurUserConsColumns.getString(Preferences.FETCHER_ORACLE_COLUMN_NAME))) {
                            MPDRParameter dbParameter = MVCCDElementFactory.instance().createMPDROracleParameter(dbFK, (MLDRParameter) null);
                            dbParameter.setTargetId(dbColumn.getId());
                            //TODO VINCENT
                            //A VOIR POUR AFFECTER LA COLONNE PK setmdrColumnPK si nécessaire
                        }
                    }
                }
            }
            pStmtUserConsColumns.close();
            rsCurseurUserConsColumns.close();
        }
        pStmtFks.close();
        rsCursorFks.close();
    }

    private void fetchSequences(List<MPDRTable> dbTables) throws SQLException {
        String requeteSQL = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_SEQUENCES;
        PreparedStatement pStmtSequences = connection.prepareStatement(requeteSQL);
        ResultSet rsCursorSequences = pStmtSequences.executeQuery();
        while (rsCursorSequences.next()) {
            String sequenceName = rsCursorSequences.getString(Preferences.FETCHER_ORACLE_SEQUENCE_NAME);
            sequencesNotInTable.add(sequenceName);
            for (MPDRTable dbTable : dbTables) {
                if (findTableAccueilleSequence2(dbTable, sequenceName) != null) {
                    MPDROracleSequence mpdrOracleSequence;
                    for (MPDRColumn mpdrColumn : dbTable.getMPDRColumns()) {
                        if (mpdrColumn.getIsPk()) {
                            mpdrOracleSequence = MVCCDElementFactory.instance().createMPDROracleSequence(mpdrColumn, null);
                            mpdrOracleSequence.setName(sequenceName);
                            mpdrOracleSequence.setMinValue(rsCursorSequences.getInt(Preferences.FETCHER_ORACLE_MIN_VALUE));
                            mpdrOracleSequence.setMinValue(rsCursorSequences.getInt(Preferences.FETCHER_ORACLE_INCREMENT_BY));
                            sequencesNotInTable.remove(sequenceName);
                            break;
                        }
                    }
                }
            }
        }
        pStmtSequences.close();
        rsCursorSequences.close();
    }

    private void fetchTriggers(List<MPDRTable> dbTables) throws SQLException {
        Map<String, String> triggers = new HashMap<>();
        String requeteSQL = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_TRIGGERS;
        PreparedStatement pStmtTriggers = connection.prepareStatement(requeteSQL);
        ResultSet rsCursorTriggers = pStmtTriggers.executeQuery();
        //Ajout du +i car si plusieurs triggers ont le même body, ils seront quand même ajoutés à la hashMap
        int i = 0;
        while (rsCursorTriggers.next()) {
            triggers.put(rsCursorTriggers.getString(Preferences.FETCHER_ORACLE_TRIGGER_NAME), rsCursorTriggers.getString(Preferences.FETCHER_ORACLE_TABLE_NAME));
            triggersNotInTable.add(rsCursorTriggers.getString(Preferences.FETCHER_ORACLE_TRIGGER_NAME));
            //On met le body en majuscule car lors de recherche(contains), c'est case sensitive
            triggersBody.put(rsCursorTriggers.getString(Preferences.FETCHER_ORACLE_TRIGGER_BODY).toUpperCase() + "//" + i, rsCursorTriggers.getString(Preferences.FETCHER_ORACLE_TABLE_NAME));
            i++;
        }
        for (MPDRTable dbTable : dbTables) {
            if (triggers.containsValue(dbTable.getName())) {
                MPDROracleTrigger dbTrigger = MVCCDElementFactory.instance().createMPDROracleTrigger(dbTable.getMPDRContTAPIs().getMPDRBoxTriggers(), null);
                String triggerName = triggers.get(dbTable.getName());
                dbTrigger.setName(triggerName);
                triggersNotInTable.remove(triggerName);
            }
        }
        pStmtTriggers.close();
        rsCursorTriggers.close();
    }

    private void fetchPackages(List<MPDRTable> dbTables) throws SQLException {
        List<String> packages = new ArrayList<>();
        StringBuilder requeteSQL = new StringBuilder();
        requeteSQL.append(Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_PROCEDURES);
        PreparedStatement pStmtPackages = connection.prepareStatement(requeteSQL.toString());
        pStmtPackages.setString(1, Preferences.FETCHER_ORACLE_PACKAGE);
        ResultSet rsCursorPackages = pStmtPackages.executeQuery();
        while (rsCursorPackages.next()) {
            packages.add(rsCursorPackages.getString(Preferences.FETCHER_ORACLE_OBJECT_NAME));
            packagesNotInTable.add(rsCursorPackages.getString(Preferences.FETCHER_ORACLE_OBJECT_NAME));
        }
        pStmtPackages.close();
        rsCursorPackages.close();
        String requetePackageGetDDL;
        //Afin d'avoir une liste pour la méthode findTableAcceuillePackage
        for (String aPackage : packages) {
            requetePackageGetDDL = "select dbms_metadata.get_ddl(\'PACKAGE\',\'" + aPackage + "\') from dual";
            PreparedStatement pStmtGetDdl = connection.prepareStatement(requetePackageGetDDL);
            ResultSet rsPackages = pStmtGetDdl.executeQuery();
            while (rsPackages.next()) {
                packagesSpec.put(rsPackages.getString(1).toUpperCase(), aPackage);
            }
            pStmtGetDdl.close();
            rsPackages.close();
        }

        for (MPDRTable dbTable : dbTables) {
            //package est un mot réservé donc utilisation du nom de variable "paquet"
            for (String paquet : packages) {
                if (findTableAccueillePackage(dbTable, paquet) != null) {
                    //if (findTableAccueillePackage(dbTable, paquet) != null) {
                    MPDROraclePackage mpdrOraclePackage = MVCCDElementFactory.instance().createMPDROraclePackage(dbTable.getMPDRContTAPIs().getMPDRBoxPackages(), null);
                    mpdrOraclePackage.setName(paquet);
                    packagesNotInTable.remove(paquet);
                }
            }
        }
    }

    //Permet d'aller récupérer le nom de la table en fonction du trigger dans laquelle elle est appelée
    private MPDRTable findTableAccueilleSequence(MPDRTable dbTable, String sequenceName) {
        if (triggersBody.containsValue(dbTable.getName())) {
            for (String body : triggersBody.keySet()) {
                String regex = "." + sequenceName + ".";
                if (Pattern.compile(regex).matcher(body).find()) {
                    triggersBody.get(dbTable);
                    return dbTable;
                }
            }
        }
        return null;
    }

    private MPDRTable findTableAccueilleSequence2(MPDRTable dbTable, String sequenceName) {
        String regex = "." + sequenceName + ".";
        Iterator<String> itr = triggersBody.keySet().iterator();
        while (itr.hasNext()){
            String body = itr.next();
            String tableName = triggersBody.get(body);
            if (Pattern.compile(regex).matcher(body).find()) {
                if (tableName.equals(dbTable.getName())) {
                    return dbTable;
                }
            }
        }
        return null;
    }

    //on se base sur la ligne "type TYPE_COLLABORATEURS  is table of COLLABORATEURS%rowtype index by pls_integer" dans le corps du package pour
    //trouver le nom de la table auquel il est lié
    private MPDRTable findTableAccueillePackage(MPDRTable dbTable, String packageName) {
        for (Map.Entry map : packagesSpec.entrySet()) {
            if (map.getValue().equals(packageName)) {
                String regex = ".IS TABLE OF " + dbTable.getName() + "%ROWTYPE";
                if (Pattern.compile(regex).matcher(map.getKey().toString()).find()) {
                    packagesSpec.remove(map.getKey());
                    return dbTable;
                }
            }
        }
        return null;
    }

    public MPDROracleModel getDbModel() {
        return dbModel;
    }

    //Attention, car il peut être normale que des séquences ne soient pas dans une table
    public List<String> getDbSequencesNotInTable() {
        return sequencesNotInTable;
    }

    //Attention, car il peut être normale que des packages ne soient pas dans une table
    public List<String> getDbPackagesNotInTable() {
        return packagesNotInTable;
    }

    //Attention, car il peut être normale que des triggers ne soient pas dans une table
    public List<String> getDbTriggersNotInTable() {
        return triggersNotInTable;
    }

    private void fetchIndex() {
        // A développer ultérieurement
    }
}