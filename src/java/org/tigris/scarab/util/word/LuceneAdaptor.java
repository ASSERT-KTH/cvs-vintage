package org.tigris.scarab.util.word;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 *
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 *
 * 5. Products derived from this software may not use the "Tigris" or
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 * prior written permission of Collab.Net.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */

// JDK classes
import java.io.File;

// Turbine classes
import org.apache.turbine.Turbine;
import org.apache.torque.om.NumberKey;

// import org.apache.fulcrum.servlet.TurbineServlet;
import org.apache.commons.util.StringStack;

// Scarab classes
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Attachment;

import com.lucene.document.Document;
import com.lucene.document.Field;
import com.lucene.index.IndexWriter;
import com.lucene.analysis.standard.StandardAnalyzer;
import com.lucene.queryParser.QueryParser;
import com.lucene.search.Query;
import com.lucene.search.IndexSearcher;
import com.lucene.search.Hits;

/**
 * Support for searching/indexing text
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: LuceneAdaptor.java,v 1.7 2001/08/09 07:59:54 jon Exp $
 */
public class LuceneAdaptor 
    implements SearchIndex
{
    /** the location of the index */
    private final String path;

    /** the attributes that will be searched */
    private NumberKey[] attributeIds;

    /** the words and boolean operators */
    private String query;

    /**
     * Ctor.  Sets up an index directory if one does not yet exist in the
     * path specified by searchindex.path property in Scarab.properties.
     */
    public LuceneAdaptor()
        throws java.io.IOException
    {
        path = 
            Turbine.getRealPath(Turbine.getConfiguration()
                                .getString(INDEX_PATH));

        File indexDir = new File(path);
        boolean createIndex = false;
        if ( indexDir.exists() ) 
        {
            if ( indexDir.listFiles().length == 0 ) 
            {
                createIndex = true;
            }       
        }
        else 
        {
            indexDir.mkdirs();
            createIndex = true;
        }
        
        if ( createIndex) 
        {
            IndexWriter indexer = 
                new IndexWriter(path, new StandardAnalyzer(), true);
            indexer.close();   
        }        
    }

    /**
     * The text attributes that will be searched.
     *
     * @param ids, a NumberKey array of attribute ids.
     */
    public void setAttributeIds(NumberKey[] ids)
    {
        this.attributeIds = ids;
    }        

    /**
     *  Specify search criteria. This is incremental.
     */
    public void addQuery(String text) 
        throws Exception
    {
            query = text;
            /*
        if ( query == null ) 
        {
            query = text;
        }
        else 
        {
            query += " " + text;
        }
            */
    }

    /**
     *  returns a list of related issue IDs sorted by relevance descending.
     *  Should return an empty/length=0 array if search returns no results.
     */
    public NumberKey[] getRelatedIssues() 
        throws Exception
    {
        // if there are no words to search for return no results 
        if ( query == null || query.length() == 0)
        {
            return EMPTY_LIST; 
        }
        
        StringBuffer fullQuery = null; 
        if ( attributeIds != null ) 
        {
            fullQuery = new StringBuffer(10*attributeIds.length + 
                ATTRIBUTE_ID.length() + query.length() + 3 );
            
            fullQuery.append("+(");
            for ( int i=attributeIds.length-1; i>=0; i-- ) 
            {
                fullQuery.append(ATTRIBUTE_ID)
                .append(':')
                .append(attributeIds[i].toString())
                    .append(' ');
            }
            fullQuery.append(") ");
            
        }
        else 
        {
            fullQuery = new StringBuffer( query.length() + 3 );            
        }
        fullQuery
            .append("+(")
            .append(query)
            .append(')');

        System.out.println("Querybefore=" + fullQuery);
        Query q = QueryParser.parse(fullQuery.toString(), TEXT, 
                                    new StandardAnalyzer());
        System.out.println("Queryafter=" + q.toString("text"));

        /*
        System.out.println("Query: " + q.toString(TEXT));
        IndexReader ir = IndexReader.open(path);
        IndexSearcher is = new IndexSearcher(ir);
        is.search(q, new HitCollector() {   
               public void collect(int doc, float score) {
                   try{
              System.out.println("Document#" + doc + ", score=" + score);
        IndexReader irtmp = IndexReader.open(path);
            Enumeration e = irtmp.document(doc).fields();
            while ( e.hasMoreElements() ) 
            {
                System.out.print("DocField|" + e.nextElement() + "| ");
            }
                System.out.println("");
                   }catch(Exception e){}
            }
          });
        */      
        
        IndexSearcher is = new IndexSearcher(path); 
        Hits hits = is.search(q);
        // remove duplicates
        StringStack deduper = new StringStack();
        for ( int i=0; i<hits.length(); i++) 
        {
            deduper.add( hits.doc(i).get(ISSUE_ID) );
        }

        NumberKey[] issueIds = new NumberKey[deduper.size()];
        for ( int i=0; i<issueIds.length; i++) 
        {
            issueIds[i] = new NumberKey(deduper.get(i));
        }
        
        is.close();
        
        return issueIds;
    }

    /**
     * Store index information for an AttributeValue
     */
    public void index(AttributeValue attributeValue)
        throws Exception
    {
        Document doc = new Document();
        Field issueId = Field.UnIndexed(ISSUE_ID, 
            attributeValue.getIssueId().toString());
        Field attributeId = Field.Keyword(ATTRIBUTE_ID, 
            attributeValue.getAttributeId().toString());
        Field text = Field.UnStored(TEXT, attributeValue.getValue());
        doc.add(issueId);
        doc.add(attributeId);
        doc.add(text);

        IndexWriter indexer = 
            new IndexWriter(path, new StandardAnalyzer(), false);
        indexer.addDocument(doc);
        indexer.close();
    }


    /**
     * Store index information for an AttributeValue
     */
    public void index(Attachment attachment)
        throws Exception
    {
        Document doc = new Document();
        Field issueId = Field.UnIndexed(ISSUE_ID, 
            attachment.getIssueId().toString());
        Field attachmentId = Field.Keyword(ATTACHMENT_ID, 
            attachment.getAttachmentId().toString());
        Field text = Field.UnStored(TEXT, attachment.getDataAsString());
        doc.add(issueId);
        doc.add(attachmentId);
        doc.add(text);

        IndexWriter indexer = 
            new IndexWriter(path, new StandardAnalyzer(), false);
        indexer.addDocument(doc);
        indexer.close();
    }
}
