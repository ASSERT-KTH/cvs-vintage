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
import java.util.Iterator;

// Turbine Stuff 
import org.apache.fulcrum.template.DefaultTemplateContext;
import org.apache.torque.om.NumberKey; 
import org.apache.turbine.TemplateAction;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.RunData;
import org.apache.turbine.Turbine;
import org.apache.turbine.ParameterParser;
import org.apache.fulcrum.security.TurbineSecurity;


// Scarab Stuff
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.QueryPeer;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.om.PendingGroupUserRole;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.IssueTemplateInfo;
import org.tigris.scarab.om.IssueTemplateInfoPeer;
import org.tigris.scarab.util.Email;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.SecurityAdminTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.services.security.ScarabSecurity;

/**
    This class is responsible for edit issue forms.
    ScarabIssueAttributeValue
    @author <a href="mailto:elicia@collab.net">Elicia David</a>
    @version $Id: Approval.java,v 1.19 2002/07/22 22:10:21 elicia Exp $
*/
public class Approval extends RequireLoginFirstAction
{

    private static final String EMAIL_ERROR = "Your changes were saved, " +
                                "but could not send notification email due " + 
                                "to a sendmail error.";

    public void doSubmit( RunData data, TemplateContext context )
        throws Exception
    {
        ScarabRequestTool scarabR = (ScarabRequestTool)context
            .get(ScarabConstants.SCARAB_REQUEST_TOOL);
        ScarabUser user = (ScarabUser)data.getUser();
        Module module = scarabR.getCurrentModule();
        String globalComment = data.getParameters().getString("global_comment");
       
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String action = null;
        String actionWord = null;
        String artifact = null;
        String artifactName = null;
        String comment = null;
        ScarabUser toUser = null;
        String userId;

        for (int i =0; i<keys.length; i++)
        {
            action="none";
            key = keys[i].toString();
            if (key.startsWith("query_id_"))
            {
               String queryId = key.substring(9);
               Query query = (Query) QueryPeer
                                     .retrieveByPK(new NumberKey(queryId));

               action = params.getString("query_action_" + queryId);
               comment = params.getString("query_comment_" + queryId);

               userId = params.getString("query_user_" + queryId);
               toUser = (ScarabUser) ScarabUserImplPeer
                                     .retrieveByPK(new NumberKey(userId));
               artifact = "query";
               artifactName = query.getName();

               if (action.equals("reject"))
               {
                   try
                   {
                       query.approve(user, false);
                   }
                   catch (ScarabException e)
                   {
                       scarabR.setAlertMessage(e.getMessage());
                   }
                   actionWord = "rejected";
               } 
               else if (action.equals("approve"))
               {
                   try
                   {
                       query.approve(user, true);
                   }
                   catch(ScarabException e)
                   {
                       scarabR.setAlertMessage(e.getMessage());
                   }
                   actionWord = "approved";
               }

            }
            else if (key.startsWith("template_id_"))
            {
               String templateId = key.substring(12);
               IssueTemplateInfo info = (IssueTemplateInfo) IssueTemplateInfoPeer
                                     .retrieveByPK(new NumberKey(templateId));

               action = params.getString("template_action_" + templateId);
               comment = params.getString("template_comment_" + templateId);

               userId = params.getString("template_user_" + templateId);
               toUser = scarabR.getUser(userId);
               artifact = "issue entry template";
               artifactName = info.getName();

               if (action.equals("reject"))
               {
                   try
                   {
                       info.approve(user, false);
                   }
                   catch(ScarabException e)
                   {
                       scarabR.setAlertMessage(e.getMessage());
                   }
                   actionWord = "rejected";
               } 
               else if (action.equals("Approve"))
               {
                   try
                   {
                       info.approve(user, true);
                   }
                   catch(ScarabException e)
                   {
                       scarabR.setAlertMessage(e.getMessage());
                   }
                   actionWord = "approved";
               }
            }

            if (!action.equals("none"))
            {
                // send email
                StringBuffer bodyBuf = new StringBuffer("The user ");
                bodyBuf.append(user.getUserName());
                bodyBuf.append(" has just ").append(actionWord);
                bodyBuf.append(" your ").append(artifact).append(" '");
                bodyBuf.append(artifactName).append("'.");

                // add data to context for email template
                context.put("body", bodyBuf.toString());
                context.put("comment", comment);
                context.put("globalComment", globalComment);

                String subject = "Scarab " + artifact + " " + actionWord;
                String template = Turbine.getConfiguration().
                    getString("scarab.email.approval.template",
                              "email/Approval.vm");
                if (!Email.sendEmail(new ContextAdapter(context), 
                                     module, user, toUser, subject,
                                     template))
                {
                    scarabR.setAlertMessage(EMAIL_ERROR);
                }
            }
        }
    }

    public void doApproveroles(RunData data, TemplateContext context)
        throws Exception
    {
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Module module = scarabR.getCurrentModule();
        if (((ScarabUser)data.getUser())
            .hasPermission(ScarabSecurity.USER__APPROVE_ROLES, module)) 
        {
            SecurityAdminTool scarabA = getSecurityAdminTool(context);
            List pendings = scarabA.getPendingGroupUserRoles(module);
            Iterator i = pendings.iterator();
            while (i.hasNext()) 
            {
                PendingGroupUserRole pending = (PendingGroupUserRole)i.next();
                ScarabUser user = 
                    ScarabUserManager.getInstance(pending.getUserId());
                String role = data.getParameters()
                    .getString(user.getUserName());
                if (role != null && role.length() > 0) 
                {
                    if (!role.equals("defer") && !role.equals("deny")) 
                    {
                        TurbineSecurity.grant( user, 
                            (org.apache.fulcrum.security.entity.Group)module, 
                            TurbineSecurity.getRole(role) );
                        pending.delete();
                    }
                    else if (role.equals("deny"))
                    {
                        pending.delete();
                    }
                }
            }
            scarabR.setConfirmMessage("All roles have been processed.");
        }        
        setTarget(data, nextTemplate);
    }

    /**
     * Helper method to retrieve the ScarabRequestTool from the Context
     */
    private SecurityAdminTool getSecurityAdminTool(TemplateContext context)
    {
        return (SecurityAdminTool)context
            .get(ScarabConstants.SECURITY_ADMIN_TOOL);
    }
}
