package org.tigris.scarab.util.xml;

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
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.digester.Rule;
import org.apache.commons.digester.Digester;
import org.apache.commons.util.GenerateUniqueId;
import org.apache.log4j.Category;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImpl;

import org.apache.fulcrum.security.TurbineSecurity;

/**
 * Handler for the xpath "scarab/user".
 *
 * @author <a href="mailto:kevin.minshull@bitonic.com">Kevin Minshull</a>
 * @author <a href="mailto:richard.han@bitonic.com">Richard Han</a>
 */
public class UserRule extends BaseRule 
{
    private static final int UNIQUE_ID_MAX_LEN = 10;
    
    public UserRule(Digester digester, String state, ArrayList userList)
    {
        super(digester, state, userList);
    }
    
    /**
     * This method is called when the end of a matching XML element
     * is encountered.
     */
    public void end()
        throws Exception
    {
        cat.debug("(" + state + ") user end()");
        super.doInsertionOrValidationAtEnd();
    }
    
    /**
     * handle creating the user.  sets the password to a temporary random password.
     * when the user signs in, they should just click on forgot password, and this
     * will reset their password so that they can access the application.  No roles
     * are granted to the individual, just a user account created.
     */
    protected void doInsertionAtEnd()
        throws Exception
    {
        String email = (String)digester.pop();
        String lastName = (String)digester.pop();
        String firstName = (String)digester.pop();
        
        ScarabUser user;
        try
        {
            user = (ScarabUser)TurbineSecurity.getUser(email);
        }
        catch (Exception e)
        {
            String tempPassword = GenerateUniqueId.getIdentifier();
            if (tempPassword.length() > UNIQUE_ID_MAX_LEN)
            {
                tempPassword = tempPassword.substring(0, UNIQUE_ID_MAX_LEN);
            }
            user  = (ScarabUser) TurbineSecurity.getAnonymousUser();
            user.setUserName(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPassword(tempPassword);
            user.createNewUser();
            ScarabUserImpl.confirmUser(email);
            user.setPasswordExpire(Calendar.getInstance());
        }
    }
    
    /**
     * handle the validation
     */
    protected void doValidationAtEnd()
        throws Exception
    {
        String email = (String)digester.pop();
        String lastName = (String)digester.pop();
        String firstName = (String)digester.pop();
        
        if (userList.contains(email))
        {
            throw new Exception("User: " + email + ", already defined");
        }
        
        userList.add(email);
    }
}
