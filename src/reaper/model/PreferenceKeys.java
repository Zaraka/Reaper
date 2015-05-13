package reaper.model;

/**
 *
 * @author nikita.vanku
 */
public enum PreferenceKeys {
    CREATE_SITEMAP {
        @Override
        public String getKey() {
            return "createSitemap";
        }

        @Override
        public Object getValue() {
            return true;
        }
    },
    DB_HOST {
        @Override
        public String getKey() {
            return "dbHost";
        }

        @Override
        public Object getValue() {
            return "remote:localhost/ReaperTest";
        }
    },
    DB_USER {
        @Override
        public String getKey() {
            return "dbUser";
        }

        @Override
        public Object getValue() {
            return "admin";
        }
    },
    DB_PASS {
        @Override
        public String getKey() {
            return "dbPass";
        }

        @Override
        public Object getValue() {
            return "root";
        }
    },
    PHANTOM_PATH {
        @Override
        public String getKey() {
            return "phantomPath";
        }        

        @Override
        public Object getValue() {
            return "";
        }
    },
    GALLERY_PATH {
        @Override
        public String getKey(){
            return "galleryPath";
        }

        @Override
        public Object getValue() {
            return System.getProperty("user.home") + "/Reaper/";
        }
    },
    AUTO_CONNECT {
        @Override
        public String getKey() {
            return "autoConnect";
        }

        @Override
        public Object getValue() {
            return false;
        }
    };
    public abstract String getKey();
    public abstract Object getValue();
}
