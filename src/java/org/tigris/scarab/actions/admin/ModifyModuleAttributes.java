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
 * @version $Id: ModifyModuleAttributes.java,v 1.52 2001/12/10 01:01:10 elicia Exp $
 */
public class ModifyModuleAttributes extends RequireLoginFirstAction
{
    /**
     * Changes the properties of existing IssueTypes.
     */
    public synchronized void doManageissuetypes ( RunData data, 
                                                  TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        ModuleEntity module = scarabR.getCurrentModule();
        List rmits = module.getRModuleIssueTypes();
        int navCount = 0;
        Group rmitGroup = null;

        if ( intake.isAllValid() )
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
                   data.setMessage("You cannot select more than 5 to appear "
                                   +"in the left hand navigation.");
                }
            }
            for (int i=0;i < rmits.size(); i++)
            {
                RModuleIssueType rmit = (RModuleIssueType)rmits.get(i);
                rmitGroup = intake.get("RModuleIssueType", 
                                 rmit.getQueryKey(), false);
                rmitGroup.setProperties(rmit);
                rmit.save();
            }

        } 
        String nextTemplate = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, nextTemplate);            
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
        IssueType issueType = getIssueType(data);
        return issueType.createNewGroup(module);
    }

    /**
     * Selects issue type to add to module.
     */
    public void doSelectissuetype( RunData data, TemplateContext context )
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        IssueType issueType = getIssueType(data);
        ModuleEntity module = scarabR.getCurrentModule();

        if (module.getRModuleIssueType(issueType) != null)
        {
            data.setMessage("The Artifact type is already associated "
                            + "with the module.");
        }
        else
        {
            module.addRModuleIssueType(getIssueType(data));
            data.setMessage("The Artifact type has been added to the module.");
        }
        setTarget(data, "admin,ManageArtifactTypes.vm");            
    }

    /**
     * Selects attribute to add to artifact type and attribute group.
     */
    public void doSelectattribute( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ModuleEntity module = scarabR.getCurrentModule();
        IssueType issueType = getIssueType(data);
        IssueType templateType = 
            scarabR.getIssueType(issueType.getTemplateId().toString());
        Attribute attribute = scarabR.getAttribute();
 
        if (attribute.getAttributeId() == null)
        { 
            data.setMessage("Please select an attrubute.");
        }
        else
        {        
            AttributeGroup attGroup = scarabR.getAttributeGroup();

            // add module-attribute groupings
            RModuleAttribute rma = module.addRModuleAttribute(issueType, 
                                                              attGroup);
            Group rmaGroup = intake.get("RModuleAttribute", 
                                         IntakeTool.DEFAULT_KEY);
            rmaGroup.setProperties(rma);
            rma.setAttributeId(attribute.getAttributeId());
            rma.save();

            // add module-attributeoption mappings
            List options = attribute.getAttributeOptions();
            for (int i=0;i < options.size();i++)
            {
                AttributeOption option = (AttributeOption)options.get(i);
                RModuleOption rmo = module.addRModuleOption(issueType, option);
                rmo.save();

                // add module-attributeoption mappings to template type
                RModuleOption rmo2 = module.
                     addRModuleOption(templateType, option);
                rmo2.save();
            }

            // add module-attribute mappings to template type
            RModuleAttribute rma2 = module.addRModuleAttribute(templateType,
                                                               attGroup);
            rma2.setAttributeId(attribute.getAttributeId());
            rma2.save();

            // attribute group-attribute mapping
            RAttributeAttributeGroup raag =  
                attGroup.addRAttributeAttributeGroup(attribute);
            raag.save();

            data.getParameters().add("groupid", 
               attGroup.getAttributeGroupId().toString());
           setTarget(data, "admin,AttributeGroup.vm");            
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
        IssueType issueType = getIssueType(data);
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
     
            String nextTemplate = data.getParameters()
                .getString(ScarabConstants.NEXT_TEMPLATE);
            setTarget(data, nextTemplate);            
        }
    }

    /**
     * Adds or modifies an issue type's properties.
     */
    public synchronized void doModifyissuetype ( RunData data, 
                                                 TemplateContext context )
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

        if (attributeGroups.size() > 0)
        {
            int dupeOrder = Integer.parseInt(data.getParameters()
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
                attGroup.save();
            }
        }
        data.getParameters().add("issueTypeId", 
                                 issueType.getIssueTypeId().toString());

        String nextTemplate = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, nextTemplate);            
    }

    /**
     * Deletes an issue type from a module.
     */
    public void doDeletemoduleissuetype ( RunData data, 
                                          TemplateContext context )
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        ParameterParser params = data.getParameters();
        ModuleEntity module = scarabR.getCurrentModule();
        Object[] keys = params.getKeys();
        String key;
        String issueTypeId;
        List rmits = module.getRModuleIssueTypes();

        boolean foundOne = false;
        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("delete_"))
            {
                if (rmits.size() < 1)
                {
                    data.setMessage("You cannot have fewer than one "
                                    + "artifact type.");
                    break;
                }
                else
                {
                    try
                    {
                        issueTypeId = key.substring(7);
                        IssueType issueType = scarabR.getIssueType(issueTypeId);
                        if (issueType != null)
                        {
                            foundOne = true;
                            // delete module-issue type mappings
                            RModuleIssueType rmit = module
                               .getRModuleIssueType(issueType);
                            rmit.delete(user);

                            data.setMessage("The selected Artifact Types have"
                                            + " been removed from the module.");
                        }
                    }
                    catch (Exception e)
                    {
                        data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
                    }
                }
            }
        }

        if (!foundOne)
        {
            data.setMessage("Please select an Artifact Type " + 
                "to delete from the module.");
        }
        String nextTemplate = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, nextTemplate);            
    }

    /**
     * Changes the properties of existing AttributeGroups and their attributes.
     */
    public synchronized void doModifygroup ( RunData data, 
                                             TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);

        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = (AttributeGroup) AttributeGroupPeer
                            .retrieveByPK(new NumberKey(groupId));
        List attributes = ag.getAttributes();

        if ( intake.isAllValid() )
        {
            ModuleEntity module = scarabR.getCurrentModule();
            for (int i=attributes.size()-1; i>=0; i--) 
            {
                // Set properties for module-attribute mapping
                Attribute attribute = (Attribute)attributes.get(i);
                RModuleAttribute rma = (RModuleAttribute)module
                                       .getRModuleAttribute(attribute, 
                                                            ag.getIssueType());
                Group rmaGroup = intake.get("RModuleAttribute", 
                                 rma.getQueryKey(), false);
                rmaGroup.setProperties(rma);
                rma.save();

                // Set properties for attribute-attribute group mapping
                RAttributeAttributeGroup raag = 
                    ag.getRAttributeAttributeGroup(attribute);
                Group raagGroup = intake.get("RAttributeAttributeGroup", 
                                 raag.getQueryKey(), false);
                raagGroup.setProperties(raag);
                raag.save();
            }

            // Set properties for group info
            Group agGroup = intake.get("AttributeGroup", 
                                        ag.getQueryKey(), false);
            agGroup.setProperties(ag);
            ag.save();
        } 
        String nextTemplate = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, nextTemplate);            
    }


    /**
     * Changes the properties of existing RModuleAttributes.
     */
    public synchronized void doModifyattributes ( RunData data, 
                                                  TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        IssueType issueType = getIssueType(data);
        ModuleEntity module = scarabR.getCurrentModule();
        List rmas = new ArrayList (module
                           .getRModuleAttributes(issueType, false));
        List attributeGroups = issueType.getAttributeGroups(module);

        boolean isValid = true;
        boolean areThereDupes = false;
        RModuleAttribute rma = null;
        Field order1 = null;
        Field order2 = null;
        int dupeOrder = Integer.parseInt(data.getParameters()
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
           data.setMessage("The duplicate check cannot be at the beginning "
                             + "or the end.");
           isValid = false;
       }

       if ( intake.isAllValid() && isValid) 
       {
           for (int i=attributeGroups.size()-1; i>=0; i--) 
           {
               AttributeGroup ag = (AttributeGroup)attributeGroups.get(i);
               Group agGroup = intake.get("AttributeGroup",
                                        ag.getQueryKey(), false);
               agGroup.setProperties(ag);
               ag.save();

               Group moduleGroup = intake.get("Module",
                                   module.getQueryKey(), false);
               moduleGroup.setProperties(module);
               module.save();
           }
       }

        String nextTemplate = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, nextTemplate);            
    }


    /**
     * Changes the properties of existing AttributeOptions.
     */
    public synchronized void doModifyattributeoptions ( RunData data, 
                                                        TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Attribute attribute = getAttribute(data);

        if ( intake.isAllValid())
        {
            ModuleEntity me = scarabR.getCurrentModule();
            IssueType issueType = getIssueType(data);
            List rmos = me.getRModuleOptions(attribute, issueType);
            for (int i=rmos.size()-1; i>=0; i--) 
            {
                RModuleOption rmo = (RModuleOption)rmos.get(i);
                Group rmoGroup = intake.get("RModuleOption", 
                                 rmo.getQueryKey(), false);
                rmoGroup.setProperties(rmo);
                rmo.save();
            }
            String nextTemplate = data.getParameters()
                .getString(ScarabConstants.NEXT_TEMPLATE);
            setTarget(data, nextTemplate);            
        } 
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
        IssueType issueType = getIssueType(data);
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
    public void doDeleteattributes( RunData data, TemplateContext context ) 
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        ModuleEntity module = scarabR.getCurrentModule();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attributeId;
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = (AttributeGroup) AttributeGroupPeer
                                         .retrieveByPK(new NumberKey(groupId));

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("att_delete_"))
            {
               attributeId = key.substring(11);
               Attribute attribute = (Attribute)AttributePeer
                                     .retrieveByPK(new NumberKey(attributeId));

               // Remove attribute - module mapping
               IssueType issueType = ag.getIssueType();
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

               // Remove attribute - group mapping
               RAttributeAttributeGroup raag = 
                   ag.getRAttributeAttributeGroup(attribute);
               
               try
               {
                   raag.delete(user);
               }
               catch (Exception e)
               {
                  data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
               }

               if (attribute.isOptionAttribute())
               {
                   // Remove module-option mapping
                   List rmos = module.getRModuleOptions(attribute, issueType);
                   rmos.addAll(module.getRModuleOptions(attribute, 
                         scarabR.getIssueType(issueType.getTemplateId()
                                                       .toString())));
                   for (int j = 0; j<rmos.size();j++)
                   {
                       RModuleOption rmo = (RModuleOption)rmos.get(j);
                       try
                       {
                          rmo.delete(user);
                       }
                       catch (Exception e)
                       {
                          data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
                       }
                   }
                }
            }
        }        
        data.getParameters().add("groupid", groupId);
        setTarget(data, "admin,AttributeGroup.vm");            
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
        String nextTemplate = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, nextTemplate);            
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
        String nextTemplate = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, getNextTemplate(data, 
            "admin,GlobalAttributeShow.vm"));
    }

    private IssueType getIssueType( RunData data )
        throws Exception
    {
        String issueTypeId = data.getParameters().getString("issueTypeId");
        IssueType issueType = null;
        if (issueTypeId == null || issueTypeId.length() == 0)
        {
            data.setMessage("The artifact type is missing.");
        } 
        else
        {
            try
            {
                issueType = (IssueType) IssueTypePeer
                            .retrieveByPK(new NumberKey(issueTypeId));
            }
            catch (Exception e)
            {
                data.setMessage("The artifact type id was invalid.");
            }
        }
        return issueType;
   }
        
    
    private Attribute getAttribute( RunData data )
        throws Exception
    {
        Attribute attribute = null;
        String attributeId = data.getParameters().getString("attributeid");
        if (attributeId == null || attributeId.length() == 0)
        {
            data.setMessage("Attribute id is missing.");
        } 
        else
        {
            attribute = Attribute.getInstance(new NumberKey(attributeId));
        } 
        return attribute;
    }

    /**
     *   This manages clicking the cancel button
     */
    public void doCancel( RunData data, TemplateContext context )
        throws Exception
    {
        data.setMessage("Changes were not saved!");
        setTarget(data, getCancelTemplate(data, 
            "admin,ManageArtifactTypes.vm"));
    }

    /**
     *   This manages clicking the cancel button
     */
    public void doCreateartifacttype( RunData data, TemplateContext context )
        throws Exception
    {
        data.getParameters().remove("issueTypeId");
        setTarget(data, "admin,ArtifactTypeCreate.vm");
    }

    /**
     *   This manages clicking the back button
     */
    public void doBack( RunData data, TemplateContext context )
        throws Exception
    {
        setTarget(data, getBackTemplate(data, 
            "admin,ManageArtifactTypes.vm"));
    }
    
    /**
     *   does nothing.
     */
    public void doPerform( RunData data, TemplateContext context )
        throws Exception
    {
        doCancel(data, context);
    }
}
