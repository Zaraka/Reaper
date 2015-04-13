package reaper.model;

import com.tinkerpop.blueprints.Vertex;

/**
 *
 * @author zaraka
 */
public abstract class VertexAbstract implements VertexObject{
    Object id;
    
    public VertexAbstract(){
        this.id = null;
    }

    public VertexAbstract(Vertex ver) {
        this.id = ver.getId();
    }
    
    @Override
    public Object getID(){
        return this.id;
    }
    
    @Override
    public void setID(Object id){
        this.id = id;
    }
    
}
