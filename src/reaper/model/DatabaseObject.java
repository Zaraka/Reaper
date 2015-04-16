package reaper.model;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 *
 * @author zaraka
 */
public interface DatabaseObject {

    /**
     * Save(update) vertex to Database
     * @param graph 
     */
    public void save(OrientGraph graph);

    /**
     * Remove vertex from Database
     * @param graph 
     */
    public void remove(OrientGraph graph);

    /**
     * Update vertex from Database
     * @param graph 
     */
    public void update(OrientGraph graph);

    public Object getID();

    public void setID(Object id);
}
