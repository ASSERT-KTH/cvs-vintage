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

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.ParameterParser;

import org.apache.torque.om.NumberKey; 

// Scarab Stuff
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.QueryPeer;
import org.tigris.scarab.om.RQueryUser;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImpl;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;

/**
    This class is responsible for managing the query lists (deleting queries).
    ScarabIssueAttributeValue
    @author <a href="mailto:elicia@collab.net">Elicia David</a>
    @version $Id: QueryList.java,v 1.3 2001/09/30 18:31:38 jon Exp $
*/
public class QueryList extends RequireLoginFirstAction
{
    public void doDeletequeries( RunData data, TemplateContext context )
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
               Query query = (Query) QueryPeer
                                     .retrieveByPK(new NumberKey(queryId));
               try
               {
                   query.delete(user);
                   query.save();
               }
               catch (Exception e)
               {
                   data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
               }

            }
        } 
    } 

    public void doSubscribe( RunData data, TemplateContext context )
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String queryId;
        String frequencyId;
        Query query;
        ScarabUser user = (ScarabUser)data.getUser();

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("action_"))
            {
               queryId = key.substring(7);
               frequencyId = data.getParameters()
                            .getString("frequency_" + queryId);
               query = (Query) QueryPeer
                                     .retrieveByPK(new NumberKey(queryId));
               if (query.isUserSubscribed(user.getUserId()))
               {
                   data.setMessage("You are already subscribed " + 
                                  "to query 'query.Name'.");               
               }
               else
               {
                   RQueryUser rqu = new RQueryUser();
                   rqu.setUserId(user.getUserId());
                   rqu.setQuery(query);
                   rqu.setSubscriptionFrequency(frequencyId);
                   rqu.save();
               }

            }
        } 
    } 

    public void doUnsubscribe( RunData data, TemplateContext context )
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String queryId;
        Query query;
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("action_"))
            {
               queryId = key.substring(7);
               query = (Query) QueryPeer
                       .retrieveByPK(new NumberKey(queryId));
               if (!query.isUserSubscribed(user.getUserId()))
               {
                   data.setMessage("You are not subscribed " +
                                   "to query 'query.Name'.");               
               }
               else
               {
                   try
                   {
                       RQueryUser rqu = query
                                 .getSubscription(user.getUserId());
                       rqu.doDelete(user, scarabR.getCurrentModule());
                   }
                   catch (Exception e)
                   {
                       data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
                   }
               }

            }
        } 
    } 

    public void doSavefrequencies( RunData data, TemplateContext context )
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String queryId;
        String frequencyId;
        Query query;
        ScarabUser user = (ScarabUser)data.getUser();

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("frequency_"))
            {
               queryId = key.substring(10);
               query = (Query) QueryPeer
                      .retrieveByPK(new NumberKey(queryId));
               frequencyId = data.getParameters()
                            .getString(key);
               if (query.isUserSubscribed(user.getUserId()))
               {
                   RQueryUser rqu = query
                             .getSubscription(user.getUserId());
                   rqu.setSubscriptionFrequency(frequencyId);
                   rqu.save();
               }
            }
        }
    } 

    public void doNewquery( RunData data, TemplateContext context )
        throws Exception
    {
        setTarget(data, "AdvancedQuery.vm");     
    }
        
    public void doCopyquery( RunData data, TemplateContext context )
        throws Exception
    {
        ParameterParser pp = data.getParameters();
        Object[] keys = pp.getKeys();
        String key;
        String queryId;
        Query query;
        Query newQuery = null;
        ScarabUser user = (ScarabUser)data.getUser();

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("action_"))
            {
               queryId = key.substring(7);
               query = (Query) QueryPeer
                      .retrieveByPK(new NumberKey(queryId));
               newQuery = query.copy();
               newQuery.setName(query.getName() + " (copy)");
               newQuery.save();
               break;
             }
         }
     }

    /**
        This manages clicking the Cancel button
    */
    public void doCancel( RunData data, TemplateContext context ) throws Exception
    {
        String template = Turbine.getConfiguration()
            .getString("template.homepage", "Start.vm");
        setTarget(data, template);
    }
    
    /**
        calls doCancel()
    */
    public void doPerform( RunData data, TemplateContext context ) throws Exception
    {
        doCancel(data, context);
    }
}
