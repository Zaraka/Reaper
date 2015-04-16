package reaper.model;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OStorageException;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.sql.OCommandSQL;
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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import reaper.Reaper;

/**
 * This class should provide all necesarry Database operations. all methods are
 * expecting database to be connected
 *
 * @author nikita.vanku
 */
public class ReaperDatabase {

    private static final Logger logger = Logger.getLogger(Reaper.class.getName());

    private OrientGraphFactory factory;
    private OServerAdmin serverAdmin;
    private final BooleanProperty connectionStatus;
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    ReaperDatabase() {
        this.connectionStatus = new SimpleBooleanProperty(false);
        factory = null;
    }

    public void connect(String host, String user, String password) throws IOException {
        if (isConnected()) {
            this.disconnect();
        }

        serverAdmin = new OServerAdmin(host);
        
        serverAdmin.connect(user, password);
        
        this.factory = new OrientGraphFactory(host, user, password).setupPool(1, 10);
        this.connectionStatus.set(true);

    }

    public void disconnect() {
        this.factory.close();
        this.connectionStatus.set(false);
    }

    public void setupSchema() {
        
        try {
            if(!serverAdmin.existsDatabase()){
                serverAdmin.createDatabase("ReaperTest","graph", "plocal");
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return;
        }
        
        
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
            resource.createIndex("Resource.url", OClass.INDEX_TYPE.UNIQUE, "url");

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
            OrientVertexType blacklist = graph.createVertexType("Blacklist");
            blacklist.createProperty("url", OType.STRING);

            //Edge LinkTo
            OrientEdgeType linkTo = graph.createEdgeType("LinkTo");
            linkTo.createProperty("count", OType.INTEGER);
            linkTo.createProperty("path", OType.STRING);
            linkTo.createProperty("type", OType.STRING);

            //Edge Includes
            OrientEdgeType includes = graph.createEdgeType("Includes");

            //Edge Root
            OrientEdgeType root = graph.createEdgeType("Root");

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
            linkQue.createProperty("position", OType.INTEGER);
            linkQue.createIndex("LinkQue.position", OClass.INDEX_TYPE.UNIQUE, "position");
        } finally {
            oDB.close();
        }

    }

    public void tearDown() {
        /*
        OrientGraphNoTx graph = factory.getNoTx();
        try {
            //Edges
            graph.dropEdgeType("Includes");
            graph.dropEdgeType("LinkTo");
            graph.dropEdgeType("Root");

            //Vertices
            graph.dropVertexType("Resource");
            graph.dropVertexType("Form");
            graph.dropVertexType("Project");
            graph.dropVertexType("Blacklist");
        } finally {
            graph.shutdown();
        }

        ODatabaseDocumentTx oDB = factory.getDatabase();
        try {
            oDB.getMetadata().getSchema().dropClass("LinkQue");
        } finally {
            oDB.close();
        }
        */
        
        try {
            serverAdmin.dropDatabase("remote");
        } catch (IOException | OStorageException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }
        
    }

    public void truncateData() {
        OrientGraphNoTx graph = factory.getNoTx();
        try {
            long resourcesModified = graph.command(new OCommandSQL("TRUNCATE class Resource")).execute();
            logger.log(Level.INFO, String.valueOf(resourcesModified) + " Resources deleted.");
            long linksModified = graph.command(new OCommandSQL("TRUNCATE class LinkTo")).execute();
            logger.log(Level.INFO, String.valueOf(linksModified) + " Links deleted.");
            long formsModified = graph.command(new OCommandSQL("TRUNCATE class Resource")).execute();
            logger.log(Level.INFO, String.valueOf(formsModified) + " Forms deleted");
            long queModified = graph.command(new OCommandSQL("TRUNCATE class LinkQue")).execute();
            logger.log(Level.INFO, String.valueOf(queModified) + " link in que deleted");
            long projectModified = graph.command(new OCommandSQL("TRUNCATE class Project")).execute();
            logger.log(Level.INFO, String.valueOf(projectModified) + " Projects deleted");
            long blacklistModified = graph.command(new OCommandSQL("TRUNCATE class Blacklist")).execute();
            logger.log(Level.INFO, String.valueOf(blacklistModified) + " Blacklist items deleted");

        } finally {
            graph.shutdown();
            logger.log(Level.INFO, "Database truncated");
        }
    }

    public boolean isConnected() {
        return this.connectionStatus.get();
    }

    public BooleanProperty connectionStatus() {
        return this.connectionStatus;
    }

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
                logger.log(Level.SEVERE, null, ex);
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
                        logger.log(Level.SEVERE, ex.toString());
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
                    logger.log(Level.SEVERE, ex.getMessage());
                }
            }
        } finally {
            graph.shutdown();
        }
    }

    public void loadBlacklist(ArrayList<URL> blacklist, String cluster) {
        OrientGraph graph = factory.getTx();
        try {
            for (Vertex ver : (Iterable<Vertex>) graph.command(
                    new OCommandSQL("SELECT * FROM " + DatabaseClasses.BLACKLIST.getName() + cluster)
            ).execute()) {
                try {
                    blacklist.add(new URL(ver.getProperty("url")));
                } catch (MalformedURLException ex) {
                    logger.log(Level.WARNING, "Invalid blacklisted link in database");
                }
            }
        } finally {
            graph.shutdown();
        }
    }

    public void saveBlacklist(ArrayList<URL> blacklist, String cluster) {
        OrientGraph graph = factory.getTx();
        try {
            for (URL url : blacklist) {
                Vertex item = graph.addVertex(
                        DatabaseClasses.BLACKLIST.getName(),
                        DatabaseClasses.BLACKLIST.getName() + cluster);
                item.setProperty("url", url.toString());
            }
        } finally {
            graph.shutdown();
        }
    }

    public void createProject(String name, URL domain, int depth, ArrayList<URL> blacklist) {
        Project project = new Project(name, domain, depth);

        //save project into database and create clusters
        OrientGraph graph = factory.getTx();
        try {
            project.vertexTransaction(graph);

            //Vertices clusters
            new OCommandSQL(
                    "ALTER CLASS Resource ADDCLUSTER " + DatabaseClasses.RESOURCE.getName() + project.getCluster()
            ).execute();
            new OCommandSQL(
                    "ALTER CLASS Form ADDCLUSTER " + DatabaseClasses.FORM.getName() + project.getCluster()
            ).execute();
            new OCommandSQL(
                    "ALTER CLASS Blacklist ADDCLUSTER " + DatabaseClasses.BLACKLIST.getName() + project.getCluster()
            ).execute();

            //Edges clusters
            new OCommandSQL(
                    "ALTER CLASS LinkTo ADDCLUSTER " + DatabaseClasses.LINKTO.getName() + project.getCluster()
            ).execute();

            new OCommandSQL(
                    "ALTER CLASS Includes ADDCLUSTER " + DatabaseClasses.INCLUDES.getName() + project.getCluster()
            ).execute();

            //Que cluster
            new OCommandSQL(
                    "ALTER CLASS LinkQue ADDCLUSTER " + DatabaseClasses.LINKQUE.getName() + project.getCluster()
            ).execute();

        } finally {
            graph.shutdown();
        }

        saveBlacklist(blacklist, project.getCluster());
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
