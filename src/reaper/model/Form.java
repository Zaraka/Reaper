package reaper.model;

/**
 *
 * @author zaraka
 */
public class Form {
    private Link action;
    private Method method;
    
    Form(){
        this.action = new Link();
        this.method = Method.GET;
    }
    
    Form(Link action, Method method){
        this.action = action;
        this.method = method;
    }
    
    public Link getAction(){
        return this.action;
    }
    
    public Method getMethod(){
        return this.method;
    }
}
