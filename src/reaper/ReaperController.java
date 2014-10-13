/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package reaper;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;

/**
 *
 * @author zaraka
 */
public class ReaperController implements Initializable {
    
    @FXML private TextField domain;
    @FXML private TreeView resourceTree;
    @FXML private Label url;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        domain.getText();
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
