package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.tool.IntakeTool;

import org.apache.fulcrum.intake.model.Group;

import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.TurbineSecurity;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
// FIXME: remove the methods that reference this
import org.tigris.scarab.om.ScarabUserImpl;
import org.tigris.scarab.om.ScarabModulePeer;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.actions.base.ScarabTemplateAction;

/**
 * This class is responsible for dealing with the Confirm
 * Action.
 *   
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: Confirm.java,v 1.23 2001/10/24 23:41:57 jon Exp $
 */
public class Confirm extends ScarabTemplateAction
{
    /**
     * This manages clicking the Register button which will end up sending
     * the user to the RegisterConfirm screen.
     */
    public void doConfirm( RunData data, TemplateContext context ) throws Exception
    {
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);

        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            Object user = data
                            .getUser()
                            .getTemp(ScarabConstants.SESSION_REGISTER);
            Group register = null;
            if (user != null && user instanceof ScarabUser)
            {
                register = intake.get("Register", 
                    ((ScarabUser)user).getQueryKey(), false);
            }
            else
            {
                register = intake.get("Register",
                    IntakeTool.DEFAULT_KEY, false);
            }

            String username = register.get("Email").toString();
            String confirm = register.get("Confirm").toString();

            // FIXME: this shouldn't directly reference ScarabUserImpl
            // but should instead go through the security service or something.
            if (ScarabUserImpl.checkConfirmationCode(username, confirm))
            {
                // update the database to confirm the user
                if(ScarabUserImpl.confirmUser(username))
                {
                    // NO PROBLEMS! :-)
                    ScarabUser confirmedUser = (ScarabUser)
                                    TurbineSecurity.getUser(username);
                    confirmedUser.setHasLoggedIn(Boolean.TRUE);
                    data.setUser(confirmedUser);
                    data.save();
    
                    data.setMessage("Your account has been confirmed. Welcome to Scarab!");
                    setTarget(data, nextTemplate);
                    
                    // FIXME: Hack to give every new account a Developer Role
                    // within every Group (ie: Module). This is a major major
                    // major major major hole. The point however is to allow people
                    // using the runbox or downloading scarab a chance to be able
                    // to enter an issue without having to muck with Flux to get
                    // the right roles. Hopefully someone from the community will
                    // contribute code to clean this up. For more information, 
                    // please read this thread:
                    // http://scarab.tigris.org/servlets/ReadMsg?msgId=38339&listName=dev
                    List allModules = ScarabModulePeer.getAllModules();
                    Iterator itr = allModules.iterator();
                    Role role = TurbineSecurity.getRole("Developer");
                    while (itr.hasNext())
                    {
                        // have to use the full Group because of conflicts with
                        // Intake Group
                        org.apache.fulcrum.security.entity.Group group = 
                            (org.apache.fulcrum.security.entity.Group) itr.next();
                        // only give access to the non-global modules
                        if (!group.getName()
                            .startsWith(ScarabConstants.GLOBAL_MODULE_NAME + 
                                ModuleEntity.NAME_DELIMINATOR))
                        {
                            group.grant((User)confirmedUser, role);
                            ((ModuleEntity)group).save();
                        }
                    }
                }
                else
                {
                    data.setMessage("Your account has not been confirmed. " + 
                                    "There has been an error.");
                    setTarget(data, template);
                }
            }
            else // we don't have confirmation! :-(
            {
                data.setMessage("Sorry, that email address and/or confirmation " + 
                                "code is invalid.");
                setTarget(data, template);
            }
        }
    }

    /**
        This manages clicking the Cancel button
    */
    public void doCancel( RunData data, TemplateContext context ) throws Exception
    {
        setTarget(data, data.getParameters().getString(
                ScarabConstants.CANCEL_TEMPLATE, "Login.vm"));
    }

    /**
        calls doCancel()
    */
    public void doPerform( RunData data, TemplateContext context ) throws Exception
    {
        doCancel(data, context);
    }
}
