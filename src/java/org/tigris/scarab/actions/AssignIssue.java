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
import org.tigris.scarab.om.Issue;
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
 * @version $Id: AssignIssue.java,v 1.67 2002/10/28 23:16:22 jon Exp $
 */
public class AssignIssue extends BaseModifyIssue
{
    private static final String ADD_USER = "add_user";
    private static final int ADD_USER_LENGTH = ADD_USER.length() + 1;
    private static final String REMOVE_USER = "remove_user";
    private static final int REMOVE_USER_LENGTH = REMOVE_USER.length() + 1;

    private static final String TEMP = "temp";
    private static final String FINAL = "final";

    private static final String TEMP_LIST_CONTEXT = "tempList";

    /**
     * Adds users to temporary working list.
     */
    public void doAdd(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        if (user.hasPermission(ScarabSecurity.ISSUE__ASSIGN, 
                               user.getCurrentModule()))
        {
            List tempList = getWorkingList(data, TEMP);
            ValueParser params = data.getParameters();
            Object[] keys =  params.getKeys();
            for (int i =0; i<keys.length; i++)
            {
                String key = keys[i].toString();
                if (key.startsWith(ADD_USER))
                {
                    List pair = new ArrayList();
                    String userId = key.substring(ADD_USER_LENGTH);
                    String attrId = params.get("user_attr_" + userId);
                    pair.add(attrId);
                    pair.add(userId);
                    tempList.add(pair);
                }
            }
            context.put(TEMP_LIST_CONTEXT, tempList);
        }
        else 
        {
            data.setTarget(user.getHomePage());
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
        }
    }
        
    /**
     * Removes users from temporary working list.
     */
    public void doRemove(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        if (user.hasPermission(ScarabSecurity.ISSUE__ASSIGN, 
                               user.getCurrentModule()))
        {
            List tempList = getWorkingList(data, TEMP);
            ValueParser params = data.getParameters();
            Object[] keys =  params.getKeys();
            for (int i =0; i<keys.length; i++)
            {
                String key = keys[i].toString();
                if (key.startsWith(REMOVE_USER))
                {
                    List pair = new ArrayList();
                    String userId = key.substring(REMOVE_USER_LENGTH);
                    String attrId = params.get("temp_user_attr_" + userId);
                    pair.add(attrId);
                    pair.add(userId);
                    tempList.remove(pair);
                }
            }
            context.put(TEMP_LIST_CONTEXT, tempList);
        }
        else 
        {
            data.setTarget(user.getHomePage());
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
        }
    }

    /**
     * Adds or removes users, sends email, and return to previous page.
     */
    public void doDone(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        if (user.hasPermission(ScarabSecurity.ISSUE__ASSIGN, 
                               user.getCurrentModule()))
        {
            commitAssigneeChanges(data, context, scarabR);
            ScarabCache.clear();
        }
        else 
        {
            data.setTarget(user.getHomePage());
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
        }
    }

    private void commitAssigneeChanges(RunData data, TemplateContext context,
                                       ScarabRequestTool scarabR)
        throws Exception
    {
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        List issues = scarabR.getIssues();
        List finalList = getWorkingList(data, FINAL);
        String action = null;
        ScarabUser assigner = (ScarabUser)data.getUser();
        String reason = data.getParameters().getString("reason", "");

        for (int i=0; i < issues.size(); i++)
        {
            Issue issue = (Issue)issues.get(i);
            List oldAssignees = issue.getUserAttributeValues();
           
            // loops through users in temporary working list
            for (int j=0; j<finalList.size();j++)
            {
                List pair = (List)finalList.get(j);
                String attrId = (String)pair.get(0);
                String assigneeId = (String)pair.get(1);
                ScarabUser assignee = scarabR.getUser(new NumberKey(assigneeId));
                Attribute newUserAttribute = AttributeManager
                    .getInstance(new NumberKey(attrId));
                boolean alreadyAssigned = false;

                for (int k=0; k < oldAssignees.size(); k++)
                {
                    AttributeValue oldAttVal = (AttributeValue)oldAssignees.get(k);
                    // ignore already assigned users
                    if (assigneeId.equals(oldAttVal.getUserId().toString()))
                    {
                        // unless user has different attribute id, then
                        // switch their user attribute
                        alreadyAssigned = true;
                        if (!attrId.equals(oldAttVal.getAttributeId().toString()))
                        {
                            action = issue.doChangeUserAttributeValue(
                                assignee, assigner, oldAttVal, 
                                newUserAttribute, reason);

                            if (!notify(context, issue, assignee, assigner, 
                                        action))
                            {
                                scarabR.setAlertMessage(l10n.get(EMAIL_ERROR));
                            }
                        }
                    }
                }
                // if user was not already assigned, assigned them
                if (!alreadyAssigned)
                {
                    String attrDisplayName = issue.getModule()
                       .getRModuleAttribute(newUserAttribute, issue.getIssueType())
                       .getDisplayValue();
                    Object[] args = {
                        assigner.getUserName(),
                        assignee.getUserName(),
                        attrDisplayName
                    };
                    action = Localization.format(
                        ScarabConstants.DEFAULT_BUNDLE_NAME,
                        Locale.getDefault(),
                        "AssignIssueEmailAddedUserAction", args);
                    issue.assignUser(assignee, assigner, action, 
                                     newUserAttribute, reason);
                    
                    // Notification email
                    if (!notify(context, issue, assignee, assigner, action))
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
                for (int n=0; n<finalList.size();n++)
                {
                    List pair = (List)finalList.get(n);
//                    String attrId = (String)pair.get(0);
                    String assigneeId = (String)pair.get(1);
                    if (assigneeId.equals(oldAttVal.getUserId().toString()))
                    {
                         userStillAssigned = true;
                    }
                }
                if (!userStillAssigned)
                {
                    ScarabUser assignee = scarabR.getUser(oldAttVal.getUserId());
                    // delete the user
                    issue.deleteUser(assignee, assigner, oldAttVal, reason);
                    String attrDisplayName = issue.getModule()
                       .getRModuleAttribute(oldAttVal.getAttribute(), issue.getIssueType())
                       .getDisplayValue();
                    Object[] args = {
                        assigner.getUserName(),
                        assignee.getUserName(),
                        attrDisplayName
                    };
                    action = Localization.format(
                        ScarabConstants.DEFAULT_BUNDLE_NAME,
                        Locale.getDefault(),
                        "AssignIssueEmailRemovedUserAction", args);
                    if (!notify(context, oldAttVal.getIssue(), assignee, 
                                assigner, action))
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
     * Gets temporary working list of assigned users.
     */
    private List getWorkingList(RunData data,
                                String whichList)
        throws Exception
    {
        List workingList =  new ArrayList();
        ValueParser params = data.getParameters();
        Object[] keys =  params.getKeys();
        String key = "temp_user_attr_";
        if (whichList.equals(FINAL))
        {
            key = "finl_user_attr_";
        }
        for (int i =0; i<keys.length; i++)
        {
            String tempKey = keys[i].toString();
            if (tempKey.startsWith(key))
            {
                List pair = new ArrayList();
                String userId = tempKey.substring(15);
                String attrId = params.getString(tempKey);
                pair.add(attrId);
                pair.add(userId);
                workingList.add(pair);
            }
        }
        return workingList;
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
                           String action)     
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
        context.put("action", action);
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
}
