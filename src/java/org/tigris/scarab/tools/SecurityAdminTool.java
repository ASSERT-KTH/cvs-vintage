package org.tigris.scarab.tools;

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

import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.util.AccessControlList;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.UnknownEntityException;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;

/*
 import java.util.List;
 import java.util.ArrayList;


 import org.apache.velocity.app.FieldMethodizer;

 import org.tigris.scarab.om.AttributePeer;
 import org.tigris.scarab.om.IssueTypePeer;


 import org.apache.torque.util.Criteria;
 */

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
 * @version $Id: SecurityAdminTool.java,v 1.1 2001/12/04 15:53:48 dr Exp $
 */
public class SecurityAdminTool implements SecurityAdminScope
{
    public void init(Object data)
    {
    }
    
    public void refresh()
    {
    }
    
    
    
    /** Returns a User object retrieved by specifying the username.
     *
     * @param username the username of the user to retrieve
     * @returns the specified user, if found, or null otherwise
     * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
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
        }
        catch (DataBackendException dbe)
        {          
        }
        
        return (user);
    }
    
    /** Returns a Role object retrieved by specifying the name of the role.
     *
     * @param name the name of the role to retrieve
     * @returns the specified Role, if found, or null otherwise
     * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
     */
    public Role getRoleByName(String name) throws Exception
    {
        Role role = null;
        role = (Role)TurbineSecurity.getRole(name);   
        
        return (role);
    }
    
    /** 
     * Gets a list of all Groups
     */
    public Group[] getGroups() throws Exception
    {
        return TurbineSecurity.getAllGroups().getGroupsArray();
    }
    
    /** 
     * Gets a list of all Permissions
     */
    public Permission[] getPermissions() throws Exception
    {
        return (TurbineSecurity.getAllPermissions().getPermissionsArray());
    }
    
    /** 
     * Gets a list of all Roles.
     */
    public Role[] getRoles() throws Exception
    {
        return TurbineSecurity.getAllRoles().getRolesArray();
    }
    
    /**
     * Gets an ACL object for a user
     */
    public AccessControlList getACL(ScarabUser user) throws Exception
    {
        return TurbineSecurity.getACL(user);
    }    
    
}


