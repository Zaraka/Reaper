package reaper.model;

import com.orientechnologies.orient.core.id.ORID;
import java.net.URL;
import java.util.ArrayList;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author zaraka
 */
public interface Resource {
    public ArrayList<Link> links();
    public String getPath();
    public URL getURL();
    public int getDepth();
    public ResourceType getType();
    public Link getLinkWithPath(String Path);
    public IntegerProperty codeProperty();
    public int getCode();
    public StringProperty mimeTypeProperty();
    public String getMimeType();
    public long getDownloadTime();
    public ORID getVertexID();
    public void setVertexID(ORID id);
}
