package connections.services;

import connections.*;
import exceptions.CodeApplException;
import preferences.Preferences;
import resultat.Resultat;

import java.awt.*;
import java.io.File;
import java.sql.Connection;

public class ConConnectionService {

    public static String getLienProg(ConDB conDB, String name) {
        return conDB.getLienProg() + Preferences.LIEN_PROG_SEP + name;
    }

    public static File getDriverFileToUse(ConConnection conConnection) {
        return getDriverFileToUse(conConnection.getConDB(),
                conConnection.isDriverDefault(),
                conConnection.getDriverFileCustom());
    }

    public static File getDriverFileToUse(ConDB conDB,
                                          boolean driverDefault,
                                          String driverFileCustom) {
        if (driverDefault) {
            return new File(ConManager.getDefaultDriverFileNamePathAbsolute(conDB));
        } else
            return new File(driverFileCustom);
    }



    public static Resultat actionTestConnection(Window owner,
                                                boolean autonomous,
                                                ConDB conDB,
                                                String hostName,
                                                String port,
                                                String dbName,
                                                boolean driverDefault,
                                                String driverFileCustom,
                                                String userName,
                                                String userPW,
                                                ConIDDBName conIDDBName,
                                                Connection connection) {
        ConConnection conConnection = null;
        if (conDB == ConDB.ORACLE) {
            conConnection = new ConConnectionOracle(null);
        } else{
            throw new CodeApplException("Il faut ajouter la création de la connexion pour " + conDB.getText());
        }
        conConnection.setHostName(hostName);
        conConnection.setPort(port);
        conConnection.setDbName(dbName);
        conConnection.setDriverDefault(driverDefault);
        conConnection.setDriverFileCustom(driverFileCustom);
        conConnection.setUserName(userName);
        conConnection.setUserPW(userPW);
        conConnection.setConIDDBName(conIDDBName);

        return ConnectionsService.actionTestIConConnectionOrConnector(owner,
                autonomous,
                conConnection,
                connection);
    }

}