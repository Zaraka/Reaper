package reaper.model;

import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jsoup.UnsupportedMimeTypeException;
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
        this.maxDepth = new SimpleIntegerProperty(1);
        this.maxDownloads = new SimpleIntegerProperty(5);
    }
    
    public Domain(String hostname, int maxDownloads, int maxDepth){
        this.resources = FXCollections.observableArrayList();
        this.hostname = new SimpleStringProperty(hostname);
        this.maxDepth = new SimpleIntegerProperty(maxDepth);
        this.maxDownloads = new SimpleIntegerProperty(maxDownloads);
    }
    
    public void mine(){
        this.clearData();
        
        logger.log(Level.INFO, "Data mining "+this.getHostname()+" STARTED");
        //try just one page for start
        
        String url = this.hostname.get();
        if(!url.matches("^.*:\\/\\/.*$")){
            url = "http://" + url;
            logger.log(Level.WARNING, "You should provide protocol as well. Default proctol http is used.");
        }
        try {
            ResourceDom page = new ResourceDom(url, 0, this.maxDepth.get(), this.resources, null);
        } catch (UnsupportedMimeTypeException ex) {
            logger.log(Level.SEVERE, "Root webpage isn't text/html or text/xml");
        } catch (MalformedURLException ex) {
            logger.log(Level.SEVERE, "URL is malformed");
        }
    }
    
    private void clearData(){
        logger.log(Level.FINE, "Clearing data");
        this.resources.clear();
    }
    
    public ObservableList<Resource> resources() {
        return this.resources;
    }

    public final String getHostname(){
        return this.hostname.get();
    }
    
    public final void setHostname(String hostname){
        this.hostname.set(hostname);
    }
    
    public final int getMaxDownloads(){
        return this.maxDownloads.get();
    }
    
    public final void setMaxDownloads(int maxDownloads){
        this.maxDownloads.set(maxDownloads);
    }
    
    public IntegerProperty maxDownloadsProperty(){
        return this.maxDownloads;
    }
    
    public IntegerProperty maxDepthProperty(){
        return this.maxDepth;
    }
    
    public final int getMaxDepth(){
        return this.maxDepth.get();
    }
    
    public final void setMaxDepth(int maxDepth){
        this.maxDepth.set(maxDepth);
    }
    
}
