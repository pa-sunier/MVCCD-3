package console;


import resultat.Resultat;
import resultat.ResultatElement;
import resultat.ResultatLevel;

/**
 * This class have to be used when you want to tell something to user right now.
 * If you want to declare a message to user but you want the plug-in to wait the
 * end of process before showing the message (this is the classic way), you must
 * not use this class, but use class "Messages". The message you send will be
 * written in log files or in VP console, depending of the WarningLevel you set.
 * It's a Singleton so it exist only one instance of its.
 *
 * @author Steve Berberat
 *
 */

public class ViewLogsManager {


    static public void newText(String text, WarningLevel warningLevel){
        Console.clearMessages();
        ViewManager.showTextLine(text, warningLevel);
        LogsManager.newText(text, warningLevel);
    }


    /**
     * Print a message to the user through the console or/and the log file.
     * Print only in one line.
     * @param text
     * @param warningLevel
     */
    static public void continueText(String text, WarningLevel warningLevel){
        ViewManager.showTextLine(text, warningLevel);
        LogsManager.continueText(text, warningLevel);
    }


    public static void resultat(Resultat resultat, WarningLevel warningLevel) {
        int i = 0 ;
        for (ResultatElement resultatElement : resultat.getElementsAllLevel()){
            String text = resultatElement.getText();
            if (i == 0){
                ViewLogsManager.newText(text, warningLevel);
            }else {
                ViewLogsManager.continueText(text, warningLevel);
            }
            i++ ;
        }
    }
}