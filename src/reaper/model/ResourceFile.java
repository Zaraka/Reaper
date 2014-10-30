package reaper.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import reaper.Reaper;

/**
 *
 * @author nikita.vanku
 */
public class ResourceFile extends ResourceAbstract {
    private static final Logger logger = Logger.getLogger(Reaper.class.getName());
    
    ResourceFile(String path, int depth, int maxDepth, URL parentURL) throws MalformedURLException {
        super(path, depth, maxDepth, parentURL);
        this.type = ResourceType.FILE;
    }
}
