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
import org.tigris.scarab.om.ScarabModule;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RAttributeAttributeGroup;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.AttributeGroup;
import org.tigris.scarab.om.AttributeGroupPeer;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionPeer;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypePeer;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;

/**
 * action methods on RModuleAttribute table
 *      
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: ArtifactTypeEdit.java,v 1.7 2002/01/23 06:21:50 elicia Exp $
 */
public class ArtifactTypeEdit extends RequireLoginFirstAction
{
    /**
     * Adds or modifies an issue type's properties.
     */
    public void doSave ( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        ModuleEntity module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();
        RModuleIssueType rmit = module.getRModuleIssueType(issueType);

        List attGroups = issueType.getAttributeGroups(module);
        List attributeGroups = issueType.getAttributeGroups(module);

        boolean isValid = true;
        boolean areThereDupes = false;
        Field order1 = null;
        Field order2 = null;
        int dupeOrder = 2;
        boolean areThereDedupeAttrs = false;

        // Manage attribute groups
        if (attributeGroups.size() > 0)
        {
            dupeOrder = Integer.parseInt(data.getParameters()
                                                 .getString("dupe_order"));
            // Check for duplicate sequence numbers
            for (int i=0; i<attributeGroups.size(); i++) 
            {
                AttributeGroup ag1 = (AttributeGroup)attributeGroups.get(i);
                Group agGroup1 = intake.get("AttributeGroup", 
                                 ag1.getQueryKey(), false);
                order1 = agGroup1.get("Order");
                if (order1.toString().equals(Integer.toString(dupeOrder)))
                {
                    areThereDupes = true;
                    break;
                }

                for (int j=i-1; j>=0; j--) 
                {
                    AttributeGroup ag2 = (AttributeGroup)attributeGroups.get(j);
                    Group agGroup2 = intake.get("AttributeGroup", 
                                 ag2.getQueryKey(), false);
                    order2 = agGroup2.get("Order");

                    if (order1.toString().equals(order2.toString()))
                    {
                        areThereDupes = true;
                        break;
                    }
                }
            }
            if (areThereDupes)
            {
               data.setMessage("Please do not enter duplicate "
                                + " sequence numbers for attribute groups.");
               isValid = false;
            }
  
            // Check that duplicate check is not at the beginning or end.
            if (dupeOrder == 1 || dupeOrder == attributeGroups.size() +1)
            {
                data.setMessage("The duplicate check cannot be at the "
                                + "beginning or the end.");
                isValid = false;
            }
        }
        if (intake.isAllValid() && isValid) 
        {
            // Set properties for module-issue type info
            Group rmitGroup = intake.get("RModuleIssueType", 
                                        rmit.getQueryKey(), false);

            rmitGroup.setProperties(rmit);
            rmit.save();
           
            // Set properties for attribute groups
            for (int i=attributeGroups.size()-1; i>=0; i--) 
            {
                AttributeGroup attGroup = (AttributeGroup)attGroups.get(i);
                Group agGroup = intake.get("AttributeGroup", 
                                 attGroup.getQueryKey(), false);
                agGroup.setProperties(attGroup);

                // If an attribute group falls before the dedupe screen,
                // Mark it as a dedupe group
                if (Integer.parseInt(agGroup.get("Order").toString()) 
                                     < dupeOrder)
                {
                    if (!attGroup.getAttributes().isEmpty())
                    {
                         areThereDedupeAttrs = true;
                         attGroup.setDedupe(true);
                    }
                }
                else
                {
                    attGroup.setDedupe(false);
                }
                attGroup.save();
            }

            // Set dedupe property for module
            Group modGroup = intake.get("Module", module.getQueryKey(), false);
            modGroup.setProperties(module);
            if (!areThereDedupeAttrs)
            {
                module.setDedupe(false);
            }
            module.save();
        }
    }

    /**
     * Adds or modifies user attributes' properties
     */
    public void doSaveuserattributes ( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ModuleEntity module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();

        if (intake.isAllValid())
        {
            List userAttributes = module.getUserAttributes(issueType, false);
            for (int i=0; i < userAttributes.size(); i++)
            {
                // Set properties for module-attribute mapping
                Attribute attribute = (Attribute)userAttributes.get(i);
                RModuleAttribute rma = (RModuleAttribute)module
                        .getRModuleAttribute(attribute, issueType);
                Group rmaGroup = intake.get("RModuleAttribute", 
                                 rma.getQueryKey(), false);
                rmaGroup.setProperties(rma);
                rma.save();
            }
        }
    }

    /**
     * Creates new attribute group.
     */
    public AttributeGroup doCreatenewgroup ( RunData data, 
                                             TemplateContext context )
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ModuleEntity module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();
        return issueType.createNewGroup(module);
    }

    /**
     * Deletes an attribute group.
     */
    public void doDeletegroup ( RunData data, TemplateContext context )
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String groupId;
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ModuleEntity module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();
        List attributeGroups = issueType.getAttributeGroups(module);

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("delete_group_"))
            {
                if (attributeGroups.size() - 1 < 2)
                {
                    data.setMessage("You cannot have fewer than two groups.");
                    break;
                }
                else
                {
                    try
                    {
                        groupId = key.substring(13);
                        AttributeGroup ag = (AttributeGroup) AttributeGroupPeer
                                            .retrieveByPK(new NumberKey(groupId));
                        ag.delete(user);
                    }
                    catch (Exception e)
                    {
                        data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     * Unmaps attributes to modules.
     */
    public void doDeleteuserattribute( RunData data, TemplateContext context ) 
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        ModuleEntity module = scarabR.getCurrentModule();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attributeId;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("att_delete_"))
            {
               attributeId = key.substring(11);
               Attribute attribute = (Attribute)AttributePeer
                                     .retrieveByPK(new NumberKey(attributeId));

               // Remove attribute - module mapping
               IssueType issueType = scarabR.getIssueType();
               RModuleAttribute rma = module
                   .getRModuleAttribute(attribute, issueType);
               try
               {
                   rma.delete(user);
               }
               catch (Exception e)
               {
                   data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
               }

               // Remove attribute - module mapping from template type
               RModuleAttribute rma2 = module
                   .getRModuleAttribute(attribute, 
                   scarabR.getIssueType(issueType.getTemplateId().toString()));
               try
               {
                   rma2.delete(user);
               }
               catch (Exception e)
               {
                   data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
               }
           }
        }        
    }


    public void doCreatenewuserattribute( RunData data, 
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
     * Selects attribute to add to issue type.
     */
    public void doSelectuserattribute( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ModuleEntity module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();
        IssueType templateType = 
            scarabR.getIssueType(issueType.getTemplateId().toString());
        Attribute attribute = scarabR.getAttribute();
 
        if (attribute.getAttributeId() == null)
        { 
            data.setMessage("Please select an attrubute.");
        }
        else
        {        
            // add module-attribute groupings
            RModuleAttribute rma = module.addRModuleAttribute(issueType, 
                                                              "user");
            Group rmaGroup = intake.get("RModuleAttribute", 
                                         IntakeTool.DEFAULT_KEY);
            rmaGroup.setProperties(rma);
            rma.setAttributeId(attribute.getAttributeId());
            rma.save();

            // add module-attribute mappings to template type
            RModuleAttribute rma2 = module.addRModuleAttribute(templateType);
            rma2.setAttributeId(attribute.getAttributeId());
            rma2.save();
            doCancel(data, context);
       }      

    }
}
