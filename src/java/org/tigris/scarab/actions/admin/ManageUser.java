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
import java.util.Calendar;
import java.util.List;

// Turbine Stuff
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.util.AccessControlList;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImpl;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabGlobalTool;

/**
 * This class is responsible for dealing with the user management
 * Action(s).
 *
 * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
 * @version $Id: ManageUser.java,v 1.15 2003/02/04 11:26:00 jon Exp $
 */
public class ManageUser extends RequireLoginFirstAction
{
    /**
     * This manages clicking the Add User button
     */
    public void doAdduser(RunData data, TemplateContext context) throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);
        ScarabUser su = null;
        
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            Object user = data.getUser()
                .getTemp(ScarabConstants.SESSION_REGISTER);
            Group register = null;
            if (user != null && user instanceof ScarabUser)
            {
                register = intake.get("Register",
                                          ((ScarabUser)user).getQueryKey(), false);
            }
            else
            {
                register = intake.get("Register",
                                      IntakeTool.DEFAULT_KEY, false);
            }
            
            su  = (ScarabUser) TurbineSecurity.getAnonymousUser();
            //su.setUserName(data.getParameters().getString("UserName"));
            su.setUserName(register.get("Email").toString());
            su.setFirstName(register.get("FirstName").toString());
            su.setLastName(register.get("LastName").toString());
            su.setEmail(register.get("Email").toString());
            su.setPassword(register.get("Password").toString().trim());
            
            if (ScarabUserImplPeer.checkExists(su))
            {
                setTarget(data, template);
                scarabR.setAlertMessage("Sorry, a user with that email address already exists!");
                data.getParameters().setString("errorLast","true");
                data.getParameters().setString("state","showadduser");
                return;
            }
            
            // if we got here, then all must be good...
            try
            {
                su.createNewUser();
                ScarabUserImpl.confirmUser(register.get("Email").toString());
                // force the user to change their password the first time they login
                su.setPasswordExpire(Calendar.getInstance());
                scarabR.setConfirmMessage("SUCCESS: a new user was created [username: " + register.get("Email").toString() +"]");
                data.getParameters().setString("state","showadduser");
                data.getParameters().setString("lastAction","addeduser");
                
                setTarget(data, nextTemplate);
                return;
            }
            catch (Exception e)
            {
                setTarget(data, template);
                data.getParameters().setString("lastAction","");
                scarabR.setAlertMessage (e.getMessage());
                Log.get().error(e);
                data.getParameters().setString("state","showadduser");
                return;
            }
        }
        else
        {
            data.getParameters().setString("state","showadduser");
            data.getParameters().setString("lastAction","");
        }
    }
    
    public void doEdituser(RunData data, TemplateContext context) throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);
        ScarabUser su = null;
        
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            Object user = data.getUser()
                .getTemp(ScarabConstants.SESSION_REGISTER);
            Group register = null;
            if (user != null && user instanceof ScarabUser)
            {
                register = intake.get("Register",
                                          ((ScarabUser)user).getQueryKey(), false);
            }
            else
            {
                register = intake.get("Register",
                                      IntakeTool.DEFAULT_KEY, false);
            }
            
            
            // if we got here, then all must be good...
            try
            {
                su = (ScarabUser) TurbineSecurity
                    .getUser(data.getParameters().getString("username"));
                if ((su != null) && (register != null))
                {
                    // update the first name, last name, email and username
                    su.setFirstName(register.get("FirstName").toString());
                    su.setLastName(register.get("LastName").toString());
                    
                    String newEmail = register.get("Email").toString();
                    if (!newEmail.equals(data.getParameters().getString("username")))
                    {
                        su.setEmail(newEmail);
                        //su.setUserName(newEmail);
                        
                        if (!ScarabUserImplPeer.checkExists(su))
                        {
                            setTarget(data, template);
                            scarabR.setAlertMessage(
                                "Sorry, a user with that email address [" + 
                                newEmail + "] already exists!");
                            data.getParameters().setString("state","showedituser");
                            return;
                        }
                    }
                    TurbineSecurity.saveUser(su);
                    
                    // only update their password if the field is non-empty, 
                    // and then make sure they change the password at next login
                    String password = data.getParameters()
                        .getString("editpassword");
                    if ((password != null) && (!password.trim().equals("")))
                    {
                        su.setPasswordExpire(Calendar.getInstance());                        
                        TurbineSecurity.saveUser(su);
                        
                        TurbineSecurity.forcePassword(su, password.trim());
                    }
                    
                    scarabR.setConfirmMessage("SUCCESS: changes to the user have " + 
                                        " been saved [username: " + 
                                        register.get("Email").toString() +"]");
                    data.getParameters().setString("state","showedituser");
                    data.getParameters().setString("lastAction","editeduser");
                    
                    setTarget(data, nextTemplate);
                    return;
                }
                else
                {
                    scarabR.setAlertMessage("ERROR: couldn't retrieve the user " + 
                                        " from the DB [username: " + 
                                        register.get("Email").toString() +"]");
                    data.getParameters().setString("state","showedituser");                    
                }
            }
            catch (Exception e)
            {
                setTarget(data, template);
                data.getParameters().setString("lastAction","");
                scarabR.setAlertMessage (e.getMessage());
                Log.get().error(e);
                data.getParameters().setString("state","showedituser");
                return;
            }
        }
        else
        {
            data.getParameters().setString("state","showedituser");
            data.getParameters().setString("lastAction","");
        }
    }
    
    public void doDeleteuser(RunData data, TemplateContext context)
        throws Exception
    {
        getScarabRequestTool(context)
        .setAlertMessage("User delete is not yet implemented. Instructions on"
                         + " implementation are given in issue# 165.  " + 
                         "deleted [username: " + data.getParameters()
                         .getString("username") +"]");
        setTarget(data, data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE, "admin,AdminIndex.vm"));
    }
    
    
    /**
     * This manages clicking the 'Update Roles' button
     */
    public void doRoles(RunData data, TemplateContext context)
        throws Exception
    {
        String username = data.getParameters().getString("username");
        User user = TurbineSecurity.getUser(username);
        
        AccessControlList acl = TurbineSecurity.getACL(user);
        
        // Grab all the Groups and Roles in the system.
        org.apache.fulcrum.security.entity.Group[] groups = TurbineSecurity.getAllGroups().getGroupsArray();
        Role[] roles = TurbineSecurity.getAllRoles().getRolesArray();
        
        for (int i = 0; i < groups.length; i++)
        {
            String groupName = groups[i].getName();
            
            for (int j = 0; j < roles.length; j++)
            {
                String roleName = roles[j].getName();
                String groupRole = groupName + roleName;
                
                String formGroupRole = data.getParameters().getString(groupRole);
                
                if (formGroupRole != null && !acl.hasRole(roles[j], groups[i]))
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
    
    // all the goto's (button redirects) are here
    
    /**
     * 
     */
    public void doGotoedituser(RunData data, TemplateContext context)
        throws Exception
    {
        String userName = data.getParameters().getString("username");
        if ((userName != null) && (userName.length() > 0))
        {
            data.getParameters().setString("state","showedituser");
            setTarget(data, "admin,EditUser.vm");
        }
        else
        {
            getScarabRequestTool(context).setAlertMessage("Please select a user first!");
        }
    }
    
    /**
     * 
     */
    public void doGotoeditroles(RunData data, TemplateContext context)
        throws Exception
    {
        String userName = data.getParameters().getString("username");
        if ((userName != null) && (userName.length() > 0))
        {
            setTarget(data, "admin,EditUserRoles.vm");
        }
        else
        {
            getScarabRequestTool(context).setAlertMessage("Please select a user first!");
        }
    }
    
    /**
     * 
     */
    public void doGotodeleteuser(RunData data, TemplateContext context)
        throws Exception
    {
        setTarget(data, "admin,DeleteUser.vm");
    }
    
    /**
     * 
     */
    public void doGotoadduser(RunData data, TemplateContext context)
        throws Exception
    {
        setTarget(data, "admin,AddUser.vm");
    }

    /**
     * This manages clicking the 'Search' button
     */
    public void doSearch(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabGlobalTool gTool = 
                (ScarabGlobalTool) context.get("scarabG");
        
        String searchField = data.getParameters().getString("searchField");
        String searchCriteria = data.getParameters().getString("searchCriteria");
        String orderByField = data.getParameters().getString("orderByField");
        String ascOrDesc = data.getParameters().getString("ascOrDesc");
        String resultsPerPage = data.getParameters().getString("resultsPerPage");
        String pageNum = data.getParameters().getString("pageNum");
        
        List users = gTool.getSearchUsers(
                searchField, searchCriteria, orderByField, ascOrDesc);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        
        scarabR.setGlobalUserSearch(users);
        scarabR.setGlobalUserSearchParam("searchField", searchField);
        scarabR.setGlobalUserSearchParam("searchCriteria", searchCriteria);
        scarabR.setGlobalUserSearchParam("orderByField", orderByField);
        scarabR.setGlobalUserSearchParam("ascOrDesc", ascOrDesc);
        scarabR.setGlobalUserSearchParam("resultsPerPage", resultsPerPage);
        scarabR.setGlobalUserSearchParam("pageNum", pageNum);
        
        setTarget(data, "admin,ManageUserSearch.vm");
    }
    
    /**
     * calls doSearch()
     */
    public void doPerform(RunData data, TemplateContext context)
        throws Exception
    {
        doSearch(data, context);
    }
    

}

