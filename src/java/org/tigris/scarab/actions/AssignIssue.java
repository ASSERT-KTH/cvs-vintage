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

import javax.mail.SendFailedException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.tool.IntakeTool;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.ParameterParser;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.NumberKey;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.template.TemplateEmail;
import org.apache.fulcrum.template.DefaultTemplateContext;
import org.apache.fulcrum.util.parser.ValueParser;

// Scarab Stuff
import org.tigris.scarab.actions.base.BaseModifyIssue;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.AttributeValuePeer;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.TransactionTypePeer;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * This class is responsible for assigning users to attributes.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: AssignIssue.java,v 1.48 2002/05/24 04:32:59 elicia Exp $
 */
public class AssignIssue extends BaseModifyIssue
{

    /**
     * Adds users to temporary working list.
     */
    public void doAdd(RunData data, TemplateContext context) 
        throws Exception
    {
        List tempList = getTempList(data, context);
        ValueParser params = data.getParameters();
        Object[] keys =  params.getKeys();
        for (int i =0; i<keys.length; i++)
        {
            String key = keys[i].toString();
            if (key.startsWith("add_user"))
            {
                List pair = new ArrayList();
                String userId = key.substring(9);
                String attrId = params.get("user_attr_" + userId);
                pair.add(attrId);
                pair.add(userId);
                tempList.add(pair);
            }
        }
        context.put("tempList", tempList);
    }
        
    /**
     * Removes users from temporary working list.
     */
    public void doRemove(RunData data, TemplateContext context) 
        throws Exception
    {
        List tempList = getTempList(data, context);

        ValueParser params = data.getParameters();
        Object[] keys =  params.getKeys();
        for (int i =0; i<keys.length; i++)
        {
            String key = keys[i].toString();
            if (key.startsWith("remove_user"))
            {
                List pair = new ArrayList();
                String userId = key.substring(12);
                String attrId = params.get("temp_user_attr_" + userId);
                pair.add(attrId);
                pair.add(userId);
                tempList.remove(pair);
            }
        }
        context.put("tempList", tempList);
    }

    /**
     * Adds or removes users, sends email, and return to previous page.
     */
    public void doDone(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        List issues = scarabR.getIssues();
        List tempList = getTempList(data, context);
        String othersAction = null;
        String userAction = null;
        Attachment attachment = null;
        ScarabUser user = (ScarabUser)data.getUser();
        String reason = data.getParameters().getString("reason", "");

        for (int i=0; i < issues.size(); i++)
        {
            Issue issue = (Issue)issues.get(i);
            List oldAssignees = issue.getUserAttributeValues();
           
            // loops through users in temporary working list
            for (int j=0; j<tempList.size();j++)
            {
                List pair = (List)tempList.get(j);
                String attrId = (String)pair.get(0);
                String assigneeId = (String)pair.get(1);
                ScarabUser assignee = scarabR.getUser(new NumberKey(assigneeId));
                Attribute attribute = AttributeManager
                    .getInstance(new NumberKey(attrId));
                boolean alreadyAssigned = false;
                boolean userSwitched = false;
            
                for (int k=0; k < oldAssignees.size(); k++)
                {
                    AttributeValue oldAttVal = (AttributeValue)oldAssignees.get(k);
                    if (assigneeId.equals(oldAttVal.getUserId().toString()))
                    {
                        alreadyAssigned = true;
                        if (!attrId.equals(oldAttVal.getAttributeId().toString()))
                        {
                            switchUser(context, assignee, user, oldAttVal.getAttribute(), 
                                       attribute, oldAttVal, reason);
                        }
                    }
                }
                // if user was not already assigned, assigned them
                if (!alreadyAssigned)
                {
                    othersAction = ("User " + user.getUserName() 
                              + " has added user " 
                              + assignee.getUserName() + " to " 
                              + attribute.getName() + ".");
                    userAction = ("You have been added to " 
                                   + attribute.getName() + ".");
                    issue.assignUser(assignee, user, othersAction, attribute, reason);
                    // add assignee to the List so they appear on the
                    // template which follows this action
                    //assignees.add(attVal);
                    // Notification email
                    if (!notify(context, issue, assignee, 
                                userAction, othersAction))
                    {
                         scarabR.setAlertMessage(EMAIL_ERROR);
                    }
                }
            }
            // loops thru previously assigned users to find ones that
            // have been removed
            for (int m=0; m < oldAssignees.size(); m++)
            {
                boolean userStillAssigned = false;
                AttributeValue oldAttVal = (AttributeValue)oldAssignees.get(m);
                for (int n=0; n<tempList.size();n++)
                {
                    List pair = (List)tempList.get(n);
                    String attrId = (String)pair.get(0);
                    String assigneeId = (String)pair.get(1);
                    if (assigneeId.equals(oldAttVal.getUserId().toString()))
                    {
                         userStillAssigned = true;
                    }
                }
                if (!userStillAssigned)
                {
                    ScarabUser assignee = scarabR.getUser(oldAttVal.getUserId());
                    deleteUser(context, assignee, user, oldAttVal, reason);
                }
            }
                
        }

        if (issues.size() == 1)
        {
            Issue issue = (Issue)issues.get(0);
            data.getParameters().add("id", issue.getUniqueId());
        }
        doCancel(data, context);
    }


