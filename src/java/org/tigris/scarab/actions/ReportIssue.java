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

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.RunData;

import org.apache.commons.util.SequencedHashtable;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

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
 * @version $Id: ReportIssue.java,v 1.51 2001/10/05 16:35:31 jmcnally Exp $
 */
public class ReportIssue extends RequireLoginFirstAction
{
    public void doSubmitattributes( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = user.getReportingIssue(scarabR.getCurrentModule());

        // set any required flags
        Attribute[] requiredAttributes = 
            issue.getScarabModule().getRequiredAttributes();
        SequencedHashtable avMap = issue.getModuleAttributeValuesMap(); 
        Iterator iter = avMap.iterator();
        AttributeValue aval = null;
        Group group = null;
        while ( iter.hasNext() ) 
        {
            aval = (AttributeValue)avMap.get(iter.next());

            group = intake.get("AttributeValue", aval.getQueryKey(), false);
            if ( group != null ) 
            {            
                Field field = null;
                if ( aval instanceof OptionAttribute ) 
                {
                    field = group.get("OptionId");
                }
                else 
                {
                    field = group.get("Value");
                }
                
                for ( int j=requiredAttributes.length-1; j>=0; j-- ) 
                {
                    if ( aval.getAttribute().getPrimaryKey().equals(
                         requiredAttributes[j].getPrimaryKey() )) 
                    {
                        field.setRequired(true);
                        break;
                    }                    
                }
            }
        }
        
        if ( intake.isAllValid() ) 
        {
            // set the values entered so far
            iter = avMap.iterator();
            while (iter.hasNext()) 
            {
                aval = (AttributeValue)avMap.get(iter.next());
                group = intake.get("AttributeValue", aval.getQueryKey(),false);
                if ( group != null ) 
                {
                    group.setProperties(aval);
                }                
            }

            reusedSearchStuff(data, context, "eventSubmit_doSubmitattributes", 
                              0, "entry,Wizard3.vm");
        }

        // we know we started at Wizard1 if we are here
        user.setReportingIssueStartPoint("entry,Wizard1.vm");
    }

    private boolean reusedSearchStuff(RunData data, TemplateContext context, 
                                      String event, int threshold, 
                                      String nextTemplate)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();

        List matchingIssues = searchIssues(scarabR.getCurrentModule(), 
                                           intake, 25);                  
                
        // set the template to dedupe unless none exist, then skip
        // to final entry screen
        String template = null;
        boolean beatThreshold = false;
        if ( matchingIssues.size() > threshold )
        {
            context.put("issueList", matchingIssues);
            template = "entry,Wizard2.vm";
            // clean out the eventSubmit because we will reuse parameters
            data.getParameters().remove(event);
            data.getParameters().remove("nextTemplate");
            beatThreshold = true;
        }
        else
        {
            template = nextTemplate;
        }

