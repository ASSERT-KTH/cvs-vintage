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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.RunData;
import org.apache.turbine.ParameterParser;

import org.apache.commons.util.SequencedHashtable;
import org.apache.commons.collections.ExtendedProperties;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.upload.FileItem;
import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.upload.TurbineUploadService;
import org.apache.fulcrum.upload.UploadService;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentType;
import org.tigris.scarab.om.AttachmentTypePeer;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.TransactionTypePeer;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.tools.ScarabRequestTool;

/**
 * This class is responsible for report issue forms.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: ReportIssue.java,v 1.96 2002/01/22 01:58:09 elicia Exp $
 */
public class ReportIssue extends RequireLoginFirstAction
{
    private static final String ERROR_MESSAGE = "More information was " +
        "required to submit your request. Please " +
        "scroll down to see error messages."; 
    
    public void doCheckforduplicates(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = scarabR.getReportingIssue();
        IssueType issueType = scarabR.getIssueType();
        
        try
        {
            // set any required flags
            setRequiredFlags(issue, intake);
        }
        catch (Exception e)
        {
            data.setMessage("Error: " + e.getMessage());
            setTarget(data, "entry,Wizard1.vm");
            return;
        }
        if (intake.isAllValid()) 
        {
            // set the values entered so far
            setAttributeValues(issue, intake, context);
            
            // check for duplicates, if there are none skip the dedupe page
            searchAndSetTemplate(data, context, 0, "entry,Wizard3.vm");
        }
        
        // we know we started at Wizard1 if we are here, Wizard3 needs
        // to know where the issue entry process starts because it may
        // branch back
        data.getParameters()
            .add(ScarabConstants.HISTORY_SCREEN, "entry,Wizard1.vm");
        //getLinkTool(context).setHistoryScreen("entry,Wizard1.vm");
    }
    
    /**
     * Common code related to deduping.  A search for duplicate issues is
     * performed and if the number of possible duplicates is greater than 
     * the threshold, the results are placed in the ScarabRequestTool and 
     * the screen is set to entry,Wizard2.vm so that they can be viewed.
     *
     * @param data a <code>RunData</code> value
     * @param context a <code>TemplateContext</code> value
     * @param threshold an <code>int</code> number of issues that determines
     * whether "entry,Wizard2.vm" screen  or the screen given by
     * nextTemplate is shown
     * @param nextTemplate a <code>String</code> screen name to branch to
     * if the number of duplicate issues is less than or equal to the threshold
     * @return true if the number of possible duplicates is greater than the
     * threshold
     * @exception Exception if an error occurs
     */
    private boolean searchAndSetTemplate(RunData data, 
                                         TemplateContext context, 
                                         int threshold, 
                                         String nextTemplate)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        //ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = scarabR.getReportingIssue();
        
        // search on the option attributes and keywords
        IssueSearch search = new IssueSearch(issue);                
        List matchingIssues = search.getMatchingIssues(25);
        
        // set the template to dedupe unless none exist, then skip
        // to final entry screen
        String template = null;
        boolean dupThresholdExceeded = (matchingIssues.size() > threshold);
        if (dupThresholdExceeded)
        {
            scarabR.setIssueList(matchingIssues);
            template = "entry,Wizard2.vm";
        }
        else
        {
            template = nextTemplate;
        }
        
