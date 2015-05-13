package reaper.view;

import com.orientechnologies.orient.core.exception.OStorageException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import reaper.Reaper;
import reaper.exceptions.DatabaseNotConnectedException;
import reaper.model.Crawler;
import reaper.model.Link;
import reaper.model.MinerService;
import reaper.model.PostScannerService;
import reaper.model.Project;
import reaper.model.Resource;

/**
 *
 * @author zaraka
 */
public class ReaperController implements Initializable {

    private static final Logger loggerReaper = Logger.getLogger(Reaper.class.getName());
    private static final Logger loggerMiner = Logger.getLogger(MinerService.class.getName());
    private static final Logger loggerPostScanner = Logger.getLogger(PostScannerService.class.getName());

    private Reaper reaper;
    private String activeNode;

    @FXML
    private TextField hostname;
    @FXML
    private TextFlow consoleReaper;
    @FXML
    private TextFlow consoleMiner;
    @FXML
    private TextFlow consolePostScanner;
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
    private TableView<URL> whitelistTable;
    @FXML
    private TableColumn<URL, String> whitelistColumn;
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
    private ScrollPane consoleReaperScroll;
    @FXML
    private ScrollPane consoleMinerScroll;
    @FXML
    private ScrollPane consolePostScannerScroll;
    @FXML
    private Tab statsTab;
    @FXML
    private Tab resultsTab;
    @FXML
    private Tab projectOptionsTab;
    @FXML
    private Tab projectsTab;
    @FXML
    private TabPane tabPane;
    @FXML
    private StackPane detailsPanel;
    @FXML
    private Label overviewResourceLabel;
    @FXML
    private Label overviewLinksLabel;
    @FXML
    private Button addBlacklistItemButton;
    @FXML
    private Button addWhitelistItemButton;
    @FXML
    private Button startMiningButton;
    @FXML
    private Button stopMiningButton;
    @FXML
    private Button clearDataButton;
    @FXML
    private Button deleteProjectButton;
    @FXML
    private Text projectName;
    @FXML
    private PieChart chartOverviewStatus;
    @FXML
    private PieChart chartCodesStatus;
    @FXML
    private Button createNewProjectButton;
    @FXML
    private Button loadRootButton;
    @FXML
    private MenuItem databaseConnectMenuItem;
    @FXML
    private Button startPostScannerButton;
    @FXML
    private Button stopPostScannerButton;
    @FXML
    private CheckMenuItem createSitemapMenuItem;
    @FXML
    private MenuItem closeProjectMenuItem;
    @FXML
    private TableView<PieChart.Data> statsTypeTable;
    @FXML
    private TableColumn<PieChart.Data, String> statsTypeTypeColumn;
    @FXML
    private TableColumn<PieChart.Data, Long> statsTypeCountColumn;
    @FXML
    private TableView<PieChart.Data> statsCodeTable;
    @FXML
    private TableColumn<PieChart.Data, String> statsCodeCodeColumn;
    @FXML
    private TableColumn<PieChart.Data, Long> statsCodeCountColumn;
    @FXML
    private TabPane consoleTabPane;
    @FXML
    private Tab consoleTabReaper;
    @FXML
    private Tab consoleTabMiner;
    @FXML
    private Tab consoleTabPostScanner;
    @FXML
    private Button eraseCurrentLogButton;

    @FXML
    private void clearProjectData(ActionEvent event) {
        try {
            reaper.getCrawler().truncateActiveProject();
        } catch (DatabaseNotConnectedException ex) {
            loggerReaper.log(Level.SEVERE, ex.getMessage());
        }
    }

    @FXML
    private void deleteActiveProject(ActionEvent event) {
        try {
            reaper.getCrawler().deleteActiveProject();

            tabPane.getSelectionModel().select(projectsTab);
        } catch (DatabaseNotConnectedException ex) {
            loggerReaper.log(Level.SEVERE, ex.getMessage());
        }
    }

    @FXML
    private void onChangeDisplayGraph(ActionEvent event) {
        if (createSitemapMenuItem.isSelected()) {
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
                loggerReaper.log(Level.WARNING, ex.toString());
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
            loggerReaper.log(Level.SEVERE, ex.getMessage());
        }
    }

