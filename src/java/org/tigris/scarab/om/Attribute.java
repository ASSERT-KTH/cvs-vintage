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

// JDK classes
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Collections;

// Turbine classes
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.fulcrum.cache.TurbineGlobalCacheService;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.fulcrum.cache.CachedObject;
import org.apache.fulcrum.cache.GlobalCacheService;
import org.apache.fulcrum.TurbineServices;

import org.tigris.scarab.util.ScarabException;

/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Wed Feb 28 16:36:26 PST 2001]
  *
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.
  */
public class Attribute 
    extends BaseAttribute
    implements Persistent
{
    private static final String className = "Attribute";

    private static final String SELECT_ONE = "select-one";
    
    private static Criteria allOptionsCriteria;

    /** should be cloned to use */
    private static Criteria moduleOptionsCriteria;

    private HashMap optionsMap;
    private List attributeOptionsWithDeleted;
    private List attributeOptionsWithoutDeleted;
    private static HashMap optionAttributeMap = new HashMap();

    static
    {
        allOptionsCriteria = new Criteria();
        allOptionsCriteria.addAscendingOrderByColumn(AttributeOptionPeer.NUMERIC_VALUE);
        allOptionsCriteria.addAscendingOrderByColumn(AttributeOptionPeer.OPTION_NAME);

        moduleOptionsCriteria = new Criteria();
        moduleOptionsCriteria
            .addAscendingOrderByColumn(RModuleOptionPeer.PREFERRED_ORDER);
        moduleOptionsCriteria
            .addAscendingOrderByColumn(RModuleOptionPeer.DISPLAY_VALUE);
    }

    protected Attribute()
    {
    }

    static String getCacheKey(ObjectKey key)
    {
         String keyString = key.getValue().toString();
         return new StringBuffer(className.length() + keyString.length())
             .append(className).append(keyString).toString();
    }


    /**
     * A new Attribute
     */
    public static Attribute getInstance() 
    {
        return new Attribute();
    }

    /**
     * Return an instance of Attribute based on the passed in attribute id
     */
    public static Attribute getInstance(ObjectKey attId) 
        throws Exception
    {
        TurbineGlobalCacheService tgcs = 
            (TurbineGlobalCacheService)TurbineServices
            .getInstance().getService(GlobalCacheService.SERVICE_NAME);

        String key = getCacheKey(attId);
        Attribute attribute = null;
        try
        {
            attribute = (Attribute)tgcs.getObject(key).getContents();
        }
        catch (ObjectExpiredException oee)
        {
            try
            {
                attribute = AttributePeer.retrieveByPK(attId);
            }
            catch (Exception e)
            {
                throw new ScarabException("Attribute with ID " + attId + 
                                          " can not be found");
            }
            tgcs.addObject(key, new CachedObject(attribute));
        }
        
        return attribute;
    }

    /**
     * Return an instance based on the passed in attribute id as an int
     */
    public static Attribute getInstance(int id) 
        throws Exception
    {
        return getInstance((ObjectKey)new NumberKey(id));
    }

    public static Attribute getAttributeForOption(NumberKey optionId)
    {
        return (Attribute)optionAttributeMap.get(optionId);
    }

    /**
     * return the options (for attributes that have them).  They are put
     * into order by the numeric value.
     */
    public List getattributeOptions()
        throws Exception
    {
        // return getAttributeOptions(new Criteria());  
        return getAttributeOptions(allOptionsCriteria);  
    }

    /**
     * Gets one of the options belonging to this attribute. if the 
     * PrimaryKey does not belong to an option in this attribute
     * null is returned.
     *
     * @param pk a <code>NumberKey</code> value
     * @return an <code>AttributeOption</code> value
     */
    public AttributeOption getAttributeOption(NumberKey pk)
        throws Exception
    {
        if (optionsMap == null)
        {
            buildOptionsMap();
        }
        return (AttributeOption)optionsMap.get(pk);
    }

    public AttributeOption getAttributeOption(String optionID)
        throws Exception
    {
        return getAttributeOption(new NumberKey(optionID));
    }

    public List getAttributeOptions(boolean includeDeleted)
        throws Exception
    {
        if ( includeDeleted ) 
        {
            if (attributeOptionsWithDeleted == null)
            {
                buildOptionsMap();
            }
            return attributeOptionsWithDeleted;
        }
        else 
        {
            if (attributeOptionsWithoutDeleted == null)
            {
                buildOptionsMap();
            }
            return attributeOptionsWithoutDeleted; 
        }
    }

    public synchronized void buildOptionsMap()
        throws Exception
    {
        if ( this.getAttributeType().getAttributeClass().getName()
             .equals(SELECT_ONE) ) 
        {
            // synchronized method due to getattributeOptionsWithDeleted, this needs
            // further investigation !FIXME!
            List options = getattributeOptions();
    
            optionsMap = new HashMap((int)(1.25*options.size()+1));
            attributeOptionsWithDeleted = new ArrayList(options.size());
    
            for ( int i = options.size()-1; i >= 0; i-- ) 
            {
                AttributeOption option = (AttributeOption)options.get(i);
                attributeOptionsWithDeleted.add(option);
                optionsMap.put(option.getOptionId(), option);
                optionAttributeMap.put(option.getOptionId(), this);
            }
    
            attributeOptionsWithoutDeleted = new ArrayList(attributeOptionsWithDeleted.size());
            for ( int i=0; i<attributeOptionsWithDeleted.size(); i++ ) 
            {
                if ( !((AttributeOption)attributeOptionsWithDeleted.get(i)).getDeleted() ) 
                {
                    attributeOptionsWithoutDeleted.add(attributeOptionsWithDeleted.get(i));
                }
            }
        }
    }

    /**
     * Adds a new option.  The list is resorted.
     */
    public synchronized void addAttributeOption(AttributeOption option)
        throws Exception
    {        
        Vector v = getAttributeOptions();
        
        // Check that a duplicate name is not being added
        for (int i = 0; i < v.size(); i++) 
        {
            AttributeOption opt = (AttributeOption)v.get(i);
            if ( option.getName()
                 .equalsIgnoreCase(opt.getName()) ) 
            {
                throw new ScarabException("Adding option " + 
                    option.getName() + 
                    " failed due to a non-unique name." );
            }
        }
        
        Vector sortedOptions = (Vector)v.clone();
        sortedOptions.add(option);
        option.setAttribute(this);
        sortOptions(sortedOptions);

    }

    /**
     * Sorts the options and renumbers any with duplicate numeric values
     */
    public synchronized void sortOptions(Vector v)
        throws Exception
    {
        Vector sortedOptions = (Vector)v.clone();
        Collections.sort( sortedOptions, AttributeOption.getComparator() );

        // set new numeric values in case any options duplicated
        // a numeric value
        for (int i = 0; i < sortedOptions.size(); i++) 
        {
            AttributeOption opt = (AttributeOption)sortedOptions.get(i);
            opt.setNumericValue(i+1);
            opt.save();
        }

        collAttributeOptions = sortedOptions;
    }

    /**
     * Little method to return a List of all Attribute Type's.
     * It is here for convenience with regards to needing this
     * functionality from within a Template.
     */
    public static List getAllAttributeTypes()
        throws Exception
    {
        return AttributeTypePeer.doSelect(new Criteria());
    }
}
