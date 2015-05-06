package reaper.model;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.core.exception.OStorageException;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdgeType;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * This class should provide all necesarry Database operations. all methods are
 * expecting database to be connected
 *
 * @author nikita.vanku
 */
public class ReaperDatabase {

    private static final Logger loggerReaper = Logger.getLogger(reaper.Reaper.class.getName());

    private OrientGraphFactory factory;
    private final BooleanProperty connectionStatus;
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DBConf dbConf;

    ReaperDatabase() {
        this.connectionStatus = new SimpleBooleanProperty(false);
        factory = null;
    }

    public void connect(String host, String user, String password) throws IOException {
        if (isConnected()) {
            this.disconnect();
        }

        this.dbConf = new DBConf(host, user, password);

        this.factory = new OrientGraphFactory(host, user, password).setupPool(1, 10);
        this.connectionStatus.set(true);

    }

    public void disconnect() {
        this.factory.close();
        this.connectionStatus.set(false);
    }

    /**
     * Create database on server
     */
    public void setupSchema() {
        try {
            OServerAdmin serverAdmin = new OServerAdmin(dbConf.getHostname())
                    .connect(dbConf.getUsername(), dbConf.getPassword());
            if (!serverAdmin.existsDatabase("plocal")) {
                serverAdmin.createDatabase("ReaperTest", "graph", "plocal");
            }
        } catch (IOException ex) {
            loggerReaper.log(Level.SEVERE, ex.getMessage());
            return;
        }

        loggerReaper.log(Level.INFO, "Database tables creation started, this could take a moment");

        // Create Graph scheme
        OrientGraphNoTx graph = factory.getNoTx();
        try {
            //Vertex Resource
            OrientVertexType resource = graph.createVertexType("Resource");
            resource.createProperty("code", OType.INTEGER);
            resource.createProperty("downloadTime", OType.LONG);
            resource.createProperty("mimeType", OType.STRING);
            resource.createProperty("type", OType.STRING);
            resource.createProperty("url", OType.STRING);
            //Problems with multiple clusters
            //resource.createIndex("Resource.url", OClass.INDEX_TYPE.UNIQUE, "url");

            //Vertex Form
            OrientVertexType form = graph.createVertexType("Form");
            form.createProperty("action", OType.STRING);
            form.createProperty("method", OType.STRING);

            //Vertex Project
            OrientVertexType project = graph.createVertexType("Project");
            project.createProperty("cluster", OType.STRING);
            project.createProperty("date", OType.DATETIME);
            project.createProperty("domain", OType.STRING);
            project.createProperty("name", OType.STRING);

            //Vertex Blacklist
            OrientVertexType blacklist = graph.createVertexType("BlackWhitelist");
            blacklist.createProperty("url", OType.STRING);
            blacklist.createProperty("type", OType.STRING);

            //Edge LinkTo
            OrientEdgeType linkTo = graph.createEdgeType("LinkTo");
            linkTo.createProperty("count", OType.INTEGER);
            linkTo.createProperty("path", OType.STRING);
            linkTo.createProperty("type", OType.STRING);

            //Edge Includes
            OrientEdgeType includes = graph.createEdgeType("Includes");

            //Edge Root
            OrientEdgeType root = graph.createEdgeType("Root");
        } catch (OSchemaException ex) {
            loggerReaper.log(Level.SEVERE, "Some classes already exists in database");
        } finally {
            graph.shutdown();
        }

        ODatabaseDocumentTx oDB = factory.getDatabase();
        try {
            //Link Queue
            OClass linkQue = oDB.getMetadata().getSchema().createClass("LinkQue");
            linkQue.createProperty("depth", OType.INTEGER);
            linkQue.createProperty("from", OType.STRING);
            linkQue.createProperty("path", OType.STRING);
            linkQue.createProperty("type", OType.STRING);
            linkQue.createProperty("count", OType.INTEGER);
            linkQue.createProperty("position", OType.INTEGER);
            //TODO: investigate issue
            //linkQue.createIndex("LinkQue.position", OClass.INDEX_TYPE.UNIQUE, "position");
        } finally {
            oDB.close();
        }

    }

    /**
     * Drop whole database
     */
    public void tearDown() {
        try {
            OServerAdmin serverAdmin = new OServerAdmin(dbConf.getHostname())
                    .connect(dbConf.getUsername(), dbConf.getPassword());
            serverAdmin.dropDatabase("plocal");
        } catch (IOException | OStorageException ex) {
            loggerReaper.log(Level.SEVERE, ex.getMessage());
        }

    }

    /**
     * @deprecated only for testing reasons
     */
    public void truncateData() {
        OrientGraphNoTx graph = factory.getNoTx();
        try {
            long resourcesModified = graph.command(new OCommandSQL("TRUNCATE class Resource")).execute();
            loggerReaper.log(Level.INFO, String.valueOf(resourcesModified) + " Resources deleted.");
            long linksModified = graph.command(new OCommandSQL("TRUNCATE class LinkTo")).execute();
            loggerReaper.log(Level.INFO, String.valueOf(linksModified) + " Links deleted.");
            long formsModified = graph.command(new OCommandSQL("TRUNCATE class Resource")).execute();
            loggerReaper.log(Level.INFO, String.valueOf(formsModified) + " Forms deleted");
            long queModified = graph.command(new OCommandSQL("TRUNCATE class LinkQue")).execute();
            loggerReaper.log(Level.INFO, String.valueOf(queModified) + " link in que deleted");
            long projectModified = graph.command(new OCommandSQL("TRUNCATE class Project")).execute();
            loggerReaper.log(Level.INFO, String.valueOf(projectModified) + " Projects deleted");
            long blacklistModified = graph.command(new OCommandSQL("TRUNCATE class BlackWhitelist")).execute();
            loggerReaper.log(Level.INFO, String.valueOf(blacklistModified) + " Blacklist items deleted");
        } finally {
            graph.shutdown();
            loggerReaper.log(Level.INFO, "Database truncated");
        }
    }