    @FXML
    private void overviewLoadAll(ActionEvent event) {
        try {
            reaper.getCrawler().loadAll();
        } catch (DatabaseNotConnectedException ex) {
            Logger.getLogger(ReaperController.class.getName()).log(Level.SEVERE, null, ex);
        }
        activeNode = reaper.getCrawler().getActiveProject().getRoot().toString();
        createResourcePane(reaper.getCrawler().resources().get(activeNode));
    }

    @FXML
    private void addBlacklistItem(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("http://subdomain.example.com");
        dialog.setTitle("Add blacklisted domain");
        dialog.setHeaderText("Add blacklisted domain");
        dialog.setContentText("Please enter domain you want add to blacklist");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(domain -> {
            String url = domain;
            if (!url.matches("^.*:\\/\\/.*$")) {
                url = "http://" + url;
                loggerReaper.log(Level.WARNING, "You should provide protocol as well. Default proctol http is used.");
            }
            try {
                //TODO
                this.reaper.getCrawler().blacklistProperty().add(new URL(url));
            } catch (MalformedURLException ex) {
                loggerReaper.log(Level.SEVERE, null, ex);
            }
        });
    }

    @FXML
    private void addWhitelistItem(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("http://anotherdomain.com");
        dialog.setTitle("Add whitelisted domain");
        dialog.setHeaderText("Add whitelisteddomain");
        dialog.setContentText("Please enter domain you want add to whitelist");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(domain -> {
            String url = domain;
            if (!url.matches("^.*:\\/\\/.*$")) {
                url = "http://" + url;
                loggerReaper.log(Level.WARNING, "You should provide protocol as well. Default proctol http is used.");
            }
            try {
                //TODO
                this.reaper.getCrawler().blacklistProperty().add(new URL(url));
            } catch (MalformedURLException ex) {
                loggerReaper.log(Level.SEVERE, null, ex);
            }
        });
    }

    @FXML
    public void createNewProject() {
        NewProjectModal project = new NewProjectModal(null);
        project.showAndWait();
        if (project.getAccepted()) {
            try {
                reaper.getCrawler().createProject(project.getName(), project.getDomain(), project.getDepth(), project.getBlacklist(), project.getWhitelist());
                refreshProjects();
            } catch (DatabaseNotConnectedException ex) {
                loggerReaper.log(Level.SEVERE, ex.getMessage());
            }
        }
    }

    @FXML
    public void refreshProjects() {
        try {
            reaper.getCrawler().refreshProjects();
        } catch (DatabaseNotConnectedException ex) {
            loggerReaper.log(Level.SEVERE, ex.getMessage());
        }
    }

    @FXML
    public void databaseConnect() {
        try {
            reaper.getCrawler().databaseConnect();
            refreshProjects();
        } catch (IOException | OStorageException ex) {
            loggerReaper.log(Level.SEVERE, ex.getMessage());
        }
    }

    @FXML
    public void databaseDisconnect() {
        reaper.getCrawler().databaseDisconnect();
    }

