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
import org.apache.commons.collections.SequencedHashMap;
import org.apache.turbine.ParameterParser;

// Scarab Stuff
import org.tigris.scarab.actions.base.BaseModifyIssue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentManager;
import org.tigris.scarab.om.AttachmentType;
import org.tigris.scarab.om.AttachmentTypePeer;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.ActivitySetManager;
import org.tigris.scarab.om.ActivitySetTypePeer;
import org.tigris.scarab.om.ActivityManager;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.Depend;
import org.tigris.scarab.om.DependPeer;
import org.tigris.scarab.om.DependType;
import org.tigris.scarab.om.DependTypePeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.services.cache.ScarabCache; 
import org.tigris.scarab.services.security.ScarabSecurity;
import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.upload.TurbineUploadService;
import org.apache.fulcrum.upload.UploadService;


import org.tigris.scarab.attribute.OptionAttribute;

import org.tigris.scarab.util.ScarabConstants;

/**
    This class is responsible for edit issue forms.
    ScarabIssueAttributeValue
    @author <a href="mailto:elicia@collab.net">Elicia David</a>
    @version $Id: ModifyIssue.java,v 1.115 2002/08/09 00:23:06 elicia Exp $
*/
public class ModifyIssue extends BaseModifyIssue
{

    public void doSubmitattributes(RunData data, TemplateContext context)
        throws Exception
    {
        if (isCollision(data, context)) 
        {
            return;
        }

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String id = data.getParameters().getString("id");
        if (id == null || id.length() == 0)
        {
            scarabR.setAlertMessage("Could not locate issue.");
            return;
        }
        Issue issue = Issue.getIssueById(id);
        if (issue == null)
        {
            scarabR.setAlertMessage("Could not locate issue: " + id);
            return;
        }
        IssueType issueType = issue.getIssueType();
        ScarabUser user = (ScarabUser)data.getUser();

        IntakeTool intake = getIntakeTool(context);
       
        // Comment field is required to modify attributes
        Attachment attachment = AttachmentManager.getInstance();
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
        List requiredAttributes = issue.getModule()
                                              .getRequiredAttributes(issueType);
        AttributeValue aval = null;
        AttributeValue aval2 = null;
        HashMap newAttVals = new HashMap();
        Group group = null;

        SequencedHashMap modMap = issue.getModuleAttributeValuesMap();
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
            
                for (int j=requiredAttributes.size()-1; j>=0; j--) 
                {
                    if (aval.getAttribute().getPrimaryKey().equals(
                         ((Attribute)requiredAttributes.get(j)).getPrimaryKey())) 
                    {
                        field.setRequired(true);
                        break;
                    }                    
                }
            } 
        } 

        if (intake.isAllValid()) 
        {
            // Save explanatory comment
            commentGroup.setProperties(attachment);
            attachment.setTextFields(user, issue, 
                                     Attachment.MODIFICATION__PK);
            attachment.save();

            // Create activitySet record
            ActivitySet activitySet = issue.getActivitySet(user, attachment,
                                      ActivitySetTypePeer.EDIT_ISSUE__PK);
            activitySet.save();

            // Set the attribute values entered 
            SequencedHashMap avMap = issue.getModuleAttributeValuesMap(); 
            Iterator iter2 = avMap.iterator();
            while (iter2.hasNext())
            {
                aval = (AttributeValue)avMap.get(iter2.next());
                aval2 = AttributeValue.getNewInstance(aval.getAttributeId(), 
                                                      aval.getIssue());
                aval2.setProperties(aval);
 
                group = intake.get("AttributeValue", aval.getQueryKey(), false);
                String oldValue = "";
                String newValue = "";

                if (group != null) 
                {            
                    if (aval instanceof OptionAttribute) 
                    {
                        newValue = group.get("OptionId").toString();
                        oldValue = aval.getOptionIdAsString();
                    }
                    else 
                    {
                        newValue = group.get("Value").toString();
                        oldValue = aval.getValue();
                    }
                    if (!newValue.equals("") && 
                        (oldValue == null  || !oldValue.equals(newValue)))
                    {
                        group.setProperties(aval2);
                        newAttVals.put(aval.getAttributeId(), aval2);
                    }
                }
            } 
            String msg = issue.setProperties(newAttVals, activitySet, user);
            if (msg == null)
            {
                intake.removeAll();
                sendEmail(activitySet, issue, DEFAULT_MSG, context, data);
                scarabR.setConfirmMessage("Your changes have been saved.");
            }
            else
            {
                scarabR.setAlertMessage(msg);
            }
        } 
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
        }
    }
    
    /**
     *  Modifies attachments of type "url".
     */
    public void doSaveurl (RunData data, TemplateContext context) 
        throws Exception
    {
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String id = data.getParameters().getString("id");
        if (id == null || id.length() == 0)
        {
            scarabR.setAlertMessage("Could not locate issue.");
            return;
        }
        Issue issue = Issue.getIssueById(id);
        if (issue == null)
        {
            scarabR.setAlertMessage("Could not locate issue: " + id);
            return;
        }
        List urls = issue.getAttachments();
        for (int i = 0; i<urls.size(); i++)
        {
            Attachment attachment = (Attachment)urls.get(i);
            if (attachment.getTypeId().equals(Attachment.URL__PK)
                && !attachment.getDeleted())
            {
                IntakeTool intake = getIntakeTool(context);
                Group group = intake.get("Attachment", attachment.getQueryKey(), false);

                Field nameField = group.get("Name"); 
                Field dataField = group.get("DataAsString"); 
                if (nameField.isValid())
                {
                    nameField.setRequired(true);
                }
                if (dataField.isValid())
                {
                    dataField.setRequired(true);
                }
                
                if (intake.isAllValid())
                {
                    // store the new and old data
                    String oldDescription = attachment.getName();
                    String oldURL = new String(attachment.getData());
                    String newDescription = nameField.toString();
                    String newURL = dataField.toString();

                    if (!oldDescription.equals(newDescription) ||
                        !oldURL.equals(newURL))
                    {
                        group.setProperties(attachment);
                        attachment.save();
                        attachment.registerSaveURLActivity(
                            (ScarabUser)data.getUser(), issue, 
                            oldDescription, newDescription, 
                            oldURL, newURL);
                    }
                }
            }
        } 
    }

    /**
     *  Adds an attachment of type "url".
     */
    public void doSubmiturl (RunData data, TemplateContext context) 
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        Group group = intake.get("Attachment", "urlKey", false);
        if (group != null) 
        {
            handleAttachment(data, context, Attachment.URL__PK, group);
        }
    }

    /**
     *  Adds an attachment of type "comment".
     */
    public void doSubmitcomment (RunData data, TemplateContext context) 
         throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        Group group = intake.get("Attachment", "commentKey", false);
        if (group != null) 
        {
            handleAttachment(data, context, Attachment.COMMENT__PK, group);
        }
    } 

    /**
     * Add an attachment of type "file"
     */
    public void doSubmitfile (RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        Group group = intake.get("Attachment", "fileKey", false);
        if (group != null) 
        {
            handleAttachment(data, context, Attachment.FILE__PK, group);
        }
    }

    private void handleAttachment (RunData data, TemplateContext context, 
                                 NumberKey typeId, Group group)
        throws Exception
    {
        // grab the data from the group
        Field nameField = group.get("Name"); 
        Field dataField = group.get("DataAsString");
        // set some required fields
        if (nameField.isValid())
        {
            nameField.setRequired(true);
        }
        if (dataField.isValid() && (typeId == Attachment.COMMENT__PK 
                                    || typeId == Attachment.URL__PK))
        {
            dataField.setRequired(true);
        }

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        IntakeTool intake = getIntakeTool(context);
        // validate intake
        if (intake.isAllValid())
        {
            // get the issue id
            String id = data.getParameters().getString("id");
            if (id == null || id.length() == 0)
            {
                scarabR.setAlertMessage("Could not locate issue.");
                return;
            }
            // get the issue object
            Issue issue = Issue.getIssueById(id);
            if (issue == null)
            {
                scarabR.setAlertMessage("Could not locate issue: " + id);
                return;
            }
            // create the new attachment
            Attachment attachment = AttachmentManager.getInstance();
            String message = null;
            boolean addSuccess = false;
            if (typeId == Attachment.FILE__PK)
            {
                // adding a file is a special process
                addSuccess = addFileAttachment(issue, group, attachment, 
                              scarabR, data, intake);
                issue.save();
                message = "Your file was saved.";
            }
            else if (typeId == Attachment.URL__PK || typeId == Attachment.COMMENT__PK)
            {
                // set the form data to the attachment object
                group.setProperties(attachment);
                if (typeId == Attachment.URL__PK)
                {
                    message = "Your url was saved.";
                }
                else
                {
                    message = "Your comment was saved.";
                }
                addSuccess = true;
            }

            ScarabUser user = (ScarabUser)data.getUser();
            String nameFieldString = nameField.toString();
            String dataFieldString = dataField.toString();
            // register the add activity
            ActivitySet activitySet = attachment.registerAddActivity(user, issue, typeId, 
                                        nameFieldString, dataFieldString);
            if (addSuccess)
            {
                // remove the group
                intake.remove(group);
                sendEmail(activitySet, issue, message, context, data);
                scarabR.setConfirmMessage(message);
            }
        }
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
        }
    }

    static boolean addFileAttachment(Issue issue, Group group, Attachment attachment, 
        ScarabRequestTool scarabR, RunData data, IntakeTool intake)
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
                // remove the group so that the form data doesn't show up again
                intake.remove(group);
                return true;
            }
            else
            {
                scarabR.setAlertMessage(ERROR_MESSAGE);
            }
        }
        else
        {
            scarabR.setAlertMessage("Could not locate attachment group");
        }
        return false;
    }

    /**
     * Eventually, this should be moved somewhere else once we can figure
     * out how to separate email out of the request context scope.
     */
    private void sendEmail(ActivitySet activitySet, Issue issue, String msg,
                           TemplateContext context, RunData data)
        throws Exception
    {
        if (!activitySet.sendEmail(new ContextAdapter(context), issue))
        {
            StringBuffer sb = 
                new StringBuffer(msg.length() + EMAIL_ERROR.length());
            sb.append(msg).append(EMAIL_ERROR);
            getScarabRequestTool(context).setConfirmMessage(sb.toString());
        }
    }

    /**
     *  Edits a comment.
     */
    public void doEditcomment (RunData data, TemplateContext context)
        throws Exception
    {                          
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attachmentId;
        String newComment = null;
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String id = data.getParameters().getString("id");
        if (id == null || id.length() == 0)
        {
            scarabR.setAlertMessage("Could not locate issue.");
            return;
        }
        Issue issue = Issue.getIssueById(id);
        if (issue == null)
        {
            scarabR.setAlertMessage("Could not locate issue: " + id);
            return;
        }

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("edit_comment"))
            {
                attachmentId = key.substring(13);
                newComment = params.getString(key, "");
                Attachment attachment = AttachmentManager
                    .getInstance(new NumberKey(attachmentId), false);
                String oldComment = attachment.getDataAsString();
                if (!newComment.equals(oldComment)) 
                {
                    attachment.setDataAsString(newComment);
                    attachment.save();
                   
                    // Generate description of modification
                    String from = "changed comment from '";
                    String to = "' to '";
                    int capacity = from.length() + oldComment.length() +
                        to.length() + newComment.length();
                    String desc = new StringBuffer(capacity)
                        .append(from).append(oldComment).append(to)
                        .append(newComment).append('\'').toString();
                    registerActivity(desc, DEFAULT_MSG, issue, user, 
                        attachment, context, data, oldComment, newComment);
                }
            }
        }
    }

    /**
    *  Deletes an url.
    */
   public void doDeleteurl (RunData data, TemplateContext context)
        throws Exception
    {                          
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attachmentId;
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String id = data.getParameters().getString("id");
        if (id == null || id.length() == 0)
        {
            scarabR.setAlertMessage("Could not locate issue.");
            return;
        }
        Issue issue = Issue.getIssueById(id);
        if (issue == null)
        {
            scarabR.setAlertMessage("Could not locate issue: " + id);
            return;
        }

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("url_delete_"))
            {
               attachmentId = key.substring(11);
               Attachment attachment = AttachmentManager
                   .getInstance(new NumberKey(attachmentId), false);
               attachment.setDeleted(true);
               attachment.save();

               // Generate description of modification
               String name = attachment.getName();
               String desc = new StringBuffer(name.length() + 14)
                   .append("deleted URL '").append(name).append("'")
                   .toString();
               registerActivity(desc, "Your link was deleted", 
                                issue, user, attachment, context, data);
               data.setMessage(DEFAULT_MSG);  
            } 
        }
    }
    
   /**
    *  Deletes a file.
    */
   public void doDeletefile (RunData data, TemplateContext context)
        throws Exception
    {      
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attachmentId;
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String id = data.getParameters().getString("id");
        if (id == null || id.length() == 0)
        {
            scarabR.setAlertMessage("Could not locate issue.");
            return;
        }
        Issue issue = Issue.getIssueById(id);
        if (issue == null)
        {
            scarabR.setAlertMessage("Could not locate issue: " + id);
            return;
        }

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("file_delete_"))
            {
               attachmentId = key.substring(12);
               Attachment attachment = AttachmentManager
                   .getInstance(new NumberKey(attachmentId), false);
               attachment.setDeleted(true);
               attachment.save();

               // Generate description of modification
               String name = attachment.getFileName();
               String path = attachment.getRelativePath();
               String desc = new StringBuffer(path.length()+name.length()+38)
                   .append("deleted attachment for file '").append(name)
                   .append("'; path=").append(path).toString();
               registerActivity(desc, "Your file was deleted", issue, user, 
                                attachment, context, data);
            } 
        }
    }

    private void registerActivity(String description, String message,
        Issue issue, ScarabUser user, Attachment attachment, 
        TemplateContext context, RunData data)
        throws Exception
    {
        registerActivity(description, message, issue, user, attachment, 
                         context, data, "", "");
    }

    private void registerActivity(String description, String message,
        Issue issue, ScarabUser user, Attachment attachment, 
        TemplateContext context, RunData data, String oldVal, String newVal)
        throws Exception
    {
        // Save activitySet record
        ActivitySet activitySet = issue.getActivitySet(user, attachment,
                                  ActivitySetTypePeer.EDIT_ISSUE__PK);
        activitySet.save();

        // Save activity record
        ActivityManager
            .createTextActivity(issue, null, activitySet,
                                description, attachment,
                                oldVal, newVal);
        sendEmail(activitySet, issue, message, context, data);
    }

    /**
    *  Modifies the dependency type between the current issue
    *  And its parent or child issue.
    */
    public void doUpdatedependencies (RunData data, TemplateContext context)
        throws Exception
    {                          
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            ScarabUser user = (ScarabUser)data.getUser();
            String id = data.getParameters().getString("id");
            if (id == null || id.length() == 0)
            {
                scarabR.setAlertMessage("Could not locate issue.");
                return;
            }
            Issue currentIssue = Issue.getIssueById(id);
            if (currentIssue == null)
            {
                scarabR.setAlertMessage("Could not locate issue: " + id);
                return;
            }

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
                        otherIssue = IssueManager
                            .getInstance(depend.getObserverId(), false);
                    }
                    else
                    {
                        otherIssue = IssueManager
                            .getInstance(depend.getObservedId(), false);
                    }

                    currentIssue.save();
                    String uniqueId = otherIssue.getUniqueId();
                    String s = "changed dependency type for issue ";
                    String from = " from ";
                    String to = " to ";
                    int capacity = s.length() + uniqueId.length() + 
                        from.length() + oldValue.length() + to.length() + 
                        newValue.length();
                    String desc = new StringBuffer(capacity)
                        .append(s).append(uniqueId).append(from)
                        .append(oldValue).append(to).append(newValue)
                        .toString();
                    registerActivity(desc, DEFAULT_MSG, currentIssue, 
                        user, null, context, data, oldValue, newValue);
                    data.setMessage(DEFAULT_MSG);  
                }
            }
        }
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
        }
    }

    /**
    *  Adds a dependency between this issue and another issue.
    *  This issue will be the child issue. 
    */
    public void doAdddependency (RunData data, TemplateContext context)
        throws Exception
    {                          
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String id = data.getParameters().getString("id");
        if (id == null || id.length() == 0)
        {
            scarabR.setAlertMessage("Could not locate issue.");
            return;
        }
        Issue issue = Issue.getIssueById(id);
        if (issue == null)
        {
            scarabR.setAlertMessage("Could not locate issue: " + id);
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        IntakeTool intake = getIntakeTool(context);
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
            childIssue = scarabR.getIssue(childId.toString());
            if (childIssue == null)
            {
                childId.setMessage("The id you entered does " +
                                   "not correspond to a valid issue.");
                isValid = false;
            }
            // Check whether the entered issue is already dependant on this
            // Issue. If so, and it had been marked deleted, mark as undeleted.
            else if (childIssue != null && issue.getDependency(childIssue) != null)
            {
                Depend prevDepend = issue.getDependency(childIssue);
                if (prevDepend.getDeleted())
                {
                    depend = prevDepend;
                    depend.setDeleted(false);
                    depend.save();
                }
                else
                {
                    childId.setMessage("This issue already has a dependency" 
                                      + " on the issue id you entered.");
                    isValid = false;
                }
            }
            // Make sure issue is not being marked as dependant on itself.
            else if (childIssue != null && childIssue.equals(issue))
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

            // Save activitySet record
            ActivitySet activitySet = issue.getActivitySet(user, null,
                                      ActivitySetTypePeer.EDIT_ISSUE__PK);
            activitySet.save();

            // Save activitySet record for parent
            String desc = new StringBuffer("Added '")
                .append(depend.getDependType().getName())
                .append("' child dependency on issue ")
                .append(childIssue.getUniqueId())
                .toString();

            // Save activity record
            // FIXME: test to see if null instead of "" is ok.
            ActivityManager
                .createTextActivity(issue, null, activitySet,
                                    desc, null,
                                    "", childIssue.getUniqueId());
            sendEmail(activitySet, childIssue, desc, context, data);

            // Save activitySet record for child
            desc = new StringBuffer("Added '")
                .append(depend.getDependType().getName())
                .append("' parent dependency on issue ")
                .append(issue.getUniqueId())
                .toString();

            // Save activity record
            // FIXME: test to see if null instead of "" is ok.
            ActivityManager
                .createTextActivity(childIssue, null, activitySet,
                                    desc, null,
                                    "", issue.getUniqueId());
            sendEmail(activitySet, issue, desc, context, data);

            data.setMessage(DEFAULT_MSG);
            intake.remove(group);
        }
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
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
        String id = data.getParameters().getString("id");
        if (id == null || id.length() == 0)
        {
            scarabR.setAlertMessage("Could not locate issue.");
            return;
        }
        Issue issue = Issue.getIssueById(id);
        if (issue == null)
        {
            scarabR.setAlertMessage("Could not locate issue: " + id);
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        if (user.hasPermission(ScarabSecurity.ISSUE__ASSIGN, 
                               issue.getModule()))
        {
            data.getParameters().add("issue_ids", issue.getUniqueId());
            setTarget(data, "AssignIssue.vm");
        }
        else
        {
            scarabR.setAlertMessage(
                "Insufficient permissions to assign users.");
        }
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

    /**
     * does not actually modify an issue, but sets the preferred view
     * of an issue for the current session
     */
    public void doSetissueview(RunData data, TemplateContext context)
         throws Exception
    {
        String tab = data.getParameters().getString("tab", 
                                          ScarabConstants.ISSUE_VIEW_ALL);
        data.getUser().setTemp(ScarabConstants.TAB_KEY, tab); 
    }
}
