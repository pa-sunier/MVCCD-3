package mpdr;

import main.MVCCDElement;
import mdr.MDRColumn;
import mdr.MDRContColumns;
import project.ProjectElement;

import java.util.ArrayList;
import java.util.Collections;

public class MPDRContColumns extends MDRContColumns {

    private  static final long serialVersionUID = 1000;

    public MPDRContColumns(ProjectElement parent, String name) {
        super(parent, name);
    }


    public ArrayList<MPDRColumn> getMPDRColumns(){
        ArrayList<MPDRColumn> resultat = new ArrayList<MPDRColumn>();
        for (MVCCDElement mvccdElement: getChilds()) {
            resultat.add((MPDRColumn) mvccdElement);
        }
        return resultat;
    }

    public ArrayList<MPDRColumn> getMPDRColumnsSortDefault(){
        ArrayList<MPDRColumn> mpdrColumnsSorted = new ArrayList<MPDRColumn>();
        for (MPDRColumn mpdrColumn : getMPDRColumns()){
            mpdrColumnsSorted.add(mpdrColumn);
        }
        Collections.sort(mpdrColumnsSorted, MPDRColumn::compareToDefault);
        return mpdrColumnsSorted;
    }


    // Surcharge pour l'affichage des colonnes provenant des TAPIs...
    public ArrayList<? extends MVCCDElement> getChildsSortDefault() {
        ArrayList<? extends MPDRColumn> mpdrColumns = getMPDRColumnsSortDefault();
        return mpdrColumns ;
    }


    // Surcharge pour le traitement des colonnes provenant des TAPIs...
    public ArrayList<? extends MDRColumn> getMDRColumnsSortDefault(){
        ArrayList< MPDRColumn> mpdrColumnsJnal = getMPDRColumnsSortDefault();
        return mpdrColumnsJnal ;
    }

}
