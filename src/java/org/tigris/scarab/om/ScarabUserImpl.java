package org.tigris.scarab.om;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Calendar;

import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.impl.db.entity.TurbineUserGroupRolePeer;
import org.apache.torque.Torque;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.BaseObject;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.commons.util.GenerateUniqueId;

import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.security.ScarabSecurity;
import org.tigris.scarab.security.SecurityFactory;

import org.apache.turbine.Turbine;

/**
 * This class is an abstraction that is currently based around
 * Turbine's code. We can change this later. It is here so
 * that it is easier to change later to work under different
 * implementation needs.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ScarabUserImpl.java,v 1.29 2001/10/25 00:55:09 jon Exp $
 */
public class ScarabUserImpl 
    extends BaseScarabUserImpl 
    implements ScarabUser
{
    //private static final String CURRENT_MODULE = "CURRENT_MODULE";
    private static final String REPORTING_ISSUE = "REPORTING_ISSUE";

    public static final String PASSWORD_EXPIRE = "PASSWORD_EXPIRE";

    private int issueCount = 0;

    /**
     * The maximum length for the unique identifier used at user
     * creation time.
     */
    private static final int UNIQUE_ID_MAX_LEN = 10;

    /**
     * Call the superclass constructor to initialize this object.
     */
    public ScarabUserImpl()
    {
        super();
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
            List result = (List) ScarabUserImplPeer.doSelect(criteria);
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
        This will return the username for a given userid.
        return null if there is an error or if the user doesn't exist
    */
    public static String getUserName(ObjectKey userid)
    {
        try
        {
            Criteria criteria = new Criteria();
            criteria.add (ScarabUserImplPeer.USER_ID, userid);
            User user = (User)ScarabUserImplPeer.doSelect(criteria).get(0);
            return user.getUserName();
        }
        catch (Exception e)
        {
            return null;
        }        
    }

    /**
        Pass in a string id and return username.
    */
    public static String getUserName(String userId)
    {
        return getUserName((ObjectKey)new NumberKey(userId));
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
     * @see org.tigris.scarab.om.ScarabUser#getModules()
     */
    public List getModules() throws Exception
    {
        Criteria crit = new Criteria();
        crit.addJoin(TurbineUserGroupRolePeer.USER_ID, ScarabUserImplPeer.USER_ID);
        crit.addJoin(TurbineUserGroupRolePeer.GROUP_ID, ScarabModulePeer.MODULE_ID);
        crit.add(TurbineUserGroupRolePeer.USER_ID, this.getUserId());
        GroupSet groups = TurbineSecurity.getGroups(crit);
        Iterator itr = groups.elements();
        List modules = new ArrayList(groups.size());
        while (itr.hasNext())
        {
            Group group = (Group) itr.next();
            modules.add((ModuleEntity)group);
        }
        return modules;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getEditableModules()
     */
    public List getEditableModules() throws Exception
    {
        List userModules = getModules();
        ArrayList editModules = new ArrayList();
        ScarabSecurity security = SecurityFactory.getInstance();

        for (int i=0; i<userModules.size(); i++)
        {
            ModuleEntity module = (ModuleEntity)userModules.get(i);
            if (security.hasPermission(ScarabSecurity.MODULE__EDIT, 
                                      (ScarabUser)this,
                                      (ModuleEntity)module)
               && !(module.getModuleId().toString().equals("0")))
            {
                editModules.add(module);
            }
        }
        return editModules;
     }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getRModuleUserAttributes(ModuleEntity, IssueType)
     */
    public List getRModuleUserAttributes(ModuleEntity module,
                                         IssueType issueType)
        throws Exception
    {
        List rmuas = new ArrayList();
        Criteria crit = new Criteria()
           .add(RModuleUserAttributePeer.USER_ID, getUserId())
           .add(RModuleUserAttributePeer.MODULE_ID, module.getModuleId())
           .add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                issueType.getIssueTypeId());

        return getRModuleUserAttributes(crit);
    }

            
    /**
     * @see org.tigris.scarab.om.ScarabUser#getRModuleUserAttribute(ModuleEntity, Attribute, IssueType)
     */
    public RModuleUserAttribute getRModuleUserAttribute(ModuleEntity module, 
                                                       Attribute attribute,
                                                       IssueType issueType)
        throws Exception
    {
        RModuleUserAttribute mua = null;
        Criteria crit = new Criteria(4)
           .add(RModuleUserAttributePeer.MODULE_ID, module.getModuleId())
           .add(RModuleUserAttributePeer.USER_ID, getUserId())
           .add(RModuleUserAttributePeer.ATTRIBUTE_ID, attribute.getAttributeId())
           .add(RModuleUserAttributePeer.ATTRIBUTE_ID, issueType.getIssueTypeId());
        try
        {
   
            mua = (RModuleUserAttribute)RModuleUserAttributePeer
                                        .doSelect(crit).elementAt(0);
        }
        catch (Exception e)
        {
            mua = new RModuleUserAttribute();
            mua.setModuleId(module.getModuleId());
            mua.setUserId(getUserId());
            mua.setIssueTypeId(issueType.getIssueTypeId());
            mua.setAttributeId(attribute.getAttributeId());
        }
        return mua;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules(Role)
     */
    public List getModules(Role role) 
        throws Exception
    {
    /*
        Criteria crit = new Criteria(3)
            .add(RModuleUserRolePeer.DELETED, false)
            .add(RModuleUserRolePeer.ROLE_ID, 
                 ((BaseObject)role).getPrimaryKey());        
        List moduleRoles = getRModuleUserRolesJoinScarabModule(crit);

        // rearrange so list contains Modules
        List modules = new ArrayList(moduleRoles.size());
        Iterator i = moduleRoles.iterator();
        while (i.hasNext()) 
        {
            ModuleEntity module = 
                (ModuleEntity) ((RModuleUserRole)i.next()).getScarabModule();
            modules.add(module);
        }

        return modules;
    */
        if (true)
            throw new Exception ("FIXME: This method doesn't belong here!");
        return null;
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getReportingIssue(String)
     */
    public Issue getReportingIssue(String key)
        throws Exception
    {
        return (Issue) getTemp(REPORTING_ISSUE+key);
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
            setTemp(REPORTING_ISSUE+key, issue);
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
            removeTemp(REPORTING_ISSUE+key);
        }
        else 
        {
            setTemp(REPORTING_ISSUE+key, issue);
        }
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
        NumberKey userid = getUserId();
        if (userid == null)
        {
            throw new Exception("Userid cannot be null");
        }
        UserPreference up = UserPreference.getInstance(userid);
        if (up == null)
        {
            up = UserPreference.getInstance();
            up.setUserId(userid);
        }
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
        NumberKey userid = getUserId();
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
}
