/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper.model;

/**
 *
 * @author nikita.vanku
 */
public enum ResourceState {
    UNITIALIZED {

        @Override
        public String getInfoMessage() {
            return "Resource not yet downloaded";
        }
    },
    PROCESSING {

        @Override
        public String getInfoMessage() {
            return "Resource is processing";
        }
    },
    ERROR {

        @Override
        public String getInfoMessage() {
            return "Resource processing concluded in Error";
        }
    },
    FINISHED {

        @Override
        public String getInfoMessage() {
            return "Resource is processed";
        }
    };
    public abstract String getInfoMessage();
}
