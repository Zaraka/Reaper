package reaper.view;

import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import reaper.model.Resource;

/**
 *
 * @author zaraka
 */
public class GenericCellFactory implements Callback<TableColumn<Resource, String>,TableCell<Resource, String>> {
    
    ContextMenu menu;
    EventHandler click;
    
    public GenericCellFactory(EventHandler click, ContextMenu menu) {
        this.menu = menu;
        this.click = click;
    }

    @Override
    public TableCell call(TableColumn p) {
        TableCell cell = new TableCell() {
            @Override 
            protected void updateItem(Object item, boolean empty) {
                 // calling super here is very important - don't skip this!
                 super.updateItem(item, empty);
                 if(item != null) {
                     setText(item.toString());
                 }
            }
       };
        
       // Right click
       if(menu != null) {
          cell.setContextMenu(menu);
       }
       // Double click
       if(click != null) {
          cell.setOnMouseClicked(click);
       }
        
       return cell;
    }
}

