/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Model
 * @author zaraka
 */
public class Resource {
    private String path;
    private Document doc;
    
    Resource(){
        this.path = "undefined";
    }
    
    Resource(String path){
        this.path = path;
    }
    
    public List<String> getLinks(){
        List<String> result;
        
        
        return result;
    }
    
    private void load(){
        try {
            this.doc = Jsoup.connect(this.path).get();
        } catch (IOException ex) {
            Logger.getLogger(Reaper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
