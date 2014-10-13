/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reaper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author zaraka
 */
public class Domain {
    
    private ObservableList<Resource> resources = FXCollections.observableArrayList();
    private String hostname;
    private int maxDownloads;
    private int maxDepth;
    
    public Domain() {
        this.hostname = "";
        this.maxDepth = 5;
        this.maxDownloads = 5;
    }
    
    public Domain(String hostname, int maxDownloads, int maxDepth){
        this.hostname = hostname;
        this.maxDepth = maxDepth;
        this.maxDownloads = maxDownloads;
    }
    
    public void mine(){
        //try just one page for start
        Resource page = new Resource(this.hostname);
        resources.add(page);
    }
    
    public ObservableList<Resource> getResource() {
        return this.resources;
    }

}
