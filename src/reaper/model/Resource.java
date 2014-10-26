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
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private final ObservableList<Link> links;
    private final IntegerProperty depth;
    
    private final ObservableList<Resource> resources;
    private final ObservableList<Form> forms;

    
    Resource() {
        this.path = new SimpleStringProperty("undefined");
        this.state = ResourceState.UNITIALIZED;
        this.links = FXCollections.observableArrayList();
        this.forms = FXCollections.observableArrayList();
        this.resources = FXCollections.observableArrayList();
        this.depth = new SimpleIntegerProperty(0);
    }

    Resource(String path) {
        this.path = new SimpleStringProperty(path);
        this.state = ResourceState.UNITIALIZED;
        this.links =  FXCollections.observableArrayList();
        this.forms = FXCollections.observableArrayList();
        this.resources = FXCollections.observableArrayList();
        this.depth = new SimpleIntegerProperty(0);
        this.load();
    }

    private void retrieveLinks() {
        this.retrieveHrefs();
    }
    
    private void retrieveHrefs(){
        //Element content = this.doc.getE("content");
        Elements docLinks = this.doc.getElementsByTag("a");
        for (Element link : docLinks) {
            String href = link.attr("href");
            Link newLink = new Link(this.getPath(), href, this);
            this.links.add(newLink);
            logger.log(Level.FINE, href);
        }
    }
    
    private void retrieveForms(){
        
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
    
    public ObservableList<Link> getLinks(){
        return this.links;
    }

    public String getPath() {
        return this.path.get();
    }

    public void setPath(String path) {
        this.path.set(path);
    }
    
    public void setDepth(int depth){
        this.depth.set(depth);
    }
    
    public int getDepth(){
        return this.depth.get();
    }
    
    public IntegerProperty getDepthProperty(){
        return this.depth;
    }
}
