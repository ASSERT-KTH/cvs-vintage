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

// Scarab classes
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Attachment;

/**
 * Support for searching/indexing text
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: SearchIndex.java,v 1.16 2005/01/06 21:03:55 dabbous Exp $
 */
public interface SearchIndex
{
    String PARSE_ERROR = 
        "Search engine could not parse the query: ";
    String INDEX_PATH = "path";
    String VALUE_ID = "valid";
    String ISSUE_ID = "issid";
    String ATTRIBUTE_ID = "attid";
    String ATTACHMENT_ID = "atchid";
    String ATTACHMENT_TYPE_ID = "atchtypeid";
    String TEXT = "text";
    Long[] EMPTY_LIST = new Long[0];

    /**
     *  Specify search criteria. This is incremental.
     */
    void addQuery(Integer[] attributeIds, String text) 
        throws Exception;

    /**
     *  Specify search criteria for attachments
     */
    void addAttachmentQuery(Integer[] ids, String text) 
        throws Exception;

    /**
     *  returns a list of related issue IDs sorted by relevance descending.
     *  Should return an empty/length=0 array if search returns no results.
     */
    Long[] getRelatedIssues() 
        throws Exception;

    /**
     * Store index information for an AttributeValue
     */
    void index(AttributeValue attributeValue)
        throws Exception;

    /**
     * Store index information for an Attachment
     */
    void index(Attachment attachment)
        throws Exception;

    /**
     * update the index for all entities that currently exist
     */
    void updateIndex()
        throws Exception;
    
    /**
     * Prepares the instance for reuse.
     *
     */
    void clear();
}
