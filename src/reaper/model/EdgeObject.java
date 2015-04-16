package reaper.model;

import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 *
 * @author zaraka
 */
public interface EdgeObject extends VertexObject{
    
    public OrientEdge getEdge(OrientGraph graph);
}