    private void deleteUser(TemplateContext context, 
                            ScarabUser assignee, ScarabUser assigner,
                            AttributeValue attVal, String reason)
        throws Exception
    {
        // Create attachments and email notification text
        // For assigned user, and for other associated users
        Attribute attribute = attVal.getAttribute();
        StringBuffer buf1 = new StringBuffer("You have been "
                                             + "removed from ");
        buf1.append(attribute.getName()).append(".");
        String userAction = buf1.toString();
         
        StringBuffer buf2 = new StringBuffer("User " );
        buf2.append(assigner.getUserName() + " deleted user ");
        buf2.append(assignee.getUserName()).append(" from ");
        buf2.append(attribute.getName());
        String othersAction = buf2.toString();
        Attachment attachment = new Attachment();
        attachment.setName(othersAction);
        attachment.setDataAsString(reason);
        attachment.setTextFields(assigner, attVal.getIssue(), 
                                 Attachment.MODIFICATION__PK);
        attachment.save();

        // Save transaction record
        Transaction transaction = new Transaction();
        transaction.create(TransactionTypePeer
                           .EDIT_ISSUE__PK, 
                           assigner, attachment);
        attVal.startTransaction(transaction);

        // Save activity record
        Activity activity = new Activity();
        activity.create(attVal.getIssue(), attVal.getAttribute(),
                        othersAction, transaction, 0, 0, null, 
                       null, null, null, "", "");

        // remove the user from the List and reset the 
        // index, so the next AttributeValue is not skipped
        attVal.setDeleted(true);
        attVal.save();
        //attVal.getIssue().getAttributeValues(attribute).remove(attVal);

        if (!notify(context, attVal.getIssue(), assignee, 
                    userAction, othersAction))
        {
            getScarabRequestTool(context).setAlertMessage(EMAIL_ERROR);
        }
   }

    /**
     * Switches the attribute a user is assigned to.
     */
    private void switchUser(TemplateContext context, 
                            ScarabUser assignee, ScarabUser assigner,
                            Attribute oldAttribute, Attribute newAttribute,
                            AttributeValue attVal, String reason)
        throws Exception
    {
        Issue issue = attVal.getIssue();

        // Create attachments and email notification text
        // For assigned user, and for other associated users
        Attachment attachment = new Attachment();
        StringBuffer buf1 = new StringBuffer("You have been "
               + "switched from attribute ");
        buf1.append(oldAttribute.getName()).append(" to ");
        buf1.append(newAttribute.getName()).append(".");
        String userAction = buf1.toString();

        StringBuffer buf2 = new StringBuffer();
        buf2.append("User " + assigner.getUserName());
        buf2.append(" has switched user ");
        buf2.append(assignee.getUserName()).append(" from ");
        buf2.append(oldAttribute.getName()).append(" to ");
        buf2.append(newAttribute.getName() + ".");
        String othersAction = buf2.toString();
        attachment.setName(othersAction);
        attachment.setDataAsString(reason);
        attachment.setTextFields(assigner, issue, 
                                 Attachment.MODIFICATION__PK);
        attachment.save();

        // Save transaction record
        Transaction transaction = new Transaction();
        transaction.create(TransactionTypePeer
                           .EDIT_ISSUE__PK, 
                           assigner, attachment);
        attVal.startTransaction(transaction);

        // Save activity record
        Activity activity = new Activity();
        activity.create(issue, newAttribute, othersAction, 
                        transaction, 0, 0, null, null, null, 
                        null, "", "");

        // Save assignee value
        attVal.setAttributeId(newAttribute.getAttributeId());
        attVal.save();
        if (!notify(context, issue, assignee, 
                    userAction, othersAction))
        {
            getScarabRequestTool(context).setAlertMessage(EMAIL_ERROR);
        }
    }

    /**
     * Gets temporary working list of assigned users.
     */
    private List getTempList(RunData data, TemplateContext context) 
        throws Exception
    {
        List tempList =  new ArrayList();
        ValueParser params = data.getParameters();
        Object[] keys =  params.getKeys();
        for (int i =0; i<keys.length; i++)
        {
            String key = keys[i].toString();
            if (key.startsWith("temp_user_attr_"))
            {
                List pair = new ArrayList();
                String userId = key.substring(15);
                String attrId = params.getString(key);
                pair.add(attrId);
                pair.add(userId);
                tempList.add(pair);
            }
        }
        return tempList;
    }

    /**
     * Takes care of giving an email notice about an issue to a list of users 
     * with a comment.
     *
     * @param issue a <code>Issue</code> to notify users about being assigned to.
     * @param assignee a <code>ScarabUser</code> user being assigned.
     * @param userAction <code>String</code> text to email to the assigned user.
     * @param othersAction <code>String</code> text to email to others.
     */
    private boolean notify(TemplateContext context, Issue issue, 
                           ScarabUser assignee,
                           String userAction, String othersAction )     
        throws Exception
    {
        if (issue == null )
        {
            return false;
        }

        boolean success = true;
        Module module = issue.getModule();
        context.put("issue", issue);

        String fromUser = "scarab.email.modifyissue";
        String template = Turbine.getConfiguration().
           getString("scarab.email.assignissue.template",
                     "email/AssignIssue.vm");

        // First notify user
        context.put("action", userAction);
        String subject = "Assign Issue " + "[" + issue.getUniqueId() + "]";
        if (!Email.sendEmail(new ContextAdapter(context), module, fromUser, 
                            assignee, subject, template))
        {
            success = false;
        }

        // Then notify others associated with issue
        context.put("action", othersAction);
        subject = "[" + issue.getModule().getRealName()
                         .toUpperCase() + "] Issue #" 
                         + issue.getUniqueId() + " modified";
        List toUsers = issue.getUsersToEmail(AttributePeer.EMAIL_TO);
        List ccUsers = issue.getUsersToEmail(AttributePeer.CC_TO);
        if (!Email.sendEmail(new ContextAdapter(context), module, fromUser, 
                            toUsers, ccUsers, subject, template))
        {
            success = false;
        }
        return success;
    }

}
