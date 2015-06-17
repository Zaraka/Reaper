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

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
    @FXML
    private CheckBox autoConnectCheckBox;
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
    
    @FXML
    private void clearData(ActionEvent event) {
        reaper.getCrawler().dataReset();
    }
    
    @FXML
    private void saveSettings(ActionEvent event){
        reaper.getCrawler().updateDBPref();
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
        this.autoConnectCheckBox.selectedProperty().bindBidirectional(crawler.autoConnectProperty());
        
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
