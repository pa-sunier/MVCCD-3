package mpdr.tapis.postgresql;

import mpdr.MPDRConstraintCustomJnal;
import mpdr.MPDRDB;
import mpdr.tapis.MPDRColumnJnalTech;
import project.ProjectElement;
import stereotypes.Stereotype;

public class MPDRPostgreSQLColumnJnalTech extends MPDRColumnJnalTech {

    protected MPDRDB mpdrDb ;

    public MPDRPostgreSQLColumnJnalTech(ProjectElement parent,
                                        MPDRConstraintCustomJnal mpdrConstraintCustomJnal,
                                        Stereotype stereotype) {
        super(parent, mpdrConstraintCustomJnal, stereotype);
    }
}
