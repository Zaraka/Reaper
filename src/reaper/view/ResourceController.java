package reaper.view;

import reaper.model.Crawler;
import reaper.model.Resource;

/**
 *
 * @author zaraka
 */
public interface ResourceController {
    public void loadResource(Resource res, Crawler crawler);
}
