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

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.ParameterParser;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

import org.apache.torque.om.NumberKey; 

// Scarab Stuff
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.QueryManager;
import org.tigris.scarab.om.RQueryUser;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;

/**
 * This class is responsible for managing the query lists (deleting queries).
 *    
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: QueryList.java,v 1.20 2003/02/04 11:25:59 jon Exp $
 */
public class QueryList extends RequireLoginFirstAction
{

    public void doSave(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Module me = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getCurrentIssueType();
       
        if (intake.isAllValid())
        {
            List queries = scarabR.getQueries();
            for (int i = 0; i < queries.size(); i++)
            {    
                Query query = (Query)queries.get(i);
                RQueryUser rqu = query.getRQueryUser(user);
                Group queryGroup = intake.get("RQueryUser",
                                              rqu.getQueryKey(), false);
                Field sub = queryGroup.get("Subscribed");
                if (sub.toString().equals("true"))
                {
                    Field freq = queryGroup.get("Frequency");
                    freq.setRequired(true);
                    if (freq.isValid())
                    {
                       queryGroup.setProperties(rqu);
                       rqu.save();
                    }
                    else
                    {
                       freq.setMessage("EnterSubscriptionFrequency");
                    }
                }
            }
       }
       else
       {
           scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
       }

       // Delete previous default
       user.resetDefaultQuery(me, issueType);

       // Save default query.
       String queryId = data.getParameters().getString("default");
       if (queryId != null && queryId.length() > 0)
       {
           Query defaultQuery = QueryManager
               .getInstance(new NumberKey(queryId), false);
           RQueryUser rqu = defaultQuery.getRQueryUser(user);
           rqu.setIsdefault(true);
           rqu.save();
       }
    } 


    public void doDeletequeries(RunData data, TemplateContext context)
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String queryId;
        ScarabUser user = (ScarabUser)data.getUser();

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("action_"))
            {
               queryId = key.substring(7);
               Query query = QueryManager
                   .getInstance(new NumberKey(queryId), false);
               try
               {
                   query.delete(user);
                   query.save();
               }
               catch (Exception e)
               {
                   ScarabLocalizationTool l10n = getLocalizationTool(context);
                   getScarabRequestTool(context).setAlertMessage(
                       l10n.get(NO_PERMISSION_MESSAGE));
               }

            }
        } 
    } 

    public void doNewquery(RunData data, TemplateContext context)
        throws Exception
    {
        setTarget(data, "AdvancedQuery.vm");     
    }
        
    public void doCopyquery(RunData data, TemplateContext context)
        throws Exception
    {
        ParameterParser pp = data.getParameters();
        Object[] keys = pp.getKeys();
        String key;
        String queryId;
        Query query;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("action_"))
            {
               queryId = key.substring(7);
               query = QueryManager
                   .getInstance(new NumberKey(queryId), false);
               query.copyQuery((ScarabUser)data.getUser());
             }
         }
     }

}
