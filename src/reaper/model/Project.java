package reaper.model;

import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
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

    public static SimpleDateFormat clusterDate = new SimpleDateFormat("yyyy_MM_dd");
    public static SimpleDateFormat niceDate = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

    Project(String name, URL domain, Date date, String cluster) {
        this.domain = domain;
        this.name = name;
        this.date = date;
        this.cluster = cluster;
    }

    Project(String name, URL domain) {
        this.name = name;
        this.domain = domain;

        Date dt = new Date();
        this.date = dt;
        this.cluster = "p";
        this.cluster += UUID.randomUUID().toString();
    }

    Project(Vertex ver) throws MalformedURLException, ParseException {
        super(ver);

        this.cluster = ver.getProperty("cluster");
        this.name = ver.getProperty("name");

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
                "domain", domain.toString(), "cluster", cluster);
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
        Vertex ver = graph.getVertex(getID());
        ver.setProperty("name", name);
        ver.setProperty("date", date);
        ver.setProperty("domain", domain.toString());
        ver.setProperty("cluster", cluster);
    }

    @Override
    public void remove(OrientGraph graph) {
        //Remove project clusters
        new OCommandSQL(
                "DROP CLUSTER " + DatabaseClasses.RESOURCE.getName() + getCluster()
        ).execute();
        new OCommandSQL(
                "DROP CLUSTER " + DatabaseClasses.FORM.getName() + getCluster()
        ).execute();
        new OCommandSQL(
                "DROP CLUSTER " + DatabaseClasses.BLACKLIST.getName() + getCluster()
        ).execute();
        new OCommandSQL(
                "DROP CLUSTER " + DatabaseClasses.LINKTO.getName() + getCluster()
        ).execute();
        new OCommandSQL(
                "DROP CLUSTER " + DatabaseClasses.INCLUDES.getName() + getCluster()
        ).execute();
        new OCommandSQL(
                "DROP CLUSTER " + DatabaseClasses.LINKQUE.getName() + getCluster()
        ).execute();

        //remove Project entry
        super.remove(graph);
    }
    
    public void truncate(OrientGraph graph) {
        //Truncate project clusters
        new OCommandSQL(
                "TRUNCATE CLUSTER " + DatabaseClasses.RESOURCE.getName() + getCluster()
        ).execute();
        new OCommandSQL(
                "TRUNCATE CLUSTER " + DatabaseClasses.FORM.getName() + getCluster()
        ).execute();
        new OCommandSQL(
                "TRUNCATE CLUSTER " + DatabaseClasses.BLACKLIST.getName() + getCluster()
        ).execute();
        new OCommandSQL(
                "TRUNCATE CLUSTER " + DatabaseClasses.LINKTO.getName() + getCluster()
        ).execute();
        new OCommandSQL(
                "TRUNCATE CLUSTER " + DatabaseClasses.INCLUDES.getName() + getCluster()
        ).execute();
        new OCommandSQL(
                "TRUNCATE CLUSTER " + DatabaseClasses.LINKQUE.getName() + getCluster()
        ).execute();
    }
}
