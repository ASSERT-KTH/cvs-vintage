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

import org.apache.log4j.Logger;
import org.apache.torque.om.Persistent;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.Turbine;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.util.ScarabRuntimeException;


/** 
 * This class manages GlobalParameter objects.  Global is used a bit
 * loosely here.  Parameters can be module scoped as well.  for example,
 * the email parameters have a global set which is the default, if the
 * module does not provide alternatives.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: GlobalParameterManager.java,v 1.10 2005/01/11 22:55:01 dabbous Exp $
 */
public class GlobalParameterManager
    extends BaseGlobalParameterManager
{
    private static final String MANAGER_KEY = DEFAULT_MANAGER_CLASS;
    private static final String GET_STRING  = "getString";
    private static final String GET_BOOLEAN = "getBoolean";

    private static final Logger LOG = Logger.getLogger("org.tigris.scarab");

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
        throws TorqueException
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
        throws TorqueException
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

    public static String getString(String key)
        throws TorqueException
    {
        // we do not call getString(name, null) here because we do
        // not want to cache results for every module if the parameter
        // is global.
        String result = null;
        // reversing order because we want to be able to invalidate based
        // on the parameter name, not the method name.
        Object obj = getMethodResult().get(MANAGER_KEY, key, GET_STRING); 
        if (obj == null) 
        {
            result = getInstance(key).getValue();
            if (result == null)
            {
                result = Turbine.getConfiguration().getString(key);
                if (result == null || result.trim().length() == 0) 
                {
                    result = "";
                }
            }
            if(!result.equals(""))
            {
                getMethodResult().put(result, MANAGER_KEY, key, GET_STRING);
            }
        }
        else 
        {
            result = (String)obj;
        }
        return result;
    }

    public static String getString(String name, Module module)
        throws TorqueException
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

            getMethodResult().put(value, MANAGER_KEY, name, GET_STRING, module);
        }
    }

    public static boolean getBoolean(String name)
        throws TorqueException
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
        throws TorqueException
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
    
    /**
     * Recursively look up for the existence of the key.
     * Further details, @see #getBooleanFromHierarchy(String key, Module module, boolean def)
     * 
     * If no value was not found, return "def" instead.
     * 
     * @param key
     * @param module
     * @param def
     * @return
     */
    public static boolean getBooleanFromHierarchy(String key, Module module, boolean def)
    {
        String defAsString = (def)? "T":"F";
        String bp = getStringFromHierarchy(key,module, defAsString );

        // bp is "[T|F] when it comes from the database, 
        // or [true|false] when it comes from Turbine
        boolean result = (bp.equals("T") || bp.equals("true"))? true:false;

        return result;
    }

    /**
     * Recursively look up for the existence of the key in the
     * module hierarchy. Backtrack towards the module root.
     * If no value was found, check for the existence of a 
     * module-independent global parameter. 
     * If still no value found, check for the Turbine
     * configuration property with the same key. 
     * If still no definition found, return the parameter 
     * "def" instead.
     * 
     * @param key
     * @param module
     * @param def
     * @return
     */
    public static String getStringFromHierarchy(String key, Module module, String def)
    {
        String result = null; 
        Module me = module;
        try
        {
            do 
            {
                Object obj = getMethodResult().get(MANAGER_KEY, key, GET_STRING, me); 
                if (obj == null) 
                {
                    GlobalParameter p = getInstance(key, me);
                    if(p != null)
                    {
                        result = p.getValue();
                        getMethodResult()
                            .put(result, MANAGER_KEY, key, GET_STRING, me); 
                    }
                }
                else 
                {
                    result = (String)obj;
                }
                Module parent = me.getParent();
                if(parent==me)
                {
                    break;
                }
                me = parent;
            } while (result==null || result.equals(""));

            if(result==null || result.equals(""))
            {
                // here try to retrieve the module independent parameter,
                // or as last resort get it from the Turbine config.
                result = getString(key);
                
                // ok, give up and use the hard coded default value.
                if(result == null || result.equals(""))
                {
                    result = def;
                }
            }

        }
        catch (Exception e)
        {
            LOG.warn("Internal error while retrieving data from GLOBAL_PRAMETER_TABLE: ["+e.getMessage()+"]");
            L10NMessage msg = new L10NMessage(L10NKeySet.ExceptionTorqueGeneric,e.getMessage());
            throw new ScarabRuntimeException(msg);
        }
            
        return result;        
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
