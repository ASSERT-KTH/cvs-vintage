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
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypeManager;
import org.tigris.scarab.om.IssueTypePeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * This class deals with modifying Global Artifact Types.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @version $Id: GlobalArtifactTypes.java,v 1.33 2003/02/02 23:51:07 jon Exp $
 */
public class GlobalArtifactTypes extends RequireLoginFirstAction
{
    /**
     * Used on GlobalAttributeEdit.vm to modify Attribute Name/Description/Type
     * Use doAddormodifyattributeoptions to modify the options.
     */
    public void doSave(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        List issueTypes = IssueTypePeer.getAllIssueTypes(false);
        boolean dupe = false;
        boolean saved = false;
        if (intake.isAllValid())
        {
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            for (int i=0; i<issueTypes.size(); i++)
            {
                IssueType issueType = (IssueType)issueTypes.get(i);
                Group group = intake.get("IssueType", issueType.getQueryKey());
                // make sure name is unique
                Field field = group.get("Name");
                String name = field.toString();
                if (IssueTypePeer.isUnique(name, issueType.getPrimaryKey())) 
                {
                    group.setProperties(issueType);
                    issueType.save();
                    ScarabCache.clear();
                    saved = true;
                }
                else 
                {
                    dupe = true;
                    field.setMessage("Duplicate");
                }
            }
            if (dupe)
            {
                scarabR.setAlertMessage(
                    l10n.get("ChangesResultDuplicateNames"));
            }
            else if (saved)
            {
                scarabR.setConfirmMessage(
                    l10n.get(DEFAULT_MSG));
            }
            else
            {
                scarabR.setInfoMessage(
                    l10n.get(NO_CHANGES_MADE));
            }
        }
    }
                
    public void doCopy(RunData data, TemplateContext context)
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String id;
        IssueType issueType;
        boolean didCopy = false;
        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("action_"))
            {
                id = key.substring(7);
                issueType = IssueTypeManager.getInstance(new NumberKey(id));
                if (issueType != null)
                {
                    issueType.copyIssueType();
                    didCopy = true;
                }
            }
        }
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        if (didCopy)
        {
            scarabR.setConfirmMessage(l10n.get("GlobalArtifactTypeCopied"));
        }
        else
        {
            scarabR.setInfoMessage(l10n.get(NO_CHANGES_MADE));
        }
    }

    public void doDelete(RunData data, TemplateContext context)
        throws Exception
    {
        String key = null;
        String id = null;
        IssueType issueType = null;
        boolean deleted = false;
        boolean hasIssues = false;

        Object[] keys = data.getParameters().getKeys();
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IntakeTool intake = getIntakeTool(context);
        for (int i =0; i<keys.length; i++)
        {
             key = keys[i].toString();
             if (key.startsWith("action_"))
             {
                id = key.substring(7);
                issueType = IssueTypeManager.getInstance(new NumberKey(id));
                if (issueType != null)
                {
                    if (issueType.hasIssues())
                    {
                        Group group = intake.get("IssueType", issueType.getQueryKey());
                        Field field = group.get("Name");
                        field.setMessage("IssueTypeHasIssues");
                        hasIssues = true;
                    }
                    else 
                    {
                        issueType.setDeleted(true);
                        issueType.save();
                        deleted = true;
                    }
                }
            }
        }
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        if (hasIssues)
        {
            scarabR.setAlertMessage(l10n.get("CannotDeleteIssueTypesWithIssues"));
        }
        else if (deleted)
        {
            scarabR.setConfirmMessage(l10n.get("GlobalIssueTypesDeleted"));
        }
        else
        {
            scarabR.setInfoMessage(l10n.get(NO_CHANGES_MADE));
        }
    }

    public void doUndelete(RunData data, TemplateContext context)
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String id;
        IssueType issueType;
        boolean saved = false;
        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("action_"))
            {
                id = key.substring(7);
                issueType = IssueTypeManager.getInstance(new NumberKey(id));
                if (issueType != null)
                {
                    issueType.setDeleted(false);
                    issueType.save();
                    saved = true;
                }
            }
        }
        if (saved)
        {
            getScarabRequestTool(context)
                .setConfirmMessage(
                getLocalizationTool(context)
                .get("GlobalIssueTypesUnDeleted"));
        }
    }
}