        setTarget(data, template);
        return beatThreshold;
    }

    private List searchIssues( ModuleEntity module, 
                               IntakeTool intake, int maxResults)
        throws Exception
    { 
        // search on the option attributes and keywords
        IssueSearch search = new IssueSearch();        
        search.setModuleCast(module);

        SequencedHashtable avMap = search.getModuleAttributeValuesMap(); 
        Iterator i = avMap.iterator();
        while (i.hasNext()) 
        {
            AttributeValue aval = (AttributeValue)avMap.get(i.next());
            Group group = 
                intake.get("AttributeValue", aval.getQueryKey(), false);
            if ( group != null ) 
            {
                group.setProperties(aval);
            }
        }
        
        return search.getMatchingIssues(maxResults);
    }

    /**
     * handles entering an issue
     */
    public void doEnterissue( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();

        Issue issue = user.getReportingIssue(scarabR.getCurrentModule());
        AttributeValue aval = null;

        // set any other required flags
        Criteria crit = new Criteria(3)
            .add(RModuleAttributePeer.ACTIVE, true)        
            .add(RModuleAttributePeer.REQUIRED, true);        
        Attribute[] requiredAttributes = issue.getScarabModule().getAttributes(crit);
        SequencedHashtable avMap = issue.getModuleAttributeValuesMap(); 
        Iterator iter = avMap.iterator();
        while ( iter.hasNext() ) 
        {
            aval = (AttributeValue)avMap.get(iter.next());
            
            Group group = intake.get("AttributeValue", aval.getQueryKey(), false);
            if ( group != null ) 
            {            
                Field field = null;
                if ( aval instanceof OptionAttribute ) 
                {
                    field = group.get("OptionId");
                }
                else if ( aval instanceof UserAttribute ) 
                {
                    field = group.get("UserId");
                }
                else 
                {
                    field = group.get("Value");
                }
                
                for ( int j=requiredAttributes.length-1; j>=0; j-- ) 
                {
                    if ( aval.getAttribute().getPrimaryKey().equals(
                         requiredAttributes[j].getPrimaryKey() )
                         && !aval.isSet()
                       ) 
                    {
                        field.setRequired(true);
                        break;
                    }                    
                }
            }
        }

        if (intake.isAllValid())
        {
            Iterator i = avMap.iterator();
            while (i.hasNext()) 
            {
                aval = (AttributeValue)avMap.get(i.next());
                Group group = 
                    intake.get("AttributeValue", aval.getQueryKey(), false);
                if ( group != null ) 
                {
                    group.setProperties(aval);
                }                
            }
            
            if (issue.containsMinimumAttributeValues())
            {
                // Save transaction record
                Transaction transaction = new Transaction();
                transaction
                    .create(TransactionTypePeer.CREATE_ISSUE__PK, user, null);

                // enter the values into the transaction
                i = avMap.iterator();
                while (i.hasNext()) 
                {
                    aval = (AttributeValue)avMap.get(i.next());
                    aval.startTransaction(transaction);
                }
                
                issue.setTypeId(IssueType.ISSUE__PK);
                issue.save();
                user.setReportingIssue(null);

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
                        attachment.setIssue(issue);
                        attachment.setTypeId(new NumberKey(1));
                        attachment.save();
                    }
                }

                // set the template to the user selected value
                String template = data.getParameters()
                    .getString(ScarabConstants.NEXT_TEMPLATE, 
                    "entry,Wizard3.vm");
                if (template != null && template.equals("AssignIssue.vm"))
                {
                    data.getParameters().add("intake-grp", "issue"); 
                    data.getParameters().add("issue", "_0"); 
                    data.getParameters().add("issue_0id", 
                                             issue.getIssueId().toString());
                }

                // need to not hardcode summary here. !FIXME!
                String summary = 
                    ((AttributeValue)avMap.get("SUMMARY")).getValue();
                summary = summary == null ? "" : " - " + summary;
                StringBuffer subj = new StringBuffer("[");
                subj.append(issue.getScarabModule().getRealName().toUpperCase());
                subj.append("] Issue #").append(issue.getUniqueId());
                subj.append(summary);
                transaction.sendEmail(new ContextAdapter(context), issue, 
                                      subj.toString(),
                                      "email/NewIssueNotification.vm"); 
                setTarget(data, template);

                intake.removeAll();
                data.getParameters().add("issue_id", 
                                         issue.getIssueId().toString());
            }
            else 
            {
                // this would be an application or hacking error
            }            
        }
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
                if (attachment.getData().length > 0)
                {
                    ScarabRequestTool scarabR = getScarabRequestTool(context);
                    Issue issue = scarabR.getIssue();
                    issue.addComment(attachment);

                    data.setMessage("Your comment for issue #" + 
                                    issue.getUniqueId() + 
                                    " has been added.");
                    String nextTemplate = Turbine.getConfiguration()
                        .getString("template.homepage", "Start.vm");
                    if (! reusedSearchStuff(data, context, 
                             "eventSubmit_doAddnote",1, nextTemplate))
                    {
                        ((ScarabUser)data.getUser()).setReportingIssue(null);
                    }
                }
            }
        }
        else 
        {
            reusedSearchStuff(data, context, "eventSubmit_doAddnote",
                              0, "entry,Wizard2.vm");
        }
    }

    public void doAddvote(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            Group group = intake.get("Issue", IntakeTool.DEFAULT_KEY);        
            Issue issue = IssuePeer
                .retrieveByPK((NumberKey)group.get("Id").getValue());
            try
            {
                issue.addVote((ScarabUser)data.getUser());
                data.setMessage("Your vote for issue #" + issue.getUniqueId() 
                                + " has been accepted.");
                String nextTemplate = Turbine.getConfiguration()
                    .getString("template.homepage", "Start.vm");
                if (! reusedSearchStuff(data, context, 
                          "eventSubmit_doAddvote",1, nextTemplate))
                {
                    ((ScarabUser)data.getUser()).setReportingIssue(null);
                }
            }
            catch (ScarabException e)
            {
                data.setMessage("Vote could not be added.  Reason given: "
                                + e.getMessage() );
                reusedSearchStuff(data, context, "eventSubmit_doAddvote",
                                  0, "entry,Wizard2.vm");
            }
        }
        else 
        {
            reusedSearchStuff(data, context, "eventSubmit_doAddvote",
                              0, "entry,Wizard2.vm");
        }
    }

    public void doGotowizard3(RunData data, TemplateContext context)
        throws Exception
    {
        setTarget(data, "entry,Wizard3.vm");
    }

    /**
        This manages clicking the Cancel button
    */
    public void doCancel(RunData data, TemplateContext context) throws Exception
    {
        String template = Turbine.getConfiguration()
            .getString("template.homepage", "Start.vm");
        setTarget(data, template);
    }

    /**
        calls doCancel()
    */
    public void doPerform(RunData data, TemplateContext context) throws Exception
    {
        doCancel(data, context);
    }
}
