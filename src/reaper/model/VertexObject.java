package reaper.model;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 *
 * @author nikita.vanku
 */
public interface VertexObject {
    
    public void save(OrientGraph graph);
    
    public void remove(OrientGraph graph);
    
    public void update(OrientGraph graph);
    
    public Object getID();
    
    public void setID(Object id);
}
