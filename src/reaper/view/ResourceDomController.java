package reaper.view;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import reaper.Reaper;
import reaper.model.Crawler;
import reaper.model.Form;
import reaper.model.Link;
import reaper.model.Resource;
import reaper.model.ResourceDom;

/**
 * FXML Controller class
 *
 * @author zaraka
 */
public class ResourceDomController implements ResourceController {
    
    private static final Logger loggerReaper = Logger.getLogger(Reaper.class.getName());
    
    private ObservableList<Form> forms;
    private ObservableList<Link> links;
    
    @FXML
    private TableView<Form> formTable;
    @FXML
    private TableColumn<Form, String> formActionColumn;
    @FXML
    private TableColumn<Form, String> formMethodColumn;
    @FXML
    private TableView<Link> urlTable;
    @FXML
    private TableColumn<Link, String> urlURLColumn;
    @FXML
    private TableColumn<Link, String> urlTypeColumn;
    @FXML
    private TableColumn<Link, Integer> urlCountColumn;
    @FXML
    private Hyperlink resourceURL;
    @FXML
    private Label resourceStatusCodeProperty;
    @FXML
    private Label resourceMimeTypeProperty;
    @FXML
    private Label resourceDownloadTime;
    @FXML
    private Label resourceType;
    @FXML
    private ImageView resourceImage;
    
    @Override
    public void loadResource(Resource resource, Crawler crawler) {        
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
        formActionColumn.setCellValueFactory((TableColumn.CellDataFeatures<Form, String> p) -> new ReadOnlyObjectWrapper<>(p.getValue().getAction().toString()));
        formMethodColumn.setCellValueFactory((TableColumn.CellDataFeatures<Form, String> p) -> new ReadOnlyObjectWrapper<>(p.getValue().getMethod().toString()));
        
        resourceURL.setText(resource.getURL().toString());
        resourceURL.setOnAction((ActionEvent event) -> {
            /*
            TODO:Fixit
            try {
                new ProcessBuilder("x-www-browser", resource.getURL().toString()).start();
            } catch (IOException e) {
                loggerReaper.log(Level.SEVERE, e.getMessage());
            }
            */
        });
        resourceMimeTypeProperty.setText(resource.mimeTypeProperty().get());
        resourceStatusCodeProperty.setText(Integer.toString(resource.codeProperty().get()));
        resourceDownloadTime.setText(String.valueOf(resource.getDownloadTime()));
        resourceType.setText(resource.getType().toString());        
        
        String path = crawler.getGalleryPath();
        if (!path.endsWith("/")) {
            path += "/";
        }
        
        path += resource.getNormalizedID() + "/capture.png";
        File f = new File(path);
        if (f.exists() && f.isFile()) {
            try {
                Image image = new Image(f.toURI().toString());
                resourceImage.setImage(image);
                resourceImage.setVisible(true);
            } catch (Exception e) {
                loggerReaper.log(Level.SEVERE, e.getMessage());
            }
        } else {
            resourceImage.setVisible(false);
        }
        
    }
}
