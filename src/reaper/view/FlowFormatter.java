package reaper.view;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author nikita.vanku
 */
public class FlowFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return record.getLevel().getName() + ": " + record.getMessage()+ "\n";
    }
    
    private String formatLevel(LogRecord record){
        return "not supported";
    }
    
}
