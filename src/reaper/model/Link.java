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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import reaper.Reaper;

/**
 *
 * @author nikita.vanku
 */
public class Link {

    private static final Logger logger = Logger.getLogger(Reaper.class.getName());

    private final StringProperty link;
    private Resource fromResource;
    private Resource toResource;
    private final IntegerProperty count;
    private LinkType type;
    private String fromURL, toURL;

    Link() {
        this.link = new SimpleStringProperty("undefined");
        this.fromResource = null;
        this.toResource = null;
        this.count = new SimpleIntegerProperty(1);
        this.type = LinkType.UNDEFINED;
    }

    Link(String fromPath, String link, LinkType type){
        this.link = new SimpleStringProperty(link);
        this.count = new SimpleIntegerProperty(1);
        this.fromResource = null;
        this.toResource = null;
        this.type = type;
        this.fromURL = fromPath;
        try {
            URL rfromURL = new URL(fromPath);
            this.toURL = new URL(rfromURL, link).toString();
        } catch(MalformedURLException ex){
            
        }
    }
    
    Link(String link, Resource source) {
        this.link = new SimpleStringProperty(link);
        this.count = new SimpleIntegerProperty(1);
        this.fromResource = source;
        this.type = LinkType.UNDEFINED;
        this.fromResource = source;
        this.toResource = null;
        this.fromURL = source.getURL().toString();
        try {
            this.toURL = new URL(source.getURL(), link).toString();
        } catch(MalformedURLException ex){
            
        }
        
    }

    Link(String link, Resource source, LinkType type) {
        this.link = new SimpleStringProperty(link);
        this.count = new SimpleIntegerProperty(1);
        this.fromResource = source;
        this.type = type;
        this.fromResource = source;
        this.toResource = null;
        this.fromURL = source.getURL().toString();
        try {
            this.toURL = new URL(source.getURL(), link).toString();
        } catch(MalformedURLException ex){
            
        }
    }

    Link(String link, Resource source, Resource destination, LinkType type) {
        this.link = new SimpleStringProperty(link);
        this.count = new SimpleIntegerProperty(1);
        this.fromResource = source;
        this.toResource = destination;
        this.type = type;
        this.fromResource = source;
        this.toResource = destination;
        this.fromURL = source.getURL().toString();
        this.toURL = destination.getURL().toString();
    }

    public void setLink(String path) {
        this.link.set(path);
    }

    public String getLink() {
        return this.link.get();
    }

    public StringProperty linkProperty() {
        return this.link;
    }

    public Resource getFromResource() {
        return this.fromResource;
    }

    public void setFromResource(Resource res) {
        this.fromResource = res;
        this.fromURL = res.getURL().toString();
    }

    public Resource getToResource() {
        return this.toResource;
    }

    public void setToResource(Resource to) {
        this.toResource = to;
        this.toURL = to.getURL().toString();
    }

    public String getEdgeFormat() {
        if (this.fromResource != null && this.toResource != null) {
            String fromUrl = this.getFromResource().getURL().toString();
            String toUrl = this.getToResource().getURL().toString();
            return fromUrl + "<=>" + toUrl;
        }
        return null;
    }

    public int getCount() {
        return this.count.get();
    }

    public void setCount(int count) {
        this.count.set(count);
    }

    public void addCount() {
        this.setCount(this.getCount() + 1);
    }
    
    public IntegerProperty countProperty(){
        return this.count;
    }

    public LinkType getType() {
        return this.type;
    }

    public void setType(LinkType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        String from = "";
        String to = "";
        if (this.fromResource != null) {
            from = this.fromResource.getURL().toString();
        }
        if (this.toResource != null) {
            to = this.toResource.getURL().toString();
        }
        String result = from + ">" + this.link.get() + ">" + to;
        return result.trim();
    }
    
    public String getFromURL(){
        return this.fromURL;
    }
    
    public String getToURL(){
        return this.toURL;
    }
}
