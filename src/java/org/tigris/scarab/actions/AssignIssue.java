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

import java.util.Iterator;
import java.util.List;
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

import org.apache.commons.util.SequencedHashtable;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.NumberKey;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.template.TemplateEmail;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.services.user.UserManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.AttributeValuePeer;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.TransactionTypePeer;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabLink;

/**
 * This class is responsible for report issue forms.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: AssignIssue.java,v 1.31 2002/01/22 00:53:23 jon Exp $
 */
public class AssignIssue extends RequireLoginFirstAction
{
    private static final String ERROR_MESSAGE = "More information was " +
                                "required to submit your request. Please " +
                                "scroll down to see error messages."; 

    public void doSavevalues(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String id = data.getParameters().getString("id");
        Issue issue = scarabR.getIssue(id);
        boolean isChanged = false;

        if (intake.isAllValid())
        {
            List userAttrs = issue.getModule()
                             .getUserAttributes(issue.getIssueType(), true);
            for (int i=0; i < userAttrs.size(); i++)
            {
                Attribute attribute = (Attribute)userAttrs.get(i);
                List attVals = issue.getAttributeValues(attribute);
                for (int j=0; j < attVals.size(); j++)
                {
                    AttributeValue attVal = (AttributeValue)attVals.get(j);
                    Group group = intake.get("AttributeValue", 
                                             attVal.getQueryKey(), false);
                    if (group != null)
                    {
                        isChanged = false;
                        String action = null;
                        String emailAction = null;

                        // Save attachment
                        Attachment attachment = new Attachment();

                        ScarabUser assignee = scarabR.getUser(attVal.getUserId());
                        Field deleted = group.get("Deleted");
                        String oldAttributeId = attVal.getAttributeId().toString();
                        String newAttributeId = group.get("AttributeId").toString();
                        Attribute oldAttribute = (Attribute)AttributePeer.
                                  retrieveByPK(new NumberKey(oldAttributeId));
                        Attribute newAttribute = (Attribute)AttributePeer.
                                  retrieveByPK(new NumberKey(newAttributeId));

                        if (deleted.toString().equals("true"))
                        {
                            StringBuffer buf1 = new StringBuffer("You have been "
                                                                 + "removed from ");
                            buf1.append(oldAttribute.getName()).append(".");
                            emailAction = buf1.toString();
                             
                            StringBuffer buf2 = new StringBuffer("Deleted user ");
                            buf2.append(assignee.getUserName()).append(" from ");
                            buf2.append(oldAttribute.getName());
                            action = buf2.toString();
                            attachment.setName(action);
                            isChanged = true;
                        }
                        else if (!newAttributeId.equals(oldAttributeId))
                        {
                            // Check to see if user is already assigned to 
                            // New selected attribute
                            Criteria crit = new Criteria()
                               .add(AttributeValuePeer.ISSUE_ID, issue.getIssueId())
                               .add(AttributeValuePeer.VALUE, assignee.getUserName())
                               .add(AttributeValuePeer.ATTRIBUTE_ID, newAttributeId);
                            if (!issue.getAttributeValues(crit).isEmpty())
                            {
                                data.setMessage("User " + assignee.getUserName() + 
                                                " is already assigned to attribute " +
                                                newAttribute.getName() + ".");
                                intake.remove(group);
                            }     
                            else
                            {
                                StringBuffer buf1 = new StringBuffer("You have been "
                                                                     + "switched from ");
                                buf1.append(oldAttribute.getName()).append(" to ");
                                buf1.append(newAttribute.getName()).append(".");
                                emailAction = buf1.toString();

                                StringBuffer buf2 = new StringBuffer("Switched user");
                                buf2.append(assignee.getUserName()).append(" from ");
                                buf2.append(oldAttribute.getName()).append(" to ");
                                buf2.append(newAttribute.getName());
                                action = buf2.toString();
                                attachment.setName(action);
                                isChanged = true;
                            }
                        }
         
                        if (isChanged)
                        {
                             attachment.setTextFields(user, issue, 
                                        Attachment.MODIFICATION__PK);
                             attachment.save();

                             // Save transaction record
                             Transaction transaction = new Transaction();
                             transaction.create(TransactionTypePeer
                                                .EDIT_ISSUE__PK, 
                                                user, attachment);
                             attVal.startTransaction(transaction);

                             // Save assignee value
                             group.setProperties(attVal);
                             attVal.save();
                             data.getParameters().add("isChanged", "true");
                             emailAssignIssueToUser(issue, assignee, emailAction, context); 
                        }
                    }
                }
            }
        }
        else
        {
           data.setMessage(ERROR_MESSAGE);
        }
    }

