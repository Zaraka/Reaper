package reaper.view;

import java.util.logging.Logger;
import reaper.Reaper;

/**
 *
 * @author zaraka
 */
public class SitemapController {
    private static final Logger logger = Logger.getLogger(Reaper.class.getName());
    
    private ReaperController parent;
    
    public void openNode(String nodeId){
        parent.setActiveNode(nodeId);
    }
    
    public void setParentController(ReaperController parent){
        this.parent = parent;
    }
    
}
