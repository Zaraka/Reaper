package reaper.model;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author nikita.vanku
 */
public class ResourceOutside extends ResourceAbstract{

    public ResourceOutside(URL url, int depth, int maxDepth, Resource parent) throws MalformedURLException {
        super(url, depth, maxDepth, parent);
        this.type = ResourceType.OUTSIDE;
    }
        
}
