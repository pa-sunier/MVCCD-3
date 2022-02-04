package mpdr;

import md.MDElement;
import mdr.MDRCheck;
import mldr.interfaces.IMLDRElement;
import mpdr.interfaces.IMPDRConstraint;
import mpdr.interfaces.IMPDRElement;
import mpdr.interfaces.IMPDRElementWithSource;
import project.ProjectElement;

public abstract class MPDRCheck extends MDRCheck implements IMPDRElement, IMPDRElementWithSource,
        IMPDRConstraint {

    private  static final long serialVersionUID = 1000;
    private IMLDRElement mldrElementSource;

    public MPDRCheck(ProjectElement parent, String name, IMLDRElement mldrElementSource) {
        super(parent);
        this.mldrElementSource = mldrElementSource;
    }

    public MPDRCheck(ProjectElement parent, IMLDRElement mldrElementSource) {
        super(parent);
        this.mldrElementSource = mldrElementSource;
    }

    public MPDRCheck(ProjectElement parent, IMLDRElement mldrElementSource, int id) {
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





}