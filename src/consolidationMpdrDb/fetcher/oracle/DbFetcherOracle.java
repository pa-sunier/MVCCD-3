package consolidationMpdrDb.fetcher.oracle;

import connections.ConConnection;
import consolidationMpdrDb.fetcher.DbFetcher;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import main.MVCCDElementFactory;
import mldr.MLDRParameter;
import mldr.MLDRTable;
import mpdr.MPDRCheck;
import mpdr.MPDRColumn;
import mpdr.MPDRIndex;
import mpdr.MPDRParameter;
import mpdr.MPDRTable;
import mpdr.MPDRUnique;
import mpdr.oracle.MPDROracleCheckSpecific;
import mpdr.oracle.MPDROracleColumn;
import mpdr.oracle.MPDROracleFK;
import mpdr.oracle.MPDROracleModel;
import mpdr.oracle.MPDROraclePK;
import mpdr.oracle.MPDROracleSequence;
import mpdr.oracle.MPDROracleTable;
import mpdr.oracle.MPDROracleUnique;
import mpdr.oracle.interfaces.IMPDROracleElement;
import mpdr.tapis.oracle.MPDROraclePackage;
import mpdr.tapis.oracle.MPDROracleTrigger;
import preferences.Preferences;
import preferences.PreferencesManager;

public class DbFetcherOracle extends DbFetcher {

