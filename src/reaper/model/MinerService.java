package reaper.model;

import com.orientechnologies.orient.core.exception.OStorageException;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.jsoup.UnsupportedMimeTypeException;
import reaper.exceptions.OutsidePageException;

/**
 *
 * @author zaraka
 */
public class MinerService extends Service<Void> {

    private String hostname;
    private int maxDepth;
    private String databaseHost;
    private String databaseUser;
    private String databasePassword;
    private ArrayList<Link> linksQueue;
    private HashSet<String> paths;
    private OrientGraphFactory graphFactory;

    public void init(String hostname, int maxDepth, String dbHost, String dbUser, String dbPass) {
        this.hostname = hostname;
        this.maxDepth = maxDepth;
        this.linksQueue = new ArrayList<>();
        this.paths = new HashSet<>();
        this.databaseHost = dbHost;
        this.databasePassword = dbPass;
        this.databaseUser = dbUser;
        
        try {
            this.graphFactory = new OrientGraphFactory(dbHost, dbUser, dbPass).setupPool(1, 10);
        } catch(OStorageException ex){
            throw ex;
        }
        
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
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
            OrientVertex vertex = graph.addVertex("class:Resource",
                    "url", res.getURL().toString(), "code", res.getCode(),
                    "downloadTime", res.getDownloadTime(), "mimeType", res.getMimeType(),
                    "type", res.getType().toString());
            res.setVertexID(vertex.getIdentity());
        } finally {
            graph.shutdown();
        }
    }

    private void linkTransaction(Link link) {
        OrientGraph graph = graphFactory.getTx();
        try {
            OrientVertex from, to;
            if (link.getFromResource().getVertexID() == null) {
                from = graph.addVertex("class:Resource", "url", link.getFromResource().getURL().toString(),
                        "code", link.getFromResource().getCode(),
                        "downloadTime", link.getFromResource().getDownloadTime(),
                        "mimeType", link.getFromResource().getMimeType(),
                        "type", link.getFromResource().getType().toString());
                link.getFromResource().setVertexID(from.getIdentity());
            } else {
                from = graph.getVertex(link.getFromResource().getVertexID());
            }

            if (link.getToResource().getVertexID() == null) {
                to = graph.addVertex("class:Resource", "url", link.getToResource().getURL().toString(),
                        "code", link.getToResource().getCode(),
                        "downloadTime", link.getFromResource().getDownloadTime(),
                        "mimeType", link.getToResource().getMimeType(),
                        "type", link.getToResource().getType().toString());
                link.getToResource().setVertexID(to.getIdentity());
            } else {
                to = graph.getVertex(link.getToResource().getVertexID());
            }
            from.addEdge("LinkTo", to, null, null, "path", link.getLink(), "count", link.getCount());
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
                    System.out.println("You should provide protocol as well. Default proctol http is used.");

                }

                try {
                    URL uURL = new URL(url);
                    ResourceDom root = new ResourceDom(uURL, 0, maxDepth, null);
                    paths.add(root.getURL().toString());
                    linksQueue.addAll(root.links());                    
                    createSingleVertex(root);
                } catch (UnsupportedMimeTypeException | MalformedURLException ex) {
                    throw ex;
                }
                Link link;
                while ((link = popLink()) != null) {
                    URL linkUrl = new URL(link.getFromResource().getURL(), link.getLink());
                    if (paths.contains(linkUrl.toString())) {
                        continue;
                    } else {
                        paths.add(linkUrl.toString());
                    }
                    try {
                        ResourceDom child = new ResourceDom(linkUrl, link.getFromResource().getDepth() + 1, maxDepth, link.getFromResource());
                        if (child.getDepth() < maxDepth) {
                            linksQueue.addAll(child.links());
                        }
                        link.setToResource(child);
                        linkTransaction(link);
                    } catch (UnsupportedMimeTypeException ex) {
                        try {
                            ResourceFile child = new ResourceFile(linkUrl, link.getFromResource().getDepth() + 1, maxDepth, link.getFromResource());
                            link.setToResource(child);
                            linkTransaction(link);

                        } catch (MalformedURLException ex1) {
                            throw ex1;
                        }
                    } catch (OutsidePageException ex) {
                        ResourceOutside child = new ResourceOutside(linkUrl, link.getFromResource().getDepth() + 1, maxDepth, link.getFromResource());
                        link.setToResource(child);
                        linkTransaction(link);
                    } catch (MalformedURLException ex) {
                        throw ex;
                    }
                }
                return null;
            }

        };
    }

}
