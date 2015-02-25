package reaper.model;

import com.tinkerpop.blueprints.Vertex;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author nikita.vanku
 */
public class ResourceOutside extends ResourceAbstract{

    ResourceOutside(URL url, int depth, int maxDepth) throws MalformedURLException {
        super(url, depth, maxDepth);
        this.type = ResourceType.OUTSIDE;
    }
    
    ResourceOutside(Vertex vertex) throws MalformedURLException{
        super(vertex);
        
        this.type = ResourceType.OUTSIDE;
    }
}
