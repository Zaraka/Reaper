package reaper.model;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.impls.orient.OrientEdgeType;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;

/**
 *
 * @author nikita.vanku
 */
public class ReaperDatabase {
    private final OrientGraphFactory factory;
    
    ReaperDatabase(OrientGraphFactory factory){
        this.factory = factory;
    }
    
    public void setupSchema(){
        // Create Graph scheme
        OrientGraph graph = factory.getTx();
        try {
            //Vertex Resource
            OrientVertexType resource = graph.createVertexType("Resource");
            resource.createProperty("code", OType.INTEGER);
            resource.createProperty("downloadTime", OType.LONG);
            resource.createProperty("mimeType", OType.STRING);
            resource.createProperty("type", OType.STRING);
            resource.createProperty("url", OType.STRING);
            resource.createIndex("Resource.url", OClass.INDEX_TYPE.UNIQUE, "url");
   
            //Vertex Form
            OrientVertexType form = graph.createVertexType("Form");
            form.createProperty("action", OType.STRING);
            form.createProperty("method", OType.STRING);
            
            //Vertex Project
            OrientVertexType project = graph.createVertexType("Project");
            project.createProperty("cluster", OType.STRING);
            project.createProperty("date", OType.DATETIME);
            project.createProperty("domain", OType.STRING);
            project.createProperty("name", OType.STRING);
            
            //Edge LinkTo
            OrientEdgeType linkTo = graph.createEdgeType("LinkTo");
            linkTo.createProperty("count", OType.INTEGER);
            linkTo.createProperty("path", OType.STRING);
            linkTo.createProperty("type", OType.STRING);
            
            //Edge Includes
            OrientEdgeType includes = graph.createEdgeType("Includes");
            
            //Edge Root
            OrientEdgeType root = graph.createEdgeType("Root");
            
        } finally {
            graph.shutdown();
        }
        
        ODatabaseDocumentTx oDB = factory.getDatabase();
        try{
            //Link Queue
            OClass linkQue = oDB.getMetadata().getSchema().createClass("LinkQue");
            linkQue.createProperty("depth", OType.INTEGER);
            linkQue.createProperty("from", OType.STRING);
            linkQue.createProperty("path", OType.STRING);
            linkQue.createProperty("position", OType.INTEGER);
            linkQue.createIndex("LinkQue.position", OClass.INDEX_TYPE.UNIQUE, "position");
        } finally {
            oDB.close();
        }
            
    }
    
    public void tearDown(){
        OrientGraph graph = factory.getTx();
        try {
            //Edges
            graph.dropEdgeType("Includes");
            graph.dropEdgeType("LinkTo");
            graph.dropEdgeType("Root");
            
            //Vertices
            graph.dropVertexType("Resource");
            graph.dropVertexType("Form");
            graph.dropVertexType("Project");
        } finally {
            graph.shutdown();
        }
        
        ODatabaseDocumentTx oDB = factory.getDatabase();
        try {
            oDB.getMetadata().getSchema().dropClass("LinkQue");
        } finally {
            oDB.close();
        }
    }
}
