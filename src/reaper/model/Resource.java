/*
 * The MIT License
 *
 * Copyright 2015 Reaper.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
    public IntegerProperty codeProperty();
    public IntegerProperty depthProperty();
    public StringProperty pathProperty();
    public StringProperty mimeTypeProperty();
    
    
    public ArrayList<Link> links();
    public String getPath();
    public void setPath(String path);
    public URL getURL();
    public void setURL(URL url);
    public int getDepth();
    public void setDepth(int depth);
    public ResourceType getType();
    public void setType(ResourceType type);
    public Link getLinkWithPath(String Path);
    public int getCode();
    public void setCode(int code);
    public String getMimeType();
    public void setMimeType(String mimeType);
    public long getDownloadTime();
    public void setDownloadTime(long downloadTime);
    public Object getVertexID();
    public void setVertexID(Object id);
    public void vertexTransaction(OrientGraph grapth, String cluster);
    public String getNormalizedID();
}
