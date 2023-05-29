package mpdr.tapis;

import main.MVCCDElement;
import mdr.MDRColumn;
import mdr.MDRContColumns;
import project.ProjectElement;

import java.util.ArrayList;
import java.util.Collections;

public class MPDRContColumnsView extends MDRContColumns {

    private  static final long serialVersionUID = 1000;

    public MPDRContColumnsView(ProjectElement parent, String name) {
        super(parent, name);
    }


    public ArrayList<MPDRColumnView> getMPDRColumnsView(){
        ArrayList<MPDRColumnView> resultat = new ArrayList<MPDRColumnView>();
        for (MVCCDElement mvccdElement: getChilds()){
            resultat.add((MPDRColumnView) mvccdElement);
        }
        return resultat;
    }

    public ArrayList<MPDRColumnView> getMPDRColumnsViewSortDefault(){
        ArrayList<MPDRColumnView> mpdrColumnsViewSorted = new ArrayList<MPDRColumnView>();
        for (MPDRColumnView mpdrColumnView : getMPDRColumnsView()){
            mpdrColumnsViewSorted.add(mpdrColumnView);
        }
        Collections.sort(mpdrColumnsViewSorted, MPDRColumnView::compareToDefault);
        return mpdrColumnsViewSorted;
    }


    // Surcharge pour l'affichage des colonnes de vue... (recopie de Jnal)
    public ArrayList<? extends MVCCDElement> getChildsSortDefault() {
        ArrayList<? extends MPDRColumnView> mpdrColumnsView = getMPDRColumnsViewSortDefault();
        return mpdrColumnsView ;
    }


    // Surcharge pour le traitement des colonnes de vue... (recopie de Jnal)
    public ArrayList<? extends MDRColumn> getMDRColumnsSortDefault(){
        ArrayList< MPDRColumnView> mpdrColumnsView = getMPDRColumnsViewSortDefault();
        return mpdrColumnsView ;
    }



}
