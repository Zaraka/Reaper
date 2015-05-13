package reaper.model;

import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 *
 * @author zaraka
 */
public interface EdgeObject extends DatabaseObject{
    
    public OrientEdge getEdge(OrientGraph graph);
}
