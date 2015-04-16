package reaper.model;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

/**
 *
 * @author nikita.vanku
 */
public interface VertexObject extends DatabaseObject{
    
    public OrientVertex getVertex(OrientGraph graph);
    
}
