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
import java.util.*;

// Turbine classes
import org.apache.turbine.services.db.om.*;

// Scarab classes
import org.tigris.scarab.om.*;
import org.tigris.scarab.util.ScarabConstants;

/**
 * Support for searching/indexing text
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: SearchIndex.java,v 1.2 2001/06/29 01:57:27 jmcnally Exp $
 */
public interface SearchIndex
{
    public static final String INDEX_PATH = "searchindex.path";
    public static final String CLASSNAME = "searchindex.class";
    public static final String ISSUE_ID = "issid";
    public static final String ATTRIBUTE_ID = "attid";
    public static final String TEXT = "text";
    public static final NumberKey[] EMPTY_LIST = new NumberKey[0];

    /**
     * The text attributes that will be searched.
     *
     * @param ids, a NumberKey array of attribute ids.
     */
    public void setAttributeIds(NumberKey[] ids);

    /**
     *  Specify search criteria. This is incremental.
     */
    public void addQuery(String text) 
        throws Exception;

    /**
     *  returns a list of related issue IDs sorted by relevance descending.
     *  Should return an empty/length=0 array if search returns no results.
     */
    public NumberKey[] getRelatedIssues() 
        throws Exception;

    /**
     * Store index information for an AttributeValue
     */
    public void index(AttributeValue attributeValue)
        throws Exception;
}


