package reaper.model;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for storing Form elements of Web Documents.
 * @author zaraka
 */
public class Form extends VertexAbstract{
    
    private static final Logger loggerMiner = Logger.getLogger(reaper.model.MinerService.class.getName());
    
    private URL action;
    private RestMethod method;
    private ResourceDom parent;
    
    Form(Vertex ver, ResourceDom parent){
        this.method = RestMethod.valueOf(ver.getProperty("method"));
        try {
            this.action = new URL(ver.getProperty("action"));
        } catch (MalformedURLException ex) {
            loggerMiner.log(Level.WARNING, ex.getMessage());
        }
        this.parent = parent;
    }
    
    Form(URL action, RestMethod method, ResourceDom parent){
        this.action = action;
        this.method = method;
    }
    
    public URL getAction(){
        return this.action;
    }
    
    public void setAction(URL action){
        this.action = action;
    }
    
    public RestMethod getMethod(){
        return this.method;
    }
    
    public void setMethod(RestMethod method){
        this.method = method;
    }
    
    public void setParent(ResourceDom parent){
        this.parent = parent;
    }
    
    public ResourceDom getParent(){
        return this.parent;
    }

    @Override
    public void save(OrientGraph graph) {
        graph.getVertex(id).setProperties("action", action.toString(), "method", method.toString());
    }

    @Override
    public void update(OrientGraph graph) {
        OrientVertex ver = graph.getVertex(id);
        
        //TODO action;
        method = RestMethod.valueOf(ver.getProperty("method"));
        
        try {
            action = new URL(ver.getProperty("action"));
        } catch (MalformedURLException ex) {
            loggerMiner.log(Level.WARNING, ex.getMessage());
        }
    }
}
