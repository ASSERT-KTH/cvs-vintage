package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.SequencedHashMap;

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.torque.om.NumberKey;

import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.Scope;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.IssueTemplateInfo;
import org.tigris.scarab.om.IssueTemplateInfoPeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.ActivitySetManager;
import org.tigris.scarab.om.ActivitySetTypePeer;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.util.ScarabException;


/**
 * This class is responsible for report managing issue entry
 * templates.
 *   
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: TemplateList.java,v 1.53 2003/08/19 05:37:55 venkatesh Exp $
 */
public class TemplateList extends RequireLoginFirstAction
{
    /**
     * Creates new template.
     */
    public void doCreatenew(RunData data, TemplateContext context)
         throws Exception
    {        
        IntakeTool intake = getIntakeTool(context);        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = scarabR.getIssueTemplate();
        if (issue == null)
        {
            scarabR.setAlertMessage(l10n.get("IssueTypeNotAvailable"));
            return;
        }

        SequencedHashMap avMap = issue.getModuleAttributeValuesMap();
        AttributeValue aval = null;
        Group group = null;
        
        IssueTemplateInfo info = scarabR.getIssueTemplateInfo();
        Group infoGroup = intake.get("IssueTemplateInfo", info.getQueryKey());
        Group issueGroup = intake.get("Issue", issue.getQueryKey());
        ActivitySet activitySet = null;

        if (intake.isAllValid()) 
        {
            issueGroup.setProperties(issue);
            infoGroup.setProperties(info);
            if (checkForDupes(info, infoGroup.get("Name").toString(), 
                              user, scarabR.getCurrentModule(), 
                              scarabR.getCurrentIssueType()))
            {
                scarabR.setAlertMessage(l10n.get("DuplicateTemplateName"));
            }
            else
            {
                boolean atLeastOne = false;
                Iterator iter = avMap.iterator();
                if (iter.hasNext()) 
                {
                    // Save activitySet record
                    activitySet = ActivitySetManager
                        .getInstance(ActivitySetTypePeer.CREATE_ISSUE__PK, user);
                    activitySet.save();
                    while (iter.hasNext()) 
                    {
                        aval = (AttributeValue)avMap.get(iter.next());
                        group = intake.get("AttributeValue", aval.getQueryKey(),false);
                        String value = null;
                        if (group != null)
                        {
                            if (aval instanceof OptionAttribute) 
                            {
                                value = group.get("OptionId").toString();
                            }
                            else 
                            {
                                value = group.get("Value").toString();
                            }
                            if (StringUtils.isNotEmpty(value))
                            {
                                atLeastOne = true;
                                aval.startActivitySet(activitySet);
                                group.setProperties(aval);
                            }
                        }
                    }
                }
                if (atLeastOne)
                {
                    issue.setCreatedTransId(activitySet.getActivitySetId());
                    issue.save();
                    info.setIssueId(issue.getIssueId());

                    // Save template info
                    boolean success = info.saveAndSendEmail(user, 
                                      scarabR.getCurrentModule(), context);
                    if (success)
                    {
                        scarabR.setConfirmMessage(l10n.get("NewTemplateCreated"));

                        // For a module-scoped template which is now
                        // pending approval, the user may not have
                        // permission to edit the new issue template.
                        if (info.canEdit((ScarabUser) data.getUser()))
                        {
                            data.getParameters().add
                                ("templateId", issue.getIssueId().toString());
                        }
                        else
                        {
                            // Display the list of issue templates.
                            setTarget(data, "TemplateList.vm");
                            doPerform(data, context);
                        }
                    }
                    else
                    {
                        scarabR.setAlertMessage(l10n.get(EMAIL_ERROR));
                    }
                } 
                else
                {
                    scarabR.setAlertMessage(l10n.get("AtLeastOneAttributeForTemplate"));
                }
            }
        }
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
    }

