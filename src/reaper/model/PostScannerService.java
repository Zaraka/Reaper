package reaper.model;

import com.orientechnologies.orient.core.exception.OStorageException;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import java.util.List;
import java.util.logging.Logger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *  Post scanner service for picture grabbing etc.
 *  After minning is finished. Post scanner load all DOM vertices from DB
 *  and try to capture a screenshot by PhantomJS. This task is considered
 *  long.
 * 
 * @author zaraka
 */
public class PostScannerService extends Service<Void>{

    private static final Logger logger = Logger.getLogger(reaper.Reaper.class.getName());
    
    private OrientGraphFactory graphFactory;
    private Project project;
    private List<Resource> resources;
    
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
    
    public void init(){
        this.graphFactory = null;
        this.project = null;
    }
    
    public void prepare(Project proj) {
        this.project = proj;
    }
    
    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                //Load DOM resources
                 
                
                //Capture them by PhantomJS
                
                return null;
            }
        };
    }
    
}
