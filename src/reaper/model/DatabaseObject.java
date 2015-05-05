package reaper.model;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * Basic object for storing various database vertices.
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

    /**
     * get ORID
     * @return Object (ORID) id
     */
    public Object getID();

    /**
     * set ORID
     * <b>WARNING: Setting ID manually may broke Database</b>
     * @param id Object
     */
    public void setID(Object id);
}
