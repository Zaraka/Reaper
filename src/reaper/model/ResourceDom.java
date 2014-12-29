package reaper.model;

import com.tinkerpop.blueprints.Vertex;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import reaper.Reaper;
import reaper.exceptions.OutsidePageException;

/**
 *
 * @author nikita.vanku
 */
public class ResourceDom extends ResourceAbstract {

    private static final Logger logger = Logger.getLogger(Reaper.class.getName());

    private Document doc;
    private ArrayList<Form> forms;
    private static final String JsoupUserAgent = "Reaper/1.0";

    ResourceDom() {
        super();

        this.forms = new ArrayList<>();
        this.type = ResourceType.DOM;
    }

    ResourceDom(URL url, int depth, int maxDepth, Resource parent) throws UnsupportedMimeTypeException, MalformedURLException, OutsidePageException {
        super(url, depth, maxDepth, parent);

        this.forms = new ArrayList<>();
        this.type = ResourceType.DOM;

        if (this.parent != null) {
            //First check if resource is in domain
            if (!url.getHost().equals(parent.getURL().getHost())) {
                throw new OutsidePageException("Document isn't in scanned area");
            }
        }

        //then check if resource has DOM element
        try {
            this.load();
        } catch (UnsupportedMimeTypeException ex) {
            throw ex;
        }
    }
    
    ResourceDom(Vertex vertex) throws MalformedURLException{
        super(vertex);
        
        this.forms = new ArrayList<>();
        this.type = ResourceType.DOM;
        
    }

    private void scratchURLs() {
        this.retrieveScripts();
        this.retrieveLinks();
        this.retrieveHyperlinks();
        this.retrieveForms();
    }
    
    private void retrieveScripts() {
        Elements docScripts = this.doc.getElementsByTag("script");
        for(Element script : docScripts){
            if(script.hasAttr("src")){
                String src = script.attr("src").trim();
                String key = "script#" + src;
                if(this.links.containsKey(key)){
                    this.links.get(key).addCount();
                } else {
                    this.links.put(key, new Link(src, this, LinkType.SCRIPT));
                }
            }
        }
    }
    
    private void retrieveLinks(){
        Elements docLinks = this.doc.getElementsByTag("link");
        for(Element link : docLinks) {
            String href = link.attr("href").trim();
            Link newLink = new Link(href, this, LinkType.LINK_UNDEFINED);
            String rel = link.attr("rel").trim().toLowerCase();
            switch(rel){
                case "icon":
                    newLink.setType(LinkType.ICON);
                    break;
                case "author":
                    newLink.setType(LinkType.AUTHOR);
                    break;
                case "stylesheet":
                    newLink.setType(LinkType.STYLESHEET);
                    break;
            }
            String key = "link#" + href;
            if(this.links.containsKey(key)){
                this.links.get(key).addCount();
            } else {
                this.links.put(key, newLink);
            }
        }
    }

    private void retrieveHyperlinks() {
        Elements docLinks = this.doc.getElementsByTag("a");
        for (Element hyperlink : docLinks) {
            String href = hyperlink.attr("href").trim();
            String key = "a#" + href;
            if(this.links.containsKey(key)){
                this.links.get(key).addCount();
            } else {
                this.links.put(key, new Link(href, this, LinkType.HYPERLINK));
            }
        }
    }

    private void retrieveForms() {
        Elements docForms = this.doc.getElementsByTag("form");
        for (Element form : docForms) {
            Method method = Method.GET;
            if (form.hasAttr("mehod")) {
                String methodStr = form.attr("method").trim().toUpperCase();
                if ("POST".equals(methodStr)) {
                    method = Method.POST;
                } else if ("PUT".equals(methodStr)) {
                    method = Method.PUT;
                } else if ("DELETE".equals(methodStr)) {
                    method = Method.DELETE;
                }
            }
            String formAction = "";
            if (form.hasAttr("action")) {
                formAction = form.attr("action").trim();
            }
            Link link = new Link(formAction, this);
            this.forms.add(new Form(link, method));
        }
    }

    private void load() throws UnsupportedMimeTypeException {
        this.state = ResourceState.PROCESSING;
        System.out.println("Resource " + this.url.toString() + " loading START");
        try {
            long startTime = System.currentTimeMillis();
            Connection.Response response = Jsoup.connect(this.url.toString()).userAgent(JsoupUserAgent).execute();
            //measure time
            this.downloadTime = System.currentTimeMillis() - startTime;

            this.setCode(response.statusCode());
            this.doc = response.parse();
            this.setMimeType(response.contentType());
        } catch (UnsupportedMimeTypeException ex) {
            this.setMimeType(ex.getMimeType());
            throw ex;
        } catch (HttpStatusException ex) {
            //I dont give a fuuuck
            this.setCode(ex.getStatusCode());
            System.out.println("URL " + ex.getUrl() + " code: " + ex.getStatusCode());
            this.state = ResourceState.ERROR;
        } catch (UnknownHostException ex) {
            System.out.println("Cannot reach host " + url);
            this.state = ResourceState.ERROR;
        } catch (IOException ex) {
            System.out.println(ex);
            this.state = ResourceState.ERROR;
        }

        if (this.state == ResourceState.PROCESSING) {
            this.state = ResourceState.FINISHED;
            this.scratchURLs();
        }
    }
}
