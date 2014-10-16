/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package reaper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import reaper.model.Domain;
import reaper.view.ReaperController;

/**
 * Is this the place where I should put all my business logic?
 * @author zaraka
 */
public class Reaper extends Application {
    private Domain domain;
    
    Reaper(){
        
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResource("Reaper.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
        
        ReaperController controller = loader.getController();
        controller.setReaper(this);
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
