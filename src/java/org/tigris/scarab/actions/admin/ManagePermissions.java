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
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.util.EntityExistsException;


// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;

/**
 * This class is responsible for dealing with the permission management
 * Action(s).
 *
 * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
 * @version $Id: ManagePermissions.java,v 1.7 2003/02/04 11:26:00 jon Exp $
 */
public class ManagePermissions extends RequireLoginFirstAction
{
    
    /**
     * 
     */
    public void doGotoaddpermission(RunData data, TemplateContext context)
        throws Exception
    {
        setTarget(data, "admin,AddPermission.vm");
    }
    
    /**
     * 
     */
    public void doGotodeletepermission(RunData data, TemplateContext context)
        throws Exception
    {
        setTarget(data, "admin,DeletePermission.vm");
    }
    
    /** 
     * Manages the adding of a new role when the 'Add Role' button is pressed.
     */
    public void doAddpermission(RunData data, TemplateContext context)
        throws Exception
    {
         IntakeTool intake = getIntakeTool(context);
        
        if (intake.isAllValid())
        {
            Object user = data.getUser().getTemp(ScarabConstants.SESSION_REGISTER);
            
            Group editPermission = null;
            if (user != null && user instanceof ScarabUser)
            {
                editPermission = intake.get("EditPermission", ((ScarabUser)user).getQueryKey(), false);
            }
            else
            {
                editPermission = intake.get("EditPermission", IntakeTool.DEFAULT_KEY, false);
            }
            String name = editPermission.get("PermissionName").toString();
            
            try
            {        
                Permission permission = TurbineSecurity.getNewPermission(null);
                permission.setName(name);
                
                TurbineSecurity.addPermission(permission);
                data.getParameters().setString("lastAction","addedpermission");
                getScarabRequestTool(context).setConfirmMessage("SUCCESS: a new permission was created [permission: " + name +"]");
                
            }
            catch (EntityExistsException eee)
            {
                getScarabRequestTool(context).setAlertMessage(
                    "A permission already exists with that name: " + name);
                data.getParameters().setString("lastAction","");
            }
        }       
    }
    
    /**
     * This manages the clicking of the 'Confirm Delete' button and actually
     * deletes the Permission.
     */
    public void doDeletepermission(RunData data, TemplateContext context)
        throws Exception
    {
        String name = data.getParameters().getString("name");
        Permission permission = TurbineSecurity.getPermission(name);    
        TurbineSecurity.removePermission(permission);
        
        getScarabRequestTool(context).setConfirmMessage("SUCCESS: the " + name + " permission was deleted.");
        setTarget(data, data.getParameters().getString(ScarabConstants.NEXT_TEMPLATE, "admin,ManagePermissions.vm"));
        
    }
    
    
    /**
     * This manages clicking the Cancel button
     */
    public void doCancel(RunData data, TemplateContext context) throws Exception
    {
        setTarget(data, data.getParameters()
                      .getString(ScarabConstants.CANCEL_TEMPLATE, "admin,AdminIndex.vm"));
    }
    
    /**
     * calls doCancel()
     */
    public void doPerform(RunData data, TemplateContext context)
        throws Exception
    {
        doCancel(data,context);
    }
}

