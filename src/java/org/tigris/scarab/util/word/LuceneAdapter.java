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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

// Turbine classes
import org.apache.turbine.Turbine;
import org.apache.torque.om.ComboKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.util.Criteria;
import com.workingdogs.village.Record;

// Scarab classes
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttributeValuePeer;
import org.tigris.scarab.om.AttachmentPeer;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.Log;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Hits;

/**
 * Support for searching/indexing text
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: LuceneAdapter.java,v 1.25 2003/03/25 16:57:55 jmcnally Exp $
 */
public class LuceneAdapter 
    implements SearchIndex
{
    // used to occasionally optimize the index
    private static int counter = 0;

    /** the location of the index */
    private String path;

    /** the attributes that will be searched */
    private List attributeIds;

    /** the words and boolean operators */
    private List queryText;

    /** the attachments that will be searched */
    private List attachmentIds;

    /** the words and boolean operators */
    private List attachmentQueryText;

    /**
     * Ctor.  Sets up an index directory if one does not yet exist in the
     * path specified by searchindex.path property in Scarab.properties.
     */
    public LuceneAdapter()
        throws IOException
    {
        path = Turbine.getConfiguration().getString(INDEX_PATH);
        File indexDir = new File(path);
        if (!indexDir.isAbsolute()) 
        {
            path = Turbine.getRealPath(path);
            indexDir = new File(path);
        }
        
        boolean createIndex = false;
        if (indexDir.exists()) 
        {
            if (indexDir.listFiles().length == 0) 
            {
                createIndex = true;
            }       
        }
        else 
        {
            indexDir.mkdirs();
            createIndex = true;
        }
        
        if (createIndex)
        {
            Log.get().info("Creating index at '" + path + '\'');
            synchronized (getClass())
            {
                IndexWriter indexer = null;
                try
                {
                    indexer = 
                        new IndexWriter(path, new PorterStemAnalyzer(), true);
                }
                finally
                {
                    if (indexer != null) 
                    {
                        indexer.close();                           
                    }
                }
            }
        }        

        attributeIds = new ArrayList(5);
        queryText = new ArrayList(5);
        attachmentIds = new ArrayList(2);
        attachmentQueryText = new ArrayList(2);
    }

    public void addQuery(Integer[] ids, String text)
    {
        attributeIds.add(ids);
        queryText.add(text);
    }

    public void addAttachmentQuery(Integer[] ids, String text)
    {
        attachmentIds.add(ids);
        attachmentQueryText.add(text);
    }

    /**
     *  returns a list of related issue IDs sorted by relevance descending.
     *  Should return an empty/length=0 array if search returns no results.
     */
    public Long[] getRelatedIssues() 
        throws Exception
    {
        Long[] result;
        List issueIds = null; 
        // if there are no words to search for return no results 
        if (queryText.size() != 0 || attachmentQueryText.size() != 0)
        {
            // attributes
            for (int j=attributeIds.size()-1; j>=0; j--) 
            {
                Integer[] ids = (Integer[])attributeIds.get(j);
                String query = (String) queryText.get(j);
                issueIds = performPartialQuery(ATTRIBUTE_ID, 
                                               ids, query, issueIds);
            }

            // attachments
            for (int j=attachmentIds.size()-1; j>=0; j--) 
            {
                Integer[] ids = (Integer[])attachmentIds.get(j);
                String query = (String) attachmentQueryText.get(j);
                issueIds = performPartialQuery(ATTACHMENT_TYPE_ID, 
                                               ids, query, issueIds);
            }

            // put results into final form
            result = new Long[issueIds.size()];
            for (int i=0; i<issueIds.size(); i++) 
            {
                result[i] = (Long)issueIds.get(i);
            }
        }
        else
        {
            result = EMPTY_LIST; 
        }
        
        return result;
    }

    private List performPartialQuery(String key, Integer[] ids, 
                                     String query, List issueIds)
        throws ScarabException, IOException
    {
        StringBuffer fullQuery = new StringBuffer(query.length()+100);
        
        if (query.length() > 0)
        {
            query.trim();
        }
        
                if (ids != null && ids.length != 0) 
                {
                    fullQuery.append("+((");
                    for (int i=ids.length-1; i>=0; i--) 
                    {
                        fullQuery.append(key)
                            .append(':')
                            .append(ids[i].toString());
                        if (i != 0) 
                        {
                            fullQuery.append(" OR ");
                        }
                    }
                    fullQuery.append(") AND (")
                        .append(query)
                        .append("))");            
                }
                else
                {
                    fullQuery
                        .append("+(")
                        .append(query)
                        .append(')');
                }
                
                Query q = null;
                try
                {
                    Log.get().debug("Querybefore=" + fullQuery);
                    q = QueryParser.parse(fullQuery.toString(), TEXT, 
                                          new PorterStemAnalyzer());
                    Log.get().debug("Queryafter=" + q.toString("text"));
                }
                catch (Throwable t)
                {
                    throw new ScarabException(PARSE_ERROR + fullQuery + 
                        ". Reason given was: " +  t.getMessage());
                }
                
                IndexSearcher is = new IndexSearcher(path); 
                Hits hits = is.search(q);
                // remove duplicates
                Map deduper = new HashMap((int)(1.25*hits.length()+1));
                for (int i=0; i<hits.length(); i++) 
                {
                    deduper.put(hits.doc(i).get(ISSUE_ID), null);
                    Log.get().debug("Possible issueId from search: " + 
                                  hits.doc(i).get(ISSUE_ID));
                }
                is.close();
                
                if (issueIds == null) 
                {
                    issueIds = new ArrayList(deduper.size());
                    Iterator iter = deduper.keySet().iterator();
                    while (iter.hasNext()) 
                    {
                        issueIds.add(new Long((String)iter.next()));
                        Log.get().debug("Adding issueId from search: " + 
                                  issueIds.get(issueIds.size()-1));
                    }
                }
                else 
                {
                    // perform an AND operation
                    removeUniqueElements(issueIds, deduper);
                }
        return issueIds;
    }

    /**
     * Elements from the list that are not in map are removed from the list
     */
    private void removeUniqueElements(List list, Map map)
    {
        for (int i=list.size()-1; i>=0; i--) 
        {
            Object obj = list.get(i);
            if (!map.containsKey(obj.toString())) 
            {
                Log.get().debug("removing issueId from search: " + obj);
                list.remove(i);
            }
        }
    }


    /**
     * Store index information for an AttributeValue
     */
    public void index(AttributeValue attributeValue)
        throws Exception
    {
        String valId = attributeValue.getValueId().toString();

        // make sure any old data stored for this attribute value is deleted.
        Term term = new Term(VALUE_ID, valId);
        int deletedDocs = 0;
        try
        {
            synchronized (getClass())
            {
                IndexReader reader = null;
                try
                {
                    reader = IndexReader.open(path);
                    deletedDocs = reader.delete(term);
                }
                finally
                {
                    if (reader != null) 
                    {
                        reader.close();
                    }
                }
            }
        }
        catch (NullPointerException npe)
        {
            /* Lucene is throwing npe in reader.delete, so have to explicitely
               search.  Not sure if the npe will be thrown in the 
               case where the attribute has previously been indexed, so
               test whether the npe is harmful.
            */
            IndexSearcher is = new IndexSearcher(path); 
            Query q = QueryParser.parse("+" + VALUE_ID + ":" + valId, TEXT, 
                                        new PorterStemAnalyzer());
            Hits hits = is.search(q);
            if (hits.length() > 0) 
            {
                String mesg = "An error in Lucene prevented removing " + 
                    "stale data for AttributeValue with ID=" + valId;
                Log.get().debug(mesg);
                throw new ScarabException(mesg, npe);
            }
        }
        if (deletedDocs > 1) 
        {
            throw new ScarabException("Multiple AttributeValues in Lucene" +
                                      "index with same ValueId: " + valId);
        }
        /*
        System.out.println("deleting valId: " + valId);
        IndexSearcher is = new IndexSearcher(path); 
        Hits hits = is.search("+" + VALUE_ID + ":" + valId);
        System.out.println("deleting previous: " + hits.length());
        if (hits.length() > 1) 
        {
            throw new ScarabException("Multiple AttributeValues in Lucene" +
                                      "index with same ValueId: " + valId);
        }
        Document doc = hits.doc(0);
        */

        if (attributeValue.getValue() == null) 
        {
            Log.get().warn("Attribute value pk=" + valId + 
                           " has a null value.");
        }
        else 
        {
            Document doc = new Document();
            Field valueId = Field.Keyword(VALUE_ID, valId);
            Field issueId = Field.UnIndexed(ISSUE_ID, 
                attributeValue.getIssueId().toString());
            Field attributeId = Field.Keyword(ATTRIBUTE_ID, 
                attributeValue.getAttributeId().toString());
            Field text = Field.UnStored(TEXT, attributeValue.getValue());
            doc.add(valueId);
            doc.add(issueId);
            doc.add(attributeId);
            doc.add(text);
            addDoc(doc);
        }    
    }

    private void addDoc(Document doc)
        throws IOException
    {
        synchronized (getClass())
        {
            IndexWriter indexer = null;
            try
            {
                indexer = new IndexWriter(path, 
                                          new PorterStemAnalyzer(), false);
                indexer.addDocument(doc);
                
                if (++counter % 100 == 0) 
                {
                    indexer.optimize();
                }
            }
            finally
            {
                if (indexer != null) 
                {
                    indexer.close();                    
                }
            }
        }
    }        

    /**
     * Store index information for an Attachment
     */
    public void index(Attachment attachment)
        throws Exception
    {
        String attId = attachment.getAttachmentId().toString();

        // make sure any old data stored for this attribute value is deleted.
        Term term = new Term(ATTACHMENT_ID, attId);
        int deletedDocs = 0;
        try
        {
            synchronized (getClass())
            {
                IndexReader reader = null;
                try
                {
                    reader = IndexReader.open(path);
                    deletedDocs = reader.delete(term);
                }
                finally
                {
                    if (reader != null) 
                    {
                        reader.close();
                    }
                }
            }
        }
        catch (NullPointerException npe)
        {
            /* Lucene is throwing npe in reader.delete, so have to explicitely
               search.  Not sure if the npe will be thrown in the 
               case where the attribute has previously been indexed, so
               test whether the npe is harmful.
            */
            IndexSearcher is = new IndexSearcher(path); 
            Query q = QueryParser.parse("+" + ATTACHMENT_ID + ":" + attId, 
                                        TEXT, new PorterStemAnalyzer());
            Hits hits = is.search(q);
            if (hits.length() > 0) 
            {
                String mesg = "An error in Lucene prevented removing " + 
                    "stale data for Attachment with ID=" + attId;
                Log.get().debug(mesg);
                throw new ScarabException(mesg, npe);
            }
        }
        if (deletedDocs > 1) 
        {
            throw new ScarabException("Multiple Attachments in Lucene" +
                                      "index with same Id: " + attId);
        }


        if (attachment.getData() == null) 
        {
            Log.get().warn("Attachment pk=" + attId + " has a null data.");
        }
        else 
        {
            Document doc = new Document();
            Field attachmentId = Field.Keyword(ATTACHMENT_ID, attId);
            Field issueId = Field.UnIndexed(ISSUE_ID, 
                attachment.getIssueId().toString());
            Field typeId = Field.Keyword(ATTACHMENT_TYPE_ID, 
                attachment.getTypeId().toString());
            Field text = Field.UnStored(TEXT, attachment.getData());
            doc.add(attachmentId);
            doc.add(issueId);
            doc.add(typeId);
            doc.add(text);
            addDoc(doc);
        }            
    }

    /**
     * update the index for all entities that currently exist
     */
    public void updateIndex()
        throws Exception
    {
        // find estimate of max id
        Criteria crit = new Criteria();
        crit.addSelectColumn("max(" + AttributeValuePeer.VALUE_ID + ")");
        List records = AttributeValuePeer.doSelectVillageRecords(crit);
        long max = ((Record)records.get(0)).getValue(1).asLong();
        
        long i = 0L;
        List avs = null;
        do
        {
            crit = new Criteria();
            Criteria.Criterion low = crit.getNewCriterion(
                 AttributeValuePeer.VALUE_ID, 
                 new Long(i), Criteria.GREATER_THAN);
            i += 100L;
            Criteria.Criterion high = crit.getNewCriterion(
                AttributeValuePeer.VALUE_ID, 
                new Long(i), Criteria.LESS_EQUAL);
            crit.add(low.and(high));
            crit.add(AttributeValuePeer.DELETED, false);
            // don't index issues that have been deleted
            crit.addJoin(AttributeValuePeer.ISSUE_ID, IssuePeer.ISSUE_ID);
            crit.add(IssuePeer.DELETED, false);
            avs = AttributeValuePeer.doSelect(crit);
            if (!avs.isEmpty()) 
            {
                Iterator avi = avs.iterator();
                while (avi.hasNext()) 
                {
                    AttributeValue av = (AttributeValue)avi.next();
                    index(av);
                }
                if (Log.get().isDebugEnabled()) 
                {
                    Log.get().debug("Updated index for attribute values (" + 
                        (i-100L) + "-" + i + "]");                    
                    Log.debugMemory();
                }                
            }  
        }
        while (i<max || !avs.isEmpty());

        // Attachments

        crit = new Criteria();
        crit.addSelectColumn("max(" + AttachmentPeer.ATTACHMENT_ID + ")");
        records = AttachmentPeer.doSelectVillageRecords(crit);
        max = ((Record)records.get(0)).getValue(1).asLong();
        i = 0L;
        List atts = null;
        do
        {
            crit = new Criteria();
            Criteria.Criterion low = crit.getNewCriterion(
                 AttachmentPeer.ATTACHMENT_ID, 
                 new Long(i), Criteria.GREATER_THAN);
            i += 100L;
            Criteria.Criterion high = crit.getNewCriterion(
                AttachmentPeer.ATTACHMENT_ID, 
                new Long(i), Criteria.LESS_EQUAL);
            crit.add(low.and(high));
            crit.add(AttachmentPeer.DELETED, false);
            // don't index issues that have been deleted
            crit.addJoin(AttachmentPeer.ISSUE_ID, IssuePeer.ISSUE_ID);
            crit.add(IssuePeer.DELETED, false);
            atts = AttachmentPeer.doSelect(crit);
            if (!atts.isEmpty()) 
            {
                Iterator atti = atts.iterator();
                while (atti.hasNext()) 
                {
                    Attachment att = (Attachment)atti.next();
                    if (att.getData() != null && att.getData().length() > 0 &&
                        att.getIssueId() != null && att.getTypeId() != null) 
                    {
                        index(att);
                    }                    
                }
                
                if (Log.get().isDebugEnabled()) 
                {
                    Log.get().debug("Updated index for attachments (" + 
                        (i-100L) + "-" + i + "]");                    
                    Log.debugMemory();
                }                
            }  
        }
        while (i<max || !atts.isEmpty());

        // finish off with an optimized index
        synchronized (getClass())
        {
            IndexWriter indexer = null;
            try
            {
                indexer = new IndexWriter(path, 
                                          new PorterStemAnalyzer(), false);
                indexer.optimize();
            }
            finally
            {
                if (indexer != null) 
                {
                    indexer.close();                    
                }
            }
        }
    }
}
