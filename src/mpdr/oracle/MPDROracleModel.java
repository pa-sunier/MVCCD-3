package mpdr.oracle;

import main.MVCCDElementFactory;
import mdr.MDRElement;
import mldr.MLDRTable;
import mldr.interfaces.IMLDRElement;
import mpdr.MPDRModel;
import mpdr.mysql.MPDRMySQLTable;
import project.ProjectElement;

public class MPDROracleModel extends MPDRModel {

    private  static final long serialVersionUID = 1000;

    public MPDROracleModel(ProjectElement parent, String name) {
        super(parent, name);
    }


    @Override
    public MPDROracleTable createTable(MLDRTable mldrTable){
        MPDROracleTable newTable = MVCCDElementFactory.instance().createMPDROracleTable(
                getMDRContTables(), mldrTable);

        return newTable;
    }

}