/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.view;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author nikita.vanku
 */
public class FlowHandler extends Handler {
    TextFlow console;
    
    public FlowHandler(TextFlow output){
        super();
        
        this.console = output;
        
        this.setFormatter(new FlowFormatter());
    }

    @Override
    public void publish(LogRecord record) {        
        Text msg = new Text(this.getFormatter().format(record));
        
        msg.setFont(Font.font("Monospace"));
        
        if(record.getLevel() == Level.SEVERE){
            msg.setFill(Color.RED);
        } else if(record.getLevel() == Level.WARNING){
            msg.setFill(Color.DARKORANGE);
        } else if (record.getLevel() == Level.FINE){
            //msg.setFill(Color.DARKGRAY);
        } else {
            msg.setFill(Color.BLACK);
        }
        
        console.getChildren().add(msg);
    }

    @Override
    public void flush() {
        
    }

    @Override
    public void close() throws SecurityException {
        
    }
}
