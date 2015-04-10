package reaper.model;

import com.orientechnologies.orient.core.exception.OStorageException;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.jsoup.UnsupportedMimeTypeException;
import reaper.exceptions.OutsidePageException;

/**
 *
 * @author zaraka
 */
public class MinerService extends Service<Void> {

    private static final Logger logger = Logger.getLogger(reaper.Reaper.class.getName());

    private String hostname;
    private int maxDepth;
    private ArrayList<Link> linksQueue;
    private Map<String, String> resources;
    private OrientGraphFactory graphFactory;
    private int resourceCount, linksCount;
    private Object rootId;
    private LinkQue linkScrambler;
    private String cluster;

    public void init() {
        this.hostname = "";
        this.maxDepth = 5;
        this.linksQueue = new ArrayList<>();
        this.resources = new HashMap<>();
        this.cluster = "";
        

        this.resourceCount = 0;
        this.linksCount = 0;
        this.rootId = null;

        this.graphFactory = null;
        
        this.linkScrambler = new LinkQue(graphFactory);

    }
    
    public void prepare(String hostname, String cluster, int maxDepth){
        this.hostname = hostname;
        this.cluster = cluster;
        this.maxDepth = maxDepth;
    }
    
    public void databaseConnect(String host, String user, String pass){
        this.databaseDisconnect();
        
        try {
            graphFactory = new OrientGraphFactory(host, user, pass).setupPool(1, 10);
        } catch (OStorageException ex) {
            throw ex;
        }
    }
    
    public void databaseDisconnect(){
        if(graphFactory != null){
            graphFactory.close();
        }
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getResourceCount() {
        return this.resourceCount;
    }

    public int getLinksCount() {
        return this.linksCount;
    }

    public Object getRootId() {
        return this.rootId;
    }

    private void createSingleVertex(Resource res) {
        OrientGraph graph = graphFactory.getTx();
        try {
            res.vertexTransaction(graph, cluster);
            resourceCount++;
            resources.put(res.getURL().toString(), res.getVertexID().toString());
        } finally {
            graph.shutdown();
        }
    }

    private void linkTransaction(Link link) {
        OrientGraph graph = graphFactory.getTx();
        try {
            OrientVertex from, to;
            String fromID, toID;
            fromID = resources.get(link.getFromURL());
            if (fromID == null) {
                System.out.println("error");
                return;
            }
            toID = resources.get(link.getToURL());
            if (toID == null) {
                System.out.println("error");
                return;
            }
            from = graph.getVertex(fromID);
            to = graph.getVertex(toID);
            from.addEdge("LinkTo", to, null, null, "path", link.getLink(),
                    "count", link.getCount(), "type", link.getType().toString());
            linksCount += link.getCount();
        } finally {
            graph.shutdown();
        }
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                String url = hostname;
                if (!url.matches("^.*:\\/\\/.*$")) {
                    url = "http://" + url;
                    Platform.runLater(() -> {
                        logger.log(Level.WARNING, "You should provide protocol as well. Default proctol http is used.");
                    });
                }

                try {
                    URL uURL = new URL(url);
                    ResourceDom root = new ResourceDom(uURL, 0, maxDepth);
                    linksQueue.addAll(root.links());
                    for (Link queLink : root.links()) {
                        linkScrambler.linkEnter(cluster, queLink.getLink(), queLink.getFromURL(), queLink.getFromResource().getDepth());
                    }
                    createSingleVertex(root);
                    rootId = root.getVertexID();
                } catch (UnsupportedMimeTypeException | MalformedURLException ex) {
                    throw ex;
                }
                while (linkScrambler.queLength(cluster) > 0) {
                    ODocument docLink = linkScrambler.linkLeave(cluster);
                    int docDepth = docLink.field("depth");
                    Link link = new Link(docLink.field("from").toString(), docLink.field("path").toString());
                    if (resources.get(link.getToURL()) == null) {
                        Resource toRes;
                        URL parentURL = new URL(link.getFromURL());
                        URL linkUrl = new URL(parentURL, link.getLink());
                        try {
                            toRes = new ResourceDom(linkUrl, docDepth + 1, maxDepth);
                            if (toRes.getDepth() < maxDepth) {
                                for (Link queLink : toRes.links()) {
                                    linkScrambler.linkEnter(cluster, queLink.getLink(), queLink.getFromURL(), queLink.getFromResource().getDepth());
                                }
                            }
                        } catch (UnsupportedMimeTypeException ex) {
                            try {
                                toRes = new ResourceFile(linkUrl, docDepth + 1, maxDepth);
                            } catch (MalformedURLException ex1) {
                                throw ex1;
                            }
                        } catch (OutsidePageException ex) {
                            toRes = new ResourceOutside(linkUrl, docDepth + 1, maxDepth);
                        } catch (MalformedURLException ex) {
                            throw ex;
                        }
                        link.setToResource(toRes);
                        createSingleVertex(toRes);
                    }
                    linkTransaction(link);
                }
                return null;
            }
        };
    }

}
