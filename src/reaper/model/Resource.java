package reaper.model;

import java.net.URL;
import javafx.collections.ObservableList;

/**
 *
 * @author zaraka
 */
public interface Resource {
    public ObservableList<Link> links();
    public String getPath();
    public URL getURL();
    public String getAbsoluteURL();
    public int getDepth();
    public ResourceType getType();
}
