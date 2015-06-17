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
