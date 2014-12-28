package reaper.model;

import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import reaper.Reaper;

/**
 *
 * @author nikita.vanku
 */
public class Link {
    
    private static final Logger logger = Logger.getLogger(Reaper.class.getName());

    private final StringProperty link;
    private Resource fromResource;
    private Resource toResource;
    private final IntegerProperty count;
    private LinkType type;

    Link() {
        this.link = new SimpleStringProperty("undefined");
        this.fromResource = null;
        this.toResource = null;
        this.count = new SimpleIntegerProperty(0);
        this.type = LinkType.UNDEFINED;
    }

    Link(String link, Resource source) {
        this.link = new SimpleStringProperty(link);
        this.count = new SimpleIntegerProperty(0);
        this.fromResource = source;
        this.type = LinkType.UNDEFINED;
        this.fromResource = source;
        this.toResource = null;
    }
    
    Link(String link, Resource source, LinkType type) {
        this.link = new SimpleStringProperty(link);
        this.count = new SimpleIntegerProperty(0);
        this.fromResource = source;
        this.type = type;
        this.fromResource = source;
        this.toResource = null;
    }
    
    Link(String link, Resource source, Resource destination, LinkType type){
        this.link = new SimpleStringProperty(link);
        this.count = new SimpleIntegerProperty(0);
        this.fromResource = source;
        this.toResource = destination;
        this.type = type;
        this.fromResource = source;
        this.toResource = destination;
    }

    public void setLink(String path) {
        this.link.set(path);
    }

    public String getLink() {
        return this.link.get();
    }

    public StringProperty linkProperty() {
        return this.link;
    }

    public Resource getFromResource() {
        return this.fromResource;
    }

    public void setFromResource(Resource res) {
        this.fromResource = res;
    }

    public Resource getToResource() {
        return this.toResource;
    }

    public void setToResource(Resource to) {
        this.toResource = to;
    }

    public String getEdgeFormat() {
        if(this.fromResource != null && this.toResource != null){
            return this.fromResource.getPath()+"@"+this.toResource.getPath();
        }
        return null;
    }
    
    public int getCount(){
        return this.count.get();
    }
    
    public void setCount(int count){
        this.count.set(count);
    }
    
    public void addCount(){
        this.setCount(this.getCount() + 1);
    }
    
    public LinkType getType(){
        return this.type;
    }
    
    public void setType(LinkType type){
        this.type = type;
    }
    
    @Override
    public String toString(){
        String from = "";
        String to = "";
        if(this.fromResource != null){
            from = this.fromResource.getURL().toString();
        }
        if(this.toResource != null){
            to = this.toResource.getURL().toString();
        }
        String result = from + " " + this.link.get() + " " + to;
        return result.trim();
    }
}
