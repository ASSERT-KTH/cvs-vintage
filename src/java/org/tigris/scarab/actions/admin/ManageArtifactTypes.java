package org.tigris.scarab.actions.admin;

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
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.ParameterParser;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.services.cache.ScarabCache; 

/**
 * This action handles associating issue types to modules.  It should probably
 * have 'Module' in its name, but regardless, the base permission for this
 * class is Module | Configure, so global artifact type actions do not belong
 * here. 
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: ManageArtifactTypes.java,v 1.27 2003/02/04 11:26:00 jon Exp $
 */
public class ManageArtifactTypes extends RequireLoginFirstAction
{
    /**
     * Changes the properties of existing IssueTypes.
     */
    public synchronized void doSave (RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);

        Module module = scarabR.getCurrentModule();
        List rmits = module.getRModuleIssueTypes();
        int navCount = 0;
        Group rmitGroup = null;

        if (intake.isAllValid())
        {
            for (int i=0;i < rmits.size(); i++)
            {
                RModuleIssueType rmit = (RModuleIssueType)rmits.get(i);
                rmitGroup = intake.get("RModuleIssueType", 
                                 rmit.getQueryKey(), false);
                Field display = rmitGroup.get("Display");

                if (display.toString().equals("true"))
                {
                    navCount++;
                }
                if (navCount > 5)
                {
                   scarabR.setAlertMessage(l10n.get("NavIssueTypeLimit"));
                   return;
                }
            }
            for (int i=0;i < rmits.size(); i++)
            {
                RModuleIssueType rmit = (RModuleIssueType)rmits.get(i);
                rmitGroup = intake.get("RModuleIssueType", 
                                 rmit.getQueryKey(), false);
                rmitGroup.setProperties(rmit);
                rmit.save();
                String pageNum = data.getParameters().getString("pageNum","1");
                data.getParameters().add("pageNum", pageNum);
            }
            ScarabCache.clear();
        } 
    }


    /**
     * Selects issue type to add to module.
     */
    public void doSelectissuetype(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        Module module = scarabR.getCurrentModule();

        if (issueType.getIssueTypeId() == null)
        {
            scarabR.setAlertMessage(l10n.get("SelectIssueType"));
        }
        else if (module.getRModuleIssueType(issueType) != null)
        {
            scarabR.setAlertMessage(l10n.get("IssueTypeAlreadyAssociated"));
        }
        else
        {
            module.addIssueType(issueType);
            ScarabCache.clear();
            scarabR.setConfirmMessage(l10n.get("IssueTypeAddedToModule"));
            setTarget(data, "admin,ManageArtifactTypes.vm");            
        }
    }


    /**
     *   This manages clicking the cancel button
     */
    public void doCreateartifacttype(RunData data, TemplateContext context)
        throws Exception
    {
        data.getParameters().remove("issueTypeId");
        setTarget(data, getOtherTemplate(data));
    }

    /**
     * Deletes an issue type from a module.
     */
    public void doDeletemoduleissuetype (RunData data, 
                                          TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        ParameterParser params = data.getParameters();
        Module module = scarabR.getCurrentModule();
        Object[] keys = params.getKeys();
        String key;
        String issueTypeId;
        List rmits = module.getRModuleIssueTypes();

        boolean foundOne = false;
        boolean success = false;
        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("delete_"))
            {
                if (rmits.size() < 1)
                {
                    scarabR.setAlertMessage(l10n.get("CannotZeroIssueType"));
                    break;
                }
                else
                {
                    issueTypeId = key.substring(7);
                    IssueType issueType = scarabR.getIssueType(issueTypeId);
                    RModuleIssueType rmit = module.getRModuleIssueType(issueType);
                    Group rmitGroup = getIntakeTool(context).get("RModuleIssueType", 
                                 rmit.getQueryKey(), false);
                    Field field = rmitGroup.get("DisplayName");
                    if (issueType != null)
                    {
                        foundOne = true;
                        if (issueType.getLocked())
                        {
                            //scarabR.setAlertMessage(l10n.get("LockedIssueType"));
                            field.setMessage("LockedIssueType");
                        }
                        else if (issueType.hasIssues(module))
                        {
                            scarabR.setAlertMessage(l10n.get("CannotDeleteIssueTypesWithIssues"));
                            field.setMessage("IssueTypeHasIssues");
                        }
                        else
                        {
                            try
                            {
                                // delete module-issue type mappings
                                rmit.delete(user);
                                success = true;
                                module.getNavIssueTypes().remove(issueType);
                            }
                            catch (Exception e)
                            {
                                scarabR.setAlertMessage(l10n.get("CannotDeleteIssueType"));
                            }
                        }

                    }
                }
            }
         
         }
         if (success)
         { 
            scarabR.setConfirmMessage(l10n.get(
                "SelectedIssueTypesRemovedFromModule"));
         }
         if (!foundOne)
         {
            scarabR.setAlertMessage(
                l10n.get("SelectIssueTypeToDeleteFromModule"));
         }
    }
}
