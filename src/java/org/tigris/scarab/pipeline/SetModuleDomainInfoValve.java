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

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Module;

import org.apache.commons.lang.StringUtils;

/**
 * This valve will attempt to set the module domain information.
 * It isn't a 100% necessary valve and will probably be removed
 * in the next revision because this information will be defined
 * automatically when the Module is created. The purpose of this
 * valvue is to define this information once since it doesn't exist
 * in upgraded databases.
 *
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @version $Id: SetModuleDomainInfoValve.java,v 1.2 2003/04/21 20:08:59 jon Exp $
 */
public class SetModuleDomainInfoValve
    extends AbstractValve
{
/*
    public SetModuleDomainInfoValve()
    {
    }
*/
    /**
     * @see org.apache.turbine.Valve#invoke(RunData, ValveContext)
     */
    public void invoke(RunData data, ValveContext context)
        throws IOException, TurbineException
    {
        Module module = null;
        ScarabUser user = (ScarabUser)data.getUser();
        if (user != null)
        {
            module = user.getCurrentModule();
            if (module != null)
            {
                try
                {
                    setModuleDomainInfo(data, module);
                }
                catch (Exception e)
                {
                    throw new TurbineException(e);
                }
            }
        }

        // Pass control to the next Valve in the Pipeline
        context.invokeNext(data);
    }
    
    private void setModuleDomainInfo(RunData data, Module module)
        throws Exception
    {
        boolean hasChanges = false;
        if (StringUtils.isEmpty(module.getDomain()))
        {
            module.setDomain(data.getServerName());
            hasChanges = true;
        }
        if (StringUtils.isEmpty(module.getPort()))
        {
            module.setPort(String.valueOf(data.getServerPort()));
            hasChanges = true;
        }
        if (StringUtils.isEmpty(module.getScheme()))
        {
            module.setScheme(data.getServerScheme());
            hasChanges = true;
        }
        if (hasChanges)
        {
            module.save();
        }
    }
}
