package org.tigris.scarab.actions.admin;

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


// JDK stuff
import java.util.Calendar;

// Turbine Stuff
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.Log;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.User;


// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImpl;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.actions.base.ScarabTemplateAction;

/**
 * This class is responsible for dealing with the Register
 * Action.
 *
 * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
 * @version $Id: EditUser.java,v 1.1 2001/11/21 23:04:48 jon Exp $
 */
public class EditUser extends ScarabTemplateAction
{
    public void doEdituser( RunData data, TemplateContext context ) throws Exception
    {
	String template = getCurrentTemplate(data, null);
	String nextTemplate = getNextTemplate(data, template);
	String state = data.getParameters().getString("state");
	ScarabUser su = null;
	
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
	    
	    
	    
	    
	    // if we are adding a new user, make sure that the email addres isn't already in use
	    if (state.equals("execadduser"))
	    {
		su  = (ScarabUser) TurbineSecurity.getAnonymousUser();
		//su.setUserName(data.getParameters().getString("UserName"));
		su.setUserName(register.get("Email").toString());
		su.setFirstName(register.get("FirstName").toString());
		su.setLastName(register.get("LastName").toString());
		su.setEmail(register.get("Email").toString());
		su.setPassword(register.get("Password").toString().trim());
		
		if (ScarabUserImplPeer.checkExists(su))
		{
		    setTarget(data, template);
		    data.setMessage("Sorry, a user with that email address already exists!");
		    data.getParameters().setString("errorLast","true");
		    resetParameters(data);
		    return;
		}
	    }
	    
	    
	    // if we got here, then all must be good...
	    try
	    {
		// take action based on the state...
		if (state.equals("execadduser"))
		{
		    su.createNewUser();
		    ScarabUserImpl.confirmUser(register.get("Email").toString());
		    // force the user to change their password the first time they login
		    su.setPasswordExpire(Calendar.getInstance());
		    data.setMessage("SUCCESS: a new user was created [username: " + register.get("Email").toString() +"]");
		    data.getParameters().setString("state","showadduser");
		    data.getParameters().setString("lastAction","addeduser");
		    
		    setTarget(data, nextTemplate);
		    return;
		}
		else if (state.equals("execedituser"))
		{
		    su = (ScarabUser) TurbineSecurity.getUser(data.getParameters().getString("username"));
		    if ((su != null) && (register != null))
		    {
			// update the first name, last name, email and username
			su.setFirstName(register.get("FirstName").toString());
			su.setLastName(register.get("LastName").toString());
			
			String newEmail = register.get("Email").toString();
			if (!newEmail.equals(data.getParameters().getString("username")))
			{
			    su.setEmail(newEmail);
			    //su.setUserName(newEmail);
			    
			    if (!ScarabUserImplPeer.checkExists(su))
			    {
				setTarget(data, template);
				data.setMessage("Sorry, a user with that email address [" + newEmail + "] already exists!");
				resetParameters(data);
				return;
			    }
			}
			
			
			// only update their password if the field is non-empty, and then make sure they change the password at next login
			String password = data.getParameters().getString("editpassword");
			if ((password != null) && (!password.trim().equals("")))
			{
			    su.setPassword(password.trim());
			    su.setPasswordExpire(Calendar.getInstance());
			    
			}
			TurbineSecurity.saveUser(su);
			
			data.setMessage("SUCCESS: changes to the user have been saved [username: " + register.get("Email").toString() +"]");
			data.getParameters().setString("state","showedituser");
			data.getParameters().setString("lastAction","editeduser");
			
			setTarget(data, nextTemplate);
			return;
			
		    }
		    else
		    {
			
			data.setMessage("ERROR: couldn't retrieve the user from the DB [username: " + register.get("Email").toString() +"]");
			data.getParameters().setString("state","showedituser");
			
		    }
		}
		
		
	    }
	    catch (Exception e)
	    {
		setTarget(data, template);
		data.getParameters().setString("lastAction","");
		data.setMessage (e.getMessage());
		Log.error(e);
		resetParameters(data);
		return;
	    }
	    
	}
	else
	{
	    resetParameters(data);
	    data.getParameters().setString("lastAction","");
	}
    }
    
    
    /**
     This manages clicking the Cancel button
     */
    public void doCancel( RunData data, TemplateContext context ) throws Exception
    {
	setTarget(data, data.getParameters().getString(ScarabConstants.CANCEL_TEMPLATE, "admin,AdminIndex.vm"));
    }
    
    /**
     calls doCancel()
     */
    public void doPerform( RunData data, TemplateContext context ) throws Exception
    {
	doCancel(data, context);
    }
    
    
    private void resetParameters(RunData data) {
	// re-set the state appropriately
	String state = data.getParameters().getString("state");
	if (state.equals("execadduser"))
	{
	    data.getParameters().setString("state","showadduser");
	}
	else if (state.equals("execedituser"))
	{
	    data.getParameters().setString("state","showedituser");
	}
	
    }
    
}
