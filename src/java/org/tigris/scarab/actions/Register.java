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

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.Log;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.security.TurbineSecurity;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.actions.base.ScarabTemplateAction;

/**
 * This class is responsible for dealing with the Register
 * Action.
 *   
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: Register.java,v 1.16 2002/01/16 01:29:21 jon Exp $
 */
public class Register extends ScarabTemplateAction
{
    /**
     * This manages clicking the Register button which will end up sending
     * the user to the RegisterConfirm screen.
     */
    public void doRegister( RunData data, TemplateContext context ) 
        throws Exception
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

            // not quite sure why this happens, but it does, so case
            // for it and deal with it.
            if (register == null)
            {
                setTarget(data,"Register.vm");
                data.setMessage("Sorry! There is an error in your session. " + 
                "Please close your browser and start over.");
                return;
            }

            String password = register.get("Password").toString();
            String passwordConfirm = register.get("PasswordConfirm").toString();

            // check to make sure the passwords match
            if (!password.equals(passwordConfirm))
            {
                setTarget(data, template);
                data.setMessage("The password's you entered do not match!");
                return;
            }

            // get an anonymous user
            ScarabUser su = (ScarabUser) TurbineSecurity.getAnonymousUser();
            try
            {
                register.setProperties(su);
                // need to set this specially
                su.setUserName(register.get("Email").toString());
            }
            catch (Exception e)
            {
                setTarget(data, template);
                data.setMessage (e.getMessage());
                return;
            }

            // check to see if the user already exists
            if(ScarabUserImplPeer.checkExists(su))
            {
                setTarget(data, template);
                data.setMessage(
                    "Sorry, a user with that email address already exists!");
                return;
            }

            // put the user object into the context so that it can be
            // used on the nextTemplate
            data.getUser().setTemp(ScarabConstants.SESSION_REGISTER, su);
            setTarget(data, nextTemplate);
        }
        else
        {
            // if the intake information is invalid, then null out the user
            // that is stored in the session just to be careful
            data.getUser().setTemp(ScarabConstants.SESSION_REGISTER, null);
        }
    }

}
