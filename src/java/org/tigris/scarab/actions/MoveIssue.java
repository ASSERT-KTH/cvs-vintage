package org.tigris.scarab.actions;

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
import java.util.ArrayList;

// Turbine Stuff
import org.apache.turbine.Turbine;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.tool.IntakeTool;

import org.apache.fulcrum.intake.model.Group;
import org.apache.turbine.ParameterParser;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypeManager;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.EmailContext;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.services.security.ScarabSecurity;

/**
 * This class is responsible for moving/copying an issue
 * from one module to another.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: MoveIssue.java,v 1.56 2003/04/17 22:49:06 elicia Exp $
 */
public class MoveIssue extends RequireLoginFirstAction
{

    /**
     * From MoveIssue.vm -> MoveIssue2.vm, we only need to validate the inputs.
     * Intake + Pull is so friggen cool.
     */
    public void doValidate(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        if (!intake.isAllValid())
        {
            return;
        }

        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        String[] issueIds = data.getParameters().getStrings("issue_ids");
        List issues = new ArrayList();
        Issue issue = null;
        if (issueIds == null || issueIds.length == 0)
        {
            scarabR.setAlertMessage(l10n.get("SelectIssueToMove"));
            return;
        }
        else
        {
            for (int i= 0; i<issueIds.length; i++)
            {
                issues.add((Issue)scarabR.getIssue(issueIds[i]));
            }
            issue = (Issue)issues.get(0);
        }

        Module oldModule = issue.getModule();
        Group moveIssue = intake.get("MoveIssue",
                          IntakeTool.DEFAULT_KEY, false);
        String[] modIssueTypes = 
            data.getParameters().getStrings("mod_issuetype");
        String modIssueType = null;
        if (modIssueTypes != null)
        {
            for (int i=0; i<modIssueTypes.length; i++) 
            {
                String testOption = modIssueTypes[i];
                if (testOption != null && testOption.length() > 0) 
                {
                    if (modIssueType == null) 
                    {
                        modIssueType = testOption;
                    }
                    else 
                    {
                        scarabR.setAlertMessage(l10n.get("OnlySelectOneDestination"));
                        return;
                    }
                }
            }
            
        }

        if (modIssueType == null)
        {
            scarabR.setAlertMessage(l10n.get("SelectModuleAndIssueType"));
            return;
        }
        
        Integer newModuleId = null;
        Integer newIssueTypeId = null;
        Module newModule = null;
        IssueType newIssueType = null;
        try
        {
            newModuleId = new Integer(modIssueType.
                      substring(0, modIssueType.indexOf('_')));
            newIssueTypeId = new Integer(modIssueType.
                      substring(modIssueType.indexOf('_')+1, modIssueType.length()));
            newModule = ModuleManager
                               .getInstance(newModuleId);
            newIssueType = IssueTypeManager
                               .getInstance(newIssueTypeId);
        }
        catch (Exception e)
        {
            scarabR.setAlertMessage(l10n.get("SelectModuleAndIssueType"));
            return;
        }
          
        String selectAction = moveIssue.get("Action").toString();
        ScarabUser user = (ScarabUser)data.getUser();
        boolean changeModule = !newModuleId.equals(oldModule.getModuleId());
        boolean changeIssueType = !newIssueTypeId
            .equals(issue.getIssueType().getIssueTypeId());

        // Check permissions
        // Must have ISSUE_ENTER in new module
        // If moving to a new module, must have ISSUE_MOVE in old module
        // If moving to a new issue type, must have ISSUE_EDIT in old module
        if (!user.hasPermission(ScarabSecurity.ISSUE__ENTER, newModule))
        {
            data.setMessage(l10n.get(NO_PERMISSION_MESSAGE));
            return;
        }
        if ("move".equals(selectAction))
        {
            if (changeModule && 
                !user.hasPermission(ScarabSecurity.ISSUE__MOVE, oldModule) || 
                (changeIssueType && 
                !user.hasPermission(ScarabSecurity.ISSUE__EDIT, oldModule)))
            {
                data.setMessage(l10n.get(NO_PERMISSION_MESSAGE));
                return;
            }
        }
        // Do not allow user to move issue if source and destination
        // Module and issue type are the same
        if ("move".equals(selectAction) && !changeModule && !changeIssueType)
        {
            scarabR.setAlertMessage(l10n.get("CannotMoveToSameModule"));
            return;
        }
       
        context.put("newModuleId", newModuleId.toString());
        context.put("newIssueTypeId", newIssueTypeId.toString());
        String nextTemplate = getNextTemplate(data);
        setTarget(data, nextTemplate);
    }

