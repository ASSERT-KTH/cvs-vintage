package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000 Collab.Net.  All rights reserved.
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
import java.math.BigDecimal;

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateAction;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.turbine.util.SequencedHashtable;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.services.intake.IntakeTool;
import org.apache.turbine.services.intake.model.Group;
import org.apache.turbine.services.intake.model.Field;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabLink;

/**
    This class is responsible for report issue forms.
    ScarabIssueAttributeValue
    @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
    @version $Id: AssignIssue.java,v 1.4 2001/08/02 22:15:14 elicia Exp $
*/
public class AssignIssue extends TemplateAction
{
    public static final String NEW_ASSIGNEES = "newaddusers";
    public static final String ASSIGNEES = "addedusers";
    public static final String NEW_ELIGIBLE_USERS = "newremoveusers";
    public static final String ELIGIBLE_USERS = "removedusers";

    public void doAdd( RunData data, TemplateContext context ) 
        throws Exception
    {
        String[] newAssigneeIds = data.getParameters()
            .getStrings(NEW_ASSIGNEES);
        String[] eligibleUserIds = 
            data.getParameters().getStrings(ELIGIBLE_USERS);

        // check if a user who was added was in the removed
        // list.  if so, remove them from the removal list
        nullOutDuplicates(newAssigneeIds, eligibleUserIds);
        
        // rebuild the action url with the new users added
        String[] assigneeIds = 
            data.getParameters().getStrings(ASSIGNEES);
        ScarabLink actionLink = 
            getActionLink(data, eligibleUserIds, assigneeIds);

        populateLink(actionLink, ASSIGNEES, newAssigneeIds);
        addToParameters(data, ASSIGNEES, newAssigneeIds);
        data.getParameters().remove(ELIGIBLE_USERS);
        addToParameters(data, ELIGIBLE_USERS, eligibleUserIds);
        context.put("actionLink", actionLink);

        data.setMessage(
            "Remember to add note and submit, when ready to save changes.");  
    }

    public void doRemove( RunData data, TemplateContext context ) 
        throws Exception
    {
        String[] newEligibleUserIds = 
            data.getParameters().getStrings(NEW_ELIGIBLE_USERS);
        String[] assigneeIds = 
            data.getParameters().getStrings(ASSIGNEES);

        // check if a user who was removed was in the assignee
        // list.  if so, remove them from the assignee list
        nullOutDuplicates(newEligibleUserIds, assigneeIds);
        
        // rebuild the action url with the new users added
        String[] eligibleUsers = 
            data.getParameters().getStrings(ELIGIBLE_USERS);
        ScarabLink actionLink = 
            getActionLink(data, eligibleUsers, assigneeIds);

        populateLink(actionLink, ELIGIBLE_USERS, newEligibleUserIds);
        addToParameters(data, ELIGIBLE_USERS, newEligibleUserIds);
        data.getParameters().remove(ASSIGNEES);
        addToParameters(data, ASSIGNEES, assigneeIds);
        context.put("actionLink", actionLink);

        data.setMessage(
            "Remember to add note and submit, when ready to save changes.");
    }

    private ScarabLink getActionLink(RunData data, String[] eligibleUsers, 
                                     String[] assignees)
    {
        ScarabLink actionLink = new ScarabLink();
        actionLink.init((Object)data);
        actionLink.addPathInfo("action", "AssignIssue");
        actionLink.setPage("AssignIssue.vm");
        populateLink(actionLink, ELIGIBLE_USERS, eligibleUsers);
        populateLink(actionLink, ASSIGNEES, assignees);
        
        return actionLink;
    }

    /**
     * if a value is found in both arrays they cancel each other,
     * so remove the value from both.
     *
     * @param s1 a <code>String[]</code> value
     * @param s2 a <code>String[]</code> value
     */
    private void nullOutDuplicates(String[] s1, String[] s2)
    {
        if ( s1 != null && s2 != null ) 
        {
            for ( int i=s1.length-1; i>=0; i-- ) 
            {
                for ( int j=s2.length-1; j>=0; j-- ) 
                {
                    if ( s1[i].equals(s2[j]) ) 
                    {
                        s2[j] = null;
                    }
                }
            }
        }
    }
    
