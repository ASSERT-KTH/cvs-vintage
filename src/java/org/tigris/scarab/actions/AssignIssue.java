package org.tigris.scarab.actions;

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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.modules.ContextAdapter;

import org.apache.torque.om.NumberKey;
import org.apache.fulcrum.util.parser.ValueParser;
import org.apache.fulcrum.localization.Localization;

// Scarab Stuff
import org.tigris.scarab.actions.base.BaseModifyIssue;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.services.security.ScarabSecurity;

/**
 * This class is responsible for assigning users to attributes.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: AssignIssue.java,v 1.69 2002/11/05 19:45:40 elicia Exp $
 */
public class AssignIssue extends BaseModifyIssue
{
    private static final String ADD_USER = "add_user";
    private static final int ADD_USER_LENGTH = ADD_USER.length() + 1;
    private static final String REMOVE_USER = "remove_user";
    private static final int REMOVE_USER_LENGTH = REMOVE_USER.length() + 1;

    //private static final String TEMP = "temp";
    //private static final String FINAL = "final";

    //private static final String TEMP_LIST_CONTEXT = "tempList";

        
    /**
     * Adds users to temporary working list.
     */
    public void doAdd(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        HashMap userMap = user.getAssociatedUsersMap();
        ValueParser params = data.getParameters();
        Object[] keys =  params.getKeys();
        for (int i =0; i<keys.length; i++)
        {
            String key = keys[i].toString();
            if (key.startsWith(ADD_USER))
            {
                List item = new ArrayList();
                String userId = key.substring(ADD_USER_LENGTH);
                String attrId = params.get("user_attr_" + userId);
                Attribute attribute = null;
                ScarabUser su = null;
                try
                {
                    attribute = AttributeManager.getInstance(new NumberKey(attrId));
                    su = ScarabUserManager.getInstance(new NumberKey(userId));
                }
                catch (Exception e)
                {
                    scarabR.setAlertMessage(e.getMessage());
                }
                item.add(attribute);
                item.add(su);
                List issues = scarabR.getIssues();
                for (int j=0; j<issues.size(); j++)
                {
                    Issue issue = (Issue)issues.get(j);
                    NumberKey issueId = issue.getIssueId();
                    List userList = (List)userMap.get(issueId);
                    if (userList == null)
                    {
                        userList = new ArrayList();
                    }
                    userList.add(item);
                    userMap.put(issueId, userList);
                    user.setAssociatedUsersMap(userMap);
                }
            }
        }
    }
        
