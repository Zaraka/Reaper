package reaper.model;

import com.orientechnologies.orient.core.exception.OStorageException;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Post scanner service for picture grabbing etc. After mining is finished. Post
 * scanner load all DOM vertices from DB and try to capture a screen shot by
 * PhantomJS. This task is considered long.
 *
 * @author zaraka
 */
public class PostScannerService extends Service<Void> {

    private static final Logger loggerPostScanner = Logger.getLogger(PostScannerService.class.getName());

    private OrientGraphFactory graphFactory;
    private Project project;
    private List<ResourceDom> resources;
    private String phantomPath;
    private String galleryPath;

    public void databaseConnect(String host, String user, String pass) {
        this.databaseDisconnect();

        try {
            graphFactory = new OrientGraphFactory(host, user, pass).setupPool(1, 10);
        } catch (OStorageException ex) {
            throw ex;
        }
    }

    public void databaseDisconnect() {
        if (graphFactory != null) {
            graphFactory.close();
        }
    }

    public void init() {
        this.graphFactory = null;
        this.project = null;
        this.resources = new ArrayList<>();
    }

    /**
     * Before starting service, setup scanner by this
     *
     * @param proj
     * @param phantomPath
     * @param galleryPath
     */
    public void prepare(Project proj, String phantomPath, String galleryPath) {
        this.project = proj;
        this.phantomPath = phantomPath;
        this.galleryPath = galleryPath;
        if (!this.galleryPath.endsWith("/")) {
            this.galleryPath += "/";
        }
        this.resources.clear();
    }

    /**
     * Fill Resource list
     */
    private void grabData() {
        OrientGraph graph = graphFactory.getTx();
        try {
            for (Vertex ver : (Iterable<Vertex>) graph.command(
                    new OSQLSynchQuery(
                            "SELECT * FROM cluster:"
                            + DatabaseClasses.RESOURCE
                            + project.getCluster()
                            + " where type = 'DOM'")
            ).execute()) {
                try {
                    ResourceDom res = new ResourceDom(ver);
                    resources.add(res);
                } catch (MalformedURLException ex) {
                    loggerPostScanner.log(Level.WARNING, ex.getMessage());
                }
            }
        } finally {
            graph.shutdown();
        }
    }

    /**
     * Create Directory tree from resource list
     */
    private void createDirectoryTree() {
        String projectGalleryPath = galleryPath + project.getName() + "/";
        for (ResourceDom res : resources) {
            File resFile = new File(projectGalleryPath + res.getNormalizedID());
            resFile.mkdirs();
        }
    }

    /**
     * Run PhantomJS for all web documents
     */
    private void phantomCapture() {
        String projectGalleryPath = galleryPath + project.getName() + "/";
        
        for (ResourceDom res : resources) {
            String resourceDir = projectGalleryPath + res.getNormalizedID() + "/";
            try {
                ProcessBuilder pb = new ProcessBuilder(phantomPath, "capture.js", 
                        res.getURL().toString(), resourceDir + "capture.png");
                pb.redirectOutput(new File(resourceDir + "javascript.txt"));
                pb.start().waitFor();

            } catch (IOException | InterruptedException ex) {
                loggerPostScanner.log(Level.SEVERE, ex.getMessage());
            }
        }
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                //Load DOM resources
                loggerPostScanner.log(Level.INFO, "Loading resources");
                grabData();
                //Create directory tree
                loggerPostScanner.log(Level.INFO, "Creating directory tree");
                createDirectoryTree();
                //Capture them by PhantomJS
                loggerPostScanner.log(Level.INFO, "Capturing images");
                phantomCapture();

                loggerPostScanner.log(Level.INFO, "Done");
                return null;
            }
        };
    }

}