    @FXML
    public void showAbout() {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("About.fxml"));
            Stage stage = new Stage();
            stage.setTitle("About Reaper");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            loggerReaper.log(Level.SEVERE, e.getMessage());
        }
    }

    @FXML
    public void showSettings() {
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Settings.fxml"));
            root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(new Scene(root));
            SettingsController controller = loader.getController();
            controller.setReaper(reaper);
            stage.show();
        } catch (IOException e) {
            loggerReaper.log(Level.SEVERE, e.getMessage());
        }
    }

    @FXML
    public void exitReaper() {
        Platform.exit();
    }

    @FXML
    private void startMining(ActionEvent event) {
        if ("".equals(hostname.getText())) {
            loggerReaper.log(Level.SEVERE, "You need to specify hostname.");
        }

        try {
            reaper.getCrawler().minerStart();
        } catch (MalformedURLException ex) {
            loggerReaper.log(Level.SEVERE, ex.getMessage());
        }

    }

    @FXML
    private void stopMining(ActionEvent event) {
        reaper.getCrawler().minerStop();
    }

    @FXML
    public void startPostScanner() {
        reaper.getCrawler().postScannerStart();
    }

    @FXML
    public void stopPostScanner() {
        reaper.getCrawler().postScannerStop();
    }

    @FXML
    public void closeProject() {
        reaper.getCrawler().closeProject();
    }
    
    @FXML
    public void eraseCurrentLog(){
        switch(consoleTabPane.getSelectionModel().getSelectedIndex()){
            case 0 :
                consoleReaper.getChildren().clear();
                break;
            case 1 :
                consoleMiner.getChildren().clear();
                break;
            case 2:
                consolePostScanner.getChildren().clear();
                break;
            default:  
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loggerReaper.addHandler(new FlowHandler(consoleReaper, consoleTabReaper));
        this.consoleReaper.getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
            consoleReaperScroll.setVvalue(consoleReaperScroll.getVmax());
        });
        consoleTabReaper.setOnSelectionChanged((Event event) -> {
            consoleTabReaper.getStyleClass().remove("tab-unread");
        });

        loggerMiner.addHandler(new FlowHandler(consoleMiner, consoleTabMiner));
        this.consoleMiner.getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
            consoleMinerScroll.setVvalue(consoleMinerScroll.getVmax());
        });
        consoleTabMiner.setOnSelectionChanged((Event event) -> {
            consoleTabMiner.getStyleClass().remove("tab-unread");
        });

        loggerPostScanner.addHandler(new FlowHandler(consolePostScanner, consoleTabPostScanner));
        this.consolePostScanner.getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
            consolePostScannerScroll.setVvalue(consolePostScannerScroll.getVmax());
        });
        consoleTabPostScanner.setOnSelectionChanged((Event event) -> {
            consoleTabPostScanner.getStyleClass().remove("tab-unread");
        });

        WebEngine engine = sitemap.getEngine();
        engine.setOnError((WebErrorEvent event) -> {
            loggerReaper.log(Level.WARNING, "WebError: " + event.getMessage());
        });

        String sitemapURL = Reaper.class.getResource("view/sitemap.html").toExternalForm();
        engine.load(sitemapURL);
        engine.setOnError((WebErrorEvent event) -> {
            loggerReaper.log(Level.WARNING, event.toString());
        });

        engine.setOnAlert((WebEvent<String> event) -> {
            loggerReaper.log(Level.INFO, event.toString());
        });

        maxDepth.setTextFormatter(new TextFormatter<>(new NumberStringConverter(NumberFormat.getIntegerInstance())));

        //Hide our content tabs
        statsTab.getContent().setVisible(false);
        resultsTab.getContent().setVisible(false);
        projectOptionsTab.getContent().setVisible(false);

        //Stats tables
        statsCodeCodeColumn.setCellValueFactory((CellDataFeatures<PieChart.Data, String> cellData) -> new ReadOnlyObjectWrapper<>(cellData.getValue().getName()));
        statsCodeCountColumn.setCellValueFactory((CellDataFeatures<PieChart.Data, Long> cellData) -> new ReadOnlyObjectWrapper<>((long) cellData.getValue().getPieValue()));

        statsTypeTypeColumn.setCellValueFactory((CellDataFeatures<PieChart.Data, String> cellData) -> new ReadOnlyObjectWrapper<>(cellData.getValue().getName()));
        statsTypeCountColumn.setCellValueFactory((CellDataFeatures<PieChart.Data, Long> cellData) -> new ReadOnlyObjectWrapper<>((long) cellData.getValue().getPieValue()));
        
        //Tooltips
        eraseCurrentLogButton.setTooltip(new Tooltip("Erase selected log output"));
    }

    public void setReaper(Reaper reaper) {
        //bind data
        this.reaper = reaper;
        Crawler crawler = this.reaper.getCrawler();

        createSitemapMenuItem.selectedProperty().bindBidirectional(crawler.createSitemapProperty());

        createSitemapMenuItem.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            crawler.updateDBPref();
        });

        crawler.activeProjectProperty().addListener((ObservableValue<? extends Project> observable, Project oldValue, Project newValue) -> {
            this.hostname.setText(newValue.getDomain().toString());
            this.maxDepth.setText(String.valueOf(newValue.getDepth()));
            this.projectName.setText(newValue.getName());
        });

        crawler.resources().addListener((MapChangeListener.Change<? extends String, ? extends Resource> change) -> {
            if (createSitemapMenuItem.isSelected()) {
                if (change.wasRemoved()) {
                    removeSitemapNode(change.getValueRemoved());

                }
                if (change.wasAdded()) {
                    addSitemapNode(change.getValueAdded());
                }
            }
        });

        crawler.links().addListener((ListChangeListener.Change<? extends Link> change) -> {
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

        crawler.openProjectProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            statsTab.getContent().setVisible(newValue);
            resultsTab.getContent().setVisible(newValue);
            projectOptionsTab.getContent().setVisible(newValue);
            closeProjectMenuItem.setDisable(!newValue);

        });

        //projectTable
        projectTable.setItems(crawler.projects());
        projectTable.setEditable(false);
        projectNameColumn.setCellValueFactory((CellDataFeatures<Project, String> cellData) -> new ReadOnlyObjectWrapper<>(cellData.getValue().getName()));
        projectDomainColumn.setCellValueFactory((CellDataFeatures<Project, String> cellData) -> new ReadOnlyObjectWrapper<>(cellData.getValue().getDomain().toString()));
        projectDateColumn.setCellValueFactory((CellDataFeatures<Project, String> cellData) -> new ReadOnlyObjectWrapper<>(Project.niceDate.format(cellData.getValue().getDate())));

        MenuItem projectViewItem = new MenuItem("View Project");
        projectViewItem.setOnAction((ActionEvent event) -> {
            Project proj = projectTable.getSelectionModel().getSelectedItem();
            try {
                crawler.loadProject(proj);
                List<Map<String, Long>> stats = crawler.loadProjectStats(proj);
                chartOverviewStatus.setTitle("Crawled nodes");
                ObservableList<PieChart.Data> typeDataList = FXCollections.observableArrayList(
                        new PieChart.Data("DOM nodes", stats.get(0).get("DOM")),
                        new PieChart.Data("File nodes", stats.get(0).get("FILE")),
                        new PieChart.Data("Not scanned nodes", stats.get(0).get("OUTSIDE"))
                );
                chartOverviewStatus.setData(typeDataList);
                statsTypeTable.setItems(typeDataList);
                chartCodesStatus.setTitle("Nodes return codes");
                ObservableList<PieChart.Data> codeDataList = FXCollections.observableArrayList();
                for (Map.Entry<String, Long> entry : stats.get(1).entrySet()) {
                    codeDataList.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
                chartCodesStatus.setData(codeDataList);
                statsCodeTable.setItems(codeDataList);
                tabPane.getSelectionModel().select(projectOptionsTab);
            } catch (DatabaseNotConnectedException ex) {
                loggerReaper.log(Level.SEVERE, ex.getMessage());
            }
        });
        MenuItem projectDeleteItem = new MenuItem("Delete Project");
        projectDeleteItem.setOnAction((ActionEvent event) -> {
            Project proj = projectTable.getSelectionModel().getSelectedItem();
            try {
                crawler.deleteProject(proj);
            } catch (DatabaseNotConnectedException ex) {
                loggerReaper.log(Level.SEVERE, ex.getMessage());
            }
        });
        MenuItem projectTruncateItem = new MenuItem("Delete Project data");
        projectTruncateItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

            }
        });
        projectTable.setContextMenu(new ContextMenu(
                projectViewItem,
                projectDeleteItem,
                projectTruncateItem
        ));

        //resourceTable
        resourceTable.setItems(crawler.activeResources());
        resourceTable.setEditable(false);
        //Value factories
        resourceCodeColumn.setCellValueFactory(
                cellData -> cellData.getValue().codeProperty()
        );
        resourceMimeTypeColumn.setCellValueFactory(
                cellData -> cellData.getValue().mimeTypeProperty()
        );
        resourcePathColumn.setCellValueFactory(
                cellData -> cellData.getValue().pathProperty()
        );
        resourceURLColumn.setCellValueFactory((CellDataFeatures<Resource, String> p) -> new ReadOnlyObjectWrapper<>(p.getValue().getURL().toString()));
        resourceTypeColumn.setCellValueFactory((CellDataFeatures<Resource, String> p) -> new ReadOnlyObjectWrapper<>(p.getValue().getType().toString()));
        MenuItem viewResource = new MenuItem("View Resource");
        viewResource.setOnAction((ActionEvent event) -> {
            Resource selected = resourceTable.getSelectionModel().getSelectedItem();
            setActiveNode(selected.getVertexID().toString());
        });
        resourceTable.setContextMenu(new ContextMenu(
                viewResource
        ));

        //blacklistTable
        blacklistTable.setEditable(false);
        blacklistTable.setItems(crawler.blacklistProperty());
        blacklistColumn.setCellValueFactory((CellDataFeatures<URL, String> p) -> new ReadOnlyObjectWrapper<>(p.getValue().toString()));
        MenuItem removeBlacklistItem = new MenuItem("Remove Item");
        removeBlacklistItem.setOnAction((ActionEvent event) -> {
            //TODO
        });
        blacklistTable.setContextMenu(new ContextMenu(
                removeBlacklistItem
        ));

        //whitelistTable
        whitelistTable.setEditable(false);
        whitelistTable.setItems(crawler.whitelistProperty());
        blacklistColumn.setCellValueFactory((CellDataFeatures<URL, String> p) -> new ReadOnlyObjectWrapper<>(p.getValue().toString()));
        MenuItem removeWhitelistItem = new MenuItem("Remove Item");
        removeWhitelistItem.setOnAction((ActionEvent event) -> {
            //TODO
        });
        whitelistTable.setContextMenu(new ContextMenu(
                removeWhitelistItem
        ));

        //overview
        overviewResourceLabel.textProperty().bindBidirectional(crawler.resourcesCountProperty(), new NumberStringConverter());
        overviewLinksLabel.textProperty().bindBidirectional(crawler.linksCountProperty(), new NumberStringConverter());

        //WebView controller
        sitemap.setContextMenuEnabled(false);
        JSObject jsobs = (JSObject) sitemap.getEngine().executeScript("window");
        SitemapController sitemapController = new SitemapController();
        sitemapController.setParentController(this);
        jsobs.setMember("controller", sitemapController);

        //Options locker while mining is running
        crawler.minerBusy().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            lockUnlockControls(newValue);
        });

        //Database connecter watchener
        crawler.connectionStatus().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                databaseConnectMenuItem.setText("Reload");
            } else {
                databaseConnectMenuItem.setText("Connect");
            }
        });

        //--------------------------ON STARTUP----------------------------
        if (crawler.getAutoConnect()) {
            databaseConnect();
        }
    }

    private void lockUnlockControls(boolean value) {
        hostname.setDisable(value);
        maxDepth.setDisable(value);
        addBlacklistItemButton.setDisable(value);
        addWhitelistItemButton.setDisable(value);
        startMiningButton.setDisable(value);
        startPostScannerButton.setDisable(value);
        clearDataButton.setDisable(value);
        deleteProjectButton.setDisable(value);
        createNewProjectButton.setDisable(value);
        loadRootButton.setDisable(value);
        databaseConnectMenuItem.setDisable(value);
    }

    private void createResourcePane(Resource res) {
        try {
            detailsPanel.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(resourceToFXML(res));
            detailsPanel.getChildren().add(loader.load());
            ResourceController controller = loader.<ResourceController>getController();
            controller.loadResource(res, reaper.getCrawler());
        } catch (IOException ex) {
            loggerReaper.log(Level.SEVERE, "Couldn't change Panel, probably wrong url of FXML file.");
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
                loggerReaper.log(Level.WARNING, ex.toString());
            }
        }
    }

    private void removeEdge(Link link) {
        if (link.getEdgeFormat() != null) {
            String edge = "removeEdge('" + link.getEdgeFormat() + "');";
            try {
                this.sitemap.getEngine().executeScript(edge);
            } catch (JSException ex) {
                loggerReaper.log(Level.WARNING, ex.toString());
            }
        }
    }

    private void addSitemapNode(Resource resource) {
        String path = resource.getURL().toString();
        String script = "addResourceIfNotExists('" + path + "', '" + resource.getType().getGroup() + "','" + resource.getVertexID().toString() + "');";
        try {
            this.sitemap.getEngine().executeScript(script);
        } catch (JSException ex) {
            loggerReaper.log(Level.WARNING, ex.toString());
        }
    }

    private void removeSitemapNode(Resource resource) {
        String script = "removeResource('" + resource.getURL().toString() + "');";
        try {
            this.sitemap.getEngine().executeScript(script);
        } catch (JSException ex) {
            loggerReaper.log(Level.WARNING, ex.toString());
        }
    }

    private void enableFirebug() {
        try {
            loggerReaper.log(Level.FINE, "Firebug started");
            this.sitemap.getEngine().executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
        } catch (JSException ex) {
            loggerReaper.log(Level.WARNING, ex.toString());
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
            loggerReaper.log(Level.SEVERE, ex.getMessage());
        }
    }
}
