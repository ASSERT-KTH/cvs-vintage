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

import java.util.Iterator;
import java.util.List;

import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.intake.model.Group;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.ParameterParser;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.tool.IntakeTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeGroup;
import org.tigris.scarab.om.AttributeGroupManager;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.workflow.WorkflowFactory;

/**
 * action methods on RModuleAttribute table
 *      
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: ArtifactTypeEdit.java,v 1.62 2004/10/16 12:31:40 dep4b Exp $
 */
public class ArtifactTypeEdit extends RequireLoginFirstAction
{
    /**
     * Adds or modifies an issue type's properties.
     */
    public boolean doSaveinfo (RunData data, TemplateContext context)
        throws Exception
    {
        boolean success = true;
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return false;
        }
        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(L10NKeySet.LockedIssueType);
            return false;
        }
        IntakeTool intake = getIntakeTool(context);
        Module module = scarabR.getCurrentModule();
        RModuleIssueType rmit = module.getRModuleIssueType(issueType);
        if (rmit == null)
        {
            scarabR.setAlertMessage(L10NKeySet.IssueTypeRemovedFromModule);
            doCancel(data, context);
            return false;
        }
        // Set properties for module-issue type info
        Group rmitGroup = intake.get("RModuleIssueType",
                                        rmit.getQueryKey(), false);
        if (intake.isAllValid())
        {
            boolean nameTaken = false;
            List issueTypes = module.getRModuleIssueTypes();
            if (issueTypes != null)
            {
                Field displayName = rmitGroup.get("DisplayName");
                if (displayName.toString().trim().length() == 0)
                {
                    displayName.setMessage("intake_IssueTypeNameNotAllowedEmpty");
                    scarabR.setAlertMessage(ERROR_MESSAGE);
                    return false;
                }
                for (int i=0;i<issueTypes.size();i++)
                {
                    RModuleIssueType tmpRmit = ((RModuleIssueType)issueTypes.get(i));
                    if (tmpRmit.getDisplayName().equals(displayName.toString()) 
                        && !tmpRmit.getIssueTypeId().equals(issueType.getIssueTypeId()))
                    {
                        nameTaken = true;
                        break;
                    }
                }
            }
         
            if (nameTaken) 
            {
                scarabR.setAlertMessage(ERROR_MESSAGE);
                rmitGroup.get("DisplayName").setMessage("IssueTypeNameExists");
                return false;
            }
            else
            {
                rmitGroup.setProperties(rmit);
                rmit.save();
                scarabR.setConfirmMessage(DEFAULT_MSG);
            }
        }
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
            return false;
        }
        return success;
    }

    /**
     * Adds or modifies an issue type's attribute groups.
     */
    public boolean doSavegroups (RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);

        IssueType issueType = scarabR.getIssueType();
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return false;
        }
        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(L10NKeySet.LockedIssueType);
            return false;
        }

        Module module = scarabR.getCurrentModule();
        RModuleIssueType rmit = module.getRModuleIssueType(issueType);
        if (rmit == null)
        {
            scarabR.setAlertMessage(L10NKeySet.IssueTypeRemovedFromModule);
            doCancel(data, context);
            return false;
        }
        List attGroups = issueType.getAttributeGroups(module, false);

        int dupeOrder = 2;
        boolean areThereDedupeAttrs = false;

        // Manage attribute groups
        // Only have dedupe if there are more than one active group
        if (issueType.getAttributeGroups(module, true).size() > 1)
        {
            dupeOrder = data.getParameters().getInt("dupe_order");

            // Check that duplicate check is not at the beginning.
            if (dupeOrder == 1)
            {
                scarabR.setAlertMessage(L10NKeySet.CannotPositionDuplicateCheckFirst);
                return false;
            }
            // Check for duplicate sequence numbers
            if (areThereDupeSequences(attGroups, intake, "AttributeGroup",
                   "Order", dupeOrder))
            {
               scarabR.setAlertMessage(l10n.format("DuplicateSequenceNumbersFound",
                   l10n.get("AttributeGroups").toLowerCase()));
               return false;
            }
        }

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
                areThereDedupeAttrs = true;
                attGroup.setDedupe(true);
                List dedupeGroups = module.
                         getDedupeGroupsWithAttributes(issueType);
                if (!dedupeGroups.contains(attGroup))
                {
                    dedupeGroups.add(attGroup);
                }
            }
            else
            {
                attGroup.setDedupe(false);
            }
            attGroup.save();
        }

            // Set dedupe property for module-issueType
        if (!areThereDedupeAttrs
                || issueType.getAttributeGroups(module, true).size() < 2)
        {
            rmit.setDedupe(false);
        }
        else
        {
            Group rmitGroup = intake.get("RModuleIssueType",
                                        rmit.getQueryKey(), false);
            Field dedupe = rmitGroup.get("Dedupe");
            dedupe.setProperty(rmit);
        }
        rmit.save();
        ScarabCache.clear();
        scarabR.setConfirmMessage(DEFAULT_MSG);

        return true;
    }

    /**
     * Adds or modifies user attributes' properties
     */
    public boolean doSaveuserattributes (RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        boolean success = true;

        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            success = false;
        }
        else if (issueType.getLocked())
        {
            scarabR.setAlertMessage(L10NKeySet.LockedIssueType);
            success = false;
        }
        else
        {
            Module module = scarabR.getCurrentModule();
            List rmas = module.getRModuleAttributes(issueType, false, "user");
            if (areThereDupeSequences(rmas, intake, "RModuleAttribute",
                                                             "Order", 0))
            {
                scarabR.setAlertMessage(
                    l10n.format("DuplicateSequenceNumbersFound",
                    l10n.get("UserAttributes").toLowerCase()));
                success = false;
            }
            else
            {
                for (Iterator itr = rmas.iterator(); itr.hasNext(); )
                {
                    // Set properties for module-attribute mapping
                    RModuleAttribute rma = (RModuleAttribute)itr.next();
                    Group rmaGroup = intake.get("RModuleAttribute",
                             rma.getQueryKey(), false);
                    // if attribute gets set to inactive, delete dependencies
                    boolean newActive = Boolean.valueOf(rmaGroup.get("Active").
                                                    toString()).booleanValue();
                    boolean oldActive = rma.getActive();
                    if (!newActive && oldActive)
                    {
                        WorkflowFactory.getInstance().
                            deleteWorkflowsForAttribute(
                                      rma.getAttribute(), module, issueType);
                    }
                    rmaGroup.setProperties(rma);
                    rma.save();
                }
                scarabR.setConfirmMessage(DEFAULT_MSG);
            }

        }
        return success;
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
        Module module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return null;
        }
        scarabR.setConfirmMessage(DEFAULT_MSG);
        return issueType.createNewGroup(module);
    }

    /**
     * Deletes an attribute group.
     */
    public void doDeletegroup (RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        boolean noAGSelected = true;
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return;
        }
        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(L10NKeySet.LockedIssueType);
            return;
        }

        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String groupId;
        Module module = scarabR.getCurrentModule();
        List attributeGroups = issueType.getAttributeGroups(module, false);

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
                    ag.delete();
                    noAGSelected = false;
                    scarabR.setConfirmMessage(DEFAULT_MSG);  
                    ScarabCache.clear();
                    getIntakeTool(context).removeAll();
                }
                catch (Exception e)
                {
                    scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
                }
                if (attributeGroups.size() -1 < 2)
                {
                    // If there are fewer than 2 attribute groups,
                    // Turn of deduping
                    RModuleIssueType rmit =  module.getRModuleIssueType(issueType);
                    rmit.setDedupe(false);
                    rmit.save();
                }
            }
        }
        if (noAGSelected)
        {
           scarabR.setAlertMessage(L10NKeySet.NoAttributeGroupSelected);
        }
    }


    /**
     * Unmaps attributes to modules.
     */
    public void doDeleteuserattribute(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        boolean hasAttributes = false;
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return;
        }
        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(L10NKeySet.LockedIssueType);
            return;
        }
        Module module = scarabR.getCurrentModule();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attributeId;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("att_delete_"))
            {
               hasAttributes = true;
               attributeId = key.substring(11);
               Attribute attribute = AttributeManager
                   .getInstance(new NumberKey(attributeId), false);

               // Remove attribute - module mapping
               RModuleAttribute rma = module
                   .getRModuleAttribute(attribute, issueType);
               rma.delete();

               // Remove attribute - module mapping from template type
               RModuleAttribute rma2 = module
                   .getRModuleAttribute(attribute,
                   scarabR.getIssueType(issueType.getTemplateId().toString()));
               rma2.delete();
               scarabR.setConfirmMessage(DEFAULT_MSG);
               ScarabCache.clear();
           }
        }
        if(!hasAttributes)
        {
            scarabR.setAlertMessage(L10NKeySet.NoUserAttributeSelected);
        }
    }


    public void doCreatenewuserattribute(RunData data,
                                            TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        IssueType issueType = scarabR.getIssueType();
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return;
        }
        Group attGroup = intake.get("Attribute", IntakeTool.DEFAULT_KEY);
        intake.remove(attGroup);
        scarabR.setAttribute(null);
        setTarget(data, getOtherTemplate(data));
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
        if (issueType.isSystemDefined())
        {
            scarabR.setAlertMessage(L10NKeySet.SystemSpecifiedIssueType);
            return;
        }
        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(L10NKeySet.LockedIssueType);
            return;
        }

        Module module = scarabR.getCurrentModule();
        String[] attributeIds = data.getParameters()
                                    .getStrings("attribute_ids");
 
        if (attributeIds == null || attributeIds.length <= 0)
        { 
            scarabR.setAlertMessage(L10NKeySet.SelectAttribute);
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
                    // add module-attribute groupings
                    module.addRModuleAttribute(issueType,attribute);
                }
                doCancel(data, context);
            }
        }
    }

    /**
     * Manages clicking of the AllDone button
     */
    public void doDone(RunData data, TemplateContext context)
        throws Exception
    {
        boolean success = doSaveinfo(data, context) &&
                              doSavegroups(data, context) &&
                                  doSaveuserattributes(data, context);
        if (success)
        {
            doCancel(data, context);
        }
        //Reset confirm message in case some of the changes got saved
        else
        {
            getScarabRequestTool(context).setConfirmMessage(null);
        }
    }

}
