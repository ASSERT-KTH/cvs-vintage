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

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

import org.apache.torque.util.Criteria;
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * This class represents an AttributePeer.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: AttributePeer.java,v 1.33 2003/10/14 04:59:23 jmcnally Exp $
 */
public class AttributePeer 
    extends org.tigris.scarab.om.BaseAttributePeer
{
    public static final Integer ASSIGNED_TO__PK = new Integer(2);
    public static final Integer STATUS__PK = new Integer(3);
    public static final Integer RESOLUTION__PK = new Integer(4);
    public static final Integer TOTAL_VOTES__PK = new Integer(13);
    public static final String EMAIL_TO = "to";
    public static final String CC_TO = "cc";
    public static final String USER = "user";
    public static final String NON_USER = "non-user";

    private static final String ATTRIBUTE_PEER = 
        "AttributePeer";

    /**
     *  Gets a List of all of the Attribute objects in the database.
     */
    public static List getAttributes(String attributeType, boolean includeDeleted,
                                     String sortColumn, String sortPolarity)
        throws Exception
    {
        List result = null;
        Boolean deletedBool = (includeDeleted ? Boolean.TRUE : Boolean.FALSE);
        // 4th element is ignored due to bug in torque that is being
        // Matched in ScarabCache
        Serializable[] keys = {ATTRIBUTE_PEER, attributeType, deletedBool, 
                               sortColumn, null, sortPolarity};
        Object obj = ScarabCache.get(keys); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.add(AttributePeer.ATTRIBUTE_ID, 0, Criteria.NOT_EQUAL);
            if (!includeDeleted)
            {
                crit.add(AttributePeer.DELETED, 0);
            }
            // add user type criteria  - user or non-user
            if (attributeType.equals("user"))
            {
                crit.add(AttributePeer.ATTRIBUTE_TYPE_ID, 
                         AttributeTypePeer.USER_TYPE_KEY);
            }
            else
            {
                crit.add(AttributePeer.ATTRIBUTE_TYPE_ID, 
                         AttributeTypePeer.USER_TYPE_KEY, Criteria.NOT_EQUAL);
            }
            // sort criteria
            if (sortColumn.equals("desc"))
            {
                addSortOrder(crit, AttributePeer.DESCRIPTION, 
                             sortPolarity);
            }
            else if (sortColumn.equals("date"))
            {
                addSortOrder(crit, AttributePeer.CREATED_DATE, 
                             sortPolarity);
            }
            else if (sortColumn.equals("type"))
            {
                crit.addJoin(AttributePeer.ATTRIBUTE_TYPE_ID, 
                             AttributeTypePeer.ATTRIBUTE_TYPE_ID);
                addSortOrder(crit, AttributeTypePeer .ATTRIBUTE_TYPE_NAME, 
                             sortPolarity);
            }
            else if (!sortColumn.equals("user"))
            {
                // sort by name
                addSortOrder(crit, AttributePeer.ATTRIBUTE_NAME, 
                             sortPolarity);
            }
            result = doSelect(crit);
        }
        else 
        {
            result = (List)obj;
        }
        if (sortColumn.equals("user"))
        {
            result = sortAttributesByCreatingUser(result, sortPolarity);
        }
                
        ScarabCache.put(result, keys);
        return result;
    }

    /**
     *  Gets a List of all of the Attribute objects filtered
     *  on name or description
     */
    public static List getFilteredAttributes(String name, String description,
                                             String searchField)
        throws Exception
    {
        List result = null;
        List allAttributes = getAttributes();
        if (allAttributes == null || allAttributes.size() == 0) 
        {
            result = Collections.EMPTY_LIST;
        }
        else 
        {
            List attrIds = new ArrayList();
            for (int i = 0; i < allAttributes.size(); i++)
            {
                attrIds.add(((Attribute)allAttributes.get(i)).getAttributeId());
            }
            Criteria crit = new Criteria();
            crit.addIn(AttributePeer.ATTRIBUTE_ID, attrIds);
            Criteria.Criterion c = null;
            Criteria.Criterion c1 = null;
            Criteria.Criterion c2 = null;
            
            if (name != null)
            {
                c1 = crit.getNewCriterion(AttributePeer.ATTRIBUTE_NAME,
                         addWildcards(name), Criteria.LIKE);
            }
            if (description != null)
            { 
                c2 = crit.getNewCriterion(AttributePeer.DESCRIPTION,
                         addWildcards(description), Criteria.LIKE);
            }
            if (searchField.equals("Name"))
            {
                c = c1;
            } 
            else if (searchField.equals("Description"))
            {
                c = c2;
            }
            else if (searchField.equals("Any"))
            {
                c = c1.or(c2);
            }
            crit.and(c);
            result = AttributePeer.doSelect(crit);
        }
        return result;
    }

    private static Object addWildcards(String s)
    {
        return new StringBuffer(s.length() + 2)
            .append('%').append(s).append('%').toString(); 
    }

    /**
     *  Gets a List of all of the Attribute objects in the database.
     *  Sorts on selected column.
     */
    public static List getAttributes()
        throws Exception
    {
        return getAttributes(NON_USER, false, AttributePeer.ATTRIBUTE_NAME, "asc");
    }

    /**
     *  Gets a List of Attribute objects in the database.
     */
    public static List getAttributes(String attributeType)
        throws Exception
    {
        return getAttributes(attributeType, false, AttributePeer.ATTRIBUTE_NAME, 
                             "asc");
    }

    /**
     *  Gets a List of Attribute objects in the database.
     */
    public static List getAttributes(String attributeType, boolean includeDeleted)
        throws Exception
    {
        return getAttributes(attributeType, includeDeleted, AttributePeer.ATTRIBUTE_NAME, 
                             "asc");
    }

    /**
     *  Gets a List of data Attribute objects in the database.
     *  Sorts on selected column.
     */
    public static List getAttributes(String sortColumn, 
                                     String sortPolarity)
        throws Exception
    {
        return getAttributes(NON_USER, false, sortColumn, sortPolarity);
    }

    private static List sortAttributesByCreatingUser(List result,
                                                     String sortPolarity)
        throws Exception
    {
        final int polarity = ("asc".equals(sortPolarity)) ? 1 : -1;   
        Comparator c = new Comparator() 
        {
            public int compare(Object o1, Object o2) 
            {
                int i = 0;
                try
                {
                    i = polarity * 
                        ((Attribute)o1).getCreatedUserName()
                         .compareTo(((Attribute)o2).getCreatedUserName());
                }
                catch (Exception e)
                {
                    //
                }
                return i;
             }
        };
        Collections.sort(result, c);
        return result;
    }

    private static Criteria addSortOrder(Criteria crit, 
                       String sortColumn, String sortPolarity)
    {
        if (sortPolarity.equals("desc"))
        {
            crit.addDescendingOrderByColumn(sortColumn);
        }
        else
        {
            crit.addAscendingOrderByColumn(sortColumn);
        }
        return crit;
    }

}
