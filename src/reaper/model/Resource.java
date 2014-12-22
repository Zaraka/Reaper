package reaper.model;

import java.net.URL;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
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
    public Link getLinkWithPath(String Path);
    public IntegerProperty codeProperty();
    public int getCode();
    public StringProperty mimeTypeProperty();
    public String getMimeType();
    public long getDownloadTime();
}
