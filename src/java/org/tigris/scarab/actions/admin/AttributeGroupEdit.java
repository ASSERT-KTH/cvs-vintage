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
import java.util.ArrayList;

// Turbine Stuff 
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.ParameterParser;
import org.apache.torque.om.NumberKey;
import org.apache.torque.TorqueException;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.RIssueTypeAttribute;
import org.tigris.scarab.om.RAttributeAttributeGroup;
import org.tigris.scarab.om.AttributeGroup;
import org.tigris.scarab.om.AttributeGroupManager;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.services.cache.ScarabCache; 
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.workflow.WorkflowFactory;

/**
 * action methods on RModuleAttribute or RIssueTypeAttribute tables
 *      
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: AttributeGroupEdit.java,v 1.42 2003/02/04 11:26:00 jon Exp $
 */
public class AttributeGroupEdit extends RequireLoginFirstAction
{
    /**
     * Updates attribute group info.
     */
    public void doSaveinfo (RunData data, TemplateContext context)
        throws Exception
    {
        // Set properties for group info
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = AttributeGroupManager
                            .getInstance(new NumberKey(groupId), false);

        if (!ag.isGlobal() && scarabR.getIssueType().getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            return;
        }
        if (intake.isAllValid())
        {
            Group agGroup = intake.get("AttributeGroup", 
                                        ag.getQueryKey(), false);
            agGroup.setProperties(ag);
            ag.save();
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
    }

    /**
     * Changes the properties of existing AttributeGroups and their attributes.
     */
    public void doSave (RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        IssueType issueType = scarabR.getIssueType();
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = AttributeGroupManager
                            .getInstance(new NumberKey(groupId), false);
        ScarabLocalizationTool l10n = getLocalizationTool(context);

        if (!ag.isGlobal() && issueType.getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            return;
        }
        IntakeTool intake = getIntakeTool(context);
        List attributes = ag.getAttributes();
        Module module = scarabR.getCurrentModule();
        String msg = DEFAULT_MSG;
        ArrayList lockedAttrs = new ArrayList();

        if (intake.isAllValid())
        {
            for (int i=attributes.size()-1; i>=0; i--) 
            {
                boolean locked = false;
                // Set properties for module-attribute mapping
                Attribute attribute = (Attribute)attributes.get(i);
                RModuleAttribute rma = module
                                       .getRModuleAttribute(attribute, 
                                                            ag.getIssueType());
                Group rmaGroup = intake.get("RModuleAttribute", 
                                 rma.getQueryKey(), false);

                // Test to see if attribute is locked
                RModuleAttribute rmaTest = rma.copy();
                rmaTest.setModified(false);
                rmaGroup.setProperties(rmaTest);
                if (rmaTest.isModified())
                {
                    RIssueTypeAttribute ria = issueType.getRIssueTypeAttribute(attribute);
                    if (ria != null &&  ria.getLocked())
                    {
                         lockedAttrs.add(attribute);
                         locked = true;
                    }
                }

                if (!locked)
                {
                    // if attribute gets set to inactive, delete dependencies
                    String newActive = rmaGroup.get("Active").toString();
                    String oldActive = String.valueOf(rma.getActive());
                    if (newActive.equals("false") && oldActive.equals("true"))
                    {
                        WorkflowFactory.getInstance()
                            .deleteWorkflowsForAttribute(attribute, module, 
                                                         issueType);
                    }
                    rmaGroup.setProperties(rma);
                    String defaultTextKey = data.getParameters()
                      .getString("default_text");
                    if (defaultTextKey != null && 
                         defaultTextKey.equals(rma.getAttributeId().toString())) 
                    {
                        if (!rma.getRequired())
                        {
                            msg = "ChangesSavedButDefaultTextAttributeRequired";
                        }
                        rma.setIsDefaultText(true);
                        rma.setRequired(true);
                    }
                    try
                    {
                        rma.save();
                        // Set properties for attribute-attribute group mapping
                        RAttributeAttributeGroup raag = 
                            ag.getRAttributeAttributeGroup(attribute);
                        Group raagGroup = intake.get("RAttributeAttributeGroup", 
                                         raag.getQueryKey(), false);
                        raagGroup.setProperties(raag);
                        raag.save();
                    }
                    catch (TorqueException e) 
                    {
                        msg = e.getMessage();
                    }
                }

                // If they attempted to modify locked attributes, give message.
                if (lockedAttrs.size() > 0)
                {
                    setLockedMessage(lockedAttrs, context);
                }
            }
            scarabR.setConfirmMessage(l10n.get(msg));
            intake.removeAll();
            ScarabCache.clear();
        } 
    }


    /**
     * Changes the properties of global AttributeGroups and their attributes.
     */
    public void doSaveglobal (RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = AttributeGroupManager
                            .getInstance(new NumberKey(groupId), false);
        List attributes = ag.getAttributes();
        IssueType issueType = scarabR.getIssueType();
        String msg = DEFAULT_MSG;

        if (intake.isAllValid())
        {
            for (int i=attributes.size()-1; i>=0; i--) 
            {
                // Set properties for module-attribute mapping
                Attribute attribute = (Attribute)attributes.get(i);
                RIssueTypeAttribute ria = issueType
                                       .getRIssueTypeAttribute(attribute);
                Group riaGroup = intake.get("RIssueTypeAttribute", 
                                 ria.getQueryKey(), false);
                riaGroup.setProperties(ria);
                String defaultTextKey = data.getParameters()
                    .getString("default_text");
                if (defaultTextKey != null && 
                     defaultTextKey.equals(ria.getAttributeId().toString())) 
                {
                    if (!ria.getRequired())
                    {
                        msg = "ChangesSavedButDefaultTextAttributeRequired";
                    }
                    ria.setIsDefaultText(true);
                    ria.setRequired(true);
                }
                ria.save();

                // Set properties for attribute-attribute group mapping
                RAttributeAttributeGroup raag = 
                    ag.getRAttributeAttributeGroup(attribute);
                Group raagGroup = intake.get("RAttributeAttributeGroup", 
                                 raag.getQueryKey(), false);
                raagGroup.setProperties(raag);
                raag.save();
            }
            scarabR.setConfirmMessage(l10n.get(msg));
            ScarabCache.clear();
        } 
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
    }

    /**
     * Unmaps attributes to modules.
     */
    public void doDeleteattributes(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        Module module = scarabR.getCurrentModule();
        IssueType issueType = scarabR.getIssueType();
        ScarabUser user = (ScarabUser)data.getUser();
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = AttributeGroupManager
            .getInstance(new NumberKey(groupId), false);

        if (!user.hasPermission(ScarabSecurity.MODULE__EDIT, module))
        {
            scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }
        if (!ag.isGlobal() && issueType.getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            return;
        }
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attributeId;
        ArrayList lockedAttrs = new ArrayList();

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("att_delete_"))
            {
                attributeId = key.substring(11);
                Attribute attribute = AttributeManager
                   .getInstance(new NumberKey(attributeId), false);
                RIssueTypeAttribute ria = issueType.getRIssueTypeAttribute(attribute);
                if (!ag.isGlobal() && ria != null &&  ria.getLocked())
                { 
                    lockedAttrs.add(attribute);
                }
                else
                {
                    try
                    {
                        ag.deleteAttribute(attribute, user, module);
                    }
                    catch (Exception e) 
                    {
                        scarabR.setAlertMessage(e.getMessage());
                    }
                }
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
                if (module == null)
                {
                    issueType.setDedupe(false);
                    issueType.save();
                }
                else
                {
                    RModuleIssueType rmit = module.getRModuleIssueType(issueType);
                    rmit.setDedupe(false);
                    rmit.save();
                }
            }
        }

