package reaper.view;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import reaper.model.Form;
import reaper.model.Link;
import reaper.model.RestMethod;
import reaper.model.Resource;
import reaper.model.ResourceDom;

/**
 * FXML Controller class
 *
 * @author zaraka
 */
public class ResourceDomController implements ResourceController {
    
    private ObservableList<Form> forms;
    private ObservableList<Link> links;

    @FXML
    private TableView<Form> formTable;
    @FXML
    private TableColumn<Link, String> formActionColumn;
    @FXML
    private TableColumn<RestMethod, String> formMethodColumn;
    @FXML
    private TableView<Link> urlTable;
    @FXML
    private TableColumn<Link, String> urlURLColumn;
    @FXML
    private TableColumn<Link, String> urlTypeColumn;
    @FXML
    private TableColumn<Link, Integer> urlCountColumn;
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
        forms = FXCollections.observableArrayList();
        links = FXCollections.observableArrayList();
        links.addAll(resource.links());
        ResourceDom dom = (ResourceDom) resource;
        forms.addAll(dom.forms());
        
        
        urlTable.setItems(links);
        urlURLColumn.setCellValueFactory(cellData -> cellData.getValue().linkProperty());
        urlTypeColumn.setCellValueFactory((TableColumn.CellDataFeatures<Link, String> p) -> new ReadOnlyObjectWrapper<>(p.getValue().getType().toString()));
        urlCountColumn.setCellValueFactory((TableColumn.CellDataFeatures<Link, Integer> p) -> new ReadOnlyObjectWrapper<>(p.getValue().getCount()));
        
        formTable.setItems(forms);
        
        
        resourceURL.setText(resource.getPath());
        resourceMimeTypeProperty.setText(resource.mimeTypeProperty().get());
        resourceStatusCodeProperty.setText(Integer.toString(resource.codeProperty().get()));
        resourceDownloadTime.setText(String.valueOf(resource.getDownloadTime()));
        resourceType.setText(resource.getType().toString());
        
        
    }
}
