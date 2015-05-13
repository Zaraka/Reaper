package reaper.model;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OConfigurationException;
import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import com.orientechnologies.orient.core.exception.OStorageException;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

import reaper.Reaper;
import reaper.exceptions.DatabaseNotConnectedException;

/**
 * Main Crawler class Also spawns a number of worker when executing DB tasks
 *
 * @author zaraka
 */
public class Crawler {

    private static final Logger loggerReaper = Logger.getLogger(Reaper.class.getName());

    private final ObservableMap<String, Resource> resources;
    private final ObservableList<Resource> activeResources; //Nasty hack bacause Map
    private final ObservableList<Link> links;
    private final ObservableList<Project> projects;
    private final ObservableList<URL> blacklist;
    private final ObservableList<URL> whitelist;
    private final StringProperty hostname;
    private final IntegerProperty maxDepth;
    private final StringProperty dbHost;
    private final StringProperty dbUser;
    private final StringProperty dbPassword;
    private final IntegerProperty resourcesCount;
    private final IntegerProperty linksCount;
    private final StringProperty name;
    private final BooleanProperty minerBusy;
    private final BooleanProperty createSitemap;
    private final ReaperDatabase database;
    private final Preferences prefs;
    private final StringProperty phantomPath;
    private final StringProperty galleryPath;
    private final BooleanProperty openProject;
    private final BooleanProperty autoConnect;
    private final ObjectProperty<Project> activeProject;

    private final MinerService minerService;
    private final PostScannerService postScannerService;

    public Crawler() {
        this.resources = FXCollections.observableHashMap();
        this.links = FXCollections.observableArrayList();
        this.prefs = Preferences.userNodeForPackage(Reaper.class);
        this.hostname = new SimpleStringProperty("");
        this.maxDepth = new SimpleIntegerProperty(1);
        this.dbHost = new SimpleStringProperty(getPrefDBHost());
        this.dbPassword = new SimpleStringProperty(getPrefDBPassword());
        this.dbUser = new SimpleStringProperty(getPrefDBUser());
        this.resourcesCount = new SimpleIntegerProperty(0);
        this.linksCount = new SimpleIntegerProperty(0);
        this.blacklist = FXCollections.observableArrayList();
        this.whitelist = FXCollections.observableArrayList();
        this.minerBusy = new SimpleBooleanProperty(false);
        this.minerService = new MinerService();
        this.database = new ReaperDatabase();
        this.projects = FXCollections.observableArrayList();
        this.activeResources = FXCollections.observableArrayList();
        this.name = new SimpleStringProperty("");
        this.phantomPath = new SimpleStringProperty(getPrefPhantomPath());
        this.galleryPath = new SimpleStringProperty(getPrefGalleryPath());
        this.postScannerService = new PostScannerService();
        this.createSitemap = new SimpleBooleanProperty(getPrefCreateSitemap());
        this.openProject = new SimpleBooleanProperty(false);
        this.autoConnect = new SimpleBooleanProperty(getPrefAutoConnect());
        this.activeProject = new SimpleObjectProperty<>(null);

        this.init();
    }

    private String getPrefDBHost() {
        return prefs.get(PreferenceKeys.DB_HOST.getKey(), (String) PreferenceKeys.DB_HOST.getValue());
    }

    private String getPrefDBUser() {
        return prefs.get(PreferenceKeys.DB_USER.getKey(), (String) PreferenceKeys.DB_USER.getKey());
    }

    private String getPrefDBPassword() {
        return prefs.get(PreferenceKeys.DB_PASS.getKey(), (String) PreferenceKeys.DB_PASS.getKey());
    }

    private String getPrefPhantomPath() {
        return prefs.get(PreferenceKeys.PHANTOM_PATH.getKey(), (String) PreferenceKeys.PHANTOM_PATH.getKey());
    }

    private String getPrefGalleryPath() {
        return prefs.get(PreferenceKeys.GALLERY_PATH.getKey(), (String) PreferenceKeys.GALLERY_PATH.getKey());
    }

    private boolean getPrefCreateSitemap() {
        return prefs.getBoolean(PreferenceKeys.CREATE_SITEMAP.getKey(), (Boolean) PreferenceKeys.CREATE_SITEMAP.getValue());
    }

    private boolean getPrefAutoConnect() {
        return prefs.getBoolean(PreferenceKeys.AUTO_CONNECT.getKey(), (Boolean) PreferenceKeys.AUTO_CONNECT.getValue());
    }