        // If they attempted to modify locked attributes, give message.
        if (lockedAttrs.size() > 0)
        {
            setLockedMessage(lockedAttrs, context);
        }
        ScarabCache.clear();
        scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));  
    }

    /**
     * This manages clicking the create new button on AttributeSelect.vm
     */
    public void doCreatenewglobalattribute(RunData data, 
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
     * Selects attribute to add to issue type and attribute group.
     */
    public void doSelectattribute(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        AttributeGroup ag = scarabR.getAttributeGroup();

        if (!ag.isGlobal() && scarabR.getIssueType().getLocked())
        {
            scarabR.setAlertMessage(l10n.get("LockedIssueType"));
            return;
        }
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
                    scarabR.getAttribute(new NumberKey(attributeIds[i]));
                ag.addAttribute(attribute);
            }
            doCancel(data, context);
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
        }
    }

    /**
     * Saves all data when Done is clicked.
     */
    public void doDone (RunData data, TemplateContext context)
        throws Exception
    {
        String groupId = data.getParameters().getString("groupId");
        AttributeGroup ag = AttributeGroupManager
                            .getInstance(new NumberKey(groupId), false);
        doSaveinfo(data, context);
        if (ag.isGlobal())
        {
            doSaveglobal(data, context);
        }
        else
        {
            doSave(data, context);
        }
        doCancel(data, context);
    }
        

    /**
     * If user attempts to modify locked attributes, gives message.
     */
    private void setLockedMessage (List lockedAttrs, TemplateContext context)
        throws Exception
    {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<lockedAttrs.size(); i++)
        {
            Attribute attr = (Attribute)lockedAttrs.get(i);
            buf.append(attr.getName());
            if (i == lockedAttrs.size()-1)
            {
                buf.append(".");
            }
            else
            {
                buf.append(",");
            }
        }
        getScarabRequestTool(context).setAlertMessage(getLocalizationTool(context).format("LockedAttributes", buf.toString()));
    }
}
