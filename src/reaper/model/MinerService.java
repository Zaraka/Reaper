package reaper.model;

import com.google.common.net.InternetDomainName;
import com.orientechnologies.orient.core.exception.OStorageException;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.jsoup.UnsupportedMimeTypeException;

/**
 *
 * @author zaraka
 */
public class MinerService extends Service<Void> {

    private static final Logger loggerMiner = Logger.getLogger(MinerService.class.getName());

    private final List<String> allowedProtocols = Arrays.asList("http", "https");

    private Map<String, String> resources;
    private OrientGraphFactory graphFactory;
    private int resourceCount, linksCount;
    private LinkQue linkScrambler;
    private LinkSet linkSet;
    private Project project;

    public void init() {
        this.resources = new HashMap<>();
        this.resourceCount = 0;
        this.linksCount = 0;
        this.graphFactory = null;
        this.project = null;
        this.linkScrambler = new LinkQue(graphFactory);
        this.linkSet = new LinkSet(graphFactory);

    }

    public void prepare(Project proj) {
        this.project = proj;
    }

    public void databaseConnect(String host, String user, String pass) {
        this.databaseDisconnect();

        try {
            graphFactory = new OrientGraphFactory(host, user, pass).setupPool(1, 10);
            linkScrambler.setGraphFactory(graphFactory);
            linkSet.setFactory(graphFactory);
        } catch (OStorageException ex) {
            throw ex;
        }
    }

    public void databaseDisconnect() {
        if (graphFactory != null) {
            graphFactory.close();
        }
    }

    public int getResourceCount() {
        return this.resourceCount;
    }

    public int getLinksCount() {
        return this.linksCount;
    }

    private void createSingleVertex(Resource res) {
        OrientGraph graph = graphFactory.getTx();
        try {
            res.vertexTransaction(graph, project.getCluster());
            resourceCount++;
            resources.put(res.getURL().toString(), res.getVertexID().toString());
            linkSet.put(res.getURL().toString(), res.getVertexID().toString(), project.getCluster());
        } finally {
            graph.shutdown();
        }
    }

    private void linkTransaction(Link link) {
        OrientGraph graph = graphFactory.getTx();
        try {
            OrientVertex from, to;
            String fromID, toID;
            fromID = resources.get(link.getFromURL());
            if (fromID == null) {
                loggerMiner.log(Level.INFO, "error");
                return;
            }
            toID = resources.get(link.getToURL());
            if (toID == null) {
                loggerMiner.log(Level.INFO, "error");
                return;
            }
            from = graph.getVertex(fromID);
            to = graph.getVertex(toID);
            from.addEdge("LinkTo", to, "LinkTo",
                    DatabaseClasses.LINKTO.getName() + project.getCluster(),
                    "path", link.getLink(), "count", link.getCount(),
                    "type", link.getType().toString());
            linksCount += link.getCount();
        } finally {
            graph.shutdown();
        }
    }

    private boolean blackWhiteCheck(String host) {
        InternetDomainName check = InternetDomainName.from(host);
        InternetDomainName projectHost = InternetDomainName.from(project.getDomain().getHost());
        
        //loggerMiner.log(Level.INFO, check.toString() + " " +projectHost.toString());
        
        //check main scanning domain or whitelist
        if (!check.topPrivateDomain().equals(projectHost.topPrivateDomain())){
            
            //loop by whitelist if result is found continue
            boolean whitelisted = false;
            for (URL whiteURL : project.getWhitelist()) {
                if (check.equals(InternetDomainName.from(whiteURL.getHost()))) {
                    whitelisted = true;
                    break;
                }
            }

            if (!whitelisted) {
                return false;
            }
        }

        for (URL blackURL : project.getBlacklist()) {
            if (check.equals(InternetDomainName.from(blackURL.getHost()))) {
                return false;
            }
        }

        //loop by blacklist if result is found deny 
        return true;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                String url = project.getDomain().toString();
                if (!url.matches("^.*:\\/\\/.*$")) {
                    url = "http://" + url;
                    loggerMiner.log(Level.WARNING, "You should provide protocol as well. Default proctol http is used.");
                }

                //Grab unfinished data from database
                linkSet.fillMap(resources, project.getCluster());
                
                //If root is missing, start new minning
                if (linkScrambler.queLength(project.getCluster()) == 0) {
                    try {
                        URL uURL = new URL(url);
                        ResourceDom root = new ResourceDom(uURL, 0);
                        for (Link queLink : root.links()) {
                            linkScrambler.linkEnter(project.getCluster(), queLink);
                        }
                        createSingleVertex(root);
                        OrientGraph graph = graphFactory.getTx();
                        try {
                            project.getVertex(graph).addEdge(DatabaseClasses.ROOT.getName(), root.getVertex(graph));
                        } finally {
                            graph.shutdown();
                        }
                    } catch (UnsupportedMimeTypeException | MalformedURLException ex) {
                        throw ex;
                    }
                }
                while (linkScrambler.queLength(project.getCluster()) > 0) {
                    ODocument docLink = linkScrambler.linkLeave(project.getCluster());
                    int docDepth = docLink.field("depth");
                    Link link = new Link((String) docLink.field("from"),
                            (String) docLink.field("path"),
                            LinkType.valueOf(docLink.field("type")));
                    if (resources.get(link.getToURL()) == null) {
                        Resource toRes;
                        URL parentURL, linkUrl;
                        try {
                            parentURL = new URL(link.getFromURL());
                            linkUrl = new URL(parentURL, link.getLink());
                        } catch (MalformedURLException ex) {
                            loggerMiner.log(Level.WARNING, ex.getMessage());
                            continue;
                        }

                        //protocol check
                        if (!allowedProtocols.contains(linkUrl.getProtocol())) {
                            loggerMiner.log(Level.FINE, "Unsupported procool " + linkUrl.getProtocol());
                            continue;
                        }

                        //First check if resource is in domain
                        if (blackWhiteCheck(linkUrl.getHost())) {
                            //If so try to create DOM or FILE

                            try {
                                if (!link.getType().equals(LinkType.HYPERLINK)) {
                                    throw new UnsupportedMimeTypeException("", "", ""); //bad hack
                                }
                                toRes = new ResourceDom(linkUrl, docDepth + 1);
                                if (toRes.getDepth() < project.getDepth()) {
                                    for (Link queLink : toRes.links()) {
                                        linkScrambler.linkEnter(project.getCluster(), queLink);
                                    }
                                }
                            } catch (UnsupportedMimeTypeException ex) {
                                try {
                                    toRes = new ResourceFile(linkUrl, docDepth + 1, project.getDepth());
                                } catch (MalformedURLException ex1) {
                                    throw ex1;
                                }
                            } catch (MalformedURLException ex) {
                                throw ex;
                            }
                        } else {
                            //Otherwise fallback to outside.
                            toRes = new ResourceOutside(linkUrl, docDepth + 1, project.getDepth());
                        }
                        link.setToResource(toRes);
                        createSingleVertex(toRes);
                    }
                    linkTransaction(link);
                }
                return null;
            }
        };
    }

}
