package reaper.model;

/**
 * Define names of classes in databases & another usefull data
 * @author zaraka
 */
public enum DatabaseClasses {
    RESOURCE {
        @Override
        public String getType() {
            return "V";
        }

        @Override
        public String getName() {
            return "Resource";
        }
    },
    FORM {
        @Override
        public String getType() {
            return "V";
        }
        @Override
        public String getName() {
            return "Form";
        }
    },
    BLACWHITEKLIST {
        @Override
        public String getType() {
            return "V";
        }

        @Override
        public String getName() {
            return "BlackWhiteList";
        }
    },
    PROJECT {
        @Override
        public String getType() {
            return "V";
        }
        @Override
        public String getName() {
            return "Project";
        }
    },
    INCLUDES {
        @Override
        public String getType() {
            return "E";
        }

        @Override
        public String getName() {
            return "Includes";
        }
    },
    ROOT {

        @Override
        public String getType() {
            return "E";
        }

        @Override
        public String getName() {
            return "Root";
        }
    },
    LINKTO {
        @Override
        public String getType() {
            return "E";
        }

        @Override
        public String getName() {
            return "LinkTo";
        }
    },
    LINKQUE {
        @Override
        public String getType() {
            return "D";            
        }

        @Override
        public String getName() {
            return "LinkQue";
        }
        
    },
    LINKSET {
        @Override
        public String getType() {
            return "D";
        }

        @Override
        public String getName() {
            return "LinkSet";
        }
        
    };
    public abstract String getType();
    public abstract String getName();
}
