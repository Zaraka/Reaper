package reaper.view;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import reaper.Reaper;

/**
 * FXML Controller class
 *
 * @author nikita.vanku
 */
public class NewProjectModalController implements Initializable {
    
    private static final Logger logger = Logger.getLogger(Reaper.class.getName());
    
    private ArrayList<URL> blacklist;
    
    @FXML
    private TextField hostname;
    @FXML
    private TableView<URL> blacklistTable;
    @FXML
    private TableColumn<URL, String> blacklistColumn;
    
    @FXML
    private void addBlacklistItem(){
         TextInputDialog dialog = new TextInputDialog("http://subdomain.example.com");
        dialog.setTitle("Add blacklisted domain");
        dialog.setHeaderText("Add blacklisted domain");
        dialog.setContentText("Please enter domain you want to blacklist");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(domain -> {
            String url = domain;
            if (!url.matches("^.*:\\/\\/.*$")) {
                url = "http://" + url;
                logger.log(Level.WARNING, "You should provide protocol as well. Default proctol http is used.");
            }
            try {
                blacklist.add(new URL(url));
            } catch (MalformedURLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });
    }
    
    @FXML
    private void createNewProject(ActionEvent event){
        if(hostname.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error!");
            alert.setHeaderText(null);
            alert.setContentText("Hostname cannot be empty");
            return;
        }
        
        Stage stage = (Stage) hostname.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void cancelModal(ActionEvent event){
        Stage stage = (Stage) hostname.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public String getHostname(){
        return hostname.getText();
    }
    
    public ArrayList<URL> getBlacklist(){
        return this.blacklist;
    }
    
}
