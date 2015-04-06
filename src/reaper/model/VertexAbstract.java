package reaper.model;

import com.tinkerpop.blueprints.Vertex;

/**
 *
 * @author zaraka
 */
public abstract class VertexAbstract {
    Object id;
    
    public VertexAbstract(){
        this.id = null;
    }

    public VertexAbstract(Vertex ver) {
        this.id = ver.getId();
    }
    
    public Object getID(){
        return this.id;
    }
    
    public void setID(Object id){
        this.id = id;
    }
}
