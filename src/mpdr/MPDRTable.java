package mpdr;

import md.MDElement;
import mdr.MDRConstraint;
import mdr.MDRTable;
import mldr.MLDRColumn;
import mldr.MLDRFK;
import mldr.MLDRPK;
import mldr.MLDRUnique;
import mldr.interfaces.IMLDRElement;
import mldr.services.MLDRColumnService;
import mpdr.interfaces.IMPDRElement;
import mpdr.interfaces.IMPDRElementWithSource;
import mpdr.services.MPDRColumnService;
import mpdr.services.MPDRTableService;
import project.ProjectElement;

import java.util.ArrayList;

public abstract class MPDRTable extends MDRTable implements IMPDRElement, IMPDRElementWithSource {

    private  static final long serialVersionUID = 1000;
    private IMLDRElement mldrElementSource;

    public MPDRTable(ProjectElement parent, String name, IMLDRElement mldrElementSource) {
        super(parent);
        this.mldrElementSource = mldrElementSource;
    }

    public MPDRTable(ProjectElement parent, IMLDRElement mldrElementSource) {
        super(parent);
        this.mldrElementSource = mldrElementSource;
    }

    public MPDRTable(ProjectElement parent, IMLDRElement mldrElementSource, int id) {
        super(parent, id);
        this.mldrElementSource = mldrElementSource;
    }

    @Override
    public IMLDRElement getMldrElementSource() {
        return mldrElementSource;
    }

    @Override
    public void setMldrElementSource(IMLDRElement imldrElementSource) {
        this.mldrElementSource = mldrElementSource;
    }


    @Override
    public MDElement getMdElementSource() {
        return (MDElement) getMldrElementSource();
    }


    //TODO-0
    // Une association n:n sans entité associative doit avoir Name et shortName!

    /*
    @Override
    public String getShortName() {
        return ((IMLDRElementWithSource) getMldrElementSource()).getMcdElementSource().getShortName();
    }

     */


    public MPDRColumn getMPDRColumnByMLDRColumnSource(MLDRColumn mldrColumn){
        return MPDRTableService.getMPDRColumnByMLDRColumnSource(this, mldrColumn);
    }


    public MDRConstraint getMPDRConstraintByMLDRConstraintSource(MDRConstraint mldrConstraint){
        return MPDRTableService.getMPDRConstraintByMLDRConstraintSource(this, mldrConstraint);
    }

    public ArrayList<MPDRColumn> getMPDRColumns() {
        return MPDRColumnService.to(getMDRColumns());
    }

    public  abstract MPDRColumn createColumn(MLDRColumn mldrColumn);

    public  abstract MPDRPK createPK(MLDRPK mldrPK);


    public abstract MDRConstraint createFK(MLDRFK mldrFK);


    public abstract MDRConstraint createUnique(MLDRUnique mldrUnique);
}
