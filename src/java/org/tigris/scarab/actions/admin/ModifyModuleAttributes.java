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
import org.tigris.scarab.om.ScarabModule;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RAttributeAttributeGroup;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.AttributeGroup;
import org.tigris.scarab.om.AttributeGroupPeer;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypePeer;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;

/**
 * action methods on RModuleAttribute table
 *      
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ModifyModuleAttributes.java,v 1.31 2001/10/20 00:34:24 jon Exp $
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

        ScarabModule module = (ScarabModule)scarabR.getCurrentModule();
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
     * This manages clicking the Add Attribute button.
     * FIXME: this should be done with a form variable
     */
    public void doGotoartifactpage( RunData data, TemplateContext context ) 
        throws Exception
    {
        setTarget(data, "admin,ArtifactTypeEdit.vm");            
    }

    /**
     * This manages clicking the Add Issue Type button.
     * FIXME: this should be done with a form variable
     */
    public void doGotoartifactselect( RunData data, TemplateContext context ) 
        throws Exception
    {
        setTarget(data, "admin,ArtifactTypeSelect.vm");            
    }

    /**
     * This manages clicking the Add Attribute button.
     * FIXME: this should be done with a form variable
     */
    public void doGotoattributeselect( RunData data, TemplateContext context ) 
        throws Exception
    {
        setTarget(data, "admin,AttributeSelect.vm");            
    }

    /**
     * Creates default attribute groups.
     * Must create 2 groups, one for dedupe attributes, one for non-dedupe.
     */
    public void doCreatedefaults ( RunData data, TemplateContext context )
        throws Exception
    {
        AttributeGroup ag = doCreatenewgroup(data, context);
        ag.setOrder(1);
        ag.save();
        AttributeGroup ag2 = doCreatenewgroup(data, context);
        ag2.setOrder(3);
        ag2.save();

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
        AttributeGroup ag = null;
    
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabModule module = (ScarabModule)scarabR.getCurrentModule();
        String issueTypeId = data.getParameters().getString("issueTypeId");
        if (issueTypeId == null || issueTypeId.length() == 0)
        {
            data.setMessage("The artifact type id was not set");
            return ag;
        }
        IssueType issueType = null;
        try
        {
            issueType = (IssueType) IssueTypePeer
                        .retrieveByPK(new NumberKey(issueTypeId));
        }
        catch (Exception e)
        {
            data.setMessage("The artifact type id was invalid.");
            return ag;            
        }

        List groups = issueType.getAttributeGroups(module);

        ag = new AttributeGroup();
        // Make default group name 'attribute group x' where x is size + 1
        ag.setName("attribute group " + Integer.toString(groups.size()+1));
        ag.setOrder(groups.size() +2);
        ag.setModuleId(module.getModuleId());
        ag.setIssueTypeId(issueTypeId);
        ag.save();
        return ag;
    }

    /**
     * Selects issue type to add to module.
     */
    public void doSelectissuetype( RunData data, TemplateContext context )
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String issueTypeId = data.getParameters().getString("issueTypeId");
        if (issueTypeId == null || issueTypeId.length() == 0)
        {
            data.setMessage("Please select an Artifact type.");
            return;
        }
        try
        {
            IssueType issueType = (IssueType) IssueTypePeer
                                .retrieveByPK(new NumberKey(issueTypeId));
        }
        catch (Exception e)
        {
            data.setMessage("The artifact type id was invalid.");
            return;            
        }

        RModuleIssueType rmit = new RModuleIssueType();
        rmit.setModuleId(scarabR.getCurrentModule().getModuleId());
        rmit.setIssueTypeId(issueTypeId);
        rmit.setActive(false);
        rmit.setDisplay(false);
        rmit.save();

        data.setMessage("The Artifact type has been added to the module.");
        setTarget(data, "admin,ManageArtifactTypes.vm");            
    }

    /**
     * Selects attribute to add to artifact type and attribute group.
     */
    public void doSelectattribute( RunData data, TemplateContext context )
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabModule module = (ScarabModule)scarabR.getCurrentModule();

        String attributeId = data.getParameters().getString("attributeid");
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup group = null;

        // FIXME: use intake for this stuff...
        if (groupId == null || groupId.length() == 0)
        {
            data.setTarget("AttributeGroup.vm");
            data.setMessage("Could not get a valid group id.");
            return;
        }
        if (attributeId == null || attributeId.length() == 0)
        {
            data.setMessage("Please select an attribute.");
            return;
        }

        try
        {
            group = (AttributeGroup) AttributeGroupPeer
                                   .retrieveByPK(new NumberKey(groupId));
        }
        catch (Exception e)
        {
            data.setTarget("AttributeGroup.vm");
            data.setMessage("Could not get a valid group id.");
            return;
        }

        NumberKey issueTypeId = group.getIssueTypeId();
        IssueType issueType = (IssueType) IssueTypePeer
                            .retrieveByPK(new NumberKey(issueTypeId));

        RModuleAttribute rma = new RModuleAttribute();
        rma.setModuleId(scarabR.getCurrentModule().getModuleId());
        rma.setAttributeId(attributeId);
        rma.setIssueTypeId(group.getIssueTypeId());
        rma.setDedupe(group.getOrder() < issueType.getDedupeSequence(module));
        rma.save();

        RAttributeAttributeGroup raag = new RAttributeAttributeGroup();
        raag.setGroupId(groupId);
        raag.setAttributeId(attributeId);
        raag.setOrder(group.getAttributes().size() +1 );
        raag.save();
        data.getParameters().add("groupid", groupId);
        setTarget(data, "admin,AttributeGroup.vm");            
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

        ScarabModule module = (ScarabModule)scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();
        RModuleIssueType rmit = module.getRModuleIssueType(issueType);
