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

import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.User;

import org.apache.velocity.app.FieldMethodizer;

import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.IssueTypePeer;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.MITListManager;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.workflow.Workflow;
import org.tigris.scarab.workflow.WorkflowFactory;
import org.tigris.scarab.util.IssueIdParser;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.SkipFiltering;
import org.tigris.scarab.util.SimpleSkipFiltering;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.util.ScarabUtil;

import org.apache.torque.util.Criteria;
import org.apache.torque.TorqueException;

import org.apache.turbine.Turbine;

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
 * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
 * @version $Id: ScarabGlobalTool.java,v 1.56 2003/03/28 00:01:46 jon Exp $
 */
public class ScarabGlobalTool implements ScarabGlobalScope
{
    private static final Logger LOG = 
        Logger.getLogger("org.tigris.scarab");

    /**
     * holds the Scarab constants
     */
    private FieldMethodizer constant = null;
    
    /**
     * holds the Scarab security permission constants
     */
    private FieldMethodizer security = null;

    /**
     * holds the Scarab parameter name constants
     */
    private FieldMethodizer parameterName = null;

    private static final String BUILD_VERSION = 
        Turbine.getConfiguration().getString("scarab.build.version", "");

    private static String siteName = 
        Turbine.getConfiguration().getString("scarab.site.name","");

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
        parameterName = new FieldMethodizer(
            "org.tigris.scarab.om.GlobalParameterManager");
    }

    /**
     * returns Scarab's build version.
     */
    public String getBuildVersion()
    {
        return BUILD_VERSION;
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
     * holds the names of parameters that are configurable through the ui.
     */
    public FieldMethodizer getParameterName()
    {
        return parameterName;
    }

    public GlobalParameterManager getParameter()
    {
        return GlobalParameterManager.getManager();
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
     * Gets a List of all of the data (non-user) Attribute objects.
     */
    public List getAllAttributes()
        throws Exception
    {
        return AttributePeer.getAttributes();
    }
    
    /**
     * Gets a List of all of the Attribute objects by type.
     */
    public List getAttributes(String attributeType)
        throws Exception
    {
        return AttributePeer.getAttributes(attributeType, false);
    }

    /**
     * Gets a List of all of the  data (non-user) Attribute objects.
     * Passes in sort criteria.
     */
    public List getAllAttributes(String attributeType, boolean includeDeleted,
                                 String sortColumn, String sortPolarity)
        throws Exception
    {
        return AttributePeer.getAttributes(attributeType, includeDeleted, 
                                           sortColumn, sortPolarity);
    }

    /**
     * Gets a List of all of the  data (non-user) Attribute objects.
     * Passes in sort criteria.
     */
    public List getAllAttributes(String sortColumn, String sortPolarity)
        throws Exception
    {
        return AttributePeer.getAttributes(sortColumn, sortPolarity);
    }
    
    /**
     * Gets a List of all of user Attributes.
     */
    public List getUserAttributes()
        throws Exception
    {
        return AttributePeer.getAttributes("user");
    }


    /**
     * Gets a List of all of the Attribute objects.
     */
    public List getUserAttributes(String sortColumn, String sortPolarity)
        throws Exception
    {
        return AttributePeer.getAttributes("user", false, sortColumn, sortPolarity);
    }
    
    /**
     * Gets a List of all of the Attribute objects.
     */
    public List getUserAttributes(boolean includeDeleted, String sortColumn, 
                                  String sortPolarity)
        throws Exception
    {
        return AttributePeer.getAttributes("user", includeDeleted, sortColumn, sortPolarity);
    }

    /**
     * Gets a List of all of user Attribute objects.
     */
    public List getAttributes(String attributeType, boolean includeDeleted, 
                              String sortColumn, String sortPolarity)
        throws Exception
    {
        return AttributePeer.getAttributes(attributeType, includeDeleted,
                                           sortColumn, sortPolarity);
    }

    public List getAllIssueTypes()
        throws Exception
    {
        return IssueTypePeer.getAllIssueTypes(false, "name", "asc");
    }

    /**
     * gets a list of all Issue Types 
     */
    public List getAllIssueTypes(boolean deleted)
        throws Exception
    {
        return IssueTypePeer.getAllIssueTypes(deleted, "name", "asc");
    }
    
    /**
     * Gets a List of all of the Attribute objects.
     */
    public List getAllIssueTypes(boolean deleted, String sortColumn, 
                                 String sortPolarity)
        throws Exception
    {
        return IssueTypePeer.getAllIssueTypes(deleted, sortColumn, sortPolarity);
    }
    
    /**
     * Makes the workflow tool accessible.
     */
    public Workflow getWorkflow()
        throws Exception
    {
        return WorkflowFactory.getInstance();
    }
    
    /** 
     * Returns a List of users based on the given search criteria. This method
     * is an overloaded function which returns an unsorted list of users.
     *
     * @param searchField the name of the database attribute to search on
     * @param searchCriteria the search criteria to use within the LIKE command
     * @return a List of users matching the specifed criteria
     *
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
     * @return a List of users matching the specifed criteria
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
                                        (Object)('%' + searchCriteria.trim() + '%'),Criteria.LIKE);
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
     * Create a list of Modules from the given list of issues.  Each
     * Module in the list of issues will only occur once in the list of 
     * Modules.
     */
    public List getModulesFromIssueList(List issues)
        throws TorqueException
    {
        return ModuleManager.getInstancesFromIssueList(issues);
    }

    public MITListManager getMITListManager()
    {
        return MITListManager.getManager();
    }

    /**
     * Get a new Date object initialized to the current time.
     * @return a <code>Date</code> value
     */
    public Date getNow()
    {
        return new Date();
    }

    /**
     * Creates a new array with elements reversed from the given array.
     *
     * @param a the orginal <code>Object[]</code>
     * @return a new <code>Object[]</code> with values reversed from the 
     * original
     */
    public Object[] reverse(Object[] a)
    {
        Object[] b = new Object[a.length];
        for (int i=a.length-1; i>=0; i--) 
        {
            b[a.length-1-i] = a[i];
        }
        return b;
    }

    /**
     * Creates a new <code>List</code> with elements reversed from the
     * given <code>List</code>.
     *
     * @param a the orginal <code>List</code>
     * @return a new <code>List</code> with values reversed from the 
     * original
     */
    public List reverse(List a)
    {
        int size = a.size();
        List b = new ArrayList(size);
        for (int i=size-1; i>=0; i--) 
        {
            b.add(a.get(i));
        }
        return b;
    }

    /**
     * Creates a view of the portion of the given
     * <code>List</code> between the specified <code>fromIndex</code>, inclusive, and
     * <code>toIndex</code>, exclusive.
     * The list returned by this method is backed by the original, so changes
     * to either affect the other.
     *
     * @param a the orginal <code>List</code>
     * @param fromIndex the start index of the returned subset
     * @param toIndex the end index of the returned subset
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
     * @param a the orginal <code>Object[]</code>
     * @param fromIndex the start index of the returned subset
     * @param toIndex the end index of the returned subset
     * @return a new <code>Object[]</code> with a view of the original
     */
    public Object[] subset(Object[] a, Integer fromIndex, Integer toIndex)
    {
        int from = Math.min(fromIndex.intValue(), a.length);
        from = Math.max(from, 0);
        int to = Math.min(toIndex.intValue(), a.length); 
        to = Math.max(to, from); 
        Object[] b = new Object[from-to];
        for (int i=from-1; i>=to; i--) 
        {
            b[i-to] = a[i];
        }
        return b;
    }

    /**
     * Velocity has no way of getting the size of an <code>Object[]</code>
     * easily. Usually this would be done by calling obj.length
     * but this doesn't work in Velocity.
     * @param obj the <code>Object[]</code>
     * @return the number of objects in the <code>Object[]</code> or -1 if obj is null
     */
    public int sizeOfArray(Object[] obj)
    {
        return (obj == null) ? -1 : obj.length;
    }

    public boolean isString(Object obj)
    {
        return obj instanceof String;
    }

    /**
     * Breaks text into a list of Strings.  Text is separated into tokens
     * at characters given in delimiters.  The delimiters are not part
     * of the resulting tokens. if delimiters is empty or null, "\n" is used.
     *
     * @param text a <code>String</code> value
     * @param delimiters a <code>String</code> value
     * @return a <code>List</code> value
     */
    public Enumeration tokenize(String text, String delimiters)
    {
        if (delimiters == null || delimiters.length() == 0) 
        {
            delimiters = "\n";
        }

        if (text == null) 
        {
            text = "";
        }
        
        StringTokenizer st = new StringTokenizer(text, delimiters);
        return st;
    }

    public List linkIssueIds(Module module, String text)
    {
        List result = null;
        try
        {
            result = IssueIdParser.tokenizeText(module, text);
        }
        catch (Exception e)
        {
            // return the text as is and log the error
            result = new ArrayList(1);
            result.add(text);
            Log.get().warn("Could not linkify text: " + text, e);
        }
        return result;
    }

    public SkipFiltering getCommentText(String text, ScarabLink link, Module currentModule)
    {
        SkipFiltering sf = null;
        try
        {
            sf = new SimpleSkipFiltering(ScarabUtil.linkifyText(text, link, currentModule));
        }
        catch (Exception e)
        {
            sf = new SimpleSkipFiltering(text);
        }
        return sf;
    }

    /**
     * Logs a message at the debug level.  Useful for "I am here" type 
     * messages. The category is "org.tigris.scarab". 
     *
     * @param s message to log
     */
    public void log(String s)
    {
        log.debug(s);
    }

    /**
     * Logs a message at the debug level.  Useful for "I am here" type 
     * messages. The category in which to log is also specified. 
     *
     * @param category log4j Category
     * @param s message to log
     */
    public void log(String category, String s)
    {
        Category.getInstance(category).debug(s);
    }

    /**
     * Prints a message to standard out.  Useful for "I am here" type 
     * messages. 
     *
     * @param s message to log
     */
    public void print(String s)
    {
        System.out.println(s);
    }

    /**
     * Provides the site name for the top banner.
     *
     * @return the configured site name
     */
    public String getSiteName()
    {
        if (siteName == null)
        {
            siteName = "";
        }
        return siteName;
    }

    /**
     * Returns an <code>int</code> representation of the given
     * <code>Object</code> whose toString method should be a valid integer.
     * If the <code>String</code> cannot be parsed <code>0</code> is returned.
     * @param obj the object
     * @return the <code>int</code> representation of the <code>Object</code>
     *  if possible or <code>0</code>.
     */
    public int getInt(Object obj)
    {
        int result = 0;
        if (obj != null) 
        {
            try 
            {
                result = Integer.parseInt(obj.toString());
            }
            catch (Exception e)
            {
                Log.get().error(obj + " cannot convert to an integer.", e);
            }   
        }
        return result;
    }

    public int getCALENDAR_YEAR_FIELD()
    {
        return Calendar.YEAR;
    }

    public int getCALENDAR_MONTH_FIELD()
    {
        return Calendar.MONTH;
    }

    public int getCALENDAR_DAY_FIELD()
    {
        return Calendar.DAY_OF_MONTH;
    }

    public int getCALENDAR_HOUR_FIELD()
    {
        return Calendar.HOUR_OF_DAY;
    }

    public Date addApproxOneHour(Date date)
    { 
        date.setTime(date.getTime() + 3599999);
        return date;
    }
}
