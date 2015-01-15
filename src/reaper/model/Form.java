package reaper.model;

/**
 *
 * @author zaraka
 */
public class Form {
    private Link action;
    private RestMethod method;
    
    Form(){
        this.action = new Link();
        this.method = RestMethod.GET;
    }
    
    Form(Link action, RestMethod method){
        this.action = action;
        this.method = method;
    }
    
    public Link getAction(){
        return this.action;
    }
    
    public void setAction(Link action){
        this.action = action;
    }
    
    public RestMethod getMethod(){
        return this.method;
    }
    
    public void setMethod(RestMethod method){
        this.method = method;
    }
}
