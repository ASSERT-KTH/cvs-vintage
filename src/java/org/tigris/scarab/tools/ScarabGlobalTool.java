package org.tigris.scarab.tools;

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

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Category;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.util.AccessControlList;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.UnknownEntityException;

import org.apache.velocity.app.FieldMethodizer;

import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.IssueTypePeer;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.services.security.ScarabSecurity;

import org.apache.torque.util.Criteria;

/**
 * This scope is an object that is made available as a global
 * object within the system.
 * This object must be thread safe as multiple
 * requests may access it at the same time. The object is made
 * available in the context as: $scarabG
 * <p>
 * The design goals of the Scarab*API is to enable a <a
 * href="http://jakarta.apache.org/turbine/pullmodel.html">pull based
 * methodology</a> to be implemented.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ScarabGlobalTool.java,v 1.22 2002/02/19 19:30:10 jmcnally Exp $
 */
public class ScarabGlobalTool implements ScarabGlobalScope
{
    private static final Category log = 
        Category.getInstance("org.tigris.scarab");

    /**
     * holds the Scarab constants
     */
    private FieldMethodizer constant = null;
    
    /**
     * holds the Scarab security permission constants
     */
    private FieldMethodizer security = null;
    
    public void init(Object data)
    {
    }
    
    public void refresh()
    {
    }
    
    /**
     * Constructor does initialization stuff
     */    
    public ScarabGlobalTool()
    {
        constant = new FieldMethodizer(
                                       "org.tigris.scarab.util.ScarabConstants");
        security = new FieldMethodizer(
                                       "org.tigris.scarab.services.security.ScarabSecurity");
    }
    
    /**
     * holds the Scarab constants. it will be available to the template system
     * as $scarabG.Constant.CONSTANT_NAME.
     */
    public FieldMethodizer getConstant()
    {
        return constant;
    }
    
    /**
     * holds the Scarab permission constants.  It will be available to 
     * the template system as $scarabG.PERMISSION_NAME.
     */
    public FieldMethodizer getPermission()
    {
        return security;
    }

    /**
     * Returns a list of all the permissions in use by scarab.  
     *
     * @return a <code>List</code> of <code>String</code>s
     */
    public List getAllPermissions()
    {
        return ScarabSecurity.getAllPermissions();
    }
    
    /**
     * Gets a List of all of the Attribute objects.
     */
    public List getAllAttributes()
        throws Exception
    {
        return AttributePeer.getAllAttributes();
    }
    
    /**
     * Gets a List of all of user objects
     * By attribute Type : either user, or non-user.
     */
    public List getUserAttributes()
        throws Exception
    {
        return AttributePeer.getAttributes("user");
    }

    /**
     * Gets a List of all of user Attribute objects.
     */
    public List getAttributes(String attributeType)
        throws Exception
    {
        return AttributePeer.getAttributes(attributeType);
    }

    /**
     * gets a list of all Issue Types 
     */
    public List getAllIssueTypes()
        throws Exception
    {
        return IssueTypePeer.getAllIssueTypes(true);
    }
    
    /** 
     * Returns a List of users based on the given search criteria. This method
     * is an overloaded function which returns an unsorted list of users.
     *
     * @param searchField the name of the database attribute to search on
     * @param searchCriteria the search criteria to use within the LIKE command
     * @returns a List of users matching the specifed criteria
     * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
     */
    public List getSearchUsers(String searchField, String searchCriteria)
        throws Exception
    {
        return (getSearchUsers(searchField, searchCriteria, null, null));
    }
    