  private final Connection connection;
  private final ConConnection conConnection;
  private Map<String, String> triggersSequencesMap = new HashMap<>(); //K=sequenceName, V=triggerName
  private Map<String, String> packagesMap = new HashMap<>(); //K=packageName, V= tableName
  private Map<String, String> triggersMap = new HashMap<>(); //K=triggerName, V=tableName
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
    this.dbModel = MVCCDElementFactory.instance().createMPDROracleModel(null);
    this.dbModel.setName(Preferences.FETCHER_ORACLE_MPDR_NAME);
  }

  public void fetch() throws SQLException {

      this.fetchTables();
      this.fetchTriggers(
        this.dbModel.getMPDRTables()); //Trigger avant séquence car la séquence à besoin des trigger pour etre attribuée à une table
      this.fetchDependencies();
      this.fetchSequences(this.dbModel.getMPDRTables());
      this.fetchPackages(this.dbModel.getMPDRTables());
    //fetchIndex(); //Non implémenté dans ce projet - Vincent
  }


  private void fetchTables() throws SQLException {
    String[] types = {Preferences.FETCHER_ORACLE_TABLE};
    try (ResultSet rsTables = this.databaseMetaData.getTables(this.databaseName, this.schemaDB, "%", types)) {
      while (rsTables.next()) {
        MPDROracleTable dbTable = MVCCDElementFactory.instance()
            .createMPDROracleTable(this.dbModel.getMPDRContTables(), null);
        dbTable.setName(rsTables.getString(Preferences.FETCHER_ORACLE_TABLE_NAME));
        //On crée un box pour les packages et triggers pour éviter d'avoir un nullpointeur lors des comparaisons quand on boucle sur les box
        MVCCDElementFactory.instance()
            .createMPDROracleBoxPackages(dbTable.getMPDRContTAPIs(), null);
        MVCCDElementFactory.instance()
            .createMPDROracleBoxTriggers(dbTable.getMPDRContTAPIs(), null);
          this.fetchColumns(dbTable);
          this.fetchPk(dbTable);
          this.fetchUnique(dbTable);
          this.fetchCheck(dbTable);
          this.fetchFk(dbTable);
          this.fetchIndex(dbTable);
      }
    }
  }

  private void fetchColumns(MPDROracleTable dbTable) throws SQLException {
    try (ResultSet rsColumns = this.databaseMetaData.getColumns(this.databaseName, this.schemaDB,
        dbTable.getName(), null)) {
      while (rsColumns.next()) {
        MPDROracleColumn dbColumn = MVCCDElementFactory.instance()
            .createMPDROracleColumn(dbTable.getMDRContColumns(), null);
        dbColumn.setName(rsColumns.getString(Preferences.FETCHER_ORACLE_COLUMN_NAME));
        dbColumn.setDatatypeLienProg(rsColumns.getString(Preferences.FETCHER_ORACLE_TYPE_NAME));
        dbColumn.setMandatory(!rsColumns.getBoolean(
            Preferences.FETCHER_ORACLE_NULLABLE)); //Si nullable -> non obligatoire
        dbColumn.setSize(rsColumns.getInt(Preferences.FETCHER_ORACLE_COLUMN_SIZE));
        dbColumn.setScale(rsColumns.getInt(Preferences.FETCHER_ORACLE_DECIMAL_DIGITS));
        dbColumn.setInitValue(rsColumns.getString(Preferences.FETCHER_ORACLE_COLUMN_DEF));
        dbColumn.setOrder(rsColumns.getInt(Preferences.FETCHER_ORACLE_ORDINAL_POSITION));
      }
    }
  }


  private void fetchPk(MPDROracleTable dbTable) throws SQLException {
    MPDROraclePK dbPK = null;
    //On ne récupère que les contraintes PK
    String requeteSQL = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONSTRAINTS;
    PreparedStatement pStmtUnique = this.connection.prepareStatement(requeteSQL);
    pStmtUnique.setString(1, "P");
    pStmtUnique.setString(2, dbTable.getName());
    ResultSet rsCursorPk = pStmtUnique.executeQuery();
    while (rsCursorPk.next()) {
      //Si le nom de la contrainte n'existe pas, on en crée une
      if (dbTable.getMPDRPK() == null) {
        dbPK = MVCCDElementFactory.instance()
            .createMPDROraclePK(dbTable.getMDRContConstraints(), null);
        dbPK.setName(rsCursorPk.getString(Preferences.FETCHER_ORACLE_CONSTRAINT_NAME));
      }
    }
    pStmtUnique.close();
    rsCursorPk.close();
    //On récupère toutes les contraintes et les colonnes auquelles elles sont rattachées
    String requeteSQLUserConsColumns = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONS_COLUMNS;
    PreparedStatement pStmtUserConsColumns = this.connection.prepareStatement(requeteSQLUserConsColumns);
    pStmtUserConsColumns.setString(1, dbTable.getName());
    ResultSet rsCurseurUserConsColumns = pStmtUserConsColumns.executeQuery();
    while (rsCurseurUserConsColumns.next()) {
      String constraintName = rsCurseurUserConsColumns.getString(
          Preferences.FETCHER_ORACLE_CONSTRAINT_NAME);
      String columnName = rsCurseurUserConsColumns.getString(
          Preferences.FETCHER_ORACLE_COLUMN_NAME);
      //si la position est null, il s'agit d'une contrainte qui doit être mise que sur une seule colonne (par exemple NOT NULL)
      if (constraintName.equals(dbPK.getName())) {
        MPDRParameter dbParameter = MVCCDElementFactory.instance()
            .createMPDROracleParameter(dbPK, (MLDRParameter) null);
        for (MPDRColumn dbColumn : dbTable.getMPDRColumns()) {
          if (dbColumn.getName().equals(columnName)) {
            dbParameter.setTargetId(dbColumn.getId());
            dbParameter.setName(columnName);
            dbColumn.setPk(true);
          }
        }
      }
    }
    pStmtUserConsColumns.close();
    rsCurseurUserConsColumns.close();

  }

  private void fetchUnique(MPDROracleTable dbTable) throws SQLException {
    MPDROracleUnique dbUnique = null;
    //On ne récupère que les contraintes Unique
    String requeteSQL = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONSTRAINTS;
    PreparedStatement pStmtUnique = this.connection.prepareStatement(requeteSQL);
    pStmtUnique.setString(1, Preferences.FETCHER_ORACLE_CONSTRAINT_TYPE_UNIQUE);
    pStmtUnique.setString(2, dbTable.getName());
    ResultSet rsCurseurUnique = pStmtUnique.executeQuery();
    while (rsCurseurUnique.next()) {
      //Si le nom de la contrainte n'existe pas, on en crée une
      if (!dbTable.getMPDRUniques()
          .contains(rsCurseurUnique.getString((Preferences.FETCHER_ORACLE_CONSTRAINT_NAME)))) {
        dbUnique = MVCCDElementFactory.instance()
            .createMPDROracleUnique(dbTable.getMDRContConstraints(), null);
        dbUnique.setName(rsCurseurUnique.getString(Preferences.FETCHER_ORACLE_CONSTRAINT_NAME));
      }
    }
    pStmtUnique.close();
    rsCurseurUnique.close();
    //On récupère toutes les contraintes et les colonnes auquelles elles sont rattachées
    String requeteSQLUserConsColumns = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONS_COLUMNS;
    PreparedStatement pStmtUserConsColumns = this.connection.prepareStatement(requeteSQLUserConsColumns);
    pStmtUserConsColumns.setString(1, dbTable.getName());
    ResultSet rsCurseurUserConsColumns = pStmtUserConsColumns.executeQuery();
    while (rsCurseurUserConsColumns.next()) {
      String constraintName = rsCurseurUserConsColumns.getString(
          Preferences.FETCHER_ORACLE_CONSTRAINT_NAME);
      String columnName = rsCurseurUserConsColumns.getString(
          Preferences.FETCHER_ORACLE_COLUMN_NAME);
      for (MPDRUnique dbUniqueConst : dbTable.getMPDRUniques()) {
        if (dbUniqueConst instanceof MPDROracleUnique) {
          //si la position est null, il s'agit d'une contrainte qui doit être mise que sur une seule colonne (par exemple NOT NULL)
          if (constraintName.equals(dbUniqueConst.getName())) {
            MPDRParameter dbParameter = MVCCDElementFactory.instance()
                .createMPDROracleParameter((IMPDROracleElement) dbUniqueConst,
                    (MLDRParameter) null);
            for (MPDRColumn dbColumn : dbTable.getMPDRColumns()) {
              if (dbColumn.getName().equals(columnName)) {
                dbParameter.setTargetId(dbColumn.getId());
                dbParameter.setName(columnName);
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
    String condition = null;
    StringBuilder requeteSQL = new StringBuilder();
    //Utilisation de la colonne search_condition_vc (introduit à la version 12c) qui est de type varchar car search_condition est de type long
    //Lors de la création d'une contrainte sans nom, la DB en crée une avec le nom qui commence par SYS_
    requeteSQL.append(Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONSTRAINTS);
    requeteSQL.append(Preferences.FETCHER_ORACLE_AND);
    requeteSQL.append(Preferences.FETCHER_ORACLE_REQUETE_SEARCH_CONDITION_VC);
    PreparedStatement pStmtChecks = this.connection.prepareStatement(requeteSQL.toString());
    pStmtChecks.setString(1, Preferences.FETCHER_ORACLE_CONSTRAINT_TYPE_CHECK);
    pStmtChecks.setString(2, dbTable.getName());
    ResultSet rsCursorChecks = pStmtChecks.executeQuery();
    while (rsCursorChecks.next()) {
      //Si le nom de la contrainte n'existe pas, on en crée une
      if (!dbTable.getMPDRChecks()
          .contains(rsCursorChecks.getString((Preferences.FETCHER_ORACLE_CONSTRAINT_NAME)))) {
        dbCheckSpecific = MVCCDElementFactory.instance()
            .createMPDROracleCheckSpecific(dbTable.getMDRContConstraints(), null);
        dbCheckSpecific.setName(
            rsCursorChecks.getString(Preferences.FETCHER_ORACLE_CONSTRAINT_NAME));
        condition = rsCursorChecks.getString(Preferences.FETCHER_ORACLE_SEARCH_CONDITION_VC);
      }
    }
    pStmtChecks.close();
    rsCursorChecks.close();
    //On récupère toutes les contraintes et les colonnes auquelles elles sont rattachées
    String requeteSQLUserConsColumns = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONS_COLUMNS;
    PreparedStatement pStmtUserConsColumns = this.connection.prepareStatement(requeteSQLUserConsColumns);
    pStmtUserConsColumns.setString(1, dbTable.getName());
    ResultSet rsCurseurUserConsColumns = pStmtUserConsColumns.executeQuery();
    while (rsCurseurUserConsColumns.next()) {
      String constraintName = rsCurseurUserConsColumns.getString(
          Preferences.FETCHER_ORACLE_CONSTRAINT_NAME);
      String columnName = rsCurseurUserConsColumns.getString(
          Preferences.FETCHER_ORACLE_COLUMN_NAME);
      for (MPDRCheck dbCheckConst : dbTable.getMPDRChecks()) {
        if (dbCheckConst instanceof MPDROracleCheckSpecific) {
          //si la position est null, il s'agit d'une contrainte qui doit être mise que sur une seule colonne (par exemple NOT NULL)
          if (constraintName.equals(dbCheckConst.getName())) {
            MPDRParameter dbParameter = MVCCDElementFactory.instance()
                .createMPDROracleParameter((IMPDROracleElement) dbCheckConst, (MLDRParameter) null);
            for (MPDRColumn dbColumn : dbTable.getMPDRColumns()) {
              if (dbColumn.getName().equals(columnName)) {
                dbParameter.setTargetId(dbColumn.getId());
                dbParameter.setName(columnName);
                dbParameter.setValue(condition);
              }
            }
          }
        }
      }
    }
    pStmtUserConsColumns.close();
    rsCurseurUserConsColumns.close();
  }

  private void fetchFk(MPDRTable dbTable) throws SQLException {
    MPDROracleFK dbFK = null;
    String requeteSQL = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONSTRAINTS;
    PreparedStatement pStmtFks = this.connection.prepareStatement(requeteSQL);
    pStmtFks.setString(1, Preferences.FETCHER_ORACLE_CONSTRAINT_TYPE_FK);
    pStmtFks.setString(2, dbTable.getName());
    ResultSet rsCursorFks = pStmtFks.executeQuery();
    while (rsCursorFks.next()) {
      if (!dbTable.getMPDRFKs()
          .contains(rsCursorFks.getString((Preferences.FETCHER_ORACLE_CONSTRAINT_NAME)))) {
        dbFK = MVCCDElementFactory.instance()
            .createMPDROracleFK(dbTable.getMDRContConstraints(), null);
        dbFK.setName(rsCursorFks.getString(Preferences.FETCHER_ORACLE_CONSTRAINT_NAME));
        if (rsCursorFks.getString(Preferences.FETCHER_ORACLE_DELETE_RULE)
            .equals(Preferences.FETCHER_ORACLE_CASCADE)) {
          dbFK.setDeleteCascade(true);
        }
      }

      String requeteSQLUserConsColumns = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_CONS_COLUMNS;
      PreparedStatement pStmtUserConsColumns = this.connection.prepareStatement(
          requeteSQLUserConsColumns);
      pStmtUserConsColumns.setString(1, dbTable.getName());
      ResultSet rsCurseurUserConsColumns = pStmtUserConsColumns.executeQuery();
      while (rsCurseurUserConsColumns.next()) {
        String constraintName = rsCurseurUserConsColumns.getString(
            Preferences.FETCHER_ORACLE_CONSTRAINT_NAME);
        String columnName = rsCurseurUserConsColumns.getString(
            Preferences.FETCHER_ORACLE_COLUMN_NAME);
        for (MPDRColumn dbColumn : dbTable.getMPDRColumns()) {
          if (constraintName.equals(dbFK.getName())) {
            if (dbColumn.getName().equals(
                rsCurseurUserConsColumns.getString(Preferences.FETCHER_ORACLE_COLUMN_NAME))) {
              MPDRParameter dbParameter = MVCCDElementFactory.instance()
                  .createMPDROracleParameter(dbFK, (MLDRParameter) null);
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

  private void fetchIndex(MPDROracleTable dbTable) throws SQLException {
    MPDRIndex dbIndex = null;
    String requeteSQL = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_IND_COLUMNS;
    PreparedStatement pStmtIndex = this.connection.prepareStatement(requeteSQL);
    ResultSet rsCursorIndex = pStmtIndex.executeQuery();
    while (rsCursorIndex.next()) {
      String indexName = rsCursorIndex.getString(Preferences.FETCHER_ORACLE_INDEX_NAME);
      String tableName = rsCursorIndex.getString(Preferences.FETCHER_ORACLE_TABLE_NAME);
      String columnName = rsCursorIndex.getString(Preferences.FETCHER_ORACLE_COLUMN_NAME);
      if (dbTable.getName().equals(tableName)) {
        //Si le nom de l'index n'existe pas, on en crée un
        if (!dbTable.getMPDRIndexes().contains(indexName)) {
          dbIndex = MVCCDElementFactory.instance()
              .createMPDROracleIndex(dbTable.getMDRContConstraints(), null);
          dbIndex.setName(indexName);
        }
        for (MPDRColumn dbColumn : dbTable.getMPDRColumns()) {
          if (dbColumn.getName().equals(columnName)) {
            MPDRParameter mpdrParameter = MVCCDElementFactory.instance()
                .createMPDROracleParameter((IMPDROracleElement) dbIndex, (MLDRParameter) null);
            mpdrParameter.setTargetId(dbColumn.getId());
            mpdrParameter.setName(dbColumn.getName());
          }
        }
      }
    }
    pStmtIndex.close();
    rsCursorIndex.close();
  }

  private void fetchSequences(List<MPDRTable> dbTables) throws SQLException {
    String requeteSQL = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_SEQUENCES;
    PreparedStatement pStmtSequences = this.connection.prepareStatement(requeteSQL);
    ResultSet rsCursorSequences = pStmtSequences.executeQuery();
    while (rsCursorSequences.next()) {
      String sequenceName = rsCursorSequences.getString(Preferences.FETCHER_ORACLE_SEQUENCE_NAME);
        this.sequencesNotInTable.add(sequenceName);
      for (MPDRTable dbTable : dbTables) {
        if (this.findTableAccueilleSequence(dbTable, sequenceName) != null) {
          MPDROracleSequence mpdrOracleSequence;
          for (MPDRColumn mpdrColumn : dbTable.getMPDRColumns()) {
            if (mpdrColumn.getIsPk()) {
              mpdrOracleSequence = MVCCDElementFactory.instance()
                  .createMPDROracleSequence(mpdrColumn, null);
              mpdrOracleSequence.setName(sequenceName);
              mpdrOracleSequence.setMinValue(
                  rsCursorSequences.getInt(Preferences.FETCHER_ORACLE_MIN_VALUE));
              mpdrOracleSequence.setMinValue(
                  rsCursorSequences.getInt(Preferences.FETCHER_ORACLE_INCREMENT_BY));
                this.sequencesNotInTable.remove(sequenceName);
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
    String requeteSQL = Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_TRIGGERS;
    PreparedStatement pStmtTriggers = this.connection.prepareStatement(requeteSQL);
    ResultSet rsCursorTriggers = pStmtTriggers.executeQuery();
    while (rsCursorTriggers.next()) {
        this.triggersMap.put(rsCursorTriggers.getString(Preferences.FETCHER_ORACLE_TRIGGER_NAME),
          rsCursorTriggers.getString(Preferences.FETCHER_ORACLE_TABLE_NAME));
        this.triggersNotInTable.add(rsCursorTriggers.getString(Preferences.FETCHER_ORACLE_TRIGGER_NAME));

    }
    for (MPDRTable dbTable : dbTables) {
      if (this.triggersMap.containsValue(dbTable.getName())) {
        MPDROracleTrigger dbTrigger = MVCCDElementFactory.instance()
            .createMPDROracleTrigger(dbTable.getMPDRContTAPIs().getMPDRBoxTriggers(), null);
        String triggerName = null;
        for (Map.Entry<String, String> entry : this.triggersMap.entrySet()) {
          if (entry.getValue().equals(dbTable.getName())) {
            triggerName = entry.getKey();
          }
          dbTrigger.setName(triggerName);
            this.triggersNotInTable.remove(triggerName);
        }
      }
    }
    pStmtTriggers.close();
    rsCursorTriggers.close();
  }

  private void fetchPackages(List<MPDRTable> dbTables) throws SQLException {
    List<String> packages = new ArrayList<>();
    StringBuilder requeteSQL = new StringBuilder();
    requeteSQL.append(Preferences.FETCHER_ORACLE_REQUETE_SQL_USER_PROCEDURES);
    PreparedStatement pStmtPackages = this.connection.prepareStatement(requeteSQL.toString());
    pStmtPackages.setString(1, Preferences.FETCHER_ORACLE_PACKAGE);
    ResultSet rsCursorPackages = pStmtPackages.executeQuery();
    while (rsCursorPackages.next()) {
      packages.add(rsCursorPackages.getString(Preferences.FETCHER_ORACLE_OBJECT_NAME));
        this.packagesNotInTable.add(rsCursorPackages.getString(Preferences.FETCHER_ORACLE_OBJECT_NAME));
    }
    pStmtPackages.close();
    rsCursorPackages.close();

    for (MPDRTable dbTable : dbTables) {
      //package est un mot réservé donc utilisation du nom de variable "paquet"
      for (String paquet : packages) {
        if (this.findTableAccueillePackage(dbTable, paquet) != null) {
          //if (findTableAccueillePackage(dbTable, paquet) != null) {
          MPDROraclePackage mpdrOraclePackage = MVCCDElementFactory.instance()
              .createMPDROraclePackage(dbTable.getMPDRContTAPIs().getMPDRBoxPackages(),
                  (MLDRTable) null);
          mpdrOraclePackage.setName(paquet);
            this.packagesNotInTable.remove(paquet);
        }
      }
    }
  }

  private MPDRTable findTableAccueilleSequence(MPDRTable dbTable, String sequenceName) {
    Iterator<String> itr = this.triggersSequencesMap.keySet().iterator(); //K=sequenceName, V=triggerName
    while (itr.hasNext()) {
      String seqName = itr.next();
      String triggerName = this.triggersSequencesMap.get(seqName);
      if (seqName.equals(sequenceName)) {
        String tabName = this.triggersMap.get(triggerName); //K=triggerName, V=tableName
        if (dbTable.getName().equals(tabName)) {
          return dbTable;
        }
      }
    }
    return null;
  }


  private MPDRTable findTableAccueillePackage(MPDRTable dbTable, String packageName) {
    if (this.packagesMap.get(packageName) != null) {
      String tableName = this.packagesMap.get(packageName);
      if (tableName.equals(dbTable.getName())) {
        return dbTable;
      }
    }
    return null;
  }

  //Attention, ces méthode ne récupère que les séquences liées à un trigger et que les packages liés à une table
  private void fetchDependencies() throws SQLException {
      this.fetchDependenciesSequences();
      this.fetchDependenciesPackages();
  }

  private void fetchDependenciesSequences() throws SQLException {
    String requeteSQL = Preferences.FETCHER_ORACLE_USER_DEPENDENCIES;
    PreparedStatement pStmtSequences = this.connection.prepareStatement(requeteSQL);
    pStmtSequences.setString(1, Preferences.FETCHER_ORACLE_SEQUENCE);
    pStmtSequences.setString(2, Preferences.FETCHER_ORACLE_TRIGGER);
    ResultSet rsCursorSequences = pStmtSequences.executeQuery();
    while (rsCursorSequences.next()) {
      //referenced_type = nom de la séquence, NAME = nom du trigger
        this.triggersSequencesMap.put(
          rsCursorSequences.getString(Preferences.FETCHER_ORACLE_REFERENCED_NAME),
          rsCursorSequences.getString(Preferences.FETCHER_ORACLE_NAME));
    }
    pStmtSequences.close();
    rsCursorSequences.close();
  }

  private void fetchDependenciesPackages() throws SQLException {
    String requeteSQL = Preferences.FETCHER_ORACLE_USER_DEPENDENCIES;
    PreparedStatement pStmtPackages = this.connection.prepareStatement(requeteSQL);
    pStmtPackages.setString(1, Preferences.FETCHER_ORACLE_TABLE);
    pStmtPackages.setString(2, Preferences.FETCHER_ORACLE_PACKAGE);
    ResultSet rsCursorPackages = pStmtPackages.executeQuery();
    while (rsCursorPackages.next()) {
      //NAME = nom du package, REFERENCED_NAME = nom de la table
        this.packagesMap.put(rsCursorPackages.getString(Preferences.FETCHER_ORACLE_NAME),
          rsCursorPackages.getString(Preferences.FETCHER_ORACLE_REFERENCED_NAME));
    }
    pStmtPackages.close();
    rsCursorPackages.close();
  }

  public MPDROracleModel getDbModel() {
    return this.dbModel;
  }

  //Attention, car il peut être normale que des séquences ne soient pas dans une table
  public List<String> getDbSequencesNotInTable() {
    return this.sequencesNotInTable;
  }

  //Attention, car il peut être normale que des packages ne soient pas dans une table
  public List<String> getDbPackagesNotInTable() {
    return this.packagesNotInTable;
  }

  //Attention, car il peut être normale que des triggers ne soient pas dans une table
  public List<String> getDbTriggersNotInTable() {
    return this.triggersNotInTable;
  }

}