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
import java.util.Date;

import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.AttributeType;
import org.tigris.scarab.om.AttributeTypeManager;
import org.tigris.scarab.om.ROptionOption;
import org.tigris.scarab.om.ParentChildAttributeOption;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionPeer;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.services.cache.ScarabCache;  

/**
 * This class deals with modifying Global Attributes.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: GlobalAttributeEdit.java,v 1.47 2003/05/03 00:07:04 jon Exp $
 */
public class GlobalAttributeEdit extends RequireLoginFirstAction
{
    /**
     * Used on GlobalAttributeEdit.vm to modify Attribute Name/Description/Type
     * Use doSaveoptions to modify the options.
     */
    public boolean doSaveattributedata(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        boolean success = true;
        boolean confirmDelete = false;

        if (intake.isAllValid())
        {
            Attribute attr = scarabR.getAttribute();

            Group attrGroup = null;
            boolean isDupe = false;
            Field attributeName = null;
            Field description = null;
            if (attr.getAttributeId() == null)
            {
                // new attribute
                attrGroup = intake.get("Attribute", IntakeTool.DEFAULT_KEY);
                attr.setCreatedBy(((ScarabUser)data.getUser()).getUserId());
                attr.setCreatedDate(new Date());
            }
            else
            {
                attrGroup = intake.get("Attribute", attr.getQueryKey());
            }
            attributeName = attrGroup.get("Name");
            description = attrGroup.get("Description");
            isDupe = Attribute.checkForDuplicate(attributeName.toString(), attr);
         
            // Check for blank attribute names.
            if (attributeName.toString().trim().length() == 0)
            {
                attributeName.setMessage("intake_AttributeNameNotAllowedEmpty");
                scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
                success = false;
            }
            if (description.toString().trim().length() == 0)
            {
                description.setMessage("intake_AttributeDescriptionNotAllowedEmpty");
                scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
                success = false;
            }
            // Check for duplicate attribute names.
            else if (isDupe)
            {
                scarabR.setAlertMessage(
                    l10n.get("CannotCreateDuplicateAttribute"));
                success = false;
            }
            else
            {
                // if deleting attribute, and attribute is associated
                // With modules or issue types, give confirmation.
                if (!attr.getDeleted() && 
                    attrGroup.get("Deleted").toString().equals("true") &&
                    (attr.hasModuleMappings() || attr.hasIssueTypeMappings()))
                {
                    context.put("deleting", "deleting");
                    confirmDelete=true;
                    success = false;
                }
                for (int i = 0; i < attrGroup.getFieldNames().length; i++)
                {
                    String fieldName = attrGroup.getFieldNames()[i];
                    if (!fieldName.equals("Deleted") || !confirmDelete)
                    {
                        attrGroup.get(fieldName).setProperty(attr);
                    }
                }
                attr.save();
                if (success)
                {
                    scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
                }
            }
        }
        else
        {
          success = false;
          scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
        return success;
    }

    /**
     * Deletes attribute and its mappings after confirmation.
     */
    public void doDeleteattribute(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Attribute attr = scarabR.getAttribute();
        if (attr.getAttributeId() != null)
        {
            ScarabUser user = (ScarabUser)data.getUser();
            attr.deleteModuleMappings(user); 
            attr.deleteIssueTypeMappings(user); 
            attr.setDeleted(true);
            attr.save();
            scarabR.setConfirmMessage(getLocalizationTool(context).get(DEFAULT_MSG));  
            setTarget(data, "admin,GlobalAttributeShow.vm");
        }
    }

    /**
     * Used on AttributeEdit.vm to change the name of an existing
     * AttributeOption or add a new one if the name doesn't already exist.
     */
    public synchronized void 
        doSaveoptions(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
           .get(ScarabConstants.INTAKE_TOOL);
        ScarabRequestTool scarabR = (ScarabRequestTool)context
           .get(ScarabConstants.SCARAB_REQUEST_TOOL);
        ScarabLocalizationTool l10n = getLocalizationTool(context);

        if (intake.isAllValid()) 
        {
            // get the Attribute that we are working on
            Attribute attribute = scarabR.getAttribute();
            Group attGroup = intake.get("Attribute", attribute.getQueryKey());
            String attributeTypeId = attGroup.get("TypeId").toString();
            AttributeType attributeType = AttributeTypeManager
                .getInstance(new NumberKey(attributeTypeId), false);

            if (attributeType.getAttributeClass().getName()
                                                 .equals("select-one"))
            {
                boolean somethingSaved = false;
                // get the list of ParentChildAttributeOptions's 
                // used to display the page
                List pcaoList = attribute.getParentChildAttributeOptions();
                for (int i=pcaoList.size()-1; i>=0; i--) 
                {
                    ParentChildAttributeOption pcao = 
                        (ParentChildAttributeOption)pcaoList.get(i);
                
                     Group pcaoGroup = intake.get("ParentChildAttributeOption", 
                                                  pcao.getQueryKey());

                    // there could be errors here so catch and re-display
                    // the same screen again.
                    Integer currentParentId = null;
                    try
                    {
                        // store the currentParentId
                        currentParentId = pcao.getParentId();
                        // map the form data onto the objects
                        pcaoGroup.setProperties(pcao);
 
                        // If deleting, delete mappings with module 
                        if (pcao.getDeleted())
                        {
                            AttributeOption option = AttributeOptionPeer
                                .retrieveByPK(pcao.getOptionId());
                            ScarabUser user = (ScarabUser)data.getUser();
                            option.deleteModuleMappings(user);
                            option.deleteIssueTypeMappings(user);
                        }

                        // the UI prevents this from being true, but check
                        // anyway just in case.
                        if (pcao.getOptionId().equals(pcao.getParentId()))
                        {
                            scarabR.setAlertMessage(
                                l10n.get("RecursiveParentChildRelationship"));
                            intake.remove(pcaoGroup);
                            return;
                         }
                    
                        // save the PCAO now..
                        pcao.save();

                        // if we are changing the parent id's, then we want
                        // to remove the old one after the new one is created
                        if (!pcao.getParentId().equals(currentParentId))
                        {
                            ROptionOption.doRemove(currentParentId, 
                                                   pcao.getOptionId());
                        }

                       // also remove the group because we are re-displaying
                       // the form data and we want it fresh
                       intake.remove(pcaoGroup);
                       
                       somethingSaved = true;
                    }
                    catch (Exception se)
                    {
                        // on error, reset to previous values
                        intake.remove(pcaoGroup);
                        scarabR.setAlertMessage(se.getMessage());
                        log().error(se);
                        return;
                    }
                }

                if (somethingSaved)
                {
                    scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
                }

                // handle adding the new line.
                ParentChildAttributeOption newPCAO = 
                    ParentChildAttributeOption.getInstance();
                Group newPCAOGroup = intake.get("ParentChildAttributeOption", 
                                                newPCAO.getQueryKey());
                if (newPCAOGroup != null) 
                {
                    try
                    {
                        // assign the form data to the object
                        newPCAOGroup.setProperties(newPCAO);
                        // only add a new entry if there is a name defined
                        if (newPCAO.getName() != null && newPCAO.getName().length() > 0)
                        {
                            // save the new PCAO
                            newPCAO.setAttributeId(attribute.getAttributeId());
                            try
                            {
                                newPCAO.save();
                                pcaoList.add(newPCAO);
                                IssueType issueType = null;
                                AttributeOption option = null;

                                // If user came from editing a module,
                                // Add new option to module.
                                String cancelTemplate = getCancelTemplate(data);
                                if (cancelTemplate != null
                                    && (cancelTemplate.equals("admin,AttributeOptionSelect.vm")
                                        || cancelTemplate.equals("admin,GlobalAttributeOptionSelect.vm")))
                                {
                                    issueType = scarabR.getIssueType();
                                    option = scarabR.getAttributeOption(newPCAO.getOptionId());
                                }
                                // add new option to current module
                                if (cancelTemplate.equals("admin,AttributeOptionSelect.vm"))
                                {
                                    scarabR.getCurrentModule()
                                       .addAttributeOption(issueType, option);
                                    data.getParameters().setString(
                                         ScarabConstants.CANCEL_TEMPLATE, 
                                         "admin,ModuleAttributeEdit.vm");
                                }
                                // add new option to current issue type
                                else if (cancelTemplate.equals("admin,GlobalAttributeOptionSelect.vm"))
                                {
                                    issueType.addRIssueTypeOption(option);
                                    data.getParameters().setString(
                                         ScarabConstants.CANCEL_TEMPLATE, 
                                         "admin,IssueTypeAttributeEdit.vm");
                                }
                                scarabR.setConfirmMessage(
                                    l10n.get("AttributeOptionAdded") + 
                                    l10n.get(DEFAULT_MSG));
                            }
                            catch (Exception e)
                            {
                                log().error(e);
                                scarabR.setAlertMessage(e.getMessage());
                            }
                        }
                    }
                    catch (Exception se)
                    {
                        intake.remove(newPCAOGroup);
                        scarabR.setAlertMessage(se.getMessage());
                        log().error(se);
                        return;
                    }

                    // now remove the group to set the page stuff to null
                    intake.remove(newPCAOGroup);
                    attribute.buildOptionsMap();
                    ScarabCache.clear();
                }
            }
        }
    }

    /*
     * Manages clicking of the AllDone button
     */
    public void doDone(RunData data, TemplateContext context)
        throws Exception
    {
        boolean success = doSaveattributedata(data, context);
        if (getScarabRequestTool(context).getAttribute().isOptionAttribute())
        {
            doSaveoptions(data, context);
        }
        if (success)
        {
            doCancel(data, context);
        }
    }
    
    /*
     * Manages clicking of the cancel button
     */
    public void doCancel(RunData data, TemplateContext context)
        throws Exception
    {
        // If they came from the manage module page,
        // Add the attribute and return there.
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        String lastTemplate = getCancelTemplate(data);
        Attribute attribute = scarabR.getAttribute();
        if (lastTemplate != null && attribute.getAttributeId() != null)
        { 
            // Add attribute to group
            if (lastTemplate.equals("admin,AttributeGroupEdit.vm") ||
                lastTemplate.equals("admin,GlobalAttributeGroupEdit.vm"))
            {
                // Add attribute to group
                String groupId = data.getParameters().getString("groupId");
                if (groupId != null)
                {
                    scarabR.getAttributeGroup(groupId).addAttribute(attribute);
                    scarabR.setConfirmMessage(l10n.get("AttributeAdded"));
                }
            }
            else if (lastTemplate.equals("admin,ArtifactTypeEdit.vm"))
            {
                // Add user attribute to module
                scarabR.getCurrentModule()
                       .addRModuleAttribute(scarabR.getIssueType(), attribute);
            }
            else if (lastTemplate.equals("admin,GlobalArtifactTypeEdit.vm"))
            {
                // Add user attribute to issue type
                scarabR.getIssueType().addRIssueTypeAttribute(attribute);
                scarabR.setConfirmMessage(l10n.get("AttributeAdded"));
            }
            ScarabCache.clear();
            setTarget(data, lastTemplate);
        }
        else
        {
            super.doCancel(data, context);
        }
    }

}
