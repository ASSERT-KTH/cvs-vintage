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

import java.util.List;

import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.TurbineServices;

import org.apache.torque.om.ObjectKey;
import org.apache.torque.util.Criteria;


import org.tigris.scarab.services.AbstractOMService;
import org.tigris.scarab.util.ScarabException;

/**
 * This is the implementation of a ModuleService. It knows how to
 * instantiate the right Module object depending on what is in the
 * Scarab.properties file.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jon@collab.net">John McNally</a>
 * @version $Id: AbstractModuleService.java,v 1.1 2001/11/01 00:20:11 jmcnally Exp $
 */
public abstract class AbstractModuleService 
    extends AbstractOMService 
    implements ModuleService
{
    /**
     * @see org.tigris.scarab.services.module.ModuleService#getInstance()
     */
    public ModuleEntity getInstance()
        throws Exception
    {
        return (ModuleEntity) getOMInstance();
    }

    /**
     * Return an instance of Module based on the passed in module id
     */
    public ModuleEntity getInstance(ObjectKey id) 
        throws Exception
    {
        return (ModuleEntity) getOMInstance(id);
    }

    /**
     * Gets a list of ModuleEntities based on id's.
     *
     * @param moduleIds a <code>NumberKey[]</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getModules(ObjectKey[] moduleIds) 
        throws Exception
    {
        return getOMs(moduleIds);
    }

    /**
     * Gets a list of ModuleEntities based on id's.
     *
     * @param moduleIds a <code>NumberKey[]</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getModules(List moduleIds) 
        throws Exception
    {
        return getOMs(moduleIds);
    }

    /**
     *   check for a duplicate project name
     */
    public abstract boolean exists(ModuleEntity module)
        throws Exception;
}
