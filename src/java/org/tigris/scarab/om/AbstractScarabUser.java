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

import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

import java.sql.Connection;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.BaseObject;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.reports.ReportBridge;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.util.Log;

/**
 * This class contains common code for the use in ScarabUser implementations.
 * Functionality that is not implementation specific should go here.
 * 
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: AbstractScarabUser.java,v 1.66 2003/03/22 18:35:50 jon Exp $
 */
public abstract class AbstractScarabUser 
    extends BaseObject 
{
    /** Method name used as part of a cache key */
    private static final String GET_R_MODULE_USERATTRIBUTES = 
        "getRModuleUserAttributes";
    /** Method name used as part of a cache key */
    private static final String GET_R_MODULE_USERATTRIBUTE = 
        "getRModuleUserAttribute";
    /** Method name used as part of a cache key */
    private static final String GET_DEFAULT_QUERY_USER = 
        "getDefaultQueryUser";

    private static final String[] homePageArray = {"home,EnterNew.vm", 
        "home,ModuleQuery.vm", "home,XModuleList.vm", "Index.vm"};

    private static final int MAX_INDEPENDENT_WINDOWS = 10;

    /** 
     * counter used as part of a key to store an Issue the user is 
     * currently entering 
     */
    private int issueCount = 0;

    /** 
     * Map to store <code>Issue</code>'s the user is  currently entering 
     */
    private Map issueMap;

    /** 
     * counter used as part of a key to store an ReportBridge the user is 
     * currently editing
     */
    private int reportCount = 0;

    /** 
     * Map to store <code>ReportBridge</code>'s the user is  currently entering 
     */
    private Map reportMap;

    /** 
     * Map to store the most recent query entered by the user
     */
    private Map mostRecentQueryMap;

    /** 
     * Map to store the MITList that may have went with the most recent query 
     * entered by the user
     */
    private Map mostRecentQueryMITMap;

    /** 
     * Map to store the working user list in Assign Issue.
     */
    private Map associatedUsersMap;

    /** 
     * Map to store the working user list in Advanced Query.
     */
    private Map selectedUsersMap;

    /** 
     * Code for user's preference on which screen to return to
     * After entering an issue
     */
    private int enterIssueRedirect = 0;

    /**
     * The list of MITListItems that will be searched in a 
     * X-project query.
     */
    private Map mitListMap;

    private Map activeKeys = new HashMap();
    private transient ThreadLocal threadKey = null;

    /** 
     * counter used as a key to keep concurrent activities by the same
     * user from overwriting each others state.  Might want to use something
     * better than a simple counter.
     */
    private int threadCount = 0;

    // we could create Maps for these and use the threadKey, but these
    // will be reset for each request, so we can keep it simple and use
    // a ThreadLocal for each.  Even though the threads may be pooled the
    // value will be set correctly when first needed in a request cycle.
    private transient ThreadLocal currentModule = null;
    private transient ThreadLocal currentIssueType = null;

    
    /**
     * Calls the superclass constructor to initialize this object.
     */
    public AbstractScarabUser()
    {
        super();
        issueMap = new HashMap();
        reportMap = new HashMap();
        mitListMap = new HashMap();
        mostRecentQueryMap = new HashMap();
        mostRecentQueryMITMap = new HashMap();
        associatedUsersMap = new HashMap();
        selectedUsersMap = new HashMap();
        initThreadLocals();
    }

    /**
     * Need to override readObject in order to initialize
     * the transient ThreadLocal objects which are not serializable.
     */
    private void readObject(java.io.ObjectInputStream in)
         throws java.io.IOException, ClassNotFoundException
    {
        try
        {
            in.defaultReadObject();
        }
        catch (java.io.NotActiveException e)
        {
        }
        initThreadLocals();
    }

    private void initThreadLocals()
    {
        currentIssueType = new ThreadLocal();
        currentModule = new ThreadLocal();
        threadKey = new ThreadLocal();
    }

    /** The Primary Key used to reference this user in storage */
    public abstract NumberKey getUserId();

    /**
     * @see org.tigris.scarab.om.ScarabUser#getEmail()
     */
    public abstract String getEmail();

    /**
     * @see org.tigris.scarab.om.ScarabUser#getFirstName()
     */
    public abstract String getFirstName();

    /**
     * @see org.tigris.scarab.om.ScarabUser#getLastName()
     */
    public abstract String getLastName();

    /**
     * @see org.tigris.scarab.om.ScarabUser#hasPermission(String, Module)
     */
    public abstract boolean hasPermission(String perm, Module module);

    /**
     * @see org.tigris.scarab.om.ScarabUser#hasPermission(String, List)
     */
    public boolean hasPermission(String perm, List modules)
    {
        boolean hasPerm = false;
        if (modules != null && !modules.isEmpty()) 
        {
            hasPerm = true;
            Iterator i = modules.iterator();
            while (i.hasNext() && hasPerm) 
            {
                hasPerm = hasPermission(perm, (Module)i.next());
            }
        }
        return hasPerm;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getName()
     * It will be the "FirstName LastName", if  both names have a value.
     */
    public String getName()
    {
        String first = getFirstName();
        String last = getLastName();
        int firstlength = 0;
        int lastlength = 0;
        if (first != null) 
        {
            firstlength = first.length();            
        }
        if (last != null) 
        {
            lastlength = last.length();
        }        
        StringBuffer sb = new StringBuffer(firstlength + lastlength + 1);
        if (firstlength > 0) 
        {
            sb.append(first);
            if (lastlength > 0) 
            {
                sb.append(' ');
            }
        }
        if (lastlength > 0) 
        {
            sb.append(last);
        }
        
        return sb.toString();
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules()
     */
    public abstract List getModules() throws Exception;

    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules(String)
     */
    public abstract Module[] getModules(String permission) throws Exception;

    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules(boolean)
     */
    public abstract List getModules(boolean showDeletedModules)
        throws Exception;

    /**
     * @see org.tigris.scarab.om.ScarabUser#getEditableModules()
     */
    public List getEditableModules()
        throws Exception
    {
        return getEditableModules(null);
    }

    /**
     * Get modules user can copy to.
     */
    public List getCopyToModules(Module currentModule)
        throws Exception
    {
        List copyToModules = new ArrayList();
        Module[] userModules = getModules(ScarabSecurity.ISSUE__ENTER);
        for (int i=0; i<userModules.length; i++)
        {
            Module module = userModules[i];
             if (!module.isGlobalModule())
             {
                 copyToModules.add(module);
             }
        }
        return copyToModules;
    }

    /**
     * Get modules user can move to.
     * If user has Move permission, can move to any module
     * If they have Edit permission, can move to another issue type.
     */
    public List getMoveToModules(Module currentModule)
        throws Exception
    {
        List moveToModules = new ArrayList();
        if (hasPermission(ScarabSecurity.ISSUE__MOVE, currentModule))
        {
            moveToModules = getCopyToModules(currentModule);
        }
        else if (hasPermission(ScarabSecurity.ISSUE__EDIT, currentModule))
        {
            moveToModules.add(currentModule);
        }
        return moveToModules;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getEditableModules(Module)
     */
    public List getEditableModules(Module currEditModule)
        throws Exception
    {
        List userModules = getModules(true);
        List editModules = new ArrayList();

        if (currEditModule != null)
        {
            editModules.add(currEditModule.getParent());
        }
        for (int i=0; i<userModules.size(); i++)
        {
            Module module = (Module)userModules.get(i);
            Module parent = module.getParent();

            if (!editModules.contains(module) && parent != currEditModule)
            {
                if (hasPermission(ScarabSecurity.MODULE__EDIT, module))
                {
                    editModules.add(module);
                }
            }
        }
        // we want to remove the module we are editing
        if (currEditModule != null && editModules.contains(currEditModule))
        {
            editModules.remove(currEditModule);
        }

        return editModules;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getRModuleUserAttributes(Module, IssueType)
     */
    public List getRModuleUserAttributes(Module module,
                                         IssueType issueType)
        throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_R_MODULE_USERATTRIBUTES, 
                                     module, issueType); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria()
                .add(RModuleUserAttributePeer.USER_ID, getUserId())
                .add(RModuleUserAttributePeer.MODULE_ID, module.getModuleId())
                .add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                     issueType.getIssueTypeId())
                .addAscendingOrderByColumn(
                    RModuleUserAttributePeer.PREFERRED_ORDER);
            
            result = getRModuleUserAttributes(crit);
            ScarabCache.put(result, this, GET_R_MODULE_USERATTRIBUTES,  
                            module, issueType);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    /**
     * Should return a list of <code>RModuleUserAttribute</code>'s that
     * meet the given criteria. 
     */
    protected abstract List getRModuleUserAttributes(Criteria crit)
        throws TorqueException;

    /**
     * @see org.tigris.scarab.om.ScarabUser#getRModuleUserAttribute(Module, Attribute, IssueType)
     */
    public RModuleUserAttribute getRModuleUserAttribute(Module module, 
                                                       Attribute attribute,
                                                       IssueType issueType)
        throws Exception
    {
        RModuleUserAttribute result = null;
        Object obj = ScarabCache.get(this, GET_R_MODULE_USERATTRIBUTE, 
                                     module, attribute, issueType); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria(4)
                .add(RModuleUserAttributePeer.USER_ID, getUserId())
                .add(RModuleUserAttributePeer.ATTRIBUTE_ID, 
                     attribute.getAttributeId())
                .add(RModuleUserAttributePeer.LIST_ID, null);
            if (module == null) 
            {
                crit.add(RModuleUserAttributePeer.MODULE_ID, null);
            }
            else 
            {
                crit.add(RModuleUserAttributePeer.MODULE_ID, 
                         module.getModuleId());
                
            }            
            if (issueType == null) 
            {
                crit.add(RModuleUserAttributePeer.ISSUE_TYPE_ID, null);
            }
            else 
            {
                crit.add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                         issueType.getIssueTypeId());                
            }
            
            List muas = RModuleUserAttributePeer.doSelect(crit);
            if (muas.size() == 1) 
            {
                result = (RModuleUserAttribute)muas.get(0);
            }
            else if (muas.isEmpty())
            {
                result = 
                    getNewRModuleUserAttribute(attribute, module, issueType);
            }
            else 
            {
                throw new ScarabException(
                "Not sure, but this should probably only return one - jdm");
            }
            ScarabCache.put(result, this, GET_R_MODULE_USERATTRIBUTE, 
                            module, attribute, issueType);
        }
        else 
        {
            result = (RModuleUserAttribute)obj;
        }
        return result;
    }
    
    protected RModuleUserAttribute getNewRModuleUserAttribute(
        Attribute attribute, Module module, IssueType issueType)
        throws Exception
    {
        RModuleUserAttribute result = RModuleUserAttributeManager.getInstance();
        result.setUserId(getUserId());
        result.setAttributeId(attribute.getAttributeId());
        if (module != null) 
        {
            result.setModuleId(module.getModuleId());
        }
        if (issueType != null) 
        {
            result.setIssueTypeId(issueType.getIssueTypeId());
        }
        return result;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getReportingIssue(String)
     */
    public Issue getReportingIssue(String key)
    {
        return (Issue)issueMap.get(key);
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#setReportingIssue(Issue)
     */
    public String setReportingIssue(Issue issue)
        throws ScarabException
    {
        String key = null;
        if (issue == null) 
        {
            throw new ScarabException("Null Issue is not allowed.");
        }
        else 
        {
            key = String.valueOf(issueCount++);
            setReportingIssue(key, issue);
        }
        return key;
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#setReportingIssue(String, Issue)
     */
    public void setReportingIssue(String key, Issue issue)
    {
        if (issue == null) 
        {
            issueMap.remove(key);
        }
        else 
        {
            try
            {
                if (issueMap.size() >= MAX_INDEPENDENT_WINDOWS) 
                {
                    // make sure issues are not being accumulated, set a 
                    // reasonable limit of 10 open new issues
                    int intKey = Integer.parseInt(key);
                    int count = 0;
                    for (int i=intKey-1; i>=0; i--) 
                    {
                        String testKey = String.valueOf(i);
                        if (getReportingIssue(testKey) != null) 
                        {
                            if (++count >= MAX_INDEPENDENT_WINDOWS) 
                            {
                                issueMap.remove(testKey);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Log.get().error("Nonfatal error clearing old issues.  "
                                + "This could be a memory leak.", e);
            }
            
            issueMap.put(key, issue);
        }
    }



    /**
     * @see org.tigris.scarab.om.ScarabUser#getCurrentReport(String)
     */
    public ReportBridge getCurrentReport(String key)
    {
        return (ReportBridge)reportMap.get(key);
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#setCurrentReport(ReportBridge)
     */
    public String setCurrentReport(ReportBridge report)
        throws ScarabException
    {
        String key = null;
        if (report == null) 
        {
            throw new ScarabException("Null Report is not allowed.");
        }
        else 
        {
            key = String.valueOf(reportCount++);
            setCurrentReport(key, report);
        }
        return key;
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#setCurrentReport(String, ReportBridge)
     */
    public void setCurrentReport(String key, ReportBridge report)
    {
        if (report == null) 
        {
            reportMap.remove(key);
        }
        else 
        {
            try
            {
                if (reportMap.size() >= MAX_INDEPENDENT_WINDOWS) 
                {
                    // make sure reports are not being accumulated, set a 
                    // reasonable limit of MAX_INDEPENDENT_WINDOWS open reports
                    int intKey = Integer.parseInt(key);
                    int count = 0;
                    for (int i=intKey-1; i>=0; i--) 
                    {
                        String testKey = String.valueOf(i);
                        if (getCurrentReport(testKey) != null) 
                        {
                            if (++count >= MAX_INDEPENDENT_WINDOWS) 
                            {
                                reportMap.remove(testKey);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Log.get().error("Nonfatal error clearing old reports.  "
                                + "This could be a memory leak.", e);
            }

            reportMap.put(String.valueOf(key), report);
        }
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#getDefaultQueryUser(Module, IssueType)
     */
    public RQueryUser getDefaultQueryUser(Module me, IssueType issueType)
        throws Exception
    {
        RQueryUser rqu = null;
        List result = null;
        Object obj = ScarabCache.get(this, GET_DEFAULT_QUERY_USER, 
                                     me, issueType); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.add(RQueryUserPeer.USER_ID, getUserId());
            crit.add(RQueryUserPeer.ISDEFAULT, 1);
            crit.addJoin(RQueryUserPeer.QUERY_ID,
                     QueryPeer.QUERY_ID);
            crit.add(QueryPeer.MODULE_ID, me.getModuleId());
            crit.add(QueryPeer.ISSUE_TYPE_ID, issueType.getIssueTypeId());
            result = RQueryUserPeer.doSelect(crit);
            ScarabCache.put(result, this, GET_DEFAULT_QUERY_USER,  
                            me, issueType);
        }
        else 
        {
            result = (List)obj;
        }
        if (result.size() > 0)
        {
            rqu = (RQueryUser)result.get(0);
        }
        else 
        {
            // could call getDefaultDefaultQuery here
        }
        
        return rqu;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getDefaultQuery(Module, IssueType)
     */
    public Query getDefaultQuery(Module me, IssueType issueType)
        throws Exception
    {
        Query query = null;
        RQueryUser rqu = getDefaultQueryUser(me, issueType);
        if (rqu != null)
        { 
            query = rqu.getQuery();
        }
        return query;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#resetDefaultQuery(Module, IssueType)
     */
    public void resetDefaultQuery(Module me, IssueType issueType)
        throws Exception
    {
        RQueryUser rqu = getDefaultQueryUser(me, issueType);
        if (rqu != null)
        { 
            rqu.setIsdefault(false);
            rqu.save();
        }
    }

    // commented out as not yet used.
    /**
     * If user has no default query set, gets a default default query.
    private String getDefaultDefaultQuery() throws Exception
    {
        StringBuffer buf = new StringBuffer("&searchcb=");
        buf.append(getEmail());
        return buf.toString();
    }
    */

    /**
     * @see org.apache.torque.om.Persistent#save()
     * this implementation throws an UnsupportedOperationException.
     */
    public void save() throws Exception
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @see org.apache.torque.om.Persistent#save(String)
     * this implementation throws an UnsupportedOperationException.
     */
    public void save(String dbName) throws Exception
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @see org.apache.torque.om.Persistent#save(Connection)
     * this implementation throws an UnsupportedOperationException.
     */
    public void save(Connection dbCon) throws Exception
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns integer representing user preference for
     * Which screen to return to after entering an issue.
     * 1 = Enter New Issue. 2 = Assign Issue (default)
     * 3 = View Issue. 4 = Issue Types index.
     */
    public int getEnterIssueRedirect()
        throws Exception
    {
        if (enterIssueRedirect == 0)
        {
            UserPreference up = UserPreference.getInstance(getUserId());
            if (up != null && up.getEnterIssueRedirect() != 0)
            {
                enterIssueRedirect = up.getEnterIssueRedirect();
            }
        } 
        return enterIssueRedirect;
    }
    

    /**
     * Sets integer representing user preference for
     * Which screen to return to after entering an issue.
     * 1 = Enter New Issue. 2 = Assign Issue (default)
     * 3 = View Issue. 4 = Issue Types index.
     */
    public void setEnterIssueRedirect(int templateCode)
        throws Exception
    {
        UserPreference up = getUserPreference();
        up.setEnterIssueRedirect(templateCode);
        up.save();
        enterIssueRedirect = templateCode;
    }

    /**
     * @see ScarabUser#getHomePage()
     */
    public String getHomePage()
        throws Exception
    {
        return getHomePage(getCurrentModule());
    }

    /**
     * @see ScarabUser#getHomePage(Module)
     */
    public String getHomePage(Module module)
        throws Exception
    {
        String homePage = null;
        UserPreference up = UserPreference.getInstance(getUserId());
        if (up != null)
        {
            homePage = up.getHomePage();
        }
        int i=0;
        while (homePage == null || !isHomePageValid(homePage, module)) 
        {
            try
            {
                homePage = homePageArray[i++];
            }
            catch (Exception e)
            {
                homePage = "Index.vm";
                Log.get().warn("Error determining user homepage.", e);
            }
        }
        
        return homePage;
    }

    /**
     * This method is used in getHomePage() and expects the homePage to 
     * be non-null.
     */
    private boolean isHomePageValid(String homePage, Module module)
    {
        boolean result = true;
        String perm = ScarabSecurity
            .getScreenPermission(homePage.replace(',','.'));
        if (perm != null && !hasPermission(perm, module)) 
        {
            result = false;;
        }
        return result;
    }

    
    /**
     * @see ScarabUser#setHomePage(String)
     */
    public void setHomePage(String homePage)
        throws Exception
    {
        UserPreference up = getUserPreference();
        up.setHomePage(homePage);
        up.save();
    }

    private UserPreference getUserPreference()
        throws Exception
    {
        UserPreference up = UserPreference.getInstance(getUserId());
        if (up == null)
        {
            up = UserPreference.getInstance();
            up.setUserId(getUserId());
            up.setPasswordExpire(null);
        }
        return up;
    }

    /**
     * Gets active, named lists
     *
     * @return a <code>List</code> value
     * @exception TorqueException if an error occurs
     */
    public List getMITLists()
        throws TorqueException    
    {
        List result = null;

        Criteria crit = new Criteria();
        crit.add(MITListPeer.ACTIVE, true);
        Criteria.Criterion userCrit = crit.getNewCriterion(
            MITListPeer.USER_ID, getUserId(), Criteria.EQUAL);
        userCrit.or(crit.getNewCriterion(
            MITListPeer.USER_ID, null, Criteria.EQUAL));
        crit.add(userCrit);
        crit.add(MITListPeer.LIST_ID, 4, Criteria.GREATER_THAN);
        crit.add(MITListPeer.ACTIVE, true);
        crit.add(MITListPeer.NAME, (Object)null, Criteria.NOT_EQUAL);
        crit.addAscendingOrderByColumn(MITListPeer.NAME);
        result = MITListPeer.doSelect(crit);

        return result;
    }


    /**
     * @see ScarabUser#getSearchableRMITs(String, String, String, String).
     * This list does not include
     * RModuleIssueTypes that are part of the current MITList.
     */
    public List getSearchableRMITs(String searchField, String searchString, 
                                   String sortColumn, String sortPolarity)
        throws Exception    
    {
        List result = null;
        Module[] userModules = getModules(ScarabSecurity.ISSUE__SEARCH);
        if (userModules != null && (userModules.length > 1 ||
                userModules.length == 1 && !userModules[0].isGlobalModule())
           ) 
        {
            List moduleIds = new ArrayList(userModules.length);
            for (int i=0; i<userModules.length; i++) 
            {
                Module module = userModules[i];
                if (!module.isGlobalModule()) 
                {
                    moduleIds.add(module.getModuleId()); 
                }                
            }
            Criteria crit = new Criteria();
            crit.addIn(RModuleIssueTypePeer.MODULE_ID, moduleIds);
            crit.addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID,
                         IssueTypePeer.ISSUE_TYPE_ID);
            crit.add(IssueTypePeer.PARENT_ID, 0);

            // do not include RMIT's related to current MITListItems.
            MITList mitList = getCurrentMITList(getGenThreadKey());            
            if (mitList != null && mitList.getMITListItems() != null
                && !mitList.getMITListItems().isEmpty()) 
            {
                boolean addAnd = false;
                StringBuffer sb = new StringBuffer();
                Iterator mitItems = 
                    mitList.getExpandedMITListItems().iterator();
                while (mitItems.hasNext()) 
                {
                    MITListItem item = (MITListItem)mitItems.next();
                    if (mitList.getModule(item) != null 
                        && mitList.getIssueType(item) != null) 
                    {                  
                        if (addAnd) 
                        {
                            sb.append(" AND ");
                        }
                        
                        sb.append(" NOT (")
                            .append(RModuleIssueTypePeer.MODULE_ID)
                            .append('=')
                            .append(mitList.getModule(item).getModuleId())
                            .append(" AND ")
                            .append(RModuleIssueTypePeer.ISSUE_TYPE_ID)
                            .append('=')
                            .append(mitList.getIssueType(item)
                                    .getIssueTypeId())
                            .append(')');
                        addAnd = true;
                    }
                }   
                // the column name used here is arbitrary (within limits)
                crit.add(IssueTypePeer.ISSUE_TYPE_ID, 
                         (Object)sb.toString(), Criteria.CUSTOM);
            }
            // we could add the filter criteria here, but this might
            // result in full table scans.  Even if the table scan turns out
            // to be more efficient, I think it is better to move this
            // into the middle/front tier.
            //addFilterCriteria(crit, searchField, searchString);
            //addSortCriteria(crit, sortColumn, sortPolarity);
            
            result = RModuleIssueTypePeer.doSelect(crit);
            filterRMITList(result, searchField, searchString);
            sortRMITList(result, sortColumn, sortPolarity);
        }
        else 
        {
            result = Collections.EMPTY_LIST;
        }
        
        return result;
    }

    /**
     * Filter on module or issue type name.
     */
    protected void filterRMITList(List rmits, 
                                  String searchField, String searchString)
        throws Exception
    {
        String moduleName = null;
        String issueTypeName = null;
        if ("issuetype".equals(searchField)) 
        {
            issueTypeName = searchString;
        }
        else 
        {
            moduleName = searchString;
        }
        
        if (moduleName != null && moduleName.length() > 0)
        {
            for (int i=rmits.size()-1; i>=0; i--)
            {
                String name = ((RModuleIssueType)rmits.get(i))
                    .getModule().getRealName();
                if (name == null || name.indexOf(moduleName) == -1)
                {
                    rmits.remove(i);
                }
            }
        }
        if (issueTypeName != null && issueTypeName.length() > 0)
        {
            for (int i=rmits.size()-1; i>=0; i--)
            {
                String name = ((RModuleIssueType)rmits.get(i))
                    .getDisplayName();
                if (name == null || name.indexOf(issueTypeName) == -1)
                {
                    rmits.remove(i);
                }
            }
        }
    }

    /**
     * Sort module or issue type name.
     */
    protected void sortRMITList(List rmits, 
                                final String sortColumn, String sortPolarity)
        throws Exception
    {
        final int polarity = ("desc".equals(sortPolarity)) ? -1 : 1;   
        Comparator c = new Comparator() 
        {
            public int compare(Object o1, Object o2) 
            {
                int i = 0;
                if (sortColumn != null && sortColumn.equals("issuetype"))
                {
                    i =  polarity * ((RModuleIssueType)o1).getDisplayName()
                         .compareTo(((RModuleIssueType)o2).getDisplayName());
                }
                else
                {
                    try
                    {
                        i =  polarity * 
                            ((RModuleIssueType)o1).getModule().getRealName()
                            .compareTo(((RModuleIssueType)o2).getModule()
                                       .getRealName());
                    }
                    catch (TorqueException e)
                    {
                        Log.get().error("Unable to sort on module names", e);
                    }
                }
                return i;
             }
        };
        Collections.sort(rmits, c);
    }


    public void addRMITsToCurrentMITList(List rmits)
        throws TorqueException
    {
        if (rmits != null && !rmits.isEmpty()) 
        {
            MITList mitList = getCurrentMITList(getGenThreadKey());
            if (mitList == null) 
            {                
                mitList = MITListManager.getInstance();
                Log.get().debug("mitList was null, setting to a new mitList " + mitList);
                setCurrentMITList(mitList);
            }

            Iterator i = rmits.iterator();
            while (i.hasNext()) 
            {
                RModuleIssueType rmit = (RModuleIssueType)i.next();
                MITListItem item = MITListItemManager.getInstance();
                item.setModuleId(rmit.getModuleId());
                item.setIssueTypeId(rmit.getIssueTypeId());
                if (!mitList.contains(item)) 
                {
                    mitList.addMITListItem(item);        
                }
            }
        }
    }

    private Object getGenThreadKey()
    {
        Object key = threadKey.get();
        if (key == null) 
        {
            key = getNewThreadKey();
            setThreadKey((Integer)key);
        }
        return key;
    }

    private synchronized Object getNewThreadKey()
    {
        // this algorithm is not very good.  what happens if someone bookmarks
        // a deep link which includes a thread key
        Integer key = new Integer(threadCount++);
        activeKeys.put(key, null);
        // make sure user is not using up too many resources, set a 
        // reasonable limit of 10 open "threads"/browser windows.
        Integer testKey = new Integer(key.intValue()-10);
        invalidateKey(testKey);
        return key;
    }

    private void invalidateKey(Object key)
    {
        activeKeys.remove(key);
        mitListMap.remove(key);
    }

    /**
     * @see ScarabUser#getThreadKey()
     */
    public Object getThreadKey()
    {
        return threadKey.get();
    }

    /**
     * @see ScarabUser#setThreadKey(Integer)
     */
    public void setThreadKey(Integer key)
    {  
        if (activeKeys.containsKey(key)) 
        {
            threadKey.set(key);
        }        
    }

    public MITList getCurrentMITList()
    {
        return getCurrentMITList(getGenThreadKey());
    }
    private MITList getCurrentMITList(Object key)
    {
        Log.get().debug("Getting mitlist for key " + key);
        MITList mitList = (MITList)mitListMap.get(key);
        return (MITList)mitListMap.get(key);
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#setCurrentMITList(MITList)
     */
    public void setCurrentMITList(MITList list)
    {
        if (list != null) 
        {
            setCurrentMITList(getGenThreadKey(), list);            
        }
        else if (getThreadKey() != null)
        {
            setCurrentMITList(getThreadKey(), list);            
        }
    }
    private void setCurrentMITList(Object key, MITList list)
    {
        if (list == null) 
        {
            mitListMap.remove(key);
        }
        else 
        {
            try
            {
                if (mitListMap.size() >= MAX_INDEPENDENT_WINDOWS) 
                {
                    // make sure lists are not being accumulated, set a 
                    // reasonable limit of MAX_INDEPENDENT_WINDOWS open lists
                    int intKey = Integer.parseInt(String.valueOf(key));
                    int count = 0;
                    for (int i=intKey-1; i>=0; i--) 
                    {
                        String testKey = String.valueOf(i);
                        if (getCurrentMITList(testKey) != null) 
                        {
                            if (++count >= MAX_INDEPENDENT_WINDOWS) 
                            {
                                mitListMap.remove(testKey);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Log.get().error("Nonfatal error clearing old MIT lists.  "
                                + "This could be a memory leak.", e);
            }
            Log.get().debug("Set mitList for key " + key + " to " + list);
            
            mitListMap.put(key, list);
        }
    }

    public void removeItemsFromCurrentMITList(String[] ids)
    {
        MITList mitList = getCurrentMITList(getGenThreadKey());
        if (mitList != null && !mitList.isEmpty() 
            && ids != null && ids.length > 0) 
        {
            for (int i=0; i<ids.length; i++) 
            {
                Iterator iter = mitList.iterator();
                while (iter.hasNext()) 
                {
                    MITListItem item = (MITListItem)iter.next();
                    if (item.getQueryKey().equals(ids[i])) 
                    {
                        iter.remove();
                        mitList.scheduleItemForDeletion(item);
                        continue;
                    }
                }
                
            }
        }
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#hasMostRecentQuery()
     */
    public boolean hasMostRecentQuery()
    {
        return hasMostRecentQuery(getGenThreadKey());
    }
    private boolean hasMostRecentQuery(Object key)
    {
        return mostRecentQueryMap.get(key) != null;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getMostRecentQuery()
     */
    public String getMostRecentQuery()
    {
        return getMostRecentQuery(getGenThreadKey());
    }
    private String getMostRecentQuery(Object key)
    {
        setCurrentMITList(key, (MITList)mostRecentQueryMITMap.get(key));
        return (String)mostRecentQueryMap.get(key);
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#setMostRecentQuery(String)
     */
    public void setMostRecentQuery(String queryString)
    {
        if (queryString != null) 
        {
            setMostRecentQuery(getGenThreadKey(), queryString);            
        }
        else if (getThreadKey() != null)
        {
            setMostRecentQuery(getThreadKey(), null);
        }
    }
    private void setMostRecentQuery(Object key, String queryString)
    {
        if (queryString == null) 
        {
            mostRecentQueryMap.remove(key);
            mostRecentQueryMITMap.remove(key);
        }
        else 
        {
            try
            {
                if (mostRecentQueryMap.size() >= MAX_INDEPENDENT_WINDOWS) 
                {
                    // make sure lists are not being accumulated, set a 
                    // reasonable limit of MAX_INDEPENDENT_WINDOWS open lists
                    int intKey = Integer.parseInt(String.valueOf(key));
                    int count = 0;
                    for (int i=intKey-1; i>=0; i--) 
                    {
                        String testKey = String.valueOf(i);
                        if (getMostRecentQuery(testKey) != null) 
                        {
                            if (++count >= MAX_INDEPENDENT_WINDOWS) 
                            {
                                mostRecentQueryMap.remove(testKey);
                                mostRecentQueryMITMap.remove(testKey);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Log.get().error("Nonfatal error clearing old queries.  "
                                + "This could be a memory leak.", e);
            }
            mostRecentQueryMap.put(key, queryString);
            MITList list = getCurrentMITList(key);
            /*
            FIXME! currently searches that occur on the current issue type
            do not save the issue type that they used along with the query
            this means that if first, a query is run against defects that 
            returns a list of defects, then the current issue type is changed
            to patches, finally followed by executing the 'Most recent' query,
            it will return patches (if anything).  The code below is my (jdm)
            attempt to quickly fix this but had unforeseen consequences.
            Need to think about it some more.

            if (list == null) 
            {
                try 
                {
                    list = MITListManager.getSingleItemList(getCurrentModule(),
                         getCurrentIssueType(), null);
                }
                catch (Exception e)
                {
                    Log.get().warn(
                        "Error setting module/issuetype for most recent query",
                        e);
                }
            }
            */            
            mostRecentQueryMITMap.put(key, list);
        }
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getAssociatedUsersMap()
     */
    public Map getAssociatedUsersMap()
        throws Exception
    {
        return getAssociatedUsersMap(getGenThreadKey());
    }
    private Map getAssociatedUsersMap(Object key)
        throws Exception
    {
        return (Map)associatedUsersMap.get(key);
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#setAssociatedUsersMap(Map)
     */
    public void setAssociatedUsersMap(Map associatedUsers)
        throws Exception
    {
        if (associatedUsers != null) 
        {
            setAssociatedUsersMap(getGenThreadKey(), associatedUsers);            
        }
        else if (getThreadKey() != null)
        {
            setAssociatedUsersMap(getThreadKey(), associatedUsers);
        }
    }

    private void setAssociatedUsersMap(Object key, Map associatedUsers)
        throws Exception
    {
        try
        {
            if (associatedUsers.size() >= MAX_INDEPENDENT_WINDOWS) 
            {
                // make sure lists are not being accumulated, set a 
                // reasonable limit of MAX_INDEPENDENT_WINDOWS open lists
                int intKey = Integer.parseInt(String.valueOf(key));
                int count = 0;
                for (int i=intKey-1; i>=0; i--) 
                {
                    String testKey = String.valueOf(i);
                    if (getAssociatedUsersMap(testKey) != null) 
                    {
                        if (++count >= MAX_INDEPENDENT_WINDOWS) 
                        {
                            associatedUsers.remove(testKey);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.get().error("Nonfatal error clearing old queries.  "
                            + "This could be a memory leak.", e);
        }
        associatedUsersMap.put(key, associatedUsers);
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getSelectedUsersMap()
     */
    public Map getSelectedUsersMap()
        throws Exception
    {
        return getSelectedUsersMap(getGenThreadKey());
    }
    private Map getSelectedUsersMap(Object key)
        throws Exception
    {
        Map selectedUsers = null;
        if (selectedUsersMap != null && selectedUsersMap.get(key) != null)
        {
            selectedUsers = (Map)selectedUsersMap.get(key);
        }
        return selectedUsers;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#setAssociatedUsersMap(Map)
     */
    public void setSelectedUsersMap(Map selectedUsers)
        throws Exception
    {
        if (getThreadKey() != null)
        {
            setSelectedUsersMap(getThreadKey(), selectedUsers);
        }
        else
        {
            setSelectedUsersMap(getGenThreadKey(), selectedUsers);            
        }
    }
    private void setSelectedUsersMap(Object key, Map selectedUsers)
        throws Exception
    {
        try
        {
            if (selectedUsers.size() >= MAX_INDEPENDENT_WINDOWS) 
            {
                // make sure lists are not being accumulated, set a 
                // reasonable limit of MAX_INDEPENDENT_WINDOWS open lists
                int intKey = Integer.parseInt(String.valueOf(key));
                int count = 0;
                for (int i=intKey-1; i>=0; i--) 
                {
                    String testKey = String.valueOf(i);
                    if (getSelectedUsersMap(testKey) != null) 
                    {
                        if (++count >= MAX_INDEPENDENT_WINDOWS) 
                        {
                            selectedUsers.remove(testKey);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.get().error("Nonfatal error clearing old queries.  "
                            + "This could be a memory leak.", e);
        }
        selectedUsersMap.put(key, selectedUsers);
    }

    /**
     * The current module
     */
    public Module getCurrentModule() 
    {
        return (Module)currentModule.get();
    }
    
    /**
     * The current module
     */
    public void setCurrentModule(Module  v) 
    {
        this.currentModule.set(v);
    }
     
    /**
     * The current issue type
     */
    public IssueType getCurrentIssueType()
        throws Exception
    {
        IssueType issueType = (IssueType)currentIssueType.get();
        if (issueType == null && getCurrentModule() != null)
        {
            Module currentModule = getCurrentModule();
            List navIssueTypes = currentModule.getNavIssueTypes();
            if (navIssueTypes.size() > 0)
            {
                issueType = (IssueType)navIssueTypes.get(0);
            }
            else 
            {
                List activeIssueTypes = currentModule.getIssueTypes(true);
                if (activeIssueTypes.size() > 0)
                {
                    issueType = (IssueType)activeIssueTypes.get(0);
                }
            }
            setCurrentIssueType(issueType);
        }
        
        return issueType;
    }
    
    /**
     * The current issue type
     */
    public void setCurrentIssueType(IssueType  v) 
    {
        this.currentIssueType.set(v);
    }    
    
    /**
     * @see ScarabUser#getCurrentRModuleIssueType()
     */
    public RModuleIssueType getCurrentRModuleIssueType()
        throws Exception
    {
        RModuleIssueType rmit = null;
        Module module = getCurrentModule();
        if (module != null) 
        {
            IssueType it = getCurrentIssueType();
            if (it != null) 
            {
                rmit = module.getRModuleIssueType(it);
            }
        }
        
        return rmit;
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#updateIssueListAttributes(List)
     */
    public void updateIssueListAttributes(List attributes)
        throws Exception
    {
        MITList mitList = getCurrentMITList();
        Module module = null;
        IssueType issueType = null;

        // Delete current attribute selections for user
        Iterator currentAttributes = null;
        if (mitList == null) 
        {
            Criteria crit = new Criteria();
            crit.add(RModuleUserAttributePeer.USER_ID, getUserId());
            issueType = getCurrentIssueType();
            module = getCurrentModule();
            crit.add(RModuleUserAttributePeer.MODULE_ID, module.getModuleId());
            crit.add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                     issueType.getIssueTypeId());
            currentAttributes = RModuleUserAttributePeer.doSelect(crit)
                .iterator();
        }
        else 
        {
            currentAttributes = mitList.getSavedRMUAs().iterator();
        }

        while (currentAttributes.hasNext()) 
        {
            deleteRModuleUserAttribute(
                (RModuleUserAttribute)currentAttributes.next());
        }

        Iterator iter = attributes.iterator();
        int i = 1;
        while (iter.hasNext()) 
        {
            Attribute attribute = (Attribute)iter.next();
            RModuleUserAttribute rmua = null;
            if (mitList != null)
            {
                rmua = mitList.getNewRModuleUserAttribute(attribute);
            }
            else 
            {
                rmua = getNewRModuleUserAttribute(attribute, module, issueType);
            }
            rmua.setOrder(i++);
            rmua.save();
        }
    }

    protected abstract void 
        deleteRModuleUserAttribute(RModuleUserAttribute rmua)
        throws Exception;


    /**
     * Report the sizes of maps used to hold per-thread attributes
     */
    public String getStats()
    {
        return " IssueMap=" + issueMap.size()
            + "; ReportMap=" + reportMap.size()
            + "; MITListMap=" + mitListMap.size()
            + "; MostRecentQueryMap=" + mostRecentQueryMap.size()
            + "; MostRecentQueryMITMap=" + mostRecentQueryMITMap.size();
    }
}
