/*
 * $Header: /tmp/cvs-vintage/struts/contrib/artimus/WEB-INF/src/java/org/apache/artimus/article/Store.java,v 1.3 2004/03/14 07:15:05 sraeburn Exp $
 * $Revision: 1.3 $
 * $Date: 2004/03/14 07:15:05 $
 *
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 

package org.apache.artimus.article;

import java.sql.SQLException;

import org.apache.scaffold.lang.Tokens;
import org.apache.scaffold.model.ModelBeanBase;
import org.apache.scaffold.model.ModelException;
import org.apache.scaffold.model.ModelResult;
import org.apache.scaffold.model.ModelResultBase;

import org.apache.artimus.lang.Messages;
import org.apache.artimus.article.http.Form;


/**
 * Insert or update an article.
 * @version $Revision: 1.3 $ $Date: 2004/03/14 07:15:05 $
 */
public class Store extends Bean {

    // ------------------------------------------------------------ Public Methods


    /**
     * Allocate next key for article table.
     * @return key to use for insertion
     * @exception SQLException if SQL error occurs
     */
    public void allocateKey() throws ModelException {

        Integer key = Access.allocateKey();

        setArticle(key);
    }


    /**
     * Insert <code>key</code> record.
     * @returns Number of records inserted.
     */
    public void insert() throws ModelException {

        allocateKey();

        Access.insert(
            getArticle(),
            getContributed(),
            getContributor(),
            getCreator(),
            getTitle(),
            getContent()
        );

    }


    /**
     * Update <code>key</code> record.
     * @returns Number of records updated.
     */
    public void update() throws ModelException {

        Access.update(
            getArticle(),
            getContributed(),
            getContributor(),
            getCreator(),
            getTitle(),
            getContent()
        );

    }


    /**
     * Execute model for this bean, and return outcome in ModelResult.
     * @exception Collects and returns any Exceptions
     * @returns Null on success, or a collection of Exceptions
     */
    public ModelResult execute(Object source, Object target)
            throws ModelException {

        ModelResult modelResult = new ModelResultBase();

        if (isBlank(getKey())) {
            insert();
            modelResult.addMessage(Tokens.DATA_RECORD_INSERTED);
        }
        else {
            update();
            modelResult.addMessage(Tokens.DATA_RECORD_UPDATED);
        }
        modelResult.addMessage(getKey());

        modelResult.add(target);
        return modelResult;
    }

 }


/*
    public ModelResult perform(Iterator rows) throws ModelException {
        if (this.article==null)
            insert(rows);
        else
            update(rows);
            // :FIXME: Should provide a batch insert method
        return ModelResult.factory(this);
    }

    public void update(Iterator rows) throws SQLException {
        while (rows.hasNext())
            update((ModelBean) rows.next());
    }

    public void insert(Iterator rows) throws SQLException {
        int count = 0;
        while (rows.hasNext())
            insert((ModelBean) rows.next());
        return count;
    }
*/