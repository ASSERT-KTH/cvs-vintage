package org.tigris.scarab.services.module;

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

import org.tigris.scarab.om.ScarabModulePeer;

import org.apache.torque.om.ObjectKey;
import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.commons.util.StringUtils;
import org.apache.turbine.RunData;
import org.apache.torque.util.Criteria;

/**
 * This class has static methods for working with a Module object
 * <p>FIXME: {@link #getService()} and {@link #getInstance()} are
 * duplicate methods.  One should be deprecated.</p>
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ModuleManager.java,v 1.18 2001/11/01 00:20:11 jmcnally Exp $
 */
public abstract class ModuleManager
{
    /**
     * Retrieves an implementation of ModuleService, base on the settings in
     * TurbineResources.
     *
     * @return an implementation of ModuleService.
     */
    public static ModuleService getService()
    {
        return (ModuleService)TurbineServices.getInstance().
            getService(ModuleService.SERVICE_NAME);    
    }

    public static Class getOMClass()
        throws Exception
    {
        return getService().getOMClass();
    }

    public static ModuleEntity getInstance()
        throws Exception
    {
        return getService().getInstance();
    }

    public static ModuleEntity getInstance(ObjectKey id)
        throws Exception
    {
        return getService().getInstance(id);
    }

    public static boolean exists(ModuleEntity module)
        throws Exception
    {
        return getService().exists(module);
    }
}