    /**
     * Return connection status.
     * see if needed
     * @return bool
     */
    public boolean isConnected() {
        return this.connectionStatus.get();
    }

    public BooleanProperty connectionStatus() {
        return this.connectionStatus;
    }

    /**
     * Return Graph factory for you nasty queries.
     * Don't forget to graph.shutdown() or db.close!!!
     * @return OrientGraphFactory
     */
    public OrientGraphFactory getDatabase() {
        return this.factory;
    }

    public void loadResource(Object id, ObservableMap<String, Resource> resources, ObservableList<Link> links, String cluster) {
        OrientGraph graph = factory.getTx();
        try {
            OrientVertex ver = graph.getVertex(id);
            try {
                Resource res = ResourceFactory.resourceFromVector(ver);
                resources.put(ver.getId().toString(), res);
                for (Edge edge : ver.getEdges(Direction.OUT, "LinkTo")) {
                    Link link = new Link(edge.getProperty("path").toString(), res, LinkType.valueOf(edge.getProperty("type").toString()));
                    link.setCount((int) edge.getProperty("count"));
                    link.setFromResource(res);
                    Vertex toVer = edge.getVertex(Direction.IN);
                    Resource toRes = resources.get(toVer.getId().toString());
                    if (toRes == null) {
                        toRes = ResourceFactory.resourceFromVector(toVer);
                        resources.put(toVer.getId().toString(), toRes);
                    }
                    link.setToResource(toRes);
                    res.links().add(link);
                    links.add(link);
                }

                for (Edge edge : ver.getEdges(Direction.IN, "LinkTo")) {
                    Link link = new Link(edge.getProperty("path").toString(), res, LinkType.valueOf(edge.getProperty("type").toString()));
                    link.setCount((int) edge.getProperty("count"));
                    link.setToResource(res);
                    Vertex fromVer = edge.getVertex(Direction.OUT);
                    Resource fromRes = resources.get(fromVer.getId().toString());
                    if (fromRes == null) {
                        fromRes = ResourceFactory.resourceFromVector(fromVer);
                        resources.put(fromVer.getId().toString(), fromRes);
                    }
                    link.setFromResource(fromRes);
                    links.add(link);
                }
            } catch (MalformedURLException ex) {
                loggerReaper.log(Level.SEVERE, null, ex);
            }
        } finally {
            graph.shutdown();
        }
    }

    public void loadAll(ObservableMap<String, Resource> resources, ObservableList<Link> links, String cluster) {
        OrientGraph graph = factory.getTx();
        try {
            for (Vertex ver : (Iterable<Vertex>) graph.command(
                    new OCommandSQL("SELECT * FROM " + DatabaseClasses.PROJECT.getName() + cluster)
            ).execute()) {
                Resource res = resources.get(ver.getId().toString());
                if (res == null) {
                    try {
                        res = ResourceFactory.resourceFromVector(ver);
                        resources.put(ver.getId().toString(), res);
                    } catch (MalformedURLException ex) {
                        loggerReaper.log(Level.SEVERE, ex.toString());
                        return;
                    }
                }
            }
            //FIXME
            for (Edge edge : (Iterable<Edge>) graph.command(
                    new OCommandSQL("SELECT * FROM " + DatabaseClasses.LINKTO.getName() + cluster)
            ).execute()) {
                Link link = new Link(edge.getProperty("path").toString(),
                        resources.get(edge.getVertex(Direction.OUT).getId().toString()),
                        resources.get(edge.getVertex(Direction.IN).getId().toString()),
                        LinkType.valueOf(edge.getProperty("type").toString()));
                link.setCount((int) edge.getProperty("count"));
                links.add(link);
            }
        } finally {
            graph.shutdown();
        }
    }

    public void getProjects(ObservableList<Project> projects) {
        OrientGraph graph = factory.getTx();
        try {
            for (Vertex ver : graph.getVerticesOfClass(DatabaseClasses.PROJECT.getName(), false)) {
                Project proj;
                try {
                    proj = new Project(ver);
                    projects.add(proj);
                } catch (MalformedURLException | ParseException ex) {
                    loggerReaper.log(Level.SEVERE, ex.getMessage());
                }
            }
        } finally {
            graph.shutdown();
        }
    }

    public void createProject(String name, URL domain, int depth, List<URL> blacklist, List<URL> whitelist) {
        Project project = new Project(name, domain, depth, blacklist, whitelist);

        //save project into database and create clusters
        OrientGraph graph = factory.getTx();
        try {
            project.vertexTransaction(graph);
        } finally {
            graph.shutdown();
        }
    }

    public void removeProject(Project proj) {
        OrientGraph graph = factory.getTx();
        try {
            proj.remove(graph);
        } finally {
            graph.shutdown();
        }
    }

    public void truncateProject(Project proj) {
        OrientGraph graph = factory.getTx();
        try {
            proj.truncate(graph);
        } finally {
            graph.shutdown();
        }
    }
}
