

package org.tigris.scarab.om;

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
 */
public class GlobalParameterManager
    extends BaseGlobalParameterManager
{
    public static final String EMAIL_ENABLED = "email-enabled"; 
    public static final String EMAIL_INCLUDE_ISSUE_DETAILS = 
        "email-include-issue-details"; 
    public static final String EMAIL_ALLOW_MODULE_OVERRIDE = 
        "email-allow-module-overrides"; 

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
        if (oldOm == null) 
        {
        System.out.println("first put of value " + name + " to " + gp.getValue());
            
        }
        else 
        {
        System.out.println("changing value of " + name + " from " + ((GlobalParameter)oldOm).getValue() + " to " + gp.getValue());
            
        }
        
        return oldOm;
    }

    private static GlobalParameter getInstance(String name)
        throws TorqueException, ScarabException
    {
        GlobalParameter p = getInstance(name, null);
        if (p == null)
        {
            // parameters are expected to have the global value defined
            // in the db.  otherwise it is not a valid parameter
            throw new ScarabException("Invalid parameter: " + name);
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





