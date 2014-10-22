/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.model;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import reaper.Reaper;

/**
 * Model
 *
 * @author zaraka
 */
public class Resource {
    
    private static final Logger logger = Logger.getLogger(Reaper.class.getName());
    
    private final StringProperty path;
    private IntegerProperty code;
    private Document doc;
    private ResourceState state;
    private ArrayList<String> links;

    
    Resource() {
        this.path = new SimpleStringProperty("undefined");
        this.state = ResourceState.UNITIALIZED;
        this.links = new ArrayList<>();
    }

    Resource(String path) {
        this.path = new SimpleStringProperty(path);
        this.state = ResourceState.UNITIALIZED;
        this.links = new ArrayList<>();
        this.load();
    }

    private void retrieveLinks() {
        //Element content = this.doc.getE("content");
        Elements docLinks = this.doc.getElementsByTag("a");
        for (Element link : docLinks) {
            String href = link.attr("href");
            this.links.add(href);
            logger.log(Level.FINE, href);
        }
    }

    private void load() {
        this.state = ResourceState.PROCESSING;
        String url = this.path.get().trim();
        if(!url.matches("^https?:\\/\\/.*$")){
            url = "http://" + url;
        }
        logger.log(Level.FINE, "Load: " + url);
        try {
            this.doc = Jsoup.connect(url).get();
        } catch (UnknownHostException ex){
            logger.log(Level.SEVERE, "Cannot reach host " + url, ex);
            this.state = ResourceState.ERROR;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            this.state = ResourceState.ERROR;
        }
        this.state = ResourceState.FINISHED;
        logger.log(Level.FINE, "Loading finished.");
        
        this.retrieveLinks();
    }
    
    public List<String> getLinks(){
        return this.links;
    }

    public String getPath() {
        return this.path.get();
    }

    public void setPath(String path) {
        this.path.set(path);
    }
}
