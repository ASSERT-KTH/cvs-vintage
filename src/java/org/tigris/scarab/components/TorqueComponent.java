package org.tigris.scarab.components;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.torque.Torque;
import org.apache.torque.TorqueInstance;
import org.apache.turbine.Turbine;


/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
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

/**
 * Avalon component for Torque adjusted to the needs of Scarab.
 *
 * @author <a href="mailto:dabbous@saxess.com">Hussayn dabbous</a>
 * @version $Id: TorqueComponent.java,v 1.1 2004/12/03 21:58:46 dabbous Exp $
 */
public class TorqueComponent
        extends org.apache.torque.avalon.TorqueComponent
{
    public TorqueComponent()
    {
        super();
    }

    /**
     * Creates a new instance.
     *
     * @param torqueInstance The instance of the Torque core used by
     * this component.
     */
    protected TorqueComponent(TorqueInstance torqueInstance)
    {
        super(torqueInstance);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration)
            throws ConfigurationException
    {
        // we retrieve the configuration from Turbine runtime @see #initialize()
        getLogger().debug("configure(" + configuration + ")");
    }
    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize()
            throws Exception
    {
        getLogger().debug("initialize()");
        //return the configuration object with the torque values only.
        org.apache.commons.configuration.Configuration c = Turbine.getConfiguration();
        Torque.getInstance().init(c);
    }

}
