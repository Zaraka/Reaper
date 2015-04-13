package reaper.view;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import reaper.Reaper;

/**
 *
 * @author nikita.vanku
 */
public class NewProjectModal extends Stage{
    private static final Logger logger = Logger.getLogger(Reaper.class.getName());
    
    private final NewProjectModalController controller;
    
    public NewProjectModal(Stage owner){
        super();
        initOwner(owner);
        setTitle("Create new project");
        initModality(Modality.APPLICATION_MODAL);
        
        URL location = getClass().getResource("NewProjectModal.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(location);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        
        Parent root;
        try {
            root = (Parent) loader.load(location.openStream());
            Scene scene = new Scene(root);
            setScene(scene);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
        controller = (NewProjectModalController) loader.getController();
    }
    
    public URL getDomain(){
        return controller.getDomain();
    }
    
    public String getName(){
        return controller.getName();
    }
    
    public ArrayList<URL> getBlacklist(){
        return controller.getBlacklist();
    }
    
    public boolean getAccepted(){
        return controller.getAccepted();
    }
    
    public int getDepth(){
        return controller.getDepth();
    }
}
