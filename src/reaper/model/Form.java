/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
}
