package reaper.model;

/**
 *
 * @author nikita.vanku
 */
public class DBConf {
    private String hostname;
    private String username;
    private String password;
    
    DBConf(String host, String user, String pass){
        this.hostname = host;
        this.username = user;
        this.password = pass;
    }
    
    public void setHostname(String host){
        this.hostname = host;
    }
    
    public String getHostname(){
        return this.hostname;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public String getUsername(){
        return this.username;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    public String getPassword(){
        return this.password;
    }
    
}
