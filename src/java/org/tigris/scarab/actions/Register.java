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
import org.apache.fulcrum.security.TurbineSecurity;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.actions.base.ScarabTemplateAction;

/**
    This class is responsible for dealing with the Register
    Action.
    
    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: Register.java,v 1.13 2001/09/30 18:31:38 jon Exp $
*/
public class Register extends ScarabTemplateAction
{
    /**
        This manages clicking the Register button which will end up sending
        the user to the RegisterConfirm screen.
    */
    public void doRegister( RunData data, TemplateContext context ) 
        throws Exception
    {
        String template = data.getParameters()
                            .getString(ScarabConstants.TEMPLATE, null);
        String nextTemplate = data.getParameters()
                            .getString(ScarabConstants.NEXT_TEMPLATE, template);

        // create an empty user object
        ScarabUser su = (ScarabUser) TurbineSecurity.getAnonymousUser();
        try
        {
            // populate it with form data and do validation
            // FIXME: this should use intake 
            data.getParameters().setProperties(su);

            String password_confirm = data.getParameters()
                .getString("password_confirm", null);
            su.setUserName(data.getParameters().getString("Email"));
            
            // FIXME: add better email address checking to catch stupid 
            // mistakes up front
            // FIXME: add better form validation all around, make sure we 
            // don't have bad data as well as the right length.
            if (su.getFirstName() == null || su.getFirstName().length() == 0)
                throw new Exception("The first name you entered is empty!");
            if (su.getLastName() == null || su.getLastName().length() == 0)
                throw new Exception("The last name you entered is empty!");
            if (su.getUserName() == null || su.getUserName().length() == 0)
                throw new Exception("The email address you entered is empty!");
            if (su.getPassword() == null || su.getPassword().length() == 0)
                throw new Exception("The password you entered is empty!");
            if (password_confirm == null)
                throw new Exception( 
                    "The password confirm you entered is empty!");
            if (!su.getPassword().equals(password_confirm))
                throw new Exception("The password's you entered do not match!");
            
            // check to see if the user already exists
            if(ScarabUserImplPeer.checkExists(su))
            {
                throw new Exception(
                    "Sorry, a user with that loginid already exists!" );
            }

            // stick the user object into the session so that it can
            // be retrieved on the next invocation in action.RegisterConfirm
            // we don't actually create the user in the system until the next page.
            data.getUser().setTemp(ScarabConstants.SESSION_REGISTER, su);

            setTarget(data, nextTemplate);
        }
        catch (Exception e)
        {
            setTarget(data, template );
            data.setMessage (e.getMessage());
            Log.debug ("Register Error: ", e);
            return;
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
