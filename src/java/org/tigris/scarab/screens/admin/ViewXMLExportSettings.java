package org.tigris.scarab.screens.admin;

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
import java.text.SimpleDateFormat;

// Turbine Stuff 
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;

// Scarab Stuff
import org.apache.turbine.modules.Module;
import org.tigris.scarab.screens.Default;

/**
 * Sends XML Export settings contents directly to the output stream.
 *
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @version $Id: ViewXMLExportSettings.java,v 1.2 2002/06/11 01:55:38 jon Exp $
 */
public class ViewXMLExportSettings extends Default
{
    private static final String format = "yyyy-MM-dd HH:mm:ss";

    /**
     * builds up the context for display of variables on the page.
     */
    public void doBuildTemplate( RunData data, TemplateContext context )
        throws Exception 
    {
        super.doBuildTemplate(data, context);

        // probably should use intake, but i'm being lazy for now cause
        // this is only three form variables and not worth the trouble...
        String downloadType = data.getParameters().getString("downloadtype");
        if (downloadType != null && downloadType.equals("1"))
        {
            data.getResponse().setContentType("text/plain");
        }
        else
        {
            data.getResponse().setContentType("application/octet-stream");
            String filename = data.getParameters().getString("filename");
            if (filename == null 
                || filename.length() == 0 
                || filename.indexOf('/') > 0
                || filename.indexOf(':') > 0
                || filename.indexOf(';') > 0)
            {
                filename = "scarab-settings-export.xml";
            }
            data.getResponse().setHeader("Content-Disposition", 
                "attachment; filename=" + filename);
        }

        String includeUsers = data.getParameters().getString("includeUsers");
        if (includeUsers != null && includeUsers.equals("1"))
        {
            context.put ("includeUsers", Boolean.TRUE);
        }
        else
        {
            context.put ("includeUsers", Boolean.FALSE);
        }
        
        context.put ("renderedFromScreen", Boolean.TRUE);
        context.put("sdf", new SimpleDateFormat(format));
        String result = 
            Module.handleRequest (context, "macros/XMLExportSettingsMacro.vm");
        data.getOut().write(result);
        context.remove ("renderedFromScreen");

        // we already sent the response, there is no target to render
        data.setTarget(null);
    }
}
