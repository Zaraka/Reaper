package reaper.model;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import org.jsoup.UnsupportedMimeTypeException;
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

    private final MinerService mining;

    public Domain() {
        this.resources = FXCollections.observableArrayList();
        this.links = FXCollections.observableArrayList();
        this.hostname = new SimpleStringProperty("");
        this.maxDepth = new SimpleIntegerProperty(1);
        this.maxDownloads = new SimpleIntegerProperty(5);
        this.mining = new MinerService();

        this.init();
    }

    public Domain(String hostname, int maxDownloads, int maxDepth) {
        this.resources = FXCollections.observableArrayList();
        this.links = FXCollections.observableArrayList();
        this.hostname = new SimpleStringProperty(hostname);
        this.maxDepth = new SimpleIntegerProperty(maxDepth);
        this.maxDownloads = new SimpleIntegerProperty(maxDownloads);
        this.mining = new MinerService();

        this.init();
    }

    private void init() {
        mining.init(this.getHostname(), this.getMaxDepth());
        mining.setOnSucceeded((WorkerStateEvent event) -> {
            logger.log(Level.INFO, "Mining finished");
        });
        mining.setOnFailed((WorkerStateEvent event) -> {
            logger.log(Level.SEVERE, "Mining failed");
            if (event.getSource().getException() != null) {
                logger.log(Level.SEVERE, event.getSource().getException().toString());
            }
        });
        mining.setOnRunning((WorkerStateEvent event) -> {
            logger.log(Level.INFO, "Mining started");
        });
        mining.setOnCancelled((WorkerStateEvent event) -> {
            logger.log(Level.INFO, "Mining canceled");
        });

    }

    public void mineStart(String hostname) {
        if (!this.mining.isRunning()) {
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

    private static class MinerService extends Service<Void> {

        private String hostname;
        private int maxDepth;
        private ObservableList<Link> linksQueue;
        private HashSet<String> paths;
        private OrientGraphFactory graphFactory;

        public void init(String hostname, int maxDepth) {
            this.hostname = hostname;
            this.maxDepth = maxDepth;
            this.linksQueue = FXCollections.observableArrayList();
            this.paths = new HashSet<>();
            this.graphFactory = new OrientGraphFactory("remote:localhost/ReaperTest", "admin", "admin").setupPool(1, 10);
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

        private void createVertex(Resource res) {
            OrientGraph graph = graphFactory.getTx();
            try {
                Vertex ver = graph.addVertex("class:Resource", "url", res.getURL().toString(), "mimeType", res.getMimeType(), "downloadTime", res.getDownloadTime(), "code", res.getCode());
                res.setVertex(ver);
            } finally {
                graph.shutdown();
            }
        }

        private void createEdge(Link link) {
            /*
            OrientGraph graph = graphFactory.getTx();
            try {
                Edge edge = graph.addEdge(null, link.getFromResource().getVertex(), link.getToResource().getVertex(), "linkTo");
                edge.setProperty("name", link.getLink());
            } finally {
                graph.shutdown();
            }
            System.out.println("edge created");
                    */
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
                        ResourceDom page = new ResourceDom(url, 0, maxDepth, null);
                        paths.add(page.getAbsoluteURL());
                        linksQueue.addAll(page.links);
                        createVertex(page);
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
                                linksQueue.addAll(child.links);
                            }
                            link.setToResource(child);
                            createVertex(child);
                            createEdge(link);
                        } catch (UnsupportedMimeTypeException ex) {
                            try {
                                ResourceFile child = new ResourceFile(linkUrl, link.getFromResource().getDepth() + 1, maxDepth, link.getFromResource());
                                if (child.getDepth() < maxDepth) {
                                    linksQueue.addAll(child.links);
                                }
                                link.setToResource(child);
                                createVertex(child);
                                createEdge(link);

                            } catch (MalformedURLException ex1) {
                                throw ex1;
                            }
                        } catch (MalformedURLException ex) {
                            throw ex;
                        }
                    }
                    return null;
                }

            };
        }

    }

    private void clearData() {
        logger.log(Level.FINE, "Clearing data");
        this.resources.clear();
    }

    public ObservableList<Resource> resources() {
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

}
