package org.tigris.scarab.da;

/* ================================================================
 * Copyright (c) 2000 CollabNet.  All rights reserved.
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
 * software developed by CollabNet (http://www.collab.net/)."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" name
 * nor may "Tigris" appear in their names without prior written
 * permission of CollabNet.
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
 * individuals on behalf of CollabNet.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import com.workingdogs.village.Record;

import org.tigris.scarab.om.RModuleUserAttributePeer;
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * Access to data relating to attributes.
 *
 * @see org.tigris.scarab.da.AttributeAccess
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 */
public class ScarabAttributeAccess
    implements AttributeAccess
{
    /** Method name used as part of a cache key. */
    private static final String RETRIEVE_QUERY_COLUMN_IDS =
        "retrieveQueryColumnIDs";

    public ScarabAttributeAccess()
    {
    }

    public List retrieveQueryColumnIDs(String userID, String listID,
                                       String moduleID, String artifactTypeID)
    {
        List result = null;
        Object obj = ScarabCache.get(AttributeAccess.class,
                                     RETRIEVE_QUERY_COLUMN_IDS,
                                     userID, moduleID, artifactTypeID);
        if (obj == null)
        {
            Criteria crit = new Criteria();
            crit.addSelectColumn(RModuleUserAttributePeer.ATTRIBUTE_ID);
            crit.add(RModuleUserAttributePeer.USER_ID, userID);
            if (moduleID != null) 
            {
                crit.add(RModuleUserAttributePeer.MODULE_ID, moduleID);
            }
            if (artifactTypeID != null) 
            {
                crit.add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                         artifactTypeID);
            }
            // null should be added to criteria for listID
            crit.add(RModuleUserAttributePeer.LIST_ID, listID)
                .addAscendingOrderByColumn
                     (RModuleUserAttributePeer.PREFERRED_ORDER);

            try
            {
                List records = 
                    RModuleUserAttributePeer.doSelectVillageRecords(crit);
                result = new ArrayList(records.size());
                for (Iterator i = records.iterator(); i.hasNext();) 
                {
                    result.add( ((Record)i.next()).getValue(1).asString());
                }
            }
            catch (Exception e)
            {
                throw new DAException("Failed to retrieve a list of " +
                                      "attribute identifiers", e);
            }
            ScarabCache.put(result, AttributeAccess.class,
                            RETRIEVE_QUERY_COLUMN_IDS,
                            userID, moduleID, artifactTypeID);
        }
        else 
        {
            result = (List) obj;
        }
        return result;
    }

    public void deleteQueryColumnIDs(String userID, String listID,
                                     String moduleID, String artifactTypeID)
    {
        Criteria crit = new Criteria();
        crit.add(RModuleUserAttributePeer.USER_ID, userID);
        if (moduleID != null) 
        {
            crit.add(RModuleUserAttributePeer.MODULE_ID, moduleID);
        }
        if (artifactTypeID != null) 
        {
            crit.add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                     artifactTypeID);
        }
        crit.add(RModuleUserAttributePeer.LIST_ID, listID);
        try
        {
            RModuleUserAttributePeer.doDelete(crit);
        }
        catch (Exception e)
        {
            throw new DAException("Failed to delete the list of " +
                                  "attribute identifiers", e);
        }
    }
}
