package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.fulcrum.cache.TurbineGlobalCacheService;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.fulcrum.cache.CachedObject;
import org.apache.fulcrum.cache.GlobalCacheService;
import org.apache.fulcrum.TurbineServices;

// Scarab classes
import org.tigris.scarab.services.user.UserManager;

import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.cache.ScarabCache;

/** 
  * This class represents the SCARAB_R_OPTION_OPTION table.
  * Please note that this class caches several pieces of data depending
  * on the methods called. If you would like to clear these caches,
  * it is a good idea to call the doRemoveCaches() method after making
  * any modifications to the ROptionOption, ParentChildAttributeOption,
  * and AttributeOption objects.
  *
  * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
  * @version $Id: Attribute.java,v 1.41 2002/03/02 02:32:58 jmcnally Exp $
  */
public class Attribute 
    extends BaseAttribute
    implements Persistent
{
    private static final String className = "Attribute";
    
    // the following Strings are method names that are used in caching results
    private static final String ATTRIBUTE = 
        className;
    private static final String GET_INSTANCE = 
        "getInstance";
    private static final String GET_ALL_ATTRIBUTE_TYPES = 
        "getAllAttributeTypes";
    private static final String GET_ALL_ATTRIBUTES = 
        "getAllAttributes";
    private static final String GET_ALL_ATTRIBUTE_OPTIONS = 
        "getAllAttributeOptions";
    private static final String GET_ORDERED_ROPTIONOPTION_LIST = 
        "getOrderedROptionOptionList";


    private static final String SELECT_ONE = "select-one";
    
    private List orderedROptionOptionList = null;
    private List orderedAttributeOptionList = null;
    private List parentChildAttributeOptions = null;
    
    private HashMap optionsMap;
    private List attributeOptionsWithDeleted;
    private List attributeOptionsWithoutDeleted;

    /**
     * Must call getInstance()
     */
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
        throws TorqueException
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
                throw new TorqueException("Attribute with ID " + attId + 
                                          " can not be found");
            }
            tgcs.addObject(key, new CachedObject(attribute));
        }
        return attribute;
    }

    /**
     * Return an instance based on the passed in attribute id as an int
     * It will return a cached instance if possible.
     */
    public static Attribute getInstance(int id)
        throws TorqueException
    {
        return getInstance((ObjectKey)new NumberKey(id));
    }


    /**
     * Return an instance based on the passed in 
     * attribute name as a String. It will return 
     * the first match if the number of Attributes found > 0
     * Note: The business logic dicates that there should 
     * never be duplicate Attributes. Therefore, the checkForDuplicate
     * method will return true if the number of Attributes found
     * is > 0
     */
    public static Attribute getInstance(String attributeName)
        throws Exception
    {
        Attribute result = null;
        Object obj = ScarabCache.get(ATTRIBUTE, GET_INSTANCE, attributeName); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria();
            crit.add (AttributePeer.ATTRIBUTE_NAME, attributeName);
            List attributes = (List) AttributePeer.doSelect(crit);
            if (attributes.size() > 0)
            {
                result = (Attribute) attributes.get(0);
                ScarabCache.put(result, ATTRIBUTE, GET_INSTANCE, attributeName);
            } 
        }
        else 
        {
            result = (Attribute)obj;
        }
        return result;
    }

    /**
     * Checks to see if there is another attribute with the same name
     * already in the database. Returns true if there is another
     * Attribute of the same name.
     */
    public static boolean checkForDuplicate(String attributeName)
        throws Exception
    {
        return getInstance(attributeName) != null ? true : false;
    }

    /**
     * Helper method that takes a NumberKey
     */
    public void setCreatedBy (NumberKey key)
    {
        super.setCreatedBy(new Integer(key.toString()).intValue());
    }

    /**
     * Helper method that takes a NumberKey
     */
    public String getCreatedUserName () throws Exception
    {
        String userId = Integer.toString(getCreatedBy());
        String userName = null;
        if (userId.equals("0"))
        {
            userName = "Default";
        }
        else
        {
            ScarabUser su = UserManager.getInstance(new NumberKey(userId));
            userName = su.getFirstName() + su.getLastName();
        }
        return userName;
    }

    /**
     * Clears the internal caches for this object
     */
    void doRemoveCaches()
    {
        setOrderedROptionOptionList(null);
        setOrderedAttributeOptionList(null);
        setParentChildAttributeOptions(null);
    }


    /**
     * Little method to return a List of all Attribute Type's.
     * It is here for convenience with regards to needing this
     * functionality from within a Template.
     */
    public static List getAllAttributeTypes()
        throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(ATTRIBUTE, GET_ALL_ATTRIBUTE_TYPES); 
        if ( obj == null ) 
        {        
            result = AttributeTypePeer.doSelect(new Criteria());
            ScarabCache.put(result, ATTRIBUTE, GET_ALL_ATTRIBUTE_TYPES);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }


    /**
     * get a list of all of the Attributes in the database
     */
    public static List getAllAttributes()
        throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(ATTRIBUTE, GET_ALL_ATTRIBUTES); 
        if ( obj == null ) 
        {        
            result = AttributePeer.doSelect(new Criteria());
            ScarabCache.put(result, ATTRIBUTE, GET_ALL_ATTRIBUTES);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    public boolean isOptionAttribute()
        throws Exception
    {
        if ( getTypeId() != null ) 
        {
            return getAttributeType().getAttributeClass().getName()
                .equals("select-one");
        }
        return false;
    }

    public boolean isUserAttribute()
        throws Exception
    {
        if ( getTypeId() != null ) 
        {
            return getAttributeType().getAttributeClass().getName()
                .equals("user");
        }
        return false;
    }

    public boolean isTextAttribute()
        throws Exception
    {
        String[] textTypes = {"string", "email", "long-string"};
        boolean isText = false;
        if ( getTypeId() != null ) 
        {
            for ( int i=0; i<textTypes.length && !isText; i++ ) 
            {
                isText = textTypes[i].equals(getAttributeType().getName());
            }
        }
        return isText;
    }

    /**
     * This method is special. Don't use it. 
     * It is used to generate the mappings for r_option_option
     * table mappings because AO's have to have a mapping in 
     * there in order to be looked up using the getOrderedROptionOptionList()
     * method. This method has already been run and the output was
     * used to copy/paste into the scarab-default-data.sql file.
     * It is being kept here in case it is needed again someday.
    public static void createROptionOptionMapping()
        throws Exception
    {
        List attributes = Attribute.getAllAttributes();
        for (int i=0; i<attributes.size();i++)
        {
            Attribute attr = (Attribute) attributes.get(i);
            if (attr.getName().equals("Operating System") || 
                attr.getName().equals("Null Attribute"))
            {
                continue;
            }
            System.out.println ("Attribute: " + attr.getName());
            List attributeOptions = attr.getAttributeOptions();
            Iterator itr = attributeOptions.iterator();
            int counter = 1;
            while (itr.hasNext())
            {
                AttributeOption ao = (AttributeOption) itr.next();
                System.out.println ("\tAttribute Option: " + ao.getName());
                ROptionOption roo = ROptionOption.getInstance();
                roo.setOption1Id(new NumberKey(0));
                roo.setOption2Id(ao.getOptionId());
                roo.setRelationshipId(new NumberKey(1));
                roo.setPreferredOrder(counter++);
                roo.setDeleted(false);
                roo.save();
            }
        }
    }
     */

/****************************************************************************/
/* Attribute Option Methods                                                 */
/****************************************************************************/

    /**
     * Gets one of the options belonging to this attribute. if the 
     * PrimaryKey does not belong to an option in this attribute
     * null is returned.
     *
     * @param pk a <code>NumberKey</code> value
     * @return an <code>AttributeOption</code> value
     */
    public AttributeOption getAttributeOption(NumberKey pk)
        throws TorqueException
    {
        if (optionsMap == null)
        {
            buildOptionsMap();
        }
        return (AttributeOption)optionsMap.get(pk);
    }

    /**
     * Get an option by String id
     */
    public AttributeOption getAttributeOption(String optionID)
        throws TorqueException
    {
        return getAttributeOption(new NumberKey(optionID));
    }


    /**
     * Used internally to get a list of Attribute Options
     */
    private List getAllAttributeOptions()
        throws TorqueException
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_ALL_ATTRIBUTE_OPTIONS); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria();
            crit.addJoin(AttributeOptionPeer.OPTION_ID, 
                         ROptionOptionPeer.OPTION2_ID);
            crit.add(AttributeOptionPeer.ATTRIBUTE_ID, this.getAttributeId());
            crit.addAscendingOrderByColumn(ROptionOptionPeer.PREFERRED_ORDER);
            result = AttributeOptionPeer.doSelect(crit);
            ScarabCache.put(result, this, GET_ALL_ATTRIBUTE_OPTIONS);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    /**
     * package protected method to set the value of the cached
     * list. Generally, this is used to set it to null.
     */
    void setParentChildAttributeOptions(List value)
    {
        parentChildAttributeOptions = value;
    }

    /**
     * This returns a list of ParentChildAttributeOption objects
     * which have been populated with combined join data from 
     * ROptionOption and the AttributeOption table.
     *
     * @return a List of ParentChildAttributeOption objects
     */
    public List getParentChildAttributeOptions()
        throws TorqueException
    {
        if (parentChildAttributeOptions == null)
        {
            List rooList = getOrderedROptionOptionList();
            List aoList = getOrderedAttributeOptionList();
            parentChildAttributeOptions = new ArrayList(rooList.size());
            for (int i=0; i<rooList.size();i++)
            {
                ROptionOption roo = (ROptionOption)rooList.get(i);
                AttributeOption ao = (AttributeOption)aoList.get(i);
    
                ParentChildAttributeOption pcao = ParentChildAttributeOption
                        .getInstance(roo.getOption1Id(), roo.getOption2Id());
                pcao.setParentId(roo.getOption1Id());
                pcao.setOptionId(roo.getOption2Id());
                pcao.setPreferredOrder(roo.getPreferredOrder());
                pcao.setWeight(roo.getWeight());
                pcao.setName(ao.getName());
                pcao.setDeleted(ao.getDeleted());
                pcao.setAttributeId(this.getAttributeId());
                parentChildAttributeOptions.add(pcao);
            }
        }
        return parentChildAttributeOptions;
    }

    /**
     * package protected method to set the value of the cached
     * list. Generally, this is used to set it to null.
     */
    void setOrderedROptionOptionList(List value)
    {
        orderedROptionOptionList = value;
    }


    /**
     * Creates an ordered List of ROptionOption which are
     * children within this Attribute. The list is ordered according 
     * to the preferred order.
     *
     * @return a List of ROptionOption's
     */
    public List getOrderedROptionOptionList()
        throws TorqueException
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_ORDERED_ROPTIONOPTION_LIST); 
        if ( obj == null ) 
        {        
            if (orderedROptionOptionList == null)
            {
                Criteria crit = new Criteria();
                crit.addJoin(AttributeOptionPeer.OPTION_ID, 
                             ROptionOptionPeer.OPTION2_ID);
                crit.add(AttributeOptionPeer.ATTRIBUTE_ID, getAttributeId());
                crit.addAscendingOrderByColumn(
                    ROptionOptionPeer.PREFERRED_ORDER);
                orderedROptionOptionList = ROptionOptionPeer.doSelect(crit);
            }
            result = orderedROptionOptionList;
            ScarabCache.put(result, this, GET_ORDERED_ROPTIONOPTION_LIST);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    /**
     * package protected method to set the value of the cached
     * list. Generally, this is used to set it to null.
     */
    void setOrderedAttributeOptionList(List value)
    {
        orderedAttributeOptionList = value;
    }

    /**
     * Creates an ordered List of AttributeOptions which are
     * children within this Attribute. The list is ordered according 
     * to the preferred order.
     *
     * @return a List of AttributeOption's
     */
    public List getOrderedAttributeOptionList()
        throws TorqueException
    {
        if (orderedAttributeOptionList == null)
        {
            Criteria crit = new Criteria();
            crit.addJoin(AttributeOptionPeer.OPTION_ID, ROptionOptionPeer.OPTION2_ID);
            crit.add(AttributeOptionPeer.ATTRIBUTE_ID, this.getAttributeId());
            crit.addAscendingOrderByColumn(ROptionOptionPeer.PREFERRED_ORDER);
            orderedAttributeOptionList = AttributeOptionPeer.doSelect(crit);
        }
        return orderedAttributeOptionList;
    }

    /**
     * Get a list of all attribute options or just the ones
     * that have not been marked as deleted.
     */
    public List getAttributeOptions(boolean includeDeleted)
        throws TorqueException
    {
        List allOptions = getAllAttributeOptions();
        List nonDeleted = new ArrayList(allOptions.size());
        if ( includeDeleted ) 
        {
            return allOptions;
        }
        else 
        {
            for ( int i=0; i<allOptions.size(); i++ ) 
            {
                AttributeOption option = (AttributeOption)allOptions.get(i);
                if (!option.getDeleted())
                {
                    nonDeleted.add(option);
                }
            }
            return nonDeleted;
        }
    }

    /**
     * Build a list of options.
     */
    public synchronized void buildOptionsMap()
        throws TorqueException
    {
        if ( this.getAttributeType().getAttributeClass().getName()
             .equals(SELECT_ONE) ) 
        {
            // synchronized method due to getattributeOptionsWithDeleted, this needs
            // further investigation !FIXME!
            attributeOptionsWithDeleted = this.getAllAttributeOptions();
            optionsMap = new HashMap((int)(1.25*attributeOptionsWithDeleted.size()+1));
    
            attributeOptionsWithoutDeleted = new ArrayList(attributeOptionsWithDeleted.size());
            for ( int i=0; i<attributeOptionsWithDeleted.size(); i++ ) 
            {
                AttributeOption option = (AttributeOption)attributeOptionsWithDeleted.get(i);
                optionsMap.put(option.getOptionId(), option);
                if ( !option.getDeleted() ) 
                {
                    attributeOptionsWithoutDeleted.add(attributeOptionsWithDeleted.get(i));
                }
            }
        }
    }
}
