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
import java.util.Vector;

import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.impl.db.entity.TurbineUserGroupRolePeer;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.BaseObject;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.commons.util.GenerateUniqueId;

import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.Issue;

/**
    This class is an abstraction that is currently based around
    Turbine's code. We can change this later. It is here so
    that it is easier to change later to work under different
    implementation needs.

    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: ScarabUserImpl.java,v 1.20 2001/10/04 23:21:14 jon Exp $
*/
public class ScarabUserImpl 
    extends BaseScarabUserImpl 
    implements ScarabUser
{
    private static final String CURRENT_MODULE = "CURRENT_MODULE";
    private static final String REPORTING_ISSUE = "REPORTING_ISSUE";
    private static final String REPORTING_ISSUE_START_POINT = "RISP";

    /**
        The maximum length for the unique identifier used at user
        creation time.
    */
    private static final int UNIQUE_ID_MAX_LEN = 10;

    /**
        Call the superclass constructor to initialize this object.
    */
    public ScarabUserImpl()
    {
        super();
    }
    
    /**
        This method is responsible for creating a new user. It will throw an 
        exception if there is any sort of error (such as a duplicate login id) 
        and place the error message into e.getMessage(). This also creates a 
        uniqueid and places it into this object in the perm table under the
        Visitor.CONFIRM_VALUE key. It will use the current instance of this
        object as the basis to create the new User.
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
    }
    
    /**
        Utility method that takes a username and a confirmation code
        and will return true if there is a match and false if no match.
        <p>
        If there is an Exception, it will also return false.
    */
    public static boolean checkConfirmationCode (String username, String confirm)
    {
        // security check. :-)
        if (confirm.equalsIgnoreCase(User.CONFIRM_DATA))
        {
            return false;
        }
    
        try
        {
            Criteria criteria = new Criteria();
            criteria.add (ScarabUserImplPeer.getColumnName(User.USERNAME), username);
            criteria.add (ScarabUserImplPeer.getColumnName(User.CONFIRM_VALUE), confirm);
            criteria.setSingleRecord(true);
            Vector result = ScarabUserImplPeer.doSelect(criteria);
            if (result.size() > 0)
                return true;

            // FIXME: once i figure out how to build an OR in a Criteria i won't need this.
            // We check to see if the user is already confirmed because that should
            // result in a True as well.
            criteria = new Criteria();
            criteria.add (ScarabUserImplPeer.getColumnName(User.USERNAME), username);
            criteria.add (ScarabUserImplPeer.getColumnName(User.CONFIRM_VALUE), User.CONFIRM_DATA);
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
            User user = (User)ScarabUserImplPeer.doSelect(criteria).elementAt(0);
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
        This method will build up a criteria object out of the information 
        currently stored in this object and then return it.
    */
    private Criteria getCriteria()
    {
        // FIXME: clean up ugly code duplication below. this will be done
        //        by taking advantage of an autogenerated UserPeer instead
        //        of this ugly hack!!
        Criteria criteria = new Criteria();
        criteria.add (ScarabUserImplPeer.getColumnName(User.USERNAME), this.getUserName());
        criteria.add (ScarabUserImplPeer.getColumnName(User.PASSWORD), getPerm(User.PASSWORD));
        criteria.add (ScarabUserImplPeer.getColumnName(User.FIRST_NAME), getPerm(User.FIRST_NAME));
        criteria.add (ScarabUserImplPeer.getColumnName(User.LAST_NAME), getPerm(User.LAST_NAME));
        criteria.add (ScarabUserImplPeer.getColumnName(User.EMAIL), this.getUserName());
        return criteria;
    }

    /**
     * Gets all modules which are currently associated with this user 
     * (relationship has not been deleted.)
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
     * Gets all attributes which this user has selected to appear on the 
     * IssueList screen. If they have not selected attributes, 
     * Or it is an anonymous user, use the default attributes
     * In the database for user 0.
     */
    public List getAttributesForIssueList(ModuleEntity module) throws Exception
    {
        Criteria crit = new Criteria(4)
            .add(RModuleUserAttributePeer.MODULE_ID, module.getModuleId())
            .add(RModuleUserAttributePeer.USER_ID, getUserId())
            .addAscendingOrderByColumn(RModuleUserAttributePeer.PREFERRED_ORDER);
        Vector userAttributes = RModuleUserAttributePeer.doSelect(crit);
        if (userAttributes.isEmpty())
        {
            crit = new Criteria(2)
               .add(RModuleUserAttributePeer.USER_ID, 0)
               .addAscendingOrderByColumn(RModuleUserAttributePeer.PREFERRED_ORDER);
            userAttributes = RModuleUserAttributePeer.doSelect(crit);
        }

        List attributes = new ArrayList(userAttributes.size());
        Iterator i = userAttributes.iterator();
        while (i.hasNext()) 
        {
            Attribute attribute = 
                (Attribute) ((RModuleUserAttribute)i.next()).getAttribute();

            if ( !attributes.contains(attributes) ) 
            {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    /**
     * Returns list of RModuleUserAttribute objects for this
     * User and Module -- the attributes the use has selected
     * To appear on the IssueList for this module.
     */
    public List getRModuleUserAttributes(ModuleEntity module)
        throws Exception
    {
        List rmuas = new ArrayList();
        Criteria crit = new Criteria()
           .add(RModuleUserAttributePeer.USER_ID, getUserId())
           .add(RModuleUserAttributePeer.MODULE_ID, module.getModuleId());

        Vector results = getRModuleUserAttributes(crit);
        Iterator i = results.iterator();
        while ( i.hasNext() ) 
        {
            RModuleUserAttribute rmua = (RModuleUserAttribute)i.next();
            rmuas.add(rmua);
        }
        
        return rmuas;
    }

    /**
     * Returns an RModuleUserAttribute object.
     */
    public RModuleUserAttribute getModuleUserAttribute(NumberKey moduleId, 
                                                       NumberKey attributeId) 
        throws Exception
    {
        RModuleUserAttribute mua = null;
        Criteria crit = new Criteria(4)
           .add(RModuleUserAttributePeer.MODULE_ID, moduleId)
           .add(RModuleUserAttributePeer.USER_ID, getUserId())
           .add(RModuleUserAttributePeer.ATTRIBUTE_ID, attributeId);
        try
        {
   
            mua = (RModuleUserAttribute)RModuleUserAttributePeer
                                        .doSelect(crit).elementAt(0);
        }
        catch (Exception e)
        {
            mua = new RModuleUserAttribute();
            mua.setModuleId(moduleId);
            mua.setUserId(getUserId());
            mua.setAttributeId(attributeId);
        }
        return mua;
    }



    /**
     * Gets modules which are currently associated (relationship has not 
     * been deleted) with this user through the specified Role. 
     * 
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

    public Issue getReportingIssue(ModuleEntity me)
        throws Exception
    {
        Issue issue = (Issue) getTemp(REPORTING_ISSUE);
        if ( issue == null ) 
        {
            issue = me.getNewIssue(this);
            setTemp(REPORTING_ISSUE, issue);
        }
        return issue;
    }

    public void setReportingIssue(Issue issue)
    {
        if ( issue == null ) 
        {
            removeTemp(REPORTING_ISSUE);
            setReportingIssueStartPoint(null);
        }
        else 
        {
            setTemp(REPORTING_ISSUE, issue);
        }
    }

    public String getReportingIssueStartPoint()
        throws Exception
    {
        String template = (String) getTemp(REPORTING_ISSUE_START_POINT);
        if ( template == null ) 
        {
            // FIXME: this doesn't belong here
            template = "entry,Wizard3.vm";            
        }
        
        return template;
    }

    public void setReportingIssueStartPoint(String template)
    {
        if ( template == null ) 
        {
            removeTemp(REPORTING_ISSUE_START_POINT);
        }
        else 
        {
            setTemp(REPORTING_ISSUE_START_POINT, template);            
        }
    }
}
