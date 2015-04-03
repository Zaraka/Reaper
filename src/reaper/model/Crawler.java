package reaper.model;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OStorageException;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
    private final StringProperty hostname;
    private final IntegerProperty maxDownloads;
    private final IntegerProperty maxDepth;
    private final StringProperty dbHost;
    private final StringProperty dbUser;
    private final StringProperty dbPassword;
    private final IntegerProperty resourcesCount;
    private final IntegerProperty linksCount;
    private final ObservableList<URL> blacklist;
    private final BooleanProperty minerBusy;
    private final ReaperDatabase database;
    private final Preferences prefs;
    private final ObservableList<Project> projects;
    private String activeCluster;
    private Object rootId;

    private final MinerService minerService;

    public Crawler() {
        this.resources = FXCollections.observableHashMap();
        this.links = FXCollections.observableArrayList();
        this.prefs = Preferences.userNodeForPackage(Reaper.class);
        this.hostname = new SimpleStringProperty("");
        this.maxDepth = new SimpleIntegerProperty(1);
        this.maxDownloads = new SimpleIntegerProperty(5);
        this.dbHost = new SimpleStringProperty(getPrefDBHost());
        this.dbPassword = new SimpleStringProperty(getPrefDBUser());
        this.dbUser = new SimpleStringProperty(getPrefDBPassword());
        this.resourcesCount = new SimpleIntegerProperty(0);
        this.linksCount = new SimpleIntegerProperty(0);
        this.blacklist = FXCollections.observableArrayList();
        this.minerBusy = new SimpleBooleanProperty(false);
        this.minerService = new MinerService();
        this.database = new ReaperDatabase();
        this.projects = FXCollections.observableArrayList();
        this.activeCluster = "";

        this.rootId = null;

        this.init();
    }
    
    

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
        this.clearData();
        database.loadAll(resources, links, getActiveCluster());
    }

    public void loadRoot() {
        if (rootId != null) {
            database.loadResource(rootId, resources, links, getActiveCluster());
        }
    }

    public void loadResource(Object id){
        this.clearData();
        database.loadResource(id, resources, links, getActiveCluster());
    }
    

    public void createProject(String name, URL domain, ArrayList<URL> blacklist){
        database.createProject(name, domain, blacklist);
    }
    
    private void init() {
        try {
            minerService.init(this.getHostname(), this.getMaxDepth(),
                    this.getDbHost(), this.getDbUser(), this.getDbPassword());
        } catch (OStorageException ex) {
            logger.log(Level.SEVERE, ex.toString());
        }
        minerService.setOnSucceeded((WorkerStateEvent event) -> {
            this.setResourcesCount(this.minerService.getResourceCount());
            this.setLinksCount(this.minerService.getLinksCount());
            this.rootId = this.minerService.getRootId();
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

    public void mineStart(String hostname) {
        if (!this.minerService.isRunning()) {
            logger.log(Level.INFO, "Request mining on " + hostname);
            this.clearData();
            this.hostname.set(hostname);
            this.minerService.setHostname(hostname);
            this.minerService.setMaxDepth(this.maxDepth.get());
            this.minerService.start();
        }
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

    public Object getRootID() {
        return this.rootId;
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
        return prefs.get(PreferenceKeys.DB_USER.getKey(), "admin");
    }

    private String getPrefDBPassword() {
        return prefs.get(PreferenceKeys.DB_PASS.getKey(), "admin");
    }
    
    private void setActiveCluster(String cluster){
        this.activeCluster = cluster;
    }
    
    private String getActiveCluster(){
        return this.activeCluster;
    }
}
