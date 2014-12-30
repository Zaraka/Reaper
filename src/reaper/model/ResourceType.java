package reaper.model;

/**
 *
 * @author nikita.vanku
 */
public enum ResourceType {
    FILE {

        @Override
        public String getGroup() {
            return "FILE";
        }
    },
    DOM {
        
        @Override
        public String getGroup() {
            return "DOM";
        }
    },
    OUTSIDE {

        @Override
        public String getGroup() {
            return "OUTSIDE";
        }
        
    },
    UNDEFINED {

        @Override
        public String getGroup() {
            return "UNDEFINED";
        }
    };
    public abstract String getGroup();
}
