package reaper.view;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
        
        console.getChildren().add(msg);
    }

    @Override
    public void flush() {
        
    }

    @Override
    public void close() throws SecurityException {
        
    }
}
