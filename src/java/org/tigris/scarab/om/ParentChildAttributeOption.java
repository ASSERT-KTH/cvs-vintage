package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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

import org.apache.torque.om.NumberKey;
import org.apache.torque.om.Retrievable;

import org.apache.fulcrum.cache.TurbineGlobalCacheService;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.fulcrum.cache.CachedObject;
import org.apache.fulcrum.cache.GlobalCacheService;
import org.apache.fulcrum.TurbineServices;

import org.tigris.scarab.util.ScarabException;

/** 
  * This class is used by Intake on the GlobalAttributeEdit page
  * to create combination of a ROptionOption and a AttributeOption
  *
  * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
  * @version $Id: ParentChildAttributeOption.java,v 1.5 2001/10/02 23:51:40 jon Exp $
  */
public class ParentChildAttributeOption 
    implements Retrievable
{
    /** the name of this class */
    private static final String className = "ParentChildAttributeOption";

    private NumberKey attributeId = null;
    private NumberKey optionId = null;
    private NumberKey parentId = null;
    private boolean deleted = false;
    private String name = null;
    private int preferredOrder = 0;
    private int weight = 0;

    /**
     * Must call getInstance()
     */
    protected ParentChildAttributeOption()
    {
    }

    /**
     * Creates a key for use in caching AttributeOptions
     */
    static String getCacheKey(NumberKey option1, NumberKey option2)
    {
         String keyStringA = option1.toString();
         String keyStringB = option2.toString();
         String output = new StringBuffer(className.length() + 
                                keyStringA.length() + keyStringB.length())
                                .append(className).append(keyStringA)
                                .append(keyStringB).toString();
         return output;
    }

    /**
     * Gets an instance of a new ParentChildAttributeOption
     */
    public static ParentChildAttributeOption getInstance()
    {
        return new ParentChildAttributeOption();
    }

    /**
     * Gets an instance of a new ROptionOption
     */
    public static ParentChildAttributeOption getInstance(
                                NumberKey parent, NumberKey child)
    {
        TurbineGlobalCacheService tgcs = 
            (TurbineGlobalCacheService)TurbineServices
            .getInstance().getService(GlobalCacheService.SERVICE_NAME);

        String key = getCacheKey(parent, child);
        ParentChildAttributeOption pcao = null;
        try
        {
            pcao = (ParentChildAttributeOption)tgcs.getObject(key)
                        .getContents();
        }
        catch (ObjectExpiredException oee)
        {
            pcao = getInstance();
            pcao.setParentId(parent);
            pcao.setOptionId(child);
            tgcs.addObject(key, new CachedObject(pcao));
        }
        return pcao;
    }

    /**
     * Implementation of the Retrievable interface because this object
     * is used with Intake
     */
    public String getQueryKey()
    {
        if (parentId == null || optionId == null)
        {
            return "";
        }
        return getParentId().toString() + ":" + getOptionId().toString();
    }

    /**
     * Implementation of the Retrievable interface because this object
     * is used with Intake
     */
    public void setQueryKey(String key)
    {
        int index = key.indexOf(":");
        String a = key.substring(0,index);
        String b = key.substring(index,key.length());
        setParentId(new NumberKey(a));
        setOptionId(new NumberKey(b));
    }

    public NumberKey getAttributeId()
    {
        return attributeId;
    }

    public void setAttributeId(NumberKey attributeId)
    {
        this.attributeId = attributeId;
    }

    public NumberKey getOptionId()
    {
        return this.optionId;
    }

    public void setOptionId(NumberKey key)
    {
        this.optionId = key;
    }

    public NumberKey getParentId()
    {
        if (this.parentId == null)
        {
            return new NumberKey(0);
        }
        return this.parentId;
    }

    public void setParentId(NumberKey id)
    {
        this.parentId = id;
    }

    public boolean getDeleted()
    {
        return this.deleted;
    }

    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }

    public String getName()
    {
        if (this.name == null)
        {
            return "";
        }
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getPreferredOrder()
    {
        return this.preferredOrder;
    }

    public void setPreferredOrder(int preferredOrder)
    {
        this.preferredOrder = preferredOrder;
    }

    public int getWeight()
    {
        return this.weight;
    }

    public void setWeight(int weight)
    {
        this.weight = weight;
    }

    /**
     * Removes the object from the cache
     */
    public static void doRemoveFromCache(NumberKey parent, NumberKey child)
    {
        TurbineGlobalCacheService tgcs = 
            (TurbineGlobalCacheService)TurbineServices
            .getInstance().getService(GlobalCacheService.SERVICE_NAME);

        String key = getCacheKey(parent, child);
        tgcs.removeObject(key);
    }

    public String toString()
    {
        return getParentId() + ":" + getOptionId() + " -> " + getName();
    }

    public void save()
        throws Exception
    {
        AttributeOption ao = null;
        ROptionOption roo = null;

        Attribute tmpAttr = Attribute.getInstance(getAttributeId());
        
        // if it is new, it won't already have an optionId
        if (getOptionId() == null)
        {
            // if it is new, check for duplicates.
            AttributeOption duplicate = 
                AttributeOption.getInstance(tmpAttr, getName());
            if (duplicate != null)
            {
                if (duplicate.getDeleted())
                {
                    throw new Exception ("Cannot create a child " + 
                        "of an Attribute Option that is marked as deleted!");
                }
                // if there is a duplicate, then attempt to create a new
                // ROO instead of creating a new AO and a new ROO. if the
                // duplicate has the same name and parent as an existing
                // pcao, then an exception will be thrown which needs to 
                // be caught and dealt with properly
                roo = ROptionOption.getInstance();
                roo.setOption1Id(getParentId());
                roo.setOption2Id(duplicate.getOptionId());
                roo.setPreferredOrder(getPreferredOrder());
                roo.setWeight(getWeight());
                roo.setRelationshipId(OptionRelationship.PARENT_CHILD);
                try
                {
                    roo.save();
                }
                catch (Exception sqle)
                {
                    throw new Exception ("Cannot have duplicate " + 
                        "entries with the same parent!");
                }
                return;
            }
        }
        // if the pcao is deleted and the parent is not Root, then delete
        // the option option mapping
        else if (getDeleted() && ! getParentId().equals(new NumberKey(0)))
        {
            ROptionOption
                .doRemove(getParentId(), getOptionId());
            return;
        }

        // if getOptionId() is null, then it will just create a new instance
        ao = AttributeOption.getInstance(getOptionId());
        
        ao.setName(getName());
        ao.setDeleted(getDeleted());
        ao.setAttribute(tmpAttr);
        ao.save();

        // clean out the caches for the AO
        tmpAttr.doRemoveCaches();

        // now set our option id from the saved AO
        this.setOptionId(ao.getOptionId());

        // now create the ROO mapping
        try
        {
            // look for a cached ROptionOption
            roo = ROptionOption.getInstance(getParentId(), getOptionId());
        }
        catch (ScarabException se)
        {
            // could not find a cached instance create new one
            roo = ROptionOption.getInstance();
            roo.setOption1Id(getParentId());
            roo.setOption2Id(getOptionId());
        }
        roo.setPreferredOrder(getPreferredOrder());
        roo.setWeight(getWeight());
        roo.setRelationshipId(OptionRelationship.PARENT_CHILD);
        roo.save();
    }
}
