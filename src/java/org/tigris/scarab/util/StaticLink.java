package org.tigris.scarab.util;

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

// Turbine
import org.apache.turbine.RunData;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.fulcrum.pool.InitableRecyclable;


/**
 * A pull tool to render links to static content.
 *   
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: StaticLink.java,v 1.1 2002/02/13 05:18:23 jmcnally Exp $
 */
public class StaticLink
    implements InitableRecyclable, ApplicationTool
{
    private RunData data;
    private String path;

    /**
     * Constructor.
     */
    public StaticLink()
    {
    }

    /**
     * This will initialise a StaticLink object that was
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
        this.data = (RunData)data;
    }

    /**
     * Implementation of ApplicationTool interface.
     *
     */
    public void refresh()
    {
        path = null;
    }

    /**
     * Path to a file.  The path will be relative to the directory returned
     * by getPrefix().
     *
     * @param t A String with the file name.
     * @return A StaticLink.
     */
    public StaticLink setPath(String t)
    {
        path = t;
        return this;
    }
     
    /**
     * Prints out the url.
     *
     * @return a <code>String</code> url
     */
    public String toString()
    {
        String prefix = getPrefix();
        return new StringBuffer(prefix.length()+path.length())
            .append(prefix).append(path).toString();
    }

    /**
     * Prefix that will be added to the path given in setPage.  
     * This implementation returns the context path.
     *
     * @return a <code>String</code> value
     */
    protected String getPrefix()
    {
        return data.getRequest().getContextPath();
    }

    /**
     * Give subclasses access to the RunData, so they do not have to 
     * reimplement the pooling code, just to get at it.
     */
    protected RunData getRunData()
    {
        return data;
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
        data = null;
        refresh();
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
