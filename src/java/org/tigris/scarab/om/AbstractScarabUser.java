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

import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.impl.db.entity.TurbineUserGroupRolePeer;
import java.sql.Connection;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.BaseObject;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.commons.util.GenerateUniqueId;

import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * This class contains common code for the use in ScarabUser implementations.
 * Functionality that is not implementation specific should go here.
 * 
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: AbstractScarabUser.java,v 1.38 2002/07/12 01:11:06 elicia Exp $
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
     * counter used as part of a key to store an Report the user is 
     * currently editing
     */
    private int reportCount = 0;

    /** 
     * Map to store <code>Report</code>'s the user is  currently entering 
     */
    private Map reportMap;

    /** 
     * Code for user's preference on which screen to return to
     * After entering an issue
     */
    private int enterIssueRedirect = 0;

    /**
     * The template/tab to show for the home page.
     */
    private String homePage;

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
        if (firstlength > 0 ) 
        {
            sb.append(first);
            if (lastlength > 0) 
            {
                sb.append(' ');
            }
        }
        if ( lastlength > 0) 
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
     * @see org.tigris.scarab.om.ScarabUser#getEditableModules(Module)
     */
    public List getCopyToModules(Module currentModule)
        throws Exception
    {
        List copyToModules = new ArrayList();
        Module[] userModules = getModules(ScarabSecurity.ISSUE__ENTER);
        for (int i=0; i<userModules.length; i++)
        {
             Module module = (Module)userModules[i];
             if (!module.getModuleId().toString().equals("0"))
             {
                 copyToModules.add(module);
             }
         }
         return copyToModules;
    }

    public List getMoveToModules(Module currentModule)
        throws Exception
    {
        List copyToModules = new ArrayList();
        Module[] userModules = getModules(ScarabSecurity.ISSUE__ENTER);
        for (int i=0; i<userModules.length; i++)
        {
             Module module = (Module)userModules[i];
             if (!module.isGlobalModule()
                 && !module.getModuleId().toString().equals("0"))    
             if (!module.getModuleId().toString().equals("0"))
             {
                 copyToModules.add(module);
             }
         }
         return copyToModules;
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

//System.out.println ("Module: " + module.getModuleId() + ": " + module.getName());
            if (!editModules.contains(module) && parent != currEditModule)
            {
                if (hasPermission(ScarabSecurity.MODULE__EDIT, module))
                {
//System.out.println ("Added Module: " + module.getModuleId() + ": " + module.getName());
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
        if ( obj == null ) 
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
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria(4)
                .add(RModuleUserAttributePeer.MODULE_ID, module.getModuleId())
                .add(RModuleUserAttributePeer.USER_ID, getUserId())
                .add(RModuleUserAttributePeer.ATTRIBUTE_ID, 
                     attribute.getAttributeId())
                .add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                     issueType.getIssueTypeId());
            List muas = RModuleUserAttributePeer.doSelect(crit);
            if ( muas.size() == 1 ) 
            {
                result = (RModuleUserAttribute)muas.get(0);
            }
            else if ( muas.size() == 0 )
            {
                result = new RModuleUserAttribute();
                result.setModuleId(module.getModuleId());
                result.setUserId(getUserId());
                result.setIssueTypeId(issueType.getIssueTypeId());
                result.setAttributeId(attribute.getAttributeId());
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

    /**
     * @see org.tigris.scarab.om.ScarabUser#getReportingIssue(String)
     */
    public Issue getReportingIssue(String key)
        throws Exception
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
        if ( issue == null ) 
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
        if ( issue == null ) 
        {
            issueMap.remove(String.valueOf(key));
        }
        else 
        {
            issueMap.put(String.valueOf(key), issue);
        }
    }



    /**
     * @see org.tigris.scarab.om.ScarabUser#getCurrentReport(String)
     */
    public Report getCurrentReport(String key)
    {
        return (Report)reportMap.get(key);
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#setCurrentReport(Report)
     */
    public String setCurrentReport(Report report)
        throws ScarabException
    {
        String key = null;
        if ( report == null ) 
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
     * @see org.tigris.scarab.om.ScarabUser#setCurrentReport(String, Report)
     */
    public void setCurrentReport(String key, Report report)
    {
        if ( report == null ) 
        {
            reportMap.remove(key);
        }
        else 
        {
            // make sure reports are not being accumulated, set a reasonable
            // limit of 10 open reports
            int intKey = Integer.parseInt(key);
            int count = 0;
            for (int i=intKey-1; i>=0; i--) 
            {
                String testKey = String.valueOf(i);
                if (getCurrentReport(testKey) != null) 
                {
                    if (++count > 10) 
                    {
                        reportMap.remove(testKey);
                    }
                }
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
        if ( obj == null ) 
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
            query = (Query)rqu.getQuery();
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

    /**
     * If user has no default query set, gets a default default query.
     */
    private String getDefaultDefaultQuery() throws Exception
    {
        StringBuffer buf = new StringBuffer("&searchcb=");
        buf.append(getEmail());
        return buf.toString();
    }

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
        if (homePage == null)
        {
            UserPreference up = UserPreference.getInstance(getUserId());
            if (up != null)
            {
                homePage = up.getHomePage();
                if (homePage != null) 
                {
                    checkHomePage(module);
                }
            }
            for (int i=0; homePage == null; i++) 
            {
                homePage = homePageArray[i];
                checkHomePage(module);
            }
        } 
        return homePage;
    }


    /**
     * This method is used in getHomePage() and expects the homePage to 
     * be non-null.
     */
    private void checkHomePage(Module module)
    {
        String perm = ScarabSecurity
            .getScreenPermission(homePage.replace(',','.'));
        if (perm != null && !hasPermission(perm, module)) 
        {
            homePage = null;
        }
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
        this.homePage = homePage;
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
        result = MITListPeer.doSelect(crit);

        return result;
    }

    /**
     * @see ScarabUser#getSearchableRMITs().  This list does not include
     * RModuleIssueTypes that are part of the current MITList.
     */
    public List getSearchableRMITs()
        throws Exception    
    {
        List result = null;
        Module[] userModules = getModules(ScarabSecurity.ISSUE__SEARCH);
        if (userModules != null && userModules.length > 0) 
        {
            List moduleIds = new ArrayList(userModules.length);
            for (int i=0; i<userModules.length; i++) 
            {
                moduleIds.add(userModules[i].getModuleId());
            }
            Criteria crit = new Criteria();
            crit.addIn(RModuleIssueTypePeer.MODULE_ID, moduleIds);
            crit.addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID,
                         IssueTypePeer.ISSUE_TYPE_ID);
            crit.add(IssueTypePeer.PARENT_ID, 0);
            crit.addAscendingOrderByColumn(RModuleIssueTypePeer.MODULE_ID);

            // do not include RMIT's related to current MITListItems.
            MITList mitList = getCurrentMITList(getGenThreadKey());            
            if (mitList != null && mitList.getMITListItems() != null) 
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
            
            result = RModuleIssueTypePeer.doSelect(crit);
        }
        else 
        {
            result = Collections.EMPTY_LIST;
        }
        
        return result;
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
                mitList.addMITListItem(item);
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
        if ( list == null ) 
        {
            mitListMap.remove(key);
        }
        else 
        {
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
                        continue;
                    }
                }
                
            }
        }
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
}