    /**
     * Removes users from temporary working list.
     */
    private void remove(RunData data, TemplateContext context, NumberKey issueId) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        HashMap userMap = user.getAssociatedUsersMap();
        List userList = (List)userMap.get(issueId);
        ValueParser params = data.getParameters();
        Object[] keys =  params.getKeys();
        for (int i =0; i<keys.length; i++)
        {
            String key = keys[i].toString();
            if (key.startsWith(REMOVE_USER))
            {
                List item = new ArrayList();
                String userId = key.substring(REMOVE_USER_LENGTH);
                String attrId = params.get(key);
                Attribute attribute = null;
                ScarabUser su = null;
                try
                {
                    attribute = AttributeManager.getInstance(new NumberKey(attrId));
                    su = ScarabUserManager.getInstance(new NumberKey(userId));
                }
                catch (Exception e)
                {
                    scarabR.setAlertMessage(e.getMessage());
                }
                item.add(attribute);
                item.add(su);
                userList.remove(item);
                userMap.put(issueId, userList);
                user.setAssociatedUsersMap(userMap);
            }
        }
    }

    /**
     * Changes the user attribute a user is associated with.
     */
    private void update(RunData data, TemplateContext context, NumberKey issueId) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        HashMap userMap = user.getAssociatedUsersMap();
        List userList = (List)userMap.get(issueId);
        ValueParser params = data.getParameters();
        Object[] keys =  params.getKeys();
        for (int i =0; i<keys.length; i++)
        {
            String key = keys[i].toString();
            if (key.startsWith(REMOVE_USER))
            {
                List item = new ArrayList();
                List newItem = new ArrayList();
                String userId = key.substring(REMOVE_USER_LENGTH);
                String attrId = params.get(key);
                Attribute attribute = null;
                ScarabUser su = null;
                try
                {
                    attribute = AttributeManager.getInstance(new NumberKey(attrId));
                    su = ScarabUserManager.getInstance(new NumberKey(userId));
                }
                catch (Exception e)
                {
                    scarabR.setAlertMessage(e.getMessage());
                }
                item.add(attribute);
                item.add(su);
                userList.remove(item);

                String newKey = "asso_user_{" + userId + "}_issue_{" + issueId + "}";
                String newAttrId = params.get(newKey);
                Attribute newAttribute = AttributeManager
                     .getInstance(new NumberKey(newAttrId));
                newItem.add(newAttribute);
                newItem.add(su);
                userList.add(newItem);
                userMap.put(issueId, userList);
                user.setAssociatedUsersMap(userMap);
            }
        }
    }

    private void commitAssigneeChanges(RunData data, TemplateContext context,
                                       ScarabRequestTool scarabR)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        List issues = scarabR.getIssues();
        HashMap userMap = user.getAssociatedUsersMap();
        String actionString = null;
        ScarabUser assigner = (ScarabUser)data.getUser();
        String reason = data.getParameters().getString("reason", "");
        ActivitySet activitySet = new ActivitySet();

        for (int i=0; i < issues.size(); i++)
        {
            Issue issue = (Issue)issues.get(i);
            IssueType issueType = issue.getIssueType();
            Module module = issue.getModule();
            List userList = (List)userMap.get(issue.getIssueId());
            List oldAssignees = issue.getUserAttributeValues();
           
            // loops through users in temporary working list
            for (int j=0; j<userList.size();j++)
            {
                List item = (List)userList.get(j);
                Attribute newAttr = (Attribute)item.get(0);
                ScarabUser assignee = (ScarabUser)item.get(1);
                NumberKey assigneeId = assignee.getUserId();
                boolean alreadyAssigned = false;

                for (int k=0; k < oldAssignees.size(); k++)
                {
                    AttributeValue oldAttVal = (AttributeValue)oldAssignees.get(k);
                    Attribute oldAttr = oldAttVal.getAttribute();
                    // ignore already assigned users
                    if (assigneeId.equals(oldAttVal.getUserId()))
                    {
                        // unless user has different attribute id, then
                        // switch their user attribute
                        alreadyAssigned = true;
                        if (!newAttr.getAttributeId().equals(oldAttr.getAttributeId()))
                        {
                            activitySet = issue.changeUserAttributeValue(
                                                  activitySet,
                                                  assignee, assigner, 
                                                  oldAttVal, newAttr, reason);
                            actionString = issue.getUserAttributeChangeString(
                                                  assignee, assigner,
                                                  oldAttr,
                                                  newAttr);

                            if (!notify(context, issue, assignee, assigner, 
                                        actionString))
                            {
                                scarabR.setAlertMessage(l10n.get(EMAIL_ERROR));
                            }
                        }
                    }
                }
                // if user was not already assigned, assign them
                if (!alreadyAssigned)
                {
                    activitySet = issue.assignUser(activitySet, assignee, assigner,  
                                                   newAttr, reason);
                    
                    // Notification email
                    actionString = issue.getAssignUserChangeString(assigner, 
                                                                   assignee, newAttr);
                    if (!notify(context, issue, assignee, assigner, actionString))
                    {
                         scarabR.setAlertMessage(l10n.get(EMAIL_ERROR));
                    }
                }
            }
            // loops thru previously assigned users to find ones that
            // have been removed
            for (int m=0; m < oldAssignees.size(); m++)
            {
                boolean userStillAssigned = false;
                AttributeValue oldAttVal = (AttributeValue)oldAssignees.get(m);
                Attribute oldAttr = oldAttVal.getAttribute();
                for (int n=0; n<userList.size();n++)
                {
                    List item = (List)userList.get(n);
                    ScarabUser assignee = (ScarabUser)item.get(1);
                    if (assignee.getUserId().equals(oldAttVal.getUserId()))
                    {
                         userStillAssigned = true;
                    }
                }
                if (!userStillAssigned)
                {
                    ScarabUser assignee = scarabR.getUser(oldAttVal.getUserId());
                    // delete the user
                    activitySet = issue.deleteUser(activitySet, assignee, 
                                                   assigner, oldAttVal, reason);
                    actionString = issue.getUserDeleteString(assigner, assignee, 
                                                             oldAttr);
                    if (!notify(context, issue, assignee, 
                                assigner, actionString))
                    {
                        scarabR.setAlertMessage(l10n.get(EMAIL_ERROR));
                    }
                }
            }
                
        }

        if (issues.size() == 1)
        {
            Issue issue = (Issue)issues.get(0);
            data.getParameters().add("id", issue.getUniqueId());
        }
        
        if (scarabR.getAlertMessage() == null || 
            scarabR.getAlertMessage().length() == 0)
        {
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
        }
        // go back to the previous page...really a doDone, but...
        doCancel(data, context);
    }
     

    /**
     * Takes care of giving an email notice about an issue to a list of users 
     * with a comment.
     *
     * @param issue a <code>Issue</code> to notify users about being assigned to.
     * @param assignee a <code>ScarabUser</code> user being assigned.
     * @param assigner a <code>ScarabUser</code> user assigned.
     * @param action <code>String</code> text to email to others.
     */
    private boolean notify(TemplateContext context, Issue issue, 
                           ScarabUser assignee, ScarabUser assigner,
                           String actionString)     
        throws Exception
    {
        if (issue == null)
        {
            return false;
        }

        boolean success = true;
        Module module = issue.getModule();
        context.put("issue", issue);

        String[] replyToUser = module.getSystemEmail();
        String template = Turbine.getConfiguration().
           getString("scarab.email.assignissue.template",
                     "email/AssignIssue.vm");
        Object[] subjArgs = {
            issue.getModule().getRealName().toUpperCase(), 
            issue.getUniqueId(), assignee.getUserName()
        };
        String subject = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME,
                Locale.getDefault(),
                "AssignIssueEmailSubject", subjArgs);

        // email users associated with issue as well as the assignee
        context.put("actionString", actionString);
        List toUsers = issue.getUsersToEmail(AttributePeer.EMAIL_TO);
        List ccUsers = issue.getUsersToEmail(AttributePeer.CC_TO);
        boolean assigneeIncluded = false;
        for (int i=0; i<toUsers.size(); i++)
        {
            ScarabUser su = (ScarabUser)toUsers.get(i);
            if (su.equals(assignee))
            {
                assigneeIncluded = true;
            }
        }
        if (!assigneeIncluded) 
        {
            toUsers.add(assignee);
        }
        // assignee will be in to list so remove them from cc 
        for (int i=0; i<ccUsers.size(); i++)
        {
            ScarabUser su = (ScarabUser)ccUsers.get(i);
            if (su.equals(assignee))
            {
                ccUsers.remove(su);  
            }
        }
        
        if (!Email.sendEmail(new ContextAdapter(context), module, assigner, 
                            replyToUser, toUsers, ccUsers, subject, template))
        {
            success = false;
        }
        return success;
    }

    /**
     * Adds or removes users, sends email, and return to previous page.
     */
    public void doDone(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        commitAssigneeChanges(data, context, scarabR);
        ScarabCache.clear();
    }

    public void doPerform(RunData data, TemplateContext context) 
        throws Exception
    {
        ValueParser params = data.getParameters();
        Object[] keys =  params.getKeys();
        for (int i =0; i<keys.length; i++)
        {
            String key = keys[i].toString();
            if (key.startsWith("eventsubmit_doremove"))
            {
                String issueId = key.substring(21);
                remove(data, context, new NumberKey(issueId));
            }
            else if (key.startsWith("eventsubmit_doupdate"))
            {
                String issueId = key.substring(21);
                update(data, context, new NumberKey(issueId));
            }
        }
    }

}
