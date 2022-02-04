package connections.services;

import connections.*;
import connections.interfaces.IConConnectionOrConnector;
import console.ViewLogsManager;
import console.WarningLevel;
import exceptions.CodeApplException;
import main.MVCCDManager;
import messages.MessagesBuilder;
import org.apache.commons.lang.StringUtils;
import preferences.Preferences;
import utilities.UtilDivers;
import utilities.window.DialogMessage;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.sql.Connection;

public class ConnectionsService {

    public static ConElement getConElementByLienProg(String lienProg) {
        for (ConElement conElement : ConManager.instance().getConElements()){
            if (conElement.getLienProg().equals(lienProg)){
                return conElement;
            }
        }
        return null;
    }

    public static ConConnection getConConnectionByLienProg(String lienProg) {
        ConElement conElement = getConElementByLienProg(lienProg);
        if (conElement instanceof ConConnection){
            return (ConConnection)conElement;
        }
        return null;
    }

    public static ConConnector getConConnectorByLienProg(String lienProg) {
        ConElement conElement = getConElementByLienProg(lienProg);
        if (conElement instanceof ConConnector){
            return (ConConnector)conElement;
        }
        return null;
    }

    public static DefaultMutableTreeNode getNodeByLienProg(String lienProg) {

        DefaultMutableTreeNode rootConnexions = MVCCDManager.instance().getRepository().getNodeConnexionsEntry();
        return getNodeByLienProg(rootConnexions, lienProg);
    }

    public static DefaultMutableTreeNode getNodeByLienProg(DefaultMutableTreeNode root, String lienProg) {
        DefaultMutableTreeNode resultat = null;
        if (root.getUserObject() instanceof ConElement) {
            ConElement conElement = (ConElement) root.getUserObject();
            if (conElement.getLienProg().equals(lienProg)) {
                resultat = root;
            }
        }
        // Pas trouvé de correspondance
        if (resultat == null){
            // Sur le noeud conteneur de l'application
            //if (root.getUserObject() instanceof MVCCDElementApplicationConnections) {
                // Sur un noeud d'élément de connexion
                //if (root.getUserObject() instanceof ConElement) {
                    for (int i = 0; i < root.getChildCount(); i++) {
                        if (resultat == null) {
                            resultat = getNodeByLienProg((DefaultMutableTreeNode) root.getChildAt(i), lienProg);
                        }
                    }
                //}
            //}
        }
        return resultat;
    }


    public static String getResourceURL(ConDB conDB, String hostName, String port, ConIDDBName iddbName, String dbName) {
        String url = conDB.getUrlTemplate();
        if (StringUtils.isNotEmpty(hostName)){
            url = UtilDivers.replaceKeyValue(url, Preferences.CON_HOSTNAME_WORD, hostName);
        }
        if (StringUtils.isNotEmpty(port)){
            url = UtilDivers.replaceKeyValue(url, Preferences.CON_PORT_WORD, port);
        }
        if (StringUtils.isNotEmpty(dbName)){
            url = UtilDivers.replaceKeyValue(url, Preferences.CON_DBNAME_WORD, dbName);
        }
        if (iddbName != null){
            if (iddbName != ConIDDBName.NAME_STD) {
                if (iddbName == ConIDDBName.SID) {
                    url = UtilDivers.replaceKeyValue(url, Preferences.CON_DBNAME_ID_MARKER, conDB.getUrlTemplateSIDMarker());
                }
                if (iddbName == ConIDDBName.SERVICE_NAME) {
                    url = UtilDivers.replaceKeyValue(url, Preferences.CON_DBNAME_ID_MARKER, conDB.getUrlTemplateServiceNameMarker());
                }
            }
        }
        return url ;
    }


    public static Connection actionTestIConConnectionOrConnector(Window owner,
                                                               boolean autonomous,
                                                               IConConnectionOrConnector iConConnectionOrConnector) {
        String message="";
        if (autonomous) {
            message = MessagesBuilder.getMessagesProperty("con.connection.start.test");
            ViewLogsManager.clear();
            ViewLogsManager.printMessage(message, WarningLevel.INFO);
        }

        Connection connection = null;
        if (iConConnectionOrConnector != null) {
            connection = ConManager.createConnection(iConConnectionOrConnector);
            if (connection != null) {
                    message = MessagesBuilder.getMessagesProperty("editor.con.connection.btn.test.ok");
            } else {
                    message = MessagesBuilder.getMessagesProperty("editor.con.connection.btn.test.not.ok");
            }
            ViewLogsManager.printMessage(message, WarningLevel.INFO);
        } else {
            //resultat.add(new ResultatElement(messageIConConnectionOrConnectorUnknow , ResultatLevel.INFO));
            throw new CodeApplException("La connexion ou le connecteur sont inexistants");
        }
        if (autonomous ){
            if (connection != null){
                DialogMessage.showOk(owner, message);
            } else {
                DialogMessage.showNotOkErrorConsole(owner, message);
            }
        }
        return connection;
    }



}