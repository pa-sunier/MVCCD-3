package mpdr.tapis.postgresql;

import mcd.MCDAttribute;
import md.MDElement;
import mpdr.MPDRDB;
import mpdr.interfaces.IMPDRElement;
import mpdr.tapis.MPDRColumnJnalDatas;
import project.ProjectElement;

public class MPDRPostgreSQLColumnJnalDatas extends MPDRColumnJnalDatas {

    protected MPDRDB mpdrDb ;

    public MPDRPostgreSQLColumnJnalDatas(ProjectElement parent,
                                         IMPDRElement mpdrElementSource) {
        super(parent, mpdrElementSource);
    }


    @Override
    public MDElement getMdElementSource() {
        return null;
    }

    @Override
    public MCDAttribute getMcdAttributeSource() {
        return null;
    }

    @Override
    public boolean isPKForEntityIndependant() {
        return false;
    }
}
