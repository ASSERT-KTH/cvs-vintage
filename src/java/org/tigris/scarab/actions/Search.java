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
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;

// Turbine Stuff 
import org.apache.turbine.ParameterParser; 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.commons.lang.StringUtils;

import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.util.parser.ValueParser;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.QueryPeer;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.Scope;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabUtil;

/**
 *  This class is responsible for searching.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: Search.java,v 1.121 2003/04/01 02:50:43 jon Exp $
 */
public class Search extends RequireLoginFirstAction
{
    private static final String ADD_USER = "add_user";
    private static final String ADD_USER_BY_USERNAME = "add_user_by_username";
    private static final String SELECTED_USER = "select_user";
    private static final String USER_LIST = "user_list";
    
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
            String format = data.getParameters().getString("format");
            if (StringUtils.isNotEmpty(format))
            {
                // Send to the IssueListExport screen (which actually
                // has no corresponding Velocity template).
                setTarget(data, "IssueListExport.vm");
            }
            else
            {
                String template = data.getParameters()
                    .getString(ScarabConstants.NEXT_TEMPLATE, 
                               "IssueList.vm");
                setTarget(data, template);
            }
        }
    }

    /**
        Redirects to form to save the query. May redirect to Login page.
    */
    public void doRedirecttosavequery(RunData data, TemplateContext context)
         throws Exception
    {
        data.getParameters().setString("queryString", getQueryString(data));
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        if (scarabR.hasPermission(ScarabSecurity.USER__EDIT_PREFERENCES))
        {
            setTarget(data, "SaveQuery.vm");
        }
        else 
        {
            scarabR.setAlertMessage(
                getLocalizationTool(context).get(NO_PERMISSION_MESSAGE));
        }
    }

    public void doRedirecttocrossmodulelist(RunData data, TemplateContext context)
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
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        if (!scarabR.hasPermission(ScarabSecurity.USER__EDIT_PREFERENCES))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }

        IntakeTool intake = getIntakeTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Module module = scarabR.getCurrentModule();
        Query query = scarabR.getQuery();
        Group queryGroup = intake.get("Query", 
                                      query.getQueryKey());

        Field name = queryGroup.get("Name");
        name.setRequired(true);
        data.getParameters().setString("queryString", getQueryString(data));

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
                // associate the query with a new list, the current
                // implementation does not allow for multiple queries to 
                // work from the same MITList and this guarantees they 
                // will not accidently be linked.
                currentList = currentList.copy();
                query.setMITList(currentList);
                if (!currentList.isSingleModule()) 
                {
                    query.setModule(null);
                    query.setScopeId(Scope.PERSONAL__PK);                    
                }
            }
             
            ScarabUser[] userList = 
                module.getUsers(ScarabSecurity.ITEM__APPROVE);
            if (checkForDupes(query, user, module))
            {
                scarabR.setAlertMessage(l10n.get("DuplicateQueryName"));
            }
            else if (Scope.MODULE__PK.equals(query.getScopeId()) 
                 && user.hasPermission(ScarabSecurity.ITEM__APPROVE, module)
                 && (userList == null || userList.length == 0))
            {
                scarabR.setAlertMessage(
                    l10n.format("NoApproverAvailable", module.getName()));
            }
            else 
            {            
                query.saveAndSendEmail(user, module, context);
                String template = data.getParameters()
                    .getString(ScarabConstants.NEXT_TEMPLATE);
                scarabR.resetSelectedUsers();
                setTarget(data, template);            
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
    }

    public boolean doEditqueryinfo(RunData data, TemplateContext context)
        throws Exception
    {
        boolean success = false;
        IntakeTool intake = getIntakeTool(context);        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Module module = scarabR.getCurrentModule();
        Query query = getScarabRequestTool(context).getQuery();
        Group queryGroup = intake.get("Query", 
                                      query.getQueryKey());
        queryGroup.get("Name").setRequired(true);
        if (intake.isAllValid()) 
        {
            queryGroup.setProperties(query);
            if (checkForDupes(query, user, module))
            {
                scarabR.setAlertMessage(l10n.get("DuplicateQueryName"));
            }
            else
            {
                query.saveAndSendEmail(user, module, context);
                success = true;
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
        return success;
    }

    public void doGotoeditquery(RunData data, TemplateContext context)
         throws Exception
    {        
        String queryString = getQueryString(data);
        ((ScarabUser)data.getUser()).setMostRecentQuery(queryString);
        data.getParameters().setString("queryString", queryString);
        getScarabRequestTool(context).resetSelectedUsers();
    }

    /**
        Edits the stored query.
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
        scarabR.resetSelectedUsers();
        ((ScarabUser)data.getUser()).setMostRecentQuery(newValue);
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
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        if (go != null && go.length() > 0)
        {
            // if the string is a number, then execute
            // doRunstoredquery()
            if (StringUtils.isNumeric(go))
            {
                data.getParameters().setString("queryId", go);
                doRunstoredquery(data, context);
            }
            else if (go.equals("AdvancedQuery.vm"))
            {
                // reset selected users map
                scarabR.resetSelectedUsers();
                setTarget(data, go);
            }
            else if (go.equals("IssueList.vm"))
            {
                ScarabUser user = (ScarabUser)data.getUser();
                String userId = user.getQueryKey();
                StringBuffer sb = new StringBuffer(26 + 2*userId.length());
                String query = sb.append("&user_list=").append(userId)
                    .append("&user_attr_").append(userId).append("=any")
                    .toString();
                ((ScarabUser)data.getUser()).setMostRecentQuery(query);
                data.getParameters().add("queryString", query);
                List searchResults = null;
                try
                {
                    searchResults = scarabR.getCurrentSearchResults();
                }
                catch (java.lang.IllegalArgumentException e)
                {
                    // Swallow this exception.
                    Log.get().debug("", e);
                }
                if (searchResults != null && searchResults.size() > 0)
                {
                    context.put("issueList", searchResults);
                    setTarget(data, go);
                }

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
     * Redirects to ViewIssueLong.
     */
    public void doViewall(RunData data, TemplateContext context)
         throws Exception
    {        
        // First clear out issues_ids the user may have selected otherwise we
        // draw the selected issues twice in the results.
        ParameterParser pp = data.getParameters();
        while(pp.containsKey("issue_ids"))
        {
            pp.remove("issue_ids");
        }

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
        Overrides base class.
    */
    public void doDone(RunData data, TemplateContext context)  
        throws Exception
    {
        boolean success = doEditqueryinfo(data, context);
        if (success)
        {
            doEditstoredquery(data, context);
            doCancel(data, context);
        }
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
                        buf.append('=').append(ScarabUtil.urlEncode(values[j]));
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
       Check for duplicate query names. 
       A user cannot create a query with the same name as another one of their queries.
       A user cannot create a project-level query with the same name 
       as another project-level query.
    */
    private boolean checkForDupes(Query query, ScarabUser user, Module module)
        throws Exception
    {
        boolean areThereDupes = false;
        List prevQueries = QueryPeer.getUserQueries(user);
        if (query.getScopeId().equals(Scope.MODULE__PK))
        {
            prevQueries.addAll(QueryPeer.getModuleQueries(module));
        }
        if (prevQueries != null && !prevQueries.isEmpty())
        {
            Long pk = query.getQueryId();
            String name = query.getName();
            for (Iterator i = prevQueries.iterator(); 
                 i.hasNext() && !areThereDupes;)
            {
                Query q = (Query)i.next();
                areThereDupes = (pk == null || !pk.equals(q.getQueryId())) &&
                    name.trim().toLowerCase().equals(
                        q.getName().trim().toLowerCase());
            }
        }
        return areThereDupes;
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

    public void doGotoeditlist(RunData data, TemplateContext context)
        throws Exception
    {
        ValueParser params = data.getParameters();
        Map userMap = ((ScarabUser)data.getUser()).getSelectedUsersMap();
        if (userMap == null || userMap.size() == 0)
        {
            userMap = new HashMap();
            loadUsersFromUserList(data, userMap);
        }
        data.getParameters().setString(ScarabConstants.CANCEL_TEMPLATE,
                                       getCurrentTemplate(data));
        data.getParameters().setString("queryString", getQueryString(data));
        setTarget(data, "UserList.vm");            
    } 

    /**
     * Adds users from temporary working list.
     */
    public void doAddusers(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Map userMap = user.getSelectedUsersMap();
        if (userMap == null)
        {
            userMap = new HashMap();
        }
        ValueParser params = data.getParameters();
        String[] userIds = params.getStrings(ADD_USER);
        if (userIds != null && userIds.length > 0) 
        {
            for (int i =0; i<userIds.length; i++)
            {
                List item = new ArrayList(2);
                String userId = userIds[i];
                String attrId = params.get("user_attr_" + userId);
                userMap.put(userId, attrId);
            } 
            user.setSelectedUsersMap(userMap);
            scarabR.setConfirmMessage(l10n.get("SelectedUsersWereAdded"));
        }
        else 
        {
            scarabR.setAlertMessage(l10n.get("NoUsersSelected"));
        }
    }

    /**
        Adds user to the search form.
    */
    public void doAdduserbyusername(RunData data, TemplateContext context)  
        throws Exception
    {
        ValueParser params = data.getParameters();
        String userName = params.getString("ADD_USER_BY_USERNAME");
        String attrId = params.getString("add_user_attr");
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Map userMap = user.getSelectedUsersMap();
        if (userMap == null || userMap.size() == 0)
        {
            userMap = new HashMap();
            loadUsersFromUserList(data, userMap);
        }

       ScarabUser newUser = scarabR.getUserByUserName(userName);
       ScarabLocalizationTool l10n = getLocalizationTool(context);        
       boolean success = true;
       // we are only interested in users that can be assignees
       if (newUser == null)
       {
          success = false;
       }
       else if (!attrId.equals("any") && !attrId.equals("created_by"))
       {
           Attribute attribute = scarabR.getAttribute(new Integer(attrId));
           MITList mitList = scarabR.getCurrentMITList();
           if (newUser == null || !newUser.hasPermission(attribute.getPermission(), 
                                                         mitList.getModules()))
           {
               success = false;
           }
       }
       if (success)
       {
           userMap.put(newUser.getUserId().toString(), attrId.toString());
           user.setSelectedUsersMap(userMap);
           scarabR.setConfirmMessage(l10n.get("SelectedUsersWereAdded"));
       }
       else
       {
           scarabR.setAlertMessage(l10n.get("UserNotPossibleAssignee"));
       }
    }

    /**
     * Removes users from temporary working list.
     */
    public void doRemoveusers(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context); 
        Map userMap = user.getSelectedUsersMap();
        if (userMap == null || userMap.size() == 0)
        {
            userMap = new HashMap();
            loadUsersFromUserList(data, userMap);
        }
        ValueParser params = data.getParameters();
        String[] userIds =  params.getStrings(SELECTED_USER);
        if (userIds != null && userIds.length > 0) 
        {
            for (int i =0; i<userIds.length; i++)
            {
                List item = new ArrayList(2);
                String userId = userIds[i];
                userMap.remove(userId);
            }
            user.setSelectedUsersMap(userMap);
            scarabR.setConfirmMessage(l10n.get("SelectedUsersWereRemoved"));
        }
        else 
        {
            scarabR.setAlertMessage(l10n.get("NoUsersSelected"));
        }
    }

    /**
     * Changes the user attribute a user is associated with.
     */
    public void doUpdateusers(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Map userMap = user.getSelectedUsersMap();
        ValueParser params = data.getParameters();
        String[] userIds =  params.getStrings(SELECTED_USER);
        if (userIds != null && userIds.length > 0) 
        {
            for (int i =0; i<userIds.length; i++)
            {
                List item = new ArrayList(2);
                List newItem = new ArrayList(2);
                String userId = userIds[i];
                String attrId = params.getString("asso_user_{" + userId + "}");
                userMap.put(userId, attrId);
            }
            user.setSelectedUsersMap(userMap);
            scarabR.setConfirmMessage(l10n.get("SelectedUsersWereModified"));
        }
        else 
        {
            scarabR.setAlertMessage(l10n.get("NoUsersSelected"));
        }
    }

    /**
     * In the case of a saved query, puts the saved query's users
     * Into the selected users map
     */
    public void loadUsersFromUserList(RunData data, Map userMap)
        throws Exception
    {
        ValueParser params = data.getParameters();
        String[] userList = params.getStrings(USER_LIST);
        if (userList !=null && userList.length > 0)
        {
            for (int i =0;i<userList.length;i++)
            {
                String userId = (String)userList[i];
                String attrId = params.get("user_attr_" + userId);
                userMap.put(userId, attrId);
            }
            ((ScarabUser)data.getUser()).setSelectedUsersMap(userMap);
        }
    }

}