    private void populateLink(ScarabLink link, String key, String[] ids)
    {
        if ( ids != null ) 
        {        
            for ( int i=0; i<ids.length; i++ ) 
            {
                if ( ids[i] != null ) 
                {
                    link.addPathInfo(key, ids[i]);
                }
            }   
        }
    }

    private void addToParameters(RunData data, String key, String[] ids)
    {
        if ( ids != null ) 
        {        
            for ( int i=0; i<ids.length; i++ ) 
            {
                if ( ids[i] != null ) 
                {
                    data.getParameters().add(key, ids[i]);
                }
            }   
        }
    }

    public void doSubmit( RunData data, TemplateContext context ) 
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);
        ScarabRequestTool scarabR = (ScarabRequestTool)context
            .get(ScarabConstants.SCARAB_REQUEST_TOOL);

        ScarabUser modifyingUser = (ScarabUser)data.getUser();
        Issue issue = scarabR.getIssue();

        Attachment attachment = new Attachment();
        Group group = intake.get("Attachment", 
                                     attachment.getQueryKey(), false);
        group.get("DataAsString").setRequired(true);

            if ( intake.isAllValid() ) 
            {
                // Save transaction record
                Transaction transaction = new Transaction();
                transaction.create(modifyingUser);

                // save the attachment
                group.setProperties(attachment);
                if ( attachment.getData() != null 
                     && attachment.getData().length > 0 ) 
                {
                    attachment.setName("Assignee Note");
                    attachment.setTextFields(modifyingUser, issue, 
                                     Attachment.MODIFICATION__PK);
                    attachment.save();

                    // save assignee list
                    List assignees = issue.getAssigneeAttributeValues();
                    String[] newUsernames = 
                        data.getParameters().getStrings(ASSIGNEES);
                    int newUserLength = 0;
                    if ( newUsernames != null ) 
                    {
                        newUserLength = newUsernames.length;
                    }

                    // take care of users who were removed
                    Iterator iter = assignees.iterator();
                    while ( iter.hasNext() ) 
                    {
                        AttributeValue oldAV = (AttributeValue)iter.next();
                        boolean deleted = true;
                        for ( int i=0; i<newUserLength; i++ ) 
                        {
                            if (oldAV.getValue().equals(newUsernames[i])) 
                            {
                                newUsernames[i] = null;
                                deleted = false;
                                break;
                            }
                        }
                        oldAV.setDeleted(deleted);

                        // Save activity record
                        Activity activity = new Activity();
                        String desc = "Unassigned Issue";
                        activity.create(issue, oldAV.getAttribute(),
                                        desc, transaction, attachment, 
                                        oldAV.getValue(), "");
                    }
                    // add new values
                    for ( int i=0; i<newUserLength; i++ ) 
                    {
                        if ( newUsernames[i] != null ) 
                        {
                            Criteria crit = new Criteria()
                                .add(ScarabUserImplPeer.USERNAME, newUsernames[i]);
                            List users = ScarabUserImplPeer.doSelect(crit);
                            ScarabUser user = (ScarabUser)users.get(0);
                            AttributeValue av = AttributeValue.getNewInstance(
                                AttributePeer.ASSIGNED_TO__PK, issue);
                            av.setUserId(user.getUserId());
                            av.setValue(user.getUserName());
                            assignees.add(av);
                            // Save activity record
                            Activity activity = new Activity();
                            String desc = "Assigned Issue";
                            activity.create(issue, av.getAttribute(),
                                            desc, transaction, attachment, 
                                            "", user.getUserName());
                        }
                    }
                    issue.save();

                    // set up email to users here !FIXME!

                    data.setMessage("Your changes to the assignee list of issue #" 
                                    + issue.getUniqueId() + " have been saved.");

                    String nextTemplate = Turbine.getConfiguration()
                        .getString("template.homepage", "Links.vm");
                    setTarget(data, nextTemplate);
                }
            }
            else 
            {                
                String[] eligibleUsers = 
                    data.getParameters().getStrings(ELIGIBLE_USERS);
                String[] assignees = 
                    data.getParameters().getStrings(ASSIGNEES);
                context.put("actionLink", 
                            getActionLink(data, eligibleUsers, assignees) );
            }
            
        
    }
}