        setTarget(data, template);
        return dupThresholdExceeded;
    }
    
    /**
     * Checks the Module the issue is being entered into to see what
     * attributes are required to have values. If a required field was present
     * and the user did not enter anything, intake is notified that the
     * field was required.
     *
     * @param issue an <code>Issue</code> value
     * @param intake an <code>IntakeTool</code> value
     * @exception Exception if an error occurs
     */
    private void setRequiredFlags(Issue issue, IntakeTool intake)
        throws Exception
    {
        if (issue == null)
        {
            throw new Exception ("The Issue is not valid any longer. " + 
                                     "Please try again.");
        }
        IssueType issueType = issue.getIssueType();
        Attribute[] requiredAttributes = issue.getModule()
            .getRequiredAttributes(issueType);
        SequencedHashtable avMap = issue.getModuleAttributeValuesMap(); 
        Iterator iter = avMap.iterator();
        while (iter.hasNext()) 
        {
            AttributeValue aval = (AttributeValue)avMap.get(iter.next());
            
            Group group = 
                intake.get("AttributeValue", aval.getQueryKey(), false);
            if (group != null) 
            {            
                Field field = null;
                if (aval instanceof OptionAttribute) 
                {
                    field = group.get("OptionId");
                }
                else if (aval instanceof UserAttribute) 
                {
                    field = group.get("UserId");
                }
                else 
                {
                    field = group.get("Value");
                }
                
                for (int j=requiredAttributes.length-1; j>=0; j--) 
                {
                    if (aval.getAttribute().getPrimaryKey().equals(
                            requiredAttributes[j].getPrimaryKey())
                        && !aval.isSet())
                    {
                        field.setRequired(true);
                        break;
                    }                    
                }
            }
        }
    }
    
    /**
     * Add/Modify any attribute values that were just entered into intake.
     *
     * @param issue the <code>Issue</code> currently being editted 
     * @param intake an <code>IntakeTool</code> containing the fields for the
     * issue's attribute values.
     * @exception Exception pass thru
     */
    private void setAttributeValues(Issue issue, IntakeTool intake, TemplateContext context)
        throws Exception
    {
        Hashtable values = new Hashtable();
        SequencedHashtable avMap = issue.getModuleAttributeValuesMap();
        Iterator i = avMap.iterator();
        while (i.hasNext()) 
        {
            AttributeValue aval = (AttributeValue)avMap.get(i.next());
            Group group = 
                intake.get("AttributeValue", aval.getQueryKey(), false);
            if (group != null) 
            {
                group.setProperties(aval);
                
                /*
                 * The next piece of code is for storing the values
                 * of the attributes into the context (which is than
                 * used by Wizard2.vm) This is necessary because it 
                 * seems that group.setProperties(aval) does not 
                 * store the values so they are never passed to the
                 * next template (Wizard3.vm). This code fixes bug 
                 * http://scarab.tigris.org/issues/show_bug.cgi?id=70
                 */
                String field = null;
                if (aval.getAttribute().getAttributeType()
                    .getValidationKey() != null)
                {
                    field = aval.getAttribute().getAttributeType()
                        .getValidationKey();
                }
                else if (aval.getAttribute().getAttributeType()
                         .getName().equals("combo-box"))
                {
                    field = "OptionId";
                }
                else
                {
                    field = "Value";
                }
                Object key = group.get(field).getKey();
                Object value = group.get(field).getValue();
                if ((key != null) && (value != null)) 
                {
                    values.put(group.get(field).getKey()
                                   , group.get(field).getValue());
                }
            }
        }
        context.put("wizard1_intake", values);
    }
    
    /**
     * handles entering an issue
     */
    public void doEnterissue(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = scarabR.getReportingIssue();
        IssueType issueType = issue.getIssueType();
        ScarabUser user = (ScarabUser)data.getUser();
        
        // set any required flags
        setRequiredFlags(issue, intake);
        String summary = issue.getDefaultText();
        if ( summary.length() == 0 ) 
        {
            Group commentGroup = intake.get("Attachment", "_1", false);
            Field commentField = commentGroup.get("DataAsString");
            commentField.setRequired(true);
        }

        if (intake.isAllValid())
        {
            setAttributeValues(issue, intake, context);
            if (issue.containsMinimumAttributeValues())
            {
                // Save transaction record
                Transaction transaction = new Transaction();
                transaction
                    .create(TransactionTypePeer.CREATE_ISSUE__PK, user, null);
                
                // enter the values into the transaction
                SequencedHashtable avMap = 
                    issue.getModuleAttributeValuesMap(); 
                Iterator i = avMap.iterator();
                while (i.hasNext()) 
                {
                    AttributeValue aval = (AttributeValue)avMap.get(i.next());
                    try
                    {
                        aval.startTransaction(transaction);
                    }
                    catch (ScarabException se)
                    {
                        data.setMessage("Fatal Error: " + se.getMessage() 
                                            + " Please start over.");
                        setTarget(data, "entry,Wizard1.vm");
                        return;
                    }
                }
                issue.save();
                
                List files = issue.getAttachments();
                for (int k = 0; k < files.size(); k++)
                {
                    Attachment attachment = (Attachment)files.get(k);
                    if (attachment.getData() != null 
                        && attachment.getData().length > 0)
                    {
                        FileItem file = attachment.getFile();
                        String fileNameWithPath =file.getFileName();
                        String fileName = fileNameWithPath
                            .substring(fileNameWithPath.lastIndexOf(File.separator)+1);
                        
                        attachment.setData(null);
                        attachment.setCreatedBy(user.getUserId());
                        attachment.setAttachmentType(AttachmentType
                                                         .getInstance(AttachmentTypePeer.ATTACHMENT_TYPE_NAME));
                        attachment.setIssue(issue);
                        // FIXME! this duplicates setAttachmentType from two
                        // lines above, it should not be needed.
                        attachment.setTypeId(new NumberKey(1));
                        attachment.save();    
                        
                        String uploadFile = attachment
                            .getRepositoryDirectory(scarabR.getIssue().getModule().getCode())
                            + File.separator + fileName.substring(0, fileName.lastIndexOf('.')) + "_" 
                            + attachment.getPrimaryKey().toString() 
                            + fileName.substring(fileName.lastIndexOf('.')); 
                        
                        file.write(uploadFile);
                        attachment.setFilePath(uploadFile);
                        attachment.save();
                        
                    }
                    
                }

                // save the comment
                Group commentGroup = intake.get("Attachment", "_1", false);
                Attachment comment = new Attachment();
                commentGroup.setProperties(comment);
                issue.addComment(comment, (ScarabUser)data.getUser());

                // set the template to the user selected value
                String template = data.getParameters()
                    .getString(ScarabConstants.NEXT_TEMPLATE, "ViewIssue.vm");
                if (template != null && template.equals("AssignIssue.vm"))
                {
                    data.getParameters().add("issue_ids", 
                                             issue.getIssueId().toString());
                }
                setTarget(data, template);
                
                // send email
                if ( summary.length() == 0 ) 
                {
                    summary = comment.getDataAsString();
                }
                if ( summary.length() > 60 ) 
                {
                    summary = summary.substring(0,60) + "...";
                }                
                summary = (summary.length() == 0) ? summary : " - " + summary;
                StringBuffer subj = new StringBuffer("[");
                subj.append(issue.getModule().getRealName().toUpperCase());
                subj.append("] Issue #").append(issue.getUniqueId());
                subj.append(summary);
            
                if (!transaction.sendEmail(new ContextAdapter(context), issue, 
                                      subj.toString(),
                                      "email/NewIssueNotification.vm"))
                {
                    data.setMessage("Your issue was saved, but could not send "
                                    + "notification email due to a sendmail error.");
                }
                cleanup(data, context);
                data.getParameters().add("id", issue.getUniqueId().toString());
                data.setMessage("Issue " + issue.getUniqueId() +
                                " added to module " +
                                getScarabRequestTool(context)
                                .getCurrentModule().getName());
            }
            else 
            {
                // this would be an application or hacking error
            }            
        }
    }
    
    /**
     * Add attachment file
     */
    public void doAddfile(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = scarabR.getReportingIssue();
        IssueType issueType = issue.getIssueType();
        ScarabUser user = (ScarabUser)data.getUser();
        Attachment attachment = new Attachment();
        Group group = intake.get("Attachment", 
                                 attachment.getQueryKey(), false);
        
        Field nameField = group.get("Name"); 
        nameField.setRequired(true);
        // set any required flags
        setRequiredFlags(issue, intake);

        if (!nameField.isValid())
        {
            nameField.setMessage("This field requires a value.");
        }
        if (intake.isAllValid())
        {
            if (group != null) 
            {
                group.setProperties(attachment);
                if (attachment.getData() != null 
                    && attachment.getData().length > 0)
                {
                    issue.addFile(attachment);
                }
                data.getParameters().add("intake-grp", "issue"); 
                data.getParameters().add("id",issue.getUniqueId().toString());
            }
        }
        else
        {
            data.setMessage(ERROR_MESSAGE);
        }
        doGotowizard3(data, context);        
    }
    
    /**
     * Remove an attachment file
     */
    public void doRemovefile(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue reportingIssue = scarabR.getReportingIssue();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attachmentIndex;
        
        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("file_delete_"))
            {
                attachmentIndex = key.substring(12);
                reportingIssue.removeFile(attachmentIndex);
            } 
        }
        data.getParameters().add("intake-grp", "issue"); 
        data.getParameters().add("id", reportingIssue.getUniqueId().toString());
        
        doGotowizard3(data, context);
    }
    
    
    
    /**
     * Handles adding a note to an issue
     */
    public void doAddnote(RunData data, TemplateContext context) 
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);        
        if (intake.isAllValid())
        {
            // save the attachment
            Attachment attachment = new Attachment();
            Group group = intake.get("Attachment", 
                                     attachment.getQueryKey(), false);
            if (group != null)
            {
                group.setProperties(attachment);
                if (attachment.getData() != null 
                    && attachment.getData().length > 0)
                {
                    ScarabRequestTool scarabR = getScarabRequestTool(context);
                    Issue issue = scarabR.getIssue();
                    issue.addComment(attachment, (ScarabUser)data.getUser());
                    
                    data.setMessage("Your comment for artifact #" + 
                                        issue.getUniqueId() + 
                                        " has been added.");
                    // if there was only one duplicate issue and we just added
                    // a note to it, assume user is done
                    String nextTemplate = Turbine.getConfiguration()
                        .getString("template.homepage", "Index.vm");
                    if (! searchAndSetTemplate(data, context, 1, nextTemplate))
                    {
                        cleanup(data, context);
                    }
                }
                else 
                {
                    data.setMessage(
                        "No text was entered into the Notes textarea.");
                    searchAndSetTemplate(data, context, 0, "entry,Wizard2.vm");
                }
            }
        }
        else 
        {
            // Comment was probably too long.  Repopulate the issue list, so
            // the page can be shown again, and the user can fix the comment.
            searchAndSetTemplate(data, context, 0, "entry,Wizard2.vm");
        }
    }
    
    /**
     * The button for this action is commented out on Wizard2, so it
     * will not be called
     */
    public void doAddvote(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid()) 
        {
            Group group = intake.get("Issue", IntakeTool.DEFAULT_KEY);        
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            Issue issue = scarabR.getIssue();
            
            try
            {
                issue.addVote((ScarabUser)data.getUser());
                data.setMessage("Your vote for artifact #" + issue.getUniqueId() 
                                    + " has been accepted.");
                // if there was only one duplicate issue and the user just
                // voted for it, assume user is done
                String nextTemplate = Turbine.getConfiguration()
                    .getString("template.homepage", "Index.vm");
                if (! searchAndSetTemplate(data, context, 1, nextTemplate))
                {
                    cleanup(data, context);
                }
            }
            catch (ScarabException e)
            {
                data.setMessage("Vote could not be added.  Reason given: "
                                    + e.getMessage());
                // User attempted to vote when they were not allowed.  This
                // should probably not be allowed in the ui, but right now
                // it is and we should protect against url hacking anyway.
                // Repopulate the data so the dedupe page can be shown again.
                searchAndSetTemplate(data, context, 0, "entry,Wizard2.vm");
            }
        }
        else 
        {
            // Not sure this case needs to be covered, but just to be safe
            // repopulate the data so the dedupe page can be shown again.
            searchAndSetTemplate(data, context, 0, "entry,Wizard2.vm");
        }
    }
    
    public void doGotowizard3(RunData data, TemplateContext context)
        throws Exception
    {
        setTarget(data, "entry,Wizard3.vm");
    }
    
    public void doUsetemplates(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        intake.removeAll();
        String template = getCurrentTemplate(data, null);
        setTarget(data, template);
    }
    
    private void cleanup(RunData data, TemplateContext context)
    {
        data.getParameters().remove(ScarabConstants.HISTORY_SCREEN);
        String issueKey = data.getParameters()
            .getString(ScarabConstants.REPORTING_ISSUE);
        ((ScarabUser)data.getUser()).setReportingIssue(issueKey, null);
        data.getParameters().remove(ScarabConstants.REPORTING_ISSUE);
        getScarabRequestTool(context).setReportingIssue(null);
        IntakeTool intake = getIntakeTool(context);
        intake.removeAll();
    }
    
    /*
     private String getStartPoint()
     throws Exception
     {
     String historyScreen = data.getParameters()
     .getString(ScarabConstants.HISTORY_SCREEN);
     if (historyScreen == null) 
     {
     historyScreen = "entry,Wizard3.vm";            
     }
     
     return historyScreen;
     }
     */
}


