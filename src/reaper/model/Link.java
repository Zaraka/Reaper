package reaper.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author nikita.vanku
 */
public class Link {
    private final StringProperty fromPath;
    private final StringProperty toPath;
    private Resource fromResource;
    private Resource toResource;
    
    Link(){
        this.fromPath = new SimpleStringProperty("undefined");
        this.toPath = new SimpleStringProperty("undefined");
    }
    
    Link(String from, String to){
        this.fromPath = new SimpleStringProperty(from);
        this.toPath = new SimpleStringProperty(to);
    }
    
    Link(String from, String to, Resource source){
        this.fromPath = new SimpleStringProperty(from);
        this.toPath = new SimpleStringProperty(to);
        this.fromResource = source;
    }
    
    public void setFromPath(String path){
        this.fromPath.set(path);
    }
    
    public String getFromPath(){
        return this.fromPath.get();
    }
    
    public StringProperty getFromPathPropertsy(){
        return this.fromPath;
    }
    
    public void setToPath(String path){
        this.toPath.set(path);
    }
    
    public String getToPath(){
        return this.toPath.get();
    }
    
    public StringProperty getToPathProperty(){
        return this.toPath;
    }
    
    public Resource getFromResource(){
        return this.fromResource;
    }
    
    public void setFromResource(Resource res){
        this.fromResource = res;
    }
    
    public Resource getToResource(){
        return this.toResource;
    }
    
    public void setToResource(Resource to){
        this.toResource = to;
    }
}
