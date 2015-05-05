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
    },
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
    },
    PHANTOM_PATH {
        @Override
        public String getKey() {
            return "phantomPath";
        }        
    },
    GALLERY_PATH {
        @Override
        public String getKey(){
            return "galleryPath";
        }
    };
    public abstract String getKey();
}
