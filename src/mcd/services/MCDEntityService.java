package mcd.services;

import m.MRelEndMultiPart;
import m.MRelationDegree;
import main.MVCCDElement;
import mcd.*;
import org.apache.commons.lang.StringUtils;
import preferences.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MCDEntityService {

    public static void sortNameAsc(ArrayList<MCDEntity> entities){

        Collections.sort(entities, NAME_ASC);
    }

    static final Comparator<MCDEntity> NAME_ASC =
            new Comparator<MCDEntity>() {
                public int compare(MCDEntity e1, MCDEntity e2) {
                    return e1.getName().compareTo(e2.getName());
                }
    };



    public static ArrayList<MCDRelEnd> getMCDRelEnds(MCDEntity mcdEntity) {
        MCDContRelEnds mcdContRelEnds = mcdEntity.getMCDContRelEnds();
        return mcdContRelEnds.getMCDRelEnds();
    }

    public static ArrayList<MCDRelation> getMCDRelations(MCDEntity mcdEntity) {
        ArrayList<MCDRelation> resultat = new ArrayList<MCDRelation>();
        for (MCDRelEnd mcdRelEnd : getMCDRelEnds(mcdEntity)){
            if (! resultat.contains(mcdRelEnd.getImRelation())) {
                resultat.add((MCDRelation) mcdRelEnd.getImRelation());
            }
        }
        return resultat;
    }

    public static ArrayList<MCDGSEnd> getMCDGSEnds(MCDEntity mcdEntity) {
        ArrayList<MCDGSEnd> resultat = new ArrayList<MCDGSEnd>();
        for (MCDRelEnd mcdRelEnd : getMCDRelEnds(mcdEntity)){
            if (mcdRelEnd instanceof MCDGSEnd){
                resultat.add((MCDGSEnd) mcdRelEnd);
            }
        }
        return resultat;
    }

    public static ArrayList<MCDGSEnd> getGSEndsGeneralize(MCDEntity mcdEntity) {
        ArrayList<MCDGSEnd> resultat = new ArrayList<MCDGSEnd>();
        for (MCDGSEnd mcdGSEnd : getMCDGSEnds(mcdEntity)){
            if (mcdGSEnd.getDrawingDirection() == MCDRelEnd.GEN){
                resultat.add((MCDGSEnd) mcdGSEnd);
            }
        }
        return resultat;
    }


    public static ArrayList<MCDGSEnd> getGSEndsSpecialize(MCDEntity mcdEntity) {
        ArrayList<MCDGSEnd> resultat = new ArrayList<MCDGSEnd>();
        for (MCDGSEnd mcdGSEnd : getMCDGSEnds(mcdEntity)){
            if (mcdGSEnd.getDrawingDirection() == MCDRelEnd.SPEC){
                resultat.add((MCDGSEnd) mcdGSEnd);
            }
        }
        return resultat;
    }

    public static ArrayList<MCDLinkEnd> getMCDLinkEnds(MCDEntity mcdEntity) {
        ArrayList<MCDLinkEnd> resultat = new ArrayList<MCDLinkEnd>();
        for (MCDRelEnd mcdRelEnd : getMCDRelEnds(mcdEntity)){
            if (mcdRelEnd instanceof MCDLinkEnd){
                resultat.add((MCDLinkEnd) mcdRelEnd);
            }
        }
        return resultat;
    }

    public static MCDEntityNature getNature(MCDEntity mcdEntity) {
        if (mcdEntity.isInd()){
            return MCDEntityNature.IND;
        }
        if (mcdEntity.isDep()){
            return MCDEntityNature.DEP;
        }
        if (mcdEntity.isEntAss()){
            return MCDEntityNature.ENTASS;
        }
        if (mcdEntity.isEntAssDep()){
            return MCDEntityNature.ENTASSDEP;
        }
        if (mcdEntity.isNAire()){
            return MCDEntityNature.NAIRE;
        }
        if (mcdEntity.isNAireDep()){
            return MCDEntityNature.NAIREDEP;
        }
        if (mcdEntity.isSpecialized()){
            return MCDEntityNature.SPEC;
        }
        if (mcdEntity.isPseudoEntAss()){
            return MCDEntityNature.PSEUDOENTASS;
        }

        return null;
    }


    public static ArrayList<String> checkTableName(MCDEntity mcdEntity,
                                                    String tableName    ) {

        ArrayList<MCDEntity> sisters = mcdEntity.getSisters();

        ArrayList<String> messages = MCDUtilService.checkString(
                tableName,
                true,
                Preferences.ENTITY_TABLE_NAME_LENGTH,
                Preferences.NAME_REGEXPR,
                "naming.of.table.name",
                "of.entity");

        if (messages.size() == 0) {
            messages.addAll(checkExistNameInSisters(sisters, tableName,  true));
        }
        return messages;
    }



    public static ArrayList<String> checkExistNameInSisters(ArrayList<MCDEntity> sisters,
                                                            String name,
                                                            boolean uppercase) {

        MCDEntity elementConflict = nameExistInSisters(sisters, name, uppercase);
        if (elementConflict != null) {
            return MCDUtilService.messagesExistNaming(name,
                    uppercase,
                    "naming.exist.table.name.element",
                    "naming.sister.entity",
                    "naming.name.table",
                    elementConflict.getName());

        }

        return new ArrayList<String>();
    }

    public static MCDEntity nameExistInSisters(ArrayList<MCDEntity> sisters,
                                               String name,
                                               boolean uppercase) {
        for (MCDEntity sister : sisters) {
            String namingToCheck = name;
            String childNaming = sister.getMldrTableName();
            if (uppercase) {
                namingToCheck = namingToCheck.toUpperCase();
                // Pour la transition vers l'obligation de valeur de TableName
                if (childNaming == null) {
                    ;
                    childNaming = "";
                } else {
                    childNaming = childNaming.toUpperCase();
                }
            }

            if (StringUtils.isNotEmpty(childNaming) && StringUtils.isNotEmpty(namingToCheck)) {
                if (childNaming.equals(namingToCheck)) {
                    return sister;
                }
            }

        }
        return null;
    }


    // Nouveaux getMCDAssEnd...

    public static ArrayList<MCDAssEnd> getMCDAssEndsOpposites(ArrayList<MCDAssEnd> mcdAssEnds){
        ArrayList<MCDAssEnd> resultat = new ArrayList<MCDAssEnd>();
        for (MCDAssEnd mcdAssEnd : mcdAssEnds) {
            resultat.add(mcdAssEnd.getMCDAssEndOpposite());
        }
        return resultat;

    }

    public static ArrayList<MCDAssEnd> getMCDAssEnds(MCDEntity mcdEntity) {
        ArrayList<MCDAssEnd> resultat = new ArrayList<MCDAssEnd>();
        for (MVCCDElement mvccdElement : mcdEntity.getMCDContRelEnds().getChilds()){
            if (mvccdElement instanceof MCDAssEnd){
                resultat.add((MCDAssEnd) mvccdElement);
            }
        }
        return resultat;
    }


    public static ArrayList<MCDAssEnd> getMCDAssEndsNN(MCDEntity mcdEntity) {
        ArrayList<MCDAssEnd> resultat = new ArrayList<MCDAssEnd>();
        for (MCDAssEnd mcdAssEnd : getMCDAssEnds(mcdEntity)) {
            if (mcdAssEnd.getMcdAssociation().getDegree() == MRelationDegree.DEGREE_MANY_MANY) {
                resultat.add(mcdAssEnd);
            }
        }
        return resultat;
    }

    public static ArrayList<MCDAssEnd> getMCDAssEnds1N(MCDEntity mcdEntity, boolean parent) {
        ArrayList<MCDAssEnd> resultat = new ArrayList<MCDAssEnd>();
        for (MCDAssEnd mcdAssEnd : getMCDAssEnds(mcdEntity)) {
            if (mcdAssEnd.getMcdAssociation().getDegree() == MRelationDegree.DEGREE_ONE_MANY) {
                boolean c1 = mcdAssEnd.getMultiMaxStd() == MRelEndMultiPart.MULTI_MANY;
                if ( c1 && !parent ) {
                    resultat.add(mcdAssEnd);
                }
                if ( (!c1) && (parent) ){
                    resultat.add(mcdAssEnd);
                }
            }
        }
        return resultat;
    }

    public static ArrayList<MCDAssEnd> getMCDAssEnds11(MCDEntity mcdEntity, boolean parent) {
        ArrayList<MCDAssEnd> resultat = new ArrayList<MCDAssEnd>();
        for (MCDAssEnd mcdAssEnd : getMCDAssEnds(mcdEntity)) {
            if (mcdAssEnd.getMcdAssociation().getDegree() == MRelationDegree.DEGREE_ONE_ONE) {
                boolean c1 = mcdAssEnd.getMultiMinStd() == MRelEndMultiPart.MULTI_ONE;
                boolean c2 = mcdAssEnd.getMCDAssEndOpposite().getMultiMinStd() == MRelEndMultiPart.MULTI_ONE;
                MCDAssEnd mcdAssEndParent ;
                MCDAssEnd mcdAssEndChild ;
                if ( c1 != c2 ) {
                    if (c1) {
                        mcdAssEndParent = mcdAssEnd;
                        mcdAssEndChild = mcdAssEnd.getMCDAssEndOpposite();
                    } else{
                        mcdAssEndParent = mcdAssEnd.getMCDAssEndOpposite();
                        mcdAssEndChild = mcdAssEnd;
                    }
                } else{
                    boolean c3 = mcdAssEnd == mcdAssEnd.getMcdAssociation().getFrom();
                    if (c3){
                        mcdAssEndParent = mcdAssEnd;
                        mcdAssEndChild = mcdAssEnd.getMCDAssEndOpposite();
                    } else {
                        mcdAssEndParent = mcdAssEnd.getMCDAssEndOpposite();
                        mcdAssEndChild = mcdAssEnd;
                    }
                }
                if (parent){
                    resultat.add(mcdAssEndParent);
                } else {
                    resultat.add(mcdAssEndChild);
                }
            }
        }
        return resultat;
    }

    public static ArrayList<MCDAssEnd> getMCDAssEndsNotNN(MCDEntity mcdEntity, boolean parent) {
        ArrayList<MCDAssEnd> resultat = getMCDAssEnds1N(mcdEntity, parent);
        resultat.addAll(getMCDAssEnds11(mcdEntity, parent));
        return resultat;
    }

    public static ArrayList<MCDAssEnd> getMCDAssEndsAssNature(MCDEntity mcdEntity,
                                                           boolean parent,
                                                           MCDAssociationNature assNature) {
        ArrayList<MCDAssEnd> resultat = new ArrayList<MCDAssEnd>();
        for (MCDAssEnd mcdAssEnd : getMCDAssEndsNotNN(mcdEntity, parent)) {
            if (mcdAssEnd.getMcdAssociation().getNature() == assNature) {
                resultat.add(mcdAssEnd);
            }
        }
        return resultat;
    }

    public static ArrayList<MCDAssEnd> getMCDAssEndsId(MCDEntity mcdEntity,
                                                              boolean parent){
        ArrayList<MCDAssEnd> resultat = getMCDAssEndsAssNature(mcdEntity, parent, MCDAssociationNature.IDCOMP);
        resultat.addAll(getMCDAssEndsAssNature(mcdEntity, parent, MCDAssociationNature.IDNATURAL));
        resultat.addAll(getMCDAssEndsAssNature(mcdEntity, parent, MCDAssociationNature.CP));
        return resultat ;
    }

    public static ArrayList<MCDAssEnd> getMCDAssEndsLinkNN(MCDEntity mcdEntity) {
        ArrayList<MCDAssEnd> resultat = new ArrayList<MCDAssEnd>();
        for (MCDLinkEnd mcdLinkEnd: getMCDLinkEnds(mcdEntity)){
            MCDAssociation mcdAssociation = (MCDAssociation) mcdLinkEnd.getMcdLink().getEndAssociation().getmElement();
            if (mcdAssociation.getDegree() == MRelationDegree.DEGREE_MANY_MANY){
                resultat.add((MCDAssEnd) mcdAssociation.getFrom());
                resultat.add((MCDAssEnd) mcdAssociation.getTo());
            }
        }
        return resultat;
    }


    public static ArrayList<MCDAssEnd> getMCDAssEndsNoIdOptionnal(MCDEntity mcdEntity,
                                                                  boolean parent) {
        ArrayList<MCDAssEnd> resultat = new ArrayList<MCDAssEnd>();
        ArrayList<MCDAssEnd>  mcdAssEndsNoId = MCDEntityService.getMCDAssEndsAssNature(mcdEntity, parent, MCDAssociationNature.NOID);
        for (MCDAssEnd mcdAssEndNoId : mcdAssEndsNoId) {
            MCDAssEnd mcdAssEndNoIdParent ;
            if (parent){
                mcdAssEndNoIdParent =  mcdAssEndNoId;
            } else {
                mcdAssEndNoIdParent =  mcdAssEndNoId.getMCDAssEndOpposite();
            }
            if (mcdAssEndNoIdParent.getMultiMinStd() == MRelEndMultiPart.MULTI_ZERO) {
                resultat.add(mcdAssEndNoId);
            }
        }
        return resultat;
    }

    public static ArrayList<MCDAssEnd> getMCDAssEndsStructureIdForParameters(MCDEntity mcdEntity) {
        ArrayList<MCDAssEnd> resultat = new ArrayList<MCDAssEnd>();
        // Extrémités opposées aux extrémités identifiantes avec rôle child côté entité
        resultat =  MCDAssEndService.getMCDAssEndsOpposites(mcdEntity.getMCDAssEndsIdChild());
         // Extrémités d'association LinkNN
        resultat.addAll(mcdEntity.getMCDAssEndsLinkNN());
        return resultat;
    }
}
