package reaper.model;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
