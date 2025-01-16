package main.window.menu;

import console.ViewLogsManager;
import console.WarningLevel;
import main.MVCCDManager;
import main.MVCCDWindow;
import messages.MessagesBuilder;
import preferences.Preferences;
import preferences.PreferencesManager;
import project.Project;
import repository.editingTreat.ProjectEditingTreat;
import utilities.window.DialogMessage;
import window.editor.diagrammer.drawpanel.DrawPanel;
import window.editor.diagrammer.services.DiagrammerService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WinMenuContent implements ActionListener {

  private final MVCCDWindow mvccdWindow;
  private final JMenuBar menuBar;
  private final JMenu fichier;
  private final JMenu project;
  private final JMenu edit;
  private JMenu profile;
  private final JMenu help;

  private final JMenuItem exportPNG;
  private final JMenuItem exportSVG;

  private final JMenuItem projectNew;
  private final JMenuItem projectEdit;
  private final JMenuItem projectOpen;
  private JMenu projectOpenRecents;
  private JMenuItem[] projectOpenRecentsItems;


  private final JMenuItem projectClose;
  private final JMenuItem projectSave;
  private final JMenuItem projectSaveAs;
    /*
    private JMenu projectPreferences;
    private JMenuItem projectPreferencesDeveloper;
    private JMenuItem projectPreferencesGeneral;
    private JMenuItem projectPreferencesMCD;
    private JMenuItem projectProfile;

    private JMenuItem profileCreate;
    */


  public WinMenuContent(MVCCDWindow mvccdWindow, JMenuBar menuBar) {
    this.mvccdWindow = mvccdWindow;
    this.menuBar = menuBar;

    fichier = new JMenu("Fichier");
    menuBar.add(fichier);
    project = new JMenu("Projet");
    menuBar.add(project);
    edit = new JMenu("Edition");
    menuBar.add(edit);
        /*
        profile = new JMenu("Profil");
        menuBar.add(profile);
        */

    help = new JMenu("Aide");
    menuBar.add(help);

    exportPNG = new JMenuItem("Exporter au format PNG");
    exportPNG.addActionListener(this);
    fichier.add(exportPNG);
    exportSVG = new JMenuItem("Exporter au format SVG");
    exportSVG.addActionListener(this);
    fichier.add(exportSVG);

    projectNew = new JMenuItem("Nouveau");
    projectNew.addActionListener(this);
    project.add(projectNew);
    projectEdit = new JMenuItem("Propriétés");
    projectEdit.addActionListener(this);
    projectEdit.setEnabled(false);
    project.add(projectEdit);
    projectOpen = new JMenuItem("Ouvrir...");
    projectOpen.addActionListener(this);
    project.add(projectOpen);
    projectOpenRecents = new JMenu("Ouvrir récents");
    projectOpenRecents.addActionListener(this);
    project.add(projectOpenRecents);

    projectOpenRecentsItems = new JMenuItem[Preferences.FILES_RECENTS_AUTHORIZED];
    for (int i = 1; i <= Preferences.FILES_RECENTS_AUTHORIZED; i++) {
      projectOpenRecentsItems[i - 1] = new JMenuItem();
      projectOpenRecentsItems[i - 1].addActionListener(this);
      projectOpenRecentsItems[i - 1].setVisible(false);
      projectOpenRecents.add(projectOpenRecentsItems[i - 1]);
    }

    projectClose = new JMenuItem("Fermer");
    projectClose.addActionListener(this);
    projectClose.setEnabled(true);
    project.add(projectClose);

    project.addSeparator();
    projectSave = new JMenuItem("Sauver");
    projectSave.addActionListener(this);
    projectSave.setEnabled(false);
    project.add(projectSave);
    projectSaveAs = new JMenuItem("Sauver comme copie");
    projectSaveAs.addActionListener(this);
    projectSaveAs.setEnabled(false);
    project.add(projectSaveAs);

    project.addSeparator();

        /*
        projectPreferences = new JMenu("Préférences");
        projectPreferences.addActionListener(this);
        projectPreferencesDeveloper = new JMenuItem("Développeur");
        projectPreferencesDeveloper.addActionListener(this);
        projectPreferences.add(projectPreferencesDeveloper);
        projectPreferencesGeneral = new JMenuItem("Général");
        projectPreferencesGeneral.addActionListener(this);
        projectPreferences.add(projectPreferencesGeneral);
        projectPreferencesMCD = new JMenuItem("MCD");
        projectPreferencesMCD.addActionListener(this);
        projectPreferences.add(projectPreferencesMCD);
        project.add(projectPreferences);

        projectProfile = new JMenuItem("Profil");
        project.add(projectProfile);

        profileCreate = new JMenuItem("Créer/Modifier");
        profile.add(profileCreate);
*/
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String messageExceptionTarget = "";
    try {
      Object source = e.getSource();
      if (source == exportSVG || source == exportPNG) {
        DrawPanel drawPanel = DiagrammerService.getDrawPanel();
        // Déselectionne toutes les formes
        drawPanel.deselectAllShapes();

        // Ouvre une boîte de dialogue pour que l'utilisateur choisisse l'emplacement de sauvegarde et le nom du fichier
        JFileChooser fileChooser = new JFileChooser();

        int returnVal = fileChooser.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          if (source == exportSVG) {
            //exportToSVG(drawPanel, fileChooser.getSelectedFile().toString());
          } else {
            //exportToPNG(drawPanel, fileChooser.getSelectedFile().toString());
          }

        }
      }
      if (source == projectNew) {
        messageExceptionTarget = MessagesBuilder.getMessagesProperty("project.new.exception");
        newProject();
      }
      if (source == projectEdit) {
        messageExceptionTarget = MessagesBuilder.getMessagesProperty("project.edit.exception");
        ProjectEditingTreat.treatUpdate(mvccdWindow);
      }
      if (source == projectOpen) {
        messageExceptionTarget = MessagesBuilder.getMessagesProperty("project.open.exception");
        openProject();
      }
      for (int i = 0; i < Preferences.FILES_RECENTS_AUTHORIZED; i++) {
        messageExceptionTarget = MessagesBuilder.getMessagesProperty(
            "project.recent.open.exception");
        if (source == projectOpenRecentsItems[i]) {
          openProjectRecent(projectOpenRecentsItems[i].getText());
        }
      }

      if (source == projectClose) {
        messageExceptionTarget = MessagesBuilder.getMessagesProperty("project.close.exception");
        boolean confirmClose = true;
        if (MVCCDManager.instance().isDatasProjectChanged()) {
          String message = MessagesBuilder.getMessagesProperty("project.close.change.not.saved");
          confirmClose =
              DialogMessage.showConfirmYesNo_No(mvccdWindow, message) == JOptionPane.YES_OPTION;
        }
        if (confirmClose) {
          MVCCDManager.instance().closeProject();
        }
      }

      if (source == projectSave) {
        messageExceptionTarget = MessagesBuilder.getMessagesProperty("project.save.exception");
        MVCCDManager.instance().saveProject();
      }
      if (source == projectSaveAs) {
        messageExceptionTarget = MessagesBuilder.getMessagesProperty("project.save.as.exception");
        MVCCDManager.instance().saveAsProject(false);
      }

    } catch (Exception exception) {
      String messageException = MessagesBuilder.getMessagesProperty("bar.menu.exception",
          messageExceptionTarget);
      ViewLogsManager.catchException(exception, messageException);
    }
  }

  /*
  public void exportToSVG(DrawPanel panel, String fileName) {
    // On va récupérer le rectangle autour des formes graphiques dans le diagramme en gardant une bordure de 10 pixels
    Rectangle rectangle = panel.getContentBounds(panel.getShapes(), 10);

    // Si notre diagramme n'est pas vide de formes ...
    if (panel.getWidth() > 0) {
      panel.setSize((int) (rectangle.width + rectangle.getMinX() * 2),
          (int) (rectangle.height + rectangle.getMinY()));
    }

    // Si notre diagramme est vide de formes ...
    else {
      panel.setSize(panel.getWidth(), panel.getHeight());
    }
    // Désactive temporairement la grille de dessin du Diagrammer
    Preferences applicationPref = PreferencesManager.instance().getApplicationPref();
    applicationPref.setDIAGRAMMER_SHOW_GRID(false);
    // Resize technique des ClassShapes afin que le texte ne soit pas rogné par les bordures
    panel.resizeShapesBeforeExport(panel.getClassShapes());

    SVGGraphics2D svgGraphics2D = new SVGGraphics2D(panel.getWidth(), panel.getHeight());

    JFreeChart chart = new JFreeChart(new Plot() {
      @Override
      public String getPlotType() {
        return "Image";
      }

      @Override
      public void draw(Graphics2D graphics2D, Rectangle2D rectangle2D, Point2D point2D,
          PlotState plotState, PlotRenderingInfo plotRenderingInfo) {
        panel.printAll(graphics2D);
      }
    });
    try {
      chart.draw(svgGraphics2D,
          new Rectangle(panel.getWidth(), panel.getHeight()));
      File file = new File(fileName.trim() + ".svg");
      SVGUtils.writeToSVG(file, svgGraphics2D.getSVGElement());
    } catch (IOException exp) {
      exp.printStackTrace();
    } finally {
      panel.resizeShapesAfterExport(panel.getClassShapes());
      applicationPref.setDIAGRAMMER_SHOW_GRID(true);
    }
  }


   */

  public void exportToPNG(DrawPanel panel, String fileName) {
    // On va récupérer le rectangle autour des formes graphiques dans le diagramme en gardant une bordure de 10 pixels
    Rectangle rectangle = panel.getContentBounds(panel.getShapes(), 10);
    BufferedImage bufferedImage;

    // Si notre diagramme n'est pas vide de formes ...
    if (panel.getWidth() > 0) {
      panel.setSize((int) (rectangle.width + rectangle.getMinX() * 2),
          (int) (rectangle.height + rectangle.getMinY()));

      bufferedImage = new BufferedImage(panel.getWidth(),
          panel.getHeight(),
          BufferedImage.TYPE_4BYTE_ABGR_PRE);
    }
    // Si notre diagramme est vide de formes ...
    else {
      bufferedImage = new BufferedImage(panel.getWidth(), panel.getHeight(),
          BufferedImage.TYPE_4BYTE_ABGR_PRE);
    }
    Graphics2D graphics2D = bufferedImage.createGraphics();
    graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    // Désactive temporairement la grille de dessin du Diagrammer
    Preferences applicationPref = PreferencesManager.instance().getApplicationPref();
    applicationPref.setDIAGRAMMER_SHOW_GRID(false);
    // Resize technique des ClassShapes afin que le texte ne soit pas rogné par les bordures
    panel.resizeShapesBeforeExport(panel.getClassShapes());

    panel.printAll(graphics2D);
    graphics2D.dispose();
    try {
      File file = new File(fileName.trim() + ".png");
      ImageIO.write(bufferedImage, "png", file);
    } catch (IOException exp) {
      exp.printStackTrace();
    } finally {
      panel.resizeShapesAfterExport(panel.getClassShapes());
      applicationPref.setDIAGRAMMER_SHOW_GRID(true);
    }
  }



  private void newProject() {
    if (MVCCDManager.instance().getProject() == null) {
      Project project = ProjectEditingTreat.treatNew(mvccdWindow);
      if (project != null) {
        // Quittance de création d'un nouveau projet
        String message = MessagesBuilder.getMessagesProperty("project.new", project.getName());
        ViewLogsManager.printMessage(message, WarningLevel.INFO);
      }
    } else {
      String message = MessagesBuilder.getMessagesProperty("project.new.not.close");
      DialogMessage.showOk(mvccdWindow, message);
    }
  }

  private void openProject() {
    if (MVCCDManager.instance().getProject() == null) {
      MVCCDManager.instance().openProject();
    } else {
      String message = MessagesBuilder.getMessagesProperty("project.open.not.close");
      DialogMessage.showOk(mvccdWindow, message);
    }
  }

  private void openProjectRecent(String fileName) {
    if (MVCCDManager.instance().getProject() == null) {
      MVCCDManager.instance().openProjectRecent(fileName);
    } else {
      String message = MessagesBuilder.getMessagesProperty("project.open.not.close");
      DialogMessage.showOk(mvccdWindow, message);
    }

  }

  public void activateProjectOpenRecentsItem(int i, String text) {
    projectOpenRecentsItems[i].setText(text);
    projectOpenRecentsItems[i].setVisible(true);

    projectOpenRecents.setEnabled(true);
  }

  public void desActivateProjectOpenRecentsItems() {
    for (int i = 0; i < Preferences.FILES_RECENTS_AUTHORIZED; i++) {
      projectOpenRecentsItems[i].setVisible(false);
    }
    projectOpenRecents.setEnabled(false);
  }

  public JMenuItem getProjectEdit() {
    return projectEdit;
  }

  public JMenuItem getProjectSaveAs() {
    return projectSaveAs;

  }

  public JMenuItem getProjectSave() {
    return projectSave;
  }

  public JMenuItem getProjectClose() {
    return projectClose;
  }
}
