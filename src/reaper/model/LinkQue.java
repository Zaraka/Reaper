package reaper.model;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import java.util.List;

/**
 *
 * @author nikita.vanku
 */
public class LinkQue {
    //FINISH DATABASES
    private final OSQLSynchQuery<ODocument> leaveQuery;
    private final OrientGraphFactory graphFactory;
    
    LinkQue(OrientGraphFactory factory) {
        this.graphFactory = factory;

        //this.dbConf = null;
        //db = (ODatabaseDocumentTx) ODatabaseRecordThreadLocal.INSTANCE.get();
        //db = new ODatabaseDocumentTx(dbConf.getHostname());
        leaveQuery = new OSQLSynchQuery<>("SELECT * FROM LinkQue order by LinkQue.position DESC LIMIT 1");
    }
    
    public void linkEnter(String path, String from, int depth) {
        //ODatabaseDocumentTx oDB = db.open(dbConf.getUsername(), dbConf.getPassword());
        ODatabaseDocumentTx oDB = graphFactory.getDatabase();
        try {
            oDB.command(
                    new OCommandSQL("INSERT INTO LinkQue SET path = ?, from = ?, depth = ?, position = 0")).execute(path, from, depth);
            oDB.command(
                    new OCommandSQL("UPDATE LinkQue INCREMENT position = 1")).execute();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            oDB.close();
        }
    }
    
    public ODocument linkLeave() {
        ODatabaseDocumentTx oDB = graphFactory.getDatabase();
        ODocument resultDocument;
        try {
            List<ODocument> result = oDB.command(leaveQuery).execute();
            if(result.isEmpty()){
                return null;
            }
            resultDocument = result.get(0);
            Integer position = resultDocument.field("position");
            oDB.command(
                    new OCommandSQL("DELETE From LinkQue where position = ?"))
                    .execute(position);            
        } finally {
            oDB.close();
        }
        
        return resultDocument;
    }
    
    public long queLength() {
        long length = 0;
        ODatabaseDocumentTx oDB = graphFactory.getDatabase();
        try {
            length = oDB.countClass("LinkQue");
        } finally {
            oDB.close();
        }
        return length;
    }
}
