package repository.editingTreat.mpdr;

import console.ViewLogsManager;
import console.WarningLevel;
import consolidationMpdrDb.viewer.WaitingSyncViewer;
import generatorsql.viewer.SQLViewer;
import main.MVCCDElement;
import main.MVCCDWindow;
import messages.MessagesBuilder;
import mpdr.MPDRModel;
import repository.editingTreat.EditingTreat;
import treatment.services.TreatmentService;
import utilities.window.editor.DialogEditor;
import utilities.window.editor.PanelInputContent;
import window.editor.mdr.mpdr.model.MPDRModelEditor;

import java.awt.*;

public class MPDRModelEditingTreat extends EditingTreat {


    @Override
    protected PanelInputContent getPanelInputContent(MVCCDElement element) {

        return null;
    }

    @Override
    protected DialogEditor getDialogEditor(Window owner, MVCCDElement parent, MVCCDElement element, String mode) {
        return new MPDRModelEditor(owner, parent, (MPDRModel) element, mode,
            new MPDRModelEditingTreat());
    }

    @Override
    protected String getPropertyTheElement() {
        return "the.model.physical";
    }

    public boolean treatGenerate(Window owner, MVCCDElement mvccdElement) {
        MPDRModel mpdrModel = (MPDRModel) mvccdElement;

        boolean ok = true;
        String generateSQLCode = "";
        try {
            String message = MessagesBuilder.getMessagesProperty("generate.sql.mpdrtosql.start",
                    new String[]{mpdrModel.getNamePath()});
            ViewLogsManager.printMessage(message, WarningLevel.INFO);

            generateSQLCode = mpdrModel.treatGenerate();

        } catch (Exception e){
            ViewLogsManager.catchException(e, owner, "La génération de code SQL-DDL a échoué.");
            ok = false ;
        }

        TreatmentService.treatmentFinish(owner, mvccdElement, ok,
                getPropertyTheElement(), "generate.sql.mpdrtosql.ok", "generate.sql.mpdrtosql.abort") ;
        if (ok) {
            SQLViewer fen = new SQLViewer(mpdrModel, generateSQLCode);
            fen.setVisible(true);
        }
        return ok ;
   }

   //Ajouté par Vincent
    public boolean treatSyncMpdrDb(MVCCDWindow owner, MVCCDElement mvccdElement, WaitingSyncViewer waitingSyncViewer) {
        MPDRModel mpdrModel = (MPDRModel) mvccdElement;

        boolean ok = true;
        String syncSQLCode = "";
        try {
            String message = MessagesBuilder.getMessagesProperty("consolidation.sql.mpdrtosql.start",
                    new String[]{mpdrModel.getNamePath()});
            ViewLogsManager.printMessage(message, WarningLevel.INFO);

            syncSQLCode = mpdrModel.treatSync(owner);
            waitingSyncViewer.setVisible(false);

        } catch (Exception e){
            ViewLogsManager.catchException(e, owner, "consolidation.sql.mpdrtosql.error");
            ok = false ;
        }
        TreatmentService.treatmentFinish(owner, mvccdElement, ok,
                getPropertyTheElement(), "consolidation.sql.mpdrtosql.ok", "consolidation.sql.mpdrtosql.abort") ;
        if (ok) {
            SQLViewer fen = new SQLViewer(mpdrModel, syncSQLCode);
            fen.setVisible(true);
        }
        return ok ;
    }
}
