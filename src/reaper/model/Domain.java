package reaper.model;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.WorkerStateEvent;
import reaper.Reaper;

/**
 *
 * @author zaraka
 */
public class Domain {

    private static final Logger logger = Logger.getLogger(Reaper.class.getName());

    private final ObservableList<Resource> resources;
    private final ObservableList<Link> links;
    private final StringProperty hostname;
    private final IntegerProperty maxDownloads;
    private final IntegerProperty maxDepth;
    private final StringProperty dbHost;
    private final StringProperty dbUser;
    private final StringProperty dbPassword;
    private final Map<String, Resource> tmpResources;

    private final MinerService mining;

    public Domain() {
        this.resources = FXCollections.observableArrayList();
        this.links = FXCollections.observableArrayList();
        this.hostname = new SimpleStringProperty("");
        this.maxDepth = new SimpleIntegerProperty(1);
        this.maxDownloads = new SimpleIntegerProperty(5);
        this.mining = new MinerService();
        this.dbHost = new SimpleStringProperty("remote:localhost/ReaperTest");
        this.dbPassword = new SimpleStringProperty("admin");
        this.dbUser = new SimpleStringProperty("admin");
        this.tmpResources = new HashMap();
        
        this.init();
    }
    
    public void dataReset(){
        if(this.mining.isRunning()){
            logger.log(Level.WARNING, "Cant clear data while miner is running");
            return;
        }
        
        OrientGraph graph = new OrientGraph(this.getDbHost(), this.getDbUser(), this.getDbPassword());
        try {
            long resourcesModified = graph.command(new OCommandSQL("TRUNCATE class Resource")).execute();
            logger.log(Level.INFO, String.valueOf(resourcesModified) + " Resources deleted.");
            long linksModified = graph.command(new OCommandSQL("TRUNCATE class LinkTo")).execute();
            logger.log(Level.INFO, String.valueOf(linksModified) + " Links deleted.");
        } finally {
            graph.shutdown();
            logger.log(Level.INFO, "Database truncated");
        }
    }
    
    public void loadAll(){
        OrientGraph graph = new OrientGraph(this.getDbHost(), this.getDbUser(), this.getDbPassword());
        try {
            for(Vertex ver : graph.getVerticesOfClass("Resource", false)){
                try {
                    Resource res = ResourceFactory.resourceFromVector(ver);
                    //this.tmpResources.put(res.getURL().toString(), res);
                    this.resources.add(res);
                } catch (MalformedURLException ex) {
                    logger.log(Level.SEVERE, ex.toString());
                }
                
            }
        } finally {
            graph.shutdown();
        }
    }

    private void init() {
        mining.init(this.getHostname(), this.getMaxDepth(), 
                this.getDbHost(), this.getDbUser(), this.getDbPassword());
        mining.setOnSucceeded((WorkerStateEvent event) -> {
            logger.log(Level.INFO, "Mining finished");
            this.loadAll();
        });
        mining.setOnFailed((WorkerStateEvent event) -> {
            logger.log(Level.SEVERE, "Mining failed");
            if (event.getSource().getException() != null) {
                logger.log(Level.SEVERE, event.getSource().getException().toString());
            }
        });
        mining.setOnRunning((WorkerStateEvent event) -> {
            logger.log(Level.INFO, "Mining service started");
        });
        mining.setOnCancelled((WorkerStateEvent event) -> {
            logger.log(Level.INFO, "Mining canceled");
        });

    }

    public void mineStart(String hostname) {
        if (!this.mining.isRunning()) {
            logger.log(Level.INFO, "Request mining on " + hostname);
            this.clearData();
            this.hostname.set(hostname);
            this.mining.setHostname(hostname);
            this.mining.setMaxDepth(this.maxDepth.get());
            this.mining.start();
        }
    }

    public void mineStop() {
        if (this.mining.isRunning()) {
            mining.cancel();
        }
    }

    private void clearData() {
    }

    //GETs & SETs
    public ObservableList<Resource> resources() {
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
    
    public StringProperty dbHost(){
        return this.dbHost;
    }
    
    public StringProperty dbUser(){
        return this.dbUser;
    }
    
    public StringProperty dbPassword(){
        return this.dbPassword;
    }
    
    public String getDbHost(){
        return this.dbHost.get();
    }
    
    public String getDbUser(){
        return this.dbUser.get();
    }
    
    public String getDbPassword(){
        return this.dbPassword.get();
    }
    
    public void setDbHost(String host){
        this.dbHost.set(host);
    }

    public void setDbUser(String user){
        this.dbUser.set(user);
    }
    
    public void setDbPassword(String password){
        this.dbPassword.set(password);
    }
}
