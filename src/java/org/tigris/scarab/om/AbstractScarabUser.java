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
 * software developed by CollabNet <http://www.collab.net/>."
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

import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.sql.Connection;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.BaseObject;

import org.apache.fulcrum.localization.Localization;

import org.tigris.scarab.reports.ReportBridge;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;

/**
 * This class contains common code for the use in ScarabUser implementations.
 * Functionality that is not implementation specific should go here.
 * 
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: AbstractScarabUser.java,v 1.100 2004/05/01 19:04:23 dabbous Exp $
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

    private static final String[] HOME_PAGES = {"home,EnterNew.vm", 
        "query", "Index.vm"};

    private static final int MAX_INDEPENDENT_WINDOWS = 10;

    /**
     * The user's preferred locale.
     */
    protected Locale locale = null;

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

    /**
     * The last entered issue type
     */
    private Map enterIssueMap;

    /**
     * toggle switch for show/hide the cross module section of the 
     * issue type selection widget.
     */
    private boolean showOtherModulesInIssueTypeList;    

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
        enterIssueMap = new HashMap();
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
    public abstract Integer getUserId();

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
     * Get modules user can copy or move to.
     * If copying, requires ISSUE_ENTER permission
     * If moving, requires ISSUE_MOVE permission to move 
     * To another module, or ISSUE_EDIT to move to another issue type.
     */
    public List getCopyToModules(Module currentModule, String action, 
                                 String searchString)
        throws Exception
    {
        List copyToModules = new ArrayList();
        if (hasPermission(ScarabSecurity.ISSUE__MOVE, currentModule) 
            || "copy".equals(action))
        {
            Module[] userModules = getModules(ScarabSecurity.ISSUE__ENTER);
            for (int i=0; i<userModules.length; i++)
            {
                Module module = userModules[i];
                if (!module.isGlobalModule() && 
                    (searchString == null || searchString.equals("") || 
                     module.getName().indexOf(searchString) != -1))
                {
                    copyToModules.add(module);
                }
            }
        }
        else if (hasPermission(ScarabSecurity.ISSUE__EDIT, currentModule)
                 && currentModule.getIssueTypes().size() > 1)
        {
            copyToModules.add(currentModule);
        }
        return copyToModules;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getCopyToModules(Module)
     */
    public List getCopyToModules(Module currentModule)
        throws Exception
    {
        return getCopyToModules(currentModule, "copy", null);
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getCopyToModules(Module, String)
     */
    public List getCopyToModules(Module currentModule, String action)
        throws Exception
    {
        return getCopyToModules(currentModule, action, null);
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
                throw new ScarabException(L10NKeySet.ExceptionMultipleJDMs);
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
            throw new ScarabException(L10NKeySet.ExceptionNullIssueForbidden);
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
            throw new ScarabException(L10NKeySet.ExceptionNullReportForbidden);
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
     * @see org.apache.torque.om.Persistent#save()
     * this implementation throws an UnsupportedOperationException.
     */
    public void save() throws Exception
    {
        throw new UnsupportedOperationException("Not implemented"); //EXCEPTION
    }

    /**
     * @see org.apache.torque.om.Persistent#save(String)
     * this implementation throws an UnsupportedOperationException.
     */
    public void save(String dbName) throws Exception
    {
        throw new UnsupportedOperationException("Not implemented"); //EXCEPTION
    }

    /**
     * @see org.apache.torque.om.Persistent#save(Connection)
     * this implementation throws an UnsupportedOperationException.
     */
    public void save(Connection dbCon) throws Exception
    {
        throw new UnsupportedOperationException("Not implemented"); //EXCEPTION
    }

    /**
     * Returns integer representing user preference for
     * Which screen to return to after entering an issue.
     * 1 = Enter New Issue. 2 = Assign Issue (default)
     * 3 = View Issue. 4 = Issue Types index.
     */
    public int getEnterIssueRedirect()
        throws TorqueException
    {
        if (enterIssueRedirect == 0)
        {
            UserPreference up = UserPreferenceManager.getInstance(getUserId());
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
        UserPreference up = UserPreferenceManager.getInstance(getUserId());
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
    {
        String homePage = null;
        try
        {
            // A user with no id won't have preferences.  The
            // anonymous user used during password expiration (or an
            // unsaved user) would exhibit this behavior.
            Integer uid = getUserId();
            if (uid != null)
            {
                UserPreference up = UserPreferenceManager.getInstance(uid);
                homePage = up.getHomePage();

                if ("query".equals(homePage)) 
                {
                    homePage = getQueryTarget();
                }
                // protect against removal of old screens
                else if (homePage != null && 
                    (homePage.endsWith("ModuleQuery.vm") ||
                     homePage.endsWith("XModuleList.vm"))) 
                {
                    homePage = getQueryTarget();
                }

                int i = 0;
                while (homePage == null || !isHomePageValid(homePage, module)) 
                {
                    homePage = HOME_PAGES[i++];
                    if ("query".equals(homePage)) 
                    {
                        homePage = getQueryTarget();
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.get().warn("Error determining user homepage", e);
        }
        return (homePage != null ? homePage : "Index.vm");
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
            result = false;
        }
        return result;
    }

    /**
     * @see ScarabUser#setHomePage(String)
     */
    public void setHomePage(String homePage)
        throws Exception
    {
        if ("ModuleNotReady.vm".equals(homePage)) 
        {
            throw new ScarabException(L10NKeySet.ExceptionForbiddenHomeModuleNotReady);
        }
        UserPreference up = UserPreferenceManager.getInstance(getUserId());
        up.setHomePage(homePage);
        up.save();
    }

    // TODO: make this persist
    private final Map queryTargetMap = new HashMap();
    /**
     * @see ScarabUser#getQueryTarget()
     */
    public String getQueryTarget()
    {
        MITList mitlist = getCurrentMITList();
        String target = null;
        if (mitlist == null)
        {
            target = "IssueTypeList.vm";
        }
        else if (mitlist.isSingleModuleIssueType())
        {
            try 
            {
                Integer issueTypeId = mitlist.getIssueType().getIssueTypeId();
                target = (String)queryTargetMap.get(issueTypeId);
            }
            catch (Exception e)
            {
                Log.get().warn("Could not determine query target.", e);
            }
            
            if (target == null) 
            {
                target = "Search.vm";
            }
        }
        else 
        {
            target = "AdvancedQuery.vm";
        }
        return target;
    }

    /**
     * @see ScarabUser#setSingleIssueTypeQueryTarget(IssueType, String)
     */
    public void setSingleIssueTypeQueryTarget(IssueType type, String target)
    {
        queryTargetMap.put(type.getIssueTypeId(), target);
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
        crit.add(MITListPeer.MODIFIABLE, true);
        crit.add(MITListPeer.ACTIVE, true);
        crit.add(MITListPeer.NAME, (Object)null, Criteria.NOT_EQUAL);
        crit.addAscendingOrderByColumn(MITListPeer.NAME);
        result = MITListPeer.doSelect(crit);

        return result;
    }


    /**
     * @see ScarabUser#hasAnySearchableRMITs().
     */
    public boolean hasAnySearchableRMITs()
        throws Exception    
    {
        boolean result = false;
        List moduleIds = getSearchableModuleIds();
        if (!moduleIds.isEmpty()) 
        {
            Criteria crit = new Criteria();
            crit.addIn(RModuleIssueTypePeer.MODULE_ID, moduleIds);
            result = (RModuleIssueTypePeer.count(crit) > 0);
        }
        return result;
    }

    private List getSearchableModuleIds()
        throws Exception    
    {
        Module[] userModules = getModules(ScarabSecurity.ISSUE__SEARCH);
        List moduleIds;
        if (userModules != null && (userModules.length > 1 ||
                userModules.length == 1 && !userModules[0].isGlobalModule())
           ) 
        {
            moduleIds = new ArrayList(userModules.length);
            for (int i=0; i<userModules.length; i++) 
            {
                Module module = userModules[i];
                if (!module.isGlobalModule()) 
                {
                    moduleIds.add(module.getModuleId()); 
                }                
            }
        }
        else 
        {
            moduleIds = Collections.EMPTY_LIST;
        }
        return moduleIds;
    }


    /**
     * @see ScarabUser#getUnusedRModuleIssueTypes(Module).
     */
    public List getUnusedRModuleIssueTypes(Module module)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleIssueTypePeer.MODULE_ID, module.getModuleId())
            .addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID, 
                     IssueTypePeer.ISSUE_TYPE_ID)
            .add(IssueTypePeer.PARENT_ID, 0)
            .add(IssueTypePeer.DELETED, false);
        addCurrentMITListExclusion(crit);
        return RModuleIssueTypePeer.doSelect(crit);
    }

    private void addCurrentMITListExclusion(Criteria crit)
        throws Exception    
    {
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
                        && item.getIssueType() != null) 
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
                            .append(item.getIssueType().getIssueTypeId())
                            .append(')');
                        addAnd = true;
                    }
                }   
                // the column name used here is arbitrary (within limits)
                crit.add(IssueTypePeer.ISSUE_TYPE_ID, 
                         (Object)sb.toString(), Criteria.CUSTOM);
            }
    }

    /**
     * @see ScarabUser#getSearchableRMITs(String, String, String, String, Module).
     * This list does not include
     * RModuleIssueTypes that are part of the current MITList.
     */
    public List getSearchableRMITs(String searchField, String searchString, 
                                   String sortColumn, String sortPolarity,
                                   Module skipModule)
        throws Exception    
    {
        List moduleIds = getSearchableModuleIds();
        if (skipModule != null) 
        {
            moduleIds.remove(skipModule.getModuleId());
        }
        
        List result;
        if (moduleIds.isEmpty()) 
        {
            result = Collections.EMPTY_LIST;
        }
        else 
        {
            Criteria crit = new Criteria();
            crit.addIn(RModuleIssueTypePeer.MODULE_ID, moduleIds);
            crit.addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID,
                         IssueTypePeer.ISSUE_TYPE_ID);
            crit.add(IssueTypePeer.PARENT_ID, 0);
            crit.add(IssueTypePeer.DELETED, false);
            addCurrentMITListExclusion(crit);

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
        enterIssueMap.remove(key);
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
            MITList list = getCurrentMITList(key);
            if (list != null) 
            {
                mostRecentQueryMITMap.put(key, list);                
                mostRecentQueryMap.put(key, queryString);
            }
            else 
            {
                Log.get().warn(
                    "Tried to set most recent query without any mitlist.");
            }
        }
    }


    public Object lastEnteredIssueTypeOrTemplate()
    {
        return lastEnteredIssueTypeOrTemplate(getGenThreadKey());
    }
    private Object lastEnteredIssueTypeOrTemplate(Object key)
    {
        Log.get().debug("Getting last entered type for key " + key);
        return enterIssueMap.get(key);
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#setLastEnteredIssueType(IssueType)
     */
    public void setLastEnteredIssueType(IssueType type)
    {
        setLastEnteredIssueTypeOrTemplate(type);
    }
    /**
     * @see org.tigris.scarab.om.ScarabUser#setLastEnteredTemplate(Issue)
     */
    public void setLastEnteredTemplate(Issue template)
    {
        setLastEnteredIssueTypeOrTemplate(template);
    }
    /**
     * set the template or issue type
     */
    private void setLastEnteredIssueTypeOrTemplate(Object obj)
    {
        if (obj != null) 
        {
            setLastEnteredIssueTypeOrTemplate(getGenThreadKey(), obj);
        }
        else if (getThreadKey() != null)
        {
            setLastEnteredIssueTypeOrTemplate(getThreadKey(), null);
        }
    }
    private void setLastEnteredIssueTypeOrTemplate(Object key, Object obj)
    {
        if (obj == null) 
        {
            enterIssueMap.remove(key);
        }
        else 
        {
            try
            {
                if (enterIssueMap.size() >= MAX_INDEPENDENT_WINDOWS) 
                {
                    // make sure lists are not being accumulated, set a 
                    // reasonable limit of MAX_INDEPENDENT_WINDOWS open lists
                    int intKey = Integer.parseInt(String.valueOf(key));
                    int count = 0;
                    for (int i=intKey-1; i>=0; i--) 
                    {
                        String testKey = String.valueOf(i);
                        if (lastEnteredIssueTypeOrTemplate(testKey) != null)
                        {
                            if (++count >= MAX_INDEPENDENT_WINDOWS) 
                            {
                                enterIssueMap.remove(testKey);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Log.get().error("Nonfatal error clearing entered issue types. "
                                + "This could be a memory leak.", e);
            }
            Log.get().debug("Set issue type for key " + key + " to " + obj);
            
            enterIssueMap.put(key, obj);
        }
    }


    private void setUsersMap(Map map, Map users)
        throws Exception
    {
        Object key = (users != null ? getGenThreadKey() : getThreadKey());
        if (key == null)
        {
            // With no hash key, this method won't work.
            return;
        }

        if (users != null && users.size() >= MAX_INDEPENDENT_WINDOWS)
        {
            try
            {
                // Set a reasonable limit on the number of open lists.
                int intKey = Integer.parseInt(String.valueOf(key));
                int count = 0;
                for (int i = intKey - 1; i >= 0; i--)
                {
                    String testKey = String.valueOf(i);
                    if (map.get(testKey) != null)
                    {
                        if (++count >= MAX_INDEPENDENT_WINDOWS)
                        {
                            users.remove(testKey);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                // FIXME: I18N
                Log.get().warn("Error possibly resulting in memory leak", e);
            }
        }

        map.put(key, users);
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getAssociatedUsersMap()
     */
    public Map getAssociatedUsersMap()
        throws Exception
    {
        return (Map) associatedUsersMap.get(getGenThreadKey());
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#setAssociatedUsersMap(Map)
     */
    public void setAssociatedUsersMap(Map associatedUsers)
        throws Exception
    {
        setUsersMap(associatedUsersMap, associatedUsers);
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getSelectedUsersMap()
     */
    public Map getSelectedUsersMap()
        throws Exception
    {
        return (Map) selectedUsersMap.get(getGenThreadKey());
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#setSelectedUsersMap(Map)
     */
    public void setSelectedUsersMap(Map selectedUsers)
        throws Exception
    {
        setUsersMap(selectedUsersMap, selectedUsers);
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
        return (IssueType)currentIssueType.get();
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

        // Delete current attribute selections for user
        for (Iterator currentAttributes = mitList.getSavedRMUAs().iterator();
               currentAttributes.hasNext();) 
        {
            deleteRModuleUserAttribute(
                (RModuleUserAttribute)currentAttributes.next());
        }

        int i = 1;
        for (Iterator iter = attributes.iterator(); iter.hasNext();) 
        {
            Attribute attribute = (Attribute)iter.next();
            RModuleUserAttribute rmua = 
                mitList.getNewRModuleUserAttribute(attribute);
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
            + "; MostRecentQueryMITMap=" + mostRecentQueryMITMap.size()
            + "; EnterIssueMap=" + enterIssueMap.size();
    }

    /**
     * Set the user's locale to a new value.
     */
    public void setLocale(Locale newLocale)
    {
        locale = newLocale;
    }

    /**
     * Gets the users default locale from the users preferences.
     */
    public Locale getLocale()
    {
        if (locale == null)
        {
            locale = getPreferredLocale();
        }
        return locale;
    }

    /**
     * get preferred Locale from user preferences
     * @return
     */
    public Locale getPreferredLocale()
    {
    	Locale result;
        try
        {
            UserPreference up =
                UserPreferenceManager.getInstance(getUserId());
            result = Localization.getLocale(up.getLocale());
        }
        catch (Exception e)
        {
            // I think it might be ok to return null from this method
            // but until that is investigated return the default in
            // event of error
            result = ScarabConstants.DEFAULT_LOCALE;
            Log.get().warn(
                "AbstractScarabUser.getLocale() could not "
                    + "retrieve locale for user id="
                    + getUserId()
                    + "; Error message: "
                    + e.getMessage());
        }
        return result;
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#isShowOtherModulesInIssueTypeList()
     */
    public boolean isShowOtherModulesInIssueTypeList()
    {
        return showOtherModulesInIssueTypeList;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#setShowOtherModulesInIssueTypeList(boolean)
     */
    public void setShowOtherModulesInIssueTypeList(
        boolean newShowOtherModulesInIssueTypeList)
    {
        this.showOtherModulesInIssueTypeList = 
            newShowOtherModulesInIssueTypeList;
    }
}
