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

import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.ParameterParser;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypePeer;
import org.tigris.scarab.om.AttributeGroup;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.RIssueTypeAttribute;
import org.tigris.scarab.om.AttributeGroupManager;
import org.tigris.scarab.om.ScarabUser;

/**
 * This class deals with modifying Global Artifact Types.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: GlobalArtifactTypeCreate.java,v 1.34 2003/03/28 00:12:30 jon Exp $
 */
public class GlobalArtifactTypeCreate extends RequireLoginFirstAction
{

    /**
     * creates or edits global artifact type
     */
    public boolean doSaveinfo(RunData data, TemplateContext context)
        throws Exception
    {
        boolean success = true;
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = getScarabRequestTool(context).getIssueType();
        Group group = intake.get("IssueType", issueType.getQueryKey());
        Field field = group.get("Name");
        String name = field.toString();
        Integer id = issueType.getIssueTypeId();

        if (intake.isAllValid()) 
        {
            if (id == null)
            {
                // Create new issue type
                // make sure name is unique
                if (IssueTypePeer.isUnique(name, null)) 
                {
                    group.setProperties(issueType);
                    issueType.setParentId(IssueTypePeer.getRootKey());
                    issueType.save();
                    
                    // Create default attribute groups
                    issueType.createDefaultGroups();

                    // Create template type.
                    IssueType template = new IssueType();
                    template.setName(issueType.getName() + " Template");
                    template.setParentId(issueType.getIssueTypeId());
                    template.save();
                }
                else 
                {
                    scarabR.setAlertMessage(l10n.get("IssueTypeNameExists"));
                }
            }
            else
            {
                // Edit existing issue type
                if (IssueTypePeer.isUnique(name, id)) 
                {
                    // Cannot delete an issue type that has issues 
                    Field deleted = group.get("Deleted");
                    if (deleted != null && deleted.toString().equals("true") 
                        && issueType.hasIssues())
                    {
                        scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
                        deleted.setMessage("IssueTypeHasIssues");
                        success = false;
                    }
                    else
                    {
                        group.setProperties(issueType);
                        issueType.save();
                        scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
                    }
                }
                else 
                {
                    success = false;
                    scarabR.setAlertMessage(l10n.get("IssueTypeNameExists"));
                }
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
            success = false;
        }
        return success;
    }


    /**
     * Adds or modifies an issue type's attribute groups.
     */
    public boolean doSavegroups (RunData data, TemplateContext context)
        throws Exception
    {
        boolean success = true;
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        List attGroups = issueType.getAttributeGroups(false);
        String errorMsg = ERROR_MESSAGE;
        boolean isValid = true;
        boolean areThereDupes = false;
        Field order1 = null;
        Field order2 = null;
        int dupeOrder = 0;
        // Manage attribute groups
        // Only have dedupe if there are more than one active group
        if (issueType.getAttributeGroups(true).size() > 1)
        {
            dupeOrder = data.getParameters().getInt("dupe_order");

            // Check for duplicate sequence numbers
            for (int i=0; i<attGroups.size(); i++) 
            {
                AttributeGroup ag1 = (AttributeGroup)attGroups.get(i);
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
                    AttributeGroup ag2 = (AttributeGroup)attGroups.get(j);
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
               errorMsg= "DuplicateSequenceNumbersForAttributeGroups";
               isValid = false;
            }
  
            // Check that duplicate check is not at the beginning or end.
            if (dupeOrder == 1 || dupeOrder == attGroups.size() +1)
            {
                errorMsg = "DuplicateCheckCannotBeginOrEnd";
                isValid = false;
            }
        }

        if (intake.isAllValid() && isValid)
        {
            // Set properties for attribute groups
            for (int i=attGroups.size()-1; i>=0; i--) 
            {
                AttributeGroup attGroup = (AttributeGroup)attGroups.get(i);
                Group agGroup = intake.get("AttributeGroup", 
                                 attGroup.getQueryKey(), false);
                agGroup.setProperties(attGroup);

                // If an attribute group falls before the dedupe screen,
                // Mark it as a dedupe group
                if (attGroup.getOrder() < dupeOrder)
                {
                    if (!attGroup.getAttributes().isEmpty())
                    {
                         attGroup.setDedupe(true);
                    }
                }
                else
                {
                    attGroup.setDedupe(false);
                }
                attGroup.save();
                scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
                ScarabCache.clear();
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(errorMsg));
            success = false;
        }
        return success;
    }

    /**
     * Redirects to create new user attribute screen.
     */
    public void doCreatenewuserattribute(RunData data, 
                                          TemplateContext context)
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
     * Creates new attribute group.
     */
    public AttributeGroup doCreatenewgroup (RunData data, 
                                             TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
        return issueType.createNewGroup();
    }

    /**
     * Deletes an attribute group.
     */
    public void doDeletegroup (RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String groupId;
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        List attributeGroups = issueType.getAttributeGroups(false);

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("group_action"))
            {
                try
                {
                    groupId = key.substring(13);
                    AttributeGroup ag = AttributeGroupManager
                       .getInstance(new NumberKey(groupId), false); 
                    ag.delete(user, scarabR.getCurrentModule());
                }
                catch (Exception e)
                {
                    scarabR.setAlertMessage(
                        l10n.get(NO_PERMISSION_MESSAGE));
                }
                if (attributeGroups.size() -1 < 2)
                {
                    // If there are fewer than 2 attribute groups,
                    // Turn off deduping
                    issueType.setDedupe(false);
                    issueType.save();
                    scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
                    ScarabCache.clear();
                }
            }
        }
    }

    /**
     * Selects attribute to add to issue type.
     */
    public void doSelectuserattribute(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        String[] attributeIds = data.getParameters()
                                    .getStrings("attribute_ids");
 
        if (attributeIds == null || attributeIds.length <= 0)
        { 
            scarabR.setAlertMessage(l10n.get("SelectAttribute"));
            return;
        }
        else
        {        
            for (int i=0; i < attributeIds.length; i++)
            {
                Attribute attribute = 
                    scarabR.getAttribute(new Integer(attributeIds[i]));
                if (attribute != null)
                {
                    // add issuetype-attribute groupings
                    RIssueTypeAttribute ria = issueType.addRIssueTypeAttribute(attribute);
                }
                doCancel(data, context);
                ScarabCache.clear();
            }
        }
    }

    /**
     * Unmaps attributes to issue types.
     */
    public void doDeleteuserattribute(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        ParameterParser params = data.getParameters();
        IssueType issueType = scarabR.getIssueType();
        Object[] keys = params.getKeys();
        String key;
        String attributeId;
        boolean atLeastOne = false;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("att_delete_"))
            {
               atLeastOne = true;
               attributeId = key.substring(11);
               Attribute attribute = AttributeManager
                   .getInstance(new NumberKey(attributeId), false);

               // Remove attribute - issue type mapping
               RIssueTypeAttribute ria = issueType
                   .getRIssueTypeAttribute(attribute);
               try
               {
                   ria.delete(user);
               }
               catch (Exception e)
               {
                   scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
               }

               scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
               ScarabCache.clear();
           }
        }        
        if (!atLeastOne)
        {
           scarabR.setAlertMessage(l10n.get("NoAttributesSelected"));
        }
    }

    /**
     * Adds or modifies user attributes' properties
     */
    public void doSaveuserattributes (RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        IssueType issueType =  getScarabRequestTool(context).getIssueType();
        ScarabLocalizationTool l10n = getLocalizationTool(context);

        if (intake.isAllValid())
        {
            List userAttributes = issueType.getUserAttributes(false);
            for (int i=0; i < userAttributes.size(); i++)
            {
                // Set properties for issue type-attribute mapping
                Attribute attribute = (Attribute)userAttributes.get(i);
                RIssueTypeAttribute ria = issueType
                        .getRIssueTypeAttribute(attribute);
                Group riaGroup = intake.get("RIssueTypeAttribute", 
                                 ria.getQueryKey(), false);
                riaGroup.setProperties(ria);
                ria.save();
            }
            getScarabRequestTool(context)
                .setConfirmMessage(l10n.get(DEFAULT_MSG));  
        }
    }

    /*
     * Manages clicking of the AllDone button
     */
    public void doDone(RunData data, TemplateContext context)
        throws Exception
    {
        boolean infoSuccess = doSaveinfo(data, context);
        boolean groupSuccess = true;
        if (infoSuccess)
        {
            groupSuccess = doSavegroups(data, context);
            doSaveuserattributes(data, context);
            if (groupSuccess)
            {
                doCancel(data, context);
            }
        }
    }
}
