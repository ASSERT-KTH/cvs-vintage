package org.tigris.scarab.actions.admin;

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

// Turbine Stuff
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.PermissionSet;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;

/**
 * This class is responsible for dealing with the role management
 * Action(s).
 *
 * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
 * @version $Id: ManageRoles.java,v 1.9 2003/02/04 11:26:00 jon Exp $
 */
public class ManageRoles extends RequireLoginFirstAction
{
    
    /**
     * 
     */
    public void doGotoaddrole(RunData data, TemplateContext context)
        throws Exception
    {
        setTarget(data, "admin,AddRole.vm");
    }
    
    /**
     * 
     */
    public void doGotoeditrole(RunData data, TemplateContext context)
        throws Exception
    {
        setTarget(data, "admin,EditRole.vm");
    }
    
    /**
     * 
     */
    public void doGotodeleterole(RunData data, TemplateContext context)
        throws Exception
    {
        setTarget(data, "admin,DeleteRole.vm");
    }
    
    /** 
     * Manages the adding of a new role when the 'Add Role' button is pressed.
     */
    public void doAddrole(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        
        if (intake.isAllValid())
        {
            Object user = data.getUser().getTemp(ScarabConstants.SESSION_REGISTER);
            
            Group editRole = null;
            if (user != null && user instanceof ScarabUser)
            {
                editRole = intake.get("EditRole", ((ScarabUser)user).getQueryKey(), false);
            }
            else
            {
                editRole = intake.get("EditRole", IntakeTool.DEFAULT_KEY, false);
            }
            String name = editRole.get("RoleName").toString();
            
            try
            {        
                Role role = TurbineSecurity.getNewRole(null);
                role.setName(name);
                
                TurbineSecurity.addRole(role);
                data.getParameters().setString("lastAction","addedrole");
                getScarabRequestTool(context).setConfirmMessage(
                    "SUCCESS: a new role was created [role: " + name +"]");
                
            }
            catch (EntityExistsException eee)
            {
                getScarabRequestTool(context).setAlertMessage(
                    "ERROR: a role already exists with that name [role: " 
                    + name +"]");
                data.getParameters().setString("lastAction","");
            }
        }
    }
    
    /**
     * Manages the editing of an existing role when the 'Update Role' button is pressed.
     */
    public void doEditrole(RunData data, TemplateContext context)
        throws Exception
    {
        /*
         * Grab the role we are trying to update.
         */
        String name = data.getParameters().getString("name");
        Role role = TurbineSecurity.getRole(name);
        
        /*
         * Grab the permissions for the role we are
         * dealing with.
         */
        PermissionSet rolePermissions = role.getPermissions();
        
        /*
         * Grab all the permissions.
         */
        Permission[] permissions = TurbineSecurity.getAllPermissions()
            .getPermissionsArray();
        
        String roleName = role.getName();
        
        for (int i = 0; i < permissions.length; i++)
        {
            String permissionName = permissions[i].getName();
            String rolePermission = roleName + permissionName;
            
            String formRolePermission = data.getParameters().getString(rolePermission);
            Permission permission = TurbineSecurity.getPermission(permissionName);
            
            
            if (formRolePermission != null && !rolePermissions.contains(permission))
            {
                /*
                 * Checkbox has been checked AND the role doesn't already
                 * contain this permission. So assign the permission to
                 * the role.
                 */
                
                role.grant(permission);
            }
            else if (formRolePermission == null && rolePermissions.contains(permission))
            {
                /*
                 * Checkbox has not been checked AND the role
                 * contains this permission. So remove this
                 * permission from the role.
                 */
                role.revoke(permission);
            }
        }
    }
    
    /**
     * This manages the clicking of the 'Confirm Delete' button and actually
     * deletes the Role.
     */
    public void doDeleterole(RunData data, TemplateContext context)
        throws Exception
    {
        /*
         * Grab the role we are trying to delete.
         */
        String name = data.getParameters().getString("name");
        Role role = TurbineSecurity.getRole(name);
        TurbineSecurity.removeRole(role);
        
        getScarabRequestTool(context).setConfirmMessage(
            "SUCCESS: the " + name + " role was deleted.");
        setTarget(data, data.getParameters()
                      .getString(ScarabConstants.NEXT_TEMPLATE, "admin,ManageRoles.vm"));
    }
    
    
    /**
     This manages clicking the Cancel button
     */
    public void doCancel(RunData data, TemplateContext context) throws Exception
    {
        setTarget(data, data.getParameters()
                      .getString(ScarabConstants.CANCEL_TEMPLATE, "admin,AdminIndex.vm"));
    }
    
    /**
     calls doCancel()
     */
    public void doPerform(RunData data, TemplateContext context)
        throws Exception
    {
        doCancel(data,context);
    }
}

