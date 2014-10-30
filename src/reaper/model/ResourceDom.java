package reaper.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import reaper.Reaper;

/**
 *
 * @author nikita.vanku
 */
public class ResourceDom extends ResourceAbstract {

    private static final Logger logger = Logger.getLogger(Reaper.class.getName());

    private Document doc;
    private final ObservableList<Form> forms;

    ResourceDom() {
        super();

        this.forms = FXCollections.observableArrayList();
        this.type = ResourceType.DOM;
    }

    ResourceDom(String path, int depth, int maxDepth, URL parentURL) throws UnsupportedMimeTypeException, MalformedURLException {
        super(path, depth, maxDepth, parentURL);
        this.forms = FXCollections.observableArrayList();
        this.type = ResourceType.DOM;
        System.out.println("dom creation");
        try {
            this.load();
        } catch (UnsupportedMimeTypeException ex) {
            throw ex;
        }
        //this.loadChilds();
    }

    private void retrieveLinks() {
        this.retrieveHrefs();
    }

    private void retrieveHrefs() {
        Elements docLinks = this.doc.getElementsByTag("a");
        for (Element link : docLinks) {
            String href = link.attr("href").trim();
            Link newLink = new Link(href, this);
            this.links.add(newLink);
        }
    }

    private void retrieveForms() {

    }

    private void load() throws UnsupportedMimeTypeException {
        this.state = ResourceState.PROCESSING;
        System.out.println("Resource " + this.url.toString() + " loading START");
        try {
            Connection.Response response = Jsoup.connect(this.url.toString()).execute();
            this.code.set(response.statusCode());
            this.doc = response.parse();
        } catch (UnsupportedMimeTypeException ex) {
            throw ex;
        } catch (HttpStatusException ex) {
            //I dont give a fuuuck
            System.out.println("URL " + ex.getUrl() + " code: " + ex.getStatusCode());
            this.state = ResourceState.ERROR;
        } catch (UnknownHostException ex) {
            System.out.println("Cannot reach host " + url);
            this.state = ResourceState.ERROR;
        } catch (IOException ex) {
            System.out.println(ex);
            this.state = ResourceState.ERROR;
        }

        if (this.state == ResourceState.PROCESSING) {
            this.state = ResourceState.FINISHED;
            this.retrieveLinks();
        }
    }

    /*private void loadChilds() {
        if (this.state == ResourceState.FINISHED) {
            if (this.getDepth() < this.getMaxDepth()) {
                int childDepth = this.getDepth() + 1;
                for (Link link : this.links) {
                    try {
                        ResourceDom child = new ResourceDom(link.getLink(), childDepth, this.getMaxDepth(), this.masterResources, this.url);
                    } catch (UnsupportedMimeTypeException ex) {
                        try {
                            ResourceFile child = new ResourceFile(link.getLink(), childDepth, this.getMaxDepth(), this.masterResources, this.url);
                        } catch (MalformedURLException ex1) {
                            logger.log(Level.SEVERE, null, ex1);
                        }
                    } catch (MalformedURLException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }*/

    private boolean validateLink(String link) {
        if (link.equals("")) {
            return false;
        } else if (link.startsWith("#")) {
            return false;
        }
        return true;
    }
}