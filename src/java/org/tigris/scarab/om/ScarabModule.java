package org.tigris.scarab.om;


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

import java.util.*;

import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.om.*;

import org.tigris.scarab.util.*;

import org.apache.turbine.util.security.RoleSet;
import org.apache.turbine.util.security.TurbineSecurityException;
import org.apache.turbine.om.security.*;

/**
 * Implementation of a ScarabModule. For now, we just extend Module
 * so there isn't much here.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ScarabModule.java,v 1.4 2001/05/27 06:38:02 jmcnally Exp $
 */
public class ScarabModule extends Module
    implements Group,  Comparable
{
    /**
     * Gets users which are currently associated (relationship has not 
     * been deleted) with this module with Roles specified in includeRoles
     * and excluding Roles in the exclude list. 
     *
     * @param partialUserName username fragment to match against
     * @param includeRoles a <code>Role[]</code> value
     * @param excludeRoles a <code>Role[]</code> value
     * @return a <code>List</code> of ScarabUsers
     * @exception Exception if an error occurs
     */
    public List getUsers(String partialUserName, String[] permissions)
        throws Exception
    {
        Criteria crit = new Criteria(3)
            .add(RModuleUserRolePeer.DELETED, false);
        /* 
           Criteria.Criterion c = null;
        if ( includeRoles != null ) 
        {            
            crit.addIn(RModuleUserRolePeer.ROLE_ID, includeRoles);
            c = crit.getCriterion(RModuleUserRolePeer.ROLE_ID);
        }
        if ( excludeRoles != null ) 
        {   
            if ( c == null ) 
            {
                crit.addNotIn(RModuleUserRolePeer.ROLE_ID, excludeRoles);
            }
            else 
            {
                c.and(crit
                      .getNewCriterion(RModuleUserRolePeer.ROLE_ID, 
                                       excludeRoles, Criteria.NOT_IN));
            }
        }
        if ( partialUserName != null && partialUserName.length() != 0 ) 
        {
            crit.add(ScarabUserPeer.USERNAME, 
                     (Object)("%" + partialUserName + "%"), Criteria.LIKE);
        }
        */
        List moduleRoles = getRModuleUserRolesJoinScarabUser(crit);

        // rearrange so list contains Users
        List users = new ArrayList(moduleRoles.size());
        Iterator i = moduleRoles.iterator();
        while (i.hasNext()) 
        {
            ScarabUser user = ((RModuleUserRole)i.next()).getScarabUser();
            users.add(user);
        }
        
        return users;
    }

    /**
     * Saves the module into the database
     */
    public void save() throws TurbineSecurityException
    {
        try
        {
            // if new, relate the Module to the user who created it.
            if ( isNew() ) 
            {
                RModuleUserRole relation = new RModuleUserRole();
                if ( getOwnerId() == null ) 
                {
                    throw new ScarabException(
                    "Can't save a project without first assigning an owner.");
                }         
                relation.setUserId(getOwnerId());
                // !FIXME! this needs to be set to the Module Owner Role
                relation.setRoleId(new NumberKey("1"));
                relation.setDeleted(false);
                addRModuleUserRole(relation);
            }
            super.save();
        }
        catch (Exception e)
        {
            throw new TurbineSecurityException(e.getMessage(), e);
        }
    }


    public static String getGroupFromModule(Module module)
    {
        return null;
    }

    public String getModuleNameFromGroup(Group group)
    {
        String gname = group.getName();
        int lastDash = gname.lastIndexOf('-');
        return null;

    }

    // *******************************************************************
    // Turbine Group implementation get/setName and save are defined above
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
        throw new TurbineSecurityException("Not implemented");
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
        if(this.getClass() != obj.getClass())
            throw new ClassCastException();
        String name1 = ((Group)obj).getName();
        String name2 = this.getName();

        return name2.compareTo(name1);
    }

}
