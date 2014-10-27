package reaper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
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

    private final StringProperty fromPath;
    private final StringProperty toPath;
    private Resource fromResource;
    private Resource toResource;

    Link() {
        this.fromPath = new SimpleStringProperty("undefined");
        this.toPath = new SimpleStringProperty("undefined");
    }

    Link(String from, String to) {
        this.fromPath = new SimpleStringProperty(from);
        this.toPath = new SimpleStringProperty(to);
    }

    Link(String from, String to, Resource source) throws InvalidLinkException{
        logger.log(Level.FINE, "Constructing link " + to);
        if(!this.validateLink(to)){
            throw new InvalidLinkException("Link "+ to +" scrapped.");
        }
        String urlTo = to;
        String urlFrom = from;
        if (urlTo.matches("^\\/\\/.*$")){
            urlTo = "http://" + urlTo.substring(2);
        } else if(!urlTo.matches("^.*:\\/\\/.*$")) {
            if(urlTo.startsWith("/")){
                urlTo = urlTo.substring(1);
            }
            if(urlFrom.endsWith("/")){
                urlFrom = urlFrom.substring(0,urlFrom.length()-1);
            }
            urlTo = urlFrom + "/" + urlTo;
        }
        logger.log(Level.FINE, "Validated " + urlTo);
        this.fromPath = new SimpleStringProperty(from);
        this.toPath = new SimpleStringProperty(urlTo);
        this.fromResource = source;
    }
    
    private boolean validateLink(String link){
        if(link.equals("")){
            return false;
        } else if(link.startsWith("#")){
            return false;
        } 
        return true;
    }

    public void setFromPath(String path) {
        this.fromPath.set(path);
    }

    public String getFromPath() {
        return this.fromPath.get();
    }

    public StringProperty fromPathProperty() {
        return this.fromPath;
    }

    public void setToPath(String path) {
        this.toPath.set(path);
    }

    public String getToPath() {
        return this.toPath.get();
    }

    public StringProperty toPathProperty() {
        return this.toPath;
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
        List<String> sorting = new ArrayList<>();
        sorting.add(this.fromPath.get());
        sorting.add(this.toPath.get());
        Collections.sort(sorting, String.CASE_INSENSITIVE_ORDER);
        return sorting.get(0) + "-" + sorting.get(1);
    }
}
