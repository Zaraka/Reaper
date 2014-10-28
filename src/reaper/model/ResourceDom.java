/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.model;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import reaper.Reaper;

/**
 *
 * @author nikita.vanku
 */
public class ResourceDom extends Resource {

    private static final Logger logger = Logger.getLogger(Reaper.class.getName());

    private Document doc;
    private final ObservableList<Form> forms;

    ResourceDom() {
        super();
        
        this.forms = FXCollections.observableArrayList();
    }
    
    ResourceDom(String path, int depth, int maxDepth, ObservableList<ResourceInterface> masterResources) {
        super(path, depth, maxDepth, masterResources);
        this.forms = FXCollections.observableArrayList();
        this.load();
        this.masterResources.add(this);
        this.loadChilds();
    }

    private void retrieveLinks() {
        this.retrieveHrefs();
    }

    private void retrieveHrefs() {
        Elements docLinks = this.doc.getElementsByTag("a");
        for (Element link : docLinks) {
            String href = link.attr("href").trim();
            try {
                Link newLink = new Link(this.getPath(), href, this);
                this.links.add(newLink);
            } catch (InvalidLinkException ex) {
                logger.log(Level.FINER, ex.getMessage());
            }

        }
    }

    private void retrieveForms() {

    }

    private void load() {
        this.state = ResourceState.PROCESSING;
        logger.log(Level.FINE, "resource loading start");
        String url = this.getPath();
        try {
            this.doc = Jsoup.connect(url).get();
        } catch (HttpStatusException ex) {
            //I dont give a fuuuck
            logger.log(Level.INFO, "URL " + ex.getUrl() + " code: " + ex.getStatusCode());
            this.state = ResourceState.ERROR;
        } catch (UnknownHostException ex) {
            logger.log(Level.SEVERE, "Cannot reach host " + url, ex);
            this.state = ResourceState.ERROR;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            this.state = ResourceState.ERROR;
        }

        if (this.state == ResourceState.PROCESSING) {
            this.state = ResourceState.FINISHED;
            logger.log(Level.FINE, "Loading finished.");

            this.retrieveLinks();
        }
    }

    private void loadChilds() {
        if (this.state == ResourceState.FINISHED) {
            if (this.getDepth() < this.getMaxDepth()) {
                int childDepth = this.getDepth() + 1;
                for (Link link : this.links) {
                    ResourceDom child = new ResourceDom(link.getToPath(), childDepth, this.getMaxDepth(), this.masterResources);
                }
            }
        }
    }
}
