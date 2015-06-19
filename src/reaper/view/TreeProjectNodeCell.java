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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;

/**
 *
 * @author nikita.vanku
 */
public class TreeProjectNodeCell extends TreeCell<TreeProjectNode> {

    private ContextMenu menu;

    public TreeProjectNodeCell() {
    }

    @Override
    protected void updateItem(TreeProjectNode item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            switch (item.getNodeType()) {
                case PROJECT:
                    setGraphic(new FontAwesomeIconView(FontAwesomeIcon.FOLDER_ALT));
                    setText(item.getProject().getName());
                    menu = new ContextMenu();
                    MenuItem settings = new MenuItem("Settings");
                    settings.setOnAction((ActionEvent event) -> {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    });
                    menu.getItems().add(settings);
                    setContextMenu(menu);
                    break;
                case STATISTICS:
                    setText(item.getProject().getName());
                    setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PIE_CHART));
                    break;
                case VIEW:
                    setText(item.getProject().getName());
                    setGraphic(new FontAwesomeIconView(FontAwesomeIcon.FILE_ALT));
                    break;
                default:
                    setText("undefined");
                    setGraphic(null);
                    break;
            }
        }
    }
}
