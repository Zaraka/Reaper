/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.model;

import java.io.IOException;
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

    private final StringProperty path;
    private IntegerProperty code;
    private Document doc;
    private ResourceState state;
    private List<String> links;

    Resource() {
        this.path = new SimpleStringProperty("undefined");
        this.state = ResourceState.UNITIALIZED;
    }

    Resource(String path) {
        this.path = new SimpleStringProperty(path);
        
        this.load();
    }

    public List<String> getLinks() {
        Element content = this.doc.getElementById("content");
        Elements docLinks = content.getElementsByTag("a");
        for (Element link : docLinks) {
            this.links.add(link.attr("href"));
        }

        return this.links;
    }

    private void load() {
        this.state = ResourceState.PROCESSING;
        try {
            this.doc = Jsoup.connect(this.path.get()).get();
        } catch (IOException ex) {
            Logger.getLogger(Reaper.class.getName()).log(Level.SEVERE, null, ex);
            this.state = ResourceState.ERROR;
        }
        this.state = ResourceState.FINISHED;
    }

    public String getPath() {
        return this.path.get();
    }

    public void setPath(String path) {
        this.path.set(path);
    }
}
