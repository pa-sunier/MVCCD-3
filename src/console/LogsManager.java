package console;

import exceptions.TransformMCDException;
import messages.MessagesBuilder;
import preferences.Preferences;
import preferences.PreferencesManager;
import utilities.Trace;
import utilities.files.UtilFiles;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogsManager {


    static private boolean textAddedToLog = false;

    static private boolean fileError = false;  //Vrai  dès qu'une erreur survient dans la session

    static private String fileErrorMessage; // Message d'erreur reçu de gestionnaire de fichier

    /**
     * It contains the value of the enumeration WarningLevel which has assLink be used assLink determinate the level of log assLink use.
     */
    static public WarningLevel warningLevel;


    /**
     * Say if a text has been added into the logs.
     */
    static public boolean isTextAddedToLog() {
        return textAddedToLog;
    }


    /**
     * The method log the text into file.
     * The log will only be done if the warningLevel you give in parameters is more important
     * or has the same importance than the value set assLink the property PLUGIN_LOGGING_WARNINGLEVEL.
     * The log is written into a file present at the path specified by PLUGIN_LOGGING_FOLDER_PATH,
     * and the file name is given by PLUGIN_LOGGING_FILE_NAME_FORMAT.
     * @param text
     * @param warningLevel
     */


    static private void logText(String text, boolean newText , WarningLevel warningLevel){
        if (!fileError){
            Trace.println("Ecriture de log");

            WarningLevelManager wlm = WarningLevelManager.instance();

            WarningLevel prefWarningLevel = PreferencesManager.instance().preferences().getWARNING_LEVEL();

            if(warningLevel == null || wlm.OneIsAsImportantAsSecond(warningLevel,prefWarningLevel)){
                FilesManager fm = FilesManager.instance();
                try{
                    //String pluginId = FilePropertiesPluginVPManager.instance().getPluginProperty("plugin.id");
                    //String pluginDir = ApplicationManager.instance().getPluginInfo(pluginId).getPluginDir().toString();
                    String fileName = getLogFileName();
                    if (newText) {
                        String separator = "============================================================" + System.lineSeparator();
                        String datation = new SimpleDateFormat("HH:mm:ss - ").format(new Date());
                        text = separator + datation + text;
                    }
                    fm.addLineToFile(Preferences.DIRECTORY_LOGGING_NAME + File.separator, fileName, text);
                    textAddedToLog = true;
                }catch(TransformMCDException e){
                    throw e;
                } catch (Throwable e){
                    fileError = true;
                    String message = e.getMessage();
                    if (message == null){
                       message = e.toString();
                    }
                    fileErrorMessage = message ;
                    noLogFileAvailable(e);
                }
            }
        }
    }

    static public void newText(String text, WarningLevel warningLevel){
        logText(text, true, warningLevel);
    }

    static public void continueText(String text, WarningLevel warningLevel){
        logText(text, false, warningLevel);
    }


    /**
     * Log a text into file.
     * It doesn't worry to the warning level.
     */

	/*
	static public void logTextBloc(String text){
		logTextBloc(text, null);
	} */


    /**
     * Return the file name where logs is written assLink.
     */
    static private String getLogFileName(){
        if(Preferences.LOGGING_FILE_NAME_FORMAT != null){
            String shortNameFile = (new SimpleDateFormat(Preferences.LOGGING_FILE_NAME_FORMAT)).format(new Date());
            String extFile = Preferences.LOGGING_FILE_NAME_EXTENSION;
            String nameFile = UtilFiles.fileName(shortNameFile, extFile);
            return nameFile;
        }else{
            String msg = MessagesBuilder.getMessagesProperty(Preferences.LOGGING_FILE_NAME_FORMAT_ERROR_NULL);
            throw new TransformMCDException(msg);
        }
    }


    /**
     *
     * @return
     */
    static public String getlogFilePath(){
        if(Preferences.LOGGING_FOLDER_PATH != null){
            return UtilFiles.filePath(Preferences.DIRECTORY_LOGGING_NAME, getLogFileName());
       }else{
            String msg = MessagesBuilder.getMessagesProperty("plugin.logging.folder.path.null");
            throw new TransformMCDException(msg);
        }
    }

    static public void noLogFileAvailable(Throwable e) {
        if (fileError) {
            // Comme l'erreur est lié au fichier de log, il nous faut envoyer les messages directement sur la console
            // la classe MessageManager ne peut pas être utilisée.
            ViewManager.showTextLine("****  Traçabilité impossible  ****");
            // Message d'erreur système
            ViewManager.showTextLine("Attention:  " + fileErrorMessage);
            // Message d'info de démarrage de VP en mode administrateur
            ViewManager.showTextLine("Cette erreur empêche MVC-CD de tracer son activité dans son fichier de log");
            ViewManager.showTextLine("Si l'erreur est liée à un problème d'accès aux ressources, il faut alors vous assurer de lancer MVC-CD davec les privilèges d'administrateur");
            ViewManager.showTextLine("---------------------------------");
            //TODO-1 A voir aussi le manque de place et autres...
            // Affinier le traitement pour donner une indication précise de l'erreur

            // Affichage de la pile d'erreur dans la console
            Console.printStackTrace(e);
        }
    }

}
