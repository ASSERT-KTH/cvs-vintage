package org.tigris.scarab.workflow;

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

import java.util.List;

import org.apache.turbine.Turbine;
import org.tigris.scarab.util.ScarabException;

/**
 * This class retrieves the appropriate workflow tool. The DefaultWorkflow
 * is a noop implementation of workflow that always returns true.
 *
 * @author <a href="mailto:elicia@tigris.org">Elicia David</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @version $Id: WorkflowFactory.java,v 1.10 2003/03/15 21:56:59 jon Exp $
 */
public class WorkflowFactory 
{
    /** the default is false */
    private static boolean forceUseDefault = false;

    /**
     * This is used when you want to force the workflow to 
     * use the default workflow and override whatever is configured.
     * Set it to true to override and false to turn that off again.
     * This is useful during xml import to turn off workflow validation
     * in case the import process doesn't know anything about the 
     * workflow process.
     */
    public static void setForceUseDefault(boolean value)
    {
        forceUseDefault = value;
    }

    /**
     * This is used when you want to force the workflow to 
     * use the default workflow and override whatever is configured.
     * Set it to true to override and false to turn that off again.
     * This is useful during xml import to turn off workflow validation
     * in case the import process doesn't know anything about the 
     * workflow process.
     */
    public static boolean getForceUseDefault()
    {
        return forceUseDefault;
    }

    /**
     * Creates a new instance of the configured {@link
     * org.tigris.scarab.workflow.Workflow} implementation, defaulting
     * to {@link org.tigris.scarab.workflow.DefaultWorkflow} if not
     * specified.
     *
     * @return A <code>Workflow</code> instance.
     * @exception ScarabException Trouble creating
     * <code>Workflow</code>.
     */
    public static Workflow getInstance() throws ScarabException
    {
        Workflow wf = null;
        try
        {
            if (forceUseDefault)
            {
                wf = (Workflow) DefaultWorkflow.class.newInstance();
            }
            else
            {
                List classNames = Turbine.getConfiguration()
                    .getVector("scarab.workflow.classname");
                // Satisfy a strange case where one needs to append their
                // own configuration to the properties file and cannot 
                // easily remove the existing one. so, take the second
                // instance...
                String className = null;
                if (classNames.size() > 1)
                {
                    className = (String) classNames.get(1);
                }
                else
                {
                    className = (String) classNames.get(0);
                }
    
                Class wfClass = (className != null ? Class.forName(className) :
                                 DefaultWorkflow.class);
                wf = (Workflow) wfClass.newInstance();
            }
        }
        catch (Exception e)
        {
            throw new ScarabException(e);
        }
        return wf;
    }
}
