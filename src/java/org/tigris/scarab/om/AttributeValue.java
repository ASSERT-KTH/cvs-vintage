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
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.sql.Connection;

import org.apache.commons.lang.ObjectUtils;

// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.fulcrum.localization.Localization;

import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.om.Module;

/**
 * This class is for dealing with Issue Attribute Values
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: AttributeValue.java,v 1.94 2003/04/12 20:01:53 jackrepenning Exp $
 */
public abstract class AttributeValue 
    extends BaseAttributeValue
    implements Persistent
{
    private ActivitySet activitySet;
    private Integer oldOptionId;
    private Integer oldUserId;
    private String oldValue;
    private Integer oldNumericValue;
    private boolean oldOptionIdIsSet;
    private boolean oldUserIdIsSet;
    private boolean oldValueIsSet;
    private boolean oldNumericValueIsSet;
    private AttributeValue chainedValue;
    
    private String activityDescription = null;
    private Activity saveActivity = null;

    private static String className = "AttributeValue";

    
    /** Creates a new attribute. Do not do anything here.
     * All initialization should be performed in init().
     */
    protected AttributeValue()
    {
        oldOptionIdIsSet = false;
        oldUserIdIsSet = false;
        oldValueIsSet = false;
        oldNumericValueIsSet = false;
    }

    /**
     * Get the value of chainedValue.
     * @return value of chainedValue.
     */
    public AttributeValue getChainedValue() 
    {
        return chainedValue;
    }
    
    /**
     * Set the value of chainedValue.
     * @param v  Value to assign to chainedValue.
     */
    public void setChainedValue(AttributeValue  v)
        throws Exception
    {
        if (v == null)
        {
            this.chainedValue = null;
        }
        else 
        {        
            if (v.getAttributeId() == null && getAttributeId() != null) 
            {
                v.setAttributeId(getAttributeId());
            }
            else if (v.getAttribute() != null 
                     && !v.getAttribute().equals(getAttribute()))
            {
                throw new ScarabException(
                    "Values for different Attributes cannot be chained: " +
                    v.getAttributeId() + " and " + getAttributeId());
            }
            
            if (v.getIssueId() == null && getIssueId() != null) 
            {
                v.setIssueId(getIssueId());
            }
            else if (v.getIssue() != null 
                      && !v.getIssue().equals(getIssue()))
            {
                throw new ScarabException(
                    "Values for different Issues cannot be chained: " +
                    v.getIssueId() + " and " + getIssueId());
            }

            if (this.chainedValue == null) 
            {
                this.chainedValue = v;
            }
            else 
            {
                chainedValue.setChainedValue(v);
            }
            
            if (activitySet != null) 
            {
                v.startActivitySet(activitySet);
            }        
        }
    }

    /**
     * This method returns a flat List of all AttributeValues that might
     * be chained to this one. This AttributeValue will be first in the List.
     *
     * @return a <code>List</code> of AttributeValue's
     */
    public List getValueList()
    {
        List list = new ArrayList();
        list.add(this);
        AttributeValue av = getChainedValue();
        while (av != null) 
        {
            list.add(av);
            av = av.getChainedValue();
        }
        return list;
    }

    /**
     * sets the AttributeId for this as well as any chained values.
     */
    public void setAttributeId(Integer nk)
        throws TorqueException
    {
        super.setAttributeId(nk);
        if (chainedValue != null) 
        {
            chainedValue.setAttributeId(nk);
        }
    }

    /**
     * sets the IssueId for this as well as any chained values.
     */
    public void setIssueId(Long nk)
        throws TorqueException
    {
        super.setIssueId(nk);
        if (chainedValue != null) 
        {
            chainedValue.setIssueId(nk);
        }
    }

    /**
     * Enters this attribute value into a activitySet.  All changes to a
     * value must occur within a activitySet.  The activitySet is cleared
     * once the attribute value is saved.
     *
     * @param activitySet a <code>ActivitySet</code> value
     * @exception ScarabException if a new activitySet is set before
     * the value is saved.
     */
    public void startActivitySet(ActivitySet activitySet)
        throws ScarabException, Exception
    {
        if (activitySet == null) 
        {
            String mesg = "Cannot start an ActivitySet using a null ActivitySet"; 
            throw new ScarabException(mesg);
        }
        
        if (this.activitySet == null) 
        {
            this.activitySet = activitySet;
        }
        else
        {
            throw new ScarabException("A new activitySet was set and " +
                "a activitySet was already in progress.");
        }
/*
This is wrong. It prevented the old/new value stuff from working properly!
If we have an existing issue and we change some attributes, then when the
history was created, the data was not valid in it for some reason. I'm not
quite sure why this was added. (JSS)

Leaving here so that John can remove or fix.

        oldOptionIdIsSet = false;
        oldValueIsSet = false;
        oldOptionId = null;
        oldValue = null;
*/

        // Check for previous active activities on this attribute 
        // If they exist, set old value for this activity
        List result = null;
        Issue issue = getIssue();
        if (issue != null)
        {
            result = issue
                .getActivitiesWithNullEndDate(getAttribute());
        }
        if (result != null && result.size() > 0)
        {
            for (int i=0; i<result.size(); i++)
            {
                Activity a = (Activity)result.get(i);
                oldOptionId = a.getNewOptionId();
                oldValue = a.getNewValue();
            }
        }
        if (chainedValue != null) 
        {
            chainedValue.startActivitySet(activitySet);
        }
    }

    private void endActivitySet()
    {
        this.activitySet = null;
        oldOptionId = null;
        oldValue = null;
        oldOptionIdIsSet = false;
        oldValueIsSet = false;
        if (chainedValue != null) 
        {
            chainedValue.endActivitySet();
        }
    }

    private void checkActivitySet(String errorMessage)
        throws ScarabException
    {
        if (activitySet == null) 
        {
            throw new ScarabException(errorMessage);
        }
    }

    public String getQueryKey()
    {
        String key = super.getQueryKey();
        if (key == null || key.length() == 0) 
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

    public boolean equals(Object obj)
    {
        boolean b = false;
        if (obj instanceof AttributeValue) 
        {
            b = super.equals(obj);
            if (!b) 
            {
                AttributeValue aval = (AttributeValue)obj;
                b = (getChainedValue() == null) && 
                    ObjectUtils.equals(aval.getAttributeId(), getAttributeId())
                    && ObjectUtils.equals(aval.getIssueId(), getIssueId());
            }
        }
        return b;
    }

    public int hashCode()
    {
        int retVal = 0;

        if (getChainedValue() != null || getPrimaryKey() != null)
        {
            // get the hash code from the primary key
            // field from BaseObject
            retVal = super.hashCode(); 
        }
        else 
        {
            int issueHashCode = 0;
            if (getIssueId() != null) 
            {
                issueHashCode = getIssueId().hashCode();
            }
            retVal = getAttributeId().hashCode() ^ issueHashCode;
        }
        return retVal;
    }

    public String toString()
    {
        try
        {
            String s = '{' + super.toString() + ": " + getAttribute().getName();
            if (getOptionId() != null) 
            {
                s += " optionId=" + getOptionId();  
            }
            if (getUserId() != null) 
            {
                s += " userId=" + getUserId();  
            }
            if (getValue() != null) 
            {
                s += " value=" + getValue();  
            }
            
            return s + '}';
        }
        catch (Exception e)
        {
            return super.toString();
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
        {
            optionIdString = getOptionId().toString();
        }
        return optionIdString;
    }

    /**
     * Makes sure to set the Value as well, to make display of the
     * option easier
     *
     * @param optionId a <code>Integer</code> value
     */
    public void setOptionId(Integer optionId)
        throws TorqueException
    {
        if ( optionId != null ) 
        {
            Module module = getIssue().getModule();
            IssueType issueType = getIssue().getIssueType();
            if (module == null || issueType == null)
            {
                AttributeOption option = AttributeOptionManager
                    .getInstance(optionId);
                setValueOnly(option.getName());
            }
            else 
            {
                // FIXME! create a key and get the instance directly from
                // the manager.
                List options = null;
                try
                {
                    options = module
                        .getRModuleOptions(getAttribute(), issueType);
                }
                catch (Exception e)
                {
                    if (e instanceof TorqueException) 
                    {
                        throw (TorqueException)e;
                    }
                    else 
                    {
                        throw new TorqueException(e);
                    }
                }
                for (int i=options.size()-1; i>=0; i--) 
                {
                    RModuleOption option = (RModuleOption)options.get(i);
                    if (option.getOptionId().equals(optionId)) 
                    {
                        setValueOnly(option.getDisplayValue());
                        break;
                    }
                }   
            }            
        }
        else
        {
            // any reason to set a option_id to null, once its already set?
            setValueOnly(null);
        }
        
        setOptionIdOnly(optionId);
    }

    /**
     * Makes sure to set the Value as well
     *
     * @param v
     */
    public void setNumericValue(Integer v)
    {        
        setValueOnly(String.valueOf(v));
        if (v != getNumericValue())
        { 
            // if the value is set multiple times before saving only
            // save the last saved value
            if (!isNew() && !oldNumericValueIsSet) 
            {
                oldNumericValue = getNumericValue();
                oldNumericValueIsSet = true;
            }
            super.setNumericValue(v);
        }  
    }

    protected void setOptionIdOnly(Integer optionId)
        throws TorqueException
    {
        if (!ObjectUtils.equals(optionId, getOptionId()))
        { 
            // if the value is set multiple times before saving only
            // save the last saved value
            if (!isNew() && !oldOptionIdIsSet && getOptionId() != null) 
            {
                oldOptionId = getOptionId();
                oldOptionIdIsSet = true;
            }
            super.setOptionId(optionId);
        }  
    }

    /**
     * Makes sure to set the Value as well, to make display of the
     * user easier
     *
     * @param userId a <code>Integer</code> value
     */
    public void setUserId(Integer userId)
        throws TorqueException
    {
        if (userId != null) 
        {
            ScarabUser user = ScarabUserManager.getInstance(userId);
            setValueOnly(user.getUserName());
        }
        else
        {
            // any reason to set a user_id to null, once its already set?
            setValueOnly(null);
        }

        setUserIdOnly(userId);
    }

    protected void setUserIdOnly(Integer value)
        throws TorqueException
    {
        if (!ObjectUtils.equals(value, getUserId()))
        { 
            // if the value is set multiple times before saving only
            // save the last saved value
            if (!isNew() && !oldUserIdIsSet) 
            {
                oldUserId = getUserId();
                oldUserIdIsSet = true;
            }
            super.setUserId(value);
        }
    }

    /**
     * Not implemented always throws an exception
     *
     * @return a <code>Integer[]</code> value
     * @exception Exception if an error occurs
     */
    public Integer[] getOptionIds()
        throws Exception
    {
        List optionIds = new ArrayList();
        if (getOptionId() != null) 
        {
            optionIds.add(getOptionId());
        }
        AttributeValue chainedAV = getChainedValue();
        while (chainedAV != null) 
        {
            if (chainedAV.getOptionId() != null) 
            {
                optionIds.add(chainedAV.getOptionId());
            }
            chainedAV = chainedAV.getChainedValue();
        }
        if (Log.get().isDebugEnabled()) 
        {
            Log.get().debug(this + " optionIds: " + optionIds);
        }
        
        return (Integer[])optionIds.toArray(new Integer[optionIds.size()]);
    }

    public void setOptionIds(Integer[] ids)
        throws Exception
    {
        if (ids != null && ids.length > 0) 
        {
            setOptionId(ids[0]);
        }
        if (ids != null && ids.length > 1) 
        {
            for (int i=1; i<ids.length; i++) 
            {            
                AttributeValue av = AttributeValue                
                    .getNewInstance(getAttributeId(), getIssue());
                setChainedValue(av);
                av.setOptionId(ids[i]);
            }
        }
    }

    /**
     * Not implemented always throws an exception
     *
     * @return a <code>Integer[]</code> value
     * @exception Exception if an error occurs
     */
    public Integer[] getUserIds()
        throws Exception
    {
        throw new ScarabException("not implemented");
    }

    public void setUserIds(Integer[] ids)
        throws Exception
    {
        if (ids != null && ids.length > 0) 
        {
            setUserId(ids[0]);
        }
        if (ids != null && ids.length > 1) 
        {
            for (int i=1; i<ids.length; i++) 
            {            
                AttributeValue av = AttributeValue                
                    .getNewInstance(getAttributeId(), getIssue());
                setChainedValue(av);
                av.setUserId(ids[i]);
            }
        }
    }

    public void setValue(String value)
    {
        setValueOnly(value);
    }

    protected void setValueOnly(String value)
    {
        if (!ObjectUtils.equals(value, getValue()))
        { 
            // if the value is set multiple times before saving only
            // save the last saved value
            if (!isNew() && !oldValueIsSet) 
            {
                oldValue = getValue();
                oldValueIsSet = true;
            }
            super.setValue(value);
        }
    }

    public boolean isSet()
    {
        return !(getOptionId() == null && getValue() == null
                 && getUserId() == null);
    }

    public boolean isRequired()
       throws Exception
    {
        return getRModuleAttribute().getRequired();
    }

    public RModuleAttribute getRModuleAttribute()
        throws Exception
    {
        Issue issue = getIssue();
        RModuleAttribute rma = null;
        if (issue != null)
        {
            Module module = issue.getModule();
            if (module != null)
            {
                rma = module.getRModuleAttribute(
                    getAttribute(), getIssue().getIssueType());
                if (rma == null)
                {
                    throw new Exception ("RMA is null: Please report this issue.");
                }
            }
            else
            {
                throw new Exception ("Module is null: Please report this issue.");
            }
        }
        else
        {
            throw new Exception ("Issue is null: Please report this issue.");
        }
        return rma;
    }

    public AttributeOption getAttributeOption()
        throws TorqueException
    {
        return getAttribute()
            .getAttributeOption(getOptionId());
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
        List qsAttributes = getIssue().getModule()
            .getQuickSearchAttributes(getIssue().getIssueType());
        for (int i=qsAttributes.size()-1; i>=0; i--) 
        {
            if (((Attribute)qsAttributes.get(i)).equals(getAttribute())) 
            {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Creates, initializes and returns a new AttributeValue.
     * @return new Attribute instance
     * @param rma the Attribute's rma
     * @param issue Issue object which this attribute is associated with
     */
    public static AttributeValue getNewInstance(
        RModuleAttribute rma, Issue issue) throws TorqueException
    {
        return getNewInstance(rma.getAttributeId(), issue);
    }

    /**
     * Creates, initializes and returns a new AttributeValue.
     * @return new AttributeValue instance
     * @param issue Issue object which this attributeValue is associated
     * @param attId the Attribute's Id
     */
    public static AttributeValue getNewInstance(
        Integer attId, Issue issue) throws TorqueException
    {
        Attribute attribute = AttributeManager.getInstance(attId);
        return getNewInstance(attribute, issue);
    }

    /**
     * Creates, initializes and returns a new AttributeValue.
     * @return new AttributeValue instance
     * @param attribute the Attribute
     * @param issue Issue object which this attributeValue is associated
     */
    public static synchronized AttributeValue getNewInstance(
        Attribute attribute, Issue issue) throws TorqueException
    {
        AttributeValue attv = null;
        try
        {
            String className = attribute
                .getAttributeType().getJavaClassName();
            attv = (AttributeValue)
                Class.forName(className).newInstance();
            attv.setAttribute(attribute);
            attv.setIssue(issue);
    
            attv.init();
        }
        catch (Exception e)
        {
            throw new TorqueException(e);
        }
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
    
    public AttributeValue copy() throws TorqueException
    {
        AttributeValue copyObj = AttributeValue
            .getNewInstance(getAttributeId(), getIssue());
        return copyInto(copyObj);
    }

    public void save(Connection dbcon)
        throws TorqueException
    {
        if (isModified() && !getAttribute().isUserAttribute())
        {
            String desc = null;
            try
            {
                checkActivitySet("Cannot save an AttributeValue outside of an ActivitySet");
                desc = getActivityDescription();
            }
            catch (Exception e)
            {
                throw new TorqueException(e);
            }
            // Save activity record
            if (getDeleted())
            {
                saveActivity = ActivityManager
                                .create(getIssue(), getAttribute(), activitySet, 
                                        desc, null, getNumericValue(), new Integer(0), 
                                        getUserId(), null, getOptionId(), null, 
                                        getValue(), null, dbcon);
            }
            else
            {
                saveActivity = ActivityManager
                                .create(getIssue(), getAttribute(), activitySet, 
                                        desc, null, oldNumericValue, getNumericValue(), 
                                        oldUserId, getUserId(), oldOptionId, getOptionId(), 
                                        oldValue, getValue(), dbcon);
            }
        }
        super.save(dbcon);
        if (chainedValue != null) 
        {
            chainedValue.save(dbcon);
        }
        endActivitySet();
    }

    /**
     * Gets the Activity record associated with this AttributeValue
     * It can only be retrieved after the save() method has been called 
     * since that is when it is generated.
     */
    public Activity getActivity()
    {
        return this.saveActivity;
    }

    /**
     * Allows you to override the description for
     * the activity that is generated when this attributevalue
     * is saved.
     */
    public void setActivityDescription(String string)
    {
        this.activityDescription = string;
    }

    /**
     * Not sure it is a good idea to save description in activity record
     * the description can be generated from the other data.
     */
    private String getActivityDescription()
        throws Exception
    {
        if (activityDescription != null)
        {
            return activityDescription;
        }
        String attributeName = getRModuleAttribute().getDisplayValue();
        String newValue = getValue();

        String result = null;
        if (getDeleted())
        {
            result = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME,
                getLocale(),
                "AttributeHasBeenUndefined", attributeName);
        }
        else
        {
            if (newValue.length() > 30) 
            {
                newValue = newValue.substring(0,30) + "...";
            }
            if (oldValue == null) 
            {
                Object[] args = {
                    attributeName,
                    newValue
                };
                result = Localization.format(
                    ScarabConstants.DEFAULT_BUNDLE_NAME,
                    getLocale(),
                    "AttributeSetToNewValue", args);
            }
            else
            {
                // so that we don't modify the existing oldValue
                String tmpOldValue = null;
                if (oldValue.length() > 30) 
                {
                    tmpOldValue = oldValue.substring(0,30) + "...";
                }
                else
                {
                    tmpOldValue = oldValue;
                }
                Object[] args = {
                    attributeName,
                    tmpOldValue,
                    newValue 
                };
                result = Localization.format(
                    ScarabConstants.DEFAULT_BUNDLE_NAME,
                    getLocale(),
                    "AttributeChangedFromToNewValue", args);
            }
        }
        return result;
    }

    /**
     * Sets the properties of one attribute value based on another 
     * NOTE: Does not copy the deleted field
     */
    public void setProperties(AttributeValue attVal1)
        throws Exception
    {
        setAttribute(attVal1.getAttribute());
        setIssue(attVal1.getIssue());
        setNumericValue(attVal1.getNumericValue());
        setOptionId(attVal1.getOptionId());
        setUserId(attVal1.getUserId());
        setValue(attVal1.getValue());
    }

    /**
     * Returns a (possibly user-specific) locale.
     *
     * @return a Locale selected for the Fulcrum Localization context
     */
    private Locale getLocale()
    {
        return new Locale
            (Localization.getDefaultLanguage(),
             Localization.getDefaultCountry());
    }
}



