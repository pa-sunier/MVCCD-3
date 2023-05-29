package project;

import diagram.mcd.MCDDiagram;
import main.MVCCDElement;
import main.MVCCDManager;
import mcd.*;
import mdr.*;
import mldr.*;
import mpdr.*;
import mpdr.mysql.MPDRMySQLModel;
import mpdr.oracle.MPDROracleModel;
import mpdr.postgresql.MPDRPostgreSQLModel;
import org.w3c.dom.*;
import preferences.Preferences;
import preferences.PreferencesManager;
import utilities.files.TranformerForXml;
import window.editor.diagrammer.elements.shapes.classes.ClassShape;
import window.editor.diagrammer.elements.shapes.relations.RelationAnchorPointShape;
import window.editor.diagrammer.elements.shapes.relations.RelationShape;
import window.editor.diagrammer.elements.shapes.relations.labels.LabelShape;
import window.editor.diagrammer.elements.shapes.relations.labels.LabelType;
import window.editor.diagrammer.elements.shapes.relations.mcd.MCDAssociationShape;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * @author Giorgio Roncallo, adapté et complété par Steve Berberat
 */
public class ProjectSaverXml {

  private Project project = MVCCDManager.instance().getProject();
  private Preferences projectPreferences = PreferencesManager.instance().getProjectPref(); //Il est mieux de récupérer les préférences par ce biais plutôt que project.getPreferences(): cela permet de s'assurer de récupérer des préférences si celles du projet n'existent pas (à priori, à confirmer avec PAS).
  private Boolean packagesAuthorized = PreferencesManager.instance().preferences().getREPOSITORY_MCD_PACKAGES_AUTHORIZEDS();
  private Boolean manyModelsAuthorized = PreferencesManager.instance().preferences().getREPOSITORY_MCD_MODELS_MANY();

