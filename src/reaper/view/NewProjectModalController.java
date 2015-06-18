/*
 * The MIT License
 *
 * Copyright 2015 Reaper.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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

    private ObservableList<String> blacklist;
    private ObservableList<String> whitelist;
    private URL domain;
    private boolean modalAccepted;

    @FXML
    private TextField hostname;
    @FXML
    private TextField name;
    @FXML
    private TextField depth;
    @FXML
    private TableView<String> blacklistTable;
    @FXML
    private TableView<String> whitelistTable;
    @FXML
    private TableColumn<String, String> blacklistColumn;
    @FXML
    private TableColumn<String, String> whitelistColumn;

    @FXML
    private void addBlacklistItem() {
        TextInputDialog dialog = new TextInputDialog("http://subdomain.example.com");
        dialog.setTitle("Add whitelist rule");
        dialog.setHeaderText("Add whitelist regex");
        dialog.setContentText("Please enter valid regex");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(domain -> {
            String url = domain;
            blacklist.add(url);

        });
    }

    @FXML
    private void addWhitelistItem() {
        TextInputDialog dialog = new TextInputDialog("http://subdomain.example.com");
        dialog.setTitle("Add whitelist rule");
        dialog.setHeaderText("Add whitelist regex");
        dialog.setContentText("Please enter valid regex");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(domain -> {
            String url = domain;
            whitelist.add(url);
        });
    }

    @FXML
    private void createNewProject(ActionEvent event) {
        if (depth.getText().isEmpty()) {
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

        if (!name.getText().matches("^[a-zA-Z0-9]*$")) {
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
        blacklistColumn.setCellValueFactory((TableColumn.CellDataFeatures<String, String> cellData) -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        //Context menu
        MenuItem removeBlacklistItem = new MenuItem("Remove");
        removeBlacklistItem.setOnAction((ActionEvent event) -> {
            blacklist.remove(blacklistTable.getSelectionModel().getSelectedIndex());
        });
        MenuItem addBlacklistMenuItem = new MenuItem("Add new");
        addBlacklistMenuItem.setOnAction((ActionEvent event) -> {
            addBlacklistItem();
        });
        blacklistTable.setContextMenu(new ContextMenu(
                addBlacklistMenuItem,
                removeBlacklistItem
        ));

        whitelistTable.setItems(whitelist);
        whitelistColumn.setCellValueFactory((TableColumn.CellDataFeatures<String, String> cellData) -> new ReadOnlyObjectWrapper<>(cellData.getValue()));

        //Context menu
        MenuItem removeWhitelistItem = new MenuItem("Remove");
        removeWhitelistItem.setOnAction((ActionEvent event) -> {
            whitelist.remove(whitelistTable.getSelectionModel().getSelectedIndex());
        });
        MenuItem addWhitelistMenuItem = new MenuItem("Add new");
        addWhitelistMenuItem.setOnAction((ActionEvent event) -> {
            addWhitelistItem();
        });
        whitelistTable.setContextMenu(new ContextMenu(
                addWhitelistMenuItem,
                removeWhitelistItem
        ));

        depth.setTextFormatter(new TextFormatter<>(new NumberStringConverter(NumberFormat.getIntegerInstance())));
    }

    public URL getDomain() {
        return domain;
    }

    public String getName() {
        return name.getText();
    }

    public ArrayList<String> getBlacklist() {
        return new ArrayList<>(blacklist);
    }

    public ArrayList<String> getWhitelist() {
        return new ArrayList<>(whitelist);
    }

    public boolean getAccepted() {
        return modalAccepted;
    }

    public int getDepth() {
        return Integer.valueOf(depth.getText());
    }
}
