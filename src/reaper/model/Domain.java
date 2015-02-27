package reaper.model;

import com.orientechnologies.orient.core.exception.OStorageException;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import java.net.MalformedURLException;
import java.net.URL;
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
    private Object rootId;

    private final MinerService mining;

    public Domain() {
        this.resources = FXCollections.observableHashMap();
        this.links = FXCollections.observableArrayList();
        this.hostname = new SimpleStringProperty("");
        this.maxDepth = new SimpleIntegerProperty(1);
        this.maxDownloads = new SimpleIntegerProperty(5);
        this.mining = new MinerService();
        this.dbHost = new SimpleStringProperty("remote:localhost/ReaperTest");
        this.dbPassword = new SimpleStringProperty("admin");
        this.dbUser = new SimpleStringProperty("admin");
        this.resourcesCount = new SimpleIntegerProperty(0);
        this.linksCount = new SimpleIntegerProperty(0);
        this.blacklist = FXCollections.observableArrayList();
        this.rootId = null;

        this.init();
    }

    public void dataReset() {
        if (this.mining.isRunning()) {
            logger.log(Level.WARNING, "Cant clear data while miner is running");
            return;
        }

        OrientGraph graph = new OrientGraph(this.getDbHost(), this.getDbUser(), this.getDbPassword());
        try {
            long resourcesModified = graph.command(new OCommandSQL("TRUNCATE class Resource")).execute();
            logger.log(Level.INFO, String.valueOf(resourcesModified) + " Resources deleted.");
            long linksModified = graph.command(new OCommandSQL("TRUNCATE class LinkTo")).execute();
            logger.log(Level.INFO, String.valueOf(linksModified) + " Links deleted.");
            long formsModified = graph.command(new OCommandSQL("TRUNCATE class Resource")).execute();
            logger.log(Level.INFO, String.valueOf(formsModified) + " Forms deleted");
            long queModified = graph.command(new OCommandSQL("TRUNCATE class LinkQue")).execute();
            logger.log(Level.INFO, String.valueOf(queModified) + " link in que deleted");
            
        } finally {
            graph.shutdown();
            logger.log(Level.INFO, "Database truncated");
        }
    }

    public void loadAll() {
        this.clearData();
        OrientGraph graph = new OrientGraph(this.getDbHost(), this.getDbUser(), this.getDbPassword());
        try {
            for (Vertex ver : graph.getVerticesOfClass("Resource", false)) {
                Resource res = this.resources.get(ver.getId().toString());
                if (res == null) {
                    try {
                        res = ResourceFactory.resourceFromVector(ver);
                        this.resources.put(ver.getId().toString(), res);
                    } catch (MalformedURLException ex) {
                        logger.log(Level.SEVERE, ex.toString());
                        return;
                    }
                }
            }

            for (Edge edge : graph.getEdgesOfClass("LinkTo", false)) {
                Link link = new Link(edge.getProperty("path").toString(),
                        this.resources.get(edge.getVertex(Direction.OUT).getId().toString()),
                        this.resources.get(edge.getVertex(Direction.IN).getId().toString()),
                        LinkType.valueOf(edge.getProperty("type").toString()));
                link.setCount((int) edge.getProperty("count"));
                this.links.add(link);
            }
        } finally {
            graph.shutdown();
        }
    }

    public void loadRoot() {
        if (rootId != null) {
            this.loadResource(rootId);
        }
    }

    public void loadResource(Object id) {
        this.clearData();
        OrientGraph graph = new OrientGraph(this.getDbHost(), this.getDbUser(), this.getDbPassword());
        try {
            OrientVertex ver = graph.getVertex(id);
            try {
                Resource res = ResourceFactory.resourceFromVector(ver);
                this.resources.put(ver.getId().toString(), res);
                for (Edge edge : ver.getEdges(Direction.OUT, "LinkTo")) {
                    Link link = new Link(edge.getProperty("path").toString(), res, LinkType.valueOf(edge.getProperty("type").toString()));
                    link.setCount((int) edge.getProperty("count"));
                    link.setFromResource(res);
                    Vertex toVer = edge.getVertex(Direction.IN);
                    Resource toRes = this.resources.get(toVer.getId().toString());
                    if (toRes == null) {
                        toRes = ResourceFactory.resourceFromVector(toVer);
                        this.resources.put(toVer.getId().toString(), toRes);
                    }
                    link.setToResource(toRes);
                    res.links().add(link);
                    this.links.add(link);
                }

                for (Edge edge : ver.getEdges(Direction.IN, "LinkTo")) {
                    Link link = new Link(edge.getProperty("path").toString(), res, LinkType.valueOf(edge.getProperty("type").toString()));
                    link.setCount((int) edge.getProperty("count"));
                    link.setToResource(res);
                    Vertex fromVer = edge.getVertex(Direction.OUT);
                    Resource fromRes = this.resources.get(fromVer.getId().toString());
                    if (fromRes == null) {
                        fromRes = ResourceFactory.resourceFromVector(fromVer);
                        this.resources.put(fromVer.getId().toString(), fromRes);
                    }
                    link.setFromResource(fromRes);
                    this.links.add(link);
                }
            } catch (MalformedURLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } finally {
            graph.shutdown();
        }
    }

    private void init() {
        try {
            mining.init(this.getHostname(), this.getMaxDepth(),
                    this.getDbHost(), this.getDbUser(), this.getDbPassword());
        } catch (OStorageException ex) {
            logger.log(Level.SEVERE, ex.toString());
        }
        mining.setOnSucceeded((WorkerStateEvent event) -> {
            this.setResourcesCount(this.mining.getResourceCount());
            this.setLinksCount(this.mining.getLinksCount());
            this.rootId = this.mining.getRootId();
            this.mining.reset();
            logger.log(Level.INFO, "Mining finished");
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
    
    public Object getRootID(){
        return this.rootId;
    }
    
    public ObservableList<URL> blacklistProperty(){
        return this.blacklist;
    }
}
