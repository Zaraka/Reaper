package reaper.model;

import java.util.logging.Logger;
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

    Link() {
        this.link = new SimpleStringProperty("undefined");
    }

    Link(String link) {
        this.link = new SimpleStringProperty(link);
    }

    Link(String link, Resource source) {
        this.link = new SimpleStringProperty(link);
        this.fromResource = source;
    }
    
    Link(String link, Resource source, Resource destination){
        this.link = new SimpleStringProperty(link);
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

    /*
    public String getEdgeFormat() {
        List<String> sorting = new ArrayList<>();
        sorting.add(this.fromPath.get());
        sorting.add(this.toPath.get());
        Collections.sort(sorting, String.CASE_INSENSITIVE_ORDER);
        return sorting.get(0) + "-" + sorting.get(1);
    }*/
}
