package org.tigris.scarab.services.cache;

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

import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Iterator;
import java.io.Serializable;
import org.apache.fulcrum.Service;
import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.pool.TurbinePool;
import org.tigris.scarab.util.Log;

/**
 * This class provides a simple Map cache that is available to the
 * current thread.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: ScarabCache.java,v 1.6 2003/03/12 18:46:37 jmcnally Exp $
 */
public class ScarabCache 
    extends BaseService
    implements Service
{
    /** The name of the service */
    public static final String SERVICE_NAME = "ScarabCache";

    //private Configuration props;
    private Map maps;
    private Class keyClass;

    public ScarabCache()
    {
    }

    public void init()
        throws InitializationException
    {
        //props = getConfiguration();
        maps = new WeakHashMap();
        try
        {
            keyClass = Class
                .forName("org.tigris.scarab.services.cache.ScarabCacheKey");
        }
        catch (Exception x)
        {
            throw new InitializationException(
                "Failed to initialize ScarabCache",x);
        }
        setInit(true);
    }

    protected Map getMapImpl()
    {
        Thread t = Thread.currentThread();
        Map map = (Map)maps.get(t);
        if (map == null) 
        {
            map = new HashMap();
            maps.put(t, map);
        }
        
        return map;
    }

    protected void clearImpl()
    {
        Map map = (Map)maps.get(Thread.currentThread());
        if (map != null) 
        {
            Iterator i = map.keySet().iterator();
            while (i.hasNext()) 
            {
                TurbinePool.putInstance(i.next());
            }
            map.clear();
        }
    }

    protected Object getImpl(Serializable instanceOrClass, String method)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key = 
                (ScarabCacheKey)TurbinePool.getInstance(keyClass);
            key.init(instanceOrClass, method);
            result = getMapImpl().get(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    protected Object getImpl(Serializable instanceOrClass, String method,
                             Serializable arg1)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key = 
                (ScarabCacheKey)TurbinePool.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1);
            result = getMapImpl().get(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    protected Object getImpl(Serializable instanceOrClass, String method,
                             Serializable arg1, Serializable arg2)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key = 
                (ScarabCacheKey)TurbinePool.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1, arg2);
            result = getMapImpl().get(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    protected Object getImpl(Serializable instanceOrClass, String method,
                             Serializable arg1, Serializable arg2,
                             Serializable arg3)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key = 
                (ScarabCacheKey)TurbinePool.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1, arg2, arg3);
            result = getMapImpl().get(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    protected Object getImpl(Serializable[] keys)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key = 
                (ScarabCacheKey)TurbinePool.getInstance(keyClass);
            key.init(keys);
            result = getMapImpl().get(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    protected void putImpl(Object value, Serializable instanceOrClass, 
                           String method)
    {
        try
        {
            ScarabCacheKey key =  
                (ScarabCacheKey)TurbinePool.getInstance(keyClass);
            key.init(instanceOrClass, method);
            getMapImpl().put(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    protected void putImpl(Object value, Serializable instanceOrClass, 
                           String method, Serializable arg1)
    {
        try
        {
            ScarabCacheKey key =  
                (ScarabCacheKey)TurbinePool.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1);
            getMapImpl().put(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    protected void putImpl(Object value, Serializable instanceOrClass, 
                           String method, Serializable arg1, Serializable arg2)
    {
        try
        {
            ScarabCacheKey key =  
                (ScarabCacheKey)TurbinePool.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1, arg2);
            getMapImpl().put(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    protected void putImpl(Object value, Serializable instanceOrClass, 
                           String method, Serializable arg1, Serializable arg2,
                           Serializable arg3)
    {
        try
        {
            ScarabCacheKey key =  
                (ScarabCacheKey)TurbinePool.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1, arg2, arg3);
            getMapImpl().put(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    protected void putImpl(Object value, Serializable[] keys)
    {
        try
        {
            ScarabCacheKey key =  
                (ScarabCacheKey)TurbinePool.getInstance(keyClass);
            key.init(keys);
            getMapImpl().put(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }


    // *******************************************************************
    // static accessors
    // *******************************************************************

    public static Map getMap()
    {
        return getService().getMapImpl();
    }

    public static void clear()
    {
        getService().clearImpl();
    }

    public static Object get(Serializable instanceOrClass, String method)
    {
        return getService().getImpl(instanceOrClass, method);
    }
    public static Object get(Serializable instanceOrClass, String method,
                             Serializable arg1)
    {
        return getService().getImpl(instanceOrClass, method, arg1);
    }
    public static Object get(Serializable instanceOrClass, String method,
                             Serializable arg1, Serializable arg2)
    {
        return getService().getImpl(instanceOrClass, method, arg1, arg2);
    }
    public static Object get(Serializable instanceOrClass, String method,
                             Serializable arg1, Serializable arg2,
                             Serializable arg3)
    {
        return getService().getImpl(instanceOrClass, method, arg1, arg2, arg3);
    }
    public static Object get(Serializable[] keys)
    {
        return getService().getImpl(keys);
    }

    public static void put(Object value, Serializable instanceOrClass, 
                           String method)
    {
        getService().putImpl(value, instanceOrClass, method);
    }
    public static void put(Object value, Serializable instanceOrClass, 
                           String method, Serializable arg1)
    {
        getService().putImpl(value, instanceOrClass, method, arg1);
    }
    public static void put(Object value, Serializable instanceOrClass, 
                           String method, Serializable arg1, Serializable arg2)
    {
        getService().putImpl(value, instanceOrClass, method, arg1, arg2);
    }
    public static void put(Object value, Serializable instanceOrClass, 
                           String method, Serializable arg1, Serializable arg2,
                           Serializable arg3)
    {
        getService().putImpl(value, instanceOrClass, method, arg1, arg2, arg3);
    }
    public static void put(Object value, Serializable[] keys)
    {
        getService().putImpl(value, keys);
    }


    /**
     * Gets the <code>LocalizationService</code> implementation.
     *
     * @return the LocalizationService implementation.
     */
    protected static final ScarabCache getService()
    {
        return (ScarabCache) TurbineServices.getInstance()
                .getService(ScarabCache.SERVICE_NAME);
    }

}
