package reaper.view;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.util.converter.NumberStringConverter;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import reaper.Reaper;
import reaper.exceptions.DatabaseNotConnectedException;
import reaper.model.Crawler;
import reaper.model.Link;
import reaper.model.Project;
import reaper.model.Resource;

/**
 *
 * @author zaraka
 */
public class ReaperController implements Initializable {

    private static final Logger logger = Logger.getLogger(Reaper.class.getName());

    private Reaper reaper;
    private String activeNode;

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
    private TableView<URL> blacklistTable;
    @FXML
    private TableColumn<URL, String> blacklistColumn;
    @FXML
    private TableView<Project> projectTable;
    @FXML
    private TableColumn<Project, String> projectNameColumn;
    @FXML
    private TableColumn<Project, String> projectDomainColumn;
    @FXML
    private TableColumn<Project, String> projectDateColumn;
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
    private Tab projectOptionsTab;
    @FXML
    private Tab projectsTab;
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
    private Label overviewResourceLabel;
    @FXML
    private Label overviewLinksLabel;
    @FXML
    private Button addBlacklistItemButton;
    @FXML
    private Button startMiningButton;
    @FXML
    private Button stopMiningButton;
    @FXML
    private Button clearDataButton;
    @FXML
    private Button changeDatabaseButton;
    @FXML
    private Button setupDatabaseButton;
    @FXML
    private Button deleteDatabaseButton;
    @FXML
    private Label databaseStatusLabel;
    @FXML
    private Button databaseConnectButton;
    @FXML
    private Button deleteProjectButton;
    @FXML
    private Text projectName;

    @FXML
    private void startMining(ActionEvent event) {
        if ("".equals(hostname.getText())) {
            logger.log(Level.SEVERE, "You need to specify hostname.");
        }
            
        try {
            reaper.getCrawler().mineStart();
        } catch (MalformedURLException ex){
            logger.log(Level.SEVERE, "Invalid hostname " + ex.getMessage());
        }
        
    }

    @FXML
    private void stopMining(ActionEvent event) {
        reaper.getCrawler().mineStop();
    }

    @FXML
    private void clearData(ActionEvent event) {
        reaper.getCrawler().dataReset();
    }
    
    @FXML
    private void clearProjectData(ActionEvent event){
        try {
            reaper.getCrawler().truncateActiveProject();
        } catch (DatabaseNotConnectedException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }
    }
    
    @FXML
    private void deleteActiveProject(ActionEvent event){
        try {
            reaper.getCrawler().removeActiveProject();
            
            tabPane.getSelectionModel().select(projectsTab);
        } catch (DatabaseNotConnectedException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }
    }

    @FXML
    private void onChangeDisplayGraph(ActionEvent event) {
        if (displayGraph.isSelected()) {
            for (Resource res : reaper.getCrawler().resources().values()) {
                addSitemapNode(res);
            }
            for (Link link : reaper.getCrawler().links()) {
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
        try {
            Crawler crawl = reaper.getCrawler();
            crawl.loadRoot();
            activeNode = crawl.getActiveProject().getRoot().toString();
            createResourcePane(reaper.getCrawler().resources().get(activeNode));
        } catch (DatabaseNotConnectedException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }
    }

    @FXML
    private void overviewLoadAll(ActionEvent event) {
        reaper.getCrawler().loadAll();
        activeNode = reaper.getCrawler().getActiveProject().getRoot().toString();
        createResourcePane(reaper.getCrawler().resources().get(activeNode));
    }

    @FXML
    private void addBlacklistItem(ActionEvent event) {
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
                this.reaper.getCrawler().blacklistProperty().add(new URL(url));
            } catch (MalformedURLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });
    }

    @FXML
    public void createNewProject() {
        NewProjectModal project = new NewProjectModal(null);
        project.showAndWait();
        if (project.getAccepted()) {
            try {
                reaper.getCrawler().createProject(project.getName(), project.getDomain(), project.getDepth(), project.getBlacklist());
                refreshProjects();
            } catch (DatabaseNotConnectedException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
            }
        }
    }

    @FXML
    public void refreshProjects() {
        try {
            reaper.getCrawler().refreshProjects();
        } catch (DatabaseNotConnectedException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }
    }

    @FXML
    public void setupDatabase() {
        try {
            reaper.getCrawler().setupDatabase();
            logger.log(Level.INFO, "Database created");
        } catch (DatabaseNotConnectedException ex) {
            logger.log(Level.SEVERE, "Database isn't connected");
        }
    }

    @FXML
    public void teardownDatabase() {
        try {
            reaper.getCrawler().removeDatabase();
            logger.log(Level.INFO, "Database dropped");
        } catch (DatabaseNotConnectedException ex) {
            logger.log(Level.SEVERE, "Database isn't connected");
        }
    }

    @FXML
    public void databaseConnect() {
        reaper.getCrawler().databaseConnect();
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
        
        maxDepth.setTextFormatter(new TextFormatter<>(new NumberStringConverter(NumberFormat.getIntegerInstance())));
        maxDownloads.setTextFormatter(new TextFormatter<>(new NumberStringConverter(NumberFormat.getIntegerInstance())));
    }

