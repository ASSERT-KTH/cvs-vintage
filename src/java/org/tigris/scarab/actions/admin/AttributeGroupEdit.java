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

// Java Stuff
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
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.RAttributeAttributeGroup;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.AttributeGroup;
import org.tigris.scarab.om.AttributeGroupManager;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionPeer;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypePeer;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.services.cache.ScarabCache; 
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.workflow.WorkflowFactory;

/**
 * action methods on RModuleAttribute table
 *      
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: AttributeGroupEdit.java,v 1.26 2002/08/08 23:56:16 elicia Exp $
 */
public class AttributeGroupEdit extends RequireLoginFirstAction
{
    /**
     * Updates attribute group info.
     */
    public void doSaveinfo ( RunData data, TemplateContext context )
        throws Exception
    {
        // Set properties for group info
        IntakeTool intake = getIntakeTool(context);
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = AttributeGroupManager
                            .getInstance(new NumberKey(groupId), false);
        Group agGroup = intake.get("AttributeGroup", 
                                    ag.getQueryKey(), false);
        agGroup.setProperties(ag);
        ag.save();
        data.setMessage(DEFAULT_MSG);  
    }

    /**
     * Changes the properties of existing AttributeGroups and their attributes.
     */
    public void doSave ( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = AttributeGroupManager
                            .getInstance(new NumberKey(groupId), false);
        List attributes = ag.getAttributes();
        Module module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();

        if ( intake.isAllValid() )
        {
            for (int i=attributes.size()-1; i>=0; i--) 
            {
                // Set properties for module-attribute mapping
                Attribute attribute = (Attribute)attributes.get(i);
                RModuleAttribute rma = (RModuleAttribute)module
                                       .getRModuleAttribute(attribute, 
                                                            ag.getIssueType());
                Group rmaGroup = intake.get("RModuleAttribute", 
                                 rma.getQueryKey(), false);

                // if attribute gets set to inactive, delete dependencies
                String newActive = rmaGroup.get("Active").toString();
                String oldActive = String.valueOf(rma.getActive());
                if (newActive.equals("false") && oldActive.equals("true"))
                {
                    WorkflowFactory.getInstance().deleteWorkflowsForAttribute(
                                                  attribute, module, issueType);
                }
                rmaGroup.setProperties(rma);
                String defaultTextKey = data.getParameters()
                    .getString("default_text");
                if ( defaultTextKey != null && 
                     defaultTextKey.equals(rma.getAttributeId().toString()) ) 
                {
                    rma.setIsDefaultText(true);
                    rma.setRequired(true);
                }
                rma.save();

                // Set properties for attribute-attribute group mapping
                RAttributeAttributeGroup raag = 
                    ag.getRAttributeAttributeGroup(attribute);
                Group raagGroup = intake.get("RAttributeAttributeGroup", 
                                 raag.getQueryKey(), false);
                raagGroup.setProperties(raag);
                raag.save();
            }
            data.setMessage(DEFAULT_MSG);  
            ScarabCache.clear();
        } 

    }


    /**
     * Unmaps attributes to modules.
     */
    public void doDeleteattributes( RunData data, TemplateContext context ) 
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Module module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();
        ScarabUser user = (ScarabUser)data.getUser();

        if (!user.hasPermission(ScarabSecurity.MODULE__EDIT, module))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }
       
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attributeId;
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = AttributeGroupManager
            .getInstance(new NumberKey(groupId), false);

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("att_delete_"))
            {
               attributeId = key.substring(11);
               Attribute attribute = AttributeManager
                   .getInstance(new NumberKey(attributeId), false);
               ag.deleteAttribute(attribute, user);
            }
        }

        // If there are no attributes in any of the dedupe
        // Attribute groups, turn off deduping in the module
        boolean areThereDedupeAttrs = false;
        List attributeGroups = module.getAttributeGroups(issueType);
        if (attributeGroups.size() > 0)
        {
            for (int j=0; j<attributeGroups.size(); j++) 
            {
                AttributeGroup agTemp = (AttributeGroup)attributeGroups.get(j);
                if (agTemp.getDedupe() && !agTemp.getAttributes().isEmpty())
                {
                   areThereDedupeAttrs = true;
                }
            }
            if (!areThereDedupeAttrs)
            {
                RModuleIssueType rmit = module.getRModuleIssueType(issueType);
                rmit.setDedupe(false);
                rmit.save();
            }
       }
       ScarabCache.clear();
       data.setMessage(DEFAULT_MSG);  
    }

    /**
     * This manages clicking the create new button on AttributeSelect.vm
     */
    public void doCreatenewglobalattribute( RunData data, 
                                            TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Group attGroup = intake.get("Attribute", IntakeTool.DEFAULT_KEY);
        intake.remove(attGroup);
        scarabR.setAttribute(null);
        setTarget(data, getOtherTemplate(data));
    }


    /**
     * Selects attribute to add to artifact type and attribute group.
     */
    public void doSelectattribute( RunData data, TemplateContext context )
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String[] attributeIds = data.getParameters()
                                    .getStrings("attribute_ids");
 
        if (attributeIds == null || attributeIds.length <= 0)
        { 
            scarabR.setAlertMessage("Please select an attribute.");
            return;
        }
        else
        {        
            for (int i=0; i < attributeIds.length; i++)
            {
                Attribute attribute = 
                    scarabR.getAttribute(new NumberKey(attributeIds[i]));
                AttributeGroup attGroup = scarabR.getAttributeGroup();
                attGroup.addAttribute(attribute);
            }
            doCancel(data, context);
            data.setMessage(DEFAULT_MSG);  
        }
    }

    /**
     * Saves all data when Done is clicked.
     */
    public void doDone ( RunData data, TemplateContext context )
        throws Exception
    {
        doSaveinfo( data, context);
        doSave( data, context);
        doCancel( data, context);
    }
        
}
