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


// Turbine
import org.apache.turbine.util.Log;

// Helm
/*
import org.tigris.helm.om.Project;
import org.tigris.helm.om.ProjectPeer;
import org.tigris.helm.om.User;
import org.tigris.helm.om.UserPeer;
import org.tigris.helm.om.RolePeer;
import org.tigris.helm.security.AccessControlList;
import org.tigris.helm.security.ACLException;
*/

import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.ScarabUser;

/**
 * Security wrapper around helm
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: HelmScarabSecurity.java,v 1.7 2001/08/09 20:34:11 jmcnally Exp $
*/
public class HelmScarabSecurity 
    extends DefaultScarabSecurity
{
    /**
     * does nothing
     */
    public HelmScarabSecurity()
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
        /*
        try
        {
            //assumes ScarabUser's db pk will be same as helm User
            User helmUser = UserPeer.getInstance( 
                user.getPrimaryKey().toString() );
            //assumes Module's db pk will be same as helm Project
            Project project = ProjectPeer
                .getInstance( module.getPrimaryKey().toString() );
            AccessControlList acl = AccessControlList.getACL(helmUser);
            try
            {
                acl.assertPermission(permission, project);
                hasPermission = true;
            }
            catch (ACLException e)
            {
                hasPermission = false;
            }
        }
        catch (Exception e)
        {
            hasPermission = false;
            Log.error("Permission check failed on:" + permission, e);
        }
        */
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
        /*
        //assumes Module's db pk will be same as helm Project
        Project project = ProjectPeer
            .getInstance( module.getPrimaryKey().toString() );
        // note the following code is just a stab in the dark
        Vector roleIds = RolePeer.getRoleIDsWithAction(permission);
        // copy code from Project.getUsersWithRoles() since it is private
        Vector myIDs = project.getEffectiveProjectIDs();
        Criteria c = new Criteria(4)
            .addJoin(UserPeer.USER_ID, UserRoleProjectPeer.USER_ID)
            .addIn(UserRoleProjectPeer.ROLE_ID, roleIDs)
            .addIn(UserRoleProjectPeer.PROJECT_ID, myIDs);
        c.setDistinct();
        List usersAndGroups = UserPeer.doSelect(c);
        List effectiveUsers = new ArrayList(usersAndGroups.size());
        List baseUsers = new ArrayList(usersAndGroups.size());
        for (Iterator i = usersAndGroups.iterator(); i.hasNext();)
        {
            User u = (User)i.next();
            effectiveUsers.addAll(u.expand());            
        }
        for (Iterator i = effectiveUsers.iterator(); i.hasNext();)
        {
            User u = (User)i.next();
            if (u.isBase())
            {
                baseUsers.add(u);
            }
        }
        //loop over baseUsers getting pk's and put together an IN
        // query to get ScarabUsers
        return scarabUsers;
        */
        return null;
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
        return null;
    }
}
