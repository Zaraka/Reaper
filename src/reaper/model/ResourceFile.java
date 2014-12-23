package reaper.model;

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
    
    ResourceFile(URL url, int depth, int maxDepth, Resource parent) throws MalformedURLException {
        super(url, depth, maxDepth, parent);
        this.type = ResourceType.FILE;
    }
}
