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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.torque.util.Criteria;
import com.workingdogs.village.Record;

import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.AttributeGroupPeer;
import org.tigris.scarab.om.RAttributeAttributeGroupPeer;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.RModuleUserAttributePeer;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.tools.localization.L10NKeySet;

/**
 * Access to data relating to attributes.
 *
 * @see org.tigris.scarab.da.AttributeAccess
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @since Scarab 0.17
 */
public class AttributeAccess
{
    /** Method name used as part of a cache key. */
    private static final String RETRIEVE_QUERY_COLUMN_IDS =
        "retrieveQueryColumnIDs";
    private static final String RETRIEVE_QUICK_SEARCH_ATTRIBUTES = 
        "retrieveQuickSearchAttributeIDs";
    private static final String RETRIEVE_REQUIRED_ATTRIBUTES = 
        "retrieveRequiredAttributeIDs";
    private static final String RETRIEVE_ACTIVE_ATTRIBUTES = 
        "retrieveActiveAttributes";
    private static final String RETRIEVE_DEFAULT_TEXT_ATTRIBUTE = 
        "retrieveDefaultTextAttributeID";
    private static final String RETRIEVE_FIRST_ACTIVE_TEXT_ATTRIBUTE = 
        "retrieveFirstActiveTextAttributeID";

    private Serializable thisKey = AttributeAccess.class;


    /**
     * Constructor used by {@link org.tigris.scarab.da.DAFactory}.
     */
    public AttributeAccess()
    {
    }

