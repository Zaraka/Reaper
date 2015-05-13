package reaper.view;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
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
    @FXML
    private TextArea javascriptOutputTextArea;

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

        MenuItem copyMenuItem = new MenuItem("Copy");
        copyMenuItem.setOnAction((ActionEvent event) -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(resourceURL.getText());
            content.putHtml("<a href=\"" + resourceURL.getText() + "\">" + resourceURL.getText() + "</a>");
            clipboard.setContent(content);
        });
        resourceURL.setContextMenu(new ContextMenu(
                copyMenuItem
        ));

        resourceMimeTypeProperty.setText(resource.mimeTypeProperty().get());
        resourceStatusCodeProperty.setText(Integer.toString(resource.codeProperty().get()));
        resourceDownloadTime.setText(String.valueOf(resource.getDownloadTime()) + " ms");
        resourceType.setText(resource.getType().toString());

        String dirPath = crawler.getGalleryPath();
        if (!dirPath.endsWith("/")) {
            dirPath += "/";
        }

        dirPath += crawler.getActiveProject().getName() + "/";
        dirPath += resource.getNormalizedID() + "/";
        String capturePath = dirPath + "capture.png";
        String outputPath = dirPath + "javascript.txt";
        File f = new File(capturePath);
        if (f.exists() && f.isFile()) {
            try {
                Image image = new Image(f.toURI().toString());
                resourceImage.setImage(image);
                resourceImage.setVisible(true);

                StringBuilder sb = new StringBuilder();
                FileInputStream fis = new FileInputStream(outputPath);
                BufferedInputStream bis = new BufferedInputStream(fis);
                while (bis.available() > 0) {
                    sb.append((char) bis.read());
                }
                javascriptOutputTextArea.setText(sb.toString());

            } catch (Exception e) {
                loggerReaper.log(Level.SEVERE, e.getMessage());
            }
        } else {
            resourceImage.setVisible(false);
        }

    }
}
