package org.tigris.scarab.actions;

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

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.tool.IntakeTool;

import org.apache.fulcrum.intake.model.Group;

import org.apache.torque.om.NumberKey; 
import org.apache.torque.om.ObjectKey; 
import org.apache.torque.util.Criteria;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.services.module.ModuleManager;
import org.tigris.scarab.services.user.UserManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentPeer;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.RModuleOptionPeer;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.om.TransactionTypePeer;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.util.ScarabConstants;

/**
 * This class is responsible for moving/copying an issue 
 * from one module to another.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: MoveIssue.java,v 1.18 2002/01/11 00:42:47 elicia Exp $
 */
public class MoveIssue extends RequireLoginFirstAction
{

    /**
     * From MoveIssue.vm -> MoveIssue2.vm, we only need to validate the inputs.
     * Intake + Pull is so friggen cool.
     */
    public void doValidate( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            String nextTemplate = getNextTemplate(data);
            setTarget(data, nextTemplate);
        }
    }

    /**
     * Deals with moving or copying an issue from one module to
     * another module.
     * FIXME: rewrite to be more method based so that outside
     * processes can take advantage of this functionality.
     */
    public void doSaveissue( RunData data, TemplateContext context )
        throws Exception
    {
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);

        IntakeTool intake = getIntakeTool(context);
        if (!intake.isAllValid())
        {
            setTarget(data, template);
            return;
        }

        Group moveIssue = intake.get("MoveIssue",
                          IntakeTool.DEFAULT_KEY, false);
        NumberKey newModuleId = ((NumberKey) moveIssue.get("ModuleId").
            getValue());
        String selectAction = moveIssue.get("Action").toString();

        Issue issue = getScarabRequestTool(context).getIssue();
        ModuleEntity oldModule = issue.getModule();
        ScarabUser user = (ScarabUser)data.getUser();

        NumberKey newIssueId;
        Issue newIssue;
        StringBuffer descBuf = null;
        ModuleEntity newModule;
        Attachment attachment = new Attachment();

        List matchingAttributes = issue
            .getMatchingAttributeValuesList(new NumberKey(newModuleId));
        List orphanAttributes = issue
            .getOrphanAttributeValuesList(new NumberKey(newModuleId));
        Transaction transaction = new Transaction();

        // Move issue to other module
        if (selectAction.equals("move"))
        {
            // Save transaction record
            transaction.create(TransactionTypePeer.MOVE_ISSUE__PK,
                               user, null);
            newIssue = issue;
            newIssue.setModuleId(new NumberKey(newModuleId)); 
            newIssue.save();
            newModule = newIssue.getModule();
            
            // change the Issue's id prefix to match the new modules code
            newIssue.setIdPrefix(newModule.getCode());
            newIssue.save();

            // Delete non-matching attributes.
            for (int i=0;i<orphanAttributes.size();i++)
            {
               AttributeValue attVal = (AttributeValue) orphanAttributes.get(i);
               attVal.setDeleted(true);
               attVal.startTransaction(transaction);
               attVal.save();
            }
            descBuf = new StringBuffer(" moved from ");
            descBuf.append(oldModule.getName()).append(" to ");
            descBuf.append(newModule.getName());
        }
        // Copy issue to other module 
        // (no need to test, cause intake has already done that for us)
        else
        {
            // Save transaction record
            transaction.create(TransactionTypePeer.CREATE_ISSUE__PK,
                               user, null);
            newModule = ModuleManager.getInstance(new NumberKey(newModuleId));
            newIssue = newModule.getNewIssue(issue.getIssueType());
            newIssue.save();

            // Copy over attributes
            for (int i=0;i<matchingAttributes.size();i++)
            {
               AttributeValue attVal = (AttributeValue) matchingAttributes
                                                        .get(i);
               AttributeValue newAttVal = attVal.copy();
               newAttVal.setIssueId(newIssue.getIssueId());
               newAttVal.startTransaction(transaction);
               newAttVal.save();
            }
            List activityList = issue.getActivity();

            // Copy over history
            for (int i=0;i<activityList.size();i++)
            {
               Activity activity = (Activity) activityList
                                              .get(i);
               Activity newActivity = activity.copy();
               newActivity.setIssueId(newIssue.getIssueId());
               newActivity.save();
            }
            descBuf = new StringBuffer(" copied from issue ");
            descBuf.append(issue.getUniqueId());
            descBuf.append(" in module ").append(oldModule.getName());
        }

        if (!orphanAttributes.isEmpty())
        {
            // Save comment
            StringBuffer dataBuf = new StringBuffer("Removed " + 
                                                    "irrelevant attribute(s): ");
            for (int i=0;i<orphanAttributes.size();i++)
            {
               AttributeValue attVal = (AttributeValue) orphanAttributes.get(i);
               dataBuf.append(attVal.getAttribute().getName());
               String field = null;
               if (attVal.getAttribute().getAttributeType()
                   .getName().equals("combo-box"))
               {
                   field = attVal.getAttributeOption().getName();
               } 
               else if (attVal.getAttribute().getAttributeType()
                                             .getName().equals("user"))
               {
                   ScarabUser assignedUser = UserManager
                            .getInstance((ObjectKey)attVal.getUserId());
                   field = assignedUser.getUserName();
               } 
            
               dataBuf.append("=").append(field);
               if (i < orphanAttributes.size()-1 )
               {
                  dataBuf.append(",");
               } 
            }
            attachment.setDataAsString(dataBuf.toString());
            context.put("deletedAttributes", dataBuf.toString());
        }
        else
        {
            if (selectAction.equals("move"))
            {
                attachment.setDataAsString("All attributes were moved.");
            }
            else
            {
                attachment.setDataAsString("All attributes were copied.");
            }
        }
            
        if (selectAction.equals("move"))
        {
            attachment.setName("Moved Issue Note");
        }
        else
        {
            attachment.setName("Copied Issue Note");
        }
        attachment.setTextFields(user, newIssue, Attachment.MODIFICATION__PK);
        attachment.save();

        // Update transaction
        transaction.setAttachment(attachment);
        transaction.save();

        // Save activity record
        Activity activity = new Activity();
        Attribute zeroAttribute = (Attribute) AttributePeer
                                  .retrieveByPK(new NumberKey("0"));
        activity.create(newIssue, zeroAttribute, descBuf.toString(),
                        transaction, oldModule.getName(), newModule.getName());

        // placed in the context for the email to be able to access them
        context.put("action", selectAction);
        context.put("oldModule", oldModule.getName());
        context.put("newModule", newModule.getName());
        transaction.sendEmail(new ContextAdapter(context), newIssue, 
                              "issue " +  newIssue.getUniqueId() + descBuf.toString(),
                              "email/MoveIssue.vm");

        setTarget(data, nextTemplate);
    }

    /**
     * This manages clicking the Back button on MoveIssue2.vm
     */
    public void doBacktoone( RunData data, TemplateContext context ) throws Exception
    {
        setTarget(data, data.getParameters()
            .getString(ScarabConstants.CANCEL_TEMPLATE, "MoveIssue.vm"));
    }
    
} 
