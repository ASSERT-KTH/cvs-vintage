package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000 Collab.Net.  All rights reserved.
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

// Velocity Stuff 
import org.apache.turbine.services.velocity.*; 
import org.apache.velocity.*; 
// Turbine Stuff 
import org.apache.turbine.modules.*;
import org.apache.turbine.modules.actions.*;
import org.apache.turbine.om.security.*;
import org.apache.turbine.om.security.peer.*;
import org.apache.turbine.services.resources.*;
import org.apache.turbine.services.resources.TurbineResources;
import org.apache.turbine.services.security.TurbineSecurity;
import org.apache.turbine.util.*;
import org.apache.turbine.util.security.*;
// Scarab Stuff
import org.tigris.scarab.util.ScarabConstants;

/**
    This class is responsible for dealing with the Login
    Action.
    
    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: Login.java,v 1.1 2000/12/18 05:03:29 jon Exp $
*/
public class Login extends VelocityAction
{
    /**
        This manages clicking the Login button
    */
    public void doLogin( RunData data, Context context ) throws Exception
    {
        if (! checkUser(data))
            return;
        // if a next template is defined, then execute that instead of going to the 
        // Start.vm screen.    
        String nextTemplate = data.getParameters().getString(ScarabConstants.NEXT_TEMPLATE, null);
        if (nextTemplate != null)
            setTemplate(data, nextTemplate);
    }

    /**
        Checks to make sure that the user exists, has been confirmed.
    */
    public boolean checkUser(RunData data)
        throws Exception
    {
        String username = data.getParameters().getString ( "email", "" );
        String password = data.getParameters().getString ( "password", "" );
        User user = null;
        try
        {
            // quickly validate some data
            if (username.length() == 0)
                throw new TurbineSecurityException ("Please enter a email address!");
            if (password.length() == 0)
                throw new TurbineSecurityException ("Please enter a password!");

            // Authenticate the user and get the object.
            user = TurbineSecurity.getAuthenticatedUser( username, password );
        
            // check the CONFIRM_VALUE
            // FIXME: deal with this better by not throwing an
            // exception and instead showing the confirmation page.
            // by using setTemplate(data,"Confirm.vm"); return;
            // and creating an anonymous user
            if (!user.isConfirmed())
                throw new TurbineSecurityException("User is not confirmed!");

            // store the user object
            data.setUser(user);
            // mark the user as being logged in
            user.setHasLoggedIn(new Boolean(true));
            // set the last_login date in the database
            user.updateLastLogin();
            // this only happens if the user is valid
            // otherwise, we will get a valueBound in the User
            // object when we don't want to because the username is
            // not set yet.
            // save the User object into the session
            data.save();            
        }
        catch ( TurbineSecurityException e )
        {
            data.setMessage(e.getMessage());
            // Retrieve an anonymous user
            data.setUser (TurbineSecurity.getAnonymousUser());
            setTemplate(data, "Login.vm");
            return false;
        }
        return true;        
    }
    /**
        calls doLogin()
    */
    public void doPerform( RunData data, Context context ) throws Exception
    {
        doLogin(data, context);
    }
}