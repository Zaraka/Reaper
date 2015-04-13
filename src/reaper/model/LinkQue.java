package reaper.model;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import java.util.List;

/**
 * Queue of Links to process stored in DB
 * @author nikita.vanku
 */
public class LinkQue {
    private OrientGraphFactory graphFactory;

    LinkQue(OrientGraphFactory factory) {
        this.graphFactory = factory;
    }
    
    public void setGraphFactory(OrientGraphFactory factory){
        graphFactory = factory;
    }

    public void linkEnter(String cluster, String path, String from, int depth) {
        ODatabaseDocumentTx oDB = graphFactory.getDatabase();
        try {
            oDB.command(
                    new OCommandSQL("INSERT INTO "
                            + DatabaseClasses.LINKQUE.getName() + " cluster "
                            + DatabaseClasses.LINKQUE.getName() + cluster + 
                            " SET path = ?, from = ?, depth = ?, position = 0")).execute(path, from, depth);
            oDB.command(
                    new OCommandSQL("UPDATE cluster:"+
                            DatabaseClasses.LINKQUE.getName()+
                            cluster+
                            " INCREMENT position = 1")).execute();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            oDB.close();
        }
    }

    public ODocument linkLeave(String cluster) {
        ODatabaseDocumentTx oDB = graphFactory.getDatabase();
        ODocument resultDocument;
        try {
            List<ODocument> result = oDB.command(new OCommandSQL("SELECT * FROM cluster:"+
                    DatabaseClasses.LINKQUE.getName()+
                    cluster+
                    " order by LinkQue.position DESC LIMIT 1"
            )).execute();
            if (result.isEmpty()) {
                return null;
            }
            resultDocument = result.get(0);
            Integer position = resultDocument.field("position");
            oDB.command(
                    new OCommandSQL("DELETE From cluster:"+
                            DatabaseClasses.LINKQUE.getName()+
                            cluster
                            +" where position = ?"))
                    .execute(position);
        } finally {
            oDB.close();
        }

        return resultDocument;
    }

    public long queLength(String cluster) {
        long length = 0;
        ODatabaseDocumentTx oDB = graphFactory.getDatabase();
        try {
            //length = oDB.countClass("LinkQue");
            length = oDB.countClusterElements(
                    DatabaseClasses.LINKQUE.getName() + cluster
            );
        } finally {
            oDB.close();
        }
        return length;
    }
}
