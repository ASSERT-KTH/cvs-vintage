package org.tigris.scarab.security;

/* ================================================================
 * Copyright (c) 2000 Collab.Net.  All rights reserved.
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
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.TurbineException;
import org.apache.turbine.services.security.TurbineSecurity;
import org.apache.turbine.om.security.User;
import org.apache.turbine.om.security.peer.*;

import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserPeer;

/**
 * Security wrapper around turbine's implementation
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: TurbineDBScarabSecurity.java,v 1.2 2001/05/24 02:17:25 jmcnally Exp $
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
     * Determine if the user currently interacting with the scarab
     * application has a permission within the user's currently
     * selected module.
     *
     * @param permission a <code>String</code> permission value, which should
     * be a constant in this interface.
     * @return true if the permission exists for the user within the
     * current module, false otherwise
     */
    public boolean hasPermission(String permission)
    {
        boolean hasPermission = false;
        try
        {
            Module module = ((ScarabUser)data.getUser()).getCurrentModule();
            hasPermission = hasPermission(permission, module);
        }
        catch (Exception e)
        {
            hasPermission = false;
            Log.error("Permission check failed on:" + permission, e);
        }
        return hasPermission;
    }

    /**
     * Determine if the user currently interacting with the scarab
     * application has a permission within a module.
     *
     * @param permission a <code>String</code> permission value, which should
     * be a constant in this interface.
     * @param module a <code>Module</code> value
     * @return true if the permission exists for the user within the
     * given module, false otherwise
     */
    public boolean hasPermission(String permission, Module module)
    {
        boolean hasPermission = false;
        try
        {
            String groupName = module.getDomain() + module.getName();
            hasPermission = data.getACL().hasPermission(permission, groupName);
        }
        catch (Exception e)
        {
            hasPermission = false;
            Log.error("Permission check failed on:" + permission, e);
        }
        return hasPermission;
    }

    /**
     * Determine if a user has a permission within a module.
     *
     * @param permission a <code>String</code> permission value, which should
     * be a constant in this interface.
     * @param user a <code>ScarabUser</code> value
     * @param module a <code>Module</code> value
     * @return true if the permission exists for the user within the
     * given module, false otherwise
     */
    public boolean hasPermission(String permission, 
                                 ScarabUser user, Module module)
    {
        boolean hasPermission = false;
        try
        {
            String groupName = module.getDomain() + module.getName();
            hasPermission = TurbineSecurity.getACL(user)
                .hasPermission(permission, groupName);
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
     * @param module a <code>Module</code> value
     * @return null
     */
    public ScarabUser[] getUsers(String permission, Module module)
    {
        String groupName = module.getDomain() + module.getName();
        Criteria crit = new Criteria();
        crit.add(PermissionPeer.NAME, permission);
        crit.addJoin(PermissionPeer.PERMISSION_ID, 
                     RolePermissionPeer.PERMISSION_ID);
        crit.addJoin(RolePermissionPeer.ROLE_ID, UserGroupRolePeer.ROLE_ID);
        crit.addJoin(UserGroupRolePeer.GROUP_ID, GroupPeer.GROUP_ID);
        crit.add(GroupPeer.NAME, groupName);
        crit.addJoin(ScarabUserPeer.USER_ID, UserGroupRolePeer.USER_ID);
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
}
