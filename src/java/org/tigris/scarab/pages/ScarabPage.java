package org.tigris.scarab.pages;

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
import org.apache.turbine.pipeline.ClassicPipeline;
import org.apache.turbine.modules.Module;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.torque.om.NumberKey;

// Scarab Stuff
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.services.module.ModuleManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImpl;


/**
    This class is responsible for building the Context up
    for the Default Page.

    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: ScarabPage.java,v 1.10 2001/08/15 05:17:36 jon Exp $
*/
public class ScarabPage extends ClassicPipeline
{
    /**
     * builds up the context for display of variables on the page.
     */
    public void preExecuteAction( RunData data ) 
        throws Exception 
    {
        //until we get the user and module set through normal application
        tempWorkAround(data, Module.getTemplateContext( data ));

        super.preExecuteAction(data);
    }

    /**
     * This method gives us a user with a current module.
     * It should be removed as soon as we have some way to set 
     * this within the application
     */
    public static void tempWorkAround( RunData data, 
                                TemplateContext context ) 
        throws Exception
    {

        ScarabRequestTool scarab = (ScarabRequestTool)
            context.get(ScarabConstants.SCARAB_REQUEST_TOOL);
          
        if ( data.getUser() == null ) 
        {
            ScarabUser user = (ScarabUser) TurbineSecurity.getAnonymousUser();
            // bad bad bad...
            ((ScarabUserImpl)user).setPrimaryKey(new NumberKey("2"));
            user.setUserName("workarounduser");
            scarab.setUser(user);
            data.setUser(user);
        }
          
        if ( ((ScarabUser)data.getUser()).getCurrentModule() == null ) 
        {
            ModuleEntity module = 
                ModuleManager.getInstance(
                    new NumberKey("5"));
            ((ScarabUser)data.getUser()).setCurrentModule(module);
        }
    }

    // a temporary fix for losing TemplateInfo !FIXME!
    public static String getScreenTemplate(RunData data)
    {
        String temp = data.getParameters().getString("template",null);
        if ( temp != null )
        {
            temp = temp.replace(',', '/');
        }
        return temp;
    }

}
