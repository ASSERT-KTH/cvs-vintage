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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.pool.PoolService;
import org.tigris.scarab.util.Log;

/**
 * This class provides a simple Map cache that is available to the
 * current thread.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: DefaultScarabCacheService.java,v 1.1 2004/11/15 09:23:59 dep4b Exp $
 */
public class DefaultScarabCacheService 
extends AbstractLogEnabled implements ScarabCacheService, Serviceable,  Initializable
{
    

    //private Configuration props;
    private Map maps;
    private Class keyClass;
    
    private PoolService poolService;
    private ServiceManager manager;    

    public DefaultScarabCacheService()
    {
    }

    public Map getMapImpl()
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

    public void clearImpl()
    {
        Map map = (Map)maps.get(Thread.currentThread());
        if (map != null) 
        {
            Iterator i = map.keySet().iterator();
            while (i.hasNext()) 
            {
                poolService.putInstance(i.next());
            }
            map.clear();
        }
    }

    public Object getImpl(Serializable instanceOrClass, String method)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key = 
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method);
            result = getMapImpl().get(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    public Object getImpl(Serializable instanceOrClass, String method,
                             Serializable arg1)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key = 
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1);
            result = getMapImpl().get(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    public Object getImpl(Serializable instanceOrClass, String method,
                             Serializable arg1, Serializable arg2)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key = 
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1, arg2);
            result = getMapImpl().get(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    public Object getImpl(Serializable instanceOrClass, String method,
                             Serializable arg1, Serializable arg2,
                             Serializable arg3)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key = 
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1, arg2, arg3);
            result = getMapImpl().get(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    public Object getImpl(Serializable[] keys)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key = 
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(keys);
            result = getMapImpl().get(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    public void putImpl(Object value, Serializable instanceOrClass, 
                           String method)
    {
        try
        {
            ScarabCacheKey key =  
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method);
            getMapImpl().put(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    public void putImpl(Object value, Serializable instanceOrClass, 
                           String method, Serializable arg1)
    {
        try
        {
            ScarabCacheKey key =  
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1);
            getMapImpl().put(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    public void putImpl(Object value, Serializable instanceOrClass, 
                           String method, Serializable arg1, Serializable arg2)
    {
        try
        {
            ScarabCacheKey key =  
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1, arg2);
            getMapImpl().put(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    public void putImpl(Object value, Serializable instanceOrClass, 
                           String method, Serializable arg1, Serializable arg2,
                           Serializable arg3)
    {
        try
        {
            ScarabCacheKey key =  
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1, arg2, arg3);
            getMapImpl().put(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    public void putImpl(Object value, Serializable[] keys)
    {
        try
        {
            ScarabCacheKey key =  
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(keys);
            getMapImpl().put(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    
    /**
     * Avalon component lifecycle method
     * @avalon.dependency type="org.apache.fulcrum.factory.FactoryService"
     */
    public void service(ServiceManager manager)
    {
        this.manager = manager;
    }

    /**
     * Avalon component lifecycle method
     * Initializes the service by loading default class loaders
     * and customized object factories.
     *
     * @throws InitializationException if initialization fails.
     */
    public void initialize() throws Exception
    {
        try
        {
            poolService = (PoolService) manager.lookup(PoolService.ROLE);
        }
        catch (Exception e)
        {
            throw new Exception(
               "ScarabCacheServiceService.init: Failed to get a Pool object", e);
        }
        maps = new WeakHashMap();
        try
        {
            keyClass = Class
                .forName("org.tigris.scarab.services.cache.ScarabCacheKey");
        }
        catch (Exception x)
        {
            throw new InitializationException(
                "Failed to initialize ScarabCache",x); //EXCEPTION
        }
        
    }
}