    public void setReaper(Reaper reaper) {
        //bind data
        this.reaper = reaper;
        Crawler dom = this.reaper.getCrawler();

        this.hostname.textProperty().bindBidirectional(dom.hostnameProperty());
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
                } else if (change.wasRemoved()) {
                    for (Link link : change.getRemoved()) {
                        removeEdge(link);
                    }
                }

            }
        });

        //projectTable
        projectTable.setItems(dom.projects());
        projectTable.setEditable(false);
        projectNameColumn.setCellValueFactory((CellDataFeatures<Project, String> cellData) -> new ReadOnlyObjectWrapper<>(cellData.getValue().getName()));
        projectDomainColumn.setCellValueFactory((CellDataFeatures<Project, String> cellData) -> new ReadOnlyObjectWrapper<>(cellData.getValue().getDomain().toString()));
        projectDateColumn.setCellValueFactory((CellDataFeatures<Project, String> cellData) -> new ReadOnlyObjectWrapper<>(Project.niceDate.format(cellData.getValue().getDate())));

        ContextMenu projectMenu = new ContextMenu();
        MenuItem projectViewItem = new MenuItem("View Project");
        projectViewItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Project proj = projectTable.getSelectionModel().getSelectedItem();
                try {
                    dom.loadProject(proj);
                    tabPane.getSelectionModel().select(projectOptionsTab);
                } catch (DatabaseNotConnectedException ex) {
                    logger.log(Level.SEVERE, ex.getMessage());
                }
            }
        });
        projectMenu.getItems().addAll(projectViewItem);
        projectTable.setContextMenu(projectMenu);

        //resourceTable
        resourceTable.setItems(dom.activeResources());
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

        //blacklistTable
        blacklistTable.setEditable(false);
        blacklistTable.setItems(dom.blacklistProperty());
        blacklistColumn.setCellValueFactory((CellDataFeatures<URL, String> p) -> new ReadOnlyObjectWrapper<>(p.getValue().toString()));
        ContextMenu blacklistMenu = new ContextMenu();
        MenuItem removeBlacklistItem = new MenuItem("Remove");
        removeBlacklistItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                //.remove(blacklistTable.getSelectionModel().getSelectedIndex());
            }
        });
        blacklistTable.setContextMenu(blacklistMenu);
        blacklistMenu.getItems().add(removeBlacklistItem);

        //overview
        overviewResourceLabel.textProperty().bindBidirectional(dom.resourcesCountProperty(), new NumberStringConverter());
        overviewLinksLabel.textProperty().bindBidirectional(dom.linksCountProperty(), new NumberStringConverter());

        //WebView controller
        sitemap.setContextMenuEnabled(false);
        JSObject jsobs = (JSObject) sitemap.getEngine().executeScript("window");
        SitemapController sitemapController = new SitemapController();
        sitemapController.setParentController(this);
        jsobs.setMember("controller", sitemapController);

        //Options locker while mining is running
        dom.minerBusy().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            lockUnlockControls(newValue);
        });

        //Database connecter watchener
        dom.connectionStatus().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    databaseStatusLabel.setText("connected");
                    databaseStatusLabel.setTextFill(Color.web("green"));
                } else {
                    databaseStatusLabel.setText("not connected");
                    databaseStatusLabel.setTextFill(Color.web("red"));
                }
            }
        });
    }

    private void lockUnlockControls(boolean value) {
        hostname.setDisable(value);
        maxDownloads.setDisable(value);
        maxDepth.setDisable(value);
        databaseHost.setDisable(value);
        databasePassword.setDisable(value);
        databaseUser.setDisable(value);
        addBlacklistItemButton.setDisable(value);
        startMiningButton.setDisable(value);
        stopMiningButton.setDisable(value);
        clearDataButton.setDisable(value);
        changeDatabaseButton.setDisable(value);
        setupDatabaseButton.setDisable(value);
        deleteDatabaseButton.setDisable(value);
        deleteProjectButton.setDisable(value);
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
            String edge = "addEdgeIfNotExists('" + link.getEdgeFormat() + "', '" + fromUrl + "', '" + toUrl + "', '" + link.getCount() + "');";
            try {
                this.sitemap.getEngine().executeScript(edge);
            } catch (JSException ex) {
                logger.log(Level.WARNING, ex.toString());
            }
        }
    }

    private void removeEdge(Link link) {
        if (link.getEdgeFormat() != null) {
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
        String script = "addResourceIfNotExists('" + path + "', '" + resource.getType().getGroup() + "','" + resource.getVertexID().toString() + "');";
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
            case OUTSIDE:
                return getClass().getResource("ResourceOutside.fxml");
            default:
                return getClass().getResource("ResourceOutside.fxml");
        }
    }

    public void setActiveNode(String node) {
        try {
            reaper.getCrawler().loadResource(node);
            activeNode = node;
            createResourcePane(reaper.getCrawler().resources().get(node));
        } catch (DatabaseNotConnectedException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }
    }

    private void lockSettings() {
        addBlacklistItemButton.setDisable(true);
    }

    private void unlockSettings() {
        addBlacklistItemButton.setDisable(false);
    }

}
