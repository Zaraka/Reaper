package reaper;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import reaper.model.Crawler;
import reaper.view.ReaperController;

/**
 * Is this the place where I should put all my business logic?
 * @author zaraka
 */
public class Reaper extends Application {
    
    private static final Logger logger = Logger.getLogger(Reaper.class.getName());
    private Crawler crawler;
    
    @Override
    public void start(Stage stage) throws Exception {
        logger.setLevel(Level.ALL);
        
        URL location = getClass().getResource("view/Reaper.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(location);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        
        Parent root = (Parent) loader.load(location.openStream());
        
        Scene scene = new Scene(root);
        stage.setTitle("Reaper - Web Crawler");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
        
        this.initialize();
        
        ReaperController controller = (ReaperController)loader.getController();
        controller.setReaper(this);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public Crawler getCrawler(){
        return this.crawler;
    }
    
    private void initialize(){
        this.crawler = new Crawler();
    }
    
}
