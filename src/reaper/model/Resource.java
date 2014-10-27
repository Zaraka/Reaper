package reaper.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Model
 *
 * @author zaraka
 */
abstract class Resource {
    protected ResourceType type;
    private final StringProperty path;
    protected IntegerProperty code;
    protected ResourceState state;
    private final IntegerProperty depth;
    private final IntegerProperty maxDepth;
    protected final ObservableList<Resource> masterResources;
    protected final ObservableList<Link> masterLinks;


    Resource() {
        this.path = new SimpleStringProperty("undefined");
        this.state = ResourceState.UNITIALIZED;
        this.depth = new SimpleIntegerProperty(0);
        this.maxDepth = new SimpleIntegerProperty(0);
        this.masterResources = FXCollections.observableArrayList();
        this.masterLinks = FXCollections.observableArrayList();
    }
    
    Resource(String path, int depth, int maxDepth, ObservableList<Resource> masterResources, ObservableList<Link> masterLinks) {
        this.path = new SimpleStringProperty(path);
        this.state = ResourceState.UNITIALIZED;
        this.depth = new SimpleIntegerProperty(depth);
        this.maxDepth = new SimpleIntegerProperty(maxDepth);
        this.masterResources = masterResources;
        this.masterLinks = masterLinks;
    }

    public String getPath() {
        return this.path.get();
    }

    public void setPath(String path) {
        this.path.set(path);
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
}
