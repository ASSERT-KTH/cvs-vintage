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

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.RIssueTypeAttribute;
import org.tigris.scarab.om.RIssueTypeOption;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: IssueTypeAttributeEdit.java,v 1.16 2004/05/10 21:04:44 dabbous Exp $
 */
public class IssueTypeAttributeEdit extends RequireLoginFirstAction
{
    /**
     * Changes the properties of existing AttributeOptions.
     */
    public synchronized void doSave(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Attribute attribute = scarabR.getAttribute();
        IssueType issueType =  scarabR.getIssueType();
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(getLocalizationTool(context)
                                       .get("SystemSpecifiedIssueType"));
            return;
        }
        if (intake.isAllValid())
        {
            List rios = issueType.getRIssueTypeOptions(attribute, false);
            // Check for duplicate sequence numbers
            if (areThereDupeSequences(rios, intake,
                    "RIssueTypeOption","Order", 0))
            {
                scarabR.setAlertMessage(l10n.format("DuplicateSequenceNumbersFound",
                         l10n.get("AttributeOptions").toLowerCase()));
                return;
            }

            if (rios != null)
            {
                for (int i=rios.size()-1; i>=0; i--) 
                {
                    RIssueTypeOption rio = (RIssueTypeOption)rios.get(i);
                    Group rioGroup = intake.get("RIssueTypeOption",
                                     rio.getQueryKey(), false);
                    rioGroup.setProperties(rio);
                    rio.save();
                    ScarabCache.clear();
                    scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
                }
            }
        }
        if (attribute.isOptionAttribute())
        {
            List options = issueType.getRIssueTypeOptions(attribute, true);
            if (options == null || options.isEmpty())
            {
                RIssueTypeAttribute ria = issueType.getRIssueTypeAttribute(attribute);
                if (ria.getRequired())
                {
                    ria.setRequired(false);        
                    ria.save();
                    scarabR.setAlertMessage(l10n.get("DeletedOptionsFromRequiredAttribute"));
                }
            }
        }
    }

    /**
     * Unmaps attribute options to issueTypes.
     */
    public void doDeleteissuetypeoptions(RunData data,
                                          TemplateContext context) 
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        IssueType issueType = scarabR.getIssueType();
        Attribute attribute = scarabR.getAttribute();

        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(l10n.get("SystemSpecifiedIssueType"));
            return;
        }
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String optionId;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("delete_"))
            {
               optionId = key.substring(7);
               AttributeOption option = AttributeOptionManager
                  .getInstance(new Integer(optionId));

               RIssueTypeOption rio = issueType.getRIssueTypeOption(option);
               List rios = issueType.getRIssueTypeOptions(attribute,
                                                          false);
               try
               {
                   rio.delete(user, scarabR.getCurrentModule());
                   rios.remove(rio);
               }
               catch (Exception e)
               {
                   scarabR.setAlertMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
               }
               scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
            }
        }        
        if (attribute.isOptionAttribute())
        {
            List options = issueType.getRIssueTypeOptions(attribute, true);
            if (options == null || options.isEmpty())
            {
                RIssueTypeAttribute ria = issueType.getRIssueTypeAttribute(attribute);
                if (ria.getRequired())
                {
                    ria.setRequired(false);        
                    ria.save();
                    scarabR.setAlertMessage(l10n.get("DeletedOptionsFromRequiredAttribute"));
                }
            }
        }
    }


    /**
     * Selects option to add to attribute.
     */
    public void doSelectissuetypeoption(RunData data, 
                                         TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(l10n.get("SystemSpecifiedIssueType"));
            return;
        }
        String[] optionIds = data.getParameters().getStrings("option_ids");
 
        if (optionIds == null || optionIds.length <= 0)
        {
            scarabR.setAlertMessage(l10n.get("SelectOption"));
            return;
        }
        else
        {        
            for (int i=0; i < optionIds.length; i++)
            { 
                AttributeOption option = null;
                try
                {
                    option = scarabR.getAttributeOption(new Integer(optionIds[i]));
                    issueType.addRIssueTypeOption(option);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            doCancel(data, context);
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
        }
    }
    /**
     * Overridden method to check for system defined issue types
     * and prevent new attributes from being added to them.
     */

    public void doGotoothertemplate(RunData data,
                                     TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        IssueType issueType =  scarabR.getIssueType();
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(l10n.get("SystemSpecifiedIssueType"));
            return;
        }
        super.doGotoothertemplate(data,context);
    }

    /**
     * Manages clicking of the Done button
     */
    public void doDone( RunData data, TemplateContext context )
        throws Exception
    {
        doSave(data, context);
        if (getScarabRequestTool(context).getAlertMessage() == null)
        {
            doCancel( data, context);
        }
    }

}
