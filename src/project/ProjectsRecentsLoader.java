package project;

import exceptions.CodeApplException;
import main.MVCCDElement;
import preferences.Preferences;

import java.io.*;

public class ProjectsRecentsLoader {

    public ProjectsRecentsLoader() {
    }


    public ProjectsRecents load() {
        ObjectInputStream reader = null;
        ProjectsRecents projectsRecents = null;
        try {
            File file = new File(Preferences.FILE_FILES_RECENTS_NAME);
            FileInputStream fileInputStream = new FileInputStream(file);
            if (fileInputStream != null) {
                reader = new ObjectInputStream(fileInputStream);
                boolean eof = false;
                while (!eof) {
                    try {
                        projectsRecents = (ProjectsRecents) reader.readObject();
                    } catch (EOFException e) {
                        eof = true;
                    }
                }
            } else {
                // Absence de fichier
                projectsRecents = new ProjectsRecents();
            }
       } catch (Exception e){
            throw (new CodeApplException(e));	// L'erreur est renvoyée
       } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
        return projectsRecents;
    }

}
