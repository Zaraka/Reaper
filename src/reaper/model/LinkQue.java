package reaper.model;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import java.util.List;

/**
 *
 * @author nikita.vanku
 */
public class LinkQue {
    private final DBConf dbConf;
    private final ODatabaseDocumentTx db;
    private final OSQLSynchQuery<ODocument> leaveQuery;
    private final OSQLSynchQuery<ODocument> enterQuery;
    private final OSQLSynchQuery<ODocument> incrementQuery;
    
    LinkQue(DBConf conf){
        this.dbConf = conf;
        
        db  = new ODatabaseDocumentTx(dbConf.getHostname());
        leaveQuery = new OSQLSynchQuery<>("select * from LinkQue order by LinkQue.position DESC", 1);
        enterQuery = new OSQLSynchQuery<>("INSERT INTO LinkQue SET path = ?, from = ?, depth = ?, position = 0");
        incrementQuery = new OSQLSynchQuery<>("UPDATE LinkQue INCREMENT position = 1");
    }
    
    public void linkEnter(String path, String from, int depth){
        ODatabaseDocumentTx oDB = db.open(dbConf.getUsername(), dbConf.getPassword());
        try{
            oDB.command(enterQuery).execute(path, from, depth);
        } finally {
            oDB.close();
        }
    }
    
    public ODocument linkLeave(){
        ODatabaseDocumentTx oDB = db.open(dbConf.getUsername(), dbConf.getPassword());
        ODocument resultDocument;
        try{
            List<ODocument> result = oDB.command(leaveQuery).execute();
            System.out.println("LinkLeave returned " + String.valueOf(result.size()));
            oDB.command(incrementQuery).execute();
            
            resultDocument = result.get(0);
        } finally {
            oDB.close();
        }
        
        return resultDocument;
    }
    
    public long queLength(){
        long length = 0;
        ODatabaseDocumentTx oDB = db.open(dbConf.getUsername(), dbConf.getPassword());
        try{
            length = oDB.countClass("LinkQue");
        } finally {
            oDB.close();
        }
        return length;
    }
}
