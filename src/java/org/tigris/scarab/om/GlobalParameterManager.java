package org.tigris.scarab.om;

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
 * software developed by CollabNet <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 *
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 *
 * 5. Products derived from this software may not use the "Tigris" or
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 * prior written permission of CollabNet.
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
 * individuals on behalf of CollabNet.
 */

import java.util.List;
import java.io.Serializable;
import org.apache.torque.om.Persistent;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

import org.tigris.scarab.util.ScarabException;

/** 
 * This class manages GlobalParameter objects.  Global is used a bit
 * loosely here.  Parameters can be module scoped as well.  for example,
 * the email parameters have a global set which is the default, if the
 * module does not provide alternatives.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: GlobalParameterManager.java,v 1.5 2003/04/17 22:56:32 jon Exp $
 */
public class GlobalParameterManager
    extends BaseGlobalParameterManager
{
    private static final String MANAGER_KEY = 
        DEFAULT_MANAGER_CLASS;
    private static final String GET_STRING = "getString";
    private static final String GET_BOOLEAN = "getBoolean";

    /**
     * Creates a new <code>GlobalParameterManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public GlobalParameterManager()
        throws TorqueException
    {
        super();
        setRegion(getClassName().replace('.', '_'));
    }

    protected Persistent putInstanceImpl(Persistent om)
        throws TorqueException
    {
        Persistent oldOm = super.putInstanceImpl(om);
        //Serializable obj = (Serializable)om;
        GlobalParameter gp = (GlobalParameter)om;
        Serializable moduleId = gp.getModuleId();
        String name = gp.getName();
        if (moduleId == null) 
        {
            // if altering a global parameter, its possible the 
            // module overrides are invalid.
            getMethodResult().removeAll(MANAGER_KEY, name);
            getMethodResult().removeAll(MANAGER_KEY, name);
        }
        else 
        {
            getMethodResult().remove(MANAGER_KEY, name, GET_BOOLEAN, 
                                     moduleId);
            getMethodResult().remove(MANAGER_KEY, name, GET_STRING, 
                                     moduleId);
        }
/*
    DEBUGGING
        if (oldOm == null) 
        {
        System.out.println("first put of value " + name + " to " + gp.getValue());
            
        }
        else 
        {
        System.out.println("changing value of " + name + " from " + ((GlobalParameter)oldOm).getValue() + " to " + gp.getValue());
            
        }
*/
        return oldOm;
    }

    private static GlobalParameter getInstance(String name)
        throws TorqueException, ScarabException
    {
        // try to get a global without a module
        GlobalParameter p = getInstance(name, null);
        if (p == null)
        {
            // get a local new instance
            p = getInstance();
            p.setName(name);
        }
        return p;
    }

    private static GlobalParameter getInstance(String name, Module module)
        throws TorqueException, ScarabException
    {
        GlobalParameter result = null;
        Criteria crit = new Criteria();
        crit.add(GlobalParameterPeer.NAME, name);
        if (module == null) 
        {
            crit.add(GlobalParameterPeer.MODULE_ID, null);
        }
        else 
        {
            crit.add(GlobalParameterPeer.MODULE_ID, module.getModuleId());
        }
        List parameters = GlobalParameterPeer.doSelect(crit);
        if (!parameters.isEmpty()) 
        {
            result = (GlobalParameter)parameters.get(0);
        }
        return result;
    }

    public static String getString(String name)
        throws TorqueException, ScarabException
    {
        // we do not call getString(name, null) here because we do
        // not want to cache results for every module if the parameter
        // is global.
        String result = null;
        // reversing order because we want to be able to invalidate based
        // on the parameter name, not the method name.
        Object obj = getMethodResult().get(MANAGER_KEY, name, GET_STRING); 
        if (obj == null) 
        {
            result = getInstance(name).getValue();
            getMethodResult()
                .put(result, MANAGER_KEY, name, GET_STRING); 
        }
        else 
        {
            result = (String)obj;
        }
        return result;
    }

    public static String getString(String name, Module module)
        throws TorqueException, ScarabException
    {
        String result = null;
        if (module == null) 
        {
            result = getString(name);
        }
        else 
        {
            Object obj = getMethodResult()
                .get(MANAGER_KEY, name, GET_STRING, module); 
            if (obj == null) 
            {
                GlobalParameter p = getInstance(name, module);
                if (p == null)
                {
                    // use global default
                    result = getString(name);
                }
                else 
                {
                    result = p.getValue();
                    getMethodResult()
                        .put(result, MANAGER_KEY, name, GET_STRING, module); 
                }
            }
            else 
            {
                result = (String)obj;
            }
        }
        return result;
    }

    public static void setString(String name, String value)
        throws Exception
    {
        GlobalParameter p = getInstance(name);
        p.setValue(value);
        p.save();
    }

    public static void setString(String name, Module module, String value)
        throws Exception
    {
        if (module == null) 
        {
            setString(name, value);
        }
        else 
        {
            GlobalParameter p = getInstance(name, module);
            if (p == null) 
            {
                p = getInstance(name).copy();
                p.setModuleId(module.getModuleId());
            }
            p.setValue(value);
            p.save();
        }
    }

    public static boolean getBoolean(String name)
        throws TorqueException, ScarabException
    {
        // we do not call getBoolean(name, null) here because we do
        // not want to cache results for every module if the parameter
        // is global.
        Boolean result = null;
        Object obj = getMethodResult().get(MANAGER_KEY, name, GET_BOOLEAN); 
        if (obj == null) 
        {
            result = ("T".equals(getInstance(name).getValue())) ? 
                Boolean.TRUE : Boolean.FALSE;
            getMethodResult()
                .put(result, MANAGER_KEY, name, GET_BOOLEAN); 
        }
        else 
        {
            result = (Boolean)obj;
        }
        return result.booleanValue();
    }

    public static boolean getBoolean(String name, Module module)
        throws TorqueException, ScarabException
    {
        boolean b = false;
        if (module == null) 
        {
            b = getBoolean(name);
        }
        else 
        {
            Object obj = getMethodResult()
                .get(MANAGER_KEY, name, GET_BOOLEAN, module); 
            if (obj == null) 
            {
                GlobalParameter p = getInstance(name, module);
                if (p == null)
                {
                    // use global default
                    b = getBoolean(name);
                }
                else 
                {
                    b = "T".equals(p.getValue());
                    getMethodResult().put((b ? Boolean.TRUE : Boolean.FALSE), 
                        MANAGER_KEY, name, GET_BOOLEAN, module);
                }
            }
            else 
            {
                b = ((Boolean)obj).booleanValue();
            }   
        }
        return b;
    }

    public static void setBoolean(String name, boolean value)
        throws Exception
    {
        setString(name, (value ? "T" : "F"));
    }

    public static void setBoolean(String name, Module module, boolean value)
        throws Exception
    {
        setString(name, module, (value ? "T" : "F"));
    }
}
