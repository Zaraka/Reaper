package reaper.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import reaper.model.Crawler;
import reaper.model.Resource;

/**
 * FXML Controller class
 *
 * @author zaraka
 */
public class ResourceFileController implements ResourceController {

    @FXML
    private Label resourceURL;
    @FXML
    private Label resourceStatusCodeProperty;
    @FXML
    private Label resourceMimeTypeProperty;
    @FXML
    private Label resourceDownloadTime;
    @FXML
    private Label resourceType;

    @Override
    public void loadResource(Resource resource, Crawler crawler) {
        if (resource == null) {
            return;
        }
        resourceURL.setText(resource.getURL().toString());
        resourceMimeTypeProperty.setText(resource.mimeTypeProperty().get());
        resourceStatusCodeProperty.setText(Integer.toString(resource.codeProperty().get()));
        resourceDownloadTime.setText(String.valueOf(resource.getDownloadTime()));
        resourceType.setText(resource.getType().toString());
    }

}
