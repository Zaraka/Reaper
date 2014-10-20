package reaper.model;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import reaper.Reaper;

/**
 *
 * @author zaraka
 */
public class Domain {
    private static final Logger logger = Logger.getLogger(Reaper.class.getName());
    
    private final ObservableList<Resource> resources;
    private final StringProperty hostname;
    private final IntegerProperty maxDownloads;
    private final IntegerProperty maxDepth;
    
    public Domain() {
        this.resources = FXCollections.observableArrayList();
        this.hostname = new SimpleStringProperty("");
        this.maxDepth = new SimpleIntegerProperty(5);
        this.maxDownloads = new SimpleIntegerProperty(5);
    }
    
    public Domain(String hostname, int maxDownloads, int maxDepth){
        this.resources = FXCollections.observableArrayList();
        this.hostname = new SimpleStringProperty(hostname);
        this.maxDepth = new SimpleIntegerProperty(maxDepth);
        this.maxDownloads = new SimpleIntegerProperty(maxDownloads);
    }
    
    public void mine(){
        logger.log(Level.INFO, "Data mining start");
        //try just one page for start
        Resource page = new Resource(this.hostname.get());
        resources.add(page);
    }
    
    public ObservableList<Resource> getResource() {
        return this.resources;
    }

    public final String getHostname(){
        return this.hostname.get();
    }
    
    public final void setHostname(String hostname){
        this.hostname.set(hostname);
    }
    
    public int getMaxDownloads(){
        return this.maxDownloads.get();
    }
    
    public void setMaxDownloads(int maxDownloads){
        this.maxDownloads.set(maxDownloads);
    }
    
    public int getMaxDepth(){
        return this.maxDepth.get();
    }
    
    public void setMaxDepth(int maxDepth){
        this.maxDepth.set(maxDepth);
    }
    
}
