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
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.fulcrum.security.TurbineSecurity;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.screens.ModuleSwitchingLink;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.AnonymousUserUtil;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.actions.base.ScarabTemplateAction;

/**
 * This class is responsible for Logging a user out of the system.
 *    
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: Logout.java,v 1.16 2005/01/04 01:31:01 dabbous Exp $
 */
public class Logout extends ScarabTemplateAction
{
    /**
     * Logs out the currently logged-in user. Only sets the confirmation
     * message if there was a user previously logged in.
     */
    public void doLogout(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        boolean bWasLoggedIn = data.getUserFromSession() != null && !((ScarabUser)data.getUserFromSession()).isUserAnonymous();
        scarabR.setCurrentModule(null);
        data.getParameters().remove(ScarabConstants.CURRENT_MODULE);
        data.setACL(null);
        //data.setUser(TurbineSecurity.getAnonymousUser());
        AnonymousUserUtil.anonymousLogin(data);
        context.put("modulelink", new ModuleSwitchingLink(data));
        
        data.save();
        if (bWasLoggedIn)
            scarabR.setConfirmMessage(L10NKeySet.YouHaveBeenLoggedOut);
        setTarget(data, "Login.vm");
    }

    /**
     * @see #doLogout(RunData, TemplateContext)
     */
    public void doPerform(RunData data, TemplateContext context)
        throws Exception
    {
        doLogout(data, context);
    }
}
