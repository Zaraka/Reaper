package reaper.model;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import reaper.Reaper;

/**
 *
 * @author nikita.vanku
 */
public class Project extends VertexAbstract {

    private static final Logger logger = Logger.getLogger(Reaper.class.getName());

    private URL domain;
    private Date date;
    private String name;
    private String cluster;
    private Object rootID;
    private List<URL> blacklist;
    private List<URL> whitelist;
    private int depth;

    public static SimpleDateFormat clusterDate = new SimpleDateFormat("yyyy_MM_dd");
    public static SimpleDateFormat niceDate = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

    Project(String name, URL domain, Date date, String cluster, int depth) {
        this.domain = domain;
        this.name = name;
        this.date = date;
        this.cluster = cluster;
        this.depth = depth;

        this.blacklist = new ArrayList<>();
        this.whitelist = new ArrayList<>();
    }

    Project(String name, URL domain, int depth, List<URL> blacklist, List<URL> whitelist) {
        this.name = name;
        this.domain = domain;
        this.depth = depth;

        Date dt = new Date();
        this.date = dt;
        this.cluster = "p";
        this.cluster += UUID.randomUUID().toString();

        this.blacklist = blacklist;
        this.whitelist = whitelist;
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

        this.blacklist = new ArrayList<>();
        this.whitelist = new ArrayList<>();
    }

    private void loadBlacklist(OrientGraph graph) {
        for (Vertex ver : (Iterable<Vertex>) graph.command(
                new OCommandSQL("SELECT * FROM cluster:"
                        + DatabaseClasses.BLACWHITEKLIST.getName()
                        + cluster + " WHERE type = 'BLACKLIST'")
        ).execute()) {
            try {
                blacklist.add(new URL(ver.getProperty("url")));
            } catch (MalformedURLException ex) {
                logger.log(Level.WARNING, "Invalid blacklisted link in database");
            }
        }
    }

    private void loadWhitelist(OrientGraph graph) {
        for (Vertex ver : (Iterable<Vertex>) graph.command(
                new OCommandSQL("SELECT * FROM cluster:"
                        + DatabaseClasses.BLACWHITEKLIST.getName()
                        + cluster + " WHERE type = 'WHITELIST'")
        ).execute()) {
            try {
                whitelist.add(new URL(ver.getProperty("url")));
            } catch (MalformedURLException ex) {
                logger.log(Level.WARNING, "Invalid whitelisted link in database");
            }
        }
    }

    private void saveBlackWhiteList(OrientGraph graph, List<URL> blackWhiteList, String cluster, String type) {
        try {
            for (URL url : blackWhiteList) {
                Vertex item = graph.addVertex(
                        DatabaseClasses.BLACWHITEKLIST.getName(),
                        DatabaseClasses.BLACWHITEKLIST.getName() + cluster);
                item.setProperty("url", url.toString());
                item.setProperty("type", type);
            }
        } finally {
            graph.shutdown();
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

        saveBlackWhiteList(graph, blacklist, cluster, "BLACKLIST");
        saveBlackWhiteList(graph, whitelist, cluster, "WHITELIST");

        //Vertices clusters
        graph.command(new OCommandSQL(
                "ALTER CLASS " + DatabaseClasses.RESOURCE.getName()
                + " ADDCLUSTER " + DatabaseClasses.RESOURCE.getName() + getCluster()
        )).execute();
        graph.command(new OCommandSQL(
                "ALTER CLASS " + DatabaseClasses.FORM.getName()
                + " ADDCLUSTER " + DatabaseClasses.FORM.getName() + getCluster()
        )).execute();
        graph.command(new OCommandSQL(
                "ALTER CLASS " + DatabaseClasses.FORM.getName()
                + " ADDCLUSTER " + DatabaseClasses.FORM.getName() + getCluster()
        )).execute();
        graph.command(new OCommandSQL(
                "ALTER CLASS " + DatabaseClasses.BLACWHITEKLIST.getName()
                + " ADDCLUSTER " + DatabaseClasses.BLACWHITEKLIST.getName() + getCluster()
        )).execute();

        //Edges clusters
        graph.command(new OCommandSQL(
                "ALTER CLASS " + DatabaseClasses.LINKTO.getName()
                + " ADDCLUSTER " + DatabaseClasses.LINKTO.getName() + getCluster()
        )).execute();

        graph.command(new OCommandSQL(
                "ALTER CLASS " + DatabaseClasses.INCLUDES.getName()
                + " ADDCLUSTER " + DatabaseClasses.INCLUDES.getName() + getCluster()
        )).execute();

        //Que cluster
        graph.command(new OCommandSQL(
                "ALTER CLASS " + DatabaseClasses.LINKQUE.getName()
                + " ADDCLUSTER " + DatabaseClasses.LINKQUE.getName() + getCluster()
        )).execute();
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

    public int getDepth() {
        return this.depth;
    }

    public void setDepth(int depth) {
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
                        "ALTER CLASS " + DatabaseClasses.BLACWHITEKLIST.getName() + " REMOVECLUSTER "
                        + DatabaseClasses.BLACWHITEKLIST.getName() + getCluster())
        ).execute();
        graph.command(
                new OCommandSQL("DROP CLUSTER " + DatabaseClasses.BLACWHITEKLIST.getName() + getCluster())
        ).execute();
        super.remove(graph);
    }

