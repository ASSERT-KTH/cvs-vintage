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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.RunData;
import org.apache.turbine.ParameterParser;

import org.apache.commons.collections.SequencedHashMap;
import org.apache.commons.collections.ExtendedProperties;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.upload.TurbineUploadService;
import org.apache.fulcrum.upload.UploadService;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.ActivitySetManager;
import org.tigris.scarab.om.ActivitySetTypePeer;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentType;
import org.tigris.scarab.om.AttachmentTypePeer;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.services.security.ScarabSecurity;

/**
 * This class is responsible for report issue forms.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: ReportIssue.java,v 1.143 2002/10/22 22:43:07 elicia Exp $
 */
public class ReportIssue extends RequireLoginFirstAction
{
    public void doCheckforduplicates(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = scarabR.getReportingIssue();
        
        try
        {
            // set any required flags
            setRequiredFlags(issue, intake);
        }
        catch (Exception e)
        {
            scarabR.setAlertMessage(
                l10n.format("ErrorExceptionMessage", e.getMessage()));
            setTarget(data, "entry,Wizard1.vm");
            return;
        }
        // set the values entered so far and if that is successful look
        // for duplicates
        if (setAttributeValues(issue, intake, context)) 
        {
            // check for duplicates, if there are none skip the dedupe page
            searchAndSetTemplate(data, context, 0, "entry,Wizard3.vm");
        }
        
        if (!intake.isAllValid())
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
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
        // remove special characters from the text attributes
        Iterator textAVs = search.getTextAttributeValues().iterator();        
        while (textAVs.hasNext()) 
        {
            AttributeValue av = (AttributeValue)textAVs.next();
            String s = av.getValue();
            if (s != null && s.length() > 0) 
            {
                StringTokenizer tokens = new StringTokenizer(s, 
                    ScarabConstants.INVALID_SEARCH_CHARACTERS);
                StringBuffer query = new StringBuffer(s.length() + 10);
                while (tokens.hasMoreTokens())
                {
                    query.append(' ');
                    query.append(tokens.nextToken());
                }
                av.setValue(query.toString());       
            }
        }
        List matchingIssues = search.getMatchingIssues();

        // set the template to dedupe unless none exist, then skip
        // to final entry screen
        String template = null;
        boolean dupThresholdExceeded = (matchingIssues.size() > threshold);
        if (dupThresholdExceeded)
        {
            context.put("issueList", matchingIssues);     
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
        List requiredAttributes = issue.getModule()
            .getRequiredAttributes(issueType);
        SequencedHashMap avMap = issue.getModuleAttributeValuesMap(); 
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
                
                for (int j=requiredAttributes.size()-1; j>=0; j--) 
                {
                    if (aval.getAttribute().getPrimaryKey().equals(
                            ((Attribute)requiredAttributes.get(j)).getPrimaryKey())
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
    private boolean setAttributeValues(Issue issue, IntakeTool intake, 
                                       TemplateContext context)
        throws Exception
    {
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        boolean success = false;
        // set any required flags on attribute values
        setRequiredFlags(issue, intake);
        if ( intake.isAllValid() ) 
        {
            Hashtable values = new Hashtable();
            SequencedHashMap avMap = issue.getModuleAttributeValuesMap();
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
                     * FIXME! I think changes in the code have made this
                     * hack unnecessary, but I do not have time to test
                     * this theory atm. -jdm
                     *
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
                             .getName().equals(ScarabConstants.DROPDOWN_LIST))
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
            // end code related to issue 70
    
            success = true;
        }
        else
        {
            getScarabRequestTool(context).setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
        return success;
    }

    /**
     * handles entering an issue
     */
    public void doEnterissue(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getReportingIssue();
        IssueType issueType = issue.getIssueType();
        ScarabUser user = (ScarabUser)data.getUser();
        
        // set the attribute values and if that was successful save the issue.
        if (setAttributeValues(issue, intake, context))
        {
            if (issue.containsMinimumAttributeValues())
            {
                // we need to see that the default text was filled out 
                // if necessary.  We can
                // only do this after setting the attributes above.
                boolean saveIssue = true;
                String summary = issue.getDefaultText();
                Group commentGroup = intake.get("Attachment", "_1", false);
                Field commentField = commentGroup.get("Data");
                if ( summary == null || summary.length() == 0 ) 
                {
                    commentField.setRequired(true);
                    saveIssue = false;
                }

                // If there is a default text attribute,or if a comment has 
                // Been provided, proceed.
                if (commentField.isValid() || saveIssue)
                {
                    HashMap newValues = new HashMap();
                    AttributeValue aval = null;
                    AttributeValue aval2 = null;
                    List modAttrs = issue.getModule().getRModuleAttributes(issue.getIssueType(), true, "all");

                    // this is used for the workflow stuff...FIXME: it should
                    // be refactored as soon as we possibly can. the reason is
                    // that all of this data can be retrieved by simply using
                    // issue.getModuleAttributeValuesMap() because the call
                    // to setAttributeValues() above already gets the group
                    // information into the module attribute values.
                    for (int i = 0; i<modAttrs.size(); i++)
                    {
                        Attribute attr = ((RModuleAttribute)modAttrs.get(i)).getAttribute();
                        String queryKey = "__" + attr.getAttributeId().toString();
                        Group group = intake.get("AttributeValue", queryKey, false);
                        String newValue = "";

                        if (group != null) 
                        {
                            if (attr.isOptionAttribute())
                            {
                                newValue = group.get("OptionId").toString();
                            }
                            else 
                            {
                                newValue = group.get("Value").toString();
                            }
                            if (newValue.length() != 0)
                            {
                                newValues.put(attr.getAttributeId(), newValue);
                            }
                        }
                    }
        
                    ActivitySet activitySet = null;
                    try
                    {
                        activitySet = issue.setInitialAttributeValues(null, newValues, user);
                    }
                    catch (Exception se)
                    {
                        scarabR.setAlertMessage(se.getMessage());
                        return;
                    }
                 
            
                    // save the comment
                    Attachment comment = new Attachment();
                    commentField.setProperty(comment);
                    if ( comment.getData() != null 
                         && comment.getData().length() > 0) 
                    {
                        issue.addComment(comment, (ScarabUser)data.getUser());     
                    }
                    
                    // set the template to the user selected value
                    int templateCode = data.getParameters()
                        .getInt("template_code", 2);
                
                    // if user preference for next template is unset,
                    // set it.
                    int userPref = user.getEnterIssueRedirect();
                    if (userPref == 0 || userPref != templateCode)
                    {
                        user.setEnterIssueRedirect(templateCode);
                    }
                    doRedirect(data, context, templateCode, issue);
                
                    // send email
                    if ( summary.length() == 0 ) 
                    {
                        summary = comment.getData();
                    }
                    if ( summary.length() > 60 ) 
                    {
                        summary = summary.substring(0,60) + "...";
                    }                
                    summary = (summary.length() == 0) ? summary : " - " + summary;
                    
                    String[] args = { 
                        issue.getModule().getRealName().toUpperCase(), 
                        issue.getUniqueId(), summary};
                    String subj = l10n.format("IssueAddedEmailSubject", args);
                
                    if (!activitySet.sendEmail(
                         new ContextAdapter(context), issue, 
                         subj, "email/NewIssueNotification.vm"))
                    {
                        scarabR.setInfoMessage(
                            l10n.get("IssueSavedButEmailError"));
                    }
                    cleanup(data, context);
                    data.getParameters().add("id", issue.getUniqueId().toString());
                    scarabR.setConfirmMessage(l10n.format("IssueAddedToModule",
                        issue.getUniqueId(), getScarabRequestTool(context)
                        .getCurrentModule().getRealName()));
                }
                else
                {
                    scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
                }
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
        Attachment attachment = new Attachment();
        Group group = intake.get("Attachment", 
                                 attachment.getQueryKey(), false);

        if (ModifyIssue.addFileAttachment(issue, group, attachment, 
                                  scarabR, data, intake))
        {        
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            scarabR.setConfirmMessage(l10n.get("FileAdded"));
        }

        // set any attribute values that were entered before adding the file.
        setAttributeValues(issue, intake, context);
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
        // set any attribute values that were entered before adding the file.
        setAttributeValues(scarabR.getReportingIssue(), intake, context);
        doGotowizard3(data, context);
    }
    
    
    
    /**
     * Handles adding a note to an issue
     */
    public void doAddnote(RunData data, TemplateContext context) 
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);        
        ScarabLocalizationTool l10n = getLocalizationTool(context);
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
                    && attachment.getData().length() > 0)
                {
                    ScarabRequestTool scarabR = getScarabRequestTool(context);
                    List issues = scarabR.getIssues();
                    for (int i=0; i < issues.size(); i++)
                    {
                        Issue issue = (Issue)issues.get(i);
                        issue.addComment(attachment, (ScarabUser)data.getUser());
                    }
                    
                    scarabR.setConfirmMessage(l10n.get("CommentAdded"));
                    // if there was only one duplicate issue and we just added
                    // a note to it, assume user is done
                    String nextTemplate = 
                        ((ScarabUser)data.getUser()).getHomePage();
                    if (! searchAndSetTemplate(data, context, 1, nextTemplate))
                    {
                        cleanup(data, context);
                    }
                }
                else 
                {
                    getScarabRequestTool(context).setAlertMessage(
                        l10n.get("NoTextInNotesTextArea"));
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
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        if (intake.isAllValid()) 
        {
            Group group = intake.get("Issue", IntakeTool.DEFAULT_KEY);        
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            Issue issue = scarabR.getIssue();
            
            try
            {
                issue.addVote((ScarabUser)data.getUser());
                scarabR.setConfirmMessage(
                    l10n.format("VoteForIssueAccepted", issue.getUniqueId()));
                // if there was only one duplicate issue and the user just
                // voted for it, assume user is done
                String nextTemplate = 
                    ((ScarabUser)data.getUser()).getHomePage();
                if (! searchAndSetTemplate(data, context, 1, nextTemplate))
                {
                    cleanup(data, context);
                }
            }
            catch (ScarabException e)
            {
                scarabR.setAlertMessage(
                    l10n.format("VoteFailedException", e.getMessage()));
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
        getIntakeTool(context).removeAll();
        String templateId = data.getParameters().getString("select_template_id");
        if (templateId != null && templateId.length() > 0)
        {
            data.getParameters().add("templateId", templateId);
        }
       
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
    
    /**
     * User selects page to redirect to after entering issue.
     */
    private void doRedirect(RunData data, TemplateContext context, 
                            int templateCode, Issue issue)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        String template = null;
        switch (templateCode)
        {
            case 1: 
                if (user.hasPermission(ScarabSecurity.ISSUE__ENTER, 
                                       user.getCurrentModule()))
                {
                    template = scarabR.getNextEntryTemplate();
                }
                else 
                {
                    template = user.getHomePage();
                    scarabR.setAlertMessage(
                        l10n.get("InsufficientPermissionsToEnterIssues"));
                }
               break;
            case 2: 
                if (user.hasPermission(ScarabSecurity.ISSUE__ASSIGN, 
                                       user.getCurrentModule()))
                {
                    template = "AssignIssue.vm";
                    data.getParameters().add("issue_ids", 
                                             issue.getUniqueId());
                    data.getParameters().add(ScarabConstants.CANCEL_TEMPLATE, 
                                             "ViewIssue.vm");
                    data.getParameters()
                        .add("id", issue.getUniqueId().toString());
                }
                else 
                {
                    template = user.getHomePage();
                    scarabR.setAlertMessage(
                        l10n.get("InsufficientPermissionsToAssignIssues"));
                }
               break;
            case 3: 
                if (user.hasPermission(ScarabSecurity.ISSUE__VIEW, 
                                       user.getCurrentModule()))
                {
                    template = "ViewIssue.vm";
                    data.getParameters()
                        .add("id",issue.getUniqueId().toString());
                }
                else 
                {
                    template = user.getHomePage();
                    scarabR.setAlertMessage(
                        l10n.get("InsufficientPermissionsToViewIssues"));
                }
               break;
            case 4: 
               template = "Index.vm";
        } 
        setTarget(data, template);
    }

}


