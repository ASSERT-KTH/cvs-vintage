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

// Scarab Stuff
import org.tigris.scarab.om.BaseScarabObject;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserPeer;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.util.*;

/**
    This class is responsible for report issue forms.
    ScarabIssueAttributeValue
    @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
    @version $Id: ReportIssue.java,v 1.7 2001/04/12 00:21:19 jmcnally Exp $
*/
public class ReportIssue extends VelocityAction
{
    public void doSubmitattributes( RunData data, Context context ) 
        throws Exception
    {
        //until we get the user and module set through normal application
        BaseScarabObject.tempWorkAround(data,context);

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

        if ( intake.isAllValid() ) 
        {
            // search for duplicate issues based on summary
            StringTokenizer st = new StringTokenizer(summary.toString(), " ");
            String[] keywords = new String[st.countTokens()];
            int i=0;
            while (st.hasMoreTokens()) 
            {
                keywords[i++] = st.nextToken();
            }

            List matchingIssues = Issue.searchKeywords(keywords, false);
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
        //until we get the user and module set through normal application
        BaseScarabObject.tempWorkAround(data,context);

        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);
        
        // Summary is always required.
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = user.getReportingIssue();
        AttributeValue aval = (AttributeValue)issue
            .getModuleAttributeValuesMap().get("SUMMARY");
        Group group = intake.get("AttributeValue", aval.getQueryKey());
        Field summary = group.get("Value");
        summary.setRequired(true);

        if ( intake.isAllValid() ) 
        {
            Iterator i = issue.getModuleAttributeValuesMap()
                .values().iterator();
            while (i.hasNext()) 
            {
                aval = (AttributeValue)i.next();
                group = intake.get("AttributeValue", aval.getQueryKey());
                if ( group != null ) 
                {
                    group.setProperties(aval);
                }                
            }
            
            if ( issue.containsMinimumAttributeValues() ) 
            {
                issue.save();

                String template = data.getParameters()
                    .getString(ScarabConstants.NEXT_TEMPLATE, 
                               "entry,Wizard3.vm");
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
        /*
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = user.getReportingIssue();
        issue.addVote();
        */

        
    }

    /**
        This manages clicking the Cancel button
    */
    public void doCancel( RunData data, Context context ) throws Exception
    {
        setTemplate(data, "Start.vm");
    }
    /**
        calls doCancel()
    */
    public void doPerform( RunData data, Context context ) throws Exception
    {
        doCancel(data, context);
    }
}
