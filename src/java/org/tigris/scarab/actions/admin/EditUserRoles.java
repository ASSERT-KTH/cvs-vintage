package org.tigris.scarab.actions.admin;

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


// Turbine Stuff
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.util.AccessControlList;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.actions.base.ScarabTemplateAction;


/**
 * This class is responsible for dealing with the EditUserRoles
 * Action.
 *
 * At this point this is basically a rip off of the 'Flux' code.  This should
 *   create a jump point from which to continue the development of the
 *   management of user roles.
 *
 * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
 */
public class EditUserRoles extends ScarabTemplateAction
{
    /**
     * This manages clicking the 'Update Roles' button
     */
    public void doRoles( RunData data, TemplateContext context )
        throws Exception
    {
        String username = data.getParameters().getString("username");
        User user = TurbineSecurity.getUser(username);
        
        AccessControlList acl = TurbineSecurity.getACL(user);
        
        // Grab all the Groups and Roles in the system.
        Group[] groups = TurbineSecurity.getAllGroups().getGroupsArray();
        Role[] roles = TurbineSecurity.getAllRoles().getRolesArray();
        
        for (int i = 0; i < groups.length; i++)
        {
            String groupName = groups[i].getName();
            
            for (int j = 0; j < roles.length; j++)
            {
                String roleName = roles[j].getName();
                String groupRole = groupName + roleName;
                
                String formGroupRole = data.getParameters().getString(groupRole);
                
                if ( formGroupRole != null && !acl.hasRole(roles[j], groups[i]))
                {
                    TurbineSecurity.grant(user, groups[i], roles[j]);
                }
                else if (formGroupRole == null && acl.hasRole(roles[j], groups[i]))
                {
                    TurbineSecurity.revoke(user, groups[i], roles[j]);
                }
            }
        }
    }
    
    /**
     * This manages clicking the 'Cancel' button
     */
    public void doCancel( RunData data, TemplateContext context )
        throws Exception
    {
        setTarget(data, data.getParameters()
                      .getString(ScarabConstants.CANCEL_TEMPLATE, 
                                 "admin,ManageUserSearch.vm"));
    }
    
    /**
     calls doCancel()
     */
    public void doPerform( RunData data, TemplateContext context ) throws Exception
    {
        doCancel(data, context);
    }
    
}

