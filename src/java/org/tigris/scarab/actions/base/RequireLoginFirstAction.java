package org.tigris.scarab.actions.base;

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

// Java Stuff
import java.util.Stack;
import java.util.HashMap;

// Turbine Stuff
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.TemplateSecureAction;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;

// Scarab Stuff
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.screens.Default;
import org.tigris.scarab.om.ScarabUser;

/**
 * This is a badly named class which is essentially equivalent to the 
 * Default.java Screen except that it has a few helper methods.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: RequireLoginFirstAction.java,v 1.19 2002/01/18 22:26:05 jon Exp $    
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
     * Returns the cancelTemplate to be executed. 
     * Otherwise returns defaultValue.
     */
    public String getCancelTemplate(RunData data, String defaultValue)
    {
        return data.getParameters()
                   .getString(ScarabConstants.CANCEL_TEMPLATE, 
                              defaultValue);
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
     * Returns the backTemplate to be executed. 
     * Otherwise returns defaultValue.
     */
    public String getBackTemplate(RunData data, String defaultValue)
    {
        return data.getParameters()
                   .getString(ScarabConstants.BACK_TEMPLATE, defaultValue);
    }

    /**
     * Returns the other template that is being executed, otherwise
     * it returns null.
     */
    public String getOtherTemplate(RunData data)
    {
        return data.getParameters()
                   .getString(ScarabConstants.OTHER_TEMPLATE);
    }

    public void doSave( RunData data, TemplateContext context )
        throws Exception
    {
    }

    public void doDone( RunData data, TemplateContext context )
        throws Exception
    {
        doSave(data, context);
        doCancel(data, context);
    }

    public void doGonext( RunData data, TemplateContext context )
        throws Exception
    {
        setTarget(data, getNextTemplate(data));            
    }

    public void doGotoothertemplate( RunData data, 
                                     TemplateContext context )
        throws Exception
    {
        data.getParameters().remove(ScarabConstants.CANCEL_TEMPLATE);
        data.getParameters().add(ScarabConstants.CANCEL_TEMPLATE, 
                                 data.getTarget());
        setTarget(data, getOtherTemplate(data));            
    }

    public void doRefresh( RunData data, TemplateContext context )
        throws Exception
    {
        setTarget(data, getCurrentTemplate(data));            
    }

    public void doReset( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        intake.removeAll();
        setTarget(data, getCurrentTemplate(data));            
    }
        
    public void doCancel( RunData data, TemplateContext context )
        throws Exception
    {
        String cancelTo = null;

        ScarabUser user = (ScarabUser)data.getUser();
        Stack cancelTargets = (Stack)user.getTemp("cancelTargets");
        cancelTargets.pop();
        String cancelPage = (String)cancelTargets.pop();
        if (cancelTargets.contains(cancelPage))
        {
            int cancelPageIndex = cancelTargets.indexOf(cancelPage);
            for (int i = cancelTargets.size(); i > (cancelPageIndex + 1); i--)
            {
               cancelTargets.pop();
            }
            cancelPage = (String)cancelTargets.pop();
        }

        data.setTarget(cancelPage);
    }

}
