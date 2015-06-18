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
    
    public ArrayList<String> getBlacklist(){
        return controller.getBlacklist();
    }
    
    public ArrayList<String> getWhitelist(){
        return controller.getWhitelist();
    }
    
    public boolean getAccepted(){
        return controller.getAccepted();
    }
    
    public int getDepth(){
        return controller.getDepth();
    }
}
