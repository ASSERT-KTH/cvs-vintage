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

import org.apache.stratum.jcs.JCS;
import org.apache.stratum.jcs.access.behavior.ICacheAccess;

import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.Persistent;
import org.apache.log4j.Category;
import org.tigris.scarab.util.ScarabException;

/**
 * This class contains common functionality of a Service for 
 * instantiating OM's.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: AbstractOMService.java,v 1.6 2002/02/27 20:43:41 jmcnally Exp $
 */
public abstract class AbstractOMService 
    extends BaseService 
{
    protected static final Category category = 
        Category.getInstance(AbstractOMService.class.getName());

    /** used to cache the om objects */
    private ICacheAccess cache;

    /** the class that the service will instantiate */
    private Class omClass;

    private String className;

    private String region;

    /**
     * Initializes the OMService, locating the apropriate class and caching it
     */
    public void init()
        throws InitializationException
    {
        setInit(true);
    }

    /**
     * Get the Class instance
     */
    protected Class getOMClass()
    {
        return omClass;
    }

    /**
     * Set the Class that will be instantiated by this manager
     */
    protected void setOMClass(Class omClass)
    {
        this.omClass = omClass;
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
     * Get the classname to instantiate for getInstance()
     * @return value of className.
     */
    public String getClassName()
    {
        return className;
    }
    
    /**
     * Set the classname to instantiate for getInstance()
     * @param v  Value to assign to className.
     */
    public void setClassName(String  v) 
        throws InitializationException
    {
        this.className = v;

        try
        {
            setOMClass( Class.forName(getClassName()) );
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new InitializationException("Could not load "+getClassName());
        }
    }
    

    /**
     * Return an instance of an om based on the id
     */
    protected Object getOMInstance(ObjectKey id) 
        throws Exception
    {
        return getOMInstance(id, true);
    }

    /**
     * Return an instance of an om based on the id
     */
    protected Object getOMInstance(ObjectKey id, boolean fromCache) 
        throws Exception
    {
        String key = id.toString();
        Object om = null;
        if (fromCache)
        {
            om = cache.get(key);
        }

        if (om == null)
        {
            om = retrieveStoredOM(id);
            if (fromCache) 
            {
                cache.put(key, om);                
            }
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
            // start a new list where we will replace the id's with om's
            oms = new ArrayList(ids);
            List newIds = new ArrayList(ids.size());
            for ( int i=0; i<ids.size(); i++ ) 
            {
                ObjectKey id = (ObjectKey)ids.get(i);
                String key = id.toString();
                Object om = cache.get(key);
                if (om == null)
                {
                    newIds.add(id);
                }
                else
                {
                    oms.set(i, om); 
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
                                cache.put(oms.set(i, om).toString(), om);
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

    /**
     * Get the value of region.
     * @return value of region.
     */
    public String getRegion() 
    {
        return region;
    }
    
    /**
     * Set the value of region.
     * @param v  Value to assign to region.
     */
    public void setRegion(String  v) 
        throws InitializationException
    {
        category.debug(this + " Setting region to: " + v);
        this.region = v;
        try 
        {
            cache = JCS.getInstance(getRegion());
        } 
        catch (Exception e) 
        {
            throw new InitializationException(
                "Cache could not be initialized", e);
        }
        if (cache == null) 
        {
            throw new InitializationException(
                "Cache could not be initialized for region: " + v);            
        }
        
    }
    
}
