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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;
import java.util.Collections;

import org.apache.fulcrum.localization.Localization;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.impl.db.entity.TurbinePermissionPeer;
import org.apache.fulcrum.security.impl.db.entity.TurbineRolePermissionPeer;
import org.apache.fulcrum.security.impl.db.entity.TurbineRolePeer;
import org.apache.fulcrum.security.impl.db.entity.TurbineUserGroupRolePeer;
import org.apache.fulcrum.security.util.AccessControlList;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.commons.util.GenerateUniqueId;

import org.tigris.scarab.reports.ReportBridge;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.util.ScarabException;

import org.apache.turbine.Turbine;
import org.apache.log4j.Logger;


/**
 * This class is an abstraction that is currently based around
 * Turbine's code. We can change this later. It is here so
 * that it is easier to change later to work under different
 * implementation needs.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ScarabUserImpl.java,v 1.98 2003/04/09 18:04:22 elicia Exp $
 */
public class ScarabUserImpl 
    extends BaseScarabUserImpl 
    implements ScarabUser
{
    private static final Logger TORQUE_LOG = 
        Logger.getLogger("org.apache.torque");

    public static final String PASSWORD_EXPIRE = "PASSWORD_EXPIRE";
    
    private AbstractScarabUser internalUser;
    
    /**
     * The maximum length for the unique identifier used at user
     * creation time.
     */
    private static final int UNIQUE_ID_MAX_LEN = 10;

    private Locale locale = null;

    /**
     * Call the superclass constructor to initialize this object.
     */
    public ScarabUserImpl()
    {
        super();
        
        /*
         * Functionality that would be useful in any implementation of
         * ScarabUser is available in AbstractScarabUser (ASU).  This 
         * implementation must extend from TurbineUser, so TurbineUser 
         * would need to extend ASU to gain the functionality through
         * inheritance.  This is possible with some modifications to 
         * fulcrum's build process.  But until changes to fulcrum allow it,
         * we will wrap a instance of ASU.
         */
        internalUser = new AbstractScarabUser()
        {
            public Integer getUserId()
            {
                return getPrivateUserId();
            }
            
            public String getEmail()
            {
                return getPrivateEmail();
            }
            
            public String getFirstName()
            {
                return getPrivateFirstName();
            }
            
            public String getLastName()
            {
                return getPrivateLastName();
            }

            protected List getRModuleUserAttributes(Criteria crit)
                throws TorqueException
            {
                return getPrivateRModuleUserAttributes(crit);
            }
            
            public boolean hasPermission(String perm, Module module)
            {
                return hasPrivatePermission(perm, module);
            }
            
            public List getModules() 
                throws Exception
            {
                return getModules(false);
            }
            
            public List getModules(boolean showDeletedModules) 
                throws Exception
            {
                List permList = ScarabSecurity.getAllPermissions();
                String[] perms = new String[permList.size()];
                perms = (String[])permList.toArray(perms);
                
                Module[] modules = getPrivateModules(perms, showDeletedModules);
                return (modules == null || modules.length == 0
                        ? Collections.EMPTY_LIST : Arrays.asList(modules));
            }

            /**
             * @see org.tigris.scarab.om.ScarabUser#getModules(String)
             */
            public Module[] getModules(String permission)
            {
                return getPrivateModules(permission);
            }

            protected void 
                deleteRModuleUserAttribute(RModuleUserAttribute rmua)
                throws Exception
            {
                privateDeleteRModuleUserAttribute(rmua);
            }    
        };
    }
    
    // the following getPrivateFoo methods are to avoid naming conflicts when
    // supplying implementations of the methods needed by AbstractScarabUser
    // when instantiated in the constructor
    private Integer getPrivateUserId()
    {
        return getUserId();
    }
    private String getPrivateEmail()
    {
        return getEmail();
    }
    private String getPrivateFirstName()
    {
        return getFirstName();
    }
    private String getPrivateLastName()
    {
        return getLastName();
    }
    public String getName()
    {
        return internalUser.getName();
    }
    private List getPrivateRModuleUserAttributes(Criteria crit)
        throws TorqueException
    {
        return getRModuleUserAttributes(crit);
    }
    private boolean hasPrivatePermission(String perm, Module module)
    {
        return hasPermission(perm, module);
    }
    private Module[] getPrivateModules(String permission)
    {        
        String[] perms = {permission};
        return getModules(perms);
    }
    private Module[] getPrivateModules(String[] permissions, boolean showDeletedModules)
    {        
        return getModules(permissions, showDeletedModules);
    }

    private void privateDeleteRModuleUserAttribute(RModuleUserAttribute rmua)
        throws Exception
    {
        rmua.delete(this);
    }

    /**
     *   Utility method that takes a username and a confirmation code
     *   and will return true if there is a match and false if no match.
     *   <p>
     *   If there is an Exception, it will also return false.
     */
    public static boolean checkConfirmationCode (String username, 
                                                 String confirm)
    {
        // security check. :-)
        if (confirm.equalsIgnoreCase(User.CONFIRM_DATA))
        {
            return false;
        }
        
        try
        {
            Criteria criteria = new Criteria();
            criteria.add (ScarabUserImplPeer.getColumnName(User.USERNAME), 
                          username);
            criteria.add (ScarabUserImplPeer.getColumnName(User.CONFIRM_VALUE),
                          confirm);
            criteria.setSingleRecord(true);
            List result = ScarabUserImplPeer.doSelect(criteria);
            if (result.size() > 0)
            {
                return true;
            }

            // FIXME: once i figure out how to build an OR in a Criteria i 
            // won't need this.
            // We check to see if the user is already confirmed because that
            // should result in a True as well.
            criteria = new Criteria();
            criteria.add (ScarabUserImplPeer.getColumnName(User.USERNAME), 
                          username);
            criteria.add (ScarabUserImplPeer.getColumnName(User.CONFIRM_VALUE),
                          User.CONFIRM_DATA);
            criteria.setSingleRecord(true);
            result = ScarabUserImplPeer.doSelect(criteria);
            return (result.size() > 0);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     This method will mark username as confirmed.
     returns true on success and false on any error
     */
    public static boolean confirmUser (String username)
    {
        try
        {
            User user = TurbineSecurity.getUser(username);
            user.setConfirmed(User.CONFIRM_DATA);
            TurbineSecurity.saveUser(user);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#hasPermission(String, Module)
     * Determine if a user has a permission, either within the specified
     * module or within the 'Global' module.
     */
    public boolean hasPermission(String perm, Module module)
    {
        boolean hasPermission = false;
        
        if (TORQUE_LOG.isDebugEnabled()) 
        {
            String name = (module == null) ? null : module.getName();
            TORQUE_LOG.debug("ScarabUserImpl.hasPermission(" + perm + ", " + 
                            name + ") started");
        }
        
        // Cache permission check results internally, so that we do not have
        // to ask the acl everytime.  FIXME!  This mechanism needs to be
        // modified to allow for invalidating the cached results.  Possible
        // candidates are the TurbineGlobalCacheService or JCS.  But keeping
        // this in place for the moment while investigating other sql, so
        // turbine's security sql does not dominate.
        String moduleKey = (module == null) ? null : module.getQueryKey();
        Object obj = getTemp("hasPermission" + perm + moduleKey);
        if (obj == null) 
        {        
        try
        {
            AccessControlList acl = TurbineSecurity.getACL(this);
            if (acl != null) 
            {
                if (module != null)
                {
                    // first check for the permission in the specified module
                    hasPermission = acl.hasPermission(perm, (Group)module);
                }
                
                if (!hasPermission)
                {
                    // check for the permission within the 'Global' module
                    Module globalModule = ModuleManager
                        .getInstance(Module.ROOT_ID);
                    hasPermission = acl.hasPermission(perm, 
                                                      (Group)globalModule);
                }
            }
        }
        catch (Exception e)
        {
            hasPermission = false;
            log().error("Permission check failed on:" + perm, e);
        }
        
        Boolean b = hasPermission ? Boolean.TRUE : Boolean.FALSE;
        setTemp("hasPermission" + perm + moduleKey, b);
        }
        else 
        {
            hasPermission = ((Boolean)obj).booleanValue();
        }
        
        if (TORQUE_LOG.isDebugEnabled()) 
        {
            String name = (module == null) ? null : module.getName();
            TORQUE_LOG.debug("ScarabUserImpl.hasPermission(" + perm + ", " + 
                            name + ") end\n");
        }
        return hasPermission;
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#hasPermission(String, List)
     */
    public boolean hasPermission(String perm, List modules)
    {
        return internalUser.hasPermission(perm, modules);
    }
        
    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules()
     */
    public List getModules() throws Exception
    {
        return internalUser.getModules();
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules(boolean)
     */
    public List getModules(boolean showDeletedModules)
        throws Exception
    {
        return internalUser.getModules(showDeletedModules);
    }


    public Module[] getModules(String permission) throws Exception
    {
        return internalUser.getModules(permission);
    }

    private static final String GET_MODULES = 
        "getModules";

    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules(String[])
     */
    public Module[] getModules(String[] permissions)
    {
        return getModules(permissions, false);
    }
    
    /**
     * Get modules that user can copy an issue to.
     */
    public List getCopyToModules(Module currentModule) throws Exception
    {        
         return internalUser.getCopyToModules(currentModule);
    }

    /**
     * Get modules that user can move an issue to.
     */
    public List getMoveToModules(Module currentModule) throws Exception
    {        
         return internalUser.getMoveToModules(currentModule);
    }
   
   
    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules(String[], boolean)
     */
    public Module[] getModules(String[] permissions, boolean showDeletedModules)
    {        
        Module[] result = null;
        Object obj = ScarabCache.get(this, GET_MODULES, permissions); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.setDistinct();
            if (!showDeletedModules)
            {
                crit.add(ScarabModulePeer.DELETED, 0);
            }
            crit.addIn(TurbinePermissionPeer.PERMISSION_NAME, permissions);
            crit.addJoin(TurbinePermissionPeer.PERMISSION_ID, 
                     TurbineRolePermissionPeer.PERMISSION_ID);
            crit.addJoin(TurbineRolePermissionPeer.ROLE_ID, 
                         TurbineUserGroupRolePeer.ROLE_ID);
            crit.add(TurbineUserGroupRolePeer.USER_ID, getUserId());
            crit.addJoin(ScarabModulePeer.MODULE_ID, 
                         TurbineUserGroupRolePeer.GROUP_ID);
            
            try
            {
                List scarabModules = ScarabModulePeer.doSelect(crit);
                // check for permissions in global, if so get all modules
                for (int i=scarabModules.size()-1; i>=0; i--) 
                {
                    if (Module.ROOT_ID.equals(
                     ((Module)scarabModules.get(i)).getModuleId())) 
                    {
                        crit = new Criteria();
                        if (!showDeletedModules)
                        {
                            crit.add(ScarabModulePeer.DELETED, 0);
                        }
                        scarabModules = ScarabModulePeer.doSelect(crit);
                        break;
                    }
                }
                result = new Module[scarabModules.size()];
                for (int i=scarabModules.size()-1; i>=0; i--) 
                {
                    result[i] = (Module)scarabModules.get(i);
                }
            }
            catch (Exception e)
            {
                log().error("An exception prevented retrieving any modules", e);
            }
            ScarabCache.put(result, this, GET_MODULES, permissions);
        }
        else 
        {
            result = (Module[])obj;
        }
        return result;
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#hasAnyRoleIn(Module)
     */ 
    public boolean hasAnyRoleIn(Module module)
        throws Exception
    {
        return getRoles(module).size() != 0;
    }
    
    private static final String GET_ROLES = 
        "getRoles";

    /* *
     * @see org.tigris.scarab.om.ScarabUser#getRoles(Module)
     * !FIXME! need to define a Role interface (maybe the one in fulcrum is 
     * sufficient?) before making a method like this public.   
     * Right now it is only used in one place to determine
     * if the user has any roles available, so we will use a more specific
     * public method for that.
     */
    private List getRoles(Module module)
        throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_ROLES, module); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.setDistinct();
            crit.add(TurbineUserGroupRolePeer.USER_ID, getUserId());
            crit.add(TurbineUserGroupRolePeer.GROUP_ID, module.getModuleId());
            crit.addJoin(TurbineRolePeer.ROLE_ID, 
                     TurbineUserGroupRolePeer.ROLE_ID);
            result = TurbineRolePeer.doSelect(crit);
            
            // check the global module
            if (!Module.ROOT_ID.equals(module.getModuleId())) 
            {
                crit = new Criteria();
                crit.setDistinct();
                crit.add(TurbineUserGroupRolePeer.USER_ID, getUserId());
                crit.add(TurbineUserGroupRolePeer.GROUP_ID, Module.ROOT_ID);
                crit.addJoin(TurbineRolePeer.ROLE_ID, 
                             TurbineUserGroupRolePeer.ROLE_ID);
                List globalRoles = TurbineRolePeer.doSelect(crit);
                
                for (int i=0; i<globalRoles.size(); i++) 
                {
                    if (!result.contains(globalRoles.get(i))) 
                    {
                        result.add(globalRoles.get(i));
                    }
                }
            }
            ScarabCache.put(result, this, GET_ROLES, module);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#createNewUser()
     */
    public void createNewUser()
        throws Exception
    {
        // get a unique id for validating the user
        String uniqueId = GenerateUniqueId.getIdentifier();
        if (uniqueId.length() > UNIQUE_ID_MAX_LEN)
        {
            uniqueId = uniqueId.substring(0, UNIQUE_ID_MAX_LEN);
        }
        // add it to the perm table
        setConfirmed(uniqueId);
        TurbineSecurity.addUser (this, getPassword());
        setPasswordExpire();
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#getEditableModules()
     */
    public List getEditableModules() throws Exception
    {
        return internalUser.getEditableModules();
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getEditableModules(Module)
     */
    public List getEditableModules(Module currEditModule)
        throws Exception
    {
        return internalUser.getEditableModules(currEditModule);
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#getRModuleUserAttributes(Module, IssueType)
     */
    public List getRModuleUserAttributes(Module module,
                                         IssueType issueType)
        throws Exception
    {
        return internalUser.getRModuleUserAttributes(module, issueType);
    }
    
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#getRModuleUserAttribute(Module, Attribute, IssueType)
     */
    public RModuleUserAttribute getRModuleUserAttribute(Module module, 
                                                        Attribute attribute,
                                                        IssueType issueType)
        throws Exception
    {
        return internalUser
            .getRModuleUserAttribute(module, attribute, issueType);
    }
    
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#getReportingIssue(String)
     */
    public Issue getReportingIssue(String key)
        throws Exception
    {
        return internalUser.getReportingIssue(key);
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#setReportingIssue(Issue)
     */
    public String setReportingIssue(Issue issue)
        throws ScarabException
    {
        return internalUser.setReportingIssue(issue);
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#setReportingIssue(String, Issue)
     */
    public void setReportingIssue(String key, Issue issue)
    {
        internalUser.setReportingIssue(key, issue);
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#getCurrentReport(String)
     */
    public ReportBridge getCurrentReport(String key)
        throws Exception
    {
        return internalUser.getCurrentReport(key);
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#setCurrentReport(ReportBridge)
     */
    public String setCurrentReport(ReportBridge report)
        throws ScarabException
    {
        return internalUser.setCurrentReport(report);
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#setCurrentReport(String, ReportBridge)
     */
    public void setCurrentReport(String key, ReportBridge report)
    {
        internalUser.setCurrentReport(key, report);
    }
    
    /**
     * Sets the password to expire with information from the scarab.properties
     * scarab.login.password.expire value.
     *
     * @exception Exception if problem setting the password.
     */
    public void setPasswordExpire()
        throws Exception
    {
        String expireDays = Turbine.getConfiguration()
            .getString("scarab.login.password.expire", null);
        if (expireDays == null)
        {
            setPasswordExpire(null);
        }
        else
        {
            Calendar expireDate = Calendar.getInstance();
            expireDate.add(Calendar.DATE, 
                           new Integer(expireDays).intValue());
            setPasswordExpire(expireDate);
        }
    }
    
    /**
     * Sets the password to expire on the specified date.
     *
     * @param expire a <code>Calendar</code> value specifying the expire date.  If
     * this value is null, the password will be set to expire 10 years from the
     * current year. Since Logging in resets this value, it should be ok to 
     * have someone's password expire after 10 years.
     *
     * @exception Exception if problem updating the password.
     */
    public void setPasswordExpire(Calendar expire)
        throws Exception
    {
        Integer userid = getUserId();
        if (userid == null)
        {
            throw new Exception("Userid cannot be null");
        }
        UserPreference up = UserPreferenceManager.getUserPreference(getUserId());
        if (expire == null)
        {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 10);
            up.setPasswordExpire(cal.getTime());
        }
        else
        {
            up.setPasswordExpire(expire.getTime());
        }
        up.save();
    }

    /**
     * Checks if the users password has expired.
     *
     * @exception Exception if problem querying for the password.
     */
    public boolean isPasswordExpired()
        throws Exception
    {
        Integer userid = getUserId();
        if (userid == null)
        {
            throw new Exception ("Userid cannot be null");
        }
        Criteria crit = new Criteria();
        crit.add(UserPreferencePeer.USER_ID, userid);
        Calendar cal = Calendar.getInstance();
        crit.add(UserPreferencePeer.PASSWORD_EXPIRE, 
                 cal.getTime() , Criteria.LESS_THAN);
        List result = UserPreferencePeer.doSelect(crit);
        return result.size() == 1 ? true : false;
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
        return internalUser.getEnterIssueRedirect();
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
        internalUser.setEnterIssueRedirect(templateCode);
    }
                
    /**
     * @see ScarabUser#getHomePage()
     */
    public String getHomePage()
        throws Exception
    {
        return internalUser.getHomePage();
    }
    
    /**
     * @see ScarabUser#getHomePage(Module)
     */
    public String getHomePage(Module module)
        throws Exception
    {
        return internalUser.getHomePage(module);
    }

    /**
     * @see ScarabUser#setHomePage(String)
     */
    public void setHomePage(String homePage)
        throws Exception
    {
        internalUser.setHomePage(homePage);
    }


    /**
     * @see ScarabUser#getMITLists()
     */
    public List getMITLists()
        throws TorqueException
    {
        return internalUser.getMITLists();
    }
  
    /**
     * @see ScarabUser#getSearchableRMITs(String, String, String, String)
     */
    public List getSearchableRMITs(String searchField, String searchString, 
                                   String sortColumn, String sortPolarity)
        throws Exception    
    {
        return internalUser.getSearchableRMITs(searchField, searchString, 
                                               sortColumn, sortPolarity);
    }

    /**
     * @see ScarabUser#addRMITsToCurrentMITList(List)
     */
    public void addRMITsToCurrentMITList(List rmits)
        throws TorqueException
    {
        internalUser.addRMITsToCurrentMITList(rmits);
    }

    /**
     * @see ScarabUser#getCurrentMITList()
     */
    public MITList getCurrentMITList()
    {
        return internalUser.getCurrentMITList();
    }

    /**
     * @see ScarabUser#setCurrentMITList(MITList)
     */
    public void setCurrentMITList(MITList list)
    {
        internalUser.setCurrentMITList(list);
    }

    /**
     * @see ScarabUser#removeItemsFromCurrentMITList(String[])
     */
    public void removeItemsFromCurrentMITList(String[] ids)
    {
        internalUser.removeItemsFromCurrentMITList(ids);
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#getMostRecentQuery()
     */
    public String getMostRecentQuery()
    {
        return internalUser.getMostRecentQuery();
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#setMostRecentQuery(String)
     */
    public void setMostRecentQuery(String queryString)
    {
        internalUser.setMostRecentQuery(queryString);
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#hasMostRecentQuery()
     */
    public boolean hasMostRecentQuery()
    {
        return internalUser.hasMostRecentQuery();
    }

    public Map getAssociatedUsersMap()
        throws Exception
    {
        return internalUser.getAssociatedUsersMap();
    }

    public void setAssociatedUsersMap(Map associatedUsers)
        throws Exception
    {
        internalUser.setAssociatedUsersMap(associatedUsers);
    }

    public Map getSelectedUsersMap()
        throws Exception
    {
        return internalUser.getSelectedUsersMap();
    }

    public void setSelectedUsersMap(Map selectedUsers)
        throws Exception
    {
        internalUser.setSelectedUsersMap(selectedUsers);
    }
    
    
    /**
     * @see ScarabUser#getThreadKey()
     */
    public Object getThreadKey()
    {
        return internalUser.getThreadKey();
    }

    /**
     * @see ScarabUser#setThreadKey(Integer)
     */
    public void setThreadKey(Integer key)
    {  
        internalUser.setThreadKey(key);
    }


    /**
     * The current module
     */
    public Module getCurrentModule() 
    {
        return internalUser.getCurrentModule();
    }
    
    /**
     * The current module
     */
    public void setCurrentModule(Module  v) 
    {
        internalUser.setCurrentModule(v);
    }
     
    /**
     * The current issue type
     */
    public IssueType getCurrentIssueType()
        throws Exception
    {
        return internalUser.getCurrentIssueType();
    }
    
    /**
     * The current issue type
     */
    public void setCurrentIssueType(IssueType  v) 
    {
        internalUser.setCurrentIssueType(v);
    }    
    
    /**
     * @see ScarabUser#getCurrentRModuleIssueType()
     */
    public RModuleIssueType getCurrentRModuleIssueType()
        throws Exception
    {
        return internalUser.getCurrentRModuleIssueType();
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#updateIssueListAttributes(List)
     */
    public void updateIssueListAttributes(List attributes)
        throws Exception
    {
        internalUser.updateIssueListAttributes(attributes);
    }

    public List getRoleNames(Module module)
       throws Exception
    {
       return null;
    }

    /**
     * Report on size of several maps
     */
    public String getStats()
    {
        return internalUser.getStats() 
            + "; TempStorage=" + getTempStorage().size() 
            + "; PermStorage=" + getPermStorage().size(); 
    }

    /**
     * Sets the users default locale to the users preferences.
     * No need to call user.save() as this method will save the
     * preferences for us.
     */
    public void setLocale(String acceptLanguage)
        throws Exception
    {
        UserPreference up = UserPreferenceManager.getUserPreference(getUserId());
        up.setAcceptLanguage(acceptLanguage);
        up.save();
        locale = Localization.getLocale(acceptLanguage);
    }

    /**
     * Gets the users default locale from the users preferences.
     */
    public Locale getLocale()
        throws Exception
    {
        if (locale == null)
        {
            UserPreference up = UserPreferenceManager.getUserPreference(getUserId());
            String header = up.getLanguage();
            locale = Localization.getLocale(header);
        }
        return locale;
    }
}
