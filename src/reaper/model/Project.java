package reaper.model;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author nikita.vanku
 */
public class Project extends VertexAbstract {

    private URL domain;
    private Date date;
    private String name;
    private String cluster;
    private Object rootID;
    private int depth;

    public static SimpleDateFormat clusterDate = new SimpleDateFormat("yyyy_MM_dd");
    public static SimpleDateFormat niceDate = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

    Project(String name, URL domain, Date date, String cluster, int depth) {
        this.domain = domain;
        this.name = name;
        this.date = date;
        this.cluster = cluster;
        this.depth = depth;
    }

    Project(String name, URL domain, int depth) {
        this.name = name;
        this.domain = domain;
        this.depth = depth;

        Date dt = new Date();
        this.date = dt;
        this.cluster = "p";
        this.cluster += UUID.randomUUID().toString();
    }

    Project(Vertex ver) throws MalformedURLException, ParseException {
        super(ver);

        this.cluster = ver.getProperty("cluster");
        this.name = ver.getProperty("name");
        this.depth = ver.getProperty("depth");
        this.date = ver.getProperty("date");
        this.domain = new URL(ver.getProperty("domain"));

        this.rootID = null;
        for (Vertex root : ver.getVertices(Direction.OUT, "Root")) {
            this.rootID = root.getId();
        }

    }

    /**
     * Inserts project into database
     *
     * @param graph
     */
    public void vertexTransaction(OrientGraph graph) {
        Vertex ver = graph.addVertex("class:" + DatabaseClasses.PROJECT.getName(),
                "name", name, "date", date,
                "domain", domain.toString(), "cluster", cluster, "depth", depth);
        graph.commit();
        setID(ver.getId());
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public URL getDomain() {
        return this.domain;
    }

    public void setDomain(URL url) {
        this.domain = url;
    }

    public String getCluster() {
        return this.cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
    
    public int getDepth(){
        return this.depth;
    }
    
    public void setDepth(int depth){
        this.depth = depth;
    }

    /**
     * Return root resource from Project
     *
     * @return Object Id or null
     */
    public Object getRoot() {
        return rootID;
    }

    @Override
    public void save(OrientGraph graph) {
        OrientVertex ver = graph.getVertex(getID());
        ver.setProperties("name", name, "date", date, 
                "domain", domain.toString(), "cluster", cluster, "depth", depth);
    }

    @Override
    public void remove(OrientGraph graph) {

        graph.command(
                new OCommandSQL(
                        "ALTER CLASS " + DatabaseClasses.RESOURCE.getName() + " REMOVECLUSTER "
                        + DatabaseClasses.RESOURCE.getName() + getCluster())
        ).execute();
        graph.command(
                new OCommandSQL("DROP CLUSTER " + DatabaseClasses.RESOURCE.getName() + getCluster())
        ).execute();
        
        graph.command(
                new OCommandSQL(
                        "ALTER CLASS " + DatabaseClasses.FORM.getName() + " REMOVECLUSTER "
                        + DatabaseClasses.FORM.getName() + getCluster())
        ).execute();
        graph.command(
                new OCommandSQL("DROP CLUSTER " + DatabaseClasses.FORM.getName() + getCluster())
        ).execute();
        
        graph.command(
                new OCommandSQL(
                        "ALTER CLASS " + DatabaseClasses.LINKTO.getName() + " REMOVECLUSTER "
                        + DatabaseClasses.LINKTO.getName() + getCluster())
        ).execute();
        graph.command(
                new OCommandSQL("DROP CLUSTER " + DatabaseClasses.LINKTO.getName() + getCluster())
        ).execute();
        
        graph.command(
                new OCommandSQL(
                        "ALTER CLASS " + DatabaseClasses.LINKQUE.getName() + " REMOVECLUSTER "
                        + DatabaseClasses.LINKQUE.getName() + getCluster())
        ).execute();
        graph.command(
                new OCommandSQL("DROP CLUSTER " + DatabaseClasses.LINKQUE.getName() + getCluster())
        ).execute();
        
        graph.command(
                new OCommandSQL(
                        "ALTER CLASS " + DatabaseClasses.INCLUDES.getName() + " REMOVECLUSTER "
                        + DatabaseClasses.INCLUDES.getName() + getCluster())
        ).execute();
        graph.command(
                new OCommandSQL("DROP CLUSTER " + DatabaseClasses.INCLUDES.getName() + getCluster())
        ).execute();
        
        graph.command(
                new OCommandSQL(
                        "ALTER CLASS " + DatabaseClasses.BLACKLIST.getName() + " REMOVECLUSTER "
                        + DatabaseClasses.BLACKLIST.getName() + getCluster())
        ).execute();
        graph.command(
                new OCommandSQL("DROP CLUSTER " + DatabaseClasses.BLACKLIST.getName() + getCluster())
        ).execute();
        super.remove(graph);
    }

    public void truncate(OrientGraph graph) {
        graph.command(
                new OCommandSQL("TRUNCATE CLUSTER " + DatabaseClasses.RESOURCE.getName() + getCluster())
        ).execute();
        graph.command(
                new OCommandSQL("TRUNCATE CLUSTER " + DatabaseClasses.FORM.getName() + getCluster())
        ).execute();
        graph.command(
                new OCommandSQL("TRUNCATE CLUSTER " + DatabaseClasses.LINKTO.getName() + getCluster())
        ).execute();
        graph.command(
                new OCommandSQL("TRUNCATE CLUSTER " + DatabaseClasses.LINKQUE.getName() + getCluster())
        ).execute();
        graph.command(
                new OCommandSQL("TRUNCATE CLUSTER " + DatabaseClasses.INCLUDES.getName() + getCluster())
        ).execute();
    }

    @Override
    public void update(OrientGraph graph) {
        OrientVertex vertex = graph.getVertex(id);
        
        cluster = vertex.getProperty("cluster");
        date = vertex.getProperty("date");
        name = vertex.getProperty("name");
        try {
            domain = new URL(vertex.getProperty("domain"));
        } catch (MalformedURLException ex) {
            domain = null;
        }
        for (Vertex root : vertex.getVertices(Direction.OUT, "Root")) {
            rootID = root.getId();
        }
    }
}
