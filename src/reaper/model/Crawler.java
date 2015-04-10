package reaper.model;

import com.orientechnologies.orient.core.exception.OStorageException;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.WorkerStateEvent;
import reaper.Reaper;
import reaper.exceptions.DatabaseNotConnectedException;

/**
 *
 * @author zaraka
 */
public class Crawler {

    private static final Logger logger = Logger.getLogger(Reaper.class.getName());

    private final ObservableMap<String, Resource> resources;
    private final ObservableList<Link> links;
    private final ObservableList<Project> projects;
    private final ObservableList<URL> blacklist;
    private final StringProperty hostname;
    private final IntegerProperty maxDownloads;
    private final IntegerProperty maxDepth;
    private final StringProperty dbHost;
    private final StringProperty dbUser;
    private final StringProperty dbPassword;
    private final IntegerProperty resourcesCount;
    private final IntegerProperty linksCount;
    private final BooleanProperty minerBusy;
    private final ReaperDatabase database;
    private final Preferences prefs;
    private Project activeProject;

    private final MinerService minerService;

    public Crawler() {
        this.resources = FXCollections.observableHashMap();
        this.links = FXCollections.observableArrayList();
        this.prefs = Preferences.userNodeForPackage(Reaper.class);
        this.hostname = new SimpleStringProperty("");
        this.maxDepth = new SimpleIntegerProperty(1);
        this.maxDownloads = new SimpleIntegerProperty(5);
        this.dbHost = new SimpleStringProperty(getPrefDBHost());
        this.dbPassword = new SimpleStringProperty(getPrefDBPassword());
        this.dbUser = new SimpleStringProperty(getPrefDBUser());
        this.resourcesCount = new SimpleIntegerProperty(0);
        this.linksCount = new SimpleIntegerProperty(0);
        this.blacklist = FXCollections.observableArrayList();
        this.minerBusy = new SimpleBooleanProperty(false);
        this.minerService = new MinerService();
        this.database = new ReaperDatabase();
        this.projects = FXCollections.observableArrayList();
        this.activeProject = null;

        this.init();
        
        dbHost.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            prefs.put(PreferenceKeys.DB_HOST.getKey(), newValue);
            logger.log(Level.INFO, "dbHost changed");
        });
        
        dbPassword.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            prefs.put(PreferenceKeys.DB_PASS.getKey(), newValue);
            logger.log(Level.INFO, "dbPass changed");
        });
        
        dbUser.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            prefs.put(PreferenceKeys.DB_USER.getKey(), newValue);
            logger.log(Level.INFO, "dbUser changed");
        });
    }
    
    public void refreshProjects() throws DatabaseNotConnectedException{
        if(!database.isConnected()){
            throw new DatabaseNotConnectedException("Database is not connected");
        }
        projects.clear();
        database.getProjects(projects);
    }

    /**
     * Connects to database
     */
    public void databaseConnect() {
        try {
            database.connect(getDbHost(), getDbUser(), getDbPassword());
            minerService.databaseConnect(getDbHost(), getDbUser(), getDbPassword());
        } catch (OStorageException ex) {
            logger.log(Level.SEVERE, "Cant connect to database " + getDbHost());
        }
    }

    public void databaseDisconnect() {
        database.disconnect();
        minerService.databaseDisconnect();
    }

    public void setupDatabase() throws DatabaseNotConnectedException {
        if(!database.isConnected()){
            throw new DatabaseNotConnectedException("Database is not connected");
        }
        database.setupSchema();
    }

    public void removeDatabase() throws DatabaseNotConnectedException {
        if(!database.isConnected()){
            throw new DatabaseNotConnectedException("Database is not connected");
        }
        database.tearDown();
    }

    public void dataReset() {
        if (this.minerService.isRunning()) {
            logger.log(Level.WARNING, "Cant clear data while miner is running");
            return;
        }

        database.truncateData();
    }

    public void updateDBPref() {
        prefs.put(PreferenceKeys.DB_HOST.getKey(), getDbHost());
        prefs.put(PreferenceKeys.DB_USER.getKey(), getDbUser());
        prefs.put(PreferenceKeys.DB_PASS.getKey(), getDbPassword());
    }

    public void loadAll(){
        clearData();
        database.loadAll(resources, links, activeProject.getCluster());
    }

    public void loadRoot() throws DatabaseNotConnectedException {
        if(!database.isConnected()){
            throw new DatabaseNotConnectedException("Database is not connected");
        }
        
        if (activeProject.getRoot() != null) {
            database.loadResource(
                    activeProject.getRoot(), 
                    resources, 
                    links, 
                    activeProject.getCluster());
        }
    }

    public void loadResource(Object id) throws DatabaseNotConnectedException{
        if(!database.isConnected()){
            throw new DatabaseNotConnectedException("Database is not connected");
        }
        
        clearData();
        database.loadResource(id, resources, links, activeProject.getCluster());
    }
    

    public void createProject(String name, URL domain, ArrayList<URL> blacklist) throws DatabaseNotConnectedException{
        if(!database.isConnected()){
            throw new DatabaseNotConnectedException("Database is not connected");
        }
        
        database.createProject(name, domain, blacklist);
    }
    
    public void loadProject(Project proj) throws DatabaseNotConnectedException{
        clearData();
        blacklist.clear();
        
        activeProject = proj;
        
        if(activeProject.getRoot() == null){
            logger.log(Level.INFO, "Project not yet mined");
            return;
        }
        
        OrientGraph graph = database.getDatabase().getTx();
        try{
            loadResource(activeProject.getRoot());
        } catch (DatabaseNotConnectedException ex) {
            throw ex;
        } finally {
            graph.shutdown();
        }
        
        setHostname(activeProject.getDomain().toString());
        
    }
    
    private void init() {
        try {
            minerService.init();
        } catch (OStorageException ex) {
            logger.log(Level.SEVERE, ex.toString());
        }
        minerService.setOnSucceeded((WorkerStateEvent event) -> {
            this.setResourcesCount(this.minerService.getResourceCount());
            this.setLinksCount(this.minerService.getLinksCount());
            this.minerService.reset();
            this.setMinerBusy(false);
            logger.log(Level.INFO, "Mining finished");
        });
        minerService.setOnFailed((WorkerStateEvent event) -> {
            this.setMinerBusy(false);
            logger.log(Level.SEVERE, "Mining failed");
            if (event.getSource().getException() != null) {
                logger.log(Level.SEVERE, event.getSource().getException().toString());
            }
        });
        minerService.setOnRunning((WorkerStateEvent event) -> {
            this.setMinerBusy(true);
            logger.log(Level.INFO, "Mining service started");
        });
        minerService.setOnCancelled((WorkerStateEvent event) -> {
            this.setMinerBusy(false);
            logger.log(Level.INFO, "Mining canceled");
        });

        //minerService.set
    }

    public void mineStart() throws MalformedURLException{
        if (!minerService.isRunning()) {
            logger.log(Level.INFO, "Request mining on " + hostname);
            
            //delete data
            clearData();
            blacklist.clear();
            
            URL url = new URL(hostname.get());
            activeProject.setDomain(url);
            
            minerService.prepare(hostname.get(), activeProject.getCluster(), maxDepth.get());
            minerService.start();
        }
    }
    
    public void setActiveProject(Project proj){
        this.activeProject = proj;
    } 
    
    public Project getActiveProject(){
        return this.activeProject;
    }

    public void mineStop() {
        if (this.minerService.isRunning()) {
            minerService.cancel();
        }
    }

    private void clearData() {
        this.links.clear();
        this.resources.clear();
    }

    //GETs & SETs
    public ObservableMap<String, Resource> resources() {
        //return new ArrayList<>(this.tmpResources.values());
        return this.resources;
    }

    public ObservableList<Link> links() {
        return this.links;
    }

    public final String getHostname() {
        return this.hostname.get();
    }

    public final void setHostname(String hostname) {
        this.hostname.set(hostname);
    }

    public StringProperty hostnameProperty() {
        return this.hostname;
    }

    public final int getMaxDownloads() {
        return this.maxDownloads.get();
    }

    public final void setMaxDownloads(int maxDownloads) {
        this.maxDownloads.set(maxDownloads);
    }

    public IntegerProperty maxDownloadsProperty() {
        return this.maxDownloads;
    }

    public IntegerProperty maxDepthProperty() {
        return this.maxDepth;
    }

    public final int getMaxDepth() {
        return this.maxDepth.get();
    }

    public final void setMaxDepth(int maxDepth) {
        this.maxDepth.set(maxDepth);
    }

    public StringProperty dbHost() {
        return this.dbHost;
    }

    public StringProperty dbUser() {
        return this.dbUser;
    }

    public StringProperty dbPassword() {
        return this.dbPassword;
    }

    public String getDbHost() {
        return this.dbHost.get();
    }

    public String getDbUser() {
        return this.dbUser.get();
    }

    public String getDbPassword() {
        return this.dbPassword.get();
    }

    public void setDbHost(String host) {
        this.dbHost.set(host);
    }

    public void setDbUser(String user) {
        this.dbUser.set(user);
    }

    public void setDbPassword(String password) {
        this.dbPassword.set(password);
    }

    public int getResourcesCount() {
        return this.resourcesCount.get();
    }

    public int getLinkCount() {
        return this.linksCount.get();
    }

    public void setResourcesCount(int count) {
        this.resourcesCount.set(count);
    }

    public void setLinksCount(int count) {
        this.linksCount.set(count);
    }

    public IntegerProperty resourcesCountProperty() {
        return this.resourcesCount;
    }

    public IntegerProperty linksCountProperty() {
        return this.linksCount;
    }

    public ObservableList<URL> blacklistProperty() {
        return this.blacklist;
    }

    public boolean getMinerBusy() {
        return this.minerBusy.get();
    }

    public void setMinerBusy(boolean value) {
        this.minerBusy.set(value);
    }

    public BooleanProperty minerBusy() {
        return this.minerBusy;
    }

    private String getPrefDBHost() {
        return prefs.get(PreferenceKeys.DB_HOST.getKey(), "remote:localhost/ReaperTest");
    }

    private String getPrefDBUser() {
        return prefs.get(PreferenceKeys.DB_USER.getKey(), "root");
    }

    private String getPrefDBPassword() {
        return prefs.get(PreferenceKeys.DB_PASS.getKey(), "admin");
    }
    
    public ObservableList<Project> projects(){
        return this.projects;
    }
    
    public BooleanProperty connectionStatus(){
        return database.connectionStatus();
    }
    
    public ReaperDatabase getDatabase(){
        return database;
    }
}