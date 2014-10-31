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
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import javafx.util.converter.NumberStringConverter;
import netscape.javascript.JSException;
import reaper.Reaper;
import reaper.model.Domain;
import reaper.model.Link;
import reaper.model.Resource;

/**
 *
 * @author zaraka
 */
public class ReaperController implements Initializable {

    private static final Logger logger = Logger.getLogger(Reaper.class.getName());

    Reaper reaper;

    @FXML
    private TextField hostname;
    @FXML
    private TreeView resourceTree;
    @FXML
    private Label url;
    @FXML
    private TextFlow console;
    @FXML
    private TableView<Link> urlTable;
    @FXML
    private TableColumn<Link, String> urlColumn;
    @FXML
    private WebView sitemap;
    @FXML
    private TextField maxDepth;
    @FXML
    private TextField maxDownloads;
    @FXML
    private ScrollPane consoleScroll;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        Domain data = reaper.getDomain();
        if (!"".equals(hostname.getText())) {
            data.setHostname(hostname.getText());
            data.mine();
            //this.showResource(data.resources().get(0));
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.addHandler(new FlowHandler(this.console));
        logger.log(Level.INFO, "Starting");

        WebEngine engine = sitemap.getEngine();
        engine.setOnError((WebErrorEvent event) -> {
            logger.log(Level.WARNING, "WebError: " + event.getMessage());
        });

        String sitemapURL = Reaper.class.getResource("view/sitemap.html").toExternalForm();
        engine.load(sitemapURL);
        this.console.getChildren().addListener(new ListChangeListener<Node>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends Node> c) {
                consoleScroll.setVvalue(consoleScroll.getVmax());
            }
        });
        engine.setOnError((WebErrorEvent event) -> {
            logger.log(Level.WARNING, event.toString());
        });
        this.enableFirebug();
        
    }

    private void showResourceDetails(Resource res) {
        this.url.setText(res.getPath());
    }

    public void setReaper(Reaper reaper) {
        //bind data
        this.reaper = reaper;
        Domain dom = this.reaper.getDomain();

        this.maxDownloads.textProperty().bindBidirectional(dom.maxDownloadsProperty(), new NumberStringConverter());
        this.maxDepth.textProperty().bindBidirectional(dom.maxDepthProperty(), new NumberStringConverter());

        dom.resources().addListener((ListChangeListener.Change<? extends Resource> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Resource res : change.getAddedSubList()) {
                        addSitemapNode(res);
                    }
                } else if (change.wasRemoved()) {
                    for (Resource res : change.getRemoved()) {
                        removeSitemapNode(res);
                    }
                }
            }
        });

    }

    private void showResource(Resource resource) {
        if (resource != null) {
            url.setText(resource.getPath());
            urlTable.setItems(resource.links());
            urlColumn.setCellValueFactory(cellData -> cellData.getValue().linkProperty());
        } else {
            url.setText("");
        }
    }

    private void addSitemapNode(Resource resource) {
        String script = "addResourceIfNotExists('" + resource.getPath() + "', '" + resource.getType().getGroup() + "');";
        try {
            this.sitemap.getEngine().executeScript(script);
        } catch (JSException ex) {
            logger.log(Level.WARNING, ex.toString());
        }
        for (Link link : resource.links()) {
            if (link.getEdgeFormat() != null) {
                String edge = "addEdgeIfNotExists('" + link.getEdgeFormat() + "', '"+link.getFromResource().getPath()+"', '"+link.getToResource().getPath()+"');";
                try {
                    this.sitemap.getEngine().executeScript(edge);
                } catch (JSException ex) {
                    logger.log(Level.WARNING, ex.toString());
                }
            }
        }
    }

    private void removeSitemapNode(Resource resource) {
        String script = "removeResource('" + resource.getPath() + "');";
        try {
            this.sitemap.getEngine().executeScript(script);
        } catch (JSException ex) {
            logger.log(Level.WARNING, ex.toString());
        }
        for (Link link : resource.links()) {
            String edge = "removeEdge('" + link.getEdgeFormat() + "');";
            try {
                this.sitemap.getEngine().executeScript(edge);
            } catch (JSException ex) {
                logger.log(Level.WARNING, ex.toString());
            }
        }
    }

    private void enableFirebug(){
        this.sitemap.getEngine().executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
    }
}
