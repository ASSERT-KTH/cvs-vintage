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

// Turbine Stuff 
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.tool.IntakeTool;

import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.util.TurbineSecurityException;
import org.apache.fulcrum.template.TemplateEmail;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.actions.base.ScarabTemplateAction;

// FIXME: remove the methods that reference this
import org.tigris.scarab.om.ScarabUserImpl;
import org.tigris.scarab.om.ScarabUserImplPeer;

import org.xbill.DNS.Record;
import org.xbill.DNS.dns;
import org.xbill.DNS.Type;

/**
 * This class is responsible for dealing with the Register
 * Action.
 *   
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: Register.java,v 1.35 2003/03/04 17:27:18 jmcnally Exp $
 */
public class Register extends ScarabTemplateAction
{

    private boolean checkRFC2505(String email)
    {
        // try just the end portion of the domain
        String domain = getDomain(email);
        if (domain != null)
        {
            // try to find any A records for the domain
            Record[] records = dns.getRecords(domain, Type.A);
            if (records != null || records.length > 0)
            {
                return true;
            }
            // now try just the domain after the @
            // this is for domains like foo.co.uk
            String fullDomain = email.substring(email.indexOf('@')+1);
            records = dns.getRecords(fullDomain, Type.A);
            if (records != null || records.length > 0)
            {
                return true;
            }
            // now try to find any MX records for the domain
            records = dns.getRecords(domain, Type.MX);
            if (records != null || records.length > 0)
            {
                return true;
            }
            // now try to find any MX records for the fullDomain
            records = dns.getRecords(fullDomain, Type.MX);
            if (records != null || records.length > 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This manages clicking the "Register" button in the Register.vm
     * template. As a result, the user will go to the 
     * RegisterConfirm.vm screen.
     */
    public void doRegister(RunData data, TemplateContext context) 
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
                getScarabRequestTool(context).setAlertMessage("Sorry! There is an error in your session. " + 
                "Please close your browser and start over.");
                return;
            }

            String password = register.get("Password").toString();
            String passwordConfirm = register.get("PasswordConfirm").toString();

            // check to make sure the passwords match
            if (!password.equals(passwordConfirm))
            {
                setTarget(data, template);
                getScarabRequestTool(context).setAlertMessage("The password's you entered do not match!");
                return;
            }

            // get an anonymous user
            ScarabUser su = (ScarabUser) TurbineSecurity.getAnonymousUser();
            try
            {
                register.setProperties(su);
            }
            catch (Exception e)
            {
                setTarget(data, template);
                getScarabRequestTool(context).setAlertMessage(e.getMessage());
                return;
            }

            String email = su.getEmail();
            // check to see if the email is a valid domain (has A records)
            if (Turbine.getConfiguration()
                    .getBoolean("scarab.register.email.checkRFC2505", false))
            {
                if (!checkRFC2505(email))
                {
                    setTarget(data, template);
                    getScarabRequestTool(context).setAlertMessage(
                        "Sorry, the email you submitted (" + email + ") " + 
                        "does not have a DNS A or MX record defined. " + 
                        "It is likely that the domain is invalid and that we cannot send you email. " + 
                        "Please see ftp://ftp.isi.edu/in-notes/rfc2505.txt for more details. " + 
                        "Please try another email address or contact your system administrator.");
                    return;
                }
            }
            String[] badEmails = Turbine.getConfiguration().getStringArray("scarab.register.email.badEmails");
            if (badEmails != null && badEmails.length > 0)
            {
                for (int i=0;i<badEmails.length;i++)
                {
                    if (email.equalsIgnoreCase(badEmails[i]))
                    {
                        setTarget(data, template);
                        getScarabRequestTool(context).setAlertMessage(
                        "Sorry, you have attempted to register with a known invalid email: [" + email + 
                        "]. Please try another.");
                        return;
                    }
                }
            }
            
            // check to see if the user already exists
            if (ScarabUserImplPeer.checkExists(su))
            {
                setTarget(data, template);
                getScarabRequestTool(context).setAlertMessage(
                    "Sorry, a user with that user name already exists!");
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

    public void doConfirmregistration(RunData data, TemplateContext context)
        throws Exception
    {
        String template = getCurrentTemplate(data);
        String nextTemplate = getNextTemplate(data);

        try
        {
            // pull the user object from the session
            ScarabUser su = (ScarabUser) data.getUser()
                .getTemp(ScarabConstants.SESSION_REGISTER);
            if (su == null)
            {
                // assign the template to the cancel template, not the 
                // current template
                template = getCancelTemplate(data, "Register.vm");
                throw new Exception(
                    "Unable to retrive user object from session.");
            }

            try
            {
                // attempt to create a new user!
                su.createNewUser();
            }
            catch (org.apache.fulcrum.security.util.EntityExistsException e)
            {
                getScarabRequestTool(context).setAlertMessage(e.getMessage());
                setTarget(data, "Confirm.vm");
                return;
            }

            // grab the ScarabRequestTool object so that we can populate the  
            // User object for redisplay of the form data on the screen
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            if (scarabR != null)
            {
                scarabR.setUser(su);
            }
            
            // send an email that is for confirming the registration
            TemplateEmail te = new TemplateEmail();
            te.setContext(new ContextAdapter(context));
            te.setTo(su.getFirstName() + " " + su.getLastName(), su.getEmail());
            te.setFrom(
                Turbine.getConfiguration()
                    .getString("scarab.email.register.fromName",
                                "Scarab System"), 
                Turbine.getConfiguration()
                    .getString("scarab.email.register.fromAddress",
                                "register@localhost"));
            te.setSubject(
                Turbine.getConfiguration()
                    .getString("scarab.email.register.subject",
                               "Account Confirmation"));
            te.setTemplate(
                Turbine.getConfiguration()
                    .getString("scarab.email.register.template",
                               "Confirmation.vm"));
            te.send();

            // set the next template on success
            setTarget(data, nextTemplate);
        }
        catch (Exception e)
        {
            setTarget(data, template);
            getScarabRequestTool(context).setAlertMessage(e.getMessage());
            Log.get().error(e);
            return;
        }
    }

    /**
     * returns you to Register.vm
     */
    public void doBack(RunData data, TemplateContext context) 
        throws Exception
    {
        // set the template to the template that we should be going back to
        setTarget(data, data.getParameters().getString(
                ScarabConstants.CANCEL_TEMPLATE, "Register.vm"));
    }

    /**
     * calls doRegisterConfirm()
     */
    public void doPerform(RunData data, TemplateContext context) 
        throws Exception
    {
        doConfirmregistration(data, context);
    }

    /**
     * This manages clicking the Confirm button in the Confirm.vm
     * template. As a result, this will end up sending
     * the user to the Confirm screen.
     */
    public void doConfirm(RunData data, TemplateContext context) 
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

            if (register == null)
            {
                getScarabRequestTool(context).setAlertMessage(
                    "Register group is null, please report this error.");
                return;
            }
            String username = null;
            String confirm = null;
            Field usernameField = register.get("UserName");
            Field confirmField = register.get("Confirm");
            if (usernameField == null)
            {
                getScarabRequestTool(context).setAlertMessage(
                    "Username field is null, please report this error.");
                return;
            }
            else if (confirmField == null)
            {
                getScarabRequestTool(context).setAlertMessage(
                    "Confirm field is null, please report this error.");
                return;
            }
            username = usernameField.toString();
            confirm = confirmField.toString();

            // This reference to ScarabUserImpl is ok because this action
            // is specific to use with that implementation.
            if (ScarabUserImpl.checkConfirmationCode(username, confirm))
            {
                // update the database to confirm the user
                if(ScarabUserImpl.confirmUser(username))
                {
                    // NO PROBLEMS! :-)
                    ScarabUser confirmedUser = (ScarabUser)
                                    TurbineSecurity.getUser(username);
                    // we set this to false and make people login again
                    // because of this issue:
                    // http://scarab.tigris.org/issues/show_bug.cgi?id=115
                    // there may be a better way, but given that on the confirm
                    // screen, we aren't asking for a password and checkConfirmationCode
                    // will return true if someone is already confirmed, 
                    // we need to do this for security purposes.
                    confirmedUser.setHasLoggedIn(Boolean.FALSE);
                    data.setUser(confirmedUser);
                    data.save();
    
                    getScarabRequestTool(context).setConfirmMessage(
                        "Your account has been confirmed. Welcome to Scarab! " + 
                        "Please login now.");
                    setTarget(data, nextTemplate);
                }
                else
                {
                    getScarabRequestTool(context).setAlertMessage("Your account has not been confirmed. " + 
                                    "There has been an error.");
                    setTarget(data, template);
                }
            }
            else // we don't have confirmation! :-(
            {
                getScarabRequestTool(context).setAlertMessage("Sorry, that user name and/or confirmation"
                                + "code is invalid.");
                setTarget(data, template);
            }
        }
    }

    /**
     * This manages clicking the "Resend code" button 
     * in the Confirm.vm template.
     */
    public void doResendconfirmationcode(RunData data, TemplateContext context)
        throws Exception
    {
        String template = getCurrentTemplate(data, null);

        try
        {
            Object user = data
                            .getUser()
                            .getTemp(ScarabConstants.SESSION_REGISTER);
 
            IntakeTool intake = getIntakeTool(context);
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
        
            if (register == null)
            {
                getScarabRequestTool(context).setAlertMessage(
                    "Register group is null, please report this error.");
                return;
            }
            String username = register.get("UserName").toString();
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            try
            {
                // Authenticate the user and get the object.
                user = TurbineSecurity.getUser(username);

                // grab the ScarabRequestTool object so that we can 
                // populate the User object for redisplay of the form 
                // data on the screen
                if (scarabR != null)
                {
                    scarabR.setUser((ScarabUser) user);
                }
            }
            catch (TurbineSecurityException e)
            {
                scarabR.setAlertMessage("Invalid username.");
                Log.get().error ("RegisterConfirm: ", e);
                return;
            }
        
            // send an email that is for confirming the registration
            sendConfirmationEmail((ScarabUser) user, context);
            scarabR.setConfirmMessage("Confirmation code sent!");

            // set the next template on success
            data.getUser().setTemp(ScarabConstants.SESSION_REGISTER, user);
            intake.remove(register);

            setTarget(data, "Confirm.vm");
        }
        catch (Exception e)
        {
            setTarget(data, template);
            getScarabRequestTool(context).setAlertMessage (e.getMessage());
            Log.get().error(e);
            return;
        }
    }

    /**
     * If email: jon@foo.bar.com then return bar.com
     */
    private String getDomain(String email)
    {
        String result = null;
        char[] emailChar = email.toCharArray();
        int dotCount=0;
        for (int i=emailChar.length-1;i>=0;i--)
        {
            if (emailChar[i] == '.')
            {
                dotCount++;
            }
            if (dotCount == 2 || emailChar[i] == '@')
            {
                result = email.substring(i+1,email.length());
                break;
            }
        }
        return result;
    }
    
    /**
     * Send the confirmation code to the given user.
     */
    private void sendConfirmationEmail(ScarabUser su, TemplateContext context)
        throws Exception
    {
        TemplateEmail te = new TemplateEmail();
        te.setContext(new ContextAdapter(context));
        te.setTo(su.getFirstName() + " " + su.getLastName(), su.getEmail());
        te.setFrom(
            Turbine.getConfiguration()
                .getString("scarab.email.register.fromName",
                           "Scarab System"), 
            Turbine.getConfiguration()
                .getString("scarab.email.register.fromAddress",
                           "register@localhost"));
        te.setSubject(
            Turbine.getConfiguration()
                .getString("scarab.email.register.subject",
                           "Account Confirmation"));
        te.setTemplate(
            Turbine.getConfiguration()
                .getString("scarab.email.register.template",
                           "email/Confirmation.vm"));
        te.send();        
    }
}
