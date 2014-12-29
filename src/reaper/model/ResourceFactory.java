package reaper.model;

import com.tinkerpop.blueprints.Vertex;
import java.net.MalformedURLException;


/**
 *
 * @author zaraka
 */
public class ResourceFactory {
    public static Resource resourceFromVector(Vertex vertex) throws MalformedURLException{
        switch(vertex.getProperty("type").toString()){
            case "DOM":
                return new ResourceDom(vertex);
            case "FILE":
                return new ResourceFile(vertex);
            case "OUTSIDE":
                return new ResourceOutside(vertex);
            default:
                return new ResourceOutside(vertex);
                
        }
    }
}
