package main;

import com.formdev.flatlaf.FlatLightLaf;
import console.ViewLogsManager;
import messages.MessagesBuilder;

import javax.swing.*;
import java.io.Console;

public class MVCCD {

  /**
   * Démarre MVC-CD-3
   */
  public static void main(String[] args) {
    Console console;

    try {
            /*
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MVCCDManager.instance().start();
                }
            });

            */
      try {
        UIManager.setLookAndFeel( new FlatLightLaf() );
      } catch( Exception ex ) {
        System.err.println( "Failed to initialize LaF" );
      }
      MVCCDManager.instance().start();

    } catch (Exception e) {
      if (MVCCDManager.instance().getConsoleManager() != null) {
        String message = MessagesBuilder.getMessagesProperty("main.finish.error");
        ViewLogsManager.catchException(e, MVCCDManager.instance().getMvccdWindow(), message);
      } else {
        e.printStackTrace();
      }
    }
  }
}