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

import java.util.*;
import java.math.BigDecimal;

// Velocity Stuff 
import org.apache.turbine.services.velocity.*; 
import org.apache.velocity.*; 
import org.apache.velocity.context.*; 
// Turbine Stuff 
import org.apache.turbine.util.*;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.services.resources.*;
import org.apache.turbine.services.intake.IntakeTool;
import org.apache.turbine.services.intake.model.Group;
import org.apache.turbine.services.intake.model.Field;
import org.apache.turbine.modules.*;
import org.apache.turbine.modules.actions.*;
import org.apache.turbine.om.*;

// Scarab Stuff
import org.tigris.scarab.om.BaseScarabObject;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserPeer;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.util.*;
import org.tigris.scarab.util.word.IssueSearch;

/**
    This class is responsible for report issue forms.
    ScarabIssueAttributeValue
    @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
    @version $Id: ReportIssue.java,v 1.16 2001/06/27 00:30:38 jmcnally Exp $
*/
public class ReportIssue extends VelocityAction
{
    public void doSubmitattributes( RunData data, Context context )
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);
        
        // Summary is always required (because we are going to search on it.)
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = user.getReportingIssue();
        AttributeValue aval = (AttributeValue)issue
            .getModuleAttributeValuesMap().get("SUMMARY");
        Group group = intake.get("AttributeValue", aval.getQueryKey());
        Field summary = group.get("Value");
        summary.setRequired(true);

        // set any other required flags
        Criteria crit = new Criteria(3)
            .add(RModuleAttributePeer.ACTIVE, true)        
            .add(RModuleAttributePeer.REQUIRED, true);        
        Attribute[] requiredAttributes = issue.getModule().getAttributes(crit);
        SequencedHashtable avMap = issue.getModuleAttributeValuesMap(); 
        Iterator iter = avMap.iterator();
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

            // search on the option attributes and keywords
            IssueSearch search = new IssueSearch();
            search.setSearchWords(summary.toString());

            search.setModule(user.getCurrentModule());
            avMap = search.getModuleAttributeValuesMap(); 
            Iterator i = avMap.iterator();
            while (i.hasNext()) 
            {
                aval = (AttributeValue)avMap.get(i.next());
                group = intake.get("AttributeValue", aval.getQueryKey(),false);
                if ( group != null ) 
                {
                    group.setProperties(aval);
                }
            }
            
            List matchingIssues = search.getMatchingIssues(25);

            // set the template to dedupe unless none exist, then skip
            // to final entry screen
            String template = null;
            if ( matchingIssues.size() > 0 )
            {
                context.put("issueList", matchingIssues);
                template = "entry,Wizard2.vm";
            }
            else
            {
                template = "entry,Wizard3.vm";
            }
            setTemplate(data, template);
        }
    }


    public void doEnterissue( RunData data, Context context )
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = user.getReportingIssue();
        AttributeValue aval = null;

        // set any other required flags
        Criteria crit = new Criteria(3)
            .add(RModuleAttributePeer.ACTIVE, true)        
            .add(RModuleAttributePeer.REQUIRED, true);        
        Attribute[] requiredAttributes = issue.getModule().getAttributes(crit);
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
                if ( aval instanceof UserAttribute ) 
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


        if ( intake.isAllValid() ) 
        {
            Iterator i = avMap.iterator();
            while (i.hasNext()) 
            {
                aval = (AttributeValue)avMap.get(i.next());
                Group group = 
                    intake.get("AttributeValue", aval.getQueryKey(),false);
                if ( group != null ) 
                {
                    group.setProperties(aval);
                }                
            }
            
            if ( issue.containsMinimumAttributeValues() ) 
            {
                issue.save();
                user.setReportingIssue(null);

                // save the attachment
                Attachment attachment = new Attachment();
                Group group = intake.get("Attachment", 
                                   attachment.getQueryKey(), false);
                if ( group != null ) 
                {
                    group.setProperties(attachment);
                    if ( attachment.getData().length > 0 ) 
                    {
                        attachment.setIssue(issue);
                        attachment.setTypeId(new NumberKey(1));
                        attachment.save();
                    }
                }

                // set the template to the user selected value
                String template = data.getParameters()
                    .getString(ScarabConstants.NEXT_TEMPLATE, 
                               "entry,Wizard4.vm");
                setTemplate(data, template);                
            }
            else 
            {
                // this would be an application or hacking error
            }
            
            
        }

    }

    public void doAddvote( RunData data, Context context ) 
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);

        Group group = intake.get("Issue", IntakeTool.DEFAULT_KEY);
        
        if ( intake.isAllValid() ) 
        {
            Issue issue = IssuePeer
                .retrieveByPK((NumberKey)group.get("Id").getValue());
            issue.addVote();
            doCancel(data, context);
        }
        doCancel(data, context);
    }

    public void doGotowizard3( RunData data, Context context )
        throws Exception
    {
        setTemplate(data, "entry,Wizard3.vm");
    }

    /**
        This manages clicking the Cancel button
    */
    public void doCancel( RunData data, Context context ) throws Exception
    {
        String template = TurbineResources
            .getString("template.homepage", "Start.vm");
        setTemplate(data, template);
    }
    /**
        calls doCancel()
    */
    public void doPerform( RunData data, Context context ) throws Exception
    {
        doCancel(data, context);
    }
}
