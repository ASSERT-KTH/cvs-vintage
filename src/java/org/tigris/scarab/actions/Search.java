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
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;

// Turbine Stuff 
import org.apache.turbine.TemplateAction;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.turbine.util.SequencedHashtable;

import org.apache.turbine.services.db.util.Criteria;
import org.apache.turbine.services.intake.IntakeTool;
import org.apache.turbine.services.intake.model.Group;
import org.apache.turbine.services.intake.model.Field;

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
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.word.IssueSearch;

/**
    This class is responsible for report issue forms.
    ScarabIssueAttributeValue
    @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
    @version $Id: Search.java,v 1.14 2001/07/27 19:39:50 elicia Exp $
*/
public class Search extends TemplateAction
{
    private static int DEFAULT_ISSUE_LIMIT = 25;

    public void doSearch( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);

        ScarabUser user = (ScarabUser)data.getUser();

        HttpServletRequest request = data.getRequest();    
        context.put("queryString", request.getQueryString());

        if ( intake.isAllValid() ) 
        {
            ScarabRequestTool scarab = (ScarabRequestTool)context
                .get(ScarabConstants.SCARAB_REQUEST_TOOL);

            IssueSearch search = new IssueSearch();
            Group searchGroup = intake.get("SearchIssue", 
                                     scarab.getSearch().getQueryKey() );
            searchGroup.setProperties(search);

            search.setModuleCast(user.getCurrentModule());
            SequencedHashtable avMap = search.getModuleAttributeValuesMap();
            Iterator i = avMap.iterator();
            while (i.hasNext()) 
            {
                AttributeValue aval = (AttributeValue)avMap.get(i.next());
                Group group = intake.get("AttributeValue", aval.getQueryKey());
                if ( group != null ) 
                {
                    group.setProperties(aval);
                }                
            }
            
            int issueLimit = DEFAULT_ISSUE_LIMIT;
            Field limitField = searchGroup.get("ResultsPerPage");
            if ( limitField.getValue() != null ) 
            {
                issueLimit = 20 * ((Integer)limitField.getValue()).intValue();
            }

            List matchingIssues = search.getMatchingIssues(issueLimit);
            if ( matchingIssues.size() > 0 )
            {
                user.setTemp("issueList", matchingIssues);
                
                String template = data.getParameters()
                    .getString(ScarabConstants.NEXT_TEMPLATE, 
                               "IssueList.vm");
                setTarget(data, template);            
            }
            else
            {
                data.setMessage("No matching issues.");
            }            
        }
    }

    public void doSavequery( RunData data, TemplateContext context )
         throws Exception
    {        
        String queryString = data.getParameters().getString("queryString");
        context.put("queryString", queryString);
        setTarget(data, "SaveQuery.vm");            
    }

    /**
        This manages clicking the Cancel button
    */
    public void doCancel( RunData data, TemplateContext context ) throws Exception
    {
        setTarget(data, "Start.vm");
    }
    /**
        calls doCancel()
    */
    public void doPerform( RunData data, TemplateContext context ) throws Exception
    {
        doCancel(data, context);
    }
}