    /** 
     * Returns a List of users based on the given search criteria and orders
     * the list by the specified field.  The method will use the LIKE
     * SQL command and perform a search as such (assuming 'doug' is
     * specified as the search criteria:
     * <code>WHERE some_field LIKE '%doug%'</code>
     *
     * @param searchField the name of the database attribute to search on
     * @param searchCriteria the search criteria to use within the LIKE command
     * @param orderByField the name of the database attribute to order the list by
     * @param ascOrDesc either "ASC" of "DESC" specifying the order to sort in
     * @returns a List of users matching the specifed criteria
     * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
     */
    /**
     * Describe <code>getSearchUsers</code> method here.
     *
     * @param searchField a <code>String</code> value
     * @param searchCriteria a <code>String</code> value
     * @param orderByField a <code>String</code> value
     * @param ascOrDesc a <code>String</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getSearchUsers(String searchField, String searchCriteria, 
                               String orderByField, String ascOrDesc)
        throws Exception
    {
        ArrayList userSearchList = new ArrayList();
        String lSearchField = "";
        String lOrderByField = "";
        
        Criteria criteria = new Criteria();
        
        // add the input from the user
        if (searchCriteria != null && searchCriteria.length() > 0)
        {
            if (searchField.equals("FIRST_NAME"))
            {
                lSearchField = ScarabUser.FIRST_NAME;
            }
            else if (searchField.equals("LAST_NAME"))
            {
                lSearchField = ScarabUser.LAST_NAME;
            }
            else if (searchField.equals("LOGIN_NAME"))
            {
                lSearchField = ScarabUser.USERNAME;
            }
            else
            {
                lSearchField = ScarabUser.EMAIL;
            }
            
            // FIXME: Probably shouldn't be using ScarabUserPeerImpl here
            // What should we do to get the right table name?
            lSearchField = ScarabUserImplPeer.getTableName() + '.' + lSearchField;
            
            criteria = criteria.add(lSearchField,
                                        (Object)("%" + searchCriteria.trim() + "%"),Criteria.LIKE);
        }
        
        // sort the results
        if (orderByField != null && orderByField.length() > 0)
        {
            if (orderByField.equals("FIRST_NAME"))
            {
                lOrderByField = ScarabUser.FIRST_NAME;
            }
            else if (orderByField.equals("LAST_NAME"))
            {
                lOrderByField = ScarabUser.LAST_NAME;
            }
            else if (orderByField.equals("LOGIN_NAME"))
            {
                lOrderByField = ScarabUser.USERNAME;
            }
            else
            {
                lOrderByField = ScarabUser.EMAIL;
            }
            
            // FIXME: Probably shouldn't be using ScarabUserPeerImpl here
            // What should we do to get the right table name?
            lOrderByField = ScarabUserImplPeer.getTableName() + '.' + lOrderByField;
            
            if (ascOrDesc != null && ascOrDesc.equalsIgnoreCase("DESC"))
            {
                criteria = criteria.addDescendingOrderByColumn(lOrderByField);
            } 
            else
            {
                criteria = criteria.addAscendingOrderByColumn(lOrderByField);
            }
        }
        
        User[] tempUsers = TurbineSecurity.getUsers(criteria);  
        for (int i=0; i < tempUsers.length; i++)
        {
            userSearchList.add(i, tempUsers[i]);
        }
        return (userSearchList);
    }

    /**
     * Creates a new array with elements reversed from the given array.
     *
     * @param the orginal <code>Object[]</code> 
     * @return a new <code>Object[]</code> with values reversed from the 
     * original
     */
    public Object[] reverse(Object[] a)
    {
        Object[] b = new Object[a.length];
        for ( int i=a.length-1; i>=0; i--) 
        {
            b[a.length-1-i] = a[i];
        }
        return b;
    }

    /**
     * Creates a new List with elements reversed from the given List.
     *
     * @param the orginal <code>List</code> 
     * @return a new <code>List</code> with values reversed from the 
     * original
     */
    public List reverse(List a)
    {
        int size = a.size();
        List b = new ArrayList(size);
        for ( int i=size-1; i>=0; i--) 
        {
            b.add(a.get(i));
        }
        return b;
    }

    /**
     * Creates  a view of the portion of the given
     * List between the specified fromIndex, inclusive, and toIndex, exclusive
     * The list returned by this method is backed by the original, so changes
     * to either affect the other.
     *
     * @param the orginal <code>List</code> 
     * @return a derived <code>List</code> with a view of the original
     */
    public List subset(List a, Integer fromIndex, Integer toIndex)
    {
        int from = Math.min(fromIndex.intValue(), a.size());         
        from = Math.max(from, 0);
        int to = Math.min(toIndex.intValue(), a.size()); 
        to = Math.max(to, from); 
        return a.subList(from, to);
    }

    /**
     * Creates a new array with a view of the portion of the given array
     * between the specified fromIndex, inclusive, and toIndex, exclusive
     *
     * @param the orginal <code>Object[]</code> 
     * @return a new <code>Object[]</code> with a view of the original
     */
    public Object[] subset(Object[] a, Integer fromIndex, Integer toIndex)
    {
        int from = Math.min(fromIndex.intValue(), a.length);
        from = Math.max(from, 0);
        int to = Math.min(toIndex.intValue(), a.length); 
        to = Math.max(to, from); 
        Object[] b = new Object[from-to];
        for ( int i=from-1; i>=to; i--) 
        {
            b[i-to] = a[i];
        }
        return b;
    }

    public void log(String s)
    {
        log.debug(s);
    }

    public void log(String category, String s)
    {
        Category.getInstance(category).debug(s);
    }
}

