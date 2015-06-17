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

import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * Basic object for storing various database vertices.
 * @author zaraka
 */
public interface DatabaseObject {

    /**
     * Save(update) vertex to Database
     * @param graph 
     */
    public void save(OrientGraph graph);

    /**
     * Remove vertex from Database
     * @param graph 
     */
    public void remove(OrientGraph graph);

    /**
     * Update vertex from Database
     * @param graph 
     */
    public void update(OrientGraph graph);

    /**
     * get ORID
     * @return Object (ORID) id
     */
    public Object getID();

    /**
     * set ORID
     * <b>WARNING: Setting ID manually may broke Database</b>
     * @param id Object
     */
    public void setID(Object id);
}
