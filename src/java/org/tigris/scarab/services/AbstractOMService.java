package org.tigris.scarab.services;

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

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.TurbineServices;

import org.apache.fulcrum.cache.TurbineGlobalCacheService;
import org.apache.fulcrum.cache.GlobalCacheService;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.fulcrum.cache.CachedObject;

import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.Persistent;
import org.apache.log4j.Category;
import org.tigris.scarab.util.ScarabException;

/**
 * This class contains common functionality of a Service for 
 * instantiating OM's.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: AbstractOMService.java,v 1.4 2002/01/25 02:58:14 jmcnally Exp $
 */
public abstract class AbstractOMService 
    extends BaseService 
{
    protected static final Category category = 
        Category.getInstance(AbstractOMService.class.getName());

    /** used to cache the objects to save multiple lookups */
    TurbineGlobalCacheService cache;

    /** the class that the service will instantiate */
    private Class omClass;

    /**
     * Initializes the OMService, locating the apropriate class and caching it
     */
    public void init()
        throws InitializationException
    {
        String className = getClassName();
        try
        {
            omClass = Class.forName(className);
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new InitializationException("Could not load " + className);
        }

        cache = (TurbineGlobalCacheService)TurbineServices
            .getInstance().getService(GlobalCacheService.SERVICE_NAME);

        doInit();
        setInit(true);
    }
    
    /**
     * Called by init() to allow concrete implementations to add 
     * initialization.
     */
    protected void doInit()
        throws InitializationException
    {
    }

    /**
     * Get the Class instance
     */
    public Class getOMClass()
    {
        return omClass;
    }

    protected String getCacheKey(ObjectKey key)
    {
        String keyPrefix = getClassName();
        String keyString = key.getValue().toString();
        return new StringBuffer(keyPrefix.length() + keyString.length())
            .append(keyPrefix).append(keyString).toString();
    }

    /**
     * Get a fresh instance of an om
     */
    protected Object getOMInstance()
        throws InstantiationException, IllegalAccessException
    {
        return omClass.newInstance();
    }

    /**
     * Get the classname to instantiate
     */
    protected abstract String getClassName();

    /**
     * Return an instance of an om based on the id
     */
    protected Object getOMInstance(ObjectKey id) 
        throws Exception
    {
        String key = getCacheKey(id);
        Object om = null;
        try
        {
            om = cache.getObject(key).getContents();
        }
        catch (ObjectExpiredException oee)
        {
            om = retrieveStoredOM(id);
            cache.addObject(key, new CachedObject(om));
        }
        
        return om;
    }

    protected abstract Object retrieveStoredOM(ObjectKey id)
        throws Exception;

    /**
     * Gets a list of om's based on id's.
     *
     * @param ids a <code>ObjectKey[]</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    protected List getOMs(ObjectKey[] ids) 
        throws Exception
    {
        return getOMs(Arrays.asList(ids));
    }

    /**
     * Gets a list of om's based on id's.
     *
     * @param ids a <code>List</code> of <code>ObjectKey</code>'s
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    protected List getOMs(List ids) 
        throws Exception
    {
        List oms = null;
        if ( ids != null && ids.size() > 0 ) 
        {
            // start a new list where we well replace the id's with om's
            oms = new ArrayList(ids);
            List newIds = new ArrayList(ids.size());
            for ( int i=0; i<ids.size(); i++ ) 
            {
                ObjectKey id = (ObjectKey)ids.get(i);
                String key = getCacheKey(id);
                Object om = null;
                try
                {
                    oms.set(i, cache.getObject(key).getContents()); 
                }
                catch (ObjectExpiredException oee)
                {
                    newIds.add(id);
                }
            }
            
            if ( newIds.size() > 0 ) 
            {
                List newOms = retrieveStoredOMs(newIds);
                for ( int i=0; i<oms.size(); i++ ) 
                {
                    if ( oms.get(i) instanceof ObjectKey ) 
                    {
                        for ( int j=newOms.size(); j>=0; j-- ) 
                        {
                            Persistent om = (Persistent)newOms.get(j);
                            if ( om.getPrimaryKey().equals(oms.get(i)) ) 
                            {
                                // replace the id with the om and add the om
                                // to the cache
                                cache.addObject(
                                    getCacheKey((ObjectKey)oms.set(i, om)), 
                                    new CachedObject(om) );
                                newOms.remove(j);
                                break;
                            }
                        }
                    }
                }                
            }
        }
        return oms;
    }

    protected abstract List retrieveStoredOMs(List ids)
        throws Exception;
}