    /**
     * Retrieves a list of attribute identifiers for use in
     * determining which columns to display for a user's query
     * results.
     *
     * @param userID The associated user (must be
     * non-<code>null</code>).
     * @param listID The associated artifact type list (can be
     * <code>null</code>).
     * @param moduleID The associated module (ignored if <code>null</code>).
     * @param artifactTypeID The associated artifact type (ignored if
     * <code>null</code>).
     * @return A list of attribute identifiers.
     * @throws DAException If any problems are encountered.
     */
    public List retrieveQueryColumnIDs(String userID, String listID,
                                       String moduleID, String artifactTypeID) throws DAException
    {
        List result = null;
        Object obj = ScarabCache.get(thisKey,
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
                    result.add(((Record) i.next()).getValue(1).asString());
                }
            }
            catch (Exception e)
            {
                throw new DAException(L10NKeySet.ExceptionFailedToReadIdentifierList,
                                      e);
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

    /**
     * Deletes the persisted choice of issue list display columns for
     * the given user and artifact type(s).
     *
     * @param userID The associated user (must be
     * non-<code>null</code>).
     * @param listID The associated artifact type list (can be
     * <code>null</code>).
     * @param moduleID The associated module (ignored if <code>null</code>).
     * @param artifactTypeID The associated artifact type (ignored if
     * <code>null</code>).
     * @throws DAException If any problems are encountered.
     */
    public void deleteQueryColumnIDs(String userID, String listID,
                                     String moduleID, String artifactTypeID)
        throws DAException
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
            throw new DAException(L10NKeySet.ExceptionFailedToDeleteIdentifierList,
                                  e);
        }
    }


    /**
     * Set of attributeIDs which are active and required within the given 
     * module for the given issue type and whose attribute group's are 
     * also active.
     *
     * @param moduleID The associated module (must be
     * non-<code>null</code>).
     * @param artifactTypeID The associated artifact type (must be
     * non-<code>null</code>).
     * @return an <code>String</code> of String attribute ids
     */
    public Set retrieveRequiredAttributeIDs(String moduleID, 
                                            String artifactTypeID)
    throws DAException
    {
        Set attributes = null;
        Object obj = ScarabCache.get(thisKey, RETRIEVE_REQUIRED_ATTRIBUTES, 
                                     moduleID, artifactTypeID); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria(7)
                .add(RModuleAttributePeer.REQUIRED, true)
                .add(RModuleAttributePeer.ACTIVE, true)
                .add(RModuleAttributePeer.MODULE_ID, moduleID)
                .add(RModuleAttributePeer.ISSUE_TYPE_ID, artifactTypeID);
            addGroupCriteria(crit, moduleID, artifactTypeID);
            attributes = getRMAAttributeIdSet(crit);
            ScarabCache.put(attributes, thisKey, RETRIEVE_REQUIRED_ATTRIBUTES, 
                            moduleID, artifactTypeID);
        }
        else 
        {
            attributes = (Set) obj;
        }
        return attributes;
    }
    
    /**
     * Set of attributeIDs which are active and marked for custom search 
     * within the given 
     * module for the given issue type and whose attribute group's are 
     * also active.
     *
     * @param moduleID The associated module (must be
     * non-<code>null</code>).
     * @param artifactTypeID The associated artifact type (must be
     * non-<code>null</code>).
     * @return an <code>Set</code> of String attribute ids
     */
    public Set retrieveQuickSearchAttributeIDs(String moduleID, 
                                               String artifactTypeID)
    throws DAException
    {
        Set attributes = null;
        Object obj = ScarabCache.get(thisKey, RETRIEVE_QUICK_SEARCH_ATTRIBUTES,
                                     moduleID, artifactTypeID); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria(3)
                .add(RModuleAttributePeer.QUICK_SEARCH, true)
                .add(RModuleAttributePeer.MODULE_ID, moduleID)
                .add(RModuleAttributePeer.ISSUE_TYPE_ID, artifactTypeID);
            attributes = getRMAAttributeIdSet(crit);
            ScarabCache.put(attributes, thisKey,
                            RETRIEVE_QUICK_SEARCH_ATTRIBUTES, 
                            moduleID, artifactTypeID);
        }
        else 
        {
            attributes = (Set) obj;
        }
        return attributes;
    }

    private Set getRMAAttributeIdSet(Criteria crit)
        throws DAException
    {
        crit.addSelectColumn(RModuleAttributePeer.ATTRIBUTE_ID);
        Set attributes = null;
        try 
        {
            List records = RModuleAttributePeer.doSelectVillageRecords(crit);
            attributes = new HashSet(records.size());
            for (Iterator i = records.iterator(); i.hasNext(); )
            {
                attributes.add(((Record) i.next()).getValue(1).asString());
            }
        }
        catch (Exception e)
        {
            throw new DAException(L10NKeySet.ExceptionFailedToReadIdentifierList, e);
        }
        return attributes;
    }

    private void addGroupCriteria(Criteria crit, 
                                  String moduleID, String artifactTypeID)
    {
        crit.addJoin(RAttributeAttributeGroupPeer.ATTRIBUTE_ID, 
                     RModuleAttributePeer.ATTRIBUTE_ID)
            .addJoin(RAttributeAttributeGroupPeer.GROUP_ID, 
                     AttributeGroupPeer.ATTRIBUTE_GROUP_ID)
            .add(AttributeGroupPeer.MODULE_ID, moduleID)
            .add(AttributeGroupPeer.ISSUE_TYPE_ID, artifactTypeID)
            .add(AttributeGroupPeer.ACTIVE, true);
    }


    /**
     * Torque <code>Attribute</code>s which are active within the 
     * given module for the given issue type 
     * <strike>and whose attribute group's are also active</strike>.  
     *
     * @param moduleID The associated module (must be
     * non-<code>null</code>).
     * @param artifactTypeID The associated artifact type (must be
     * non-<code>null</code>).
     * @param isOrdered indication whether an iterator over the Attributes 
     * should return them in their natural order.
     * @return an <code>Collection</code> of torque Attribute objects.  The
     * collection will be a List if isOrdered is true, otherwise a Set is
     * returned.
     */
    public Collection retrieveActiveAttributeOMs(String moduleID,
                                                 String artifactTypeID, 
                                                 boolean isOrdered)
    throws DAException
    {
        Collection attributes = null;
        Boolean ordered = isOrdered ? Boolean.TRUE : Boolean.FALSE;
        Object obj = ScarabCache.get(thisKey, RETRIEVE_ACTIVE_ATTRIBUTES, 
                                     moduleID, artifactTypeID, ordered);
        if (obj == null)
        {
            Criteria crit = new Criteria(2);
            crit.add(RModuleAttributePeer.ACTIVE, true);
            crit.add(RModuleAttributePeer.MODULE_ID, moduleID);
            crit.add(RModuleAttributePeer.ISSUE_TYPE_ID, artifactTypeID);
            // FIXME! would like to eliminate attributes that exist in
            // inactive groups, but user attributes are not in groups, so 
            // this cannot be as simple as the required attributes which
            // never include user attributes.  Will probably require a left
            // join.  Leaving as it was for now.  An attribute is active
            // even if it is in an inactive group.
            //addGroupCriteria(crit, moduleID, artifactTypeID);

            if (isOrdered) 
            {
                crit.addAscendingOrderByColumn(
                    RModuleAttributePeer.PREFERRED_ORDER);
                crit.addAscendingOrderByColumn(
                    RModuleAttributePeer.DISPLAY_VALUE);                
            }

            crit.addJoin(AttributePeer.ATTRIBUTE_ID, 
                         RModuleAttributePeer.ATTRIBUTE_ID);
            List records = null;
            try 
            {
                records = AttributePeer.doSelect(crit);
            }
            catch (Exception e)
            {
                throw new DAException(L10NKeySet.ExceptionFailedToReadIdentifierList, e);
            }
            if (isOrdered) 
            {
                attributes = new ArrayList(records.size());                
            }
            else 
            {
                attributes = new HashSet(records.size());
            }

            for (Iterator i = records.iterator(); i.hasNext();) 
            {
                attributes.add(i.next());
            }
            
            ScarabCache.put(attributes, thisKey, RETRIEVE_ACTIVE_ATTRIBUTES, 
                            moduleID, artifactTypeID, ordered);
        }
        else
        {
            attributes = (Collection)obj;
        }
        return attributes;
    }


    /**
     * Retrieves the attribute ID which is active and marked as the 
     * default text attribute within the given 
     * module for the given issue type and whose attribute group is 
     * also active.
     *
     * @param moduleID The associated module (must be
     * non-<code>null</code>).
     * @param artifactTypeID The associated artifact type (must be
     * non-<code>null</code>).
     * @return an <code>String</code> attribute ID
     */
    public String retrieveDefaultTextAttributeID(String moduleID, 
                                                 String artifactTypeID)
        throws DAException
    {
        String result = null;
        Object obj = ScarabCache.get(thisKey, RETRIEVE_DEFAULT_TEXT_ATTRIBUTE, 
                                     moduleID, artifactTypeID); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria(7);
            crit.add(RModuleAttributePeer.DEFAULT_TEXT_FLAG, true)
                .add(RModuleAttributePeer.ACTIVE, true)
                .add(RModuleAttributePeer.MODULE_ID, moduleID)
                .add(RModuleAttributePeer.ISSUE_TYPE_ID, artifactTypeID);
            addGroupCriteria(crit, moduleID, artifactTypeID);
            result = getRMAAttributeId(crit);
            ScarabCache.put(result, thisKey, RETRIEVE_DEFAULT_TEXT_ATTRIBUTE,
                            moduleID, artifactTypeID);
        }
        else 
        {
            result = (String) obj;
        }
        
        return result.length() == 0 ? null : result;
    }

    private String getRMAAttributeId(Criteria crit)
        throws DAException
    {
        String result = null;
        crit.addSelectColumn(RModuleAttributePeer.ATTRIBUTE_ID);
        try 
        {
            List records = RModuleAttributePeer.doSelectVillageRecords(crit);
            if (records.isEmpty()) 
            {
                result = ""; // for caching
            }
            else
            {
                result = ((Record) records.get(0)).getValue(1).asString();
            }
        }
        catch (Exception e)
        {
            throw new DAException(L10NKeySet.ExceptionFailedToReadIdentifierList, e);
        }
        return result;
    }


    /**
     * Retrieves the attribute ID which is active and is the first id returned
     * when results are ordered based on numerical preferred order and/or 
     * alphabetical by name within the given 
     * module for the given issue type and whose attribute group is 
     * also active.
     *
     * @param moduleID The associated module (must be
     * non-<code>null</code>).
     * @param artifactTypeID The associated artifact type (must be
     * non-<code>null</code>).
     * @return An <code>String</code> attribute ID.
     */
    public String retrieveFirstActiveTextAttributeID(String moduleID, 
                                                     String artifactTypeID)
        throws DAException
    {
        String result = null;

        Object obj = ScarabCache.get(thisKey, 
                                     RETRIEVE_FIRST_ACTIVE_TEXT_ATTRIBUTE, 
                                     moduleID, artifactTypeID); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria(7);
            crit.add(RModuleAttributePeer.ACTIVE, true)
                .add(RModuleAttributePeer.MODULE_ID, moduleID)
                .add(RModuleAttributePeer.ISSUE_TYPE_ID, artifactTypeID);
            addGroupCriteria(crit, moduleID, artifactTypeID);

            crit.addAscendingOrderByColumn(
                RModuleAttributePeer.PREFERRED_ORDER);
            crit.addAscendingOrderByColumn(
                RModuleAttributePeer.DISPLAY_VALUE);                

            result = getRMAAttributeId(crit);
            ScarabCache.put(result, thisKey, 
                            RETRIEVE_FIRST_ACTIVE_TEXT_ATTRIBUTE,
                            moduleID, artifactTypeID);
        }
        else 
        {
            result = (String) obj;
        }
        
        return result.length() == 0 ? null : result;
    }
}
