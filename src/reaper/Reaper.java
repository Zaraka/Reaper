/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package reaper;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import reaper.model.Domain;

/**
 * Is this the place where I should put all my business logic?
 * @author zaraka
 */
public class Reaper extends Application {
    private Domain domain;

    
    @Override
    public void start(Stage stage) throws Exception {
        URL location = getClass().getResource("Reaper.fxml");
        
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        Parent root = (Parent) fxmlLoader.load(location.openStream());
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
        
        //ReaperController controller = fxmlLoader.getController();
        //controller.setReaper(this);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public Domain getDomain(){
        return this.domain;
    }
    
}
