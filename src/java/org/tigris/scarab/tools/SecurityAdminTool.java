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

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.Serializable;

import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.util.AccessControlList;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.UnknownEntityException;

import org.apache.torque.util.Criteria;
import org.apache.torque.TorqueException;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.PendingGroupUserRolePeer;
import org.tigris.scarab.om.PendingGroupUserRole;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * This scope is an object that is made available as a global
 * object within the system to allow access to methods dealing
 * with security (users, roles, permissions, etc).
 * This object must be thread safe as multiple
 * requests may access it at the same time. The object is made
 * available in the context as: $securityAdmin
 * <p>
 * The design goals of the Scarab*API is to enable a <a
 * href="http://jakarta.apache.org/turbine/pullmodel.html">pull based
 * methodology</a> to be implemented.
 *
 * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
 * @version $Id: SecurityAdminTool.java,v 1.12 2003/03/28 00:01:46 jon Exp $
 */
public class SecurityAdminTool 
    implements SecurityAdminScope, Serializable
{
    private static final String HAS_REQUESTED_ROLE = "hasRequestedRole";

    private static final String GET_PENDING = "getPendingGroupUserRoles";

    public void init(Object data)
    {
    }
    
    public void refresh()
    {
    }
    
    /** Returns a User object retrieved by specifying the username.
     *
     * @param username the username of the user to retrieve
     * @return the specified user, if found, or null otherwise
     */
    public ScarabUser getUserByUsername(String username) throws Exception
    {
        ScarabUser user = null;
        
        try
        {
            user = (ScarabUser)TurbineSecurity.getUser(username);
        }
        catch (UnknownEntityException uee)
        {
            // FIXME are we sure we want to do nothing with these excetpions?
            //if so, state it explicitly
        }
        catch (DataBackendException dbe)
        {          
        }
        
        return user;
    }
    
    /** Returns a Permission object retrieved by specifying the name of the permission.
     *
     * @param name the name of the permission to retrieve
     * @return the specified Permission, if found, or null otherwise
     */
    public Permission getPermissionByName(String name) throws Exception
    {
        Permission permission = null;
        permission = TurbineSecurity.getPermission(name);
        
        return permission;
    }
    
    /** Returns a Role object retrieved by specifying the name of the role.
     *
     * @param name the name of the role to retrieve
     * @return the specified Role, if found, or null otherwise
     */
    public Role getRoleByName(String name) throws Exception
    {
        Role role = null;
        role = TurbineSecurity.getRole(name);
        
        return role;
    }
    
    /** 
     * Gets a list of all Groups
     */
    public Group[] getGroups() throws Exception
    {
        return TurbineSecurity.getAllGroups().getGroupsArray();
    }

    /** 
     * Gets a list of active Groups in which the user does not have a current
     * role and has not already requested a role.
     */
    public List getNonMemberGroups(ScarabUser user) throws Exception
    {
        AccessControlList acl = getACL(user);
        Group[] groups = TurbineSecurity.getAllGroups().getGroupsArray();
        List nonmemberGroups = new LinkedList();
        for (int i=0; i<groups.length; i++) 
        {
            Module module = (Module)groups[i];
            if (!module.isGlobalModule() && !module.getDeleted()) 
            {
                RoleSet roleSet = acl.getRoles(groups[i]);
                if (roleSet == null || roleSet.size() == 0) 
                {
                    boolean hasRole = false;
                    // need to check for already requested roles
                    Role[] roles = 
                        TurbineSecurity.getAllRoles().getRolesArray();
                    for (int j=0; j<roles.length; j++) 
                    {
                        if (hasRequestedRole(user, roles[j], groups[i])) 
                        {
                            hasRole = true;
                            break;
                        }
                    }
                    if (!hasRole) 
                    {
                        nonmemberGroups.add(groups[i]);   
                    }                    
                }   
            }
        }
        return nonmemberGroups;
    }
    
    public boolean hasRequestedRole(ScarabUser user, Role role, Group group)
        throws TorqueException
    {
        List result = null;
        Object obj = ScarabCache.get(this, HAS_REQUESTED_ROLE, user); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.add(PendingGroupUserRolePeer.USER_ID, user.getUserId());
            result = PendingGroupUserRolePeer.doSelect(crit);
            ScarabCache.put(result, this, HAS_REQUESTED_ROLE);
        }
        else 
        {
            result = (List)obj;
        }
        boolean b = false;
        Iterator iter = result.iterator();
        while (iter.hasNext()) 
        {
            PendingGroupUserRole pmur = (PendingGroupUserRole)iter.next();
            if (pmur.getRoleName().equals(role.getName())
                && ((Module)group).getModuleId().equals(pmur.getGroupId())) 
            {
                b = true;
                break;
            }
        }
        return b;
    }

    /** 
     * Gets a list of all Permissions
     */
    public Permission[] getPermissions() throws Exception
    {
        return (TurbineSecurity.getAllPermissions().getPermissionsArray());
    }

    /** 
     * Gets a list of all Permissions
     */
    public List getPermissionsAsStrings() throws Exception
    {
        Permission[] allPerms = this.getPermissions();
        List list = new ArrayList(allPerms.length);
        for (int i=0; i<allPerms.length;i++)
        {
            list.add(allPerms[i].getName());
        }
        return list;
    }
    
    /** 
     * Gets a list of all Roles.
     */
    public Role[] getRoles() throws Exception
    {
        return TurbineSecurity.getAllRoles().getRolesArray();
    }
    
    /** 
     * Gets a list of all Roles.
     */
    public List getNonRootRoles() throws Exception
    {
        List nonRootRoles = new LinkedList();
        Role[] roles = TurbineSecurity.getAllRoles().getRolesArray();
        for (int i=0; i<roles.length; i++) 
        {
            Role role = roles[i];
            if (!role.getName().equals("Root")) 
            {
                nonRootRoles.add(role);
            }
        }
        return nonRootRoles;
    }
   
    public List getPendingGroupUserRoles(Module module)
        throws TorqueException
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_PENDING, module); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.add(PendingGroupUserRolePeer.GROUP_ID, module.getModuleId());
            result = PendingGroupUserRolePeer.doSelect(crit);
            ScarabCache.put(result, this, GET_PENDING);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }
 
    /**
     * Gets an ACL object for a user
     */
    public AccessControlList getACL(ScarabUser user) throws Exception
    {
        return TurbineSecurity.getACL(user);
    }    
}
