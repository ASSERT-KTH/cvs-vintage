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

// JDK classes
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Category;

// Turbine classes
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.BasePeer;
import org.apache.torque.oid.IDBroker;
import org.apache.torque.pool.DBConnection;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.fulcrum.security.util.TurbineSecurityException;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.impl.db.entity.TurbineUserGroupRole;

// Scarab classes
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.services.user.UserManager;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.security.ScarabSecurity;

import org.apache.turbine.Log;
import org.apache.fulcrum.security.impl.db.entity
    .TurbinePermissionPeer;
import org.apache.fulcrum.security.impl.db.entity
    .TurbineUserGroupRolePeer;
import org.apache.fulcrum.security.impl.db.entity
    .TurbineRolePermissionPeer;
import org.apache.fulcrum.security.impl.db.entity
    .TurbineRolePeer;

/**
 * The ScarabModule class is the focal point for dealing with
 * Modules. It implements the concept of a ModuleEntity which is a
 * single module and is the base interface for all Modules. In code,
 * one should <strong>never reference ScarabModule directly</strong>
 * -- use its ModuleEntity interface instead.  This allows us to swap
 * out ModuleEntity implementations by modifying the Scarab.properties
 * file.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: ScarabModule.java,v 1.84 2002/01/27 21:21:12 jmcnally Exp $
 */
