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
import java.util.ArrayList;

// Turbine Stuff 
import org.apache.turbine.ParameterParser; 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.RunData;

import org.apache.commons.util.SequencedHashtable;
import org.apache.commons.util.StringUtils;

import org.apache.turbine.tool.IntakeTool;
import org.apache.torque.om.NumberKey; 
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.RQueryUser;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.word.IssueSearch;

/**
    This class is responsible for report issue forms.

    @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
    @version $Id: Search.java,v 1.54 2002/01/22 01:58:09 elicia Exp $
*/
public class Search extends RequireLoginFirstAction
{
    private static int DEFAULT_ISSUE_LIMIT = 25;

    private static final String ERROR_MESSAGE = "More information was " +
                                "required to submit your request. Please " +
                                "see error messages."; 

    public void doSearch(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);

        ScarabUser user = null;

        context.put("queryString", getQueryString(data));

        if (intake.isAllValid()) 
        {
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            IssueSearch search = new IssueSearch(scarabR.getCurrentModule(), 
                                                 scarabR.getCurrentIssueType());
            Group searchGroup = intake.get("SearchIssue", 
                                     scarabR.getSearch().getQueryKey());
            searchGroup.setProperties(search);

            SequencedHashtable avMap = search.getModuleAttributeValuesMap();
            Iterator i = avMap.iterator();
            while (i.hasNext()) 
            {
                AttributeValue aval = (AttributeValue)avMap.get(i.next());
                Group group = intake.get("AttributeValue", aval.getQueryKey());
                if (group != null) 
                {
                    group.setProperties(aval);
                }                
            }

            List matchingIssues = null;
            try
            {
                matchingIssues = search.getMatchingIssues();
            }
            catch (Exception e)
            {
            }
            if (matchingIssues != null && matchingIssues.size() > 0)
            {
                // we could pass these results, but intake is not being used
                // for sort criteria and polarity, why?.  Otherwise we have
                // to duplicate logic from ScarabRequestTool here, so for
                // now live with the inefficiency. !FIXME!
                /*
                List issueIdList = new ArrayList();
                i = matchingIssues.iterator();
                for (int j=0;j<matchingIssues.size();j++)
                {
                    issueIdList.add(((Issue)matchingIssues.get(j)).getIssueId());
                }
                */

                user = (ScarabUser)data.getUser();
                user.setTemp(ScarabConstants.CURRENT_QUERY, getQueryString(data));

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


    /**
        Redirects to form to save the query. May redirect to Login page.
    */
    public void doRedirecttosavequery(RunData data, TemplateContext context)
         throws Exception
    {        
        context.put("queryString", getQueryString(data));
        data.getParameters().remove("template");
        data.getParameters().add("template",  "SaveQuery.vm");
        setTarget(data, "SaveQuery.vm");            
    }

    /**
        Saves query.
    */
    public void doSavequery(RunData data, TemplateContext context)
         throws Exception
    {        
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Query query = scarabR.getQuery();
        Group queryGroup = intake.get("Query", 
                                      query.getQueryKey());

        Field name = queryGroup.get("Name");
        name.setRequired(true);
        Field value = queryGroup.get("Value");
        context.put("queryString", value);

        if (intake.isAllValid()) 
        {
            queryGroup.setProperties(query);
            query.setUserId(user.getUserId());
            query.setIssueType(scarabR.getCurrentIssueType());
            query.saveAndSendEmail(user, scarabR.getCurrentModule(),
                new ContextAdapter(context));

            String template = data.getParameters()
                .getString(ScarabConstants.NEXT_TEMPLATE);
            setTarget(data, template);            
        }
        else
        {
            data.setMessage(ERROR_MESSAGE);
        }
    }


    /**
        Edits the stored story.
    */
    public void doEditstoredquery(RunData data, TemplateContext context)
         throws Exception
    {        
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Query query = scarabR.getQuery();
        Group queryGroup = intake.get("Query", 
                                      query.getQueryKey());
        String newValue = getQueryString(data);
        queryGroup.setProperties(query);
        query.setValue(newValue);
        query.saveAndSendEmail((ScarabUser)data.getUser(), 
                        scarabR.getCurrentModule(), 
                        new ContextAdapter(context));
    }

    /**
        Runs the stored story.
    */
    public void doRunstoredquery(RunData data, TemplateContext context)
         throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Query query = scarabR.getQuery();
        ScarabUser user = (ScarabUser)data.getUser();
        user.setTemp(ScarabConstants.CURRENT_QUERY, query.getValue());
        setTarget(data, "IssueList.vm");
    }

    /** 
     * This method handles clicking the Go button in the SearchNav.vm
     * file. First it checks to see if the select box passed in a number
     * or a string. If it is a number, then we run the stored query
     * assuming the number is the query id. Else, we assume it is a
     * string and that is our template to redirect to.
     */
    public void doSelectquery(RunData data, TemplateContext context)
        throws Exception
    {
        String go = data.getParameters().getString("go");
        if (go != null && go.length() > 0)
        {
            // if the string is a number, then execute
            // doRunstoredquery()
            if (StringUtils.isNumeric(go))
            {
                data.getParameters().add("queryId", go);
                doRunstoredquery(data, context);
            }
            else
            {
                setTarget(data, go);
            }
        }
        else
        {
            // set the next template
            String nextTemplate = data.getParameters()
                .getString(ScarabConstants.NEXT_TEMPLATE, 
                Turbine.getConfiguration()
                           .getString("template.homepage", "Index.vm"));
            setTarget(data, nextTemplate);
        }
    }

    /**
        Redirects to ViewIssueLong.
    */
    public void doViewall(RunData data, TemplateContext context)
         throws Exception
    {        
        setTarget(data, "ViewIssueLong.vm");            
    }

    /**
        Gets selected id's and redirects to ViewIssueLong.
    */
    public void doViewselected(RunData data, TemplateContext context)
         throws Exception
    {        
        getSelected(data, context);
        setTarget(data, "ViewIssueLong.vm");            
    }

    /**
        Redirects to AssignIssue.
    */
    public void doReassignall(RunData data, TemplateContext context)
         throws Exception
    {        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        List issueList = scarabR.getIssueList();
        List issueIdList = new ArrayList();
        ParameterParser pp = data.getParameters();
        for (int j=0;j<issueList.size();j++)
        {
           pp.add("issue_ids", 
              ((Issue)issueList.get(j)).getIssueId().toString());
        }
        context.put("issueIdList", issueIdList);
        setTarget(data, "AssignIssue.vm");            
    }

    /**
        Gets selected id's and redirects to AssignIssue.
    */
    public void doReassignselected(RunData data, TemplateContext context)
         throws Exception
    {
        getSelected(data, context);
        setTarget(data, "AssignIssue.vm");            
    }

    /**
        redirects to AdvancedQuery.
    */
    public void doRefinequery(RunData data, TemplateContext context)
         throws Exception
    {        
        setTarget(data, "AdvancedQuery.vm");            
    }

    public String getQueryString(RunData data) throws Exception
    {
        String queryString = null;
        StringBuffer buf = new StringBuffer();
        Object[] keys =  data.getParameters().getKeys();
        for (int i =0; i<keys.length; i++)
        {
            String key = keys[i].toString();
            if (key.startsWith("attv") || key.startsWith("search") ||
                key.startsWith("intake"))
            {
                String[] values = data.getParameters().getStrings(key);
                for (int j=0; j<values.length; j++)
                {
                    buf.append("&").append(key);
                    buf.append("=").append(values[j]);
                }
            }
         }
         queryString = buf.toString();
         return queryString;
    }
        
    /**
        Retrieves list of selected issue id's and puts in the context.
    */
    private void getSelected(RunData data, TemplateContext context) 
    {
        List newIssueIdList = new ArrayList();
        String key;
        ParameterParser pp = data.getParameters();
        Object[] keys =  data.getParameters().getKeys();
        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("selected_"))
            {
                pp.add("issue_ids", key.substring(9).toString());
            }
        }
    }
    

}
