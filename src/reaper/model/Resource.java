package reaper.model;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
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
    public IntegerProperty depthProperty();
    public StringProperty pathProperty();
    public StringProperty mimeTypeProperty();
    public int getCode();
    public String getMimeType();
    public long getDownloadTime();
    public Object getVertexID();
    public void setVertexID(Object id);
    public void vertexTransaction(OrientGraph grapth, String cluster);
}
