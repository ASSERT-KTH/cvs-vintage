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

import java.util.List;
import java.util.ArrayList;

// Turbine Stuff 
import org.apache.turbine.ParameterParser; 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.commons.lang.StringUtils;

import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.Scope;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabConstants;

/**
 *  This class is responsible for searching.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: Search.java,v 1.101 2003/01/16 01:30:14 elicia Exp $
 */
public class Search extends RequireLoginFirstAction
{
    public void doPerform(RunData data, TemplateContext context)
        throws Exception
    {
        doGonext(data, context);
    }

    public void doSearch(RunData data, TemplateContext context)
        throws Exception
    {
        String queryString = getQueryString(data);
        ((ScarabUser)data.getUser()).setMostRecentQuery(queryString);
        data.getParameters().setString("queryString", queryString);

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        List queryResults = scarabR.getCurrentSearchResults();
        if (queryResults != null && queryResults.size() > 0)
        {
            context.put("queryResults", queryResults);
            String template = data.getParameters()
                .getString(ScarabConstants.NEXT_TEMPLATE, 
                           "IssueList.vm");
            setTarget(data, template);            
        }
    }

    /**
        Redirects to form to save the query. May redirect to Login page.
    */
    public void doRedirecttosavequery(RunData data, TemplateContext context)
         throws Exception
    {
        data.getParameters().setString("queryString", getQueryString(data));
        setTarget(data, "SaveQuery.vm");
    }

    public void doRedirecttocrossmodulequery(RunData data, TemplateContext context)
         throws Exception
    {
        data.getParameters().setString("queryString", getQueryString(data));
        setTarget(data, "home,XModuleList.vm");
    }

    /**
        Saves query.
    */
    public void doSavequery(RunData data, TemplateContext context)
         throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Query query = scarabR.getQuery();
        Group queryGroup = intake.get("Query", 
                                      query.getQueryKey());

        Field name = queryGroup.get("Name");
        name.setRequired(true);
        data.getParameters().setString("queryString", getQueryString(data));

        Module module = scarabR.getCurrentModule();
        if (intake.isAllValid()) 
        {
            queryGroup.setProperties(query);
            query.setScarabUser(user);
            MITList currentList = user.getCurrentMITList();
            if (currentList == null) 
            {
                query.setIssueType(scarabR.getCurrentIssueType());    
            }
            else 
            {
                query.setMITList(currentList);
                if (!currentList.isSingleModule()) 
                {
                    query.setModule(null);
                    query.setScopeId(Scope.PERSONAL__PK);                    
                }
            }
            
            ScarabUser[] userList = 
                module.getUsers(ScarabSecurity.ITEM__APPROVE);
            if (Scope.MODULE__PK.equals(query.getScopeId()) &&
                (userList == null || userList.length == 0))
            {
                scarabR.setAlertMessage(
                    l10n.format("NoApproverAvailable", module.getName()));
            }
            else 
            {            
                query.saveAndSendEmail(user, module, context);
                
                String template = data.getParameters()
                    .getString(ScarabConstants.NEXT_TEMPLATE);
                setTarget(data, template);            
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
    }

    public void doEditqueryinfo( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);        
        Query query = getScarabRequestTool(context).getQuery();
        Group queryGroup = intake.get("Query", 
                                      query.getQueryKey());
        queryGroup.setProperties(query);
        query.saveAndSendEmail((ScarabUser)data.getUser(), 
                 getScarabRequestTool(context).getCurrentModule(), context);
    }

    /**
        Edits the stored story.
    */
    public void doEditstoredquery(RunData data, TemplateContext context)
         throws Exception
    {        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Query query = scarabR.getQuery();
        String newValue = getQueryString(data);
        query.setValue(newValue);
        query.saveAndSendEmail((ScarabUser)data.getUser(), 
                 scarabR.getCurrentModule(), context);
    }

    /**
        Runs the stored story.
    */
    public void doRunstoredquery(RunData data, TemplateContext context)
         throws Exception
    {
        // Set current query to the stored query
        Query query = getScarabRequestTool(context).getQuery();
        ScarabUser user = (ScarabUser)data.getUser();
        MITList mitList = query.getMITList();
        user.setCurrentMITList(mitList);
        if (mitList != null)
        {
            mitList.setScarabUser(user);
        }
        user.setMostRecentQuery(query.getValue());

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
                data.getParameters().setString("queryId", go);
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
        getAllIssueIds(data);
        setTarget(data, "ViewIssueLong.vm");            
    }

    /**
        Gets selected id's and redirects to ViewIssueLong.
    */
    public void doViewselected(RunData data, TemplateContext context)
         throws Exception
    {        
        List selectedIds = getSelected(data);
        if (selectedIds.size() > 0)
        {
            setTarget(data, "ViewIssueLong.vm");            
        }
        else
        {
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            getScarabRequestTool(context)
                .setAlertMessage(l10n.get("SelectIssuesToView"));
        }
    }