//System.out.println(rmit);
//System.out.println(rmit.getIssueTypeId());
//System.out.println(rmit.getDisplayName());
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
//System.out.println(rmit);
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
    public void doDeletemoduleissuetype ( RunData data, TemplateContext context )
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        ParameterParser params = data.getParameters();
        ScarabModule module = (ScarabModule)scarabR.getCurrentModule();
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
                if (rmits.size() - 1 < 1)
                {
                    data.setMessage("You cannot have fewer than one artifact group.");
                    break;
                }
                else
                {
                    try
                    {
                        issueTypeId = key.substring(7);
                        IssueType issueType = (IssueType) IssueTypePeer
                            .retrieveByPK(new NumberKey(issueTypeId));
                        RModuleIssueType rmit = module
                               .getRModuleIssueType(issueType);

                        // delete attribute groups 
                        List attGroups = issueType.getAttributeGroups(); 
                        for (int j=0; j<attGroups.size(); j++)
                        {
                            // delete attribute-attribute group map
                            AttributeGroup attGroup = (AttributeGroup)attGroups.get(j);
                            List raags = attGroup.getRAttributeAttributeGroups();
                            for (int k=0; k<raags.size(); k++)
                            {
                                RAttributeAttributeGroup raag = 
                                    (RAttributeAttributeGroup)raags.get(k);
                                raag.delete(user);
                            }
                  
                            attGroup.delete(user);
                        }
                         
                        List rmas = module.getRModuleAttributes(issueType);
                        // delete module-attribute mappings
                        for (int m=0; m<rmas.size(); m++)
                        {
                            RModuleAttribute rma = (RModuleAttribute)rmas.get(0);
                            rma.delete(user);
                        }

                        // delete module-issue type mappings
                        rmit.delete(user);

                    }
                    catch (Exception e)
                    {
                        data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
                    }
                    foundOne = true;
                }
            }
        }

        if (!foundOne)
        {
            data.setMessage("Please select an Artifact Type " + 
                "to delete from the module.");
        }
        else
        {
            data.setMessage("The selected Artifact Types have " + 
                "been removed from the module.");
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

        ScarabModule module = (ScarabModule)scarabR.getCurrentModule();
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = (AttributeGroup) AttributeGroupPeer
                            .retrieveByPK(new NumberKey(groupId));
        List attributes = ag.getAttributes();

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

        ScarabModule module = (ScarabModule)scarabR.getCurrentModule();
        String issueTypeId = data.getParameters().getString("issueTypeId");
        IssueType issueType = (IssueType) IssueTypePeer
                            .retrieveByPK(new NumberKey(issueTypeId));
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
        ScarabModule module = (ScarabModule)scarabR.getCurrentModule();
        String issueTypeId = data.getParameters().getString("issueTypeId");
        IssueType issueType = (IssueType) IssueTypePeer
                            .retrieveByPK(new NumberKey(issueTypeId));
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
        String nextTemplate = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, nextTemplate);            
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
               RModuleAttribute rma = module
                   .getRModuleAttribute(attribute, ag.getIssueType());
               rma.delete(user);

               // Remove attribute - group mapping
               RAttributeAttributeGroup raag = 
                   ag.getRAttributeAttributeGroup(attribute);
               raag.delete(user);
            }
        }        
        data.getParameters().add("groupid", groupId);
        setTarget(data, "admin,AttributeGroup.vm");            
    }

    /**
     * This manages clicking the create new button on AttributeSelect.vm
     */
    public void doCreatenewglobalattribute( RunData data, TemplateContext context )
        throws Exception
    {
        data.getParameters().setString(ScarabConstants.NEXT_TEMPLATE, 
            "admin,AttributeSelect.vm");
        setTarget(data, getCancelTemplate(data, 
            "admin,GlobalAttributeEdit.vm"));
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