    public void truncate(OrientGraph graph) {

        //Delete Root connection
        OrientVertex ver = graph.getVertex(id);
        for (Edge edge : ver.getEdges(Direction.OUT, "Root")) {
            graph.removeEdge(edge);
        }

        //Truncate clusters
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
        rootID = null;
        for (Vertex root : vertex.getVertices(Direction.OUT, "Root")) {
            rootID = root.getId();
        }

        blacklist.clear();
        whitelist.clear();
        loadBlacklist(graph);
        loadWhitelist(graph);
    }

    public long getResourceCount(ODatabaseDocumentTx oDB) {
        return oDB.countClusterElements(
                DatabaseClasses.RESOURCE.getName() + cluster
        );
    }

    public long getLinksCount(ODatabaseDocumentTx oDB) {
        return oDB.countClusterElements(
                DatabaseClasses.LINKTO.getName() + cluster
        );
    }

    public List<URL> getBlacklist() {
        return blacklist;
    }

    public List<URL> getWhitelist() {
        return whitelist;
    }

    public void setBlacklist(List<URL> blacklist) {
        this.blacklist = blacklist;
    }

    public void setWhitelist(List<URL> whitelist) {
        this.whitelist = whitelist;
    }

    public Map<String, Long> getStatsTypes(OrientGraph graph) {
        HashMap<String, Long> result = new HashMap<>();
        List<ODocument> dom = graph.getRawGraph().query(
                new OSQLSynchQuery<>("select count(*) from cluster:"
                        + DatabaseClasses.RESOURCE.getName()
                        + getCluster()
                        + " WHERE type = 'DOM'")
        );
        List<ODocument> outside = graph.getRawGraph().query(
                new OSQLSynchQuery<>("select count(*) from cluster:"
                        + DatabaseClasses.RESOURCE.getName()
                        + getCluster()
                        + " WHERE type = 'OUTSIDE'")
        );
        List<ODocument> file = graph.getRawGraph().query(
                new OSQLSynchQuery<>("select count(*) from cluster:"
                        + DatabaseClasses.RESOURCE.getName()
                        + getCluster()
                        + " WHERE type = 'FILE'")
        );
        result.put("DOM", (Long) dom.get(0).field("count"));
        result.put("OUTSIDE", (Long) outside.get(0).field("count"));
        result.put("FILE", (Long) file.get(0).field("count"));
        
        return result;
    }
    
    public Map<String, Long> getStatsCodes(OrientGraph graph) {
        HashMap<String, Long> result = new HashMap<>();
        List<ODocument> codesList = graph.getRawGraph().query(
                new OSQLSynchQuery<>("select code, count(*) from cluster:"
                        + DatabaseClasses.RESOURCE.getName()
                        + getCluster()
                        + " group by code")
        );
        for(ODocument doc : codesList){
            String name = doc.field("code").toString();
            if(name.equals("0")){
                name = "Not Scanned";
            }
            result.put(name, (Long)doc.field("count"));
        }
        return result;
    }
}
