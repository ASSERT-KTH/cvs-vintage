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

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.collections.ExtendedProperties;

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.RunData;

import org.apache.torque.om.NumberKey; 
import org.apache.torque.om.ObjectKey; 
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.torque.util.Criteria;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.commons.util.SequencedHashtable;
import org.apache.turbine.ParameterParser;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentPeer;
import org.tigris.scarab.om.AttachmentType;
import org.tigris.scarab.om.AttachmentTypePeer;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.om.TransactionTypePeer;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionPeer;
import org.tigris.scarab.om.Depend;
import org.tigris.scarab.om.DependPeer;
import org.tigris.scarab.om.DependType;
import org.tigris.scarab.om.DependTypePeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.apache.fulcrum.upload.FileItem;
import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.upload.TurbineUploadService;
import org.apache.fulcrum.upload.UploadService;


import org.tigris.scarab.attribute.OptionAttribute;

import org.tigris.scarab.util.ScarabConstants;

/**
    This class is responsible for edit issue forms.
    ScarabIssueAttributeValue
    @author <a href="mailto:elicia@collab.net">Elicia David</a>
    @version $Id: ModifyIssue.java,v 1.73 2002/02/06 01:17:44 jon Exp $
*/
public class ModifyIssue extends RequireLoginFirstAction
{
    private static final String ERROR_MESSAGE = "More information was " +
                                "required to submit your request. Please " +
                                "scroll down to see error messages."; 


