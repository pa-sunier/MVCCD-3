package utilities.files;

import exceptions.CodeApplException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileRead {

    public static String readToString(String dir, String fileName) {
        Path path = Paths.get(dir, fileName);
        return readToString(path);
    }


    public static String readToString(String  filePath) {
        Path path = Paths.get(filePath);
        return readToString(path);
    }

    public static String readToString(File file) {
        Path path = file.toPath();
        return readToString(path);
    }

    // A voir la version dans UtilFile
    public static String readToString(Path path) {
        String content = "";

        try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (content != "") {
                    content += System.lineSeparator();
                }
                content += line;
            }
            bufferedReader.close();
        } catch (IOException e) {
            throw new CodeApplException("Erreur de lecture du fichier" + path.toString());
        }

        return content;
    }
}
