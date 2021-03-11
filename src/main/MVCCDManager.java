package main;

import console.Console;
import datatypes.MDDatatypesManager;
import main.window.console.WinConsoleContent;
import main.window.diagram.WinDiagram;
import main.window.diagram.WinDiagramContent;
import main.window.menu.WinMenuContent;
import main.window.repository.WinRepository;
import main.window.repository.WinRepositoryContent;
import main.window.repository.WinRepositoryTree;
import mcd.MCDRelEnd;
import mcd.MCDRelation;
import messages.LoadMessages;
import preferences.Preferences;
import preferences.PreferencesManager;
import project.*;
import repository.Repository;
import utilities.Trace;
import utilities.files.UtilFiles;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.ArrayList;

/**
 * Il s'agit de la classe d'orchestration du programme.
 * Les attributs permettent cette orchestration.
 */
public class MVCCDManager {

    private static MVCCDManager instance;

    private MVCCDWindow mvccdWindow;  //Ecran principal
    private Repository repository;  //Référentiel
    private MVCCDElementRepositoryRoot rootMVCCDElement; //Elément root du référentiel repository.root.name=Application MVCCD
    private Project project;    //Projet en cours de traitement
    private Console console;    //Classe d'accès à la console d'affichage de messages
    private ProjectsRecents projectsRecents = null; //Projets ouverts  récemment
    private File fileProjectCurrent = null; //Fichier de sauvegarde du projet en cours de traitement
    private boolean datasProjectChanged = false; //Indicateur de changement de données propres au projet
    //private boolean datasEdited = true; //Indicateur d'édition de données y-compris les préférences d'application

    public static synchronized MVCCDManager instance() {
        if (instance == null) {
            instance = new MVCCDManager();
        }
        return instance;
    }

    /**
     * Lance MVC-CD-3
     */
    public void start() {
        // Chargement des messages de traduction
        LoadMessages.main();

        //Chargement des préférences de l'application
        PreferencesManager.instance().loadOrCreateFileXMLApplicationPref(); //Ajout de Giorgio Roncallo
        /*
        if(Preferences.PERSISTENCE_SERIALISATION_INSTEADOF_XML){
            PreferencesManager.instance().loadOrCreateFileApplicationPreferences(); //Persistance avec sérialisation
        }else{
            PreferencesManager.instance().loadOrCreateFileXMLApplicationPref(); //Ajout de Giorgio Roncallo
        }
         */

        // Création et affichage de l'écran d'accueil
        startMVCCDWindow();
        // Création de la console
        startConsole();
        // Création du référentiel
        startRepository();
        // Ajustement de la taille de la zone d'affichage du référentiel
        mvccdWindow.adjustPanelRepository();
        // Chargement des adresses disques des derniers fichiers de projets utilisés
        projectsRecents = new ProjectsRecentsLoader().load();
        // Création du menu contextuel des fichiers de projets récemment utilisés
        changeActivateProjectOpenRecentsItems();
        // Ouverture du dernier fichier de projet utilisés
        openLastProject();
    }


