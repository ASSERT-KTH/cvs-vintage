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
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateAction;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.fulcrum.security.entity.User;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.turbine.tool.IntakeTool;
import org.apache.turbine.services.intake.model.Group;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.fulcrum.security.util.TurbineSecurityException;

// Scarab Stuff
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.om.ScarabUser;

/**
    This class is responsible for dealing with the Login
    Action.
    
    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: Login.java,v 1.18 2001/08/09 07:59:52 jon Exp $
*/
public class Login extends TemplateAction
{
    /**
        This manages clicking the Login button
    */
    public void doLogin( RunData data, TemplateContext context ) throws Exception
    {
        data.setACL(null);

        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);

        Group login = intake.get("Login", IntakeTool.DEFAULT_KEY);
        
        if ( intake.isAllValid() && checkUser(data, context) ) 
        {
            String template = data.getParameters()
                .getString(ScarabConstants.NEXT_TEMPLATE, 
                Turbine.getConfiguration()
                           .getString("template.homepage", "Start.vm") );
            setTarget(data, template);
        }
        else 
        {
            failAction(data);
        }
    }

    /**
        Checks to make sure that the user exists, has been confirmed.
    */
    public boolean checkUser(RunData data, TemplateContext context)
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);

        Group login = intake.get("Login", IntakeTool.DEFAULT_KEY);
        String username = login.get("Username").toString();
        String password = login.get("Password").toString();
        
        User user = null;
        try
        {
            // Authenticate the user and get the object.
            user = TurbineSecurity
                .getAuthenticatedUser( username, password );
        }
        catch ( TurbineSecurityException e )
        {
            data.setMessage("Invalid username or password.");
            return failAction(data);
        }
        
        try
        {
            // check the CONFIRM_VALUE
            if (!user.isConfirmed())
            {
                ApplicationTool srt = 
                    getTool(context, ScarabConstants.SCARAB_REQUEST_TOOL);
                if (srt != null)
                {
                    user = TurbineSecurity.getUserInstance();
                    user.setEmail (username);
                    ((ScarabRequestTool)srt).setUser((ScarabUser)user);
                }

                setTarget(data, "Confirm.vm");
                throw new TurbineSecurityException("User is not confirmed!");
            }

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
            return failAction(data);
        }
        return true;
    }

    /**
     * sets an anonymous user
     * sets the template to "Login.vm"
     */
    private boolean failAction(RunData data)
        throws UnknownEntityException
    {
        // Retrieve an anonymous user
        data.setUser (TurbineSecurity.getAnonymousUser());
        setTarget(data, 
            data.getParameters().getString(ScarabConstants.TEMPLATE, "Login.vm"));
        return false;
    }
    
    /**
        calls doLogin()
    */
    public void doPerform( RunData data, TemplateContext context ) throws Exception
    {
        doLogin(data, context);
    }
}
