package org.tigris.scarab.actions.admin;

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

import java.util.ArrayList;
import java.util.List;

// Turbine Stuff 
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.ParameterParser;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.intake.model.BooleanField;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionPeer;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;

/**
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: ModuleAttributeEdit.java,v 1.3 2002/01/03 17:16:39 jon Exp $
 */
public class ModuleAttributeEdit extends RequireLoginFirstAction
{
    /**
     * Changes the properties of existing AttributeOptions.
     */
    public synchronized void doSave ( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Attribute attribute = scarabR.getAttribute();

        if ( intake.isAllValid())
        {
            ModuleEntity me = scarabR.getCurrentModule();
            IssueType issueType = scarabR.getIssueType();
            List rmos = me.getRModuleOptions(attribute, issueType);
            for (int i=rmos.size()-1; i>=0; i--) 
            {
                RModuleOption rmo = (RModuleOption)rmos.get(i);
                Group rmoGroup = intake.get("RModuleOption", 
                                 rmo.getQueryKey(), false);
                rmoGroup.setProperties(rmo);
                rmo.save();
            }
        } 
    }

    /**
     * Unmaps attribute options to modules.
     */
    public void doDeleteattributeoptions( RunData data,
                                          TemplateContext context ) 
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        ModuleEntity module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getCurrentIssueType();
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
               AttributeOption option = AttributeOption
                  .getInstance(new NumberKey(optionId));

               RModuleOption rmo = module.getRModuleOption(option, issueType);
               try
               {
                   rmo.delete(user);
               }
               catch (Exception e)
               {
                   data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
               }

               // Remove option - module mapping from template type
               RModuleOption rmo2 = module.getRModuleOption(option, 
                   scarabR.getIssueType(issueType.getTemplateId().toString()));
               try
               {
                   rmo2.delete(user);
               }
               catch (Exception e)
               {
                   data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
               }
            }
        }        
    }


    /**
     * Selects option to add to attribute.
     */
    public void doSelectattributeoption( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ModuleEntity module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();
        IssueType templateType = 
            scarabR.getIssueType(issueType.getTemplateId().toString());
        AttributeOption option = scarabR.getAttributeOption();

        if (option.getOptionId() == null)
        { 
            data.setMessage("Please select an option.");
        }
        else
        {        
            RModuleOption rmo = module.
                 addRModuleOption(issueType, option);
            rmo.save();

            // add module-attributeoption mappings to template type
            RModuleOption rmo2 = module.
                 addRModuleOption(templateType, option);
            rmo2.save();
        }
    }

}
