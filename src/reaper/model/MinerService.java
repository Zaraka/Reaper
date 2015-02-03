package reaper.model;

import com.orientechnologies.orient.core.exception.OStorageException;
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
    private String databaseHost;
    private String databaseUser;
    private String databasePassword;
    private ArrayList<Link> linksQueue;
    private Map<String, String> resources;
    private OrientGraphFactory graphFactory;
    private int resourceCount, linksCount;
    private Object rootId;

    public void init(String hostname, int maxDepth, String dbHost, String dbUser, String dbPass) {
        this.hostname = hostname;
        this.maxDepth = maxDepth;
        this.linksQueue = new ArrayList<>();
        this.resources = new HashMap<>();
        this.databaseHost = dbHost;
        this.databasePassword = dbPass;
        this.databaseUser = dbUser;

        this.resourceCount = 0;
        this.linksCount = 0;
        this.rootId = null;

        try {
            this.graphFactory = new OrientGraphFactory(dbHost, dbUser, dbPass).setupPool(1, 10);
        } catch (OStorageException ex) {
            throw ex;
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

    private Link popLink() {
        if (this.linksQueue.size() < 1) {
            return null;
        }
        Link result = this.linksQueue.get(0);
        this.linksQueue.remove(0);
        return result;
    }

    private void createSingleVertex(Resource res) {
        OrientGraph graph = graphFactory.getTx();
        try {
            res.vertexTransaction(graph);
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
                    ResourceDom root = new ResourceDom(uURL, 0, maxDepth, null);
                    linksQueue.addAll(root.links());
                    createSingleVertex(root);
                    rootId = root.getVertexID();
                } catch (UnsupportedMimeTypeException | MalformedURLException ex) {
                    throw ex;
                }
                Link link;
                while ((link = popLink()) != null) {
                    if (resources.get(link.getToURL()) == null) {
                        Resource toRes;
                        URL linkUrl = new URL(link.getFromResource().getURL(), link.getLink());
                        try {
                            toRes = new ResourceDom(linkUrl, link.getFromResource().getDepth() + 1, maxDepth, link.getFromResource());
                            if (toRes.getDepth() < maxDepth) {
                                linksQueue.addAll(toRes.links());
                            }
                        } catch (UnsupportedMimeTypeException ex) {
                            try {
                                toRes = new ResourceFile(linkUrl, link.getFromResource().getDepth() + 1, maxDepth, link.getFromResource());
                            } catch (MalformedURLException ex1) {
                                throw ex1;
                            }
                        } catch (OutsidePageException ex) {
                            toRes = new ResourceOutside(linkUrl, link.getFromResource().getDepth() + 1, maxDepth, link.getFromResource());
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
