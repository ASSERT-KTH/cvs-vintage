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
import java.util.HashMap;

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.torque.om.NumberKey; 
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.mimetype.TurbineMimeTypes;
import org.apache.commons.collections.SequencedHashMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.turbine.ParameterParser;

// Scarab Stuff
import org.tigris.scarab.actions.base.BaseModifyIssue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentManager;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.Depend;
import org.tigris.scarab.om.DependManager;
import org.tigris.scarab.om.DependType;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.util.ScarabException;

import org.tigris.scarab.attribute.OptionAttribute;

import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.EmailContext;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.util.Log;

/**
 * This class is responsible for edit issue forms.
 * ScarabIssueAttributeValue
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: ModifyIssue.java,v 1.152 2003/03/15 21:56:57 jon Exp $
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
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue(false);
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        IntakeTool intake = getIntakeTool(context);       
        // Reason field is required to modify attributes
        Group reasonGroup = intake.get("Attachment", "attCommentKey", false);
        Field reasonField = null;
        reasonField = reasonGroup.get("Data");
        reasonField.setRequired(true);
        // make sure to trim the whitespace
        String reasonFieldString = reasonField.toString();
        if (reasonFieldString != null)
        {
            reasonFieldString = reasonFieldString.trim();
        }
        if (reasonGroup == null || !reasonField.isValid() ||
            reasonFieldString.length() == 0)
        {
            reasonField.setMessage(
                "ExplanatoryReasonRequiredToModifyAttributes");
        }

        // Set any other required flags
        IssueType issueType = issue.getIssueType();
        List requiredAttributes = issue.getModule()
                                              .getRequiredAttributes(issueType);
        AttributeValue aval = null;
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
            AttributeValue aval2 = null;
            HashMap newAttVals = new HashMap();

            // Set the attribute values entered 
            Iterator iter2 = modMap.iterator();
            boolean modifiedAttribute = false;
            while (iter2.hasNext())
            {
                aval = (AttributeValue)modMap.get(iter2.next());
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
                    // A value has been entered for the attribute.
                    // The old value is different from the new, or is unset:
                    // Set new value.
                    if (newValue.length() > 0
                         && ((oldValue == null) ||
                            (oldValue != null && !oldValue.equals(newValue))))
                    {
                        group.setProperties(aval2);
                        newAttVals.put(aval.getAttributeId(), aval2);
                        modifiedAttribute = true;
                    }
                    // The attribute is being undefined. 
                    else if (oldValue != null && newValue.length() == 0 && 
                             oldValue.length() != 0)
                    {
                        aval2.setValue(null);
                        newAttVals.put(aval.getAttributeId(), aval2);
                        modifiedAttribute = true;
                    }
                }
            } 
            if (!modifiedAttribute)
            {
                scarabR.setAlertMessage(l10n.get("MustModifyAttribute"));
                return;
            }
            Attachment attachment = AttachmentManager.getInstance();
            reasonGroup.setProperties(attachment);
            try
            {
                ActivitySet activitySet = issue.setAttributeValues(null, 
                                                newAttVals, attachment, user);
                intake.removeAll();
                sendEmail(activitySet, issue, DEFAULT_MSG, context);
                scarabR.setConfirmMessage(l10n.get("ChangesSaved"));
            }
            catch (Exception se)
            {
                scarabR.setAlertMessage(se.getMessage());
            }
        } 
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
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
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue(false);
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        ScarabUser user = (ScarabUser)data.getUser();

        List urls = issue.getAttachments();
        ActivitySet activitySet = null;
        for (int i = 0; i<urls.size(); i++)
        {
            Attachment attachment = (Attachment)urls.get(i);
            if (attachment.getTypeId().equals(Attachment.URL__PK)
                && !attachment.getDeleted())
            {
                Group group = intake.get("Attachment", attachment.getQueryKey(), false);

                Field nameField = group.get("Name"); 
                Field dataField = group.get("Data"); 
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

                    if (!oldDescription.equals(newDescription))
                    {
                        group.setProperties(attachment);
                        attachment.save();
                        activitySet = issue
                            .doChangeUrlDescription(activitySet, user, 
                                                    attachment, oldDescription);
                        scarabR.setConfirmMessage(l10n.get("UrlSaved"));
                    }
                    if (!oldURL.equals(newURL))
                    {
                        group.setProperties(attachment);
                        attachment.save();
                        activitySet = issue
                            .doChangeUrlUrl(activitySet, user, 
                                            attachment, oldURL);
                        scarabR.setConfirmMessage(l10n.get("UrlSaved"));
                    }
                }
            }
        }

        // if there is a new URL, add it
        Group newGroup = intake.get("Attachment", "urlKey", false);
        if (newGroup != null)
        {
            Field nameField = newGroup.get("Name"); 
            Field dataField = newGroup.get("Data");
            String nameFieldString = nameField.toString();
            String dataFieldString = dataField.toString();
            if (nameFieldString != null && nameFieldString.trim().length() > 0)
            {
                if (dataFieldString != null && dataFieldString.trim().length() > 0)
                {
                    // create the new attachment
                    Attachment attachment = AttachmentManager.getInstance();
                    // set the form data to the attachment object
                    newGroup.setProperties(attachment);

                    activitySet = issue.addUrl(attachment, user);

                    // remove the group
                    intake.remove(newGroup);

                    sendEmail(activitySet, issue, l10n.get("UrlSaved"), context);
                    scarabR.setConfirmMessage(l10n.get("UrlSaved"));
                }
                else
                {
                    dataField.setRequired(true);                    
                    scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
                }
            }
        }
    }

    /**
     *  Adds an attachment of type "comment".
     */
    public void doSubmitcomment (RunData data, TemplateContext context) 
         throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue(false);
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        Group group = intake.get("Attachment", "commentKey", false);
        if (group != null) 
        {
            Attachment attachment = AttachmentManager.getInstance();
            try
            {
                group.setProperties(attachment);
            }
            catch (Exception e)
            {
                scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
                return;
            }
            ActivitySet activitySet = null;
            try
            {
                activitySet = 
                    issue.addComment(attachment, (ScarabUser)data.getUser());
            }
            catch(Exception e)
            {
                scarabR.setAlertMessage(e.getMessage());
                return;
            }
            sendEmail(activitySet, issue, l10n.get("CommentSaved"), context);
            scarabR.setConfirmMessage(l10n.get("CommentSaved"));
            intake.remove(group);
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
    } 

    /**
     * Add an attachment of type "file"
     */
    public void doSubmitfile (RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue(false);
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        Group group = intake.get("Attachment", "fileKey", false);
        Field nameField = group.get("Name"); 
        // set some required fields
        if (nameField.isValid())
        {
            nameField.setRequired(true);
        }
        // validate intake
        if (intake.isAllValid() && group != null)
        {
            // adding a file is a special process
            addFileAttachment(issue, group, scarabR, data, intake);
            ActivitySet activitySet = issue.doSaveFileAttachments(user);
            if (activitySet != null)
            {
                sendEmail(activitySet, issue, l10n.get("FileSaved"), context);
                scarabR.setConfirmMessage(l10n.get("FileSaved"));
            }
            else
            {
                scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
    }

    static void addFileAttachment(Issue issue, Group group, 
            ScarabRequestTool scarabR, RunData data, IntakeTool intake)
        throws Exception
    {
        addFileAttachment(issue, group, null, scarabR, data, intake);
    }

    static void addFileAttachment(Issue issue, Group group, Attachment attachment,
            ScarabRequestTool scarabR, RunData data, IntakeTool intake)
        throws Exception
    {
        ScarabLocalizationTool l10n = (ScarabLocalizationTool)
            getTemplateContext(data).get(ScarabConstants.LOCALIZATION_TOOL);
        ActivitySet activitySet = null;
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
            else if ("autodetect".equals(mimeA) && fileField.isValid())
            {
                try 
                {
                    String filename = 
                        ((FileItem)fileField.getValue()).getName();
                    String contentType = 
                        TurbineMimeTypes.getContentType(filename, null);
                    if (contentType == null) 
                    {
                        // could not match extension.
                        mimeAField
                            .setMessage("intake_CouldNotDetermineMimeType");
                    }
                    else 
                    {
                        mimeType = contentType;
                    }
                }
                catch (Exception e)
                {
                    // we do not want any exception thrown here to affect
                    // the user experience, it is just considered a 
                    // non-detectable file type.  But still the exception is
                    // not expected, so log it.
                    mimeAField.setMessage("intake_CouldNotDetermineMimeType");
                    Log.get().info(
                        "Could not determine mimetype of uploaded file.", e);
                }                
            }
            else
            {
                mimeAField.setRequired(true);
                mimeType = mimeA;
            }
            if (group.isAllValid()) 
            {
                if (attachment == null)
                {
                    attachment = AttachmentManager.getInstance();
                }
                group.setProperties(attachment);
                attachment.setMimeType(mimeType);
                issue.addFile(attachment, (ScarabUser)data.getUser());
                // remove the group so that the form data doesn't show up again
                intake.remove(group);
            }
            else
            {
                scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get("CouldNotLocateAttachmentGroup"));
        }
    }

    /**
     * Eventually, this should be moved somewhere else once we can figure
     * out how to separate email out of the request context scope.
     */
    private void sendEmail(ActivitySet activitySet, Issue issue, String msg,
                           TemplateContext context)
        throws Exception
    {
        EmailContext ectx = new EmailContext();
        ectx.setLocalizationTool((ScarabLocalizationTool)context.get("l10n"));
        ectx.setLinkTool((ScarabLink)context.get("link"));

        if (!activitySet.sendEmail(ectx, issue))
        {
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            String emailError = l10n.get(EMAIL_ERROR);
            StringBuffer sb = 
                new StringBuffer(msg.length() + emailError.length());
            sb.append(msg).append(emailError);
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
        
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue(false);
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }

        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        ActivitySet activitySet = null;
        for (int i=0; i<keys.length; i++)
        {
            String key = (String) keys[i];
            if (key.startsWith("edit_comment_"))
            {
                String attachmentId = key.substring(13);
                String newComment = params.getString(key,"");
                Attachment attachment = AttachmentManager
                    .getInstance(new NumberKey(attachmentId), false);
                activitySet = issue.doEditComment(activitySet, newComment, attachment, user);
            }
        }
        if (activitySet != null)
        {
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
            sendEmail(activitySet, issue, l10n.get(DEFAULT_MSG), context);
        }
        else
        {
            scarabR.setInfoMessage(l10n.get("NoCommentsChanged"));
        }
    }

   /**
    *  Deletes a url.
    */
   public void doDeleteurl (RunData data, TemplateContext context)
        throws Exception
    {                          
        if (isCollision(data, context)) 
        {
            return;
        }
        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue(false);
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }

        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        ActivitySet activitySet = null;
        for (int i=0; i < keys.length; i++)
        {
            String key = (String) keys[i];
            if (key.startsWith("url_delete_"))
            {
                String attachmentId = key.substring(11);
                Attachment attachment = AttachmentManager
                    .getInstance(new NumberKey(attachmentId), false);
                activitySet = issue.doDeleteUrl(activitySet, attachment, user);
            }
        }
        if (activitySet != null)
        {
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
            sendEmail(activitySet, issue, l10n.get("UrlDeleted"), 
                      context);
        }
        else
        {
            scarabR.setInfoMessage(l10n.get("NoUrlsChanged"));
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
        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue(false);
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }

        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        ActivitySet activitySet = null;
        for (int i =0; i<keys.length; i++)
        {
            String key = (String) keys[i];
            if (key.startsWith("file_delete_"))
            {
                String attachmentId = key.substring(12);
                Attachment attachment = AttachmentManager
                    .getInstance(new NumberKey(attachmentId), false);
                activitySet = issue.doDeleteFile(activitySet, attachment, user);
            } 
        }
        if (activitySet != null)
        {
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
            sendEmail(activitySet, issue, l10n.get("FileDeleted"), 
                      context);
        }
        else
        {
            scarabR.setInfoMessage(l10n.get("NoFilesChanged"));
        }
    }

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    public void doDeletedependencies(RunData data, TemplateContext context)
        throws Exception
    {
        saveDependencyChanges(data, context, true);
    }

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    public void doSavedependencychanges(RunData data, TemplateContext context)
        throws Exception
    {
        saveDependencyChanges(data, context, false);
    }

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    private void saveDependencyChanges(RunData data, TemplateContext context, 
                                       boolean doDelete)
        throws Exception
    {
        if (isCollision(data, context)) 
        {
            return;
        }

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue(false);
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }

        ScarabUser user = (ScarabUser)data.getUser();
        if (!user.hasPermission(ScarabSecurity.ISSUE__EDIT, 
                               issue.getModule()))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        Group group = intake.get("Depend", IntakeTool.DEFAULT_KEY);
        String reasonForChange = group.get("Description").toString();

        boolean depAdded = doAdddependency(issue, intake, group, scarabR,
                                           context, l10n, user,
                                           reasonForChange);
        boolean changesMade = doUpdatedependencies(issue, intake, scarabR, 
                                                   context, l10n, user, 
                                                   reasonForChange, doDelete);
        if (!depAdded)
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
        if (!depAdded && !changesMade)
        {
            scarabR.setInfoMessage(l10n.get(NO_CHANGES_MADE));
        }
        else
        {
            intake.remove(group);
        }
    }

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    private boolean doAdddependency(Issue issue, IntakeTool intake, 
                                 Group group, ScarabRequestTool scarabR,
                                 TemplateContext context,
                                 ScarabLocalizationTool l10n,
                                 ScarabUser user,
                                 String reasonForChange)
        throws Exception
    {
        // Check that dependency type entered is valid
        Field type = group.get("TypeId");
        type.setRequired(true);
        // Check that child ID entered is valid
        Field childId = group.get("ObserverUniqueId");
        childId.setRequired(true);
        if (!type.isValid() && childId.isValid())
        {
            type.setMessage(l10n.get("EnterValidDependencyType"));
            return false;
        }
        else if (type.isValid() && !childId.isValid())
        {
            childId.setMessage(l10n.get("EnterValidIssueId"));
            return false;
        }
        String childIdStr = childId.toString();
        // we need to struggle here because if there is no
        // issue id, we just want to return because the person
        // on the page could just be updating existing deps
        // and in this case, the issue id might be empty.
        if (childIdStr != null)
        {
            childIdStr.trim();
        }
        if (childIdStr == null || childIdStr.length() == 0)
        {
            return true;
        }
        // Check that child ID entered corresponds to a valid issue
        // The id might not have the prefix appended so use the current
        // module prefix as the thing to try.
        Issue childIssue = null;
        try
        {
            childIssue = scarabR.getCurrentModule()
                                .getIssueById(childIdStr);
        }
        catch(Exception e)
        {
            // Ignore this
        }
        if (childIssue == null)
        {
            childId.setMessage(l10n.get("EnterValidIssueId"));
            return false;
        }
        // Make sure issue is not being marked as dependant on itself.
        else if (childIssue.equals(issue))
        {
            childId.setMessage(l10n.get("CannotAddSelfDependency"));
            return false;
        }
        if (intake.isAllValid())
        {
            Depend depend = DependManager.getInstance();
            depend.setDefaultModule(scarabR.getCurrentModule());
            group.setProperties(depend);
            ActivitySet activitySet = null;
            try
            {
                activitySet = issue
                    .doAddDependency(activitySet, depend, childIssue, user);
            }
            catch (ScarabException se)
            {
                scarabR.setAlertMessage(l10n.get(se.getMessage()));
                return false;
            }
            catch (Exception e)
            {
                log().debug("Delete error: ", e);
                return false;
            }

            if (activitySet != null)
            {
                // FIXME: I think that we are sending too many emails here
                sendEmail(activitySet, childIssue, l10n.get(DEFAULT_MSG), 
                          context);
                sendEmail(activitySet, issue, l10n.get(DEFAULT_MSG), 
                          context);
            }
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     *  Modifies the dependency type between the current issue
     *  And its parent or child issue.
     */
    private boolean doUpdatedependencies(Issue issue, IntakeTool intake, 
                                       ScarabRequestTool scarabR,
                                       TemplateContext context,
                                       ScarabLocalizationTool l10n,
                                       ScarabUser user,
                                       String reasonForChange,
                                       boolean doDelete)
        throws Exception
    {
        ActivitySet activitySet = null;
        List dependencies = issue.getAllDependencies();            
        for (int i=0; i < dependencies.size(); i++)
        {
            Depend oldDepend = (Depend)dependencies.get(i);
            Depend newDepend = DependManager.getInstance();
            // copy oldDepend properties to newDepend
            newDepend.setProperties(oldDepend);
            Group group = intake.get("Depend", oldDepend.getQueryKey(), false);
            // there is nothing to doo here, so move along now kiddies.
            if (group == null)
            {
                continue;
            }

            DependType oldDependType = oldDepend.getDependType();
            // set properties on the object
            group.setProperties(newDepend);
            DependType newDependType = newDepend.getDependType();

            // set the description of the changes
            newDepend.setDescription(reasonForChange);

            // make the changes
            if (doDelete && newDepend.getDeleted())
            {
                try
                {
                    activitySet = 
                        issue.doDeleteDependency(activitySet, oldDepend, user);
                }
                catch (ScarabException se)
                {
                    // it will error out if they attempt to delete
                    // a dep via a child dep.
                    scarabR.setAlertMessage(l10n.get(se.getMessage()));
                }
                catch (Exception e)
                {
                    scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
                    log().debug("Delete error: ", e);
                }
            }
            else if (! oldDependType.equals(newDependType))
            {
                // need to do this because newDepend could have the deleted
                // flag set to true if someone selected it as well as 
                // clicked the save changes button. this is why we have the 
                // doDeleted flag as well...issue.doChange will only do the
                // change if the deleted flag is false...so force it...
                newDepend.setDeleted(false);
                // make the changes
                activitySet = 
                    issue.doChangeDependencyType(activitySet, oldDepend,
                                                 newDepend, user);
            }
            intake.remove(group);
        }

        // something changed...
        if (activitySet != null)
        {
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
            
            // FIXME: when we add a dep, we send email to both issues,
            // but here we are not...should we? it almost seems like 
            // to much email. We need someone to define this behavior
            // better. (JSS)
            sendEmail(activitySet, issue, l10n.get(DEFAULT_MSG), context);
            return true;
        }
        else // nothing changed
        {
            return false;
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
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getIssue(false);
        if (issue == null)
        {
            // no need to set the message here as
            // it is done in scarabR.getIssue()
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
        if (user.hasPermission(ScarabSecurity.ISSUE__ASSIGN, 
                               issue.getModule()))
        {
            // call it issue_ids because AssignIssue can be used to
            // assign to multiple issues at the same time. however, this
            // ui interface just sets one id.
            data.getParameters().add("issue_ids", issue.getUniqueId());
            scarabR.resetAssociatedUsers();
            setTarget(data, "AssignIssue.vm");
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
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
