package org.tigris.scarab.util;

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

// Turbine
import org.apache.turbine.util.template.TemplateLink;
import org.apache.turbine.RunData;
import org.apache.turbine.util.DynamicURI;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.services.pool.InitableRecyclable;

// Scarab
import org.tigris.scarab.om.*;

/**
    This class adds a ModuleManager.CURRENT_PROJECT to every link. This class is added
    into the context to replace the $link that Turbine adds.
    
    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: ScarabLink.java,v 1.6 2001/07/17 01:52:07 jmcnally Exp $
*/
public class ScarabLink extends TemplateLink
                        implements InitableRecyclable
{
    private RunData data;

    /**
     * Constructor.
     *
     * @param data A Turbine RunData object.
     */
    public ScarabLink()
    {
    }


    /**
     * This will initialise a TemplateLink object that was
     * constructed with the default constructor (ApplicationTool
     * method).
     *
     * @param data assumed to be a RunData object
     */
    public void init(Object data)
    {
        // we just blithely cast to RunData as if another object
        // or null is passed in we'll throw an appropriate runtime
        // exception.
        super.init(data);
        this.data = (RunData)data;
    }

    /**
     * Sets the template variable used by the Template Service.
     *
     * @param t A String with the template name.
     * @return A TemplateLink.
     */
    public TemplateLink setPage(String t)
    {
        /*
        String project_id = data.getParameters().getString(ModuleManager.CURRENT_PROJECT, "");
        if (project_id.length() > 0)
            addPathInfo(ModuleManager.CURRENT_PROJECT, project_id);
        addPathInfo("template",t);
        */
        super.setPage(t);
        return this;
    }

    /**
     * Returns the name of the template that is being being processed
     */
    public String getCurrentView()
    {
        return data.getTemplateInfo().getScreenTemplate().replace('/', ',');
    }

    public ScarabLink setPathInfo(String key, String value)
    {
        removePathInfo(key);
        addPathInfo(key, value);
        return this;
    }

    public ScarabLink addPathInfo(String key, ParameterParser pp)
    {
        addPathInfo(key, pp);
        return this;
    }
    
    // ****************************************************************
    // ****************************************************************
    // Implementation of Recyclable
    // ****************************************************************
    // ****************************************************************

    private boolean disposed = false;

    /**
     * Recycles the object by removing its disposed flag.
     */
    public void recycle()
    {
        disposed = false;
    }

    /**
     * Disposes the object by setting its disposed flag.
     */
    public void dispose()
    {
        this.data = null;
        disposed = true;
    }

    /**
     * Checks whether the object is disposed.
     *
     * @return true, if the object is disposed.
     */
    public boolean isDisposed()
    {
        return disposed;
    }    
}    
