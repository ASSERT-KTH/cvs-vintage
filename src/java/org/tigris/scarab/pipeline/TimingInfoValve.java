package org.tigris.scarab.pipeline;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
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

import java.io.IOException;
import org.apache.turbine.RunData;
import org.apache.turbine.TurbineException;
import org.apache.turbine.pipeline.AbstractValve;
import org.apache.turbine.ValveContext;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.Module;
import org.tigris.scarab.tools.ScarabRequestTool;

/**
 * This valve stores the system time in the request object on first invocation
 * and reports the elapsed time on later invocations.  It generally would be
 * set as the first and last valve in a pipeline.  This valve also sets a
 * large response buffer, so that timing results are those of the server.
 * This will actually make the system appear slower to clients so it should
 * not be used in a production system.  You need to compile this valve
 * with DEBUG=true for it to be functional.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: TimingInfoValve.java,v 1.4 2003/03/27 23:59:27 jon Exp $
 */
public class TimingInfoValve 
    extends AbstractValve
{
    private static final Logger LOG = 
        Logger.getLogger(TimingInfoValve.class);
        
    private static final String KEY = 
        TimingInfoValve.class.getName() + ".start";

    private static final boolean DEBUG = false;

    /**
     * @see org.apache.turbine.Valve#invoke(RunData, ValveContext)
     */
    public void invoke(RunData data, ValveContext context)
        throws IOException, TurbineException
    {
        if (DEBUG) 
        {
            Long start = (Long)data.getRequest().getAttribute(KEY);
            if (start == null) 
            {
                try 
                {
                    data.getResponse().setBufferSize(10000000);
                }
                catch (Exception e)
                {
                    LOG.debug("Could not set high buffer size so client may " +
                              "affect timing results.");
                }
                ((ScarabRequestTool)Module.getTemplateContext(data)
                    .get("scarabR")).startTimer();
                data.getRequest()
                    .setAttribute(KEY, new Long(System.currentTimeMillis()));
            }
            else
            {
                String s = "Action=" + data.getAction() + " and template=" + 
                    data.getTarget() + " took: " + 
                    (System.currentTimeMillis() - start.longValue()) + " ms";
                LOG.debug(s);
                try 
                {
                    data.getResponse().getWriter().println(s);        
                }
                catch (Exception ignore)
                {
                    // maybe this was a binary response?
                    // we still logged it, so ignore
                }
            }
        }
            
        // Pass control to the next Valve in the Pipeline
        context.invokeNext(data);
    }
}
