package reaper.model;

/**
 *
 * @author nikita.vanku
 */
public enum PreferenceKeys {
    DB_HOST {

        @Override
        public String getKey() {
            return "dbHost";
        }
    },
    DB_USER {

        @Override
        public String getKey() {
            return "dbUser";
        }
    },
    DB_PASS {

        @Override
        public String getKey() {
            return "dbPass";
        }
    };
    public abstract String getKey();
}