public class ScarabModule
    extends BaseScarabModule
    implements Persistent, ModuleEntity, Group
{
    protected static final NumberKey ROOT_ID = new NumberKey("0");

    /**
     * @see org.tigris.scarab.services.module.ModuleEntity#getUsers(String)
     */
    public ScarabUser[] getUsers(String permission)
    {
        List perms = new ArrayList(1);
        perms.add(permission);
        return getUsers(perms);
    }

    /**
     * @see org.tigris.scarab.services.module.ModuleEntity#getUsers(List)
     */
    public ScarabUser[] getUsers(List permissions)
    {
        ScarabUser[] scarabUsers = null;
        Criteria crit = new Criteria();
        crit.setDistinct();
        if ( permissions.size() == 1 ) 
        {
            crit.add(TurbinePermissionPeer.NAME, permissions.get(0));
        }
        else if (permissions.size() > 1)
        {
            crit.addIn(TurbinePermissionPeer.NAME, permissions);
        }      

        if (permissions.size() >= 1)
        {
            crit.addJoin(TurbinePermissionPeer.PERMISSION_ID, 
                         TurbineRolePermissionPeer.PERMISSION_ID);
            crit.addJoin(TurbineRolePermissionPeer.ROLE_ID, 
                         TurbineUserGroupRolePeer.ROLE_ID);
            crit.add(TurbineUserGroupRolePeer.GROUP_ID, 
                     ((Persistent)this).getPrimaryKey());
            crit.addJoin(ScarabUserImplPeer.USER_ID, 
                         TurbineUserGroupRolePeer.USER_ID);
            try
            {
                User[] users = TurbineSecurity.getUsers(crit);
                scarabUsers = new ScarabUser[users.length];
                for ( int i=scarabUsers.length-1; i>=0; i--) 
                {
                    scarabUsers[i] = (ScarabUser)users[i];
                }
            }
            catch (Exception e)
            {
                Log.error("An exception prevented retrieving any users", e);
            }
        }
        else 
        {
            scarabUsers = new ScarabUser[0];
        }
        return scarabUsers;
    }


    /**
     * @see org.tigris.scarab.services.module.ModuleEntity#getUsers(String, String, String, IssueType)
     */
    public List getUsers(String firstName, String lastName, 
                         String username, String email, IssueType issueType)
        throws Exception
    {
        ScarabUser[] eligibleUsers = getUsers(getUserPermissions(issueType));
        List userIds = new ArrayList();
        for (int i = 0; i < eligibleUsers.length; i++)
        {
            userIds.add(eligibleUsers[i].getUserId());
        }
        Criteria crit = new Criteria();
        crit.addIn(ScarabUserImplPeer.USER_ID, userIds);

        if (firstName != null)
        {
            crit.add(ScarabUserImplPeer.FIRST_NAME, addWildcards(firstName), 
                     Criteria.LIKE);
        }
        if (lastName != null)
        {
            crit.add(ScarabUserImplPeer.LAST_NAME, addWildcards(lastName), 
                     Criteria.LIKE);
        }
        if (username != null)
        {
            crit.add(ScarabUserImplPeer.LOGIN_NAME, addWildcards(username), 
                     Criteria.LIKE);
        }
        if (email != null)
        {
            crit.add(ScarabUserImplPeer.EMAIL, addWildcards(email), 
                     Criteria.LIKE);
        }
        return ScarabUserImplPeer.doSelect(crit);
    }

    private Object addWildcards(String s)
    {
        return new StringBuffer(s.length() + 2)
            .append('%').append(s).append('%').toString(); 
    }

    /**
     * Wrapper method to perform the proper cast to the BaseModule method
     * of the same name. FIXME: find a better way
     */
    public void setParent(ModuleEntity v) throws Exception
    {
        super.setScarabModuleRelatedByParentId((ScarabModule)v);
    }

    /**
     * Cast the getScarabModuleRelatedByParentId() to a ModuleEntity
     */
    public ModuleEntity getParent() throws Exception
    {
        return (ModuleEntity) super.getScarabModuleRelatedByParentId();
    }

    protected List getRModuleAttributesThisModuleOnly(Criteria crit)
        throws Exception
    {
        return super.getRModuleAttributes(crit);
    }

    public Vector getRModuleIssueTypes()
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleIssueTypePeer.MODULE_ID, getModuleId())
        .addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID, 
                     IssueTypePeer.ISSUE_TYPE_ID)
        .add(IssueTypePeer.PARENT_ID, 0)
        .add(IssueTypePeer.DELETED, 0)
        .addAscendingOrderByColumn(RModuleIssueTypePeer.PREFERRED_ORDER);
        return super.getRModuleIssueTypes(crit);
    }

    public boolean allowsIssues() {
        return (true);
    }
    
    /**
     * Saves the module into the database
     */
    public void save() 
        throws TurbineSecurityException
    {
        try
        {
            super.save();
        }
        catch (Exception e)
        {
            throw new TurbineSecurityException(e.getMessage(), e);
        }
    }

    /**
     * Saves the module into the database
     */
    public void save(DBConnection dbCon) 
        throws Exception
    {
        // if new, make sure the code has a value.
        if ( isNew() )
        {
            Criteria crit = new Criteria();
            crit.add(ScarabModulePeer.MODULE_NAME, getRealName());
            crit.add(ScarabModulePeer.PARENT_ID, getParentId());
            // FIXME: this should be done with a method in ModuleEntity
            // that takes the two criteria values as a argument so that other 
            // implementations can benefit from being able to get the 
            // list of modules. -- do not agree - jdm
            List result = (List) ScarabModulePeer.doSelect(crit);
            if (result.size() > 0)
            {
                throw new Exception("Sorry, a module with that name " + 
                                    "and parent already exist.");
            }
            
            String code = getCode();
            if ( code == null || code.length() == 0 )
            {
                if ( getParentId().equals(ROOT_ID) )
                {
                    throw new ScarabException(
                        "A top level module addition was"
                        + " attempted without assigning a Code");
                }

                setCode(getParent().getCode());
            }

            // need to do this before the relationship save below
            // in order to set the moduleid for the new module.
            super.save(dbCon);
            
            if ( getOwnerId() == null ) 
            {
                throw new ScarabException(
                     "Can't save a project without first assigning an owner.");
            }
            grant (UserManager.getInstance(getOwnerId()), 
                   TurbineSecurity.getRole("Project Owner"));
        
            setInitialAttributesAndIssueTypes();
        }
        else
        {
            super.save(dbCon);
        }
    }

    // *******************************************************************
    // Turbine Group implementation get/setName and save are defined in
    // parent class AbstractScarabModule
    // *******************************************************************


    /**
     * Removes a group from the system.
     *
     * @throws TurbineSecurityException if the Group could not be removed.
     */
    public void remove()
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented");
    }

    /**
     * Renames the group.
     *
     * @param name The new Group name.
     * @throws TurbineSecurityException if the Group could not be renamed.
     */
    public void rename(String name)
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented");
    }

    /**
     * Grants a Role in this Group to an User.
     *
     * @param user An User.
     * @param role A Role.
     * @throws TurbineSecurityException if there is a problem while assigning
     * the Role.
     */
    public void grant(User user, Role role)
        throws TurbineSecurityException
    {
        TurbineSecurity.grant(user,(Group)this,role);
    }

    /**
     * Grants Roles in this Group to an User.
     *
     * @param user An User.
     * @param roleSet A RoleSet.
     * @throws TurbineSecurityException if there is a problem while assigning
     * the Roles.
     */
    public void grant(User user, RoleSet roleSet)
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented");
    }

    /**
     * Revokes a Role in this Group from an User.
     *
     * @param user An User.
     * @param role A Role.
     * @throws TurbineSecurityException if there is a problem while unassigning
     * the Role.
     */
    public void revoke(User user, Role role)
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented");
    }

    /**
     * Revokes Roles in this group from an User.
     *
     * @param user An User.
     * @param roleSet a RoleSet.
     * @throws TurbineSecurityException if there is a problem while unassigning
     * the Roles.
     */
    public void revoke(User user, RoleSet roleSet)
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented");
    }

    /**
     * Used for ordering Groups.
     *
     * @param obj The Object to compare to.
     * @return -1 if the name of the other object is lexically greater than 
     * this group, 1 if it is lexically lesser, 0 if they are equal.
     */
    public int compareTo(Object obj)
    {
        //---------------------------------------------------------------------
        // dr@bitonic.com : commented out as per conversation with John McNally
        //   over IRC on 20-Dec-2001
        //---------------------------------------------------------------------
        //if (this.getClass() != obj.getClass())
        //{
        //    throw new ClassCastException();
        //}
        String name1 = ((Group)obj).getName();
        String name2 = this.getName();

        return name2.compareTo(name1);
    }

    public String toString()
    {
        String name = getName();
        if (name == null)
        {
            name = getRealName();
        }
        if (name == null)
        {
            name = getClass().getName();
        }
        return name;
    }
}

