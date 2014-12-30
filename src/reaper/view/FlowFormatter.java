package reaper.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author nikita.vanku
 */
public class FlowFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
        return dateFormat.format(record.getMillis()) + " " + record.getLevel().getName() + ": " + record.getMessage()+ "\n";
    }
    
    private String formatLevel(LogRecord record){
        return "not supported";
    }
    
}
