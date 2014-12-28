package reaper.model;

import com.tinkerpop.blueprints.Vertex;


/**
 *
 * @author zaraka
 */
public class ResourceFactory {
    public Resource resourceFromVector(Vertex vertex){
        switch(vertex.getProperty("type").toString()){
            case "DOM":
                return new ResourceDom(vertex);
                
        }
        return null;
    }
}
