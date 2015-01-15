package reaper.view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.util.converter.NumberStringConverter;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import reaper.Reaper;
import reaper.model.Domain;
import reaper.model.Link;
import reaper.model.Resource;
import reaper.model.ResourceType;

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
    private TextFlow console;
    @FXML
    private TableView<Resource> resourceTable;
    @FXML
    private TableColumn<Resource, String> resourcePathColumn;
    @FXML
    private TableColumn<Resource, String> resourceURLColumn;
    @FXML
    private TableColumn<Resource, String> resourceMimeTypeColumn;
    @FXML
    private TableColumn<Resource, Number> resourceCodeColumn;
    @FXML
    private TableColumn<Resource, String> resourceTypeColumn;
    @FXML
    private WebView sitemap;
    @FXML
    private TextField maxDepth;
    @FXML
    private TextField maxDownloads;
    @FXML
    private ScrollPane consoleScroll;
    @FXML
    private Tab detailsTab;
    @FXML
    private TabPane tabPane;
    @FXML
    private CheckBox displayGraph;
    @FXML
    private TextField databaseHost;
    @FXML
    private TextField databaseUser;
    @FXML
    private PasswordField databasePassword;
    @FXML
    private StackPane detailsPanel;
    @FXML
    private Label overviewDomainLabel;
    @FXML
    private Label overviewResourceLabel;
    @FXML
    private Label overviewLinksLabel;

    @FXML
    private void startMining(ActionEvent event) {
        if (!"".equals(hostname.getText())) {
            reaper.getDomain().mineStart(hostname.getText());
        } else {
            logger.log(Level.SEVERE, "You need to specify hostname.");
        }
    }

    @FXML
    private void stopMining(ActionEvent event) {
        reaper.getDomain().mineStop();
    }

    @FXML
    private void clearData(ActionEvent event) {
        reaper.getDomain().dataReset();
    }

    @FXML
    private void onChangeDisplayGraph(ActionEvent event) {
        if (displayGraph.isSelected()) {
            for (Resource res : reaper.getDomain().resources().values()) {
                addSitemapNode(res);
            }
            for (Link link : reaper.getDomain().links()) {
                addEdge(link);
            }
        } else {
            try {
                this.sitemap.getEngine().executeScript("resetGraph();");
            } catch (JSException ex) {
                logger.log(Level.WARNING, ex.toString());
            }
        }
    }

    @FXML
    private void overviewGetRoot(ActionEvent event) {
        reaper.getDomain().loadRoot();
    }

    @FXML
    private void overviewLoadAll(ActionEvent event) {
        reaper.getDomain().loadAll();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger.addHandler(new FlowHandler(this.console));

        WebEngine engine = sitemap.getEngine();
        engine.setOnError((WebErrorEvent event) -> {
            logger.log(Level.WARNING, "WebError: " + event.getMessage());
        });

        String sitemapURL = Reaper.class.getResource("view/sitemap.html").toExternalForm();
        engine.load(sitemapURL);
        this.console.getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
            consoleScroll.setVvalue(consoleScroll.getVmax());
        });
        engine.setOnError((WebErrorEvent event) -> {
            logger.log(Level.WARNING, event.toString());
        });

        engine.setOnAlert((WebEvent<String> event) -> {
            logger.log(Level.INFO, event.toString());
        });
    }

    public void setReaper(Reaper reaper) {
        //bind data
        this.reaper = reaper;
        Domain dom = this.reaper.getDomain();

        this.maxDownloads.textProperty().bindBidirectional(dom.maxDownloadsProperty(), new NumberStringConverter());
        this.maxDepth.textProperty().bindBidirectional(dom.maxDepthProperty(), new NumberStringConverter());

        this.databaseHost.textProperty().bindBidirectional(dom.dbHost());
        this.databasePassword.textProperty().bindBidirectional(dom.dbPassword());
        this.databaseUser.textProperty().bindBidirectional(dom.dbUser());

        dom.resources().addListener((MapChangeListener.Change<? extends String, ? extends Resource> change) -> {
            if (displayGraph.isSelected()) {
                if (change.wasRemoved()) {
                        removeSitemapNode(change.getValueRemoved());
                    
                }
                if (change.wasAdded()) {
                        addSitemapNode(change.getValueAdded());
                }
            }
        });

        dom.links().addListener((ListChangeListener.Change<? extends Link> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Link link : change.getAddedSubList()) {
                        addEdge(link);
                    }
                 } else if (change.wasRemoved()){
                     for(Link link : change.getRemoved()) {
                         removeEdge(link);
                     }
                 }
                
                
            }
        });

        //resourceTable.setItems(dom.resources());
        resourceTable.setEditable(false);
        resourceCodeColumn.setCellValueFactory(cellData -> cellData.getValue().codeProperty());
        resourceMimeTypeColumn.setCellValueFactory(cellData -> cellData.getValue().mimeTypeProperty());
        resourcePathColumn.setCellValueFactory((CellDataFeatures<Resource, String> p) -> new ReadOnlyObjectWrapper<>(p.getValue().getPath()));
        resourceURLColumn.setCellValueFactory((CellDataFeatures<Resource, String> p) -> new ReadOnlyObjectWrapper<>(p.getValue().getURL().toString()));
        resourceTypeColumn.setCellValueFactory((CellDataFeatures<Resource, String> p) -> new ReadOnlyObjectWrapper<>(p.getValue().getType().toString()));

        ContextMenu menu = new ContextMenu();
        MenuItem item = new MenuItem("View Resource");
        item.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Resource res = resourceTable.getSelectionModel().getSelectedItem();
                createResourcePane(res);
                SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
                selectionModel.select(detailsTab);
            }
        });
        menu.getItems().addAll(item);

        EventHandler click = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Resource res = (Resource) resourceTable.getItems().get(((TableCell) event.getSource()).getIndex());
                    createResourcePane(res);
                    SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
                    selectionModel.select(detailsTab);
                }
            }
        };

        GenericCellFactory cellFactory = new GenericCellFactory(click, menu);
        resourceMimeTypeColumn.setCellFactory(cellFactory);
        resourcePathColumn.setCellFactory(cellFactory);
        resourceURLColumn.setCellFactory(cellFactory);

        overviewDomainLabel.textProperty().bindBidirectional(dom.hostnameProperty());
        overviewResourceLabel.textProperty().bindBidirectional(dom.resourcesCountProperty(), new NumberStringConverter());
        overviewLinksLabel.textProperty().bindBidirectional(dom.linksCountProperty(), new NumberStringConverter());
        
        //WebView controller
        JSObject jsobs = (JSObject) this.sitemap.getEngine().executeScript("window");
        SitemapController sitemapController = new SitemapController();
        sitemapController.setDomain(dom);
        jsobs.setMember("controller", sitemapController);
    }

    private void createResourcePane(Resource res) {
        try {
            detailsPanel.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(resourceToFXML(res));
            detailsPanel.getChildren().add(loader.load());
            ResourceController controller = loader.<ResourceController>getController();
            controller.loadResource(res);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Couldn't change Panel, probably wrong url of FXML file.");
        }

    }

    private void addEdge(Link link) {
        if (link.getEdgeFormat() != null) {
            String fromUrl = link.getFromResource().getURL().toString();
            String toUrl = link.getToResource().getURL().toString();
            String edge = "addEdgeIfNotExists('" + link.getEdgeFormat() + "', '" + fromUrl + "', '" + toUrl + "', '"+link.getCount()+"');";
            try {
                this.sitemap.getEngine().executeScript(edge);
            } catch (JSException ex) {
                logger.log(Level.WARNING, ex.toString());
            }
        }
    }
    
    private void removeEdge(Link link){
        if(link.getEdgeFormat() != null){
            String edge = "removeEdge('" + link.getEdgeFormat() + "');";
            try {
                this.sitemap.getEngine().executeScript(edge);
            } catch (JSException ex) {
                logger.log(Level.WARNING, ex.toString());
            }
        }
    }

    private void addSitemapNode(Resource resource) {
        String path = resource.getURL().toString();
        String script = "addResourceIfNotExists('" + path + "', '" + resource.getType().getGroup() + "','"+resource.getVertexID().toString()+"');";
        try {
            this.sitemap.getEngine().executeScript(script);
        } catch (JSException ex) {
            logger.log(Level.WARNING, ex.toString());
        }
    }

    private void removeSitemapNode(Resource resource) {
        String script = "removeResource('" + resource.getURL().toString() + "');";
        try {
            this.sitemap.getEngine().executeScript(script);
        } catch (JSException ex) {
            logger.log(Level.WARNING, ex.toString());
        }
    }

    private void enableFirebug() {
        try {
            logger.log(Level.FINE, "Firebug started");
            this.sitemap.getEngine().executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
        } catch (JSException ex) {
            logger.log(Level.WARNING, ex.toString());
        }
    }

    private URL resourceToFXML(Resource res) {
        switch (res.getType()) {
            case DOM:
                return getClass().getResource("ResourceDom.fxml");
            case FILE:
                return getClass().getResource("ResourceFile.fxml");
            default:
                return getClass().getResource("ResourceUndefined.fxml");
        }
    }

}
