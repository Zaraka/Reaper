package reaper.model;

/**
 *
 * @author nikita.vanku
 */
public enum ResourceType {
    FILE {

        @Override
        public String getGroup() {
            return "file";
        }
    },
    DOM {

        @Override
        public String getGroup() {
            return "dom";
        }
    },
    UNDEFINED {

        @Override
        public String getGroup() {
            return "undefined";
        }
    };
    public abstract String getGroup();
}
