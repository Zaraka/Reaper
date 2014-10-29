package reaper.model;

import java.net.MalformedURLException;
import java.net.URL;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Model
 *
 * @author zaraka
 */
abstract class ResourceAbstract implements Resource{
    protected Resource parent;
    protected ResourceType type;
    protected IntegerProperty code;
    protected ResourceState state;
    private final IntegerProperty depth;
    private final IntegerProperty maxDepth;
    protected URL url;
    protected final ObservableList<Resource> masterResources;
    protected final ObservableList<Link> links;


    ResourceAbstract() {
        this.url = null;
        this.state = ResourceState.UNITIALIZED;
        this.depth = new SimpleIntegerProperty(0);
        this.maxDepth = new SimpleIntegerProperty(0);
        this.masterResources = FXCollections.observableArrayList();
        this.links = FXCollections.observableArrayList();
        this.type = ResourceType.UNDEFINED;
        this.code = new SimpleIntegerProperty(0);
    }
    
    ResourceAbstract(String path, int depth, int maxDepth, ObservableList<Resource> masterResources, URL parentURL) throws MalformedURLException {
        if(parentURL == null){
            this.url = new URL(path);
        } else {
            this.url = new URL(parentURL, path);
        }
        
        this.state = ResourceState.UNITIALIZED;
        this.depth = new SimpleIntegerProperty(depth);
        this.maxDepth = new SimpleIntegerProperty(maxDepth);
        this.masterResources = masterResources;
        this.links = FXCollections.observableArrayList();
        this.type = ResourceType.UNDEFINED;
        this.code = new SimpleIntegerProperty(0);
    }

    @Override
    public String getPath() {
        return this.url.getPath();
    }
    
    @Override
    public String getAbsoluteURL(){
        return this.url.toString();
    }

    @Override
    public URL getURL(){
        return this.url;
    }
    
    public void setPath(URL url) {
        this.url = url;
    }

    public void setDepth(int depth) {
        this.depth.set(depth);
    }

    public int getDepth() {
        return this.depth.get();
    }

    public IntegerProperty getDepthProperty() {
        return this.depth;
    }
    
    public int getMaxDepth(){
        return this.maxDepth.get();
    }
    
    public void setMaxDepth(int maxDepth){
        this.maxDepth.set(maxDepth);
    }
    
    @Override
    public ObservableList<Link> links(){
        return this.links;
    }
    
    public int getCode(){
        return this.code.get();
    }
    
    public void setCode(int code){
        this.code.set(code);
    }
    
    public IntegerProperty codeProperty(){
        return this.code;
    }
    
}
