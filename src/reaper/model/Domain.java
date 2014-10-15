package reaper.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author zaraka
 */
public class Domain {
    
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
        //try just one page for start
        Resource page = new Resource(this.hostname.get());
        resources.add(page);
    }
    
    public ObservableList<Resource> getResource() {
        return this.resources;
    }

    public String getHostname(){
        return this.hostname.get();
    }
    
    public void setHostname(String hostname){
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
