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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.RunData;
import org.apache.turbine.ParameterParser;

import org.apache.commons.collections.SequencedHashMap;
import org.apache.commons.collections.ExtendedProperties;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.TurbineServices;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.MITListManager;
import org.tigris.scarab.om.MITListItem;
import org.tigris.scarab.om.MITListItemManager;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.RModuleIssueTypePeer;
import org.tigris.scarab.om.RModuleIssueTypeManager;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.tools.ScarabRequestTool;

/**
 * This class is responsible for building a list of Module/IssueTypes.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: DefineXModuleList.java,v 1.8 2002/08/06 17:39:00 jmcnally Exp $
 */
public class DefineXModuleList extends RequireLoginFirstAction
{
    public void doGotoquerywithinternallist(RunData data, TemplateContext context)
        throws Exception
    {
        String listId = data.getParameters().getString("pd_list_id");
        if (listId == null || listId.length()==0)
        {
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            scarabR.setAlertMessage(
                "No predefined cross module list was selected.");
        }
        else 
        {
            ScarabUser user = (ScarabUser)data.getUser();
            MITList list = setAndGetCurrentList(listId, data, context);   
            if (list != null)
            {
                setTarget(data, "AdvancedQuery.vm");            
            }            
        }
    }        

    public void doGotoquery(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        MITList currentList = user.getCurrentMITList();
        if (currentList != null && !currentList.isEmpty()) 
        {
            setTarget(data, "AdvancedQuery.vm");                
        }
        else
        {
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            scarabR.setAlertMessage("A list containing at least one " + 
                "module/issue type pair must be selected.");
        }
    }

    public void doChooselist(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        String listId = data.getParameters().getString("list_id");
        if (listId == null || listId.length()==0)
        {
            scarabR.setAlertMessage("No saved cross module query was selected.");
        }
        else 
        {
            ScarabUser user = (ScarabUser)data.getUser();
            MITList list = setAndGetCurrentList(listId, data, context);   
            if (list != null && !list.getModifiable())
            {
                setTarget(data, "AdvancedQuery.vm");
            }
        }        
    }

    private MITList setAndGetCurrentList(String listId, RunData data, 
                                         TemplateContext context)
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        MITList list = null;
        try
        {
            list = MITListManager.getInstance(new NumberKey(listId));
            if (list == null) 
            {
                scarabR.setAlertMessage("An invalid id was entered: "+listId);
            }
            else 
            {
                list = list.copy();
                user.setCurrentMITList(list);
                list.setScarabUser(user);
            }
        }
        catch (Exception e)
        {
            scarabR.setAlertMessage("An invalid id was entered: "+listId);
        }
        return list;
    }

    public void doRemoveitemsfromlist(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        String[] mitids = data.getParameters().getStrings("mitlistitem");
        if (mitids == null || mitids.length == 0) 
        {
            scarabR.setAlertMessage("No items were selected for removal.");
        }
        else 
        {
            user.removeItemsFromCurrentMITList(mitids);
            scarabR.setConfirmMessage(mitids.length + " items were removed.");
        }
    }

    public void doGotosavelist(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        MITList list = user.getCurrentMITList();
        if (list == null) 
        {
            scarabR.setAlertMessage("Application error: list was null.");
            Log.get().error("Current list was null in DefineXModuleList.doGotosavelist.");
        }
        else if (list.isAnonymous())
        {
            list.save();
            scarabR.setConfirmMessage("Your changes were saved.");
            String queryId = data.getParameters().getString("queryId");
            if (queryId != null && queryId.length() > 0) 
            {
                setTarget(data, "EditQuery.vm");
            }
        }
        else
        {
            list.setName(null);
            setTarget(data, "EditXModuleList.vm");
        }
    }

    public void doStartover(RunData data, TemplateContext context)
        throws Exception
    {
        ((ScarabUser)data.getUser()).setCurrentMITList(null);
    }

    public void doSavelist(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
       
        if (intake.isAllValid()) 
        {
            ScarabUser user = (ScarabUser)data.getUser();
            MITList list = user.getCurrentMITList();
            Group group = 
                intake.get("MITList", list.getQueryKey(), false);
            group.setProperties(list);
            // check if the name already exists and inactivate the old list
            MITList oldList = MITListManager
                .getInstanceByName(list.getName(), user);
            if (oldList != null) 
            {
                // oldList should not be the same as the new, but checking
                // will not hurt
                if (!list.equals(oldList)) 
                {                                    
                    oldList.setActive(false);
                    oldList.save();
                }
            }
            // save the new list
            list.save();

            // if the list is modifiable, we do not keep the saved list
            // as the current list.  Copy it, so that changes do not
            // affect the saved version.
            if (list.getModifiable()) 
            {
                user.setCurrentMITList(list.copy());
            }
            
            scarabR.setConfirmMessage("Module/issue type list was saved.");
            setTarget(data, "home,XModuleList.vm");
        }
    }

    public void doAddselectedrmits(RunData data, TemplateContext context)
        throws Exception
    {
        String[] rmitids = data.getParameters().getStrings("rmitid");
        List selectedrmits = null;
        if (rmitids != null && rmitids.length != 0) 
        {
            selectedrmits = new ArrayList(rmitids.length);
            for (int i=0; i<rmitids.length; i++) 
            {
                selectedrmits
                    .add(RModuleIssueTypeManager.getInstance(rmitids[i]));
            }
            ScarabUser user = (ScarabUser)data.getUser();            
            user.addRMITsToCurrentMITList(selectedrmits);

            // it would be better to move this logic to AbstractScarabUser
            // (ASM) but because ScarabUserImpl does not extend ASM, it
            // lives here :-(
            MITList mitList = user.getCurrentMITList();
            if (mitList.getScarabUser() == null  ) 
            {
                mitList.setScarabUser(user);
            }

        }
    }
}
