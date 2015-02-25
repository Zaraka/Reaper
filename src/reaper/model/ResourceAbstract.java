package reaper.model;

import com.orientechnologies.orient.core.id.ORID;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model
 *
 * @author zaraka
 */
abstract class ResourceAbstract implements Resource{
    protected ResourceType type;
    protected URL url;
    protected long downloadTime;
    protected ResourceState state;
    protected Map<String, Link> links;
    private final IntegerProperty code;
    private final StringProperty mimeType;
    private final IntegerProperty depth;
    private final IntegerProperty maxDepth;
    private Object id;

    ResourceAbstract() {
        this.url = null;
        this.state = ResourceState.UNITIALIZED;
        this.depth = new SimpleIntegerProperty(0);
        this.maxDepth = new SimpleIntegerProperty(0);
        this.links = new HashMap();
        this.type = ResourceType.UNDEFINED;
        this.code = new SimpleIntegerProperty(0);
        this.mimeType = new SimpleStringProperty("");
        this.downloadTime = 0;
        this.id = null;
    }
   
    ResourceAbstract(URL url, int depth, int maxDepth) throws MalformedURLException {
        this.url = url;
        this.state = ResourceState.UNITIALIZED;
        this.depth = new SimpleIntegerProperty(depth);
        this.maxDepth = new SimpleIntegerProperty(maxDepth);
        this.links = new HashMap();
        this.type = ResourceType.UNDEFINED;
        this.code = new SimpleIntegerProperty(0);
        this.mimeType = new SimpleStringProperty("");
        this.downloadTime = 0;
        this.id = null;
    }
    
    ResourceAbstract(Vertex vertex) throws MalformedURLException{
        this.state = ResourceState.UNITIALIZED;
        this.depth = new SimpleIntegerProperty(0);
        this.maxDepth = new SimpleIntegerProperty(0);
        this.links = new HashMap<>();
        this.type = ResourceType.UNDEFINED;
        
        this.id = (ORID)vertex.getId();
        this.url = new URL(vertex.getProperty("url").toString());
        this.code = new SimpleIntegerProperty((int)vertex.getProperty("code"));
        this.downloadTime = (long)vertex.getProperty("downloadTime");
        this.mimeType = new SimpleStringProperty(vertex.getProperty("mimeType").toString());
    }
    
    @Override
    public String getPath() {
        return this.url.getPath();
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

    @Override
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
    public ArrayList<Link> links(){
        return new ArrayList<>(this.links.values());
    }
    
    @Override
    public int getCode(){
        return this.code.get();
    }
    
    public void setCode(int code){
        this.code.set(code);
    }
    
    @Override
    public IntegerProperty codeProperty(){
        return this.code;
    }
    
    @Override
    public ResourceType getType(){
        return this.type;
    }
    
    @Override
    public Link getLinkWithPath(String path){
        return this.links.get(path);
    }
    
    @Override
    public StringProperty mimeTypeProperty(){
        return this.mimeType;
    }
    
    @Override
    public String getMimeType(){
        return this.mimeType.get();
    }
    
    public void setMimeType(String type){
        this.mimeType.set(type);
    }
    
    @Override
    public long getDownloadTime(){
        return this.downloadTime;
    }
    
    
    @Override
    public Object getVertexID(){
        return this.id;
    }
    
    @Override
    public void setVertexID(Object or){
        this.id = or;
    }
    
    @Override
    public void vertexTransaction(OrientGraph graph){
        try {
            OrientVertex vertex = graph.addVertex("class:Resource",
                    "url", this.getURL().toString(), "code", this.getCode(),
                    "downloadTime", this.getDownloadTime(), "mimeType", this.getMimeType(),
                    "type", this.getType().toString());
            graph.commit();
            this.setVertexID(vertex.getId());
        } finally {
            graph.shutdown();
        }
    }
}
