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

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.tool.IntakeTool;

import org.apache.fulcrum.intake.model.Group;

import org.apache.torque.om.NumberKey; 
import org.apache.torque.om.ObjectKey; 
import org.apache.torque.util.Criteria;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentPeer;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.RModuleOptionPeer;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.ActivitySetTypePeer;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.services.security.ScarabSecurity;

/**
 * This class is responsible for moving/copying an issue 
 * from one module to another.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: MoveIssue.java,v 1.31 2002/07/30 22:48:14 jmcnally Exp $
 */
public class MoveIssue extends RequireLoginFirstAction
{

    /**
     * From MoveIssue.vm -> MoveIssue2.vm, we only need to validate the inputs.
     * Intake + Pull is so friggen cool.
     */
    public void doValidate( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        if (!intake.isAllValid())
        {
            return;
        }

        Issue issue = getScarabRequestTool(context).getIssue();
        Module oldModule = issue.getModule();
        Group moveIssue = intake.get("MoveIssue",
                          IntakeTool.DEFAULT_KEY, false);
        String selectAction = moveIssue.get("Action").toString();
        NumberKey newModuleId = ((NumberKey) moveIssue.get("ModuleId").
                                                       getValue());
        Module newModule = ModuleManager
                           .getInstance(new NumberKey(newModuleId));
        ScarabUser user = (ScarabUser)data.getUser();

        // Check permissions
        String permission = ScarabSecurity.ISSUE__ENTER;
        if (selectAction.equals("move"))
        {
            permission = ScarabSecurity.MODULE__EDIT;
        }
        if (!user.hasPermission(ScarabSecurity.ISSUE__ENTER, oldModule)
            || !user.hasPermission(permission, newModule))
        {
            data.setMessage(NO_PERMISSION_MESSAGE);
            return;
        }

        // Check that destination module has the current issue type
        if (newModule.getRModuleIssueType(issue.getIssueType()) == null)
        {
            data.setMessage("The destination module does not have "
                   + "this issue type associated with it. Please add " 
                   + " this issue type and try again.");
            return;
        }

        String nextTemplate = getNextTemplate(data);
        setTarget(data, nextTemplate);
    }

    /**
     * Deals with moving or copying an issue from one module to
     * another module.
     */
    public void doSaveissue( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        if (!intake.isAllValid())
        {
            return;
        }

        Issue issue = getScarabRequestTool(context).getIssue();
        Module oldModule = issue.getModule();
        Group moveIssue = intake.get("MoveIssue",
                          IntakeTool.DEFAULT_KEY, false);
        NumberKey newModuleId = ((NumberKey) moveIssue.get("ModuleId").
            getValue());
        Module newModule = ModuleManager
               .getInstance(new NumberKey(newModuleId));
        String selectAction = moveIssue.get("Action").toString();
        ScarabUser user = (ScarabUser)data.getUser();
        
        Issue newIssue = issue.move(newModule, selectAction, user);

        // generate comment
        StringBuffer descBuf = new StringBuffer();
        String comment = null;
        if (selectAction.equals("copy"))
        {
            comment = " copied from issue ";
        }
        else
        {
            comment = " moved from issue ";
        }
        descBuf.append(comment);
        descBuf.append(issue.getUniqueId());
        descBuf.append(" in module ").append(oldModule.getName());

        // placed in the context for the email to be able to access them
        context.put("action", selectAction);
        context.put("issue", newIssue);
        context.put("oldModule", oldModule);
        context.put("newModule", newModule.getName());

        // Send notification email
        String replyToUser = "scarab.email.modifyissue";
        String template = Turbine.getConfiguration().
           getString("scarab.email.moveissue.template",
                     "email/MoveIssue.vm");
        if (!Email.sendEmail(new ContextAdapter(context), newModule, 
                             user, replyToUser,
                             issue.getUsersToEmail(AttributePeer.EMAIL_TO),
                             issue.getUsersToEmail(AttributePeer.CC_TO),
                              "Issue " +  newIssue.getUniqueId() 
                              + descBuf.toString(), template))
        {
             getScarabRequestTool(context).setInfoMessage(
                 "Your changes were saved, but could not send "
                 + "notification email due to a sendmail error.");
        }                      

        // Redirect to moved or copied issue
        data.getParameters().remove("id");
        data.getParameters().add("id", newIssue.getUniqueId().toString());
        setTarget(data, "ViewIssue.vm");
    }

    /**
     * This manages clicking the Back button on MoveIssue2.vm
     */
    public void doBacktoone( RunData data, TemplateContext context ) throws Exception
    {
        setTarget(data, data.getParameters()
            .getString(ScarabConstants.CANCEL_TEMPLATE, "MoveIssue.vm"));
    }    
}
