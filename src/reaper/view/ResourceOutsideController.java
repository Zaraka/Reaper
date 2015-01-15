package reaper.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import reaper.model.Resource;

/**
 * FXML Controller class
 *
 * @author nikita.vanku
 */
public class ResourceOutsideController implements ResourceController {
    @FXML
    private Label resourceURL;
    @FXML
    private Label resourceType;
    
    @Override
    public void loadResource(Resource resource) {
        if(resource == null){
            return;
        }
        
        resourceURL.setText(resource.getPath());
        resourceType.setText(resource.getType().toString());
    }

}
