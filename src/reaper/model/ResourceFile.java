package reaper.model;

import com.tinkerpop.blueprints.Vertex;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import reaper.Reaper;

/**
 *
 * @author nikita.vanku
 */
public class ResourceFile extends ResourceAbstract {
    private static final Logger logger = Logger.getLogger(Reaper.class.getName());
    
    ResourceFile(URL url, int depth, int maxDepth) throws MalformedURLException {
        super(url, depth, maxDepth);
        this.type = ResourceType.FILE;
    }
    
    ResourceFile(Vertex vertex) throws MalformedURLException{
        super(vertex);
        
        this.type = ResourceType.FILE;
    }
}
