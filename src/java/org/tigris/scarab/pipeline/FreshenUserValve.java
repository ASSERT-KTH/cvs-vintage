package org.tigris.scarab.pipeline;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
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

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.io.IOException;
import org.apache.turbine.RunData;
import org.apache.turbine.ParameterParser;
import org.apache.turbine.TurbineException;
import org.apache.turbine.pipeline.AbstractValve;
import org.apache.turbine.ValveContext;
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.IssueTypeManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.MITListManager;
import org.tigris.scarab.om.RModuleIssueType;

/**
 * This valve clears any stale data out of the user due to aborted wizards.  
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: FreshenUserValve.java,v 1.23 2003/06/06 05:14:46 dlr Exp $
 */
public class FreshenUserValve 
    extends AbstractValve
{
    private static final Map XMIT_SCREENS = new HashMap();

    public FreshenUserValve()
    {
        XMIT_SCREENS.put("home,XModuleList.vm", null);
        XMIT_SCREENS.put("AdvancedQuery.vm", null);
        XMIT_SCREENS.put("IssueList.vm", null);
        XMIT_SCREENS.put("ViewIssue.vm", null);
        XMIT_SCREENS.put("QueryList.vm", null);
        XMIT_SCREENS.put("SaveQuery.vm", null);
        XMIT_SCREENS.put("EditQuery.vm", null);
        XMIT_SCREENS.put("UserList.vm", null);
        XMIT_SCREENS.put("ConfigureIssueList.vm", null);
        XMIT_SCREENS.put("EditXModuleList.vm", null);
        XMIT_SCREENS.put("reports,Info.vm", null);
        XMIT_SCREENS.put("reports,ConfineDataset.vm", null);
        XMIT_SCREENS.put("reports,XModuleList.vm", null);
        XMIT_SCREENS.put("reports,AxisConfiguration.vm", null);
        XMIT_SCREENS.put("reports,Report_1.vm", null);
        // this is not a real .vm file, but a pointer to a java screen class
        XMIT_SCREENS.put("IssueListExport.vm", null);
    }

    /**
     * @see org.apache.turbine.Valve#invoke(RunData, ValveContext)
     */
    public void invoke(RunData data, ValveContext context)
        throws IOException, TurbineException
    {
        ScarabUser user = (ScarabUser)data.getUser();
        try
        {
            setCurrentModule(user, data);
            setCurrentIssueType(user, data);
        }
        catch(Exception e)
        {
            Log.get().error("", e);
            // Ignore on purpose because if things
            // are screwed up, we don't need to know about it.
        }

        // set the thread key 
        ParameterParser parameters = data.getParameters();
        String key = parameters.getString(ScarabConstants.THREAD_QUERY_KEY);
        if (key != null) 
        {
            user.setThreadKey(new Integer(key));
        }
        else
        {
            user.setThreadKey(null);
        }
        
        // remove any report that was aborted
        String reportKey = parameters.getString(ScarabConstants.REMOVE_CURRENT_REPORT);
        if (reportKey != null && reportKey.length() > 0)
        {
            user.setCurrentReport(reportKey, null);
        }

        // remove the current module/issuetype list, if needed
        String removeMitKey = 
            parameters.getString(ScarabConstants.REMOVE_CURRENT_MITLIST_QKEY);
        if (removeMitKey != null 
            || !XMIT_SCREENS.containsKey(data.getTarget()))
        {
            Log.get().debug("xmit list set to null");
            user.setCurrentMITList(null);
        }

        // override the current module/issuetype list if one is given
        // in the url.
        String mitid = parameters.getString(ScarabConstants.CURRENT_MITLIST_ID);
        if (mitid != null) 
        {
            MITList mitList = null;
            try
            {
                // FIXME: no getInstance(Integer)
                mitList = MITListManager.getInstance(new NumberKey(mitid));
                user.setCurrentMITList(mitList);
                mitList.setScarabUser(user);
            }
            catch (TorqueException e)
            {
                throw new TurbineException(e);
            }
        }

        // should add the currently reporting issue here as well

        // Pass control to the next Valve in the Pipeline
        context.invokeNext(data);
    }

    private void setCurrentModule(ScarabUser user, RunData data)
        throws TurbineException, Exception
    {
        Module module = null;
        ParameterParser parameters = data.getParameters();
        String key = parameters.getString(ScarabConstants.CURRENT_MODULE);
        if (key != null) 
        {
            try
            {
                module = ModuleManager.getInstance(new Integer(key));
            }
            catch (Exception e)
            {
                throw new TurbineException(e);
            }
        }
        else if (parameters.getString("id") != null) 
        {
            try  
            {
                module = IssueManager.getIssueById(parameters.getString("id")).getModule();
                parameters.setString(ScarabConstants.CURRENT_MODULE, 
                             module.getQueryKey());
            }
            catch (Exception e)
            {
                // ignore
                Log.get().debug("'id' parameter was available, "
                    + parameters.getString("id") + 
                    ", but did not contain enough info to create issue.");
            }
        }
        // If they have just changed modules,
        // Set the current issue type to the new module's first active issue type.
        Module currentModule = user.getCurrentModule();
        if (module != null && currentModule != null &&
            !module.getModuleId().equals(currentModule.getModuleId()))
        {
            IssueType issueType = null;
            List navIssueTypes = module.getNavIssueTypes();
            if (navIssueTypes.size() > 0)
            {
                issueType = (IssueType)navIssueTypes.get(0);
            }
            else 
            {
                List activeIssueTypes = module.getIssueTypes(true);
                if (activeIssueTypes.size() > 0)
                {
                    issueType = (IssueType)activeIssueTypes.get(0);
                }
            }
            user.setCurrentIssueType(issueType);
            if (issueType != null)
            {
                 parameters.setString(ScarabConstants.CURRENT_ISSUE_TYPE, 
                            issueType.getQueryKey());
            }
            else
            {
                 parameters.setString(ScarabConstants.CURRENT_ISSUE_TYPE, "");
            }
        }
        user.setCurrentModule(module);
    }

    // FIXME! the setCurrentModule method now contains code setting the 
    // issue type.  So the separation is now fuzzy, we should probably combine
    // the methods to avoid confusion
    private void setCurrentIssueType(ScarabUser user, RunData data)
        throws TurbineException
    {
        IssueType issueType = null;
        ParameterParser parameters = data.getParameters();
        String key = parameters.getString(ScarabConstants.CURRENT_ISSUE_TYPE);
        if (key != null && key.length() > 0)
        {
            try
            {
                issueType = IssueTypeManager.getInstance(new Integer(key));
            }
            catch (NumberFormatException noIssueType)
            {
            }
            catch (Exception e)
            {
                throw new TurbineException(e);
            }
        }
        else if (parameters.getString("id") != null) 
        {
            try  
            {
                issueType = 
                    IssueManager.getIssueById(parameters.getString("id")).getIssueType();
                parameters.setString(ScarabConstants.CURRENT_ISSUE_TYPE, 
                             issueType.getQueryKey());
            }
            catch (Exception e)
            {
                // ignore
                Log.get().debug("'id' parameter was available, " 
                    + parameters.getString("id") + 
                    ", but did not contain enough info to create issue.");
            }
        }

        boolean isActive = false;
        if (issueType != null) 
        {
            try 
            {
                RModuleIssueType rmit = user.getCurrentModule()
                    .getRModuleIssueType(issueType);
                isActive = rmit != null && rmit.getActive();
            }
            catch (Exception e)
            {
                Log.get().warn("Unable to locate a mapping between the " +
                               "specified issue type and the current module",
                               e);
            }
        }
        
        if (isActive)
        {
            user.setCurrentIssueType(issueType);
        }
        else 
        {
            user.setCurrentIssueType(null);
            Log.get().debug("Set current IssueType to null");
        }
    }
}
