package org.tigris.scarab.actions.base;

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
import org.apache.turbine.TemplateContext;
import org.apache.turbine.TemplateSecureAction;
import org.apache.turbine.tool.IntakeTool;

// Scarab Stuff
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.screens.Default;

/**
 * This is a badly named class which is essentially equivalent to the 
 * Default.java Screen except that it has a few helper methods.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: RequireLoginFirstAction.java,v 1.14 2001/11/02 22:49:45 elicia Exp $    
 */
public abstract class RequireLoginFirstAction extends TemplateSecureAction
{
    /**
     * sets the template to template.login if the user hasn't logged in yet
     */
    protected boolean isAuthorized( RunData data ) throws Exception
    {
        return Default.checkAuthorized(data);
    }

    /**
     * Helper method to retrieve the IntakeTool from the Context
     */
    public IntakeTool getIntakeTool(TemplateContext context)
    {
        return (IntakeTool)context.get(ScarabConstants.INTAKE_TOOL);
    }

    /**
     * Helper method to retrieve the ScarabRequestTool from the Context
     */
    public ScarabRequestTool getScarabRequestTool(TemplateContext context)
    {
        return (ScarabRequestTool)context
            .get(ScarabConstants.SCARAB_REQUEST_TOOL);
    }

    /**
     * Returns the current template that is being executed, otherwisse
     * it returns null
     */
    public String getCurrentTemplate(RunData data)
    {
        return data.getParameters()
                            .getString(ScarabConstants.TEMPLATE, null);
    }

    /**
     * Returns the current template that is being executed, otherwisse
     * it returns defaultValue.
     */
    public String getCurrentTemplate(RunData data, String defaultValue)
    {
        return data.getParameters()
                            .getString(ScarabConstants.TEMPLATE, defaultValue);
    }

    /**
     * Returns the nextTemplate to be executed. Otherwise returns null.
     */
    public String getNextTemplate(RunData data)
    {
        return data.getParameters()
                            .getString(ScarabConstants.NEXT_TEMPLATE, null);
    }

    /**
     * Returns the nextTemplate to be executed. Otherwise returns defaultValue.
     */
    public String getNextTemplate(RunData data, String defaultValue)
    {
        return data.getParameters()
                            .getString(ScarabConstants.NEXT_TEMPLATE, defaultValue);
    }

    /**
     * Returns the cancelTemplate to be executed. Otherwise returns null.
     */
    public String getCancelTemplate(RunData data)
    {
        return data.getParameters()
                            .getString(ScarabConstants.CANCEL_TEMPLATE, null);
    }

    /**
     * Returns the cancelTemplate to be executed. Otherwise returns defaultValue.
     */
    public String getCancelTemplate(RunData data, String defaultValue)
    {
        return data.getParameters()
                            .getString(ScarabConstants.CANCEL_TEMPLATE, defaultValue);
    }

    /**
     * Returns the backTemplate to be executed. Otherwise returns null.
     */
    public String getBackTemplate(RunData data)
    {
        return data.getParameters()
                            .getString(ScarabConstants.BACK_TEMPLATE, null);
    }

    /**
     * Returns the backTemplate to be executed. Otherwise returns defaultValue.
     */
    public String getBackTemplate(RunData data, String defaultValue)
    {
        return data.getParameters()
                            .getString(ScarabConstants.BACK_TEMPLATE, defaultValue);
    }

    /**
     * Require people to implement this method
     */
    public abstract void doPerform( RunData data, TemplateContext context )
        throws Exception;

    public void doGotocancel( RunData data, TemplateContext context )
        throws Exception
    {
        setTarget(data, getCancelTemplate(data));            
    }

    public void doRefresh( RunData data, TemplateContext context )
        throws Exception
    {
        setTarget(data, getCurrentTemplate(data));            
    }
        
    public void doGonext( RunData data, TemplateContext context )
        throws Exception
    {
        setTarget(data, getNextTemplate(data));            
    }
}
