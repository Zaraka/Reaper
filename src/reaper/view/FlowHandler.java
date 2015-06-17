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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Reaper Flow Console Logger Handler.
 * This handler will append logger message to TextFlow node and color tab background
 * @author nikita.vanku
 */
public class FlowHandler extends Handler {
    TextFlow console;
    Tab tab;
    
    public FlowHandler(TextFlow output, Tab tab){
        super();
        
        this.console = output;
        this.tab = tab;
        
        this.setFormatter(new FlowFormatter());
    }

    @Override
    public void publish(LogRecord record) {        
        Text msg = new Text(this.getFormatter().format(record));
        
        msg.setFont(Font.font("Monospace", 14));
        
        if(record.getLevel() == Level.SEVERE){
            msg.setFill(Color.RED);
            msg.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        } else if(record.getLevel() == Level.WARNING){
            msg.setFill(Color.DARKORANGE);
        } else if (record.getLevel() == Level.FINE){
            //msg.setFill(Color.DARKGRAY);
        } else {
            msg.setFill(Color.BLACK);
        }
        
        Platform.runLater(() -> {
            //Should check how slow this is
            console.getChildren().add(msg);
            if(!tab.isSelected()){
                tab.getStyleClass().add("tab-unread");
            }
        });
    }

    @Override
    public void flush() {
        //Nothing to do here
    }

    @Override
    public void close() throws SecurityException {
        //Nothing to do here
    }
}