    public void refreshProjects() throws DatabaseNotConnectedException {
        if (!database.isConnected()) {
            throw new DatabaseNotConnectedException("Database is not connected");
        }
        projects.clear();
        try {
            database.getProjects(projects);
        } catch (OConfigurationException ex ) {
            loggerReaper.log(Level.SEVERE, "Cannot connect to Database, database probably doesnt exist");
        } catch (OSecurityAccessException ex) {
            loggerReaper.log(Level.SEVERE, "User or password not valid for database");
        }
    }

    /**
     * Connects to database
     *
     * @throws java.io.IOException
     */
    public void databaseConnect() throws IOException, OStorageException {
        database.connect(getDbHost(), getDbUser(), getDbPassword());
        minerService.databaseConnect(getDbHost(), getDbUser(), getDbPassword());
        postScannerService.databaseConnect(getDbHost(), getDbUser(), getDbPassword());
    }

    public void databaseDisconnect() {
        database.disconnect();
        minerService.databaseDisconnect();
        postScannerService.databaseDisconnect();
    }

    /**
     * Start external thread where create Database schema
     *
     * @throws DatabaseNotConnectedException
     */
    public void setupDatabase() throws DatabaseNotConnectedException {
        if (!database.isConnected()) {
            throw new DatabaseNotConnectedException("Database is not connected");
        }

        Task<Void> setupTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                database.setupSchema();
                return null;
            }
        };
        setupTask.setOnSucceeded((WorkerStateEvent event) -> {
            loggerReaper.log(Level.INFO, "Database created");
        });
        setupTask.setOnFailed((WorkerStateEvent event) -> {
            if (event.getSource().getException() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                event.getSource().getException().printStackTrace(pw);
                loggerReaper.log(Level.SEVERE, sw.toString());
            }
        });
        new Thread(setupTask).start();
    }

    /**
     * Start external thread where drop database
     *
     * @throws DatabaseNotConnectedException
     */
    public void removeDatabase() throws DatabaseNotConnectedException {
        if (!database.isConnected()) {
            throw new DatabaseNotConnectedException("Database is not connected");
        }

        Task<Void> teardownTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                database.tearDown();
                return null;
            }
        };
        teardownTask.setOnSucceeded((WorkerStateEvent event) -> {
            loggerReaper.log(Level.INFO, "Database dropped");
        });
        teardownTask.setOnFailed((WorkerStateEvent event) -> {
            if (event.getSource().getException() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                event.getSource().getException().printStackTrace(pw);
                loggerReaper.log(Level.SEVERE, sw.toString());
            }
        });
        new Thread(teardownTask).start();
    }

    public void dataReset() {
        if (this.minerService.isRunning()) {
            loggerReaper.log(Level.WARNING, "Cant clear data while miner is running");
            return;
        }

        database.truncateData();
    }

    public void deleteActiveProject() throws DatabaseNotConnectedException {
        if (!database.isConnected()) {
            throw new DatabaseNotConnectedException("Database is not connected");
        }

        if (activeProject == null) {
            loggerReaper.log(Level.SEVERE, "Project not set");
            return;
        }

        clearData();
        database.removeProject(activeProject.get());

        refreshProjects();
    }

    public void deleteProject(Project proj) throws DatabaseNotConnectedException {
        if (!database.isConnected()) {
            throw new DatabaseNotConnectedException("Database is not connected");
        }

        if (proj == activeProject.get()) {
            deleteActiveProject();
        } else {
            database.removeProject(proj);
            refreshProjects();
        }
    }

    public void truncateActiveProject() throws DatabaseNotConnectedException {
        if (!database.isConnected()) {
            throw new DatabaseNotConnectedException("Database is not connected");
        }

        if (activeProject == null) {
            loggerReaper.log(Level.SEVERE, "Project not set");
            return;
        }

        clearData();
        database.truncateProject(activeProject.get());
        loggerReaper.log(Level.INFO, "Project data deleted");
    }

    /**
     * Update all preference keys
     */
    public void updateDBPref() {
        prefs.put(PreferenceKeys.DB_HOST.getKey(), getDbHost());
        prefs.put(PreferenceKeys.DB_USER.getKey(), getDbUser());
        prefs.put(PreferenceKeys.DB_PASS.getKey(), getDbPassword());
        prefs.put(PreferenceKeys.GALLERY_PATH.getKey(), getGalleryPath());
        prefs.put(PreferenceKeys.PHANTOM_PATH.getKey(), getPhantomPath());
        prefs.putBoolean(PreferenceKeys.CREATE_SITEMAP.getKey(), getCreateSitemap());
        prefs.putBoolean(PreferenceKeys.AUTO_CONNECT.getKey(), getAutoConnect());
    }

    /**
     *
     * @throws DatabaseNotConnectedException
     * @deprecated
     */
    public void loadAll() throws DatabaseNotConnectedException {
        if (!database.isConnected()) {
            throw new DatabaseNotConnectedException("Database is not connected");
        }

        clearData();
        database.loadAll(resources, links, activeProject.get().getCluster());
    }

    /**
     * Load Root Web Document
     *
     * @throws DatabaseNotConnectedException
     */
    public void loadRoot() throws DatabaseNotConnectedException {
        if (!database.isConnected()) {
            throw new DatabaseNotConnectedException("Database is not connected");
        }

        if (activeProject.get().getRoot() != null) {
            database.loadResource(
                    activeProject.get().getRoot(),
                    resources,
                    links,
                    activeProject.get().getCluster());
        }
    }

    public void loadResource(Object id) throws DatabaseNotConnectedException {
        if (!database.isConnected()) {
            throw new DatabaseNotConnectedException("Database is not connected");
        }

        clearData();
        database.loadResource(id, resources, links, activeProject.get().getCluster());
        activeResources.setAll(resources.values()); //LOL
    }

    public void createProject(String name, URL domain, int depth, List<URL> blacklist, List<URL> whitelist) throws DatabaseNotConnectedException {
        if (!database.isConnected()) {
            throw new DatabaseNotConnectedException("Database is not connected");
        }

        Project proj = database.createProject(name, domain, depth);

        //Database schema reload - fix OrientDB bug
        databaseDisconnect();
        try {
            databaseConnect();
        } catch (IOException | OStorageException ex) {
            loggerReaper.log(Level.SEVERE, ex.getMessage());
        }

        database.insertBlackWhiteList(proj, blacklist, whitelist);
    }

    public void loadProject(Project proj) throws DatabaseNotConnectedException {
        clearData();

        activeProject.set(proj);

        OrientGraph graph = database.getDatabase().getTx();
        try {
            activeProject.get().update(graph);
            if (activeProject.get().getRoot() == null) {
                loggerReaper.log(Level.INFO, "Project not yet mined");
            } else {
                loadResource(activeProject.get().getRoot());
            }
            blacklist.addAll(activeProject.get().getBlacklist());
            whitelist.addAll(activeProject.get().getWhitelist());
        } finally {
            graph.shutdown();
        }
        setHostname(activeProject.get().getDomain().toString());
        setName(activeProject.getName());
        setMaxDepth(activeProject.get().getDepth());

        ODatabaseDocumentTx oDB = database.getDatabase().getDatabase();
        try {
            resourcesCount.set((int) activeProject.get().getResourceCount(oDB));
            linksCount.set((int) activeProject.get().getLinksCount(oDB));
        } finally {
            oDB.close();
        }
        this.openProject.set(true);
    }

    public void closeProject() {
        clearData();
        activeProject.set(null);
        this.openProject.set(false);
    }

    /**
     * Load and return list of statistics used for GUI
     *
     * @param proj Project to load
     * @return List of Map statistics 0 - Types, 1 - Codes
     */
    public List<Map<String, Long>> loadProjectStats(Project proj) {
        List<Map<String, Long>> result = new ArrayList<>();
        //Converting graph to Document like this is really dumb
        OrientGraph graph = database.getDatabase().getTx();
        try {
            result.add(proj.getStatsTypes(graph));
            result.add(proj.getStatsCodes(graph));
        } finally {
            graph.shutdown();
        }

        return result;
    }

    private void init() {
        minerService.init();
        minerService.setOnSucceeded((WorkerStateEvent event) -> {
            try {
                loadProject(activeProject.get());
            } catch (DatabaseNotConnectedException ex) {
                loggerReaper.log(Level.SEVERE, ex.getMessage());
            }
            this.minerService.reset();
            this.setMinerBusy(false);
            loggerReaper.log(Level.INFO, "Mining finished");
        });
        minerService.setOnFailed((WorkerStateEvent event) -> {
            this.setMinerBusy(false);
            loggerReaper.log(Level.SEVERE, "Mining failed");
            if (event.getSource().getException() != null) {
                loggerReaper.log(Level.SEVERE, event.getSource().getMessage());
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                event.getSource().getException().printStackTrace(pw);
                loggerReaper.log(Level.SEVERE, sw.toString());
            }
        });
        minerService.setOnRunning((WorkerStateEvent event) -> {
            this.setMinerBusy(true);
            loggerReaper.log(Level.INFO, "Mining service started");
        });
        minerService.setOnCancelled((WorkerStateEvent event) -> {
            this.setMinerBusy(false);
            loggerReaper.log(Level.INFO, "Mining canceled");
        });

        minerService.setOnReady((WorkerStateEvent event) -> {
            this.setMinerBusy(false);
        });

        postScannerService.init();
        postScannerService.setOnSucceeded((WorkerStateEvent event) -> {
            this.setMinerBusy(true);
        });
        postScannerService.setOnFailed((WorkerStateEvent event) -> {
            this.setMinerBusy(false);
            loggerReaper.log(Level.SEVERE, "Post scanning process failed");
            if (event.getSource().getException() != null) {
                loggerReaper.log(Level.SEVERE, event.getSource().getMessage());
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                event.getSource().getException().printStackTrace(pw);
                loggerReaper.log(Level.SEVERE, sw.toString());
            }
        });
        postScannerService.setOnRunning((WorkerStateEvent event) -> {
            this.setMinerBusy(true);
            loggerReaper.log(Level.INFO, "Post scanning process started");
        });
        postScannerService.setOnCancelled((WorkerStateEvent event) -> {
            this.setMinerBusy(false);
            loggerReaper.log(Level.INFO, "Post scanning process canceled");
        });

        postScannerService.setOnReady((WorkerStateEvent event) -> {
            this.setMinerBusy(false);
        });
    }

    public void minerStart() throws MalformedURLException {
        if (minerService.isRunning()) {
            return;
        }
        loggerReaper.log(Level.INFO, "Request mining on " + hostname.get());

        //delete data
        clearData();

        URL url = new URL(hostname.get());
        activeProject.get().setDomain(url);
        activeProject.get().setDepth(getMaxDepth());

        //Save project before mining
        OrientGraph graph = database.getDatabase().getTx();
        try {
            activeProject.get().save(graph);
        } finally {
            graph.shutdown();
        }

        //Set miner and start
        minerService.prepare(activeProject.get());
        minerService.start();
    }

    public void postScannerStart() {
        if (postScannerService.isRunning()) {
            return;
        }

        postScannerService.prepare(activeProject.get(), getPhantomPath(), getGalleryPath());
        postScannerService.start();
    }

    public void minerStop() {
        if (minerService.isRunning()) {
            minerService.cancel();
        }
    }

    public void postScannerStop() {
        if (postScannerService.isRunning()) {
            postScannerService.cancel();
        }
    }

    public void setActiveProject(Project proj) {
        activeProject.set(proj);
    }

    public Project getActiveProject() {
        return activeProject.get();
    }

    public ObjectProperty<Project> activeProjectProperty() {
        return activeProject;
    }

    private void clearData() {
        activeResources.clear();
        links.clear();
        resources.clear();
    }

    private void clearBlacklists() {
        blacklist.clear();
        whitelist.clear();
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

    public final String getName() {
        return this.name.get();
    }

    public final void setName(String name) {
        this.name.set(name);
    }

    public final StringProperty nameProperty() {
        return this.name;
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

    public ObservableList<URL> whitelistProperty() {
        return this.whitelist;
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

    public ObservableList<Project> projects() {
        return this.projects;
    }

    public BooleanProperty connectionStatus() {
        return database.connectionStatus();
    }

    public ReaperDatabase getDatabase() {
        return database;
    }

    public ObservableList<Resource> activeResources() {
        return activeResources;
    }

    public String getGalleryPath() {
        return galleryPath.get();
    }

    public void setGalleryPath(String path) {
        galleryPath.set(path);
    }

    public StringProperty galleryPathProperty() {
        return galleryPath;
    }

    public String getPhantomPath() {
        return phantomPath.get();
    }

    public void setPhantomPath(String path) {
        phantomPath.set(path);
    }

    public StringProperty phantomPathProperty() {
        return phantomPath;
    }

    public boolean getCreateSitemap() {
        return createSitemap.get();
    }

    public void setCreateSitemap(boolean value) {
        createSitemap.set(value);
    }

    public BooleanProperty createSitemapProperty() {
        return createSitemap;
    }

    public boolean getAutoConnect() {
        return autoConnect.get();
    }

    public void setAutoConnect(boolean value) {
        autoConnect.set(value);
    }

    public BooleanProperty autoConnectProperty() {
        return autoConnect;
    }

    public boolean isOpenProject() {
        return openProject.get();
    }

    public void setOpenProject(boolean value) {
        openProject.set(value);
    }

    public BooleanProperty openProjectProperty() {
        return openProject;
    }
}
