package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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

// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.util.Criteria;
import com.workingdogs.village.Record;
import com.workingdogs.village.DataSetException;

// Scarab classes
import org.tigris.scarab.services.cache.ScarabCache;

/** 
 * The Peer class for an issue.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: IssuePeer.java,v 1.12 2003/02/14 18:28:20 jon Exp $
 */
public class IssuePeer 
    extends org.tigris.scarab.om.BaseIssuePeer
{
    private static final String ISSUE_PEER = 
        "IssuePeer";
    private static final String RETRIEVE_BY_PK = 
        "retrieveByPK";

    private static final String COUNT = 
        "count(" + ISSUE_ID + ')';
    private static final String COUNT_DISTINCT = 
        "count(DISTINCT " + ISSUE_ID + ')';

    /**
     * Adds count(IssuePeer.ISSUE_ID) to the select clause and returns the 
     * number of rows resulting from the given Criteria.  If the criteria will
     * lead to duplicate ISSUE_ID's they will be counted.  However, if the
     * criteria is known not to lead to this, or unique ISSUE_ID's are not
     * required, this method is cheaper than {@link #countDistinct(Criteria)}
     *
     * @param crit a <code>Criteria</code> value
     * @return an <code>int</code> value
     * @exception TorqueException if an error occurs
     * @exception DataSetException if an error occurs
     */
    public static int count(Criteria crit)
        throws TorqueException, DataSetException
    {
        crit.addSelectColumn(COUNT);
        return ((Record)IssuePeer.doSelectVillageRecords(crit).get(0))
            .getValue(1).asInt();
    }

    /**
     * Adds count(DISTINCT IssuePeer.ISSUE_ID) to the select clause and returns
     * the number of rows resulting from the given Criteria.  The returned
     * value will be the number of unique ISSUE_ID's.
     *
     * @param crit a <code>Criteria</code> value
     * @return an <code>int</code> value
     * @exception TorqueException if an error occurs
     * @exception DataSetException if an error occurs
     */
    public static int countDistinct(Criteria crit)
        throws TorqueException, DataSetException
    {
        crit.addSelectColumn(COUNT_DISTINCT);
        return ((Record)IssuePeer.doSelectVillageRecords(crit).get(0))
            .getValue(1).asInt();
    }

    /** 
     * Retrieve a single object by pk
     *
     * @param pk
     */
    public static Issue retrieveByPK(ObjectKey pk)
        throws TorqueException
    {
        Issue result = null;
        Object obj = ScarabCache.get(ISSUE_PEER, RETRIEVE_BY_PK, pk); 
        if (obj == null) 
        {        
            result = BaseIssuePeer.retrieveByPK(pk);
            ScarabCache.put(result, ISSUE_PEER, RETRIEVE_BY_PK, pk);
        }
        else 
        {
            result = (Issue)obj;
        }
        return result;
    }
}
