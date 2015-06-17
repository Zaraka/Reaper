/*
 * The MIT License
 *
 * Copyright 2015 Reaper.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package reaper.model;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import java.util.List;

/**
 * Queue of Links to process stored in DB
 *
 * @author nikita.vanku
 */
public class LinkQue {

    private OrientGraphFactory graphFactory;

    LinkQue(OrientGraphFactory factory) {
        this.graphFactory = factory;
    }

    public void setGraphFactory(OrientGraphFactory factory) {
        graphFactory = factory;
    }

    public void linkEnter(String cluster, Link link) {
        ODatabaseDocumentTx oDB = graphFactory.getDatabase();
        try {
            oDB.command(
                    new OCommandSQL("INSERT INTO "
                            + DatabaseClasses.LINKQUE.getName() + " cluster "
                            + DatabaseClasses.LINKQUE.getName() + cluster
                            + " SET path = ?, from = ?, depth = ?, count = ?, type = ?, position = 0"
                    )
            ).execute(link.getLink(), link.getFromURL(), link.getFromResource().getDepth(),
                    link.getCount(), link.getType().toString());
            oDB.command(
                    new OCommandSQL("UPDATE cluster:"
                            + DatabaseClasses.LINKQUE.getName()
                            + cluster
                            + " INCREMENT position = 1")).execute();
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
            List<ODocument> result = oDB.command(new OCommandSQL("SELECT * FROM cluster:"
                    + DatabaseClasses.LINKQUE.getName()
                    + cluster
                    + " order by LinkQue.position DESC LIMIT 1"
            )).execute();
            if (result.isEmpty()) {
                return null;
            }
            resultDocument = result.get(0);
            Integer position = resultDocument.field("position");
            oDB.command(
                    new OCommandSQL("DELETE From cluster:"
                            + DatabaseClasses.LINKQUE.getName()
                            + cluster
                            + " where position = ?"))
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
