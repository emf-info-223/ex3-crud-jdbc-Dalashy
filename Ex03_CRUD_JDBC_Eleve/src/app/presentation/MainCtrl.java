package app.presentation;

import app.beans.Personne;
import app.exceptions.MyDBException;
import app.helpers.DateTimeLib;
import app.helpers.JfxPopup;
import app.workers.DbWorker;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import java.io.File;
import app.workers.DbWorkerItf;
import app.workers.PersonneManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 *
 * @author PA/STT
 */
public class MainCtrl implements Initializable {

    // DBs à tester
    private enum TypesDB {
        MYSQL, HSQLDB, ACCESS
    };

    // DB par défaut
    final static private TypesDB DB_TYPE = TypesDB.MYSQL;

    private DbWorkerItf dbWrk;
    private boolean modeAjout;
    private PersonneManager manPers;

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtPK;
    @FXML
    private TextField txtNo;
    @FXML
    private TextField txtRue;
    @FXML
    private TextField txtNPA;
    @FXML
    private TextField txtLocalite;
    @FXML
    private TextField txtSalaire;
    @FXML
    private CheckBox ckbActif;
    @FXML
    private Button btnDebut;
    @FXML
    private Button btnPrevious;
    @FXML
    private Button btnNext;
    @FXML
    private Button btnEnd;
    @FXML
    private Button btnSauver;
    @FXML
    private Button btnAnnuler;
    @FXML
    private DatePicker dateNaissance;

    /*
   * METHODES NECESSAIRES A LA VUE
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbWrk = new DbWorker();
        manPers = new PersonneManager();
        ouvrirDB();
        rendreVisibleBoutonsDepl(true);
    }

    @FXML
    public void actionPrevious(ActionEvent event) {
        afficherPersonne(manPers.precedentPersonne());
        rendreVisibleBoutonsDepl(true);
    }

    @FXML
    public void actionNext(ActionEvent event) {
        afficherPersonne(manPers.suivantPersonne());
        rendreVisibleBoutonsDepl(true);
    }

    @FXML
    private void actionEnd(ActionEvent event) {
        afficherPersonne(manPers.finPersonne());
        rendreVisibleBoutonsDepl(true);
    }

    @FXML
    private void debut(ActionEvent event) {
        afficherPersonne(manPers.debutPersonne());
        rendreVisibleBoutonsDepl(true);
    }

    @FXML
    private void menuAjouter(ActionEvent event) {
        rendreVisibleBoutonsDepl(false);
        effacerContenuChamps();
        txtPK.setText("-1");
        modeAjout = true;
    }

    @FXML
    private void menuModifier(ActionEvent event) {
        rendreVisibleBoutonsDepl(false);
        modeAjout = false;
    }

    @FXML
    private void menuEffacer(ActionEvent event) {
        Personne pe;
        try {
            dbWrk.effacer(manPers.courantPersonne());
            pe = manPers.setPersonnes(dbWrk.lirePersonnes());
            afficherPersonne(pe);
        } catch (MyDBException o) {
            JfxPopup.displayError("ERREUR", "Une erreur s'est produite", o.getMessage());
        }
    }

    @FXML
    private void menuQuitter(ActionEvent event) {
        quitter();
    }

    @FXML
    private void annulerPersonne(ActionEvent event) {
        Personne pe;
        try {
            pe = manPers.setPersonnes(dbWrk.lirePersonnes());
            afficherPersonne(pe);
            rendreVisibleBoutonsDepl(true);
        } catch (MyDBException ex) {
            JfxPopup.displayError("ERREUR", "Une erreur s'est produite", ex.getMessage());
        }
    }

    @FXML
    private void sauverPersonne(ActionEvent event) {
        try {
            Personne p = new Personne(Integer.parseInt(txtPK.getText()), txtNom.getText(), txtPrenom.getText(), DateTimeLib.localDateToDate(dateNaissance.getValue()),
                    Integer.parseInt(txtNo.getText()), txtRue.getText(), Integer.parseInt(txtNPA.getText()), txtLocalite.getText(), ckbActif.isSelected(),
                    Double.parseDouble(txtSalaire.getText()), DateTimeLib.getNow());
            if (modeAjout) {
                dbWrk.creer(p);
            } else {
                dbWrk.modifier(p);
            }
            Personne pe = manPers.setPersonnes(dbWrk.lirePersonnes());
            afficherPersonne(pe);
            rendreVisibleBoutonsDepl(true);
        } catch (MyDBException o) {
            JfxPopup.displayError("ERREUR", "Une erreur s'est produite", o.getMessage());
        }
    }

    public void quitter() {
        try {
            dbWrk.deconnecter(); // ne pas oublier !!!
        } catch (MyDBException ex) {
            System.out.println(ex.getMessage());
        }
        Platform.exit();
    }

    /*
   * METHODES PRIVEES 
     */
    private void afficherPersonne(Personne p) {
        if (p != null) {
            txtPrenom.setText(p.getPrenom());
            txtNom.setText(p.getNom());
            txtPK.setText(p.getPkPers() + "");
            dateNaissance.setValue(DateTimeLib.dateToLocalDate(p.getDateNaissance()));
            txtNo.setText(p.getNoRue() + "");
            txtRue.setText(p.getRue());
            txtNPA.setText(p.getNpa() + "");
            txtLocalite.setText(p.getLocalite());
            txtSalaire.setText(p.getSalaire() + "");
            ckbActif.setSelected(p.isActif());
        }
    }

    private void ouvrirDB() {
        try {
            switch (DB_TYPE) {
                case MYSQL:
                    dbWrk.connecterBdMySQL("223_personne_1table");
                    break;
                case HSQLDB:
                    dbWrk.connecterBdHSQLDB("../data" + File.separator + "223_personne_1table");
                    break;
                case ACCESS:
                    dbWrk.connecterBdAccess("../data" + File.separator + "223_Personne_1table.accdb");
                    break;
                default:
                    System.out.println("Base de données pas définie");
            }
            System.out.println("------- DB OK ----------");
            Personne pe = manPers.setPersonnes(dbWrk.lirePersonnes());
            afficherPersonne(pe);
        } catch (MyDBException ex) {
            JfxPopup.displayError("ERREUR", "Une erreur s'est produite", ex.getMessage());
            System.exit(1);
        }
    }

    private void rendreVisibleBoutonsDepl(boolean b) {
        btnDebut.setVisible(b);
        btnPrevious.setVisible(b);
        btnNext.setVisible(b);
        btnEnd.setVisible(b);
        btnAnnuler.setVisible(!b);
        btnSauver.setVisible(!b);
    }

    private void effacerContenuChamps() {
        txtNom.setText("");
        txtPrenom.setText("");
        txtPK.setText("");
        txtNo.setText("");
        txtRue.setText("");
        txtNPA.setText("");
        txtLocalite.setText("");
        txtSalaire.setText("");
        dateNaissance.setValue(null);
        ckbActif.setSelected(false);
    }

}
