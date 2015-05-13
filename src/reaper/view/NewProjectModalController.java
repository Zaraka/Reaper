package reaper.view;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import reaper.Reaper;

/**
 * FXML Controller class
 *
 * @author nikita.vanku
 */
public class NewProjectModalController implements Initializable {

    private static final Logger logger = Logger.getLogger(Reaper.class.getName());

    private ObservableList<URL> blacklist;
    private ObservableList<URL> whitelist;
    private URL domain;
    private boolean modalAccepted;

    @FXML
    private TextField hostname;
    @FXML
    private TextField name;
    @FXML
    private TextField depth;
    @FXML
    private TableView<URL> blacklistTable;
    @FXML
    private TableView<URL> whitelistTable;
    @FXML
    private TableColumn<URL, String> blacklistColumn;
    @FXML
    private TableColumn<URL, String> whitelistColumn;

    @FXML
    private void addBlacklistItem() {
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
    private void createNewProject(ActionEvent event) {
        if(depth.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error!");
            alert.setHeaderText("Depth canot be empty");
            alert.setContentText(null);
            alert.show();
            return;
        }
        
        if (hostname.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error!");
            alert.setHeaderText("Domain cannot be empty");
            alert.setContentText(null);
            alert.show();
            return;
        }
        
        if(!name.getText().matches("^[a-zA-Z0-9]*$")){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error!");
            alert.setHeaderText("Name can contain only alfanumerical characters");
            alert.setContentText(null);
            alert.show();
            return;
        }

        try {
            this.domain = new URL(hostname.getText());
        } catch (MalformedURLException ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error!");
            alert.setHeaderText("Cant create url from " + hostname.getText());
            alert.setContentText(ex.getMessage());
            alert.show();
            return;
        }

        if (name.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error!");
            alert.setHeaderText("Name cannot be empty");
            alert.setContentText(null);
            alert.show();
            return;
        }
        
        modalAccepted = true;
        Stage stage = (Stage) hostname.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void cancelModal(ActionEvent event) {
        modalAccepted = false;
        Stage stage = (Stage) hostname.getScene().getWindow();
        stage.close();
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        blacklist = FXCollections.observableArrayList();
        whitelist = FXCollections.observableArrayList();
        modalAccepted = false;

        blacklistTable.setItems(blacklist);
        blacklistColumn.setCellValueFactory((TableColumn.CellDataFeatures<URL, String> cellData) -> new ReadOnlyObjectWrapper<>(cellData.getValue().toString()));
        ContextMenu blacklistMenu = new ContextMenu();
        MenuItem removeBlacklistItem = new MenuItem("Remove");
        removeBlacklistItem.setOnAction((ActionEvent event) -> {
            blacklist.remove(blacklistTable.getSelectionModel().getSelectedIndex());
        });
        blacklistMenu.getItems().add(removeBlacklistItem);
        blacklistTable.setContextMenu(blacklistMenu);
        
        whitelistTable.setItems(whitelist);
        whitelistColumn.setCellValueFactory((TableColumn.CellDataFeatures<URL, String> cellData) -> new ReadOnlyObjectWrapper<>(cellData.getValue().toString()));
        ContextMenu whitelistMenu = new ContextMenu();
        MenuItem removeWhitelistItem = new MenuItem("Remove");
        removeBlacklistItem.setOnAction((ActionEvent event) -> {
            whitelist.remove(whitelistTable.getSelectionModel().getSelectedIndex());
        });
        whitelistMenu.getItems().add(removeWhitelistItem);
        whitelistTable.setContextMenu(whitelistMenu);
        
        depth.setTextFormatter(new TextFormatter<>(new NumberStringConverter(NumberFormat.getIntegerInstance())));
    }   

    public URL getDomain() {
        return domain;
    }

    public String getName() {
        return name.getText();
    }

    public ArrayList<URL> getBlacklist() {
        return new ArrayList<>(blacklist);
    }
    
    public ArrayList<URL> getWhitelist() {
        return new ArrayList<>(whitelist);
    }
    
    public boolean getAccepted(){
        return modalAccepted;
    }
    
    public int getDepth(){
        return Integer.valueOf(depth.getText());
    }
}
