package reaper.view;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import reaper.Reaper;
import reaper.exceptions.DatabaseNotConnectedException;
import reaper.model.Crawler;

/**
 * FXML Controller class
 *
 * @author zaraka
 */
public class SettingsController implements Initializable {
    
    private static final Logger loggerReaper = Logger.getLogger(Reaper.class.getName());
    
    Reaper reaper;
    
    //-------------DATABASE TAB---------------
    @FXML
    private TextField databaseUser;
    @FXML
    private TextField databaseHost;
    @FXML
    private PasswordField databasePassword;
    @FXML
    private Button setupDatabaseButton;
    @FXML
    private Button changeDatabaseButton;
    @FXML
    private Button deleteDatabaseButton;
    @FXML
    private Button truncateTablesButton;
    //-------------SETTINGS TAB---------------
    @FXML
    private TextField galleryPathField;
    @FXML
    private TextField phantomPathField;
    
    
    @FXML
    public void setupDatabase() {
        try {
            reaper.getCrawler().setupDatabase();
        } catch (DatabaseNotConnectedException ex) {
            loggerReaper.log(Level.SEVERE, "Database isn't connected");
        }
    }

    @FXML
    public void teardownDatabase() {
        try {
            reaper.getCrawler().removeDatabase();
        } catch (DatabaseNotConnectedException ex) {
            loggerReaper.log(Level.SEVERE, "Database isn't connected");
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    
    public void setReaper(Reaper reaper){
        this.reaper = reaper;
        Crawler crawler = reaper.getCrawler();
        
        this.databaseHost.textProperty().bindBidirectional(crawler.dbHost());
        this.databasePassword.textProperty().bindBidirectional(crawler.dbPassword());
        this.databaseUser.textProperty().bindBidirectional(crawler.dbUser());
        
        this.phantomPathField.textProperty().bindBidirectional(crawler.phantomPathProperty());
        this.galleryPathField.textProperty().bindBidirectional(crawler.galleryPathProperty());
    }
    
    private void lockUnlockControls(boolean value) {
        databaseHost.setDisable(value);
        databasePassword.setDisable(value);
        databaseUser.setDisable(value);
        changeDatabaseButton.setDisable(value);
        setupDatabaseButton.setDisable(value);
        deleteDatabaseButton.setDisable(value);
        truncateTablesButton.setDisable(value);
    }
    
}
