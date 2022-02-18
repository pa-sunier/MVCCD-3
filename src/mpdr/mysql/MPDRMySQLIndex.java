package mpdr.mysql;

import main.MVCCDElementFactory;
import mldr.MLDRParameter;
import mldr.interfaces.IMLDRElement;
import mpdr.MPDRIndex;
import mpdr.MPDRParameter;
import mpdr.mysql.interfaces.IMPDRMySQLElement;
import project.ProjectElement;

public class MPDRMySQLIndex extends MPDRIndex implements IMPDRMySQLElement {

    private  static final long serialVersionUID = 1000;

    public MPDRMySQLIndex(ProjectElement parent, IMLDRElement mldrElementSource) {
        super(parent, mldrElementSource);
    }

    public MPDRMySQLIndex(ProjectElement parent, IMLDRElement mldrElementSource, int id) {
        super(parent, mldrElementSource, id);
    }

    public MPDRParameter createParameter(MLDRParameter  mldrParameter) {
        MPDRParameter mpdrParameter = MVCCDElementFactory.instance().createMPDRMySQLParameter(this, mldrParameter);
        return mpdrParameter;
    }

}