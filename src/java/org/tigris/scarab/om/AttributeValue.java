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
import java.util.List;

// Turbine classes
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.pool.DBConnection;

import org.apache.fulcrum.cache.TurbineGlobalCacheService;
import org.apache.fulcrum.cache.GlobalCacheService;
import org.apache.fulcrum.cache.ObjectExpiredException;
import org.apache.fulcrum.cache.CachedObject;

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
public abstract class AttributeValue 
    extends BaseAttributeValue
    implements Persistent
{
    // need a local reference
    private Attribute aAttribute;

    private Transaction transaction;
    private NumberKey oldOptionId;
    private String oldValue;
    private boolean oldOptionIdIsSet;
    private boolean oldValueIsSet;
    
    private static String className = "AttributeValue";
    
    /** Creates a new attribute. Do not do anything here.
     * All initialization should be performed in init().
     */
    protected AttributeValue()
    {
        oldOptionIdIsSet = false;
        oldValueIsSet = false;
    }

    /**
     * Enters this attribute value into a transaction.  All changes to a
     * value must occur within a transaction.  The transaction is cleared
     * once the attribute value is saved.
     *
     * @param transaction a <code>Transaction</code> value
     * @exception ScarabException if a new transaction is set before
     * the value is saved.
     */
    public void startTransaction(Transaction transaction)
        throws ScarabException
    {
        if ( transaction == null ) 
        {
            String mesg = "Cannot start a transaction using null Transaction"; 
            throw new ScarabException(mesg);
        }
        
        if ( this.transaction == null ) 
        {
            this.transaction = transaction;
        }
        else 
        {
            throw new ScarabException("A new transaction was set and " +
                "a transaction was already in progress.");
        }
        oldOptionIdIsSet = false;
        oldValueIsSet = false;
        oldOptionId = null;
        oldValue = null;
    }

    private void endTransaction()
    {
        this.transaction = null;
        oldOptionId = null;
        oldValue = null;
        oldOptionIdIsSet = false;
        oldValueIsSet = false;
    }

    private void checkTransaction(String errorMessage)
        throws ScarabException
    {
        if ( transaction == null ) 
        {
            throw new ScarabException(errorMessage);
        }
    }

    public String getQueryKey()
    {
        String key = super.getQueryKey();
        if ( key == null || key.length() == 0 ) 
        {
            try
            {
                key = "__" + getAttribute().getQueryKey();
            }
            catch (Exception e)
            {
                key = "";
            }
        }
        
        return key;
    }

    public String toString()
    {
        try
        {
            return getAttribute().getName();
        }
        catch (Exception e)
        {
            return "";
        }
    }

    /**
     * Get the OptionId
     * @return String
     */
    public String getOptionIdAsString()
    {
      String optionIdString = "";
      if (getOptionId() != null) 
          optionIdString = getOptionId().toString();
      return optionIdString;
    }

    /**
     * Makes sure to set the Value as well, to make display of the
     * option easier
     *
     * @param optionId a <code>NumberKey</code> value
     */
    public void setOptionId(NumberKey optionId)
        throws Exception
    {
        if ( optionId != null && optionId.getValue() != null ) 
        {
            List options = getIssue().getScarabModule()
                .getRModuleOptions(getAttribute());
            for ( int i=options.size()-1; i>=0; i-- ) 
            {
                RModuleOption option = (RModuleOption)options.get(i);
                if ( option.getOptionId().equals(optionId) ) 
                {
                    setValue(option.getDisplayValue());
                    break;
                }
            }
        }
        // if the value is set multiple times before saving only
        // save the last saved value
        if ( !isNew() && !oldOptionIdIsSet ) 
        {
            oldOptionId = new NumberKey(getOptionId());
            oldOptionIdIsSet = true;
        }
        super.setOptionId(optionId);
    }


    public void setValue(String value)
    {
        // if the value is set multiple times before saving only
        // save the last saved value
        if ( !isNew() && !oldValueIsSet ) 
        {
            oldValue = getValue();
            oldValueIsSet = true;
        }
        super.setValue(value);
    }

    public boolean isRequired()
       throws Exception
    {
        RModuleAttribute rma = getIssue().getScarabModule()
            .getRModuleAttribute(getAttribute());
        return rma.getRequired();
    }

    public boolean isSet()
       throws Exception
    {
        return !(getOptionId() == null && getValue() == null
                 && getUserId() == null);
    }

    public Attribute getAttribute() throws Exception
    {
        if ( aAttribute==null && (getAttributeId() != null) )
        {
            aAttribute = Attribute.getInstance(getAttributeId());
            
            // make sure the parent attribute is in synch.
            super.setAttribute(aAttribute);            
        }
        return aAttribute;
    }

    public void setAttribute(Attribute v) throws Exception
    {
        aAttribute = v;
        super.setAttribute(v);
    }


    public AttributeOption getAttributeOption()
        throws Exception
    {
        return getAttribute().getAttributeOption(getOptionId());
    }
    //public abstract boolean isEquivalent(AttributeValue aval);


    /**
     * if the Attribute related to this value is marked as relevant
     * to checking for duplicates in the module related to the Issue
     * related to this value.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isDedupeAttribute()
        throws Exception
    {
        boolean result = false;
        Attribute[] dedupeAttributes = getIssue().getScarabModule()
            .getDedupeAttributes();
        for ( int i=dedupeAttributes.length-1; i>=0; i--) 
        {
            if ( dedupeAttributes[i].equals(getAttribute()) ) 
            {
                result = true;
                break;
            }
        }
        
        return result;
    }

    /**
     * if the Attribute related to this value is marked as relevant
     * to quick search in the module related to the Issue
     * related to this value.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isQuickSearchAttribute()
        throws Exception
    {
        boolean result = false;
        Attribute[] qsAttributes = getIssue().getScarabModule()
            .getQuickSearchAttributes();
        for ( int i=qsAttributes.length-1; i>=0; i--) 
        {
            if ( qsAttributes[i].equals(getAttribute()) ) 
            {
                result = true;
                break;
            }
        }
        
        return result;
    }

    static String getCacheKey(ObjectKey key)
    {
        String keyString = key.getValue().toString();
        return new StringBuffer(className.length() + keyString.length())
            .append(className).append(keyString).toString();
    }


    /** Creates, initializes and returns a new AttributeValue.
     * @return new Attribute instance
     * @param issue Issue object which this attribute is associated with
     * @param intId This Attribute's Id
     */
    public static AttributeValue getNewInstance(
        RModuleAttribute rma, Issue issue) throws Exception
    {
        return getNewInstance(rma.getAttributeId(), issue);
    }

    /** Creates, initializes and returns a new AttributeValue.
     * @return new AttributeValue instance
     * @param issue Issue object which this attributeValue is associated
     * @param attId the Attribute's Id
     */
    public static AttributeValue getNewInstance(
        ObjectKey attId, Issue issue) throws Exception
    {
        Attribute attribute = Attribute.getInstance(attId);
        return getNewInstance(attribute, issue);
    }

    /** Creates, initializes and returns a new AttributeValue.
     * @return new AttributeValue instance
     * @param issue Issue object which this attributeValue is associated
     * @param attId the Attribute's Id
     */
    public static synchronized AttributeValue getNewInstance(
        Attribute attribute, Issue issue) throws Exception
    {
        String className = attribute
            .getAttributeType().getJavaClassName();
        AttributeValue attv = (AttributeValue)
            Class.forName(className).newInstance();
        attv.setAttribute(attribute);
        attv.setIssue(issue);

        String key = getCacheKey(attribute.getPrimaryKey());
        TurbineGlobalCacheService tgcs = 
            (TurbineGlobalCacheService)TurbineServices
            .getInstance().getService(GlobalCacheService.SERVICE_NAME);

        Object resources = null;
        try
        {
            resources = tgcs.getObject(key).getContents();
        }
        catch (ObjectExpiredException oee)
        {
            resources = attv.loadResources();
            tgcs.addObject(key, new CachedObject(resources));
        }

        attv.setResources(resources);
        attv.init();
        return attv;
    }

    /** 
     * Loads from database data specific for this Attribute including Name.
     * These are data common to all Attribute instances with same id.
     * Data retrieved here will then be used in setResources.
     * @return Object containing Attribute resources which will be 
     *         used in setResources.
     */
    protected abstract Object loadResources() throws Exception;
    
    /** 
     * This method is used by an Attribute instance to obtain 
     * specific resources such as option list for SelectOneAttribute.
     * It may, for example put them into instance variables. Attributes
     * may use common resources as-is or create it's own resources
     * based on common, it should not, however, modify common resources
     * since they will be used by other Attribute instances.
     *
     * @param resources Resources common for Attributes with the specified id.
     */
    protected abstract void setResources(Object resources);
        
    /** Override this method if you need any initialization for this attr.
     * @throws Exception Generic Exception
     */
    public abstract void init() throws Exception;
    
    public boolean supportsVoting()
    {
        return false;
    }
    
    public AttributeValue copy() throws Exception
    {
        AttributeValue copyObj = AttributeValue
            .getNewInstance(getAttributeId(), getIssue());
        return copyInto(copyObj);
    }

    public void save(DBConnection dbcon)
        throws Exception
    {
        if ( isModified() ) 
        {
            checkTransaction("Cannot save a value outside a Transaction");
            // Save activity record
            Activity activity = new Activity();
            String desc = getActivityDescription();
            activity.create(getIssue(), getAttribute(), desc, this.transaction,
                            oldOptionId, getOptionId(),
                            oldValue , getValue());
            endTransaction();
        }
        super.save(dbcon);
    }

    // Not sure it is a good idea to save description in activity record
    // the description can be generated from the other data and it brings
    // up i18n issues.
    private String getActivityDescription()
        throws Exception
    {
        String id = getIssue().getFederatedId();
        String name = getAttribute().getName();
        String newValue = getValue();
        int length = 40 + id.length() + name.length() + newValue.length();
        if ( oldValue != null ) 
        {
            length += oldValue.length();
        }
        
        StringBuffer sb = new StringBuffer(length)
            .append("Issue ")
            .append(id)
            .append(" had attribute ")
            .append(name);
        if ( oldValue == null ) 
        {
            sb.append(" set");
        }
        else
        {
            sb.append(" changed from ")
              .append(oldValue);
        }
        sb.append(" to ")
          .append(newValue);
        return sb.toString();
    }
}



