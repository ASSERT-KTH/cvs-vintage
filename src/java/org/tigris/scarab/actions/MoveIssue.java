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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.Turbine;
import org.apache.turbine.tool.IntakeTool;
import org.tigris.scarab.actions.base.BaseModifyIssue;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypeManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.EmailContext;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;

/**
 * This class is responsible for moving/copying an issue
 * from one module to another.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: MoveIssue.java,v 1.69 2004/11/14 21:06:55 dep4b Exp $
 */
public class MoveIssue extends BaseModifyIssue
{

    /**
     * From MoveIssue.vm -> MoveIssue2.vm, we only need to validate the inputs.
     * Intake + Pull is so friggen cool.
     */
    public void doValidate(RunData data, TemplateContext context)
        throws Exception
    {
        boolean collisionOccurred = isCollision(data, context);
        context.put("collisionDetectedOnMoveAttempt",
                    collisionOccurred ? Boolean.TRUE : Boolean.FALSE);
        if (collisionOccurred)
        {
            // Report the collision to the user.
            doCancel(data, context);
            return;
        }

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
            scarabR.setAlertMessage(L10NKeySet.SelectIssueToMove);
            return;
        }
        else
        {
            for (int i= 0; i<issueIds.length; i++)
            {
                issues.add(scarabR.getIssue(issueIds[i]));
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
                        scarabR.setAlertMessage(L10NKeySet.OnlySelectOneDestination);
                        return;
                    }
                }
            }

        }

        if (modIssueType == null)
        {
            scarabR.setAlertMessage(L10NKeySet.SelectModuleAndIssueType);
            return;
        }
        
        Integer newModuleId = null;
        Integer newIssueTypeId = null;
        Module newModule = null;        
        try
        {
            newModuleId = new Integer(modIssueType.
                      substring(0, modIssueType.indexOf('_')));
            newIssueTypeId = new Integer(modIssueType.
                      substring(modIssueType.indexOf('_')+1, modIssueType.length()));
            newModule = ModuleManager
                               .getInstance(newModuleId);

        }
        catch (Exception e)
        {
            scarabR.setAlertMessage(L10NKeySet.SelectModuleAndIssueType);
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
            scarabR.setAlertMessage(L10NKeySet.CannotMoveToSameModule);
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
        boolean collisionOccurred = isCollision(data, context);
        context.put("collisionDetectedOnMoveAttempt", collisionOccurred ? Boolean.TRUE : Boolean.FALSE);
        if (collisionOccurred)
        {
            // Report the collision to the user.
            setTarget(data, "ViewIssue.vm");
            return;
        }

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
            scarabR.setAlertMessage(L10NKeySet.SelectIssueToMove);
            return;
        }
        else
        {
            for (int i= 0; i<issueIds.length; i++)
            {
                issues.add(scarabR.getIssue(issueIds[i]));
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

        // Get selected non-matching attributes to save in comment
        List commentAttrs = new ArrayList();
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        for (int i=0; i<keys.length; i++)
        {
            String key = (String) keys[i];
            if (key.startsWith("comment_attr_ids_"))
            {
                commentAttrs.add(scarabR
                    .getAttribute(new Integer(key.substring(17))));
            }
        }
        String reason = params.getString("reason");
        if (reason == null || reason.trim().length() == 0)
        {
            scarabR.setAlertMessage(L10NKeySet.ReasonRequired);
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
                L10NMessage l10nMessage = new L10NMessage(L10NKeySet.ErrorExceptionMessage,e);
                scarabR.setAlertMessage(l10nMessage);
                Log.get().warn("Exception during issue copy/move", e);
                return;
            }

            
            // Send notification email
            EmailContext ectx = new EmailContext();
            ectx.setIssue(newIssue);
            ectx.setModule(newModule);
            // placed in the context for the email to be able to access them
            // from within the email template
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
            Set allToUsers =
                issue.getAllUsersToEmail(AttributePeer.EMAIL_TO); 
            HashSet toUsers = new HashSet();
            Set allCCUsers = issue.getAllUsersToEmail(AttributePeer.CC_TO); 
            HashSet ccUsers = new HashSet();

            for (Iterator iter = allToUsers.iterator(); iter.hasNext();) 
            {
                ScarabUser su = (ScarabUser)iter.next();
                if (su.hasPermission(ScarabSecurity.ISSUE__VIEW, newModule))
                {
                    toUsers.add(su);
                }
            }
            for (Iterator iter = allCCUsers.iterator(); iter.hasNext();) 
            {
                ScarabUser su = (ScarabUser)iter.next();
                if (su.hasPermission(ScarabSecurity.ISSUE__VIEW, newModule))
                {
                    ccUsers.add(su);
                }
            }
            try
            {
                Email.sendEmail(ectx, newModule, user, replyToUser,
                                toUsers, ccUsers, template);
            }
            catch (Exception e)
            {
                L10NMessage l10nMessage = new L10NMessage(EMAIL_ERROR,e);
                 scarabR.setAlertMessage(l10nMessage);
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

        scarabR.setConfirmMessage(DEFAULT_MSG);
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