    /**
     * Edits template's attribute values.
     */
    public void doEditvalues(RunData data, TemplateContext context)
         throws Exception
    {        
        IntakeTool intake = getIntakeTool(context);        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = scarabR.getIssueTemplate();

        SequencedHashMap avMap = issue.getModuleAttributeValuesMap();
        AttributeValue aval = null;
        Group group = null;
        Group issueGroup = intake.get("Issue", issue.getQueryKey());
        issueGroup.setProperties(issue);

        if (intake.isAllValid()) 
        {
            // Save activitySet record
            ActivitySet activitySet = ActivitySetManager
                .getInstance(ActivitySetTypePeer.CREATE_ISSUE__PK, user);
            activitySet.save();

            Iterator iter = avMap.iterator();
            while (iter.hasNext()) 
            {
                aval = (AttributeValue)avMap.get(iter.next());
                group = intake.get("AttributeValue", aval.getQueryKey(),false);
                if (group != null)
                {
                    String newValue = "";
                    String oldValue = "";
                    if (aval instanceof OptionAttribute) 
                    {
                        newValue = group.get("OptionId").toString();
                        oldValue = aval.getOptionIdAsString();
                    
                        if (!newValue.equals(""))
                        {
                            AttributeOption newAttributeOption =
                              AttributeOptionManager
                              .getInstance(new Integer(newValue));
                            newValue = newAttributeOption.getName();
                        }
                        if (!oldValue.equals(""))
                        {
                            Integer oldOptionId = aval.getOptionId();
                            AttributeOption oldAttributeOption =
                              AttributeOptionManager
                              .getInstance(oldOptionId);
                            oldValue = oldAttributeOption.getName();
                        }
                        
                    }
                    else 
                    {
                        newValue = group.get("Value").toString();
                        oldValue = aval.getValue();
                    }

                    if (!newValue.equals("") && 
                        (oldValue == null  || !oldValue.equals(newValue)))
                    {
                        aval.startActivitySet(activitySet);
                        group.setProperties(aval);
                        aval.save();
                    }
                }                
            }
            scarabR.setConfirmMessage(l10n.get("TemplateModified"));
        } 
        else
        {
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
    }

    /**
     * Edits templates's basic information.
     */
    public boolean doEdittemplateinfo(RunData data, TemplateContext context)
         throws Exception
    {        
        IntakeTool intake = getIntakeTool(context);        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = scarabR.getIssueTemplate();
        boolean success = true;

        IssueTemplateInfo info = scarabR.getIssueTemplateInfo();
        Group infoGroup = intake.get("IssueTemplateInfo", info.getQueryKey());

        if (intake.isAllValid()) 
        {
            infoGroup.setProperties(info);
            info.setIssueId(issue.getIssueId());
            if (checkForDupes(info, infoGroup.get("Name").toString(), 
                              user, scarabR.getCurrentModule(), 
                              scarabR.getCurrentIssueType()))
            {
                success = false;
                scarabR.setAlertMessage(l10n.get("DuplicateTemplateName"));
            }
            else
            {
                // Save template info
                info.saveAndSendEmail(user, scarabR.getCurrentModule(), context);
                data.getParameters().add("templateId", issue.getIssueId().toString());
                scarabR.setConfirmMessage(l10n.get("TemplateModified"));
            }
        } 
        else
        {
            success = false;
            scarabR.setAlertMessage(l10n.get(ERROR_MESSAGE));
        }
        return success;
    }

    public void doDeletetemplates(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Object[] keys = data.getParameters().getKeys();
        String key;
        String templateId;
        ScarabUser user = (ScarabUser)data.getUser();
        boolean atLeastOne = false;
        boolean success = true;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("delete_"))
            {
                atLeastOne = true;
                templateId = key.substring(7);
                try
                {
                    Issue issue = IssueManager
                       .getInstance(new NumberKey(templateId), false);
                    if (issue == null)
                    {
                        throw new Exception(
                            l10n.get("CouldNotLocateTemplateToDelete"));
                    }
                    issue.delete(user);
                }
                catch (ScarabException e)
                {
                    success = false;
                    scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
                }
                catch (Exception e)
                {
                    success = false;
                    scarabR.setAlertMessage(e.getMessage());
                }
            }
        } 
        if (!atLeastOne)
        {
            scarabR.setAlertMessage(l10n.get("NoTemplateSelected"));
        }
        else if (success)
        {
            scarabR.setConfirmMessage(l10n.get("TemplateDeleted"));
        } 
    } 

    public void doUsetemplate(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        intake.removeAll();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        setTarget(data, scarabR.getNextEntryTemplate());
    }
    
    public void doSave(RunData data, TemplateContext context)
        throws Exception
    {
        doEditvalues(data, context);
        doEdittemplateinfo(data, context);
    }

    private boolean checkForDupes(IssueTemplateInfo template, 
                                  String newName, ScarabUser user, 
                                  Module module, IssueType issueType)
        throws Exception
    {
        boolean areThereDupes = false;
        List prevTemplates = IssueTemplateInfoPeer.getUserTemplates(user, 
                                                   module, issueType);
        if (template.getScopeId().equals(Scope.MODULE__PK))
        {
            prevTemplates.addAll(IssueTemplateInfoPeer.getModuleTemplates(module));
        }
        if (prevTemplates != null && !prevTemplates.isEmpty())
        {
            Long pk = template.getIssueId();
            for (Iterator i = prevTemplates.iterator(); 
                 i.hasNext() && !areThereDupes;)
            {
                IssueTemplateInfo  t = (IssueTemplateInfo)i.next();
                areThereDupes = ((pk == null || !pk.equals(t.getIssueId())) &&
                    newName.trim().toLowerCase().equals(
                        t.getName().trim().toLowerCase()));
            }
        }
        return areThereDupes;
    }

    /**
        Overrides base class.
    */
    public void doDone(RunData data, TemplateContext context)  
        throws Exception
    {
        boolean success = doEdittemplateinfo(data, context);
        if (success)
        {
            doEditvalues(data, context);
            doCancel(data, context);
        }
    }
}
