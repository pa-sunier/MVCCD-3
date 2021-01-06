package mcd;

import exceptions.CodeApplException;
import main.MVCCDElement;
import mcd.interfaces.IMCDModel;
import mcd.services.MCDElementService;
import md.MDElement;
import org.apache.commons.lang.StringUtils;
import preferences.Preferences;
import project.ProjectElement;

import java.util.ArrayList;

public abstract class MCDElement extends MDElement {

    private static final long serialVersionUID = 1000;

    public MCDElement(ProjectElement parent) {
        super(parent);
    }

    public MCDElement(ProjectElement parent, String name) {
        super(parent, name);
    }

    public String getPath(int pathMode, String separator) {
        return MCDElementService.getPath(this, pathMode, separator);
    }

    public String getShortPath(String separator) {
        return MCDElementService.getPath(this, MCDElementService.PATHSHORTNAME, separator);
    }

    public String getNormalPath(String separator) {
        return MCDElementService.getPath(this, MCDElementService.PATHNAME, separator);
    }



    public String getNamePath(int pathMode) {
        String separator = Preferences.MODEL_NAME_PATH_SEPARATOR;
        String path = getPath( pathMode, separator);
        if (StringUtils.isNotEmpty(path)){
            return path + separator + getName();
        } else {
            return getName();
        }
    }

    public String getShortNameSmartPath() {
        String separator = Preferences.MODEL_NAME_PATH_SEPARATOR;
        String path = getPath( MCDElementService.PATHSHORTNAME, separator);
        if (path !=null){
            return path + separator + getShortNameSmart();
        } else {
            return getShortNameSmart();
        }
    }



    public String getNameSource(){
        if (getName() != null) {
            return getName();
        }
        else if (getShortName() != null) {
            return getShortName();
        } else {
            throw new CodeApplException("MCDElement.getMainSource - Impossible de créer un nom") ;
        }
    }

    public String getNamePathSource(int pathMode, String separator) {
        return MCDElementService.getNamePathSource(this, pathMode, separator);
    }

    public String getNamePathSourceDefault() {
        return MCDElementService.getNamePathSource(this, MCDElementService.PATHNAME,
                Preferences.MODEL_NAME_PATH_SEPARATOR);
    }

    public IMCDModel getIMCDModelAccueil(){
        return MCDElementService.getIMCDModelAccueil(this);
    }

    public IMCDModel getMCDModelAccueil(){
        return MCDElementService.getMCDModelAccueil(this);
    }

    // L'élément lui-même et tous ses enfants MCDElement
    public ArrayList<MCDElement> getMCDElements(){
        return MCDElementService.getMCDElements(this);
    }
}