    public void doAdd(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        List issues = scarabR.getIssues();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attributeId;
        String userId;
        String action;
        String emailAction;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("add_user"))
            {
                userId = key.substring(9);
                ScarabUser user = scarabR.getUser(new NumberKey(userId));
                attributeId = params.get("user_attr_" + userId);
              
                if (attributeId == null)
                {
                    data.setMessage("Missing user attribute data.");
                    return;
                } 
                else if (userId == null)
                {
                    data.setMessage("Missing user data.");
                    return;
                }
                else
                {
                    Attribute attribute = (Attribute)AttributePeer
                            .retrieveByPK(new NumberKey(attributeId));
                    action = ("Added user " + user.getUserName()
                                       + " to " + attribute.getName());
                    emailAction = ("You have been added to " + attribute.getName());

                    for (int k=0; k < issues.size(); k++)
                    {
                        UserAttribute attVal = new UserAttribute();
                        Issue issue = (Issue)issues.get(k);
                        // Save attachment
                        Attachment attachment = new Attachment();
                        attachment.setTextFields(user, issue, 
                                                 Attachment.MODIFICATION__PK);
                        attachment.setName(action);
                        attachment.save();

                        // Save transaction record
                        Transaction transaction = new Transaction();
                        transaction.create(TransactionTypePeer.EDIT_ISSUE__PK, 
                                           user, attachment);
                        attVal.startTransaction(transaction);
                
                        // Save user value
                        boolean alreadyAssigned = false;
                        List assignees = issue.getAttributeValues(attribute);
                        for (int j=0; j < assignees.size(); j++)
                        {
                            AttributeValue assigneeAttVal = (AttributeValue)
                            assignees.get(j);
                            if (assigneeAttVal.getUserId().toString().equals(userId))
                            {
                                alreadyAssigned = true;
                            }
                        }
                        if (!alreadyAssigned)
                        {
                            attVal.setIssue(issue);
                            attVal.setAttributeId(new NumberKey(attributeId));
                            attVal.setUserId(new NumberKey(userId));
                            attVal.setValue(user.getUserName());
                            attVal.save();
                            data.getParameters().add("isChanged", "true");
                            emailAssignIssueToUser(issue, user, action, context);
                        }
                    }
                }
            }
        }
    }

    /**
     * Takes care of giving an email notice about an issue to a list of users 
     * with a comment.
     *
     * @param issue a <code>Issue</code> to notify users about being assigned to.
     * @param users a <code>ScarabUser</code> user to be notified.
     * @param comment <code>String</code>
     * @param context <code>TemplateContext</code>
     */
    private void emailAssignIssueToUser(Issue issue, ScarabUser su,
                                        String action, TemplateContext context)
        throws Exception
    {

        if (issue == null || su == null)
        {
            return;
        }

        context.put("issue", issue);
        context.put("action", action);

        TemplateEmail te = new TemplateEmail();
        te.setContext(new ContextAdapter(context));
        te.setTo(su.getFirstName() + " " + su.getLastName(), su.getEmail());
        //te.setCC(su.getFirstName() + " " + su.getLastName(), su.getEmail());
        te.setFrom(
            Turbine.getConfiguration()
                    .getString("scarab.email.assignissue.fromName",
                               "Scarab System"),
            Turbine.getConfiguration()
                .getString("scarab.email.assignissue.fromAddress",
                               "help@scarab.tigris.org"));
        te.setSubject("Assign Issue " + "[" + issue.getUniqueId() + "]");
        te.setTemplate(
            Turbine.getConfiguration()
                   .getString("scarab.email.assignissue.template",
                               "email/AssignIssue.vm"));
        te.send();
    }
}
