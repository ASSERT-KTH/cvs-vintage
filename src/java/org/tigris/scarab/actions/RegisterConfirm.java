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
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateAction;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.Turbine;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.turbine.util.Log;

import org.apache.fulcrum.template.TemplateEmail;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabConstants;

/**
        This class  will create a 
        new user in the system and send an email to the user asking for
        the user to confirm their registration by going to the Confirm.vm
        page.
        
    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: RegisterConfirm.java,v 1.20 2001/08/12 23:32:20 jon Exp $
*/
public class RegisterConfirm extends TemplateAction
{
    /**
        This manages clicking the Register confirm button
    */
    public void doConfirmregistration( RunData data, TemplateContext context ) throws Exception
    {
        String template = data.getParameters().getString(ScarabConstants.TEMPLATE, null);
        String nextTemplate = data.getParameters().getString(
            ScarabConstants.NEXT_TEMPLATE, template );

        try
        {
            // pull the user object from the session
            ScarabUser su = (ScarabUser) data.getUser()
                .getTemp(ScarabConstants.SESSION_REGISTER);
            if (su == null)
            {
                // assign the template to the cancel template, not the current template
                template = data.getParameters().getString(ScarabConstants.CANCEL_TEMPLATE, "Register.vm");
                throw new Exception ("Unable to retrive user object from session.");
            }
            // attempt to create a new user!
            su.createNewUser();
            // grab the ScarabSystem object so that we can populate the internal User object
            // for redisplay of the form data on the screen
            ApplicationTool srt = 
                getTool(context, ScarabConstants.SCARAB_REQUEST_TOOL);
            if (srt != null)
            {
                ((ScarabRequestTool)srt).setUser(su);
            }
            
            // send an email that is for confirming the registration
            TemplateEmail te = new TemplateEmail();
            te.setContext(new ContextAdapter(context));
            te.setTo(su.getFirstName() + " " + su.getLastName(), su.getEmail());
            te.setFrom(
                Turbine.getConfiguration().getString("scarab.email.register.fromName",
                    "Scarab System"), 
                Turbine.getConfiguration().getString("scarab.email.register.fromAddress",
                    "register@scarab.tigris.org"));
            te.setSubject(
                Turbine.getConfiguration().getString("scarab.email.register.subject",
                    "Account Confirmation"));
            te.setTemplate(
                Turbine.getConfiguration().getString("scarab.email.register.template",
                    "email/Confirmation.vm"));
            te.send();

            // set the next template on success
            setTarget(data, nextTemplate);
        }
        catch (Exception e)
        {
            setTarget(data, template);
            data.setMessage (e.getMessage());
            Log.error(e);
            return;
        }
    }

    /**
        returns you to Register.vm
    */
    public void doBack( RunData data, TemplateContext context ) throws Exception
    {
        // grab the ScarabRequestTool object so that we can populate the 
        // internal User object for redisplay of the form data on the screen
        ApplicationTool srt = 
            getTool(context, ScarabConstants.SCARAB_REQUEST_TOOL);
        if (srt != null)
        {
            ((ScarabRequestTool)srt).setUser((ScarabUser)data.getUser()
                .getTemp(ScarabConstants.SESSION_REGISTER));
        }
        // set the template to the template that we should be going back to
        setTarget(data, data.getParameters().getString(
                ScarabConstants.CANCEL_TEMPLATE, "Register.vm"));
    }
    /**
        calls doRegisterConfirm()
    */
    public void doPerform( RunData data, TemplateContext context ) throws Exception
    {
        doConfirmregistration(data, context);
    }
}
