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
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.MITListManager;
import org.tigris.scarab.om.RModuleIssueTypeManager;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.Log;

/**
 * This class is responsible for building a list of Module/IssueTypes based
 * on the current module and then either going to the AdvancedQuery screen
 * to define a query or running a canned query and listing the results.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: ModuleQuery.java,v 1.9 2003/01/08 22:39:07 jmcnally Exp $
 */
public class ModuleQuery extends RequireLoginFirstAction
{
    public void doPerform(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        // the list to add items to
        MITList list = null;

        boolean isAllIssueTypes = data.getParameters().getBoolean("allit");
        if (isAllIssueTypes) 
        {
            list = MITListManager.getCurrentModuleAllIssueTypesList(user);
            user.setCurrentMITList(list);
        }
        else 
        {
            String[] rmitIds = data.getParameters().getStrings("rmit_id");
            if (rmitIds == null || rmitIds.length == 0) 
            {
                scarabR.setAlertMessage(
                    l10n.get("MustSelectAtLeastOneIssueType"));
                return;
            }
            else 
            {
                List rmits = new ArrayList(rmitIds.length);
                for (int i=0; i<rmitIds.length; i++) 
                {
                    try
                    {
                        rmits.add(RModuleIssueTypeManager
                            .getInstance(rmitIds[i]));
                    }
                    catch (Exception e)
                    {
                        // would probably be a hack of the form
                        scarabR.setAlertMessage(
                          l10n.get("InvalidIssueTypeId"));
                        return;
                    }
                }
                user.setCurrentMITList(null);
                user.addRMITsToCurrentMITList(rmits);
                // Another oddity due to ScarabUserImpl not extending
                // AbstractScarabUser
                user.getCurrentMITList().setScarabUser(user);
            }
        }
        
        // Do we go to AdvancedQuery.vm or run a canned query?
        String queryType = data.getParameters().getString("querytype");
        if ("custom".equals(queryType)) 
        {
            try
            {
                // we do this here because getSearch() can throw an exception
                // if it does throw an exception, then we want to show the 
                // ModuleQuery page again. if it doesn't, then we put the result
                // into the context so that AdvancedQueryMacro can access it 
                // instead of having to call the method yet again which would
                // have a performance impact. kind of ugly, but it is in the 
                // name of performance and not throwing exceptions. =) (JSS)
                IssueSearch is = scarabR.getSearch();
                context.put("searchPutInContext", is);
                setTarget(data, "AdvancedQuery.vm");
            }
            catch (java.lang.IllegalArgumentException e)
            {
                scarabR.setAlertMessage(l10n.get("NoMatchingIssues"));
                Log.get().debug("", e);
            }
        }
        else if ("all".equals(queryType) || "my".equals(queryType)) 
        {
            String query = null; 
            if ("all".equals(queryType)) 
            {
                query = "";
            }
            else 
            {
                String userId = user.getQueryKey();
                StringBuffer sb = new StringBuffer(26 + 2*userId.length());
                query = sb.append("&user_list=").append(userId)
                    .append("&user_attr_").append(userId).append("=any")
                    .toString();
            }
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
                setTarget(data, "IssueList.vm");
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get("MustSelectQueryType"));
        }
    }
}