    /**
     * Deals with moving or copying an issue from one module to
     * another module.
     */
    public void doSaveissue(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        if (!intake.isAllValid())
        {
            return;
        }

        ScarabLocalizationTool l10n = getLocalizationTool(context);
        String[] issueIds = data.getParameters().getStrings("issue_ids");
        List issues = new ArrayList();
        Issue issue = null;
        if (issueIds == null || issueIds.length == 0)
        {
            scarabR.setAlertMessage(l10n.get("SelectIssueToMove"));
            return;
        }
        else
        {
            for (int i= 0; i<issueIds.length; i++)
            {
                issues.add((Issue)scarabR.getIssue(issueIds[i]));
            }
            issue = (Issue)issues.get(0);
        }

        Module oldModule = issue.getModule();
        IssueType oldIssueType = issue.getIssueType();
        Group moveIssue = intake.get("MoveIssue",
                          IntakeTool.DEFAULT_KEY, false);
        Integer newModuleId = ((Integer) moveIssue.get("ModuleId").
            getValue());
        Integer newIssueTypeId = ((Integer) moveIssue.get("IssueTypeId").
            getValue());
        Module newModule = ModuleManager
               .getInstance(newModuleId);
        IssueType newIssueType = IssueTypeManager
               .getInstance(newIssueTypeId);
        String selectAction = moveIssue.get("Action").toString();
        ScarabUser user = (ScarabUser)data.getUser();
        boolean changeModule = !newModuleId.equals(oldModule.getModuleId());
        boolean changeIssueType = !newIssueTypeId
            .equals(issue.getIssueType().getIssueTypeId());

        // Get selected non-matching attributes to save in comment
        List commentAttrs = new ArrayList();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        for (int i=0; i<keys.length; i++)
        {
            String key = (String) keys[i];
            if (key.startsWith("comment_attr_ids_"))
            {
                commentAttrs.add(
                    (Attribute)scarabR
                    .getAttribute(new Integer(key.substring(17))));
            }
        }
        String reason = data.getParameters().getString("reason");
        if (reason == null || reason.trim().length() == 0)
        {
            scarabR.setAlertMessage(l10n.get("ReasonRequired"));
            return;
        }

        Issue newIssue = null;
        for (int i=0; i<issues.size(); i++)
        {
            issue = (Issue)issues.get(i);
            // Do the copy/move
            try
            {
                newIssue = issue.move(newModule, newIssueType, 
                                      selectAction, user,
                                      reason, commentAttrs);
            }
            catch (Exception e)
            {
                scarabR.setAlertMessage(e.getMessage());
                return;
            }

            
            // Send notification email
            EmailContext ectx = new EmailContext();
            ectx.setLinkTool((ScarabLink)context.get("link"));
            ectx.setIssue(newIssue);
            ectx.setModule(newModule);
            // placed in the context for the email to be able to access them
            ectx.put("reason", reason);
            ectx.put("action", selectAction);
            ectx.put("oldModule", oldModule);
            ectx.put("oldIssueType", oldIssueType);
            ectx.put("oldIssue", issue);
            if (selectAction.equals("copy"))
            {
                ectx.setDefaultTextKey("CopiedIssueEmailSubject");
            }
            else
            {
                ectx.setDefaultTextKey("MovedIssueEmailSubject");
            }

            String[] replyToUser = newModule.getSystemEmail();
            String template = Turbine.getConfiguration().
               getString("scarab.email.moveissue.template",
                         "MoveIssue.vm");
            if (!Email.sendEmail(ectx, newModule,
                                 user, replyToUser,
                                 issue.getAllUsersToEmail(AttributePeer.EMAIL_TO),
                                 issue.getAllUsersToEmail(AttributePeer.CC_TO),
                                 template))
            {
                 scarabR.setAlertMessage(l10n.get(EMAIL_ERROR));
            }
        }

        // Redirect to moved or copied issue
        if (issues.size() == 1)
        {
            data.getParameters().remove("id");
            data.getParameters().add("id", newIssue.getUniqueId().toString());
            setTarget(data, "ViewIssue.vm");
        }
        else
        {
                setTarget(data, "IssueList.vm");
        }

        scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
    }

    /**
     * This manages clicking the Back button on MoveIssue2.vm
     */
    public void doBacktoone(RunData data, TemplateContext context) throws Exception
    {
        setTarget(data, data.getParameters()
            .getString(ScarabConstants.CANCEL_TEMPLATE, "MoveIssue.vm"));
    }
}
