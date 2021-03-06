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

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model
 *
 * @author zaraka
 */
public abstract class ResourceAbstract extends VertexAbstract implements Resource {
    private static final Logger loggerReaper = Logger.getLogger(reaper.Reaper.class.getName());
    private static final Logger loggerMiner = Logger.getLogger(reaper.model.MinerService.class.getName());

    protected ResourceType type;
    protected URL url;
    protected long downloadTime;
    protected ResourceState state;
    protected Map<String, Link> links;
    private final StringProperty mimeType;
    private final StringProperty path;
    private final IntegerProperty code;
    private final IntegerProperty depth;

    ResourceAbstract() {
        this.url = null;
        this.state = ResourceState.UNITIALIZED;
        this.depth = new SimpleIntegerProperty(0);
        this.links = new HashMap();
        this.type = ResourceType.UNDEFINED;
        this.code = new SimpleIntegerProperty(0);
        this.mimeType = new SimpleStringProperty("");
        this.path = new SimpleStringProperty("");
        this.downloadTime = 0;
        this.id = null;
    }

    ResourceAbstract(URL url, int depth) throws MalformedURLException {
        this.url = url;
        this.state = ResourceState.UNITIALIZED;
        this.depth = new SimpleIntegerProperty(depth);
        this.links = new HashMap();
        this.type = ResourceType.UNDEFINED;
        this.code = new SimpleIntegerProperty(0);
        this.mimeType = new SimpleStringProperty("");
        this.path = new SimpleStringProperty(url.getPath());
        this.downloadTime = 0;
        this.id = null;
    }

    ResourceAbstract(Vertex vertex) throws MalformedURLException {
        super(vertex);

        this.state = ResourceState.UNITIALIZED;
        this.depth = new SimpleIntegerProperty(0);
        this.links = new HashMap<>();
        this.type = ResourceType.UNDEFINED;

        this.url = new URL(vertex.getProperty("url").toString());
        this.code = new SimpleIntegerProperty((int) vertex.getProperty("code"));
        this.downloadTime = (long) vertex.getProperty("downloadTime");
        this.mimeType = new SimpleStringProperty(vertex.getProperty("mimeType").toString());
        this.path = new SimpleStringProperty(url.getPath());
    }

    @Override
    public String getPath() {
        return this.path.get();
    }

    @Override
    public URL getURL() {
        return this.url;
    }

    @Override
    public void setPath(String path) {
        this.path.set(path);
    }

    @Override
    public void setDepth(int depth) {
        this.depth.set(depth);
    }

    @Override
    public int getDepth() {
        return this.depth.get();
    }

    public IntegerProperty getDepthProperty() {
        return this.depth;
    }
    
    @Override
    public ArrayList<Link> links() {
        return new ArrayList<>(links.values());
    }

    @Override
    public int getCode() {
        return this.code.get();
    }

    @Override
    public void setCode(int code) {
        this.code.set(code);
    }

    @Override
    public IntegerProperty codeProperty() {
        return this.code;
    }

    @Override
    public ResourceType getType() {
        return this.type;
    }

    @Override
    public Link getLinkWithPath(String path) {
        return this.links.get(path);
    }

    @Override
    public StringProperty mimeTypeProperty() {
        return this.mimeType;
    }

    @Override
    public String getMimeType() {
        return this.mimeType.get();
    }

    @Override
    public void setMimeType(String type) {
        this.mimeType.set(type);
    }

    @Override
    public StringProperty pathProperty(){
        return path;
    }
    
    @Override
    public IntegerProperty depthProperty(){
        return this.depth;
    }
    
    @Override
    public long getDownloadTime() {
        return this.downloadTime;
    }

    @Override
    public Object getVertexID() {
        return this.id;
    }

    @Override
    public void setVertexID(Object or) {
        this.id = or;
    }

    @Override
    public void vertexTransaction(OrientGraph graph, String cluster) {
        try {
            OrientVertex vertex = graph.addVertex(
                    DatabaseClasses.RESOURCE.getName(),
                    DatabaseClasses.RESOURCE.getName() + cluster);
            vertex.setProperties(
                    "url", this.getURL().toString(), "code", this.getCode(),
                    "downloadTime", this.getDownloadTime(), "mimeType", this.getMimeType(),
                    "type", this.getType().toString());
            graph.commit();
            this.setVertexID(vertex.getId());
        } finally {
            graph.shutdown();
        }
    }

    @Override
    public void save(OrientGraph graph) {
        OrientVertex vertex = graph.getVertex(id);
        vertex.setProperties(
                "url", this.getURL().toString(), "code", this.getCode(),
                "downloadTime", this.getDownloadTime(), "mimeType", this.getMimeType(),
                "type", this.getType().toString());
    }

    @Override
    public void update(OrientGraph graph) {
        OrientVertex vertex = graph.getVertex(id);
        
        try {
            url = new URL(vertex.getProperty("url").toString());
        } catch (MalformedURLException ex) {
            url = null;
        }
        code.set((int) vertex.getProperty("code"));
        downloadTime = (long) vertex.getProperty("downloadTime");
        mimeType.set(vertex.getProperty("mimeType").toString());
    }
    
    @Override
    public String getNormalizedID(){
        return id.toString().replaceAll(":", "_");
    }
    
    
    @Override
    public void setURL(URL url) {
        this.url = url;
    }

    @Override
    public void setType(ResourceType type) {
        this.type = type;
    }

    @Override
    public void setDownloadTime(long downloadTime) {
        this.downloadTime = downloadTime;
    }
}
