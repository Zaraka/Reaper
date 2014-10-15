/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package reaper.view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.text.TextFlow;
import reaper.model.Resource;
import reaper.Reaper;
import reaper.model.Domain;

/**
 *
 * @author zaraka
 */
public class ReaperController implements Initializable {
    
    Reaper reaper;
    
    @FXML private TextField domain;
    @FXML private TreeView resourceTree;
    @FXML private Label url;
    @FXML private TextFlow console;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        Domain data = reaper.getDomain();
        
        data.setHostname(domain.getText());
        data.mine();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    private void showResourceDetails(Resource res){
        this.url.setText(res.getPath());
    }
    
    public void setReaper(Reaper reaper){
        this.reaper = reaper;
    }
    
}
