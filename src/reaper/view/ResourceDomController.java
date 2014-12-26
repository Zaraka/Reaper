package reaper.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import reaper.model.Form;
import reaper.model.Link;
import reaper.model.Method;
import reaper.model.Resource;

/**
 * FXML Controller class
 *
 * @author zaraka
 */
public class ResourceDomController implements ResourceController {

    @FXML
    private TableView<Form> formTable;
    @FXML
    private TableColumn<Link, String> formActionColumn;
    @FXML
    private TableColumn<Method, String> formMethodColumn;
    @FXML
    private TableView<Link> urlTable;
    @FXML
    private TableColumn<Link, String> urlColumn;
    @FXML
    private TableColumn<Link, String> typeColumn;
    @FXML
    private TableColumn<Link, Integer> countColumn;
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
    public void loadResource(Resource resource) {
        if (resource == null) {
            return;
        }
        resourceURL.setText(resource.getPath());
        urlTable.setItems(FXCollections.observableArrayList(resource.links()));
        urlColumn.setCellValueFactory(cellData -> cellData.getValue().linkProperty());
        resourceMimeTypeProperty.setText(resource.mimeTypeProperty().get());
        resourceStatusCodeProperty.setText(Integer.toString(resource.codeProperty().get()));
        resourceDownloadTime.setText(String.valueOf(resource.getDownloadTime()));
        resourceType.setText(resource.getType().toString());
    }
}