    public void doSubmitattributes(RunData data, TemplateContext context)
        throws Exception
    {
        String id = data.getParameters().getString("id");
        Issue issue = Issue.getIssueById(id);
        IssueType issueType = getScarabRequestTool(context).getCurrentIssueType();
        ScarabUser user = (ScarabUser)data.getUser();

        IntakeTool intake = getIntakeTool(context);
       
        // Comment field is required to modify attributes
        Attachment attachment = new Attachment();
        Group commentGroup = intake.get("Attachment", "attCommentKey", false);
        Field commentField = null;
        commentField = commentGroup.get("DataAsString");
        commentField.setRequired(true);
        if (commentGroup == null || !commentField.isValid())
        {
            commentField.setMessage("An explanatory comment is required " + 
                                    "to modify attributes.");
        }

        // Set any other required flags
        Attribute[] requiredAttributes = issue.getModule()
                                              .getRequiredAttributes(issueType);
        AttributeValue aval = null;
        Group group = null;

        SequencedHashtable modMap = issue.getModuleAttributeValuesMap();
        Iterator iter = modMap.iterator();
        while (iter.hasNext()) 
        {
            aval = (AttributeValue)modMap.get(iter.next());
            group = intake.get("AttributeValue", aval.getQueryKey(), false);

            if (group != null) 
            {            
                Field field = null;
                if (aval instanceof OptionAttribute) 
                {
                    field = group.get("OptionId");
                }
                else
                {
                    field = group.get("Value");
                }
            
                for (int j=requiredAttributes.length-1; j>=0; j--) 
                {
                    if (aval.getAttribute().getPrimaryKey().equals(
                         requiredAttributes[j].getPrimaryKey())) 
                    {
                        field.setRequired(true);
                        break;
                    }                    
                }
            } 
        } 

        if (intake.isAllValid()) 
        {
            issue.save();

            // Save explanatory comment
            commentGroup.setProperties(attachment);
            attachment.setTextFields(user, issue, 
                                     Attachment.MODIFICATION__PK);
            attachment.save();

            // Set the attribute values entered 
            HashMap avMap = issue.getAllAttributeValuesMap();
            Iterator iter2 = avMap.keySet().iterator();

            // Save transaction record
            Transaction transaction = new Transaction();
            transaction.create(TransactionTypePeer.EDIT_ISSUE__PK, 
                               user, attachment);

            while (iter2.hasNext())
            {
                aval = (AttributeValue)avMap.get(iter2.next());
                group = intake.get("AttributeValue", aval.getQueryKey(), false);

                if (group != null) 
                {            
                    NumberKey newOptionId = null;
                    NumberKey oldOptionId = null;
                    String newValue = "";
                    String oldValue = "";
                    if (aval instanceof OptionAttribute) 
                    {
                        newValue = group.get("OptionId").toString();
                        oldValue = aval.getOptionIdAsString();
                    
                        if (!newValue.equals(""))
                        {
                            newOptionId = new NumberKey(newValue);
                            AttributeOption newAttributeOption = 
                              AttributeOptionPeer
                              .retrieveByPK(new NumberKey(newValue));
                            newValue = newAttributeOption.getName();
                        }
                        if (!oldValue.equals(""))
                        {
                            oldOptionId = aval.getOptionId();
                            AttributeOption oldAttributeOption = 
                              AttributeOptionPeer
                              .retrieveByPK(oldOptionId);
                            oldValue = oldAttributeOption.getName();
                        }
                        
                    }
                    else 
                    {
                        newValue = group.get("Value").toString();
                        oldValue = aval.getValue();
                    }

                    if (!newValue.equals("") && !oldValue.equals(newValue))
                    {
                        aval.startTransaction(transaction);
                        group.setProperties(aval);
                        aval.save();
                    }
                } 
            }
            intake.removeAll();
            if (!transaction.sendEmail(new ContextAdapter(context), issue))
            {
                data.setMessage("Your changes were saved, but could not send "
                         + "notification email due to a sendmail error.");
            }
        } 
        else
        {
            data.setMessage(ERROR_MESSAGE);
        }

        String template = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, template);            
    }

    
    /**
    *  Adds an attachment of type "url".
    */
   public void doSubmiturl (RunData data, TemplateContext context) 
        throws Exception
   {
        submitAttachment (data, context, "url");
   } 

    /**
    *  Adds an attachment of type "comment".
    */
   public void doSubmitcomment (RunData data, TemplateContext context) 
        throws Exception
   {
        submitAttachment (data, context, "comment");
   } 
    
    
    /**
     * Add an attachment of type "file"
     */
    public void doSubmitfile (RunData data, TemplateContext context)
        throws Exception
    {
        submitAttachment (data, context, "file");
    }
    

   /**
    *  Adds an attachment.
    */
    private void submitAttachment (RunData data, TemplateContext context, 
                                   String type)
        throws Exception
    {                          
        String id = data.getParameters().getString("id");
        Issue issue = getScarabRequestTool(context).getIssue();
        IntakeTool intake = getIntakeTool(context);
        Attachment attachment = new Attachment();
        NumberKey typeId = null;
        Group group = null;
        ScarabUser user = (ScarabUser)data.getUser();
        Transaction transaction = new Transaction();
        
        if (type.equals("url"))
        {
            group = intake.get("Attachment", "urlKey", false);
            typeId = Attachment.URL__PK;
        } 
        else if (type.equals("comment")) 
        {
            group = intake.get("Attachment", "commentKey", false);
            typeId = Attachment.COMMENT__PK;
        }
        else if (type.equals("file"))
        {
            group = intake.get("Attachment", "fileKey", false);
            typeId = Attachment.FILE__PK;
        }
        
        if (group != null) 
        {
            Field nameField = group.get("Name"); 
            Field dataField = group.get("DataAsString"); 
            nameField.setRequired(true);
// FIXME: dataField is commented out because this method is
//        really overloaded in functionality. In reality, a
//        file attachment should require this to be filled in
//            dataField.setRequired(true);
            if (!nameField.isValid())
            {
                nameField.setMessage("This field requires a value.");
            }
//            if (!dataField.isValid())
//            {
//                dataField.setMessage("This field requires a value.");
//            }
            if (intake.isAllValid())
            {
                group.setProperties(attachment);
                
                if (type.equals("url") || type.equals("comment"))
                {
                    attachment.setTextFields(user, issue, typeId);
                    attachment.save();
                    if (type.equals("url"))
                    {
                        // Save transaction record
                        transaction.create(TransactionTypePeer.EDIT_ISSUE__PK, 
                                           user, attachment);
                        
                        // Save activity record
                        Activity activity = new Activity();
                        
                        // Generate description of modification
                        StringBuffer descBuf = new StringBuffer("added URL '");
                        descBuf.append(nameField.toString()).append("'");
                        String desc = descBuf.toString();
                        activity.create(issue, null, desc, transaction, "", "");
                    }
                }
                else if (type.equals("file"))
                {
                    addAttachment(issue, group, attachment, data, intake);
                    issue.save();
                }

                if (!transaction.sendEmail(new ContextAdapter(context), issue))
                {
                    data.setMessage("Your attachment was saved, but could not send notification email "
                                     + "due to a sendmail error.");
                }

                String template = data.getParameters()
                                 .getString(ScarabConstants.NEXT_TEMPLATE);
                setTarget(data, template);            
            } 
            else
            {
                data.setMessage(ERROR_MESSAGE);
            }
        }
        String template = data.getParameters()
                          .getString(ScarabConstants.NEXT_TEMPLATE, "ViewIssue");
        setTarget(data, template);            
   } 

    static void addAttachment(Issue issue, Group group, Attachment attachment, 
                              RunData data, IntakeTool intake)
        throws Exception
    {
        if (group != null)
        {
            Field nameField = group.get("Name");
            Field fileField = group.get("File");
            nameField.setRequired(true);
            fileField.setRequired(true);
            Field mimeAField = group.get("MimeTypeA");
            Field mimeBField = group.get("MimeTypeB");
            String mimeA = mimeAField.toString();
            String mimeB = mimeBField.toString();
            String mimeType = null;
            if (mimeB != null && mimeB.trim().length() > 0)
            {
                mimeType = mimeB;
            }
            else
            {
                mimeAField.setRequired(true);
                mimeType = mimeA;
            }
            
            if (group.isAllValid()) 
            {
                group.setProperties(attachment);
                attachment.setMimeType(mimeType);
                ScarabUser user = (ScarabUser)data.getUser();
                attachment.setCreatedBy(user.getUserId());
                issue.addFile(attachment);
                data.setMessage("Attachment was added");
                // remove the group so that the form data doesn't show up again
                intake.remove(group);
            }
            else
            {
                data.setMessage(ERROR_MESSAGE);
            }
        }
        else
        {
            data.setMessage("Could not locate Attachment group");
        }
    }


    /**
    *  Edits a comment.
    */
   public void doEditcomment (RunData data, TemplateContext context)
        throws Exception
    {                          
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attachmentId;
        String newComment = null;
        ScarabUser user = (ScarabUser)data.getUser();
        String id = data.getParameters().getString("id");
        Issue issue = Issue.getIssueById(id);

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("edit_comment"))
            {
               attachmentId = key.substring(13);
               newComment = params.getString(key);
               Attachment attachment = (Attachment) AttachmentPeer
                                     .retrieveByPK(new NumberKey(attachmentId));
               attachment.setDataAsString(newComment);
               attachment.save();
            }
        }
    }

    /**
    *  Deletes an url.
    */
   public void doDeleteurl (RunData data, TemplateContext context)
        throws Exception
    {                          
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attachmentId;
        ScarabUser user = (ScarabUser)data.getUser();
        String id = data.getParameters().getString("id");
        Issue issue = Issue.getIssueById(id);

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("url_delete_"))
            {
               attachmentId = key.substring(11);
               Attachment attachment = (Attachment) AttachmentPeer
                                     .retrieveByPK(new NumberKey(attachmentId));
               attachment.setDeleted(true);
               attachment.save();

               // Save transaction record
               Transaction transaction = new Transaction();
               transaction.create(TransactionTypePeer.EDIT_ISSUE__PK, 
                                  user, null);

               // Save activity record
               Activity activity = new Activity();

               // Generate description of modification
               StringBuffer descBuf = new StringBuffer("deleted URL '");
               descBuf.append(attachment.getName()).append("'");
               String desc = descBuf.toString();
               activity.create(issue, null, desc, transaction, "", "");
               issue.save();
               if (!transaction.sendEmail(new ContextAdapter(context), issue))
               {
                    data.setMessage("Your link was deleted, but could not send notification email "
                                     + "due to a sendmail error.");
                }
            } 
        }
        String template = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, template);            
    }
    
   /**
    *  Deletes a file.
    */
   public void doDeletefile (RunData data, TemplateContext context)
        throws Exception
    {      
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attachmentId;
        ScarabUser user = (ScarabUser)data.getUser();
        String id = data.getParameters().getString("id");
        Issue issue = Issue.getIssueById(id);

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("file_delete_"))
            {
               attachmentId = key.substring(12);
               Attachment attachment = (Attachment) AttachmentPeer
                                     .retrieveByPK(new NumberKey(attachmentId));
               attachment.setDeleted(true);
               attachment.save();

               // Save transaction record
               Transaction transaction = new Transaction();
               transaction.create(TransactionTypePeer.EDIT_ISSUE__PK, 
                                  user, null);

               // Save activity record
               Activity activity = new Activity();

               // Generate description of modification
               StringBuffer descBuf = new StringBuffer("deleted File '");
               descBuf.append(attachment.getName()).append("'");
               String desc = descBuf.toString();
               activity.create(issue, null, desc, transaction, "", "");
               issue.save();
               if (!transaction.sendEmail(new ContextAdapter(context), issue))
               {
                    data.setMessage("Your file was deleted, but could not send notification email "
                                     + "due to a sendmail error.");
               }
            } 
        }
        String template = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, template);            
    }


    /**
    *  Modifies the dependency type between the current issue
    *  And its parent or child issue.
    */
    public void doUpdatedependencies (RunData data, TemplateContext context)
        throws Exception
    {                          
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            ScarabUser user = (ScarabUser)data.getUser();
            String id = data.getParameters().getString("id");
            Issue currentIssue = Issue.getIssueById(id);

            List dependencies = new ArrayList();
            List children = currentIssue.getChildren();
            List parents = currentIssue.getParents();
            dependencies.addAll(children);
            dependencies.addAll(parents);

            for (int i=0; i< dependencies.size(); i++)
            {
                Depend depend = (Depend)dependencies.get(i);
                Group group = intake.get("Depend", depend.getQueryKey(), false);
                String oldValue = depend.getDependType().getName();
                group.setProperties(depend);
                depend.save();
                String newValue = null;
      
                // User is deleting dependency
                if (group.get("Deleted").toString().equals("true"))
                {
                    newValue = "none";
                }
                else
                {
                    newValue = depend.getDependType().getName();
                }
               
                // User is changing dependency type
                if (!newValue.equals(oldValue))
                {
                    Issue otherIssue = null;
                    if (currentIssue.getIssueId().toString()
                        .equals(depend.getObservedId().toString()))
                    {
                        otherIssue = (Issue)IssuePeer.
                                     retrieveByPK(depend.getObserverId());
                    }
                    else
                    {
                        otherIssue = (Issue)IssuePeer.
                                     retrieveByPK(depend.getObservedId());
                    }

                    // Save transaction record
                    Transaction transaction = new Transaction();
                    transaction.create(TransactionTypePeer.EDIT_ISSUE__PK, 
                                       user, null);

                    // Save activity record
                    Activity activity = new Activity();
                    StringBuffer descBuf = new StringBuffer("changed dependency " 
                                                            + "type on Issue ");
                    descBuf.append(otherIssue.getUniqueId());
                    descBuf.append(" from ").append(oldValue);
                    descBuf.append(" to ").append(newValue.toString());
                    String desc = descBuf.toString();
                    activity.create(currentIssue, null, desc, transaction,
                                    oldValue, newValue.toString());
                    currentIssue.save();
                    if (!transaction.sendEmail(new ContextAdapter(context), 
                                               currentIssue))
                    {
                         data.setMessage("Your changes were saved, but could "
                                         + "not send notification "
                                         + "email due to a sendmail error.");
                    }
                }
            }
        }
        else
        {
            data.setMessage(ERROR_MESSAGE);
        }
    }

    /**
    *  Adds a dependency between this issue and another issue.
    *  This issue will be the child issue. 
    */
    public void doAdddependency (RunData data, TemplateContext context)
        throws Exception
    {                          
        String id = data.getParameters().getString("id");
        Issue issue = Issue.getIssueById(id);
        ScarabUser user = (ScarabUser)data.getUser();
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Group group = intake.get("Depend", IntakeTool.DEFAULT_KEY);
        Issue childIssue = null;
        boolean isValid = true;
        Depend depend = new Depend();

        // Check that dependency type entered is valid
        Field type = group.get("TypeId");
        type.setRequired(true);
        if (!type.isValid())
        {
            type.setMessage("Please enter a valid dependency type.");
        }

        // Check that child ID entered is valid
        Field childId = group.get("ObserverUniqueId");
        childId.setRequired(true);
        if (!childId.isValid())
        {
            childId.setMessage("Please enter a valid issue id.");
        }
        else
        {
            // Check that child ID entered corresponds to a valid issue
            try
            {
                childIssue = scarabR.getIssue(childId.toString());
            }
            catch (Exception e)
            {
                childId.setMessage("The id you entered does " +
                                   "not correspond to a valid issue.");
                isValid = false;
            }

            // Check whether the entered issue is already dependant on this
            // Issue. If so, and it had been marked deleted, mark as undeleted.
            if (childIssue != null && issue.getDependency(childIssue) != null)
            {
                Depend prevDepend = issue.getDependency(childIssue);
                if (prevDepend.getDeleted())
                {
                    depend = prevDepend;
                    depend.setDeleted(false);
                }
                else
                {
                    childId.setMessage("This issue already has a dependency" 
                                      + " on the issue id you entered.");
                    isValid = false;
                }
            }
            // Make sure issue is not being marked as dependant on itself.
            if (childIssue.equals(issue))
            {
                childId.setMessage("You cannot add a dependency for an " 
                                  + "issue on itself.");
                isValid = false;
            }
        }
        if (intake.isAllValid() && isValid)
        {
            depend.setDefaultModule(scarabR.getCurrentModule());
            group.setProperties(depend);
            depend.save();
            intake.remove(group);
        }
        else
        {
            data.setMessage(ERROR_MESSAGE);
        }
    }


    /**
    *  Adds a dependency between this issue and another issue.
    *  This issue will be the child issue. 
    */
    public void doAdddependencyold (RunData data, TemplateContext context)
        throws Exception
    {                          
        String id = data.getParameters().getString("id");
        Issue issue = Issue.getIssueById(id);
        ScarabUser user = (ScarabUser)data.getUser();
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Depend depend = scarabR.getDepend();
        Group group = intake.get("Depend", depend.getQueryKey());
        boolean isValid = true;
        Issue parentIssue = null;

        // The depend type is required.
        Field dependTypeId = group.get("TypeId");
        if (dependTypeId.toString().equals("0"))
        {
            dependTypeId.setMessage("Please select a dependency type.");
            return;
        }

        // The parent issue id is required, and must be a valid issue.
        Field observedId = group.get("ObservedId");
        observedId.setRequired(true);
        if (!observedId.isValid())
        {
            observedId.setMessage("Please enter a valid issue id.");
        }
        else
        {
            try
            {
                parentIssue = getScarabRequestTool(context)
                               .getIssue(observedId.toString());
            }
            catch (Exception e)
            {
                observedId.setMessage("The id you entered does " +
                                      "not correspond to a valid issue.");
                isValid = false;
            }
            Depend prevDepend = parentIssue.getDependency(issue);
            if (prevDepend != null)
            {
                if (prevDepend.getDeleted())
                {
                    prevDepend.setDeleted(false);
                    depend = prevDepend;
                }
                else
                {
                    observedId.setMessage("This issue already has a dependency" 
                                      + " on the issue id you entered.");
                    isValid = false;
                }
            }
            else if (parentIssue.equals(issue))
            {
                observedId.setMessage("You cannot add a dependency for an " 
                                      + "issue on itself.");
                isValid = false;
            }
        }

        if (intake.isAllValid() && isValid)
        {
            depend.setObserverId(issue.getIssueId());
            depend.setObservedId(parentIssue.getIssueId());
            depend.setTypeId(new NumberKey(dependTypeId.toString()));
            depend.save();

            // Save transaction record
            Transaction transaction = new Transaction();
            transaction.create(TransactionTypePeer.EDIT_ISSUE__PK, 
                               user, null);

            // Save activity record
            Activity activity = new Activity();
            StringBuffer descBuf = new StringBuffer("added ");
            descBuf.append(depend.getDependType().getName());
            descBuf.append(" dependency for Issue ");
            descBuf.append(issue.getUniqueId());
            descBuf.append(" on Issue ");
            descBuf.append(parentIssue.getUniqueId());
            String desc = descBuf.toString();
            activity.create(issue, null, desc, transaction, "", "");
            issue.save();
            if (!transaction.sendEmail(new ContextAdapter(context), issue))
            {
                data.setMessage("Your changes were saved, but could "
                                + "not send notification email "
                                + "due to a sendmail error.");
            }
        }
        else
        {
            data.setMessage(ERROR_MESSAGE);
        }
    }

    /**
     * Redirects to AssignIssue page.
     */
    public void doEditassignees(RunData data, TemplateContext context)
         throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        intake.removeAll();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        data.getParameters().add("issue_ids", 
              scarabR.getIssue().getIssueId().toString());
        setTarget(data, "AssignIssue.vm");            
    }

    /**
     * Redirects to MoveIssue page with move action selected.
     */
    public void doMove(RunData data, TemplateContext context)
         throws Exception
    {
        data.getParameters().add("mv_0rb", "move");
        setTarget(data, "MoveIssue.vm");            
    }

    /**
     * Redirects to MoveIssue page with copy action selected.
     */
    public void doCopy(RunData data, TemplateContext context)
         throws Exception
    {
        data.getParameters().add("mv_0rb", "copy");
        setTarget(data, "MoveIssue.vm");            
    }
}