  /**
   * Méthode principale qui se charge de créer un fichier XML contenant la sauvegarde du projet utilisateur.
   * @param file Chemin d'accès au fichier (y compris le nom du fichier) qui sera créé ou qui sera modifié.
   */
  public void createProjectFile(File file) throws FileNotFoundException, TransformerException, ParserConfigurationException {
    try {
      //Creation du document XML en mémoire;
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.newDocument();

      //Création de la balise racine <project>
      Element projectTag = document.createElement("project");
      document.appendChild(projectTag);
      Attr idAttrOfProjectTag = document.createAttribute("id");
      idAttrOfProjectTag.setValue(this.project.getIdProjectElementAsString());
      projectTag.setAttributeNode(idAttrOfProjectTag);

      //Propriété du projet
      this.addProperties(document, projectTag);

      //Préférences du projet
      this.addProjectPreferences(document, projectTag);

      //Création de la balise <MCD>
      MCDContModels mcdContModels = this.project.getMCDContModels();
      Element mcdTag = document.createElement(mcdContModels.getName());
      projectTag.appendChild(mcdTag);
      Attr idAttrOfMcdTag = document.createAttribute("id");
      idAttrOfMcdTag.setValue(mcdContModels.getIdProjectElementAsString());
      mcdTag.setAttributeNode(idAttrOfMcdTag);

      ArrayList<MVCCDElement> mcdModels = mcdContModels.getChilds();

      //Modèle
      if (this.manyModelsAuthorized) {
        this.addModelAndChilds(document, mcdTag, mcdModels, projectTag);

        //Package
      } else if (this.packagesAuthorized) {

        this.addDiagrams(document, mcdModels, projectTag);
        this.addEntities(document, mcdModels, mcdTag);
        this.addMCDRelations(document, mcdModels, mcdTag);
        this.addPackages(document, mcdContModels, mcdTag, projectTag);
        this.addMLD(document, mcdModels, mcdTag);

        //projet simple
      } else {

        this.addDiagrams(document, mcdModels, projectTag);
        this.addEntities(document, mcdModels, mcdTag);
        this.addMCDRelations(document, mcdModels, mcdTag);
        this.addMLD(document, mcdModels, mcdTag);
      }

      //Formatage du fichier
      Transformer transformer = new TranformerForXml().createTransformer();

      //Création du fichier
      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)); //Génère un FileNotFoundException si le fichier ne peut pas être créé, s'il existe mais ne peut pas être modifié ou si un répertoire du même nom existe.
      transformer.transform(source, result);

    } catch (ParserConfigurationException | TransformerException | FileNotFoundException pce) {
      //TODO-PAS STB faire un throw(e) - Intégration dans la transaction
      throw (pce);
      //pce.printStackTrace();
    }
  }

  /**
   * Ajoute les préférences de projet au document XML
   * @param document Document XML auquel ajouter les préférences
   * @param racine Noeud racine du document XML. Les préférences seront ajoutées en tant que fils de ce noeud.
   */
  public void addProjectPreferences(Document document, Element racine) {

    Element preferences = document.createElement("preferences");
    racine.appendChild(preferences);

    //Préférences Général
    this.addNewChildTag(document, preferences, "generalRelationNotation", this.projectPreferences.getGENERAL_RELATION_NOTATION());

    //Préférences MCD
    this.addNewChildTag(document, preferences, "mcdJournalization", this.projectPreferences.getMCD_JOURNALIZATION().toString());
    this.addNewChildTag(document, preferences, "mcdJournalizationException", this.projectPreferences.getMCD_JOURNALIZATION_EXCEPTION().toString());
    this.addNewChildTag(document, preferences, "mcdAudit", this.projectPreferences.getMCD_AUDIT().toString());
    this.addNewChildTag(document, preferences, "mcdAuditException", this.projectPreferences.getMCD_AUDIT_EXCEPTION().toString());
    this.addNewChildTag(document, preferences, "mcdAidDataTypeLienProg", this.projectPreferences.getMCD_AID_DATATYPE_LIENPROG());
    this.addNewChildTag(document, preferences, "mcdDataTypeNumberSizeMode", this.projectPreferences.getMCDDATATYPE_NUMBER_SIZE_MODE());
    this.addNewChildTag(document, preferences, "mcdAidIndColumnName", this.projectPreferences.getMCD_AID_IND_COLUMN_NAME());
    this.addNewChildTag(document, preferences, "mcdAidDepColumnName", this.projectPreferences.getMCD_AID_DEP_COLUMN_NAME());
    this.addNewChildTag(document, preferences, "mcdAidWithDep", this.projectPreferences.isMCD_AID_WITH_DEP().toString());
    this.addNewChildTag(document, preferences, "mcdTreeNamingAssociation", this.projectPreferences.getMCD_TREE_NAMING_ASSOCIATION());
    this.addNewChildTag(document, preferences, "mcdModeNamingLongName", this.projectPreferences.getMCD_MODE_NAMING_LONG_NAME());
    this.addNewChildTag(document, preferences, "mcdModeNamingAttributeShortName", this.projectPreferences.getMCD_MODE_NAMING_ATTRIBUTE_SHORT_NAME());

    // Préférences MCDToMLDR
    this.addNewChildTag(document, preferences, "mcdToMldrMode", this.projectPreferences.getMCDTOMLDR_MODE());

    //Préférences MLDRToMPDR
    this.addNewChildTag(document, preferences, "mldrToMpdrDb", this.projectPreferences.getMLDRTOMPDR_DB());

    //Préférences MDR Format
    this.addNewChildTag(document, preferences, "mdrTableNameFormat", this.projectPreferences.getMDR_TABLE_NAME_FORMAT());
    this.addNewChildTag(document, preferences, "mdrTableNNNameFormat", this.projectPreferences.getMDR_TABLE_NN_NAME_FORMAT());
    this.addNewChildTag(document, preferences, "mdrTableNNNameIndiceFormat", this.projectPreferences.getMDR_TABLE_NN_NAME_INDICE_FORMAT());
    this.addNewChildTag(document, preferences, "mdrColumnAttrNameFormat", this.projectPreferences.getMDR_COLUMN_ATTR_NAME_FORMAT());
    this.addNewChildTag(document, preferences, "mdrColumnAttrShortNameFormat", this.projectPreferences.getMDR_COLUMN_ATTR_SHORT_NAME_FORMAT());
    this.addNewChildTag(document, preferences, "mdrColumnDerivedMarker", this.projectPreferences.getMDR_COLUMN_DERIVED_MARKER());
    this.addNewChildTag(document, preferences, "mdrPkNameFormat", this.projectPreferences.getMDR_PK_NAME_FORMAT());
    this.addNewChildTag(document, preferences, "mdrColumnPkNameFormat", this.projectPreferences.getMDR_COLUMN_PK_NAME_FORMAT());
    this.addNewChildTag(document, preferences, "mdrColumnfkNameFormat", this.projectPreferences.getMDR_COLUMN_FK_NAME_FORMAT());
    this.addNewChildTag(document, preferences, "mdrColumnFkNameOneAncestorFormat", this.projectPreferences.getMDR_COLUMN_FK_NAME_ONE_ANCESTOR_FORMAT());
    this.addNewChildTag(document, preferences, "mdrFkNameFormat", this.projectPreferences.getMDR_FK_NAME_FORMAT());
    this.addNewChildTag(document, preferences, "mdrFkNameWithoutRoleFormat", this.projectPreferences.getMDR_FK_NAME_WITHOUT_ROLE_FORMAT());
    this.addNewChildTag(document, preferences, "mdrRoleGeneralizeMarker", this.projectPreferences.getMDR_ROLE_GENERALIZE_MARKER());
    this.addNewChildTag(document, preferences, "mdrPathSepFormat", this.projectPreferences.getMDR_PATH_SEP_FORMAT());
    this.addNewChildTag(document, preferences, "mdrPEASepFormat", this.projectPreferences.getMDR_PEA_SEP_FORMAT());
    this.addNewChildTag(document, preferences, "mdrTableSepFormat", this.projectPreferences.getMDR_TABLE_SEP_FORMAT());
    this.addNewChildTag(document, preferences, "mdrRoleSepFormat", this.projectPreferences.getMDR_ROLE_SEP_FORMAT());
    this.addNewChildTag(document, preferences, "mdrFkIndSepFormat", this.projectPreferences.getMDR_FKIND_SEP_FORMAT());

    //Préférences MDR
    this.addNewChildTag(document, preferences, "mdrPrefColumnFkOneAncestor", this.projectPreferences.getMDR_PREF_COLUMN_FK_ONE_ANCESTOR().toString());
    this.addNewChildTag(document, preferences, "mdrPrefColumnFkOneAncestorDiff", this.projectPreferences.getMDR_PREF_COLUMN_FK_ONE_ANCESTOR_DIFF());

    // Préférences MLDR (préférences de type enum)
    this.addNewChildTag(document, preferences, "mldrPrefNamingLengthName", this.projectPreferences.getMLDR_PREF_NAMING_LENGTH().getName());
    this.addNewChildTag(document, preferences, "mldrPrefNamingLengthLength", this.projectPreferences.getMLDR_PREF_NAMING_LENGTH().getLength().toString());
    this.addNewChildTag(document, preferences, "mldrPrefNamingLengthRequired", Boolean.toString(this.projectPreferences.getMLDR_PREF_NAMING_LENGTH().isRequired()));
    this.addNewChildTag(document, preferences, "mldrPrefNamingFormatName", this.projectPreferences.getMLDR_PREF_NAMING_FORMAT().getName());

    // Préférences MPDR Oracle (préférences de type enum)
    this.addNewChildTag(document, preferences, "mpdrOraclePrefNamingLengthName", this.projectPreferences.getMPDRORACLE_PREF_NAMING_LENGTH().getName());
    this.addNewChildTag(document, preferences, "mpdrOraclePrefNamingLengthLength", this.projectPreferences.getMPDRORACLE_PREF_NAMING_LENGTH().getLength().toString());
    this.addNewChildTag(document, preferences, "mpdrOraclePrefNamingLengthRequired", Boolean.toString(this.projectPreferences.getMPDRORACLE_PREF_NAMING_LENGTH().isRequired()));
    this.addNewChildTag(document, preferences, "mpdrOraclePrefNamingFormatName", this.projectPreferences.getMPDRORACLE_PREF_NAMING_FORMAT().getName());

    // Préférences MPDR MySQL (préférences de type enum)
    this.addNewChildTag(document, preferences, "mpdrMySQLPrefNamingLengthName", this.projectPreferences.getMPDRMYSQL_PREF_NAMING_LENGTH().getName());
    this.addNewChildTag(document, preferences, "mpdrMySQLPrefNamingLengthLength", this.projectPreferences.getMPDRMYSQL_PREF_NAMING_LENGTH().getLength().toString());
    this.addNewChildTag(document, preferences, "mpdrMySQLPrefNamingLengthRequired", Boolean.toString(this.projectPreferences.getMPDRMYSQL_PREF_NAMING_LENGTH().isRequired()));
    this.addNewChildTag(document, preferences, "mpdrMySQLPrefNamingFormatName", this.projectPreferences.getMPDRMYSQL_PREF_NAMING_FORMAT().getName());

    // Préférences MPDR PostgreSQL (préférences de type enum)
    this.addNewChildTag(document, preferences, "mpdrPostgreSQLPrefNamingLengthName", this.projectPreferences.getMPDRPOSTGRESQL_PREF_NAMING_LENGTH().getName());
    this.addNewChildTag(document, preferences, "mpdrPostgreSQLPrefNamingLengthLength", this.projectPreferences.getMPDRPOSTGRESQL_PREF_NAMING_LENGTH().getLength().toString());
    this.addNewChildTag(document, preferences, "mpdrPostgreSQLPrefNamingLengthRequired", Boolean.toString(this.projectPreferences.getMPDRPOSTGRESQL_PREF_NAMING_LENGTH().isRequired()));
    this.addNewChildTag(document, preferences, "mpdrPostgreSQLPrefNamingFormatName", this.projectPreferences.getMPDRPOSTGRESQL_PREF_NAMING_FORMAT().getName());
  }

  /**
   * Create a new tag and add it as a child of the given parent tag.
   * @param doc Document into with the tag will be created.
   * @param parentTag The parent tag that will have the child tag as new child.
   * @param childTagName The name of the new child tag that will be created.
   * @param childTagText The text content of the new child tag. If null, no text node will be created for the child tag.
   * @return The new child tag is returned.
   */
  private Element addNewChildTag(Document doc, Element parentTag, String childTagName, String childTagText) {
    Element childTag = doc.createElement(childTagName);
    if (childTagText != null) {
      childTag.appendChild(doc.createTextNode(childTagText));
    }
    parentTag.appendChild(childTag);
    return childTag;
  }

  private void addModelAndChilds(Document doc, Element mcd, ArrayList<MVCCDElement> mcdModels, Element projectTag) {
    // Parcours des enfants de l'élément mcd
    for (MVCCDElement mcdModel : mcdModels) {
      // Création du modèle dans le document
      Element modelTag = doc.createElement("model");
      if (mcdModel instanceof MCDModel) {
        mcd.appendChild(modelTag);
        Attr name = doc.createAttribute("name");
        name.setValue(mcdModel.getName());
        modelTag.setAttributeNode(name);
      }

      ArrayList<MVCCDElement> modelsChilds = mcdModel.getChilds();

      if (this.packagesAuthorized) {
        // Création des différents éléments du modèle avec packages
        this.addPropertiesModelsOrPackages(doc, modelTag, mcdModel);
        this.addDiagrams(doc, modelsChilds, projectTag);
        this.addEntities(doc, modelsChilds, modelTag);
        this.addMCDRelations(doc, modelsChilds, modelTag);
        this.addPackages(doc, mcdModel, modelTag, projectTag);
        this.addMLD(doc, modelsChilds, modelTag);
      } else {
        // Création des différents éléments du modèle sans packages
        this.addPropertiesModelsOrPackages(doc, modelTag, mcdModel);
        this.addDiagrams(doc, modelsChilds, projectTag);
        this.addEntities(doc, modelsChilds, modelTag);
        this.addMCDRelations(doc, modelsChilds, modelTag);
        this.addMLD(doc, modelsChilds, modelTag);
      }
    }

  }

  private void addPackages(Document doc, MVCCDElement modelChild, Element racine, Element projectTag) {

    // Récupération des packages
    ArrayList<MCDPackage> packagesChilds = this.getPackages(modelChild);

    // Parcours des paquetage
    for (int i = 0; i < packagesChilds.size(); i++) {
      MVCCDElement pack = packagesChilds.get(i);
      ArrayList<MVCCDElement> packageChilds = pack.getChilds();

      if (!pack.getName().isEmpty()) {

        // Ajout des paquetage dans le document
        Element packages = doc.createElement("package");
        Attr name = doc.createAttribute("name");
        name.setValue(pack.getName());
        packages.setAttributeNode(name);

        // Ajout des éléments qui composent un paquetage
        this.addPropertiesModelsOrPackages(doc, packages, pack);
        this.addDiagrams(doc, packageChilds, projectTag);
        this.addEntities(doc, packageChilds, packages);
        this.addMCDRelations(doc, packageChilds, packages);
        this.addPackages(doc, pack, packages, projectTag);
        racine.appendChild(packages);
      }
    }

  }

  /**
   * Méthode pour récupérer tous les paquetages d'un élément
   */
  private ArrayList<MCDPackage> getPackages(MVCCDElement element) {
    ArrayList<MCDPackage> packages = new ArrayList<>();
    for (MVCCDElement mvccdElement : element.getChilds()) {
      if (mvccdElement instanceof MCDPackage) {
        packages.add((MCDPackage) mvccdElement);
      }
    }
    return packages;
  }

  // *** Méthodes de sauvegarde du MCD ***

  private void addDiagrams(Document doc, ArrayList<MVCCDElement> listElements, Element racineTag) {
    // Ajout du package diagrammes dans le document
    for (int i = 0; i < listElements.size(); i++) {
      if (listElements.get(i) instanceof MCDContDiagrams) {
        MCDContDiagrams mcdContDiagrams = (MCDContDiagrams) listElements.get(i);
        Element diagramsTag = doc.createElement(Preferences.NODE_DIAGRAMS);
        Attr idAttrOfDiagramsTag = doc.createAttribute(Preferences.ATTRIBUTE_ID);
        idAttrOfDiagramsTag.setValue(mcdContDiagrams.getIdProjectElementAsString());
        diagramsTag.setAttributeNode(idAttrOfDiagramsTag);

        racineTag.appendChild(diagramsTag);

        ArrayList<MVCCDElement> diagrams = mcdContDiagrams.getChilds();

        // Ajout des diagrammes dans le document
        for (MVCCDElement diagram : diagrams) {
          if (diagram instanceof MCDDiagram) {
            // Création de la balise <diagramme>
            MCDDiagram mcdDiagram = (MCDDiagram) diagram;
            Element diagramTag = doc.createElement(Preferences.NODE_DIAGRAM);
            diagramsTag.appendChild(diagramTag);

            // Ajout de l'id à la balise <diagramme>
            Attr idAttrOfDiagramTag = doc.createAttribute(Preferences.ATTRIBUTE_ID);

            idAttrOfDiagramTag.setValue(mcdDiagram.getIdProjectElementAsString());
            diagramTag.setAttributeNode(idAttrOfDiagramTag);

            // Ajout de l'attribut isActive représentant le diagramme actif au moment de la fermeture
            if (diagram == MVCCDManager.instance().getCurrentDiagram()) {
              diagramTag.setAttribute(Preferences.ATTRIBUTE_IS_ACTIVE, "true");
            }

            // Ajout de l'ID du parent pour faire la référence
            ProjectElement parentModel = (ProjectElement) mcdDiagram.getParent().getParent();
            diagramTag.setAttribute(Preferences.ATTRIBUTE_PARENT_ID, parentModel.getIdProjectElementAsString());

            // Ajout de l'attribut "name" à la balise <diagramme>
            Attr nameAttrOfDiagramTag = doc.createAttribute(Preferences.ATTRIBUTE_NAME);
            nameAttrOfDiagramTag.setValue(mcdDiagram.getName());
            diagramTag.setAttributeNode(nameAttrOfDiagramTag);

            // Ajout des shapes
            if (!((MCDDiagram) diagram).getShapes().isEmpty()) {
              Element shapesTag = doc.createElement(Preferences.NODE_SHAPES);
              diagramTag.appendChild(shapesTag);

              // ClassShapes
              for (ClassShape classShape : mcdDiagram.getClassShapes()) {
                this.addClassShape(doc, classShape, shapesTag);
              }

              // RelationShapes
              for (RelationShape relation : mcdDiagram.getRelationShapes()) {
                this.addRelationShape(doc, relation, shapesTag);
              }
            }
          }
        }
      }
    }
  }

  private void addMCDAssociationNameAnchorPoint(Document doc, MCDAssociationShape shape) {
    if (shape.hasLabel(LabelType.ASSOCIATION_NAME)) {

      LabelShape label = shape.getLabelByType(LabelType.ASSOCIATION_NAME);

      Element anchorPointElement = doc.createElement(label.getPointAncrage().getXmlTagName());

      anchorPointElement.setAttribute(Preferences.ATTRIBUTE_ID, String.valueOf(label.getPointAncrage().getId()));
      anchorPointElement.setAttribute(Preferences.ATTRIBUTE_X, String.valueOf(label.getPointAncrage().x));
      anchorPointElement.setAttribute(Preferences.ATTRIBUTE_Y, String.valueOf(label.getPointAncrage().y));

      NodeList nodes = doc.getElementsByTagName(Preferences.DIAGRAMMER_MCD_ASSOCIATION_XML_TAG);

      for (int i = 0; i < nodes.getLength(); i++) {
        Node currentNode = nodes.item(i);
        NamedNodeMap attributes = currentNode.getAttributes();

        // Vérifie s'il s'agit de l'ID de l'association traitée
        if (Integer.parseInt(attributes.getNamedItem(Preferences.ATTRIBUTE_ID).getNodeValue()) == shape.getId()) {
          for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
            Node currentChild = currentNode.getChildNodes().item(j);
            // Ajoute le noeud anchorPoint au parent
            if (currentChild.getNodeName().equals(Preferences.DIAGRAMMER_ANCHOR_POINTS_XML_TAG_NAME)) {
              currentChild.appendChild(anchorPointElement);
            }
          }
        }
      }
    }
  }

  private void addEntities(Document doc, ArrayList<MVCCDElement> mcdContModelsChilds, Element racineTag) {
    // Parcours tous les conteneurs d'entités
    for (int i = 0; i < mcdContModelsChilds.size(); i++) {
      MVCCDElement mcdContModelsChild = mcdContModelsChilds.get(i);
      if (mcdContModelsChild instanceof MCDContEntities) {
        MCDContEntities mcdContEntities = (MCDContEntities) mcdContModelsChild;

        // Création de la balise <entities>
        Element entitiesTag = doc.createElement("entities");
        racineTag.appendChild(entitiesTag);

        // Ajout de l'id à la balise <entities>
        Attr idAttrOfEntitiesTag = doc.createAttribute("id");
        idAttrOfEntitiesTag.setValue(mcdContEntities.getIdProjectElementAsString());
        entitiesTag.setAttributeNode(idAttrOfEntitiesTag);

        // Parcours et ajout des entités
        ArrayList<MVCCDElement> entities = mcdContEntities.getChilds();
        for (int j = 0; j < entities.size(); j++) {
          MCDEntity entity = (MCDEntity) entities.get(j);

          // Création de la balise <entite>
          Element entityTag = doc.createElement("entite");
          entitiesTag.appendChild(entityTag);

          // Ajout de l'attribut "id" à <entite>
          Attr idAttrOfEntityTag = doc.createAttribute("id");
          idAttrOfEntityTag.setValue(entity.getIdProjectElementAsString());
          entityTag.setAttributeNode(idAttrOfEntityTag);

          // Ajout de l'attribut "name" à <entite>
          Attr nameAttrOfEntityTag = doc.createAttribute("name");
          nameAttrOfEntityTag.setValue(entity.getName());
          entityTag.setAttributeNode(nameAttrOfEntityTag);

          // Ajout des éléments qui composent une entité
          ArrayList<MVCCDElement> entityChilds = entity.getChilds();
          this.addPropertiesEntity(doc, entityTag, entity, racineTag);
          this.addAttributs(doc, entityTag, entityChilds);
          this.addContraints(doc, entityTag, entityChilds);
        }
      }
    }
  }

  private void addProperties(Document document, Element racine) {
    // Ajout des propriétés du projet
    Element properties = document.createElement("proprietes");
    racine.appendChild(properties);

    // Persistance du nom du projet
    Element name = document.createElement("nameProject");
    name.appendChild(document.createTextNode(this.project.getName()));
    properties.appendChild(name);

    // Persistance de la version de l'application utilisée par le projet
    Element version = document.createElement("version");
    version.appendChild(document.createTextNode(Preferences.APPLICATION_VERSION));
    properties.appendChild(version);

    Element profileFileName = document.createElement("profileFileName");
    properties.appendChild(profileFileName);
    // Si le nom du fichier de profil est null, cela renvoi une exception
    if (this.project.getProfileFileName() != null) {
      profileFileName.appendChild(document.createTextNode(this.project.getProfileFileName()));
    }

    Element modelsMany = document.createElement("modelsMany");
    modelsMany.appendChild(document.createTextNode(Boolean.toString(this.project.isModelsMany())));
    properties.appendChild(modelsMany);

    Element packagesAutorizeds = document.createElement("packagesAutorizeds");
    packagesAutorizeds.appendChild(document.createTextNode(Boolean.toString(this.project.isPackagesAutorizeds())));
    properties.appendChild(packagesAutorizeds);

    // Persistance de la séquence permettant d'incrémenter les ids de nouveaux éléments créés dans le projet
    Element idElementSequence = document.createElement("idElementSequence");
    idElementSequence.appendChild(document.createTextNode(String.valueOf(this.project.getIdElementSequence())));
    properties.appendChild(idElementSequence);
  }

  private void addPropertiesEntity(Document doc, Element entity, MCDEntity mcdEntity, Element racine) {
    // Ajout des propriétés des entités
    Element properties = doc.createElement("proprietes");
    entity.appendChild(properties);

    Element shortName = doc.createElement("shortName");
    shortName.appendChild(doc.createTextNode(mcdEntity.getShortName()));
    properties.appendChild(shortName);

    Element ordered = doc.createElement("ordered");
    ordered.appendChild(doc.createTextNode(String.valueOf(mcdEntity.isOrdered())));
    properties.appendChild(ordered);

    Element entAbstract = doc.createElement("entityAbstract");
    entAbstract.appendChild(doc.createTextNode(String.valueOf(mcdEntity.isEntAbstract())));
    properties.appendChild(entAbstract);

    Element journal = doc.createElement("journal");
    journal.appendChild(doc.createTextNode(String.valueOf(mcdEntity.isJournal())));
    properties.appendChild(journal);

    Element audit = doc.createElement("audit");
    audit.appendChild(doc.createTextNode(String.valueOf(mcdEntity.isAudit())));
    properties.appendChild(audit);

    Element mldrTableName = doc.createElement("mldrTableName");
    mldrTableName.appendChild(doc.createTextNode(String.valueOf(mcdEntity.getMldrTableName())));
    properties.appendChild(mldrTableName);
  }

  private void addPropertiesModelsOrPackages(Document doc, Element element, MVCCDElement child) {
    //Ajouts des proprités pour les modèles et pour les packages
    Element properties = doc.createElement("proprietes");
    element.appendChild(properties);

    Element shortName = doc.createElement("shortName");
    properties.appendChild(shortName);

    Element audit = doc.createElement("audit");
    properties.appendChild(audit);

    Element auditException = doc.createElement("auditException");
    properties.appendChild(auditException);

    Element journalization = doc.createElement("journalization");
    properties.appendChild(journalization);

    Element journalizationException = doc.createElement("journalizationException");
    properties.appendChild(journalizationException);

    if (child instanceof MCDModel) {
      MCDModel mcdModel = (MCDModel) child;

      Element packagesAutorized = doc.createElement("packagesAutorizeds");
      packagesAutorized.appendChild(doc.createTextNode(String.valueOf(mcdModel.isPackagesAutorizeds())));
      properties.appendChild(packagesAutorized);

      shortName.appendChild(doc.createTextNode(mcdModel.getShortName()));
      audit.appendChild(doc.createTextNode(String.valueOf(mcdModel.isMcdAudit())));
      auditException.appendChild(doc.createTextNode(String.valueOf(mcdModel.isMcdAuditException())));
      journalization.appendChild(doc.createTextNode(String.valueOf(mcdModel.isMcdJournalization())));
      journalizationException.appendChild(doc.createTextNode(String.valueOf(mcdModel.isMcdJournalizationException())));

    } else if (child instanceof MCDPackage) {
      MCDPackage mcdPackage = (MCDPackage) child;

      Element parent = doc.createElement("parent");
      parent.appendChild(doc.createTextNode(mcdPackage.getParent().getName()));
      properties.appendChild(parent);

      shortName.appendChild(doc.createTextNode(mcdPackage.getShortName()));
      audit.appendChild(doc.createTextNode(String.valueOf(mcdPackage.isMcdAudit())));
      auditException.appendChild(doc.createTextNode(String.valueOf(mcdPackage.isMcdAuditException())));
      journalization.appendChild(doc.createTextNode(String.valueOf(mcdPackage.isMcdJournalization())));
      journalizationException.appendChild(doc.createTextNode(String.valueOf(mcdPackage.isMcdJournalizationException())));
    }
  }

  private void addAttributs(Document doc, Element entity, ArrayList<MVCCDElement> listElement) {
    // Ajout du package Attributs au document
    for (int i = 0; i < listElement.size(); i++) {
      MVCCDElement entitychild = listElement.get(i);
      if (entitychild.getName().equals("Attributs")) {
        Element attributsTag = doc.createElement("attributs");
        entity.appendChild(attributsTag);

        ArrayList<MVCCDElement> attributsChilds = entitychild.getChilds();

        // Ajout des attributs
        this.addAttributsChilds(doc, attributsChilds, attributsTag);

      }
    }
  }

  private void addAttributsChilds(Document doc, ArrayList<MVCCDElement> attributsChilds, Element attributs) {
    for (int i = 0; i < attributsChilds.size(); i++) {
      MVCCDElement attributsChild = attributsChilds.get(i);
      MCDAttribute childAttribut = (MCDAttribute) attributsChild;

      // Création de la balise <attribut>
      Element attributTag = doc.createElement("attribut");
      attributs.appendChild(attributTag);

      // Ajout de l'attribut "id" à <attribut>
      Attr id = doc.createAttribute("id");
      id.setValue(childAttribut.getIdProjectElementAsString());
      attributTag.setAttributeNode(id);

      // Ajout de l'attribut "name" à <attribut>
      Attr name = doc.createAttribute("name");
      name.setValue(childAttribut.getName());
      attributTag.setAttributeNode(name);

      Element aid = doc.createElement("aid");
      aid.appendChild(doc.createTextNode(String.valueOf(childAttribut.isAid())));
      attributTag.appendChild(aid);

      Element aidDep = doc.createElement("aidDep");
      aidDep.appendChild(doc.createTextNode(String.valueOf(childAttribut.isAidDep())));
      attributTag.appendChild(aidDep);

      Element mandatory = doc.createElement("mandatory");
      mandatory.appendChild(doc.createTextNode(String.valueOf(childAttribut.isMandatory())));
      attributTag.appendChild(mandatory);

      Element list = doc.createElement("list");
      list.appendChild(doc.createTextNode(String.valueOf(childAttribut.isList())));
      attributTag.appendChild(list);

      Element frozen = doc.createElement("frozen");
      frozen.appendChild(doc.createTextNode(String.valueOf(childAttribut.isFrozen())));
      attributTag.appendChild(frozen);

      Element ordered = doc.createElement("ordered");
      ordered.appendChild(doc.createTextNode(String.valueOf(childAttribut.isOrdered())));
      attributTag.appendChild(ordered);

      Element upperCase = doc.createElement("upperCase");
      upperCase.appendChild(doc.createTextNode(String.valueOf(childAttribut.isUppercase())));
      attributTag.appendChild(upperCase);

      // Tout les éléments qui suive peuvent être vide. Dans le fichier ils ne sont pas stockés dans ce cas
      Element dataTypeLienProg = doc.createElement("dataTypeLienProg");
      if (childAttribut.getDatatypeLienProg() != null) {
        dataTypeLienProg.appendChild(doc.createTextNode(childAttribut.getDatatypeLienProg()));
      }
      attributTag.appendChild(dataTypeLienProg);

      Element scale = doc.createElement("scale");
      attributTag.appendChild(scale);
      Integer test = childAttribut.getScale();
      if (test != null) {
        scale.appendChild(doc.createTextNode(String.valueOf(childAttribut.getScale())));
      }

      Element size = doc.createElement("size");
      attributTag.appendChild(size);

      if (childAttribut.getSize() != null) {
        size.appendChild(doc.createTextNode(String.valueOf(childAttribut.getSize())));
      }

      Element initValue = doc.createElement("initValue");
      if (!childAttribut.getInitValue().equals("")) {
        initValue.appendChild(doc.createTextNode(childAttribut.getInitValue()));

      }
      attributTag.appendChild(initValue);

      Element derivedValue = doc.createElement("derivedValue");
      if (!childAttribut.getDerivedValue().equals("")) {
        derivedValue.appendChild(doc.createTextNode(childAttribut.getDerivedValue()));
      }
      attributTag.appendChild(derivedValue);

      Element domain = doc.createElement("domain");
      attributTag.appendChild(domain);
      if (childAttribut.getDomain() != null) {
        domain.appendChild(doc.createTextNode(childAttribut.getDomain()));
      }

    }
  }

  private void addContraints(Document doc, Element entity, ArrayList<MVCCDElement> listElement) {
    // Ajout du package Contraintes au document
    for (int i = 0; i < listElement.size(); i++) {
      MVCCDElement entitychild = listElement.get(i);
      if (entitychild.getName().equals("Contraintes")) {
        Element contraintes = doc.createElement("contraintes");
        entity.appendChild(contraintes);

        ArrayList<MVCCDElement> contraintsChilds = entitychild.getChilds();

        // Ajout des contraintes au document
        this.addContraintsChilds(doc, contraintsChilds, contraintes);
      }
    }
  }

  private void addContraintsChilds(Document doc, ArrayList<MVCCDElement> contraintsChilds, Element contraintes) {
    for (int i = 0; i < contraintsChilds.size(); i++) {
      MCDConstraint mcdConstraint = (MCDConstraint) contraintsChilds.get(i);

      // Création de la balise <constraint>
      Element constraintTag = doc.createElement("constraint");
      contraintes.appendChild(constraintTag);

      // Ajout de l'attribut "id" à la balise <constraint>
      Attr idAttrOfConstraintTag = doc.createAttribute("id");
      idAttrOfConstraintTag.setValue(mcdConstraint.getIdProjectElementAsString());
      constraintTag.setAttributeNode(idAttrOfConstraintTag);

      // Ajout de l'attribut "name" à la balise <constraint>
      Attr nameAttrOfConstraintTag = doc.createAttribute("name");
      nameAttrOfConstraintTag.setValue(mcdConstraint.getName());
      constraintTag.setAttributeNode(nameAttrOfConstraintTag);

      Element shortName = doc.createElement("shortName");
      shortName.appendChild(doc.createTextNode(mcdConstraint.getShortName()));
      constraintTag.appendChild(shortName);

      // Récupération du type de contrainte
      if (mcdConstraint instanceof MCDNID) {
        MCDNID nid = (MCDNID) mcdConstraint;
        Element lienProg = doc.createElement("lienProg");
        lienProg.appendChild(doc.createTextNode(String.valueOf(nid.isLienProg())));
        constraintTag.appendChild(lienProg);

        Element typeConstrainte = doc.createElement("type");
        typeConstrainte.appendChild(doc.createTextNode("NID"));
        constraintTag.appendChild(typeConstrainte);
        // Ajout des parameters ( encore pas implémenté dans l'application)
        this.addParameters(doc, nid, constraintTag);

      }
      if (mcdConstraint instanceof MCDUnique) {
        MCDUnique unique = (MCDUnique) mcdConstraint;
        Element absolute = doc.createElement("absolute");
        absolute.appendChild(doc.createTextNode(String.valueOf(unique.isAbsolute())));
        constraintTag.appendChild(absolute);

        Element typeConstrainte = doc.createElement("type");
        typeConstrainte.appendChild(doc.createTextNode("Unique"));
        constraintTag.appendChild(typeConstrainte);
        // Ajout des parameters
        this.addParameters(doc, unique, constraintTag);
      }
    }
  }

  /**
   * Persiste en XML les relations du MCD.
   * @param doc Document XML dans lequel seront persistées les relations.
   * @param mcdModels Liste des éléments qui se trouvent sous le modèle "MCD" dans le référentiel.
   * @param racineTag Balise parent sous laquelle les relations seront persistées.
   */
  private void addMCDRelations(Document doc, ArrayList<MVCCDElement> mcdModels, Element racineTag) {
    // Recherche du conteneur de relations sour le modèle MCD
    for (MVCCDElement mcdModel : mcdModels) {
      if (mcdModel instanceof MCDContRelations) {

        // Création de la balise <relations>
        Element relationsTag = doc.createElement("relations");
        racineTag.appendChild(relationsTag);

        // Ajout de l'id à la balise <relations>
        Attr idAttrOfRelationsTag = doc.createAttribute("id");
        idAttrOfRelationsTag.setValue(((MCDContRelations) mcdModel).getIdProjectElementAsString());
        relationsTag.setAttributeNode(idAttrOfRelationsTag);

        // Ajout des relations au document
        this.addRelationsChilds(doc, mcdModel.getChilds(), relationsTag);

      }
    }
  }

  private void addRelationsChilds(Document doc, ArrayList<MVCCDElement> relationsChilds, Element relations) {
    // Création de la balise <associations>
    Element associations = doc.createElement("associations");
    relations.appendChild(associations);

    // Création de la balise <generalisations>
    Element generalisations = doc.createElement("generalisations");
    relations.appendChild(generalisations);

    // Création de la balise <links>
    Element links = doc.createElement("links");
    relations.appendChild(links);

    // Parcours de l'ensemble des relations qui se trouvent sous "Relations" dans le référentiel
    for (MVCCDElement relationsChild : relationsChilds) {

      // Persistance d'une relation de type association
      if (relationsChild instanceof MCDAssociation) {
        this.addAssociation(doc, (MCDAssociation) relationsChild, associations);
      }

      // Persistance d'une relation de type généralisation
      else if (relationsChild instanceof MCDGeneralization) {
        this.addGeneralization(doc, (MCDGeneralization) relationsChild, generalisations);
      }

      // Persistance d'une relation qui est un lien d'entité associative
      else if (relationsChild instanceof MCDLink) {
        this.addlink(doc, (MCDLink) relationsChild, links);
      }
    }
  }

  private void addlink(Document doc, MCDLink mcdLink, Element links) {

    // Création de la balise <lienDEntiteAssociative>
    Element lienEATag = doc.createElement("lienDEntiteAssociative");
    links.appendChild(lienEATag);

    // Ajout de l'attribut "id" à la balise <lienDEntiteAssociative>
    Attr idAttrOfLienEATag = doc.createAttribute("id");
    idAttrOfLienEATag.setValue(mcdLink.getIdProjectElementAsString());
    lienEATag.setAttributeNode(idAttrOfLienEATag);

    // Création de la balise <extremiteAssociation>
    Element extremiteAssociationTag = doc.createElement("extremiteAssociation");
    lienEATag.appendChild(extremiteAssociationTag);

    // Ajout de l'attribut "id" à la balise <extremiteAssociation>
    Attr idAttrOfExtremiteAssTag = doc.createAttribute("id");
    idAttrOfExtremiteAssTag.setValue(mcdLink.getEndAssociation().getIdProjectElementAsString());
    extremiteAssociationTag.setAttributeNode(idAttrOfExtremiteAssTag);

    // Ajout de l'attribut "association_target_id" à la balise <extremiteAssociation>
    Attr assTargetIdOfExtremiteAssTag = doc.createAttribute("association_target_id");
    assTargetIdOfExtremiteAssTag.setValue(mcdLink.getEndAssociation().getAssociation().getIdProjectElementAsString());
    extremiteAssociationTag.setAttributeNode(assTargetIdOfExtremiteAssTag);

    // Création de la balise <extremiteEntite>
    Element extremiteEntiteTag = doc.createElement("extremiteEntite");
    lienEATag.appendChild(extremiteEntiteTag);

    // Ajout de l'attribut "id" à la balise <extremiteEntite>
    Attr idAttrOfExtremiteEntiteTag = doc.createAttribute("id");
    idAttrOfExtremiteEntiteTag.setValue(mcdLink.getEndEntity().getIdProjectElementAsString());
    extremiteEntiteTag.setAttributeNode(idAttrOfExtremiteEntiteTag);

    // Ajout de l'attribut "entite_target_id" à la balise <extremiteEntite>
    Attr entiteTargetIdOfExtremiteAssTag = doc.createAttribute("entite_target_id");
    entiteTargetIdOfExtremiteAssTag.setValue(mcdLink.getEndEntity().getEntity().getIdProjectElementAsString());
    extremiteEntiteTag.setAttributeNode(entiteTargetIdOfExtremiteAssTag);
  }

  private void addGeneralization(Document doc, MCDGeneralization mcdGeneralization, Element generalisations) {

    // Création de la balise <generalisation>
    Element generalisation = doc.createElement("generalisation");
    generalisations.appendChild(generalisation);

    // Création de la balise <genEntite>
    Element entityGen = doc.createElement("genEntite");
    //#MAJ 2021-06-06 Suppression du paramétrage MElement.getPath() ; automatiquement la préférence de l'application
    entityGen.appendChild(doc.createTextNode(mcdGeneralization.getGen().getMcdEntity().getNamePath()));
    generalisation.appendChild(entityGen);

    // Ajout de l'attribut target_entity_id sur la balise <genEntity>
    Attr targetIdAttrOfGenEntityTag = doc.createAttribute("target_entity_id");
    targetIdAttrOfGenEntityTag.setValue(mcdGeneralization.getGen().getMcdEntity().getIdProjectElementAsString());
    entityGen.setAttributeNode(targetIdAttrOfGenEntityTag);

    // Création de la balise <specEntite>
    Element entitySpec = doc.createElement("specEntite");
    //#MAJ 2021-06-06 Suppression du paramétrage MElement.getPath() ; automatiquement la préférence de l'application
    entitySpec.appendChild(doc.createTextNode(mcdGeneralization.getSpec().getMcdEntity().getNamePath()));
    generalisation.appendChild(entitySpec);

    // Ajout de l'attribut target_entity_id sur la balise <specEntity>
    Attr targetIdAttrOfSpecEntityTag = doc.createAttribute("target_entity_id");
    targetIdAttrOfSpecEntityTag.setValue(mcdGeneralization.getSpec().getMcdEntity().getIdProjectElementAsString());
    entitySpec.setAttributeNode(targetIdAttrOfSpecEntityTag);
  }

  private void addAssociation(Document doc, MCDAssociation mcdAssociation, Element associations) {
    // Récupération des extrémité d'association
    MCDAssEnd extremiteFrom = mcdAssociation.getFrom();
    MCDAssEnd extremiteTo = mcdAssociation.getTo();

    // Création de la balise <association>
    Element associationTag = doc.createElement("association");
    associations.appendChild(associationTag);

    // Ajout de l'id à la balise <association>
    Attr idAttrOfAssociationTag = doc.createAttribute("id");
    idAttrOfAssociationTag.setValue(mcdAssociation.getIdProjectElementAsString());
    associationTag.setAttributeNode(idAttrOfAssociationTag);

    // Association dans nom général (noms dans les rôles)
    if (mcdAssociation.getName().equals("")) {
      this.addPropertiesAssociation(doc, associationTag, mcdAssociation);
      this.addExtremite(doc, associationTag, extremiteFrom);
      this.addExtremite(doc, associationTag, extremiteTo);

    } else {
      // Association avec nom général
      Attr name = doc.createAttribute("name");
      name.setValue(mcdAssociation.getName());
      associationTag.setAttributeNode(name);

      Attr shortName = doc.createAttribute("shortName");
      shortName.setValue(mcdAssociation.getShortName());
      associationTag.setAttributeNode(shortName);

      this.addPropertiesAssociation(doc, associationTag, mcdAssociation);
      this.addExtremite(doc, associationTag, extremiteFrom);
      this.addExtremite(doc, associationTag, extremiteTo);
    }
  }

  private void addPropertiesAssociation(Document doc, Element association, MCDAssociation mcdAssociation) {

    // Ajout des propriétés d'association
    Element properties = doc.createElement("proprietes");
    association.appendChild(properties);

    Element nature = doc.createElement("nature");
    nature.appendChild(doc.createTextNode(mcdAssociation.getNature().getName()));
    properties.appendChild(nature);

    Element oriented = doc.createElement("oriented");
    if (mcdAssociation.getOriented() != null) {
      oriented.appendChild(doc.createTextNode(String.valueOf(mcdAssociation.getOriented())));

    }
    properties.appendChild(oriented);

    Element deleteCascade = doc.createElement("deleteCascade");
    deleteCascade.appendChild(doc.createTextNode(String.valueOf(mcdAssociation.isDeleteCascade())));
    properties.appendChild(deleteCascade);

    Element frozen = doc.createElement("frozen");
    frozen.appendChild(doc.createTextNode(String.valueOf(mcdAssociation.isFrozen())));
    properties.appendChild(frozen);

  }

  private void addExtremite(Document doc, Element associationTag, MCDAssEnd assEnd) {

    // Création de la balise <roleExtremiteFrom> ou <roleExtremiteTo>
    Element roleExtremiteTag = null;
    if (assEnd.getDrawingDirection() == 2) { // Si la direction du dessin a comme valeur 2 le rôle de l'extremité est "Tracée vers"
      roleExtremiteTag = doc.createElement("roleExtremiteTo");
    } else {
      roleExtremiteTag = doc.createElement("roleExtremiteFrom");
    }
    associationTag.appendChild(roleExtremiteTag);

    // Création de l'attribut id sur la balise <roleExtremiteFrom> ou <roleExtremiteTo>
    Attr idAttrOfRoleExtremiteTag = doc.createAttribute("id");
    idAttrOfRoleExtremiteTag.setValue(assEnd.getIdProjectElementAsString());
    roleExtremiteTag.setAttributeNode(idAttrOfRoleExtremiteTag);

    // Création de l'attribut target_entity_id sur la balise <roleExtremiteFrom> ou <roleExtremiteTo>
    Attr targetEntityIdAttrOfRoleExtremiteTag = doc.createAttribute("target_entity_id");
    targetEntityIdAttrOfRoleExtremiteTag.setValue(assEnd.getMcdEntity().getIdProjectElementAsString());
    roleExtremiteTag.setAttributeNode(targetEntityIdAttrOfRoleExtremiteTag);

    // Création de la balise <name> sous <roleExtremiteFrom> ou <roleExtremiteTo>
    Element nameRoleTag = doc.createElement("name");
    nameRoleTag.appendChild(doc.createTextNode(assEnd.getName()));
    roleExtremiteTag.appendChild(nameRoleTag);

    // Création de la balise <shortName> sous <roleExtremiteFrom> ou <roleExtremiteTo>
    Element shortNameRoleTag = doc.createElement("shortName");
    shortNameRoleTag.appendChild(doc.createTextNode(assEnd.getShortName()));
    roleExtremiteTag.appendChild(shortNameRoleTag);

    // Création de la balise <entiteNamePath> sous <roleExtremiteFrom> ou <roleExtremiteTo>
    Element entityTag = doc.createElement("entiteNamePath");
    //#MAJ 2021-06-06 Suppression du paramétrage MElement.getPath() ; automatiquement la préférence de l'application
    entityTag.appendChild(doc.createTextNode(assEnd.getMcdEntity().getNamePath()));
    roleExtremiteTag.appendChild(entityTag);

    // Création de la balise <multiplicity> sous <roleExtremiteFrom> ou <roleExtremiteTo>
    Element multiplicityTag = doc.createElement("multiplicity");
    multiplicityTag.appendChild(doc.createTextNode(assEnd.getMultiStr()));
    roleExtremiteTag.appendChild((multiplicityTag));

    // Création de la balise <ordered> sous <roleExtremiteFrom> ou <roleExtremiteTo>
    Element orderedRoleTag = doc.createElement("ordered");
    orderedRoleTag.appendChild(doc.createTextNode(String.valueOf(assEnd.isOrdered())));
    roleExtremiteTag.appendChild(orderedRoleTag);

  }

  /**
   * Méthode pour ajouter les paramètres (les attributs référencés) des contraintes
   */
  private void addParameters(Document doc, MCDConstraint mcdConstraint, Element constraint) {
    for (MCDParameter parameterOfConstraint : mcdConstraint.getMcdParameters()) {

      // Créer la balise <parameter> pour chaque paramètre (donc pour chaque attribut)
      Element parameterTag = doc.createElement("parameter");
      constraint.appendChild(parameterTag);

      // Ajout de l'attribut "name" à la balise <parameter>
      Attr nameAttributeOfParameterTag = doc.createAttribute("name");
      nameAttributeOfParameterTag.setValue(parameterOfConstraint.getName());
      parameterTag.setAttributeNode(nameAttributeOfParameterTag);

      // Ajout de l'attribut "target_id" à la balise <parameter>
      Attr targetIdAttributeOfParameterTag = doc.createAttribute("target_id");
      targetIdAttributeOfParameterTag.setValue(String.valueOf(parameterOfConstraint.getTarget().getIdProjectElement()));
      parameterTag.setAttributeNode(targetIdAttributeOfParameterTag);

      // Ajout de l'attribut "target_order" à la balise <parameter>
      Attr targetOrderAttributeOfParameterTag = doc.createAttribute("target_order");
      targetOrderAttributeOfParameterTag.setValue(String.valueOf(parameterOfConstraint.getTarget().getOrder()));
      parameterTag.setAttributeNode(targetOrderAttributeOfParameterTag);

      // Ajout de l'attribut "target_ClassShortNameUI"* à la balise <parameter>
      Attr targetClassShortNameUIAttributeOfParameterTag = doc.createAttribute("target_ClassShortNameUI");
      targetClassShortNameUIAttributeOfParameterTag.setValue(parameterOfConstraint.getTarget().getClassShortNameUI());
      parameterTag.setAttributeNode(targetClassShortNameUIAttributeOfParameterTag);
    }
  }

  // *** Méthodes de sauvegarde du MLDR ***
    /*
    Règles appliquées pour définir la structure du XML généré:
    - Si un élément X contient un élément Y qui est placé directement sous X de manière visible dans l'arborescence du
    référentiel, alors cet élément Y sera une nouvelle balise placé sous la balise XML correspondante à l'élément X.
    - Si un élément X contient un élément Y accessible dans les propriétés de X dans le référentiel, alors l'élément Y
    sera un attribut de la balise XML correspondant à l'élément X.
     */

  /**
   * Sauvegarde du ou des modèles MLDR_DT ou MLDR_TI qui se trouvent sous un modèle MCD.
   * @param doc Document DOM
   * @param models Liste des modèles à parcourir: tous ceux qui sont MLDR_DT ou MLDR_TI seront traités et persistés.
   * @param racineTag balise parent (par exemple, la balise <mcd> du fichier persisté).
   */
  private void addMLD(Document doc, ArrayList<MVCCDElement> models, Element racineTag) {

    //Pour chaque modèle MLDR (que ce soit MLDR_DT ou MLDR_TI)
    //#MAJ 2022-06-28 Parcours des Modèles logiques erroné
    //for (MVCCDElement model : models) {
    //if (model instanceof MLDRModel) {
    //MLDRModel mldrModel = (MLDRModel) model;

    for (int i = 0; i < models.size(); i++) {
      MVCCDElement mvccdElement = models.get(i);
      if (mvccdElement instanceof MLDRModel) {
        MLDRModel mldrModel = (MLDRModel) mvccdElement;

        //Création de la balise <MLDR_xx>
        Element mldrTag = null;
        if (mldrModel instanceof MLDRModelDT) {
          mldrTag = doc.createElement("MLDR_DT");
        } else if (mldrModel instanceof MLDRModelTI) {
          mldrTag = doc.createElement("MLDR_TI");
        }
        racineTag.appendChild(mldrTag);

        //Ajout de l'id à la balise <MLDR_xx>
        mldrTag.setAttribute("id", mldrModel.getIdProjectElementAsString());

        //Persistance des tables (y compris les FKs)
        this.addMDRTables(doc, mldrModel, mldrTag);

        //Persistance des relations (les relations sont les liens entre tables, conservant les cardinalités et faisant ainsi redondance avec les FK (volontairement)
        this.addMDRelations(doc, mldrModel, mldrTag);

        //Persistance des MPDRs
        this.addMPDRModel(doc, mldrModel, mldrTag);
      }
    }
  }

  // *** Méthodes de sauvegarde du MPDR ***
    /*
    Règles appliquées pour définir la structure du XML généré:
    => idem que pour la sauvegarde MLDR
     */

  /**
   * Sauvegarde les modèles MPDR qui se trouvent sous MLDR, dans une balise <MPDR_XX> (XX correspondant au constructeur de BD)
   * @param doc Document XML dans lequel la persistance se fait
   * @param mldrModel Modèle MLDR parcouru pour lequel tous les modèles physiques qu'il contient seront persistés.
   * @param mldrTag Balise racine <mldr> qui sera la balise parent des balises <MPDR_XX> enfant.
   */
  private void addMPDRModel(Document doc, MLDRModel mldrModel, Element mldrTag) {

    //Parcours de tous les enfants du modèle MLDR
    for (MVCCDElement mldrModelChild : mldrModel.getChilds()) {

      //Persistance de chaque élément de type MPDR
      if (mldrModelChild instanceof MPDRModel) {
        MPDRModel mpdrModel = (MPDRModel) mldrModelChild;

        //Création de la balise <MPDR_xx>
        Element mpdrTag = null;
        if (mpdrModel instanceof MPDROracleModel) {
          mpdrTag = doc.createElement("MPDR_Oracle");
        } else if (mpdrModel instanceof MPDRPostgreSQLModel) {
          mpdrTag = doc.createElement("MPDR_PostgreSQL");
        } else if (mpdrModel instanceof MPDRMySQLModel) {
          mpdrTag = doc.createElement("MPDR_MySQL");
        }
        mldrTag.appendChild(mpdrTag);

        //Ajout de l'id à la balise <MPDR_xx>
        mpdrTag.setAttribute("id", mpdrModel.getIdProjectElementAsString());

        //Persistance des tables (y compris les FKs)
        this.addMDRTables(doc, mpdrModel, mpdrTag);

      }
    }
  }

  // *** Méthodes de sauvegarde génériques pour les MLD et MPD ***

  /**
   * Sauvegarde les tables dans une balise <tables> en parcourant le modèle MDR (MLDR ou MPDR). Toutes les contraintes de tables, y compris les FK, sont persistées également.
   * @param doc Document XML dans lequel la persistance se fait
   * @param mdrModel Modèle MDR (MLDR ou MPDR) parcouru pour lequel toutes les tables qu'il contient seront persistées.
   * @param mdrTag Balise racine <mldr> ou <mldr> qui sera la balise parent de la balise <tables> enfant.
   */
  private void addMDRTables(Document doc, MDRModel mdrModel, Element mdrTag) {

    //Création de la balise <tables>
    Element tablesTag = doc.createElement("tables");
    mdrTag.appendChild(tablesTag);

    //Ajout de l'id à la balise <tables>
    tablesTag.setAttribute("id", mdrModel.getMDRContTables().getIdProjectElementAsString());

    //Parcours des tables
    for (MDRTable mdrTable : mdrModel.getMDRTables()) {

      //Persistance d'une table
      this.addTable(doc, mdrTable, tablesTag);
    }
  }

  /**
   * Sauvegarde d'une table dans une balise <table>. Inclut également la persistance des contraintes de la table (y compris les FKs) et les extrémités de relations (pour les tables MLDR).
   * @param doc Document XML dans lequel la persistance se fait
   * @param mdrTable Table à persister
   * @param tablesTag Balise parent <tables> qui contient la nouvelle balise <table>
   */
  private void addTable(Document doc, MDRTable mdrTable, Element tablesTag) {

    //Création de la balise <table>
    Element tableTag = doc.createElement("table");
    tablesTag.appendChild(tableTag);

    //Ajout des attributs à la balise <table>
    tableTag.setAttribute("id", mdrTable.getIdProjectElementAsString());
    tableTag.setAttribute("name", mdrTable.getName());
    tableTag.setAttribute("shortName", mdrTable.getShortName());
    tableTag.setAttribute("longName", mdrTable.getShortName());
    tableTag.setAttribute("name30", mdrTable.getNames().getName30());
    tableTag.setAttribute("name60", mdrTable.getNames().getName60());
    tableTag.setAttribute("name120", mdrTable.getNames().getName120());

    //Ajout de l'attribut "element_source_id" (en récupérant le bon élément en fonction du cas MLDR ou MPDR)
    if (mdrTable instanceof MLDRTable) {
      tableTag.setAttribute("element_source_id", ((MLDRTable) mdrTable).getMcdElementSource().getIdProjectElementAsString());
    } else if (mdrTable instanceof MPDRTable) {
      tableTag.setAttribute("element_source_id", ((MPDRTable) mdrTable).getMldrElementSource().getIdProjectElementAsString());
    }

    //Persistance des colonnes
    this.addColumns(doc, mdrTable, tableTag);

    //Persistance des contraintes de la table
    this.addTableConstraints(doc, mdrTable, tableTag);

    //Persistance des extrémités de relations de la table (uniquement pour le MLDR, car les relations n'existent pas en MPDR)
    if (mdrTable instanceof MLDRTable) {
      this.addTableRelEnds(doc, mdrTable, tableTag);
    }
  }

  /**
   * Sauvegarde des colonnes d'une table.
   * @param doc Document XML dans lequel les colonnes seront persistées
   * @param mdrTable Table pour laquelle les colonnes qu'elle contient seront persistées
   * @param tableTag Balise parent <table> qui contiendra la nouvelle balise <columns>
   */
  private void addColumns(Document doc, MDRTable mdrTable, Element tableTag) {

    //Création de la balise <columns>
    Element columnsTag = doc.createElement("columns");
    tableTag.appendChild(columnsTag);

    //Ajout de l'id à la balise <columns>
    columnsTag.setAttribute("id", mdrTable.getMDRContColumns().getIdProjectElementAsString());

    //Parcours des colonnes
    for (MDRColumn mldrColumn : mdrTable.getMDRColumns()) {

      //Persistance d'une colonne
      this.addColumn(doc, mldrColumn, columnsTag);
    }
  }

  /**
   * Sauvegarde d'une colonne parmi la liste des colonnes d'une table
   * @param doc Document XML dans lequel la colonne sera persistée.
   * @param mdrColumn Colonne qui sera persistée.
   * @param columnsTag Balise parent <columns> qui contiendra la nouvelle balise <column>.
   */
  private void addColumn(Document doc, MDRColumn mdrColumn, Element columnsTag) {
    //Création de la balise <column>
    Element columnTag = doc.createElement("column");
    columnsTag.appendChild(columnTag);

    //Ajout des propriétés d'identification d'une colonne à la balise <column>
    columnTag.setAttribute("id", mdrColumn.getIdProjectElementAsString());
    columnTag.setAttribute("name", mdrColumn.getName());
    columnTag.setAttribute("shortName", mdrColumn.getShortName());
    columnTag.setAttribute("longName", mdrColumn.getLongName());
    columnTag.setAttribute("name30", mdrColumn.getNames().getName30());
    columnTag.setAttribute("name60", mdrColumn.getNames().getName60());
    columnTag.setAttribute("name120", mdrColumn.getNames().getName120());

    //Ajout de l'attribut "element_source_id"
    if (mdrColumn instanceof MLDRColumn) {
      columnTag.setAttribute("element_source_id", ((MLDRColumn) mdrColumn).getMcdElementSource().getIdProjectElementAsString());
    } else if (mdrColumn instanceof MPDRColumn) {
      columnTag.setAttribute("element_source_id", ((MPDRColumn) mdrColumn).getMldrElementSource().getIdProjectElementAsString());
    }

    //Ajout des autres propriétés relatives à une colonne
    columnTag.setAttribute("mandatory", mdrColumn.isMandatory() ? "true" : "false");
    columnTag.setAttribute("frozen", mdrColumn.isFrozen() ? "true" : "false");
    columnTag.setAttribute("uppercase", mdrColumn.isUppercase() ? "true" : "false");
    columnTag.setAttribute("iteration", String.valueOf(mdrColumn.getIteration()));
    columnTag.setAttribute("initValue", mdrColumn.getInitValue());
    columnTag.setAttribute("derivedValue", mdrColumn.getDerivedValue());

    //Ajout de l'id de la colonne PK pointée dans le cas d'une colonne FK
    if (mdrColumn.getMDRColumnPK() != null) {
      columnTag.setAttribute("target_column_pk", mdrColumn.getMDRColumnPK().getIdProjectElementAsString());
    }

    //Ajout du type de données
    columnTag.setAttribute("datatype_lienprog", mdrColumn.getDatatypeLienProg());
    columnTag.setAttribute("datatype_constraint_lienprog", mdrColumn.getDatatypeConstraintLienProg());

    //Ajout de size et scale, qui sont des propriétés optionnelles
    if (mdrColumn.getSize() != null) {
      columnTag.setAttribute("size", String.valueOf(mdrColumn.getSize()));
    }
    if (mdrColumn.getScale() != null) {
      columnTag.setAttribute("scale", String.valueOf(mdrColumn.getScale()));
    }
  }

  /**
   * Sauvegarde des contraintes d'une table.
   * @param doc Document XML dans lequel la persistance se fait
   * @param mdrTable Table pour laquelle les contraintes qu'elle contient seront persistées
   * @param tableTag Balise parent <table> qui contiendra la nouvelle balise <constraints>
   */
  private void addTableConstraints(Document doc, MDRTable mdrTable, Element tableTag) {

    //Création de la balise <constraints>
    Element tableConstraintsTag = doc.createElement("tableConstraints");
    tableTag.appendChild(tableConstraintsTag);

    //Ajout de l'id à la balise <constraints>
    tableConstraintsTag.setAttribute("id", mdrTable.getMDRContConstraints().getIdProjectElementAsString());

    //Parcours des contraintes
    for (MDRConstraint mdrConstraint : mdrTable.getMDRContConstraints().getMDRConstraints()) {

      //Persistance d'une contrainte
      this.addTableConstraint(doc, mdrConstraint, tableConstraintsTag);
    }
  }

  /**
   * Sauvegarde d'une contrainte de table (qu'elle soit PK, FK, etc.)
   * @param doc Document XML dans lequel la contrainte sera persistée.
   * @param tableConstraint La contrainte de table qui sera persistée.
   * @param tableConstraintsTag Balise parent <tableConstraints> qui contiendra la nouvelle balise <constraint>
   */
  private void addTableConstraint(Document doc, MDRConstraint tableConstraint, Element tableConstraintsTag) {

    //Préparation de la balise <xxxConstraint>
    Element constraintTag = null;

    //Sauvegarde d'une contrainte PK
    if (tableConstraint instanceof MDRPK) {
      MDRPK pkConstraint = (MDRPK) tableConstraint;

      //Création de la balise <pk>
      constraintTag = doc.createElement("pk");
      tableConstraintsTag.appendChild(constraintTag);

      //Ajout des propriétés d'identification spécifiques à une contrainte PK
      if (pkConstraint instanceof MLDRPK) {
        constraintTag.setAttribute("element_source_id", ((MLDRPK) pkConstraint).getMcdElementSource().getIdProjectElementAsString());
      } else if (pkConstraint instanceof MPDRPK) {
        constraintTag.setAttribute("element_source_id", ((MPDRPK) pkConstraint).getMldrElementSource().getIdProjectElementAsString());
      }
    }

    //Sauvegarde d'une contrainte FK
    else if (tableConstraint instanceof MDRFK) {
      MDRFK fkConstraint = (MDRFK) tableConstraint;

      //Création de la balise <fk>
      constraintTag = doc.createElement("fk");
      tableConstraintsTag.appendChild(constraintTag);

      //Ajout des propriétés d'identification spécifiques à une contrainte FK
      if (fkConstraint instanceof MLDRFK) {
        constraintTag.setAttribute("element_source_id", ((MLDRFK) fkConstraint).getMcdElementSource().getIdProjectElementAsString());
      } else if (fkConstraint instanceof MPDRFK) {
        constraintTag.setAttribute("element_source_id", ((MPDRFK) fkConstraint).getMldrElementSource().getIdProjectElementAsString());
      }

      //Ajout de la référence vers la PK (id de la PK)
      constraintTag.setAttribute("target_pk", fkConstraint.getMdrPK().getIdProjectElementAsString());
    }

    if (constraintTag != null) {

      //Ajout des propriétés d'identification d'une contrainte de table
      constraintTag.setAttribute("id", tableConstraint.getIdProjectElementAsString());
      constraintTag.setAttribute("name", tableConstraint.getName());
      constraintTag.setAttribute("shortName", tableConstraint.getShortName());
      constraintTag.setAttribute("longName", tableConstraint.getLongName());
      constraintTag.setAttribute("name30", tableConstraint.getNames().getName30());
      constraintTag.setAttribute("name60", tableConstraint.getNames().getName60());
      constraintTag.setAttribute("name120", tableConstraint.getNames().getName120());

      //Ajout de la balise <targetParameters>
      Element targetParametersTag = doc.createElement("targetParameters");
      constraintTag.appendChild(targetParametersTag);

      //Parcours des parameters contenus dans la contrainte (chaque Parameter correspondant à une colonne de la contrainte)
      for (MDRParameter mdrParameter : tableConstraint.getMDRParameters()) {

        //Persistance d'une référence vers une colonne
        this.addTargetParameterOfTableConstraint(doc, mdrParameter, targetParametersTag);
      }
    }
  }

  /**
   * Persistance d'une référence de colonne cible (d'un Parameter) d'une contrainte de table. Par exemple, persistance de la référence vers la colonne pointée par une PK.
   * @param doc Document XML dans lequel la contrainte sera persistée.
   * @param targetMdrParameter Le parameter contenant la colonne cible. Ce parameter est utilisé pour persister la référence vers la colonne.
   * @param targetParametersTag La balise parent <targetParameters> dans laquelle sera créé une nouvelle balise <targetParameter>
   */
  private void addTargetParameterOfTableConstraint(Document doc, MDRParameter targetMdrParameter, Element targetParametersTag) {

    //Création de la balise <targetParameter>
    Element targetParameterTag = doc.createElement("targetParameter");
    targetParametersTag.appendChild(targetParameterTag);

    //Ajout des propriétés d'un Parameter
    targetParameterTag.setAttribute("id", targetMdrParameter.getIdProjectElementAsString());

    //Ajout des propriétés d'un Parameter MLDR
    if (targetMdrParameter instanceof MLDRParameter) {
      targetParameterTag.setAttribute("element_source_id", ((MLDRParameter) targetMdrParameter).getMcdElementSource().getIdProjectElementAsString());
    }

    //Ajout des propriétés d'un Parameter MPDR
    if (targetMdrParameter instanceof MPDRParameter) {
      targetParameterTag.setAttribute("element_source_id", ((MPDRParameter) targetMdrParameter).getMldrElementSource().getIdProjectElementAsString());
    }

    //Ajout des propriétés de la colonne pointée par le Parameter
    if (targetMdrParameter.getTarget() instanceof MDRColumn) {
      MDRColumn targetColumn = (MDRColumn) targetMdrParameter.getTarget();
      targetParameterTag.setAttribute("target_column_id", targetColumn.getIdProjectElementAsString());
    }
  }

  /**
   * Sauvegarde les extrémités de relation rattachées à chaque table du MLD. Remarque: les extrémités de relations ne sont pas les FK. Ce sont des objets qui permettent de rattacher une relation et une table du MLD.
   * @param doc Document XML dans lequel la persistance se fait
   * @param mdrTable Table pour laquelle les extrémités de relations seront persistées
   * @param tableTag Balise parent <table> qui contiendra la nouvelle balise <extremitesRelations>
   */
  private void addTableRelEnds(Document doc, MDRTable mdrTable, Element tableTag) {
    //Création de la balise <extremitesRelations>
    Element extremitesRelationsTag = doc.createElement("extremitesRelations");
    tableTag.appendChild(extremitesRelationsTag);

    //Ajout de l'id à la balise <extremitesRelations>
    extremitesRelationsTag.setAttribute("id", mdrTable.getMDRContRelEnds().getIdProjectElementAsString());

    //Parcours des extrémités de relations
    for (MDRRelEnd mdrRelEnd : mdrTable.getMDRContRelEnds().getMDRRelEnds()) {

      //Persistance d'une extrémité de relation
      this.addTableRelEnd(doc, mdrRelEnd, extremitesRelationsTag);
    }
  }

  /**
   * Sauvegarde d'une extrémité de relation d'une table.
   * @param doc Document XML dans lequel la contrainte sera persistée.
   * @param mdrRelEnd L'extrémité de relation qui sera persistée.
   * @param extremitesRelationsTag Balise parent <extremitesRelationsTag> qui contiendra la nouvelle balise <extremiteRelation>
   */
  private void addTableRelEnd(Document doc, MDRRelEnd mdrRelEnd, Element extremitesRelationsTag) {
    //Création de la balise <extremiteRelation>
    Element extremiteRelationTag = doc.createElement("extremiteRelation");
    extremitesRelationsTag.appendChild(extremiteRelationTag);

    //Ajout des propriétés d'identification d'une extrémité de relation
    extremiteRelationTag.setAttribute("id", mdrRelEnd.getIdProjectElementAsString());
    extremiteRelationTag.setAttribute("name", mdrRelEnd.getName());
    extremiteRelationTag.setAttribute("shortName", mdrRelEnd.getShortName());
    extremiteRelationTag.setAttribute("longName", mdrRelEnd.getLongName());
    extremiteRelationTag.setAttribute("name30", mdrRelEnd.getNames().getName30());
    extremiteRelationTag.setAttribute("name60", mdrRelEnd.getNames().getName60());
    extremiteRelationTag.setAttribute("name120", mdrRelEnd.getNames().getName120());
  }

  /**
   * Sauvegarde les relations du MLD dans le fichier de sauvegarde XML du projet. Remarque: les relations ne sont pas les FKs. Il s'agit réellement des liens dessinés entre les tables, contenant les cardinalités. Cela fait redondance avec les FKs, mais cela est volontaire.
   * @param doc Document XML dans lequel la contrainte sera persistée.
   * @param mdrModel Modèle relationnel déjà créé dans l'application, dans lequel seront ajoutés les relations.
   * @param mdrTag Balise racine du modèle relationnel (par exemple MLDR_DT...), qui contiendra les nouvelles balises des relations.
   */
  private void addMDRelations(Document doc, MDRModel mdrModel, Element mdrTag) {

    //Création de la balise <mdrRelations>
    Element mdrRelationsTag = doc.createElement("mdrRelations");
    mdrTag.appendChild(mdrRelationsTag);

    //Ajout de l'id à la balise <mdrRelations>
    mdrRelationsTag.setAttribute("id", mdrModel.getMDRContRelations().getIdProjectElementAsString());

    //Parcours des relations FK
    for (MDRRelationFK mdrRelationFK : mdrModel.getMDRContRelations().getMDRRelationsFK()) {

      //Persistance d'une relation FK
      this.addMDRRelationFK(doc, mdrRelationFK, mdrRelationsTag);
    }
  }

  /**
   * Persistance d'une relation FK.
   * @param doc Document XML dans lequel la contrainte sera persistée.
   * @param mdrRelationFK La relation FK à persister.
   * @param mdrRelationsTag La balise racine <mdrRelations> sous laquelle il faut ajouter la nouvelle balise pour la relation FK à persister.
   */
  private void addMDRRelationFK(Document doc, MDRRelationFK mdrRelationFK, Element mdrRelationsTag) {

    //Création de la balise <mdrRelation>
    Element mdrRelationTag = doc.createElement("mdrRelation");
    mdrRelationsTag.appendChild(mdrRelationTag);

    //Ajout des attributs à la balise <mdrRelation>
    mdrRelationTag.setAttribute("id", mdrRelationFK.getIdProjectElementAsString());
    mdrRelationTag.setAttribute("name", mdrRelationFK.getName());
    mdrRelationTag.setAttribute("shortName", mdrRelationFK.getShortName());
    mdrRelationTag.setAttribute("longName", mdrRelationFK.getLongName());
    mdrRelationTag.setAttribute("name30", mdrRelationFK.getNames().getName30());
    mdrRelationTag.setAttribute("name60", mdrRelationFK.getNames().getName60());
    mdrRelationTag.setAttribute("name120", mdrRelationFK.getNames().getName120());

    //Ajout des attributs spécifiques à une relation FK de niveau MLD
    if (mdrRelationFK instanceof MLDRRelationFK) {

      //Ajout de la référence (id) vers l'élément MCD source dans le cas d'une relation MLDR
      mdrRelationTag.setAttribute("element_source_id", ((MLDRRelationFK) mdrRelationFK).getMcdElementSource().getIdProjectElementAsString());
    }

    //Ajout de la référence (id) vers les 2 extrémités de la relation (A et B)
    mdrRelationTag.setAttribute("extremiteRelA_target_id", ((MDRRelFKEnd) mdrRelationFK.getA()).getIdProjectElementAsString());
    mdrRelationTag.setAttribute("extremiteRelB_target_id", ((MDRRelFKEnd) mdrRelationFK.getB()).getIdProjectElementAsString());

    //Ajout de la référence (id) vers la FK
    mdrRelationTag.setAttribute("fk_target_id", String.valueOf(mdrRelationFK.getMdrFKId()));
  }

  private void addClassShape(Document doc, ClassShape shape, Element shapesTag) {
    Element shapeElement = doc.createElement(shape.getXmlTagName());

    // Ajoute les attributs à la balise créée
    shapeElement.setAttribute(Preferences.ATTRIBUTE_ID, String.valueOf(shape.getId()));
    shapeElement.setAttribute(Preferences.ATTRIBUTE_HEIGHT, String.valueOf(shape.getHeight()));
    shapeElement.setAttribute(Preferences.ATTRIBUTE_WIDTH, String.valueOf(shape.getWidth()));
    shapeElement.setAttribute(Preferences.ATTRIBUTE_X, String.valueOf(shape.getX()));
    shapeElement.setAttribute(Preferences.ATTRIBUTE_Y, String.valueOf(shape.getY()));

    // Vérifie si la forme a bien un objet du référentiel lié
    if (shape.getRelatedRepositoryElement() != null) {
      shapeElement.setAttribute(Preferences.ATTRIBUTE_REPOSITORY_ENTITY_ID, shape.getRelatedRepositoryElement().getIdProjectElementAsString());
    }

    // Ajoute l'élément à la balise parent
    shapesTag.appendChild(shapeElement);
  }

  private void addRelationShape(Document doc, RelationShape shape, Element shapeTags) {
    Element shapeElement = doc.createElement(shape.getXmlTagName());

    // Ajoute les attributs à la relation
    shapeElement.setAttribute(Preferences.ATTRIBUTE_ID, String.valueOf(shape.getId()));
    shapeElement.setAttribute(Preferences.ATTRIBUTE_SOURCE_ENTITY_SHAPE_ID, String.valueOf(shape.getSource().getId()));
    shapeElement.setAttribute(Preferences.ATTRIBUTE_DESTINATION_ENTITY_SHAPE_ID, String.valueOf(shape.getDestination().getId()));

    // Vérifie si la forme a bien un objet du référentiel lié
    if (shape.getRelatedRepositoryElement() != null) {
      shapeElement.setAttribute(Preferences.ATTRIBUTE_REPOSITORY_ASSOCIATION_ID, shape.getRelatedRepositoryElement().getName());
    }

    // Ajoute les points d'ancrage
    this.addAnchorPointsShapes(doc, shape, shapeElement);

    // Ajoute les labels
    if (!shape.getLabels().isEmpty()) {
      this.addLabelShapes(doc, shape, shapeElement);
    }

    // Ajoute l'élément à la balise parent
    shapeTags.appendChild(shapeElement);

  }

  private void addAnchorPointsShapes(Document doc, RelationShape shape, Element relationTag) {

    Element anchorPointsTag = doc.createElement(Preferences.DIAGRAMMER_ANCHOR_POINTS_XML_TAG_NAME);

    // Crée, pour chaque point d'ancrage, une balise enfant
    for (RelationAnchorPointShape pointAncrageShape : shape.getAnchorPoints()) {
      Element anchorPointElement = doc.createElement(pointAncrageShape.getXmlTagName());

      anchorPointElement.setAttribute(Preferences.ATTRIBUTE_ID, String.valueOf(pointAncrageShape.getId()));
      anchorPointElement.setAttribute(Preferences.ATTRIBUTE_X, String.valueOf(pointAncrageShape.x));
      anchorPointElement.setAttribute(Preferences.ATTRIBUTE_Y, String.valueOf(pointAncrageShape.y));

      // Ajoute le noeud "anchorPoint" au noeud "anchorPoints"
      anchorPointsTag.appendChild(anchorPointElement);
    }

    relationTag.appendChild(anchorPointsTag);
  }

  private void addLabelShapes(Document doc, RelationShape shape, Element relationTag) {

    Element labelsTag = doc.createElement(Preferences.DIAGRAMMER_LABELS_XML_TAG_NAME);

    // Crée, pour chaque label, une balise enfant
    for (LabelShape label : shape.getLabels()) {
      Element labelShapeElement = doc.createElement(label.getXmlTagName());

      labelShapeElement.setAttribute(Preferences.ATTRIBUTE_RELATED_ANCHOR_POINT_ID, String.valueOf(label.getPointAncrage().getId()));
      labelShapeElement.setAttribute(Preferences.ATTRIBUTE_X_DISTANCE_FROM_ANCHOR_POINT, String.valueOf(label.getDistanceInXFromPointAncrage()));
      labelShapeElement.setAttribute(Preferences.ATTRIBUTE_Y_DISTANCE_FROM_ANCHOR_POINT, String.valueOf(label.getDistanceInYFromPointAncrage()));
      labelShapeElement.setAttribute(Preferences.ATTRIBUTE_TYPE, String.valueOf(label.getType()));

      // Ajoute le noeud "labelShape" au noeud "labelsTag"
      labelsTag.appendChild(labelShapeElement);
    }

    relationTag.appendChild(labelsTag);
  }
}