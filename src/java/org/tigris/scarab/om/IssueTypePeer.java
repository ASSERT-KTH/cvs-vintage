package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
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
 * software developed by CollabNet <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 *
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 *
 * 5. Products derived from this software may not use the "Tigris" or
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 * prior written permission of CollabNet.
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

import java.util.List;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.ObjectKey;

// Local classes
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * This is the peer class for an IssueType
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: IssueTypePeer.java,v 1.28 2003/04/07 19:50:00 elicia Exp $
 */
public class IssueTypePeer 
    extends org.tigris.scarab.om.BaseIssueTypePeer
{
    private static final Integer ROOT_KEY = new Integer(0);

    private static final String ISSUE_TYPE_PEER = 
        "IssueTypePeer";
    private static final String GET_ALL_ISSUE_TYPES = 
        "getAllIssueTypes";

    private static final String RETRIEVE_BY_PK = 
        "retrieveByPK";

    public static Integer getRootKey()
    {
        return ROOT_KEY;
    }
    
    /** 
     * Retrieve a single object by pk
     * FIXME: is this method implementation (with the caching) still done this way? -jss
     * @param pk
     */
    public static IssueType retrieveByPK(ObjectKey pk)
        throws TorqueException
    {
        IssueType result = null;
        Object obj = ScarabCache.get(ISSUE_TYPE_PEER, RETRIEVE_BY_PK, pk); 
        if (obj == null) 
        {        
            result = BaseIssueTypePeer.retrieveByPK(pk);
            ScarabCache.put(result, ISSUE_TYPE_PEER, RETRIEVE_BY_PK, pk);
        }
        else 
        {
            result = (IssueType)obj;
        }
        return result;
    }

    /**
     *  Gets a List of all of the Issue types in the database,
     *  That are not template types.
     */
    public static List getAllIssueTypes(boolean deleted,
                       String sortColumn, String sortPolarity)
        throws Exception
    {
        List result = null;
        Boolean b = deleted ? Boolean.TRUE : Boolean.FALSE;
        Object obj = ScarabCache.get(ISSUE_TYPE_PEER, GET_ALL_ISSUE_TYPES, b); 
        if (obj == null) 
        {        
            Criteria c = new Criteria();
            c.add(IssueTypePeer.PARENT_ID, 0);
            c.add(IssueTypePeer.ISSUE_TYPE_ID, 0, Criteria.NOT_EQUAL);
            if (deleted)
            {
                c.add(IssueTypePeer.DELETED, 1);
            }
            else
            {
                c.add(IssueTypePeer.DELETED, 0);
            }
            if (sortColumn != null && sortColumn.equals("desc"))
            {
                addSortOrder(c, IssueTypePeer.DESCRIPTION, 
                             sortPolarity);
            }
            else
            {
                // sort on name
                addSortOrder(c, IssueTypePeer.NAME, 
                             sortPolarity);
            }
            result = doSelect(c);
            ScarabCache.put(result, ISSUE_TYPE_PEER, GET_ALL_ISSUE_TYPES, b);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    public static List getAllIssueTypes(boolean includeDeleted)
        throws Exception
    {
        return getAllIssueTypes(includeDeleted, "name", "asc");
    } 

    public static List getDefaultIssueTypes()
        throws Exception
    {
        Criteria c = new Criteria();
        c.add(IssueTypePeer.PARENT_ID, 0);
        c.add(IssueTypePeer.DELETED, 0);
        c.add(IssueTypePeer.ISDEFAULT, 1);
        c.add(IssueTypePeer.ISSUE_TYPE_ID, 0, Criteria.NOT_EQUAL);
        return IssueTypePeer.doSelect(c);
    }

    private static Criteria addSortOrder(Criteria crit, 
                    String sortColumn, String sortPolarity)
    {
        if (sortPolarity != null && sortPolarity.equals("desc"))
        {
            crit.addDescendingOrderByColumn(sortColumn);
        }
        else
        {
            crit.addAscendingOrderByColumn(sortColumn);
        }
        return crit;
    }

    /**
     * Checks to see if the name already exists an issue type.  if one
     * does unique will be false unless the given id matches the issue type
     * that already has the given name.
     *
     * @param name a <code>String</code> value
     * @param id an <code>ObjectKey</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public static boolean isUnique(String name, Integer id)
        throws Exception
    {
        boolean unique = true;
        Criteria crit = new Criteria().add(IssueTypePeer.NAME, name.trim());
        crit.setIgnoreCase(true);
        List types = IssueTypePeer.doSelect(crit);
        if (types.size() > 0) 
        {
            for (int i =0; i<types.size();i++)
            {
                IssueType it = (IssueType)types.get(i);
                if ((id == null ||  (id != null && !it.getPrimaryKey().equals(id)))
                    && it.getName().trim().toLowerCase().equals(name.trim().toLowerCase()))
                {
                    unique = false;
                }
            }
        }
        return unique;
    }
}
