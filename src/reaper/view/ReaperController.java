/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package reaper.view;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;
import reaper.model.Resource;
import reaper.Reaper;
import reaper.model.Domain;
import reaper.model.Link;

/**
 *
 * @author zaraka
 */
public class ReaperController implements Initializable {
    
    private static final Logger logger = Logger.getLogger(Reaper.class.getName());
    
    Reaper reaper;
    
    @FXML private TextField hostname;
    @FXML private TreeView resourceTree;
    @FXML private Label url;
    @FXML private TextFlow console;
    @FXML private TableView<Link> urlTable;
    @FXML private TableColumn<Link, String> urlColumn;
    @FXML private WebView sitemap;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        this.addSitemapNode();
        Domain data = reaper.getDomain();
        if(!"".equals(hostname.getText())){
            data.setHostname(hostname.getText());
            data.mine();
            this.showResource(this.reaper.getDomain().getResource().get(0));
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.addHandler(new FlowHandler(this.console));
        logger.log(Level.INFO, "Starting");
        String sitemapURL = Reaper.class.getResource("view/sitemap.html").toExternalForm();
        sitemap.getEngine().load(sitemapURL);
    }    
    
    private void showResourceDetails(Resource res){
        this.url.setText(res.getPath());
    }
    
    public void setReaper(Reaper reaper){
        this.reaper = reaper;
    }
    
    private void showResource(Resource resource){
        if(resource != null){
            url.setText(resource.getPath());
            urlTable.setItems(resource.getLinks());
            urlColumn.setCellValueFactory(cellData -> cellData.getValue().getToPathProperty());
        } else {
            url.setText("");
        }
    }
    
    private void addSitemapNode(){
        String script = "nodes.add([{id:'3', label:'test'}]);";
        this.sitemap.getEngine().executeScript(script);
    }
}
