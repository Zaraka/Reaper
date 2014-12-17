package reaper.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventType;
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
        mining.init(this.getHostname(), this.getMaxDepth(), this.resources(), this.links);
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
        private ObservableList<Resource> resources;
        private ObservableList<Link> linksQueue;
        private ObservableList<Link> links;
        private HashSet<String> paths;

        public void init(String hostname, int maxDepth, ObservableList<Resource> resources, ObservableList<Link> links) {
            this.hostname = hostname;
            this.maxDepth = maxDepth;
            this.resources = resources;
            this.linksQueue = FXCollections.observableArrayList();
            this.links = links;
            this.paths = new HashSet<>();
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
                        Platform.runLater(() -> {
                            resources.add(page);
                        });
                    } catch (UnsupportedMimeTypeException | MalformedURLException ex) {
                        throw ex;
                    }
                    Link link;
                    while ((link = popLink()) != null) {
                        //test hashSet This is quite stupid!
                        URL linkUrl = new URL(link.getFromResource().getURL(), link.getLink());
                        if(paths.contains(linkUrl.toString())){
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
                            Link link2 = link;
                            Platform.runLater(() -> {
                                //logger.log(Level.FINE, link2.toString());
                                resources.add(child);
                                links.add(link2);
                            });
                        } catch (UnsupportedMimeTypeException ex) {
                            try {
                                ResourceFile child = new ResourceFile(linkUrl, link.getFromResource().getDepth() + 1, maxDepth, link.getFromResource());
                                if (child.getDepth() < maxDepth) {
                                    linksQueue.addAll(child.links);
                                }
                                link.setToResource(child);
                                Link link2 = link;
                                Platform.runLater(() -> {
                                    links.add(link2);
                                    resources.add(child);
                                });
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
