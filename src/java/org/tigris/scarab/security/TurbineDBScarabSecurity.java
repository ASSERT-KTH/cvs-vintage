package org.tigris.scarab.security;

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

// Turbine
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.Log;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.util.AccessControlList;
import org.apache.torque.om.Persistent;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.impl.db.entity
    .TurbinePermissionPeer;
import org.apache.fulcrum.security.impl.db.entity
    .TurbineUserGroupRolePeer;
import org.apache.fulcrum.security.impl.db.entity
    .TurbineRolePermissionPeer;

import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.om.ScarabModulePeer;

/**
 * Security wrapper around turbine's implementation
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: TurbineDBScarabSecurity.java,v 1.13 2001/08/09 20:34:11 jmcnally Exp $
*/
public class TurbineDBScarabSecurity 
    extends DefaultScarabSecurity
{
    /**
     * does nothing
     */
    public TurbineDBScarabSecurity()
    {
    }

    /**
     * Determine if a user has a permission within a module.
     *
     * @param permission a <code>String</code> permission value, which should
     * be a constant in this interface.
     * @param user a <code>ScarabUser</code> value
     * @param module a <code>ModuleEntity</code> value
     * @return true if the permission exists for the user within the
     * given module, false otherwise
     */
    public boolean hasPermission(String permission, 
                                 ScarabUser user, ModuleEntity module)
    {
        boolean hasPermission = false;
        try
        {
            AccessControlList acl = TurbineSecurity.getACL(user);
            if ( acl != null ) 
            {
                hasPermission = acl.hasPermission(permission, (Group)module);
            }
        }
        catch (Exception e)
        {
            hasPermission = false;
            Log.error("Permission check failed on:" + permission, e);
        }
        return hasPermission;
    }

    /**
     * Get a list of <code>ScarabUser</code>'s that have the given
     * permission in the given module.
     *
     * @param permission a <code>String</code> value
     * @param module a <code>ModuleEntity</code> value
     * @return null
     */
    public ScarabUser[] getUsers(String permission, ModuleEntity module)
    {
        Criteria crit = new Criteria();
        crit.setDistinct();
        crit.add(TurbinePermissionPeer.NAME, permission);
        crit.addJoin(TurbinePermissionPeer.PERMISSION_ID, 
                     TurbineRolePermissionPeer.PERMISSION_ID);
        crit.addJoin(TurbineRolePermissionPeer.ROLE_ID, 
                     TurbineUserGroupRolePeer.ROLE_ID);
        crit.add(TurbineUserGroupRolePeer.GROUP_ID, 
                 ((Persistent)module).getPrimaryKey());
        crit.addJoin(ScarabUserImplPeer.USER_ID, TurbineUserGroupRolePeer.USER_ID);
        ScarabUser[] scarabUsers = null;
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
        return scarabUsers;
    }


    /**
     * Get a list of <code>ModuleEntity</code>'s that where a user has
     * at least one of the permissions given.
     *
     * @param user a <code>ScarabUser</code> value
     * @param permissions a <code>String[]</code> value
     * @return a <code>ModuleEntity[]</code> value
     */
    public ModuleEntity[] getModules(ScarabUser user, String[] permissions)
    {        
        Criteria crit = new Criteria();
        crit.setDistinct();
        crit.addIn(TurbinePermissionPeer.PERMISSION_NAME, permissions);
        crit.addJoin(TurbinePermissionPeer.PERMISSION_ID, 
                     TurbineRolePermissionPeer.PERMISSION_ID);
        crit.addJoin(TurbineRolePermissionPeer.ROLE_ID, 
                     TurbineUserGroupRolePeer.ROLE_ID);
        crit.add(TurbineUserGroupRolePeer.USER_ID, user.getUserId());
        crit.addJoin(ScarabModulePeer.MODULE_ID, 
                     TurbineUserGroupRolePeer.GROUP_ID);

        ModuleEntity[] modules = null;
        try
        {
            List scarabModules = ScarabModulePeer.doSelect(crit);
            modules = new ModuleEntity[modules.length];
            for ( int i=scarabModules.size()-1; i>=0; i--) 
            {
                modules[i] = (ModuleEntity)scarabModules.get(i);
            }
        }
        catch (Exception e)
        {
            Log.error("An exception prevented retrieving any modules", e);
        }
        return modules;
    }
}
