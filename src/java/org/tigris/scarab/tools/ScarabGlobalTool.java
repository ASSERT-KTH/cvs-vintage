package org.tigris.scarab.tools;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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
 
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.User;

import org.apache.velocity.app.FieldMethodizer;

import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.IssueTypePeer;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;

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
 * @version $Id: ScarabGlobalTool.java,v 1.8 2001/11/17 00:21:52 jon Exp $
 */
public class ScarabGlobalTool implements ScarabGlobalScope
{
    /**
     * holds the Scarab constants
     */
    private FieldMethodizer constant = null;

    /**
     * holds the Scarab security permission constants
     */
    private FieldMethodizer security = null;

    /**
     * Used for formatting dates in the format: M/d/yy
     */
    private static final String MDYY_DATE = "M/d/yy";

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
     * the template system as $scarabG..PERMISSION_NAME.
     */
    public FieldMethodizer getPermission()
    {
        return security;
    }
    
    /**
     * This is used to get the format for a date
     * right now, it returns "M/d/yy". In the future, we 
     * can write code to return the correct date based on
     * Localization needs.
     */
    public String getDateFormat()
    {
        return MDYY_DATE;
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
}