    /**
     * Créé le référentiel et l'affiche à l'écran d'accueil.
     */
    private void startRepository() {
        // Création de l'élément root du référentiel
        rootMVCCDElement = MVCCDFactory.instance().createRepositoryRoot();
        // Création des types de données
        MDDatatypesManager.instance().mdDatatypes();
        // Création du noeud root de l'arbre du référentiel
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootMVCCDElement);
        // Création du référentiel
        repository = new Repository(rootNode, rootMVCCDElement); //Créer un repository vide (le chargement se fait après, dans l'ouverture du projet)
        // Affiche le référentiel dans l'écran d'accueil
        getWinRepositoryContent().getTree().changeModel(repository);
    }



    private void startConsole() {

        console = new Console();
    }

    /**
     * La méthode crée et affiche l'écran d'accueil.
     * L'écran comporte 5 zones :
     * <pre>
     *  - Haut		Probablement des commandes contextuelles à terme
     *  - Gauche	L’arbre de représentation du contenu du référentiel
     *  - Centre	Le diagrammeur
     *  - Droite	Une zone de réserve
     *  - Bas		Une console d’affichage (Contrôle de conformité…)
     * </pre>
     * <img src="doc-files/UI_homeScreen.jpg" alt="Fenêtre de l'écran d'accueil">
     */
    public void startMVCCDWindow() {
        mvccdWindow = new MVCCDWindow();
        mvccdWindow.setVisible(true);
        mvccdWindow.getPanelBLResizer().resizerContentPanels();
    }

    public void completeNewProject() {
        //this.project = project;
        PreferencesManager.instance().setProjectPref(project.getPreferences()); //TODO-STB: voir avec PAS pour comprendre
        PreferencesManager.instance().copyApplicationPref(Project.NEW);
        project.adjustProfile();
        projectToRepository();
        mvccdWindow.adjustPanelRepository();
        setFileProjectCurrent(null);
    }

    //TODO-0  A remplacer par même méthode sans nodeParent
    public void addNewMVCCDElementInRepository(MVCCDElement mvccdElementNew, DefaultMutableTreeNode nodeParent) {
        DefaultMutableTreeNode nodeNew = MVCCDManager.instance().getRepository().addMVCCDElement(nodeParent, mvccdElementNew);
        getWinRepositoryContent().getTree().changeModel(repository);
        getWinRepositoryContent().getTree().scrollPathToVisible(new TreePath(nodeNew.getPath()));
        setDatasProjectChanged(true);
    }

    public void addNewMVCCDElementInRepository(MVCCDElement mvccdElementNew) {
        ProjectElement parent = (ProjectElement) mvccdElementNew.getParent();
        DefaultMutableTreeNode nodeParent = ProjectService.getNodeById(parent.getIdProjectElement());
        DefaultMutableTreeNode nodeNew = MVCCDManager.instance().getRepository().addMVCCDElement(nodeParent, mvccdElementNew);
        getWinRepositoryContent().getTree().changeModel(repository);
        getWinRepositoryContent().getTree().scrollPathToVisible(new TreePath(nodeNew.getPath()));
        setDatasProjectChanged(true);
    }

    public void showMVCCDElementInRepository(MVCCDElement mvccdElement) {
        if (mvccdElement instanceof ProjectElement) {
            ProjectElement projectElement = (ProjectElement) mvccdElement;
            DefaultMutableTreeNode node = ProjectService.getNodeById(projectElement.getIdProjectElement());
            //getWinRepositoryContent().getTree().changeModel(repository);
            getWinRepositoryContent().getTree().getTreeModel().reload(node);
            getWinRepositoryContent().getTree().scrollPathToVisible(new TreePath(node.getPath()));
        }
    }

    public void removeMVCCDElementInRepository(MVCCDElement mvccdElementToRemove, MVCCDElement parent) {
        DefaultMutableTreeNode nodeParent = ProjectService.getNodeById(((ProjectElement)parent).getIdProjectElement());
        ProjectElement child = (ProjectElement) mvccdElementToRemove;
        DefaultMutableTreeNode nodeChild= ProjectService.getNodeById(child.getIdProjectElement());

        MVCCDManager.instance().getRepository().removeNodeFromParent(nodeChild);
        getWinRepositoryContent().getTree().changeModel(repository);
        //getWinRepositoryContent().getTree().getTreeModel().reload();
        setDatasProjectChanged(true);
    }

    public void changeParentMVCCDElementInRepository(MVCCDElement mvccdElementChanged, MVCCDElement oldParent) {
        removeMVCCDElementInRepository(mvccdElementChanged, oldParent);
        addNewMVCCDElementInRepository(mvccdElementChanged);
    }

    public void showNewNodeInRepository(DefaultMutableTreeNode node) {
        // Affichage du noeud
        //getWinRepositoryContent().getTree().changeModel(repository);
        getWinRepositoryContent().getTree().getTreeModel().reload();
        getWinRepositoryContent().getTree().scrollPathToVisible(new TreePath(node.getPath()));

    }

    public void removeMCDRelationAndDependantsInRepository(MCDRelation mcdRelation) {

        ArrayList<MCDRelation> mcdRelationChilds = mcdRelation.getMCDRelationsChilds();
        for (MCDRelation mcdRelationChild : mcdRelationChilds) {
            removeMCDRelationAndDependantsInRepository(mcdRelationChild);
        }
        removeMVCCDElementInRepository(mcdRelation, mcdRelation.getParent());
        removeMVCCDElementInRepository((MCDRelEnd)mcdRelation.getA(), ((MCDRelEnd) mcdRelation.getA()).getParent());
        removeMVCCDElementInRepository((MCDRelEnd)mcdRelation.getB(), ((MCDRelEnd) mcdRelation.getB()).getParent());
    }

    public void openProject() {
        ProjectFileChooser fileChooser = new ProjectFileChooser(ProjectFileChooser.OPEN);
        File fileChoose = fileChooser.fileChoose();
        openProjectBase(fileChoose);
    }

    public void openProjectRecent(String filePath) {
        File file = new File(filePath);
        openProjectBase(file);
    }

    /**
     * Recherche un éventuel dernier projet utilisé et en demande l'ouverture.
     */
    private void openLastProject() {
        if (projectsRecents.getRecents().size() > 0) {
            File file = projectsRecents.getRecents().get(0);
            openProjectBase(file);
        }
    }

    /**
     * Ouvre un projet à partir de son fichier de sauvegarde et le charge dans le référentiel.
     */
    private void openProjectBase(File file) {
        //Mémorise le fichier associé au projet
        setFileProjectCurrent(file);

        if (file != null) {
            // Lecture du fichier de sauvegarde
            if(Preferences.PERSISTENCE_SERIALISATION_INSTEADOF_XML){
                project = new LoaderSerializable().load(fileProjectCurrent); //Persistance avec sérialisation
            }else{
                project = new ProjectLoaderXml().loadProjectFile(fileProjectCurrent); //Ajout de Giorgio Roncallo
            }
            // Chargement des préférences du projet
            PreferencesManager.instance().setProjectPref(project.getPreferences());
            // Copie des préférences d'application au sein des préférences du projet
            PreferencesManager.instance().copyApplicationPref(Project.EXISTING);

        }
        if (project != null) {
            // Reprise des préférences de profil (si existant)
            project.adjustProfile();
            // Copie du projet au sein du référentiel
            projectToRepository();
            project.debugCheckLoadDeep(); //Provisoire pour le test de sérialisation/déséralisation
            // Mémorisation du fichier de projet utilisé
            projectsRecents.add(fileProjectCurrent);
            // Mise à jour du menu contextuel des fichiers de projets récemment utilisés
            changeActivateProjectOpenRecentsItems();
            // Ajustement de la taille de la zone d'affichage du référentiel
            mvccdWindow.adjustPanelRepository();
        }
    }


    public void saveProject() {
        if (fileProjectCurrent != null) {
            if(Preferences.PERSISTENCE_SERIALISATION_INSTEADOF_XML){
                new SaverSerializable().save(fileProjectCurrent); //Persistance avec sérialisation
            }else{
                new ProjectSaverXml().createProjectFile(fileProjectCurrent); //Ajout de Giorgio
            }

        } else {
            saveAsProject();
        }
        setDatasProjectChanged(false);
    }

    public void saveAsProject() {
        ProjectFileChooser fileChooser = new ProjectFileChooser(ProjectFileChooser.SAVE);
        File fileChoose = fileChooser.fileChoose();
        if (fileChoose != null){
            if (UtilFiles.confirmIfExist(mvccdWindow, fileChoose)) {
                fileProjectCurrent = fileChoose;
                if(Preferences.PERSISTENCE_SERIALISATION_INSTEADOF_XML){
                    new SaverSerializable().save(fileProjectCurrent); //Persistance avec sérialisation
                }else{
                    new ProjectSaverXml().createProjectFile(fileProjectCurrent); //Ajout de Giorgio
                }
                projectsRecents.add(fileProjectCurrent);
                changeActivateProjectOpenRecentsItems();
            }
        }
    }


    public void closeProject() {
        project = null;
        repository.removeProject();
        PreferencesManager.instance().setProfilePref(null);
        repository.removeProfile();
        setFileProjectCurrent(null);

    }


    private void changeActivateProjectOpenRecentsItems() {
        if (projectsRecents != null) {
            getWinMenuContent().desActivateProjectOpenRecentsItems();
            int i = 0;
            for (File file : projectsRecents.getRecents()) {
                getWinMenuContent().activateProjectOpenRecentsItem(i, file.getPath());
                i++;
            }
        } else {
            projectsRecents = new ProjectsRecents();
        }
    }

    /**
     * Copie le projet ouvert au sein du référentiel.
     */
    private void projectToRepository() {
        repository.removeProject();
        repository.addProject(project);
        profileToRepository();
        WinRepositoryTree tree = getWinRepositoryContent().getTree();
        tree.changeModel(repository);
        tree.showLastPath(project);
    }

    public void profileToRepository() {
        repository.removeProfile();
        if (project.getProfile() != null) {
            repository.addProfile(project.getProfile());
        }
        getWinRepositoryContent().getTree().changeModel(repository);
    }


    public MVCCDWindow getMvccdWindow() {
        return mvccdWindow;
    }

    public WinRepository getWinRepository() {
        return mvccdWindow.getRepository();
    }

    public WinRepositoryContent getWinRepositoryContent() {
        return (WinRepositoryContent) mvccdWindow.getRepository().getPanelContent();
    }

    public WinDiagram getWinDiagram() {
        return mvccdWindow.getDiagram();
    }

    public WinDiagramContent getWinDiagramContent() {
        return (WinDiagramContent) mvccdWindow.getDiagram().getPanelContent();
    }


    public WinMenuContent getWinMenuContent() {
        return mvccdWindow.getMenuContent();
    }

    public WinConsoleContent getWinConsoleContent() {
        return (WinConsoleContent) mvccdWindow.getConsole().getPanelContent();
    }

    public Repository getRepository() {
        return repository;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Console getConsole() {
        return console;
    }

    public File getFileProjectCurrent() {
        return fileProjectCurrent;
    }

    public void setFileProjectCurrent(File fileProjectCurrent) {
        if (datasProjectChanged) {
            //
        } else {
            mvccdWindow.getMenuContent().getProjectSave().setEnabled(false);
        }

        this.fileProjectCurrent = fileProjectCurrent;
    }

    public ProjectsRecents getProjectsRecents() {
        return projectsRecents;
    }

    public void setProjectsRecents(ProjectsRecents projectsRecents) {
        this.projectsRecents = projectsRecents;
    }

    public boolean isDatasProjectChanged() {
        return datasProjectChanged;
    }

    public void setDatasProjectChanged(boolean datasProjectChanged) {
        getWinMenuContent().getProjectSave().setEnabled(datasProjectChanged);
        this.datasProjectChanged = datasProjectChanged;
    }


    public MVCCDElement getRootMVCCDElement() {
        return rootMVCCDElement;
    }

    public void setRootMVCCDElement(MVCCDElementRepositoryRoot rootMVCCDElement) {
        this.rootMVCCDElement = rootMVCCDElement;
    }


    public MVCCDElementApplicationMDDatatypes getMDDatatypesRoot() {
        MVCCDElement mvccdElement = MVCCDElementService.getUniqueInstanceByClassName(rootMVCCDElement,
                MVCCDElementApplicationMDDatatypes.class.getName());
        if (mvccdElement != null) {
            return (MVCCDElementApplicationMDDatatypes) mvccdElement;
        } else {
            return null;
        }
    }

}
