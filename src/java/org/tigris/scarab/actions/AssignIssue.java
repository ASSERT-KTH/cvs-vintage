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

import org.apache.commons.util.SequencedHashtable;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.NumberKey;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.template.TemplateEmail;
import org.apache.fulcrum.template.DefaultTemplateContext;

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
import org.tigris.scarab.tools.Email;

/**
 * This class is responsible for report issue forms.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: AssignIssue.java,v 1.38 2002/02/20 20:37:43 jmcnally Exp $
 */
public class AssignIssue extends RequireLoginFirstAction
{

    private static final String EMAIL_ERROR = "Your changes were saved, " +
                                "but could not send notification email due " + 
                                "to a sendmail error.";

    public void doSavevalues(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String id = data.getParameters().getString("id");
        Issue issue = scarabR.getIssue(id);
        boolean isChanged = false;
        String userAction = null;
        String othersAction = null;

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
                            userAction = buf1.toString();
                             
                            StringBuffer buf2 = new StringBuffer("User " );
                            buf2.append(user.getUserName() + " Deleted user ");
                            buf2.append(assignee.getUserName()).append(" from ");
                            buf2.append(oldAttribute.getName());
                            othersAction = buf2.toString();
                            attachment.setName(othersAction);
                            isChanged = true;
                            // remove the user from the List and reset the 
                            // index, so the next AttributeValue is not skipped
                            attVals.remove(j--);
                        }
                        else if (!newAttributeId.equals(oldAttributeId))
                        {
                            // Check to see if user is already assigned to 
                            // New selected attribute
                            Criteria crit = new Criteria()
                               .add(AttributeValuePeer.ISSUE_ID, issue.getIssueId())
                               .add(AttributeValuePeer.VALUE, assignee.getUserName())
                               .add(AttributeValuePeer.ATTRIBUTE_ID, newAttributeId)
                               .add(AttributeValuePeer.DELETED, false);
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
                                       + "switched from attribute ");
                                buf1.append(oldAttribute.getName()).append(" to ");
                                buf1.append(newAttribute.getName()).append(".");
                                userAction = buf1.toString();

                                StringBuffer buf2 = new StringBuffer();
                                buf2.append("User " + user.getUserName());
                                buf2.append(" has switched user ");
                                buf2.append(assignee.getUserName()).append(" from ");
                                buf2.append(oldAttribute.getName()).append(" to ");
                                buf2.append(newAttribute.getName() + ".");
                                othersAction = buf2.toString();
                                attachment.setName(othersAction);
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
                             // Save activity record
                             Activity activity = new Activity();
                             activity.create(issue, attribute, othersAction, 
                                  transaction, 0, 0, null, null, null, 
                                  null, "", "");

                             // Save assignee value
                             group.setProperties(attVal);
                             attVal.save();
                             if (!notify(issue, assignee, 
                                         userAction, othersAction))
                             {
                                 data.setMessage(EMAIL_ERROR);
                             }
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
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        List issues = scarabR.getIssues();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attributeId;
        String assigneeId;
        String userAction = null;
        String othersAction = null;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("add_user"))
            {
                assigneeId = key.substring(9);
                ScarabUser assignee = scarabR.getUser(new NumberKey(assigneeId));
                attributeId = params.get("user_attr_" + assigneeId);
              
                if (attributeId == null)
                {
                    data.setMessage("Missing user attribute data.");
                    return;
                } 
                else if (assigneeId == null)
                {
                    data.setMessage("Missing user data.");
                    return;
                }
                else
                {
                    Attribute attribute = 
                        Attribute.getInstance(new NumberKey(attributeId));
                    othersAction = ("User " + user.getUserName() + " has added user " 
                              + assignee.getUserName() + " to " 
                              + attribute.getName() + ".");
                    userAction = ("You have been added to " 
                                   + attribute.getName() + ".");

                    for (int k=0; k < issues.size(); k++)
                    {
                        UserAttribute attVal = new UserAttribute();
                        Issue issue = (Issue)issues.get(k);
                        // Save attachment
                        Attachment attachment = new Attachment();
                        attachment.setTextFields(assignee, issue, 
                                                 Attachment.MODIFICATION__PK);
                        attachment.setName(othersAction);
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
                            if (assigneeAttVal.getUserId().toString().equals(assigneeId))
                            {
                                alreadyAssigned = true;
                            }
                        }
                        if (!alreadyAssigned)
                        {
                            attVal.setIssue(issue);
                            attVal.setAttributeId(new NumberKey(attributeId));
                            attVal.setUserId(new NumberKey(assigneeId));
                            attVal.setValue(assignee.getUserName());
                            attVal.save();
                            // add assignee to the List so they appear on the
                            // template which follows this action
                            assignees.add(attVal);
                            // Notification email
                            if (!notify(issue, assignee, 
                                         userAction, othersAction))
                            {
                                 data.setMessage(EMAIL_ERROR);
                            }
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
    private boolean notify(Issue issue, ScarabUser assignee,
                           String userAction, String othersAction )     
        throws Exception
    {
        if (issue == null )
        {
            return false;
        }

        DefaultTemplateContext context = new DefaultTemplateContext();
        boolean success = true;
        ModuleEntity module = issue.getModule();
        context.put("issue", issue);

        String fromUser = "scarab.email.modifyissue";
        String template = Turbine.getConfiguration().
           getString("scarab.email.assignissue.template",
                     "email/AssignIssue.vm");

        // First notify user
        context.put("action", userAction);
        String subject = "Assign Issue " + "[" + issue.getUniqueId() + "]";
        if (!Email.sendEmail(context, module, fromUser, 
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
        if (!Email.sendEmail(context, module, fromUser, 
                            toUsers, ccUsers, subject, template))
        {
            success = false;
        }
        return success;
    }

}
