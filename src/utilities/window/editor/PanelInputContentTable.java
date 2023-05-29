package utilities.window.editor;

import exceptions.service.ExceptionService;
import m.MElement;
import main.MVCCDElement;
import main.MVCCDManager;
import preferences.Preferences;
import project.ProjectElement;
import project.ProjectService;
import repository.RepositoryService;
import repository.editingTreat.EditingTreat;
import utilities.UtilDivers;
import utilities.window.ReadTableModel;
import utilities.window.editor.services.PanelInputContentTableService;
import utilities.window.scomponents.STable;
import utilities.window.scomponents.services.STableService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;

//TODO-0 A voir - problème de renommage
// la classe devrait être renommée : PanelInputContentTableWithoutTransaction !
public abstract class PanelInputContentTable extends PanelInputContent
        implements ActionListener {

    protected JPanel panelIdTable = new JPanel();

    protected STable table;
    String[] columnsNames;
    protected DefaultTableModel model;
    protected Object[][] datas;
    protected Object[][] oldDatas;


    protected JPanel panelTableComplete = new JPanel();
    protected JScrollPane panelTable;
    protected JPanel panelButtons;
    protected JButton btnAdd;
    protected JButton btnEdit;
    protected JButton btnRemove;
    protected JPanel panelMove;
    protected JButton btnUp;
    protected JButton btnDown;

    public PanelInputContentTable(PanelInput panelInput) {
        super(panelInput);
    }


    @Override
    public void createContentCustom() {

        makeTable();
        makeButtons();
        makeLayout();
    }

    protected void makeTable() {
        columnsNames = PanelInputContentTableService.columnsNames(specificColumnsNames());

        specificInitOrLoadTable();
        oldDatas = datas;

        table = new STable(this);
        model = new ReadTableModel(datas, columnsNames);

        table.setModel(model);


        table.setRowHeight(Preferences.EDITOR_TABLE_ROW_HEIGHT);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);

        PanelInputContentTableService.genericColumnsDisplay(table);
        specificColumnsDisplay();


        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                // ce ne sont pas les boutons standards mais ceux ceux spécifiques à la table
                enabledContent();
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });
    }


    protected abstract void specificColumnsDisplay();

    protected abstract void specificInitOrLoadTable();

    protected abstract String[] specificColumnsNames();

    protected abstract boolean specificRefreshRow();


    protected void refreshRow(int selectedRow){
        int idElementSelected = (int) table.getValueAt(selectedRow, STableService.IDINDEX);
        MElement mElementSelected = (MElement) ProjectService.getProjectElementById(idElementSelected);

        Object[] row = STableService.getRecord(table, selectedRow);

        putValueInRow(mElementSelected, row);
        UtilDivers.putValueRowInTable(table, selectedRow, row);
    };

    protected void  makeButtons(){
        btnAdd = new JButton("Ajouter");
        btnAdd.addActionListener(this);

        btnEdit = new JButton("Editer");
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(this);

        btnRemove = new JButton("Supprimer");
        btnRemove.setEnabled(false);
        btnRemove.addActionListener(this);

        btnUp = new JButton("^");
        btnUp.setEnabled(false);
        btnUp.addActionListener(this);

        btnDown = new JButton("v");
        btnDown.setEnabled(false);
        btnDown.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String propertyAction = "";
        try {
            Object source = e.getSource();
            MElement mElement = getMElementSelected();

            if (source == btnAdd) {
                propertyAction = "editor.table.exception.new";
                actionAdd(e);
            }
            if (source == btnEdit) {
                propertyAction = "editor.table.exception.update";
                actionEdit(e, mElement);
            }
            if (source == btnRemove) {
                propertyAction = "editor.table.exception.delete";
                actionDelete(e, mElement);
            }
            if (source == btnUp) {
                propertyAction = "editor.table.exception.up";
                actionUp(e);
            }
            if (source == btnDown) {
                propertyAction = "editor.table.exception.down";
                actionDown(e);
            }
        } catch (Exception exception){
            ExceptionService.exceptionUnhandled(exception, getEditor(),
                    getEditor().getMvccdElementCrt(),
                    "editor.table.exception",
                    propertyAction);

        }
    }

    protected MElement actionAdd(ActionEvent e) {
        //#MAJ 2021-06-30 Affinement de la trace de modification pour déclencher Save
        //MElement mElement = newElement();
        MElement mElement = (MElement) editingTreatDetail().treatNew(getEditor(), getEditor().getMvccdElementCrt());
        if (mElement != null) {
            Object[] row = newRow(mElement);
            model.addRow(row);
            table.setRowSelectionInterval(table.getRowCount() - 1, table.getRowCount() - 1);
            enabledContent();
        }
        return mElement;
    }

    protected abstract EditingTreat editingTreatDetail();


    protected void actionEdit(ActionEvent e, MElement mElement) {
        if (mElement != null) {
            // Mise à jour du référentiel
            //#MAJ 2021-06-30 Affinement de la trace de modification pour déclencher Save
            //updateElement(mElement);
            editingTreatDetail().treatUpdate(getEditor(), mElement);

            // Mise à jour de la table
            updateRow(mElement, table.getSelectedRow());
            enabledContent();
        }
    }

    protected boolean actionDelete(ActionEvent e, MElement mElement) {
        if (mElement != null){
            // Mise à jour du référentiel
            //#MAJ 2021-06-30 Affinement de la trace de modification pour déclencher Save
            //if (deleteElement(mElement)) {
            if (editingTreatDetail().treatDelete(getEditor(), mElement)) {
                // Mise à jour de la table
                model.removeRow(table.getSelectedRow());
                enabledContent();
                return true;
            }
        }
        return false;
    }


    private void actionUp(ActionEvent e){
        int posActual = table.getSelectedRow();
        if (posActual > 0){
            model.moveRow(posActual, posActual, posActual-1);
            table.setRowSelectionInterval(posActual-1, posActual-1);
            permuteOrder(posActual, posActual - 1);
            // pour la mise à jour des  champs calculés comme les stéréotypes d'unicité
            if (specificRefreshRow()) {
                refreshRow(posActual);
                refreshRow(posActual - 1);
            }
        }
        enabledContent();
        //TODO-1 Vérfier un changement effectif
        //Détecter un changement au niveau de la table JTable --> STable et ensuite déclenechement automatique
        enabledButtons();
    }

    private void actionDown(ActionEvent e){
        int posActual = table.getSelectedRow();
        if (posActual < table.getRowCount()-1){
            model.moveRow(posActual, posActual, posActual+1);
            table.setRowSelectionInterval(posActual+1, posActual+1);
            permuteOrder(posActual, posActual + 1);
            // pour la mise à jour des  champs calculés comme les stéréotypes d'unicité
            if (specificRefreshRow()) {
                refreshRow(posActual);
                refreshRow(posActual + 1);
            }
        }
        enabledContent();
        //TODO-1 Vérfier un changement effectif
        //Détecter un changement au niveau de la table JTable --> STable et ensuite déclenechement automatique
        enabledButtons();
    }


    //protected abstract MElement newElement();

    protected abstract Object[] newRow(MElement mElement);

    
    protected void updateRow(MElement mElement, int selectedRow) {

        Object[] row = STableService.getRecord(table, selectedRow);
        putValueInRow(mElement, row);
        UtilDivers.putValueRowInTable(table, selectedRow, row);
    }



    private void  makeLayout(){
        panelTable = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);



        panelMove = new JPanel();
        panelMove.setLayout(new BoxLayout(panelMove, BoxLayout.Y_AXIS));
        panelMove.add(Box.createVerticalGlue());
        panelMove.add(btnUp);
        panelMove.add(btnDown);
        panelMove.add(Box.createVerticalGlue());


        panelButtons= new JPanel();
        panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
        //panelButtons.setMaximumSize(ApplicationManager.instance().getViewManager().getScaledDimension(new Dimension(800, 10)));

        panelButtons.add(Box.createHorizontalGlue());
        panelButtons.add(btnAdd);
        panelButtons.add(btnEdit);
        panelButtons.add(btnRemove);
        panelButtons.add(Box.createHorizontalGlue());


        BorderLayout bl = new BorderLayout(5,5);
        panelTableComplete.setLayout(bl);
        panelTableComplete.add(panelTable, BorderLayout.CENTER);
        panelTableComplete.add(panelButtons, BorderLayout.SOUTH);
        panelTableComplete.add(panelMove, BorderLayout.EAST);


    }


    protected abstract void putValueInRow(MElement mElement, Object[] row);


    @Override
    protected void enabledContent() {
        int pos = table.getSelectedRow();
        if (pos >= 0){
            btnRemove.setEnabled(true);
            btnEdit.setEnabled(true);
            if (pos > 0){
                btnUp.setEnabled(true);
            } else {
                btnUp.setEnabled(false);
            }
            if (pos == table.getRowCount()-1){
                btnDown.setEnabled(false);
            } else{
                btnDown.setEnabled(true);
            }
        } else {
            btnRemove.setEnabled(false);
            btnEdit.setEnabled(false);
            btnUp.setEnabled(false);
            btnDown.setEnabled(false);
        }
    }



    private void permuteOrder(int posActual, int posNew) {

        int posOrder = STableService.ORDERINDEX;
        Integer orderActual = (Integer) table.getModel().getValueAt(posActual, posOrder);
        Integer orderNew = (Integer) table.getModel().getValueAt(posNew, posOrder);

        int posId =  STableService.IDINDEX;
        Integer idActual = (Integer) table.getModel().getValueAt(posActual, posId);
        Integer idOther = (Integer) table.getModel().getValueAt(posNew, posId);

        // Permutation dans la table
        table.getModel().setValueAt(orderNew, posActual, posOrder);
        table.getModel().setValueAt(orderActual, posNew, posOrder);

        // Permutation dans les 2 instances de descendants de ProjectElement
        updateOrderINProjectElement(idActual, orderNew);
        updateOrderINProjectElement(idOther, orderActual);

        // Le changeemt d'ordre n'est pas encapsulé dans la tzransaction !
        MVCCDManager.instance().setDatasProjectChanged(true);


        // Mise à jour de l'affichage du référentiel
        swapNodes(idActual, idOther);
    }


    private void updateOrderINProjectElement(Integer id, Integer order) {
        ProjectElement projectElement = ProjectService.getProjectElementById(id);
        projectElement.setOrder(order);
    }

    private void swapNodes(Integer idA, Integer idB) {

        DefaultMutableTreeNode nodeParent =  ProjectService.getNodeById(
                ((ProjectElement) getEditor().getMvccdElementCrt()).getIdProjectElement());

        DefaultMutableTreeNode nodeA = RepositoryService.instance().getNodeInChildsByIdElement(
                nodeParent, idA);
        int indexA = nodeParent.getIndex(nodeA);


        DefaultMutableTreeNode nodeB = RepositoryService.instance().getNodeInChildsByIdElement(
                nodeParent, idB);
        int indexB = nodeParent.getIndex(nodeB);

        nodeParent.insert(nodeA, indexB);
        nodeParent.insert(nodeB, indexA);

        MVCCDManager.instance().showNewNodeInRepository(nodeB);

    }

    // Pour éviter de surcharger les descendants
    @Override
    protected boolean changeField(DocumentEvent e) {
        return false;
    }

    // Pour éviter de surcharger les descendants
    @Override
    protected void changeFieldSelected(ItemEvent e) {
    }

    // Pour éviter de surcharger les descendants
    @Override
    protected void changeFieldDeSelected(ItemEvent e) {

    }

    // Pour éviter de surcharger les descendants
    @Override
    public void loadDatas(MVCCDElement mvccdElementCrt) {

    }

    // Pour éviter de surcharger les descendants
    @Override
    protected void initDatas() {

    }

    // Pour éviter de surcharger les descendants
    @Override
    protected void saveDatas(MVCCDElement mvccdElement) {

    }

    public Dimension resizeContent() {
        //TODO-1 A voir plus en détail surtout la hauteur

        int widthGap = panelMove.getWidth() + (Preferences.JPANEL_HGAP *4) ;
        Dimension newDimension = super.resizeContent();
        panelTable.setPreferredSize(
                new Dimension((int) newDimension.getWidth() -widthGap, 200));
        panelTable.setSize(
                new Dimension((int) newDimension.getWidth() -widthGap, 200));
        return newDimension;
    }

    protected MElement getMElementSelected() {
        int posSelected = table.getSelectedRow();
        if (posSelected >= 0) {
            int idElementSelected = (int) table.getValueAt(posSelected, STableService.IDINDEX);

            MElement mElementSelected = (MElement) ProjectService.getProjectElementById(idElementSelected);
            return mElementSelected;
        }
        return null;
    }




}