    /**
        Gets selected id's and redirects to AssignIssue.
    */
    public void doReassignselected(RunData data, TemplateContext context)
         throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        List selectedIds = getSelected(data);
        if (selectedIds.size() > 0)
        {
            List modules = ModuleManager.getInstancesFromIssueList(
                scarabR.getIssues(selectedIds));
            if (user.hasPermission(ScarabSecurity.ISSUE__ASSIGN, modules)) 
            {
                scarabR.resetAssociatedUsers();
                setTarget(data, "AssignIssue.vm");                    
            }
            else 
            {
                scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get("SelectIssuesToView"));
        }
    }

    /**
        Redirects to AssignIssue, passing all issue ids.
    */
    public void doReassignall(RunData data, TemplateContext context)
         throws Exception
    {        
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        getAllIssueIds(data);
        List modules = ModuleManager.getInstancesFromIssueList(
            scarabR.getIssues());
        if (user.hasPermission(ScarabSecurity.ISSUE__ASSIGN, modules)) 
        {
            scarabR.resetAssociatedUsers();
            setTarget(data, "AssignIssue.vm");                    
        }
        else 
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
        }
    }


    /**
        redirects to AdvancedQuery.
    */
    public void doRefinequery(RunData data, TemplateContext context)
         throws Exception
    {        
        context.put("refine", "true");
        setTarget(data, "AdvancedQuery.vm");            
    }

    /**
        Removes users from the search form.
    */
    public void doRemoveusers(RunData data, TemplateContext context) 
    {
        List toRemove = new ArrayList();
        Object[] keys =  data.getParameters().getKeys();
        for (int i =0; i<keys.length; i++)
        {
            String key = keys[i].toString();
            if (key.startsWith("delete_user"))
            {
                String userId = key.substring(12);
                toRemove.add(userId);
            }
        }
        String[] userList = data.getParameters().getStrings("user_list");
        data.getParameters().remove("user_list");
        for (int i=0; i < userList.length; i++)
        { 
            String userInList = userList[i];
            if (!toRemove.contains(userInList))
            {
                data.getParameters().setString("user_list", userInList);
            }
        }
    }

    /**
        Adds user to the search form.
    */
    public void doAdduser(RunData data, TemplateContext context)  
        throws Exception
    {
        String userName = data.getParameters().getString("add_user");
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = scarabR.getUserByUserName(userName);
        if (user == null)
        {
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            scarabR.setAlertMessage(l10n.get("UserNotFound"));
        }
        else
        {
            data.getParameters().add("user_list", user.getUserId().toString());
        }
    }


    /**
        Overrides base class.
    */
    public void doDone(RunData data, TemplateContext context)  
        throws Exception
    {
        doEditqueryinfo(data, context);
        doEditstoredquery(data, context);
        doCancel(data, context);
    }

    public static String getQueryString(RunData data) throws Exception
    {
        String queryString = data.getParameters().getString("queryString");
        if (queryString == null) 
        {
            StringBuffer buf = new StringBuffer();
            Object[] keys =  data.getParameters().getKeys();
            for (int i =0; i<keys.length; i++)
            {
                String key = keys[i].toString();
                if (key.startsWith("attv") || key.startsWith("search") ||
                    key.startsWith("intake") || key.startsWith("user_attr")
                    || key.startsWith("user_list") || key.startsWith("results"))
                {
                    String[] values = data.getParameters().getStrings(key);
                    for (int j=0; j<values.length; j++)
                    {
                        buf.append('&').append(key);
                        buf.append('=').append(values[j]);
                    }
                }
            }
            queryString = buf.toString();
            if (queryString.length() == 0) 
            {
                queryString = 
                    ((ScarabUser)data.getUser()).getMostRecentQuery();
            }
        }
        return queryString;
    }
        
    /**
        Retrieves list of all issue id's and puts in the context.
    */
    private void getAllIssueIds(RunData data)
    {
        ParameterParser pp = data.getParameters();
        String[] allIssueIds = pp.getStrings("all_issue_ids");
        if (allIssueIds != null)
        {
            for (int i =0; i< allIssueIds.length; i++)
            {
                pp.add("issue_ids", allIssueIds[i]);
            }
        }
    }

    /**
        Retrieves list of selected issue id's and puts in the context.
    */
    private List getSelected(RunData data)
    {
        List newIssueIdList = new ArrayList();
        String[] selectedIds = data.getParameters().getStrings("issue_ids");
        if (selectedIds != null) 
        {
            for (int i=0; i<selectedIds.length; i++) 
            {
                newIssueIdList.add(selectedIds[i]);
            }
        }        
        return newIssueIdList;
    }
}
