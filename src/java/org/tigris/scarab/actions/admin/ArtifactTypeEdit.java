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
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.AttributeGroup;
import org.tigris.scarab.om.AttributeGroupManager;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.workflow.WorkflowFactory;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * action methods on RModuleAttribute table
 *      
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: ArtifactTypeEdit.java,v 1.46 2003/03/25 16:57:52 jmcnally Exp $
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
        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            return false;
        }
        IntakeTool intake = getIntakeTool(context);
        Module module = scarabR.getCurrentModule();
        RModuleIssueType rmit = module.getRModuleIssueType(issueType);
        // Set properties for module-issue type info
        Group rmitGroup = intake.get("RModuleIssueType", 
                                        rmit.getQueryKey(), false);
        if (intake.isAllValid())
        {
            boolean nameTaken = false;
            List issueTypes = module.getRModuleIssueTypes();
            if (issueTypes != null)
            {
                String displayName = rmitGroup.get("DisplayName").toString();
                for (int i=0;i<issueTypes.size();i++)
                {
                    RModuleIssueType tmpRmit = ((RModuleIssueType)issueTypes.get(i));
                    if (tmpRmit.getDisplayName().equals(displayName) 
                        && !tmpRmit.getIssueTypeId().equals(issueType.getIssueTypeId()))
                    {
                        nameTaken = true;
                        break;
                    }
                }
            }
         
            if (nameTaken) 
            {
                scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
                rmitGroup.get("DisplayName").setMessage("IssueTypeNameExists");
                return false;
            }
            else
            {
                rmitGroup.setProperties(rmit);
                rmit.save();
                scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
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
        boolean success = true;
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        String msg = DEFAULT_MSG;

        IssueType issueType = scarabR.getIssueType();
        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            success = false;
        }

        Module module = scarabR.getCurrentModule();
        RModuleIssueType rmit = module.getRModuleIssueType(issueType);

        List attGroups = module.getAttributeGroups(issueType, false);

        boolean isValid = true;
        boolean areThereDupes = false;
        Field order1 = null;
        Field order2 = null;
        int dupeOrder = 2;
        boolean areThereDedupeAttrs = false;

        // Manage attribute groups
        // Only have dedupe if there are more than one active group
        if (module.getAttributeGroups(issueType, true).size() > 1)
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
               msg = "DuplicateSequenceNumbersForAttributeGroups";
               isValid = false;
            }
  
            // Check that duplicate check is not at the beginning or end.
            if (dupeOrder == 1 || dupeOrder == attGroups.size() +1)
            {
                msg = "DuplicateCheckCannotBeginOrEnd";
                isValid = false;
            }
        }
        if (isValid) 
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
                || module.getAttributeGroups(issueType).size() < 2)
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
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(msg));
            success = false;
        }
        return success;
    }

    /**
     * Adds or modifies user attributes' properties
     */
    public void doSaveuserattributes (RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();

        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            return;
        }

        Module module = scarabR.getCurrentModule();
        List userAttributes = module.getUserAttributes(issueType, false);
        for (int i=0; i < userAttributes.size(); i++)
        {
            // Set properties for module-attribute mapping
            Attribute attribute = (Attribute)userAttributes.get(i);
            RModuleAttribute rma = module
                    .getRModuleAttribute(attribute, issueType);
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
            rma.save();
        }
        scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
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
        scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
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
        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            return;
        }

        ScarabUser user = (ScarabUser)data.getUser();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String groupId;
        Module module = scarabR.getCurrentModule();
        List attributeGroups = module.getAttributeGroups(issueType, false);

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
                    ag.delete(user, module);
                    scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
                    ScarabCache.clear();
                    getIntakeTool(context).removeAll();
                }
                catch (Exception e)
                {
                    scarabR.setAlertMessage(
                        l10n.get(NO_PERMISSION_MESSAGE));
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
        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            return;
        }
        ScarabUser user = (ScarabUser)data.getUser();
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
               attributeId = key.substring(11);
               Attribute attribute = AttributeManager
                   .getInstance(new NumberKey(attributeId), false);

               // Remove attribute - module mapping
               RModuleAttribute rma = module
                   .getRModuleAttribute(attribute, issueType);
               try
               {
                   rma.delete(user);
               }
               catch (Exception e)
               {
                   scarabR.setAlertMessage(
                       l10n.get(NO_PERMISSION_MESSAGE));
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
                   scarabR.setAlertMessage(
                       l10n.get(NO_PERMISSION_MESSAGE));
               }
               scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
               ScarabCache.clear();
           }
        }        
    }


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
     * Selects attribute to add to issue type.
     */
    public void doSelectuserattribute(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IssueType issueType = scarabR.getIssueType();
        if (issueType.getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            return;
        }

        Module module = scarabR.getCurrentModule();
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
                    // add module-attribute groupings
                    RModuleAttribute rma = module.addRModuleAttribute(issueType,
                                                                  attribute);
                }
                doCancel(data, context);
            }
        }
    }

    /*
     * Manages clicking of the AllDone button
     */
    public void doDone(RunData data, TemplateContext context)
        throws Exception
    {
        boolean groupSuccess = false;
        boolean infoSuccess = doSaveinfo(data, context);
        if (infoSuccess)
        {
            groupSuccess = doSavegroups(data, context);
            if (groupSuccess)
            {
                doSaveuserattributes(data, context);
            }
        }
        if (infoSuccess && groupSuccess)
        {
            doCancel(data, context);
        }
    }
}
