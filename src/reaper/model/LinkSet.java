package reaper.model;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import java.util.List;
import java.util.Map;

/**
 *
 * @author zaraka
 */
public class LinkSet {
    
    OrientGraphFactory factory;
    
    LinkSet(OrientGraphFactory factory){
        this.factory = factory;
    }
    
    public void setFactory(OrientGraphFactory factory){
        this.factory = factory;
    }
    
    public void put(String key, String value, String cluster){
         ODatabaseDocumentTx oDB =  factory.getDatabase();
        try {
            oDB.command(
                    new OCommandSQL("INSERT INTO "
                            + DatabaseClasses.LINKSET.getName() + " cluster "
                            + DatabaseClasses.LINKSET.getName() + cluster
                            + " SET key = ?, value = ?"
                    )
            ).execute(key, value);
        } finally {
            oDB.close();
        }
    }

    public void fillMap(Map<String, String> resources, String cluster){
        ODatabaseDocumentTx oDB =  factory.getDatabase();
        try {
            List<ODocument> result = oDB.command(new OCommandSQL("SELECT * FROM cluster:"
                    + DatabaseClasses.LINKSET.getName()
                    + cluster
            )).execute();
            for(ODocument doc : result){
                resources.put(doc.field("key"), doc.field("value"));
            }
        } finally {
            oDB.close();
        }
    }
}
