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
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.util.*;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.word.IssueSearch;

/**
    This class is responsible for report issue forms.
    ScarabIssueAttributeValue
    @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
    @version $Id: Search.java,v 1.5 2001/07/05 00:04:37 jon Exp $
*/
public class Search extends VelocityAction
{
    public void doIssueIdFind( RunData data, Context context )
        throws Exception
    {
        // the IssueView page takes a string id, nothing
    }

    /*
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
            
            // search for duplicate issues based on summary
            Vocabulary voc = new Vocabulary(summary.toString());
            issue.setVocabulary(voc);
            BigDecimal[] matchingIssueIds = voc.getRelatedIssues();
            // do not show more than 25 dupe guesses.
            int matchingIssuesCount = matchingIssueIds.length;
            if (matchingIssuesCount>25)
                matchingIssuesCount=25;
            //looks like we have to fetch them one by one to keep the order
            Vector matchingIssues = new Vector(matchingIssuesCount);

            for (int i=0; i<matchingIssuesCount; i++)
            {
                matchingIssues
                    .add(IssuePeer
                        .retrieveByPK(new NumberKey(matchingIssueIds[i])));
            }

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
        */

    public void doSearch( RunData data, Context context )
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);

        ScarabUser user = (ScarabUser)data.getUser();

        if ( intake.isAllValid() ) 
        {
            ScarabRequestTool scarab = (ScarabRequestTool)context
                .get(ScarabConstants.SCARAB_REQUEST_TOOL);

            IssueSearch search = new IssueSearch();
            Group group = intake.get("SearchIssue", 
                                     scarab.getSearch().getQueryKey() );
            group.setProperties(search);

            search.setModuleCast(user.getCurrentModule());
            SequencedHashtable avMap = search.getModuleAttributeValuesMap();
            Iterator i = avMap.iterator();
            while (i.hasNext()) 
            {
                AttributeValue aval = (AttributeValue)avMap.get(i.next());
                group = intake.get("AttributeValue", aval.getQueryKey());
                if ( group != null ) 
                {
                    group.setProperties(aval);
                }                
            }
            
            List matchingIssues = search.getMatchingIssues(25);
            if ( matchingIssues.size() > 0 )
            {
                context.put("issueList", matchingIssues);
                
                String template = data.getParameters()
                    .getString(ScarabConstants.NEXT_TEMPLATE, 
                               "IssueList.vm");
                setTemplate(data, template);            
            }
            else
            {
                data.setMessage("No matching issues.");
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
