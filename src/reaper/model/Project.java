package reaper.model;

import java.net.URL;

/**
 *
 * @author nikita.vanku
 */
public class Project {
    private URL domain;
    private String date;
    private String name;
    private String cluster;
    
    Project(String name, URL domain, String date, String cluster){
        this.domain = domain;
        this.name = name;
        this.date = date;
        this.cluster = cluster;
    }
    
    public String getName(){
        return this.name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getDate(){
        return this.date;
    }
    
    public void setDate(String date){
        this.date = date;
    }
    
    public URL getDomain(){
        return this.domain;
    }
    
    public void setDomain(URL url){
        this.domain = url;
    }
    
    public String getCluster(){
        return this.cluster;
    }
    
    public void setCluster(String cluster){
        this.cluster = cluster;
    }
}
