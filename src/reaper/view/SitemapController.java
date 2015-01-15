package reaper.view;

import java.util.logging.Logger;
import reaper.Reaper;
import reaper.model.Domain;

/**
 *
 * @author zaraka
 */
public class SitemapController {
    private static final Logger logger = Logger.getLogger(Reaper.class.getName());
    
    private Domain domain;
    
    public void openNode(String nodeId){
        domain.loadResource(nodeId);
    }
    
    public void setDomain(Domain domain){
        this.domain = domain;
    }
    
}
