package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
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
 * software developed by CollabNet <http://www.collab.net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of CollabNet.
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
 * individuals on behalf of CollabNet.
 */ 

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.ParameterParser;

import org.apache.commons.collections.SequencedHashMap;

import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.localization.Localization;


// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentManager;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.util.word.QueryResult;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.util.EmailContext;

/**
 * This class is responsible for report issue forms.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: ReportIssue.java,v 1.164 2003/03/19 01:10:04 jon Exp $
 */
public class ReportIssue extends RequireLoginFirstAction
{
    private static final int MAX_RESULTS = 25;
    
    /**
     * Calls do check for duplicates by default.
     */
    public void doPerform(RunData data, TemplateContext context)
        throws Exception
    {
        doCheckforduplicates(data, context);
    }
    
    public void doCheckforduplicates(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = scarabR.getReportingIssue();
        SequencedHashMap avMap = issue.getModuleAttributeValuesMap(); 
        try
        {
            // set the values entered so far and if that is successful look
            // for duplicates
            if (setAttributeValues(issue, intake, context, avMap)) 
            {
                // check for duplicates, if there are none skip the dedupe page
                searchAndSetTemplate(data, context, 0, MAX_RESULTS, issue, "entry,Wizard3.vm");
            }
        }
        catch (Exception e)
        {
            scarabR.setAlertMessage(
                l10n.format("ErrorExceptionMessage", e.getMessage()));
            setTarget(data, "entry,Wizard1.vm");
            return;
        }

        // we know we started at Wizard1 if we are here, Wizard3 needs
        // to know where the issue entry process starts because it may
        // branch back
        data.getParameters()
            .add(ScarabConstants.HISTORY_SCREEN, "entry,Wizard1.vm");
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
     * @param maxResults a <code>int</code> number of issues that are returned
     * as potential duplicates
     * @param nextTemplate a <code>String</code> screen name to branch to
     * if the number of duplicate issues is less than or equal to the threshold
     * @return true if the number of possible duplicates is greater than the
     * threshold
     * @exception Exception if an error occurs
     */
    private boolean searchAndSetTemplate(RunData data,
                                         TemplateContext context,
                                         int threshold,
                                         int maxResults,
                                         Issue issue,
                                         String nextTemplate)
        throws Exception
    {
        // if all of the attributes are unset, then we don't need to run
        // this query...just break out of it early...
        List attributeValues = issue.getAttributeValues();
        boolean hasSetValues = false;
        for (Iterator itr = attributeValues.iterator();
             itr.hasNext() && !hasSetValues;) 
        {
            AttributeValue attVal = (AttributeValue) itr.next();
            hasSetValues = attVal.isSet();
        }
        if (!hasSetValues)
        {
            setTarget(data, nextTemplate);
            return true;
        }

        // search on the option attributes and keywords
        IssueSearch search = new IssueSearch(issue);                

        // remove special characters from the text attributes
        for (Iterator textAVs = search.getTextAttributeValues().iterator();
             textAVs.hasNext();)
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
        
        
        List queryResults = search.getQueryResults();

        // set the template to dedupe unless none exist, then skip
        // to final entry screen
        String template = null;
        boolean dupThresholdExceeded = (queryResults.size() > threshold);
        if (dupThresholdExceeded)
        {
            List matchingIssueIds = new ArrayList(maxResults);
            // limit the number of matching issues to maxResults
            for (int i = 0; i < queryResults.size() && i <= maxResults; i++) 
            {
                matchingIssueIds.add(
                    ((QueryResult)queryResults.get(i)).getUniqueId());
            }
            context.put("issueList", matchingIssueIds);
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
    private void setRequiredFlags(Issue issue, IntakeTool intake,
                                  SequencedHashMap avMap)
        throws Exception
    {
        if (issue == null)
        {
            throw new Exception(Localization.getString("IssueNoLongerValid"));
        }
        IssueType issueType = issue.getIssueType();
        List requiredAttributes = issue.getModule()
            .getRequiredAttributes(issueType);
        for (Iterator iter = avMap.iterator(); iter.hasNext();)
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
                                       TemplateContext context,
                                       SequencedHashMap avMap)
        throws Exception
    {
        boolean success = false;
        // set any required flags on attribute values
        setRequiredFlags(issue, intake, avMap);
        if (intake.isAllValid()) 
        {
            for (Iterator i = avMap.iterator();i.hasNext();) 
            {
                AttributeValue aval = (AttributeValue)avMap.get(i.next());
                Group group = 
                    intake.get("AttributeValue", aval.getQueryKey(), false);
                if (group != null) 
                {
                    Field field = group.get(aval instanceof OptionAttribute ?
                                            "OptionId" : "Value");
                    String value = field.toString();

                    if (value != null && value.length() > 0)
                    {
                        group.setProperties(aval);
                    }
                }
            }
            success = true;
        }
        else
        {
            getScarabRequestTool(context).setAlertMessage(
                getLocalizationTool(context).get(ERROR_MESSAGE));
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
        ScarabUser user = (ScarabUser)data.getUser();
        SequencedHashMap avMap = issue.getModuleAttributeValuesMap(); 

        // set the attribute values and if that was successful save the issue.
        if (setAttributeValues(issue, intake, context, avMap))
        {
            if (issue.containsMinimumAttributeValues())
            {
                // we need to see that the default text was filled out 
                // if necessary.  We can
                // only do this after setting the attributes above.
                boolean saveIssue = true;
                Group reasonGroup = intake.get("Attachment", "_1", false);
                Field reasonField = reasonGroup.get("Data");
                if (issue.getDefaultTextAttributeValue() == null)
                {
                    reasonField.setRequired(true);
                    saveIssue = false;
                }

                // if the reason field is to long, then show an error.
                String reasonString = reasonField.toString();
                if (reasonString != null && reasonString.length() > 254)
                {
                    reasonField.setMessage("intake_ReasonMustBeLessThan256Characters");
                    saveIssue = false;
                }
                // If there is a default text attribute,or if a comment has 
                // Been provided, proceed.
                if (reasonField.isValid() || saveIssue)
                {
                    HashMap newValues = new HashMap();
                    List modAttrs = issue.getModule()
                        .getRModuleAttributes(issue.getIssueType(), true, "all");

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
                    
                    // Save the Reason
                    ActivitySet activitySet = null;
                    Attachment reason = null;
                    try
                    {
                        // grab the comment data
                        reason = new Attachment();
                        reasonField.setProperty(reason);
                        activitySet = issue
                            .setInitialAttributeValues(activitySet, reason, newValues, user);
                    }
                    catch (Exception se)
                    {
                        scarabR.setAlertMessage(se.getMessage());
                        return;
                    }

                    // Save any unsaved attachments as part of this ActivitySet as well
                    activitySet = issue.doSaveFileAttachments(activitySet, user);

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
                    EmailContext ectx= new EmailContext();
                    ectx.setLocalizationTool(l10n);
                    ectx.setLinkTool((ScarabLink)context.get("link"));
                
                    if (!activitySet.sendEmail(ectx, issue, 
                         "NewIssueNotification.vm"))
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
        Attachment attachment = AttachmentManager.getInstance();
        Group group = intake.get("Attachment", 
                                 attachment.getQueryKey(), false);

        ModifyIssue
            .addFileAttachment(issue, group, attachment, scarabR, data, intake);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        if (scarabR.getAlertMessage() == null)
        {
            scarabR.setConfirmMessage(l10n.get("FileAdded"));
        }

        SequencedHashMap avMap = issue.getModuleAttributeValuesMap(); 
        // set any attribute values that were entered before adding the file.
        setAttributeValues(issue, intake, context, avMap);
        doGotowizard3(data, context);
    }
    
    /**
     * Remove an attachment file
     */
    public void doRemovefile(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Issue issue = scarabR.getReportingIssue();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attachmentIndex;
        boolean fileDeleted = false;
        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("file_delete_"))
            {
                attachmentIndex = key.substring(12);
                issue.removeFile(attachmentIndex);
                fileDeleted = true;
            }
        }
        if (fileDeleted)
        {
            scarabR.setConfirmMessage(l10n.get("FileDeleted"));
        }
        else
        {
            scarabR.setConfirmMessage(l10n.get("NoFilesChanged"));
        }
        SequencedHashMap avMap = issue.getModuleAttributeValuesMap(); 
        // set any attribute values that were entered before adding the file.
        setAttributeValues(issue, getIntakeTool(context), context, avMap);
        doGotowizard3(data, context);
    }
    
    /**
     * Handles adding a comment to one or more issues. This is an option
     * which is available on Wizard2 during the dedupe process.
     */
    public void doAddcomment(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IntakeTool intake = getIntakeTool(context);        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Issue issue = scarabR.getReportingIssue();
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
                    List issues = scarabR.getIssues();
                    if (issues == null || issues.size() == 0)
                    {
                        scarabR.setAlertMessage(l10n.get("NoIssuesSelected"));
                        searchAndSetTemplate(data, context, 0, MAX_RESULTS, issue, "entry,Wizard2.vm");
                        return;
                    }
                    ActivitySet activitySet = null;
                    for (int i=0; i < issues.size(); i++)
                    {
                        Issue prevIssue = (Issue)issues.get(i);
                        activitySet = 
                            prevIssue.addComment(activitySet, attachment, 
                                (ScarabUser)data.getUser());
                        EmailContext ectx= new EmailContext();
                        ectx.setLocalizationTool(l10n);
                        ectx.setLinkTool((ScarabLink)context.get("link"));
                        if (!activitySet.sendEmail(ectx, prevIssue))
                        {
                            scarabR.setInfoMessage(
                                l10n.get("CommentAddedButEmailError"));
                        }
                    }

                    scarabR.setConfirmMessage(l10n.get("CommentAdded"));
                    // if there was only one duplicate issue and we just added
                    // a comment to it, assume user is done
                    String nextTemplate = 
                        ((ScarabUser)data.getUser()).getHomePage();
                    if (! searchAndSetTemplate(data, context, 1, MAX_RESULTS, issue, nextTemplate))
                    {
                        cleanup(data, context);
                    }
                    else
                    {
                        intake.remove(group);
                    }
                    return;
                }
            }
            scarabR.setAlertMessage(
                l10n.get("NoTextInCommentTextArea"));
            searchAndSetTemplate(data, context, 0, MAX_RESULTS, issue, "entry,Wizard2.vm");
        }
        else 
        {
            // Comment was probably too long.  Repopulate the issue list, so
            // the page can be shown again, and the user can fix the comment.
            searchAndSetTemplate(data, context, 0, MAX_RESULTS, issue, "entry,Wizard2.vm");
        }
    }
    
    /**
     * The button for this action is commented out on Wizard2, so it
     * will not be called
    public void doAddvote(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        if (intake.isAllValid()) 
        {
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            Issue issue = scarabR.getReportingIssue();
            
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
     */
    
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
                    data.getParameters().setString(ScarabConstants.CANCEL_TEMPLATE, 
                                             "ViewIssue.vm");
                    data.getParameters().add("issue_ids", 
                                             issue.getUniqueId());
//                    data.getParameters()
//                        .setString("id", issue.getUniqueId().toString());
                    getIntakeTool(context).removeAll();
                    scarabR.resetAssociatedUsers();
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
                        .setString("id",issue.getUniqueId().toString());
                }
                else 
                {
                    template = user.getHomePage();
                    scarabR.setAlertMessage(
                        l10n.get("InsufficientPermissionsToViewIssues"));
                }
                break;
            case 4: 
                template = user.getHomePage();
                break;
            case 5: 
                if (user.hasPermission(ScarabSecurity.ISSUE__VIEW, 
                                       user.getCurrentModule()))
                {
                    template = "ViewIssue.vm";
                    data.getParameters()
                        .setString("id",issue.getUniqueId().toString());
                    data.getParameters()
                        .setString("tab","3"); // comment tab == 3
                    data.getUser().setTemp(ScarabConstants.TAB_KEY, "3");
                }
                else 
                {
                    template = user.getHomePage();
                    scarabR.setAlertMessage(
                        l10n.get("InsufficientPermissionsToViewIssues"));
                }
                break;
        } 
        setTarget(data, template);
    }
}
