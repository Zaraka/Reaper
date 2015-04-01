package reaper.model;

import com.tinkerpop.blueprints.Vertex;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author nikita.vanku
 */
public class Project {    
    private URL domain;
    private String date;
    private String name;
    private String cluster;
    
    public static SimpleDateFormat clusterDate = new SimpleDateFormat("yyyy_MM_dd");
    
    Project(String name, URL domain, String date, String cluster){
        this.domain = domain;
        this.name = name;
        this.date = date;
        this.cluster = cluster;
    }
    
    Project(String name, URL domain){
        this.name = name;
        this.domain = domain;
        
        Date dt = new Date();
        this.date = ReaperDatabase.dateFormat.format(dt);
        this.cluster = clusterDate.format(dt);
        this.cluster
    }
    
    Project(Vertex ver) throws MalformedURLException{
        this.cluster = ver.getProperty("cluster");
        this.name = ver.getProperty("name");
        this.date = ver.getProperty("date");
        this.domain = new URL(ver.getProperty("domain"));
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
