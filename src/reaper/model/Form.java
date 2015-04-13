package reaper.model;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

/**
 *
 * @author zaraka
 */
public class Form extends VertexAbstract{
    private Link action;
    private RestMethod method;
    
    Form(){
        this.action = new Link();
        this.method = RestMethod.GET;
    }
    
    Form(Vertex ver){
        this.method = RestMethod.valueOf(ver.getProperty("method"));
    }
    
    Form(Link action, RestMethod method){
        this.action = action;
        this.method = method;
    }
    
    
    public Link getAction(){
        return this.action;
    }
    
    public void setAction(Link action){
        this.action = action;
    }
    
    public RestMethod getMethod(){
        return this.method;
    }
    
    public void setMethod(RestMethod method){
        this.method = method;
    }

    @Override
    public void save(OrientGraph graph) {
        graph.getVertex(id).setProperties("action", action.getLink(), "method", method.toString());
    }

    @Override
    public void update(OrientGraph graph) {
        OrientVertex ver = graph.getVertex(id);
        
        //TODO action;
        method = RestMethod.valueOf("method");
    }
}
