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
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.sql.Connection;

import org.apache.commons.lang.ObjectUtils;
// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.manager.MethodResultCache;
import org.apache.torque.util.Criteria;
import org.apache.commons.collections.SequencedHashMap;
import org.apache.torque.map.DatabaseMap;
import org.apache.torque.oid.IDBroker;
import org.apache.torque.util.BasePeer;
import org.apache.fulcrum.localization.Localization;

// Scarab classes
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.attribute.TotalVotesAttribute;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.workflow.WorkflowFactory;

import org.apache.commons.lang.StringUtils;

/** 
 * This class represents an Issue.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: Issue.java,v 1.227 2002/12/10 21:38:39 elicia Exp $
 */
public class Issue 
    extends BaseIssue
    implements Persistent
{
    // the following Strings are method names that are used in caching results
    private static final String ISSUE = 
        "Issue";
    protected static final String GET_ISSUE_BY_ID = 
        "getIssueById";
    protected static final String GET_ATTRIBUTE_VALUES_MAP = 
        "getAttributeValuesMap";
    protected static final String GET_ASSOCIATED_USERS = 
        "getAssociatedUsers";
    protected static final String GET_MODULE_ATTRVALUES_MAP = 
        "getModuleAttributeValuesMap";
    protected static final String GET_ATTRVALUE = 
        "getAttributeValue";
    protected static final String GET_ATTRVALUES = 
        "getAttributeValues";
    protected static final String GET_USERS_TO_EMAIL = 
        "getUsersToEmail";
    protected static final String GET_USER_ATTRIBUTEVALUES = 
        "getUserAttributeValues";
    protected static final String GET_CREATED_DATE = 
        "getCreatedDate";
    protected static final String GET_CREATED_BY = 
        "getCreatedBy";
    protected static final String GET_LAST_TRANSACTION = 
        "getLastActivitySet";
    protected static final String GET_MODIFIED_BY = 
        "getModifiedBy";
    protected static final String GET_MODIFIED_DATE = 
        "getModifiedDate";
    protected static final String GET_COMMENTS = 
        "getComments";
    protected static final String GET_URLS = 
        "getUrls";
    protected static final String GET_EXISTING_ATTACHMENTS = 
        "getExistingAttachments";
    protected static final String GET_ACTIVITY = 
        "getActivity";
    protected static final String GET_TRANSACTIONS = 
        "getActivitySets";
    protected static final String GET_CHILDREN = 
        "getChildren";
    protected static final String GET_PARENTS = 
        "getParents";
    protected static final String GET_ALL_DEPENDENCY_TYPES = 
        "getAllDependencyTypes";
    protected static final String GET_DEPENDENCY = 
        "getDependency";
    protected static final String GET_TEMPLATE_TYPES = 
        "getTemplateTypes";
    protected static final String GET_TEMPLATEINFO = 
        "getTemplateInfo";
    protected static final String GET_CLOSED_DATE = 
        "getClosedDate";
    protected static final String GET_ORPHAN_ATTRIBUTEVALUES_LIST = 
        "getOrphanAttributeValuesList";
    protected static final String GET_DEFAULT_TEXT_ATTRIBUTEVALUE = 
        "getDefaultTextAttributeValue";
    protected static final String GET_DEFAULT_TEXT = 
        "getDefaultText";

    /**
     * new issues are created only when the issuetype and module are known
     * Or by the Peer when retrieving from db
     */
    protected Issue()
    {
    }

    protected Issue(Module module, IssueType issueType)
        throws Exception
    {
        this();
        setModule(module);
        setIssueType(issueType);
    }

    /**
     * Gets an issue associated to a Module
     */
    public static Issue getNewInstance(Module module, 
                                       IssueType issueType)
        throws Exception
    {
        Issue issue = new Issue(module, issueType);
        return issue;
    }

    /**
     * Gets the UniqueId for this Issue.
     */
    public String getUniqueId()
        throws TorqueException
    {
        if (getIdPrefix() == null)
        {
            setIdPrefix(getModule().getCode());
        }
        return getIdPrefix() + getIdCount();
    }

    /**
     * NoOp for intake's benefit
     */
    public void setUniqueId(String id)
    {
    }

    public String getFederatedId()
        throws TorqueException
    {
        if ( getIdDomain() != null ) 
        {
            return getIdDomain() + '-' + getUniqueId();
        }
        return getUniqueId();
    }

    public void setFederatedId(String id)
    {
        FederatedId fid = new FederatedId(id);
        setIdDomain(fid.getDomain());
        setIdPrefix(fid.getPrefix());
        setIdCount(fid.getCount());
    }

    /**
     * A FederatedId has this format: {Domain}-{Code}{Id}
     * For example: collab.net-PACS1
     * The domain can also be null.
     */
    public static class FederatedId
        implements Serializable
    {
        String domainId;
        String prefix;
        int count;

        public FederatedId(String id)
        {
            int dash = id.indexOf('-');
            if ( dash > 0 ) 
            {
                domainId = id.substring(0, dash);
                setUniqueId(id.substring(dash+1));
            }
            else 
            {
                setUniqueId(id);
            }
        }

        public FederatedId(String domain, String prefix, int count)
        {
            domainId = domain;
            this.prefix = prefix;
            this.count = count;
        }

        public void setUniqueId(String id)
        {
            // we could start at 1 here, if the spec says one char is 
            // required, will keep it safe for now.
            StringBuffer code = new StringBuffer(4);
            int max = id.length() < 4 ? id.length() : 4;
            for ( int i=0; i<max; i++) 
            {
                char c = id.charAt(i);
                if ( c != '0' && c != '1' && c != '2' && c != '3' && c != '4'
                     && c != '5' && c != '6' && c != '7' && c!='8' && c!='9' )
                {
                    code.append(c);
                }
            }
            if ( code.length() != 0 ) 
            {
                prefix = code.toString();                 
            }
            count = Integer.parseInt( id.substring(code.length()) );
            
        }

        
        /**
         * Get the IdInstance
         * @return String
         */
        public String getDomain()
        {
            return domainId;
        }

        /**
         * Get the Prefix
         * @return String
         */
        public String getPrefix()
        {
            return prefix;
        }
                
        /**
         * Get the Count
         * @return int
         */
        public int getCount()
        {
            return count;
        }
        
        /**
         * Set the domainId
         */
        public void setDomain(String domainId)
        {
            this.domainId = domainId;
        }
        /**
         * Set the Prefix
         * @param prefix
         */
        public void setPrefix(String prefix)
        {
            this.prefix = prefix;
        }
        
        /**
         * Set the Count
         * @param count
         */
        public void setCount(int count)
        {
            this.count = count;
        }

        public boolean equals(Object obj)
        {
            boolean b = false;
            if (obj instanceof FederatedId) 
            {
                FederatedId fid = (FederatedId)obj;
                b = fid.count == this.count; 
                b &= ObjectUtils.equals(fid.domainId, domainId);
                b &= ObjectUtils.equals(fid.prefix, prefix);
            }
            return b;
        }

        public int hashCode()
        {
            int hc = count;
            if (domainId != null) 
            {
                hc += domainId.hashCode();
            }
            if (prefix != null) 
            {
                hc += prefix.hashCode();
            }
            return hc;
        }
    }

    public static List parseIssueList(Module module, String theList)
        throws Exception
    {
        String[] issues = StringUtils.split(theList, ",");
        List results = new ArrayList();
        for (int i = 0; i < issues.length; i++)
        {
            // check for a -
            if (issues[i].indexOf("-") == -1)
            {
                results.add(issues[i]);
            }
            else
            {
                String[] issue = StringUtils.split(issues[i], "-");
                if (issue.length != 2)
                {
                    throw new Exception("Id range not valid: " + issues[i]);
                }
                FederatedId fidStart = createFederatedId(module, issue[0]);
                FederatedId fidStop = createFederatedId(module, issue[1]);
                if (!fidStart.getPrefix().equalsIgnoreCase(module.getCode()) ||
                    !fidStop.getPrefix().equalsIgnoreCase(module.getCode()))
                {
                    throw new Exception("Issue id prefixes are not in the " + 
                        "currently selected module: " + module.getCode());
                }
                else if (!fidStart.getPrefix().equalsIgnoreCase(fidStop.getPrefix()))
                {
                    throw new Exception("Issue id prefixes do not match: " + 
                        fidStart.getPrefix() + " is not equal to " + fidStop.getPrefix());
                }
                else if (fidStart.getCount() > fidStop.getCount())
                {
                    throw new Exception("Issue id range not valid: " + 
                        fidStart.getCount() + " is greater than " + fidStop.getCount());
                }
                for (int j = fidStart.getCount(); j <= fidStop.getCount();j++)
                {
                    String tmp = fidStart.getPrefix() + j;
                    results.add(tmp);
                }
            }
        }
        return results;
    }

    /**
     * Catches and rethrows parsing errors when creating the federated id.
     */
    private static FederatedId createFederatedId(Module module, String id)
        throws Exception
    {
        FederatedId fid = null;
        try
        {
            fid = new FederatedId(id.trim());
            if (fid.getPrefix() == null || fid.getPrefix().length() == 0)
            {
                fid.setPrefix(module.getCode());
            }
        }
        catch (Exception e)
        {
            throw new Exception("Invaild federated id: " + id);
        }
        return fid;
    }

    /**
     * Whether this issue is an enter issue template.
     */
    public boolean isTemplate() throws Exception
    {
       return !getIssueType().equals(IssueType.ISSUE__PK);
    }

    /**
     * Adds a url to an issue and passes null as the activity set
     * to create a new one.
     */
    public ActivitySet addUrl(Attachment attachment, ScarabUser user)
        throws Exception
    {
        return addUrl(null, attachment, user);
    }

    /**
     * Adds a url to an issue.
     */
    public ActivitySet addUrl(ActivitySet activitySet, 
                           Attachment attachment, ScarabUser user)
        throws Exception
    {
        attachment.setTextFields(user, this, Attachment.URL__PK);
        attachment.save();

        String nameFieldString = attachment.getName();
        String dataFieldString = attachment.getData();
        // Generate description of modification
        int length = nameFieldString.length() + 12;
        // strip off the end
        if (length > 254)
        {
            nameFieldString = nameFieldString.substring(0, 238) + "...";
        }
        String description = new StringBuffer(length)
            .append("Added URL '").append(nameFieldString).append('\'').toString();

        // Save activitySet record
        if (activitySet == null)
        {
            activitySet = getActivitySet(user, ActivitySetTypePeer.EDIT_ISSUE__PK);
            activitySet.save();            
        }
        // Save activity record
        ActivityManager
            .createTextActivity(this, activitySet, description, attachment);
        
        return activitySet;
    }

    /**
     * Adds a comment to an issue and passes null as the activity set
     * to create a new one.
     */
    public ActivitySet addComment(Attachment attachment, ScarabUser user)
        throws Exception
    {
        return addComment(null, attachment, user);
    }

    /**
     * Adds a comment to an issue.
     */
    public ActivitySet addComment(ActivitySet activitySet, 
                           Attachment attachment, ScarabUser user)
        throws Exception
    {
        String desc = Localization.getString(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            Locale.getDefault(),
            "AddComment");
        return addMessage(activitySet, desc, attachment, user);
    }

    /**
     * Used for adding a Note on Wizard2. This creates the ActivitySet
     * since one is not passed in from Wizard2.
     */
    public ActivitySet addNote(ActivitySet activitySet, 
                            Attachment attachment, ScarabUser user)
        throws Exception
    {
        String desc = Localization.getString(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            Locale.getDefault(),
            "AddNote");
        return addMessage(activitySet, desc, attachment, user);
    }

    /**
     * Used by the addComment/addNote methods. Essentially, they
     * are the same method with different messages and slightly 
     * different usage patterns.
     */
    private ActivitySet addMessage(ActivitySet activitySet, String description, 
                           Attachment attachment, ScarabUser user)
        throws Exception
    {
        attachment.setIssue(this);
        attachment.setTypeId(Attachment.COMMENT__PK);
        attachment.setName("comment");
        attachment.setCreatedBy(user.getUserId());
        attachment.setMimeType("text/plain");
        attachment.save();

        if (activitySet == null)
        {
            activitySet = getActivitySet(user, attachment, 
                                        ActivitySetTypePeer.EDIT_ISSUE__PK);
        }
        else
        {
            activitySet.setAttachment(attachment);
        }
        activitySet.save();

        String summary = attachment.getData();
        if (summary != null && summary.length() > 60)
        {
            summary = summary.substring(0,60) + "...";
        }                
        
        ActivityManager
            .createTextActivity(this, activitySet,
                                description, summary);
        return activitySet;
    }    

    /**
     * Adds an attachment file to this issue
     */
    public ActivitySet addFile(Attachment attachment, ScarabUser user)
        throws Exception
    {
        return addFile(null, attachment, user);
    }
    
    /**
     * Adds an attachment file to this issue
     */
    public ActivitySet addFile(ActivitySet activitySet, Attachment attachment, 
                        ScarabUser user)
        throws Exception
    {
        attachment.setIssue(this);
        attachment.setTypeId(Attachment.FILE__PK);
        attachment.setCreatedBy(user.getUserId());
        super.addAttachment(attachment);
        this.save();

        // Generate description of modification
        String name = attachment.getFileName();
        String path = attachment.getRelativePath();
        String description = 
            new StringBuffer(path.length() + name.length() + 17)
                .append("Added file attachment '").append(name)
                .append("' path=").append(path).toString();

        if (activitySet == null)
        {
            // Save activitySet record
            activitySet = getActivitySet(user, ActivitySetTypePeer.EDIT_ISSUE__PK);
            activitySet.save();
        }
        // Save activity record
        ActivityManager
            .createTextActivity(this, activitySet,
                                description, attachment);
        return activitySet;
    }
    
    /** 
     * Remove an attachment file
     * @param index starts with 1 because velocityCount start from 1
     * but Vector starts from 0
     */
    public void removeFile(String index)
        throws Exception
    {
        getAttachments().remove(Integer.parseInt(index) - 1);
    }


    /**
     * Throws UnsupportedOperationException.  Use
     * <code>getModule()</code> instead.
     *
     * @return a <code>ScarabModule</code> value
     */
    public ScarabModule getScarabModule()
    {
        throw new UnsupportedOperationException(
            "Should use getModule");
    }

    /**
     * Throws UnsupportedOperationException.  Use
     * <code>setModule(Module)</code> instead.
     *
     */
    public void setScarabModule(ScarabModule module)
    {
        throw new UnsupportedOperationException(
            "Should use setModule(Module). Note module cannot be new.");
    }

    /**
     * Use this instead of setScarabModule.  Note: module cannot be new.
     */
    public void setModule(Module me)
        throws TorqueException
    {
        NumberKey id = me.getModuleId();
        if (id == null) 
        {
            throw new TorqueException("Modules must be saved prior to " +
                                      "being associated with other objects.");
        }
        setModuleId(id);
    }

    /**
     * Module getter.  Use this method instead of getScarabModule().
     *
     * @return a <code>Module</code> value
     */
    public Module getModule()
        throws TorqueException
    {
        Module module = null;
        ObjectKey id = getModuleId();
        if ( id != null ) 
        {
            module = ModuleManager.getInstance(id);
        }
        
        return module;
    }

    public static Issue getIssueById(String id)
    {
        FederatedId fid = new FederatedId(id);
        return getIssueById(fid);
    }

    public static Issue getIssueById(FederatedId fid)
    {
        Issue result = null;
        Object obj = ScarabCache.get(ISSUE, GET_ISSUE_BY_ID, fid); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria(5)
                .add(IssuePeer.ID_PREFIX, fid.getPrefix())
                .add(IssuePeer.ID_COUNT, fid.getCount());
            
            if (  fid.getDomain() != null ) 
            {
                crit.add(IssuePeer.ID_DOMAIN, fid.getDomain());    
            }
            
            try
            {
                result = (Issue)IssuePeer.doSelect(crit).get(0);
                IssueManager.putInstance(result);
                ScarabCache.put(result, ISSUE, GET_ISSUE_BY_ID, fid);
            }
            catch (Exception e) 
            {
                // return null
            }
        }
        else 
        {
            result = (Issue)obj;
        }
        return result;
    }

    /**
     * The RModuleIssueType related to this issue's module and issue type.
     *
     * @return a <code>RModuleIssueType</code> if this issue's module and
     * issue type are not null, otherwise return null.
     */
    public RModuleIssueType getRModuleIssueType()
        throws Exception
    {
        RModuleIssueType rmit = null;
        Module module = getModule();
        IssueType issueType = getIssueType();
        if (module != null && issueType != null) 
        {
            rmit = module.getRModuleIssueType(issueType);
        }
        return rmit;
    }


    /**
     * AttributeValues that are relevant to the issue's current module.
     * Empty AttributeValues that are relevant for the module, but have 
     * not been set for the issue are included.  The values are ordered
     * according to the module's preference
     */
    public SequencedHashMap getModuleAttributeValuesMap() 
        throws Exception
    {
        SequencedHashMap result = null;
        Object obj = getMethodResult().get(this, GET_MODULE_ATTRVALUES_MAP);
        if ( obj == null ) 
        {        
            Attribute[] attributes = null;
            HashMap siaValuesMap = null;

            attributes = getModule().getActiveAttributes(getIssueType());
            siaValuesMap = getAttributeValuesMap();

            result = new SequencedHashMap((int)(1.25*attributes.length + 1));
            for ( int i=0; i<attributes.length; i++ ) 
            {
                String key = attributes[i].getName().toUpperCase();
                if ( siaValuesMap.containsKey(key) ) 
                {
                    result.put( key, siaValuesMap.get(key) );
                }
                else 
                {
                    AttributeValue aval = AttributeValue
                        .getNewInstance(attributes[i], this);
                    addAttributeValue(aval);
                    siaValuesMap.put(
                        aval.getAttribute().getName().toUpperCase(), aval);
                    result.put(key, aval);
                }
            }
            getMethodResult().put(result, this, GET_MODULE_ATTRVALUES_MAP);
        }
        else 
        {
            result = (SequencedHashMap)obj;
        }
        return result;
    }

    public void addAttributeValue(AttributeValue aval)
       throws TorqueException
    {
        List avals = getAttributeValues();
        if (!avals.contains(aval)) 
        {
            super.addAttributeValue(aval);
        }
    }

    public AttributeValue getAttributeValue(Attribute attribute)
       throws Exception
    {
        AttributeValue result = null;
        Object obj = ScarabCache.get(this, GET_ATTRVALUE, attribute); 
        if ( obj == null ) 
        {        
            if ( isNew() ) 
            {
                List avals = getAttributeValues();
                if ( avals != null ) 
                {
                    Iterator i = avals.iterator();
                    while (i.hasNext()) 
                    {
                        AttributeValue tempAval = (AttributeValue)i.next();
                        if ( tempAval.getAttribute().equals(attribute)) 
                        {
                            result = tempAval;
                            break;
                        }
                    }
                }
            }
            else 
            {            
                Criteria crit = new Criteria(2)
                    .add(AttributeValuePeer.ISSUE_ID, getIssueId())        
                    .add(AttributeValuePeer.DELETED, false)        
                    .add(AttributeValuePeer.ATTRIBUTE_ID, 
                         attribute.getAttributeId());
                
                List avals = getAttributeValues(crit);               
                if (avals.size() == 1)
                {
                    result = (AttributeValue)avals.get(0);
                }
            }
            ScarabCache.put(result, this, GET_ATTRVALUE, attribute);
        }
        else 
        {
            result = (AttributeValue)obj;
        }
        return result;
    }

    /**
     * Returns AttributeValues for the Attribute (which have not been deleted.)
     * Warning: does not work for a new issue.
     * FIXME! this method should be similar to getAttributeValue and
     * getAttributeValue should call this method.
     */
    public List getAttributeValues(Attribute attribute)
       throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_ATTRVALUES, attribute); 
        if ( obj == null ) 
        {        
            // FIXME!  needs a isNew() check for alternative logic.
            Criteria crit = new Criteria(2)
                .add(AttributeValuePeer.DELETED, false)        
                .add(AttributeValuePeer.ATTRIBUTE_ID, 
                     attribute.getAttributeId());
            
            result = getAttributeValues(crit);
            ScarabCache.put(result, this, GET_ATTRVALUES, attribute);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    public boolean isAttributeValue(AttributeValue attVal)
       throws Exception
    {
        boolean isValue = false;
        List attValues = getAttributeValues(attVal.getAttribute());
        if (attValues.contains(attVal))
        {
            isValue = true;
        }
        return isValue;
    }

    /**
     * AttributeValues that are set for this Issue in the order
     * that is preferred for this module
     */
    public AttributeValue[] getOrderedAttributeValues()
        throws Exception
    {        
        Map values = getAttributeValuesMap();
        Attribute[] attributes = getModule()
            .getActiveAttributes(getIssueType());

        return orderAttributeValues(values, attributes);
    }


    /**
     * Extract the AttributeValues from the Map according to the 
     * order in the Attribute[]
     */
    private AttributeValue[] orderAttributeValues(Map values, 
                                                  Attribute[] attributes) 
        throws Exception
    {
        AttributeValue[] orderedValues = new AttributeValue[values.size()];

        int i=0;
        for ( int j=0; j<attributes.length; j++ ) 
        {
            AttributeValue av = (AttributeValue) values
                .remove( attributes[j].getName().toUpperCase() );
            if ( av != null ) 
            {
                orderedValues[i++] = av;                
            }
        }
        Iterator iter = values.values().iterator();
        while ( iter.hasNext() ) 
        {
            orderedValues[i++] = (AttributeValue)iter.next();
        }

        return orderedValues;
    }


    /**
     * AttributeValues that are set for this Issue
     */
    public HashMap getAttributeValuesMap() throws Exception
    {
        HashMap result = null;
        Object obj = ScarabCache.get(this, GET_ATTRIBUTE_VALUES_MAP); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria(2)
                .add(AttributeValuePeer.DELETED, false);        
            List siaValues = getAttributeValues(crit);
            result = new HashMap( (int)(1.25*siaValues.size() + 1) );
            for ( int i=0; i<siaValues.size(); i++ ) 
            {
                AttributeValue att = (AttributeValue) siaValues.get(i);
                String name = att.getAttribute().getName();
                result.put(name.toUpperCase(), att);
            }

            ScarabCache.put(result, this, GET_ATTRIBUTE_VALUES_MAP);
        }
        else 
        {
            result = (HashMap)obj;
        }
        return result;
    }


    /**
     * AttributeValues that are set for this issue and
     * Empty AttributeValues that are relevant for the module, but have 
     * not been set for the issue are included.
     */
    public HashMap getAllAttributeValuesMap() 
        throws Exception
    {
        Map moduleAtts = getModuleAttributeValuesMap();
        Map issueAtts = getAttributeValuesMap();
        HashMap allValuesMap = new HashMap( (int)(1.25*(moduleAtts.size() + 
                                            issueAtts.size())+1) );

        allValuesMap.putAll(moduleAtts);
        allValuesMap.putAll(issueAtts);
        return allValuesMap;
    }


    /**
     * Describe <code>containsMinimumAttributeValues</code> method here.
     *
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean containsMinimumAttributeValues()
        throws Exception
    {
        List attributes = getModule()
            .getRequiredAttributes(getIssueType());

        boolean result = true;
        SequencedHashMap avMap = getModuleAttributeValuesMap();
        Iterator i = avMap.iterator();
        while (i.hasNext()) 
        {
            AttributeValue aval = (AttributeValue)avMap.get(i.next());
            
            if ( aval.getOptionId() == null && aval.getValue() == null ) 
            {
                for ( int j=attributes.size()-1; j>=0; j-- ) 
                {
                    if ( aval.getAttribute().getPrimaryKey().equals(
                         ((Attribute)attributes.get(j)).getPrimaryKey() )) 
                    {
                        result = false;
                        break;
                    }                    
                }
                if ( !result ) 
                {
                    break;
                }
            }
        }

        return result;
    }       


    /**
     * Users who are valid values to the attribute this issue.  
     * if a user has already
     * been assigned to this issue, they will not show up in this list.
     * use module.getEligibleUsers(Attribute) to get a complete list.
     *
     * @return a <code>List</code> value
     */
    public List getEligibleUsers(Attribute attribute)
        throws Exception
    {
        ScarabUser[] users = getModule().getEligibleUsers(attribute);
        // remove those already assigned
        List assigneeAVs = getAttributeValues(attribute);
        if ( users != null && assigneeAVs != null ) 
        {        
            for ( int i=users.length-1; i>=0; i-- ) 
            {
                for ( int j=assigneeAVs.size()-1; j>=0; j-- ) 
                {
                    AttributeValue av = (AttributeValue)assigneeAVs.get(j);
                    NumberKey avUserId = av.getUserId();
                    NumberKey userUserId = users[i].getUserId();
                    if ( av != null && avUserId != null && 
                         userUserId != null && 
                         avUserId.equals( userUserId ) )
                    {
                        users[i] = null;
                        break;
                    }
                }
            }
        }

        List eligibleUsers = new ArrayList(users.length);
        for ( int i=0; i<users.length; i++ ) 
        {
            if ( users[i] != null )
            {
                eligibleUsers.add(users[i]);
            }
        }

        return eligibleUsers;
    }

    /**
     * Returns users assigned to user attributes that get emailed 
     * When issue is modified. Plus creating user.
     */
    public List getUsersToEmailThisIssueOnly(String action, Issue issue, List users) 
        throws Exception
    {
        ScarabUser createdBy = issue.getCreatedBy();
        if (action.equals(AttributePeer.EMAIL_TO) && !users.contains(createdBy))
        {
            users.add(createdBy);
        }
        Criteria crit = new Criteria()
            .add(AttributeValuePeer.ISSUE_ID, issue.getIssueId())
            .addJoin(AttributeValuePeer.ATTRIBUTE_ID,
                     AttributePeer.ATTRIBUTE_ID)
            .add(AttributePeer.ACTION, action)
            .add(AttributeValuePeer.DELETED, 0);
        List userAttVals = AttributeValuePeer.doSelect(crit);
        for (int i=0;i<userAttVals.size();i++)
        {
            AttributeValue attVal = (AttributeValue)userAttVals.get(i);
            try
            {
                ScarabUser su = ScarabUserManager.getInstance(attVal.getUserId());
                if (!users.contains(su))
                {
                    users.add(su);
                }
            }
            catch (Exception e)
            {
                throw new Exception("Error in retrieving users.");
            }
        }
        return users;
    }


    /**
     * Returns users assigned to user attributes that get emailed 
     * When issue is modified. Plus creating user.
     * Adds users to email for dependant issues as well.
     */
    public List getUsersToEmail(String action) throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_USERS_TO_EMAIL, action); 
        if ( obj == null ) 
        {        
            List users = new ArrayList();
            try
            {
                users = getUsersToEmailThisIssueOnly(action, this, users);
                List children = getChildren();
                for (int i=0;i<children.size();i++)
                {
                    Issue depIssue = IssueManager.getInstance(((Depend)children.get(i)).getObserverId());
                    users = getUsersToEmailThisIssueOnly(action, depIssue, users);
                }
            }
            catch (Exception e)
            {
                throw new Exception("Error in retrieving users.");
            }
            result = users;
            ScarabCache.put(result, this, GET_USERS_TO_EMAIL, action);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }


    /**
     * Returns users assigned to all user attributes.
    public List getAssociatedUsers() throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_ASSOCIATED_USERS); 
        if ( obj == null ) 
        {        
            List attributeList = getModule()
                .getUserAttributes(getIssueType(), true);
            List attributeIdList = new ArrayList();
            
            for ( int i=0; i<attributeList.size(); i++ ) 
            {
                Attribute att = (Attribute) attributeList.get(i);
                RModuleAttribute modAttr = getModule().
                    getRModuleAttribute(att, getIssueType());
                if (modAttr.getActive())
                {
                    attributeIdList.add(att.getAttributeId());
                }
            }
            
            result = new ArrayList();
            if (!attributeIdList.isEmpty())
            {
                Criteria crit = new Criteria()
                    .addIn(AttributeValuePeer.ATTRIBUTE_ID, attributeIdList)
                    .add(AttributeValuePeer.DELETED, false);
                crit.setDistinct();
                
                List attValues = getAttributeValues(crit);
                for ( int i=0; i<attValues.size(); i++ ) 
                {
                    AttributeValue attVal = (AttributeValue) attValues.get(i);
                    ScarabUser su = ScarabUserManager.getInstance(attVal.getUserId());
                    result.add(su);
                }
            }
            
            ScarabUser createdBy = getCreatedBy();
            if (!result.contains(createdBy))
            { 
                result.add(createdBy);
            }
            ScarabCache.put(result, this, GET_ASSOCIATED_USERS);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }
     */


    /**
     * Returns attribute values for user attributes.
     */
    public List getUserAttributeValues() throws Exception
    {
        List result = null;
        Object obj = getMethodResult().get(this, GET_USER_ATTRIBUTEVALUES); 
        if ( obj == null ) 
        {        
            List attributeList = getModule().getUserAttributes(getIssueType(), true);
            List attributeIdList = new ArrayList();
            
            for ( int i=0; i<attributeList.size(); i++ ) 
            {
                Attribute att = (Attribute) attributeList.get(i);
                attributeIdList.add(att.getAttributeId());
            }
            
            if(!attributeIdList.isEmpty())
            {
                Criteria crit = new Criteria()
                    .addIn(AttributeValuePeer.ATTRIBUTE_ID, attributeIdList)
                    .add(AttributeValuePeer.ISSUE_ID, getIssueId())
                    .add(AttributeValuePeer.DELETED, 0);
                result = AttributeValuePeer.doSelect(crit);
            }
            else 
            {
                result = new ArrayList(0);
            }
            getMethodResult().put(result, this, GET_USER_ATTRIBUTEVALUES);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

     
    /**
     * The date the issue was created.
     *
     * @return a <code>Date</code> value
     * @exception Exception if an error occurs
     */
    public Date getCreatedDate()
        throws Exception
    {
        Date result = null;
        if ( !isNew() ) 
        {
            Object obj = ScarabCache.get(this, GET_CREATED_DATE); 
            if ( obj == null ) 
            {        
                Criteria crit = new Criteria();
                crit.addJoin(ActivitySetPeer.TRANSACTION_ID, 
                             ActivityPeer.TRANSACTION_ID);
                crit.add(ActivityPeer.ISSUE_ID, getIssueId());
                crit.add(ActivitySetPeer.TYPE_ID, 
                         ActivitySetTypePeer.CREATE_ISSUE__PK);
                // there could be multiple attributes modified during the 
                // creation which will lead to duplicates
                crit.setDistinct();
                List activitySets = ActivitySetPeer.doSelect(crit);
                ActivitySet t = (ActivitySet)activitySets.get(0);
                result = t.getCreatedDate();
                ScarabCache.put(result, this, GET_CREATED_DATE);
            }
            else 
            {
                result = (Date)obj;
            }
        }
        return result;
    }


    /**
     * The user that created the issue.
     * FIXME: Not sure if this is the best way to get created user.
     *
     * @return a <code>ScarabUser</code> value
     * @exception Exception if an error occurs
     */
    public ScarabUser getCreatedBy()
        throws Exception
    {
        ScarabUser result = null;
        if ( !isNew() ) 
        {
            Object obj = ScarabCache.get(this, GET_CREATED_BY); 
            if ( obj == null ) 
            {        
                Criteria crit = new Criteria();
                crit.addJoin(ActivitySetPeer.TRANSACTION_ID, 
                             ActivityPeer.TRANSACTION_ID);
                crit.add(ActivityPeer.ISSUE_ID, getIssueId());
                crit.add(ActivitySetPeer.TYPE_ID, 
                         ActivitySetTypePeer.CREATE_ISSUE__PK);
                /*
                  // there could be multiple attributes modified during the 
                  // creation which will lead to duplicates
                  crit.setDistinct();
                */
                List activitySets = ActivitySetPeer.doSelect(crit);
                if (activitySets.size() > 0)
                {
                    ActivitySet t = (ActivitySet)activitySets.get(0);
                    result = ScarabUserManager.getInstance(t.getCreatedBy());
                }
                ScarabCache.put(result, this, GET_CREATED_BY);
            }
            else 
            {
                result = (ScarabUser)obj;
            }
        }
        return result;
    }

    public boolean isCreatingUser( ScarabUser user)
         throws Exception
    {                
         return (getCreatedBy().getUserId().equals(user.getUserId()));
    }

    /**
     * The last modification made to the issue.
     *
     * @return a <code>ScarabUser</code> value
     */
    public ActivitySet getLastActivitySet()
        throws Exception
    {
        ActivitySet t = null;
        if ( !isNew() ) 
        {
            Object obj = ScarabCache.get(this, GET_LAST_TRANSACTION); 
            if ( obj == null ) 
            {        
                Criteria crit = new Criteria();
                crit.addJoin(ActivitySetPeer.TRANSACTION_ID, 
                         ActivityPeer.TRANSACTION_ID);
                crit.add(ActivityPeer.ISSUE_ID, getIssueId());
                crit.add(ActivitySetPeer.TYPE_ID, 
                         ActivitySetTypePeer.EDIT_ISSUE__PK);
                // there could be multiple attributes modified during the 
                // creation which will lead to duplicates
                crit.setDistinct();
                crit.addDescendingOrderByColumn(ActivitySetPeer.CREATED_DATE);
                List activitySets = ActivitySetPeer.doSelect(crit);
                if ( activitySets.size() > 0 ) 
                {
                    t = (ActivitySet)activitySets.get(0);
                }
                ScarabCache.put(t, this, GET_LAST_TRANSACTION);
            }
            else 
            {
                t = (ActivitySet)obj;
            }
        }
        return t;
    }


    /**
     * The date issue was last modified.
     *
     * @return a <code>ScarabUser</code> value
     */
    public Date getModifiedDate()
        throws Exception
    {
        Date result = null;
        if ( !isNew() ) 
        {
            ActivitySet t = getLastActivitySet();
            if ( t == null)
            {
                result = getCreatedDate();
            }
            else 
            {
                result = t.getCreatedDate();
            }
        }
        return result;
    }

    /**
     * The last user to modify the issue.
     *
     * @return a <code>ScarabUser</code> value
     */
    public ScarabUser getModifiedBy()
        throws Exception
    {
        ScarabUser result = null;
        if ( !isNew() ) 
        {
            ActivitySet t = getLastActivitySet();
            if ( t == null)
            {
                result = getCreatedBy();
            }
            else 
            {
                result = ScarabUserManager.getInstance(t.getCreatedBy());
            }
        }
        return result;
    }


    /**
     * Returns the total number of comments.
     */
    public int getCommentsCount() throws Exception
    {
        return getComments(true).size();
    }

    /**
     * Determines whether the comments list is longer than
     * The default limit.
     */
    public boolean isCommentsLong() throws Exception
    {
        return (getCommentsCount() > getCommentsLimit());
    }

    /**
     * Gets default comments limit for this module-issue type.
     */
    public int getCommentsLimit() throws Exception
    {
        int limit=0;
        try
        {
            limit = getModule().getRModuleIssueType(getIssueType())
                    .getComments();
        }
        catch (Exception e)
        {}
        return limit;
    }


    /**
     * Returns a list of Attachment objects with type "Comment"
     * That are associated with this issue.
     */
    public List getComments(boolean full) throws Exception
    {
        List result = null;
        Boolean fullBool = (full ? Boolean.TRUE : Boolean.FALSE);
        Object obj = getMethodResult().get(this, GET_COMMENTS, fullBool);
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria()
                .add(AttachmentPeer.ISSUE_ID, getIssueId())
                .addJoin(AttachmentTypePeer.ATTACHMENT_TYPE_ID,
                         AttachmentPeer.ATTACHMENT_TYPE_ID)
                .add(AttachmentTypePeer.ATTACHMENT_TYPE_ID, 
                     Attachment.COMMENT__PK)
                .addDescendingOrderByColumn(AttachmentPeer.CREATED_DATE);
            if (!full)
            {
                crit.setLimit(getCommentsLimit());
            }
            result = AttachmentPeer.doSelect(crit);
            getMethodResult().put(result, this, GET_COMMENTS, fullBool);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }


    /**
     * Returns a list of Attachment objects with type "URL"
     * That are associated with this issue.
     */
    public List getUrls() throws Exception
    {
        List result = null;
        Object obj = getMethodResult().get(this, GET_URLS); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria()
                .add(AttachmentPeer.ISSUE_ID, getIssueId())
                .addJoin(AttachmentTypePeer.ATTACHMENT_TYPE_ID,
                         AttachmentPeer.ATTACHMENT_TYPE_ID)
                .add(AttachmentTypePeer.ATTACHMENT_TYPE_ID, 
                     Attachment.URL__PK)
                .add(AttachmentPeer.DELETED, 0);
            result = AttachmentPeer.doSelect(crit);
            getMethodResult().put(result, this, GET_URLS);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }
    

    /**
     * Get attachments that are not deleted
     */
    public List getExistingAttachments() throws Exception
    {
        List result = null;
        Object obj = getMethodResult().get(this, GET_EXISTING_ATTACHMENTS); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria()
                .add(AttachmentPeer.ISSUE_ID, getIssueId())
                .addJoin(AttachmentTypePeer.ATTACHMENT_TYPE_ID,
                         AttachmentPeer.ATTACHMENT_TYPE_ID)
                .add(AttachmentTypePeer.ATTACHMENT_TYPE_ID, 
                     Attachment.FILE__PK)
                .add(AttachmentPeer.DELETED, 0);
            result = AttachmentPeer.doSelect(crit);
            getMethodResult().put(result, this, GET_EXISTING_ATTACHMENTS);
        }
        else 
        {
            result = (List)obj;
        }
        return result;        
    }

    /**
     * Gets default history limit for this module-issue type.
     */
    public int getHistoryLimit() throws Exception
    {
        int limit=0;
        try
        {
            limit = getModule().getRModuleIssueType(getIssueType())
                    .getHistory();
        }
        catch (Exception e)
        {
            log().error("Issue.getHistoryLimit(): " + e);
        }
        
        return limit;
    }
        
    /**
     * Determines whether the history list is longer than
     * The default limit.
     */
    public boolean isHistoryLong() throws Exception
    {
        return isHistoryLong(getHistoryLimit());
    }

    /**
     * Determines whether the history list is longer than
     * The limit.
     */
    public boolean isHistoryLong(int limit) throws Exception
    {
        return (getActivity(true).size() > limit);
    }

    /**
     * Returns list of Activity objects associated with this Issue.
     */
    public List getActivity() throws Exception  
    {
        return getActivity(false, getHistoryLimit());
    }

    /**
     * Returns limited list of Activity objects associated with this Issue.
     */
    public List getActivity(int limit) throws Exception  
    {
        return getActivity(false, limit);
    }

    /**
     * Returns limited list of Activity objects associated with this Issue.
     * If fullHistory is false, it limits it,
     * (this is the default)
     */
    public List getActivity(boolean fullHistory) throws Exception  
    {
        return getActivity(fullHistory, getHistoryLimit());
    }


    /**
     * Returns full list of Activity objects associated with this Issue.
     */
    private List getActivity(boolean fullHistory, int limit) throws Exception  
    {
        List result = null;
        Boolean fullHistoryObj = fullHistory ? Boolean.TRUE : Boolean.FALSE;
        Object obj = getMethodResult().get(this, GET_ACTIVITY, fullHistoryObj,
                                     new Integer(limit)); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria()
                .add(ActivityPeer.ISSUE_ID, getIssueId())
                .addAscendingOrderByColumn(ActivityPeer.TRANSACTION_ID);
            if (!fullHistory)
            {
                crit.setLimit(limit);
            }
            result = ActivityPeer.doSelect(crit);
            getMethodResult().put(result, this, GET_ACTIVITY, fullHistoryObj,
                            new Integer(limit));
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    /**
     * Returns limited list of Activity objects associated with this Issue.
     */
    public void addActivity(Activity activity) throws TorqueException  
    {
        List activityList = null;
        try
        {
            activityList = getActivity(true);
        }
        catch (Exception e)
        {
            throw new TorqueException(e);
        }
        super.addActivity(activity);
        if (!activityList.contains(activity))
        {
            activityList.add(activity);
        }
    }

    /**
     * Returns a list of ActivitySet objects associated to this issue.
     */
    public List getActivitySets()
        throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_TRANSACTIONS);
        if (obj == null)
        {
            Criteria crit = new Criteria();
            crit.add(ActivityPeer.ISSUE_ID, getIssueId());
            crit.addJoin(ActivitySetPeer.TRANSACTION_ID, ActivityPeer.TRANSACTION_ID);
            crit.setDistinct();
            result = ActivitySetPeer.doSelect(crit);
            ScarabCache.put(result, this, GET_TRANSACTIONS);
        }
        else
        {
            result = (List)obj;
        }
        return result;
    }

    /**
     * Creates a new ActivitySet object for the issue.
     */
    public ActivitySet getActivitySet(ScarabUser user, Attachment attachment,
                                      NumberKey type)
        throws Exception
    {
        ActivitySet activitySet = null;
        if (attachment == null)
        {
            activitySet = ActivitySetManager
                .getInstance(type, user);
        }
        else
        {
            activitySet = ActivitySetManager
                .getInstance(type, user, attachment);
        }
        return activitySet;
    }

    /**
     * Creates a new ActivitySet object for the issue.
     */
    public ActivitySet getActivitySet(ScarabUser user, NumberKey type)
        throws Exception
    {
        return getActivitySet(user, null, type);
    }

    /**
     * Returns the combined output from getChildren() and getParents()
     */
    public List getAllDependencies()
        throws Exception
    {
        List dependencies = new ArrayList();
        dependencies.addAll(getChildren());
        dependencies.addAll(getParents());
        return dependencies;
    }

    /**
     * Returns list of child dependencies
     * i.e., related to this issue through the DEPEND table.
     */
    public List getChildren() throws Exception  
    {
        List result = null;
        Object obj = getMethodResult().get(this, GET_CHILDREN); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria()
                .add(DependPeer.OBSERVED_ID, getIssueId())
                .add(DependPeer.DELETED, false);
            result = DependPeer.doSelect(crit);
            getMethodResult().put(result, this, GET_CHILDREN);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }


    /**
     * Returns list of parent dependencies
     * i.e., related to this issue through the DEPEND table.
     */
    public List getParents() throws Exception  
    {
        List result = null;
        Object obj = getMethodResult().get(this, GET_PARENTS); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria()
                .add(DependPeer.OBSERVER_ID, getIssueId())
                .add(DependPeer.DELETED, false);
            result = DependPeer.doSelect(crit);
            getMethodResult().put(result, this, GET_PARENTS);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }
        

    /**
     * Returns list of all types of dependencies an issue can have
     * On another issue.
     * @deprecated use DependencyTypeManager.getAll();
     */
    public List getAllDependencyTypes() throws Exception
    {
        return DependTypeManager.getAll();
    }

    public ActivitySet doAddDependency(ActivitySet activitySet, Depend depend, 
                                       Issue childIssue, ScarabUser user)
        throws Exception
    {
        // Check whether the entered issue is already dependant on this
        // Issue. If so, and it had been marked deleted, mark as undeleted.
        Depend prevDepend = this.getDependency(childIssue);
        if (prevDepend != null && prevDepend.getDeleted())
        {
            prevDepend.setDefaultModule(depend.getDefaultModule());
            prevDepend.setDescription(depend.getDescription());
            prevDepend.setDeleted(false);
            depend = prevDepend;
        }
        else if (prevDepend != null)
        {
            throw new Exception("This issue already has a dependency" 
                                  + " on the issue id you entered.");
        }

        depend.save();

        if (activitySet == null)
        {
            // deal with user comments
            Attachment comment = depend.getDescriptionAsAttachment(user, this);
            // Save activitySet record
            activitySet = getActivitySet(user, comment,
                              ActivitySetTypePeer.EDIT_ISSUE__PK);
            activitySet.save();
        }

        // Save activitySet record for parent
        Object[] args = {
            depend.getDependType().getName(),
            childIssue.getUniqueId()
        };

        String desc = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            Locale.getDefault(),
            "AddParentDependency", args);

        // Save activity record
        Activity activity = ActivityManager
            .createAddDependencyActivity(this, activitySet, depend, desc);

        // Save activitySet record for child
        Object[] args2 = {
            depend.getDependType().getName(),
            this.getUniqueId()
        };
        desc = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            Locale.getDefault(),
            "AddChildDependency", args2);

        // Save activity record
        ActivityManager
            .createAddDependencyActivity(childIssue, activitySet, depend, desc);

        return activitySet;
    }

    /**
     * Returns type of dependency the passed-in issue has on
     * This issue.
     */
    public Depend getDependency(Issue childIssue) throws Exception
    {
        Depend result = null;
        Object obj = ScarabCache.get(this, GET_DEPENDENCY, childIssue); 
        if ( obj == null ) 
        {
            Criteria crit = new Criteria(2)
                .add(DependPeer.OBSERVED_ID, getIssueId() )        
                .add(DependPeer.OBSERVER_ID, childIssue.getIssueId() );
            List depends = DependPeer.doSelect(crit);
            
            Criteria crit2 = new Criteria(2)
                .add(DependPeer.OBSERVER_ID, getIssueId() )        
                .add(DependPeer.OBSERVED_ID, childIssue.getIssueId() );
            List depends2 = DependPeer.doSelect(crit2);
            
            if (depends.size() > 0 )
            {
                result = (Depend)depends.get(0);
            }
            else if (depends2.size() > 0 )
            {
                result = (Depend)depends2.get(0);
            }
            ScarabCache.put(result, this, GET_DEPENDENCY, childIssue);
        }
        else 
        {
            result = (Depend)obj;
        }
        return result;
    }

    /**
     * Removes any unset attributes and sets the issue # prior to saving
     * for the first time.  Calls super.save()
     *
     * @param dbCon a <code>DBConnection</code> value
     * @exception TorqueException if an error occurs
     */
    public void save(Connection dbCon)
        throws TorqueException
    {
        Module module = getModule();
        if (!module.allowsIssues() || (isNew() && !module.allowsNewIssues())) 
        {
            throw new UnsupportedOperationException(module.getName() + 
                " does not allow issues.");
        }
        
        // remove unset AttributeValues before saving
        List attValues = getAttributeValues();
        // reverse order since removing from list
        for ( int i=attValues.size()-1; i>=0; i-- ) 
        {
            AttributeValue attVal = (AttributeValue) attValues.get(i);
            if ( !attVal.isSet() ) 
            {
                attValues.remove(i);
            }
        }

        if ( isNew() ) 
        {
            // set the issue id
            setIdDomain(module.getDomain());
            setIdPrefix(module.getCode());
            try
            {
                setIdCount(getNextIssueId(dbCon));
            }
            catch (Exception e)
            {
                throw new TorqueException(e);
            }
        }
        super.save(dbCon);
    }


    private int getNextIssueId(Connection con)
        throws Exception
    {
        int id = -1;
        String key = getIdTableKey();
        DatabaseMap dbMap = IssuePeer.getTableMap().getDatabaseMap();
        IDBroker idbroker = dbMap.getIDBroker();
        try
        {
            id = idbroker.getIdAsInt(con, key);
        }
        catch (Exception e)
        {
            synchronized (idbroker)
            {
                try
                {
                    id = idbroker.getIdAsInt(con, key);
                }
                catch (Exception idRetrievalErr)
                {
                    // a module code entry in the id_table was likely not 
                    // entered, insert a row into the id_table and try again.
                    try
                    {
                        saveIdTableKey(con);
                        id = 1;
                    }
                    catch (Exception badException)
                    {
                        log().error("Could not get an id, even after "
                            +"trying to add a module entry into the ID_TABLE", 
                            e);
                        log()
                            .error("Error trying to create ID_TABLE entry for "
                                   + getIdTableKey(), badException);
                        // throw the original
                        throw new ScarabException(
                            "Error retrieving an id for the new issue.  " + 
                            "Please check turbine.log for reasons.", 
                            badException);
                    }
                }
            }
        }
        return id;
    }

    private String getIdTableKey()
        throws Exception
    {
        Module module = getModule();        
        String prefix = module.getCode();

        String domain = module.getDomain();            
        if ( domain != null && domain.length() > 0 ) 
        { 
            prefix = domain + "-" + prefix;
        }
        return prefix;
    }

    private void saveIdTableKey(Connection dbCon)
        throws Exception
    {
        int id = 0;
        DatabaseMap dbMap = IssuePeer.getTableMap().getDatabaseMap();
        IDBroker idbroker = dbMap.getIDBroker();
        String idTable = IDBroker.TABLE_NAME.substring(0, 
             IDBroker.TABLE_NAME.indexOf('.'));
        id = idbroker.getIdAsInt(dbCon, idTable);

        String key = getIdTableKey();

        // FIXME: UGLY! IDBroker doesn't have a Peer yet.
        String sql = "insert into " + idTable 
         + " (ID_TABLE_ID,TABLE_NAME,NEXT_ID,QUANTITY) "
         + " VALUES (" + id + ",'" + key + "',2,1)" ;
        BasePeer.executeStatement(sql, dbCon);
    }

    /**
     * Returns list of issue template types.
     */
    public List getTemplateTypes() throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_TEMPLATE_TYPES); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria()
                .add(IssueTypePeer.ISSUE_TYPE_ID, 
                     IssueType.ISSUE__PK, Criteria.NOT_EQUAL);
            result = IssueTypePeer.doSelect(crit);
            ScarabCache.put(result, this, GET_TEMPLATE_TYPES);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }


    /**
     * Get IssueTemplateInfo by Issue Id.
     */
    public IssueTemplateInfo getTemplateInfo() 
          throws Exception
    {
        IssueTemplateInfo result = null;
        Object obj = ScarabCache.get(this, GET_TEMPLATEINFO); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria(1);
            crit.add(IssueTemplateInfoPeer.ISSUE_ID, getIssueId());
            result = (IssueTemplateInfo)IssueTemplateInfoPeer
                .doSelect(crit).get(0);
            ScarabCache.put(result, this, GET_TEMPLATEINFO);
        }
        else 
        {
            result = (IssueTemplateInfo)obj;
        }
        return result;
    }

    /**
     *  Move or copy issue to destination module.
     */
    public Issue move(Module newModule, IssueType newIssueType,
                      String action, ScarabUser user, String reason,
                      List commentAttrs)
          throws Exception
    {
        Issue newIssue;
        StringBuffer descBuf = null;
        StringBuffer descBuf2 = null;
        Attachment attachment = new Attachment();

        Module oldModule = getModule();
        newIssue = newModule.getNewIssue(newIssueType);
        newIssue.save();
        Attribute zeroAttribute = AttributeManager
            .getInstance(new NumberKey("0"));

        // Save activitySet record
        ActivitySet activitySet = ActivitySetManager
            .getInstance(ActivitySetTypePeer.CREATE_ISSUE__PK, getCreatedBy());
        activitySet.save();
        
        // Copy over attributes
        List matchingAttributes = getMatchingAttributeValuesList(newModule, 
                                                                 newIssueType);

        for (int i=0;i<matchingAttributes.size();i++)
        {
           AttributeValue attVal = (AttributeValue) matchingAttributes
                                                    .get(i);
           AttributeValue newAttVal = attVal.copy();
           newAttVal.setIssueId(newIssue.getIssueId());
           newAttVal.startActivitySet(activitySet);
           newAttVal.save();
        }

        // Generate comment to deal with attributes that do not
        // Exist in destination module, as well as the user attributes.
        StringBuffer attachmentBuf = new StringBuffer();
        StringBuffer delAttrsBuf = new StringBuffer();
        if (reason != null && reason.length() > 0)
        {
            attachmentBuf.append(reason).append(". ");
        }
        if (commentAttrs.size() > 0)
        {
            attachmentBuf.append(Localization.getString(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               Locale.getDefault(),
               "DidNotCopyAttributes"));
            attachmentBuf.append("\n");
            for (int i=0;i<commentAttrs.size();i++)
            {
                AttributeValue attVal = getAttributeValue((Attribute)commentAttrs.get(i));
                String field = null;
                delAttrsBuf.append(attVal.getAttribute().getName());
                field = attVal.getValue();
                delAttrsBuf.append("=").append(field).append(". ").append("\n");
           }
           String delAttrs = delAttrsBuf.toString();
           attachmentBuf.append(delAttrs);

           // Also create a regular comment with non-matching attribute info
           Attachment comment = new Attachment();
           comment.setTextFields(user, newIssue, Attachment.COMMENT__PK);

           StringBuffer commentBuf = new StringBuffer(Localization.format(
              ScarabConstants.DEFAULT_BUNDLE_NAME,
              Locale.getDefault(),
              "DidNotCopyAttributesFromArtifact", getUniqueId()));
           commentBuf.append("\n").append(delAttrs);
           comment.setData(commentBuf.toString());
           comment.setName(Localization.getString(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               Locale.getDefault(),
               "Comment"));
           comment.save();
        }
        else
        {
            attachmentBuf.append(Localization.getString(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               Locale.getDefault(),
               "AllCopied"));
        }
        attachment.setData(attachmentBuf.toString()); 
            
        if (action.equals("move"))
        {
            attachment.setName(Localization.getString(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               Locale.getDefault(),
               "MovedIssueNote"));
        }
        else
        {
            attachment.setName(Localization.getString(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               Locale.getDefault(),
               "CopiedIssueNote"));
        }
        attachment.setTextFields(user, newIssue, Attachment.MODIFICATION__PK);
        attachment.save();

        // copy attachments: comments/files etc.
        Iterator attachments = getAttachments().iterator();
        while (attachments.hasNext()) 
        {
            Attachment oldA = (Attachment)attachments.next();
            Attachment newA = oldA.copy();
            newA.setIssueId(newIssue.getIssueId());
            newA.save();
            if (Attachment.FILE__PK.equals(newA.getTypeId())) 
            {
                oldA.copyFileTo(newA.getFullPath());
            }
        }

        // Create activitySet for the MoveIssue activity
        ActivitySet activitySet2 = ActivitySetManager
            .getInstance(ActivitySetTypePeer.MOVE_ISSUE__PK, user, attachment);
        activitySet2.save();

        // Generate comment
        // If moving issue, delete original
        String comment = null;
        String comment2 = null;
        if (action.equals("copy"))
        {
            Object[] args3= {"copied", "from"};
            comment = Localization.format(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               Locale.getDefault(),
               "MoveCopyString", args3);
            Object[] args4= {"copied", "to"};
            comment2 = Localization.format(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               Locale.getDefault(),
               "MoveCopyString", args4);
        }
        else
        {
            Object[] args5= {"moved", "from"};
            comment = Localization.format(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               Locale.getDefault(),
               "MoveCopyString", args5);
            Object[] args6 = {"moved", "to"};
            comment2 = Localization.format(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               Locale.getDefault(),
               "MoveCopyString", args6);
            // delete original issue
            delete(user);
        }

        // Save activity record
        Object[] args = {
            comment,
            getUniqueId(),
            oldModule.getName(),
            getIssueType().getName()
        };
        String desc = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            Locale.getDefault(),
            "MovedIssueDescription", args);

        ActivityManager
            .createTextActivity(newIssue, zeroAttribute, activitySet2,
                                desc, null,
                                getUniqueId(), newIssue.getUniqueId());

        // Save activity record for old issue
        Object[] args2 = {
            comment2,
            newIssue.getUniqueId(),
            newModule.getName(),
            newIssueType.getName()
        };
        desc = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            Locale.getDefault(),
            "MovedIssueDescription", args2);

        ActivityManager
            .createTextActivity(this, zeroAttribute, activitySet2,
                                desc, null,
                                getUniqueId(), newIssue.getUniqueId());

        return newIssue;
    }

    /**
     * The Date when this issue was closed.
     *
     * @return a <code>Date</code> value, null if status has not been
     * set to Closed
     */
    public Date getClosedDate()
        throws Exception
    {
        Date result = null;
        Object obj = ScarabCache.get(this, GET_CLOSED_DATE); 
        if ( obj == null ) 
        {  
            Attribute attribute = null;
            try
            { 
                attribute = AttributeManager
                    .getInstance(AttributePeer.STATUS__PK);
            }
            catch (TorqueException e)
            {
                if (e.getMessage() == null 
                    || !e.getMessage().startsWith("Failed to select one")) 
                {
                    throw e;
                }
                // closed date has no meaning
                return null;
            }

            AttributeValue status = getAttributeValue(attribute);
            if ( status != null && status.getOptionId()
                 .equals(AttributeOption.STATUS__CLOSED__PK) ) 
            {
                // the issue is currently closed, we can get the date
                Criteria crit = new Criteria()
                    .add(ActivityPeer.ISSUE_ID, getIssueId())
                    .add(ActivityPeer.ATTRIBUTE_ID, AttributePeer.STATUS__PK)
                    .addJoin(ActivityPeer.TRANSACTION_ID, 
                             ActivitySetPeer.TRANSACTION_ID)
                    .add( ActivityPeer.NEW_OPTION_ID, 
                      AttributeOption.STATUS__CLOSED__PK )
                    .addDescendingOrderByColumn(ActivitySetPeer.CREATED_DATE);
                
                List activitySets = ActivitySetPeer.doSelect(crit);
                if ( activitySets.size() > 0 ) 
                {
                    result = ((ActivitySet)activitySets.get(0))
                        .getCreatedDate();
                    ScarabCache.put(result, this, GET_CLOSED_DATE);
                }
                else 
                {
                    throw new ScarabException("Issue " + getIssueId() + 
                        " was in a closed state, but" +
                        "no activitySet is associated with the change.");   
                }
            }          
        }
        else 
        {
            result = (Date)obj;
        }
        return result;
    }

    public void addVote(ScarabUser user)
        throws ScarabException, Exception
    {
        // check to see if the user has voted for this issue
        int previousVotes = 0;
        IssueVote issueVote = null;
        Criteria crit = new Criteria()
            .add(IssueVotePeer.ISSUE_ID, getIssueId())
            .add(IssueVotePeer.USER_ID, user.getUserId());
        List votes = IssueVotePeer.doSelect(crit);
        if ( votes != null && votes.size() != 0 ) 
        {
            issueVote = (IssueVote)votes.get(0);
            previousVotes = issueVote.getVotes();
        }
        else 
        {
            issueVote = new IssueVote();
            issueVote.setIssueId(getIssueId());
            issueVote.setUserId(user.getUserId());
        }

        // check if the module accepts multiple votes
        if ( !getModule().allowsMultipleVoting() && previousVotes > 0 )
        {
            throw new ScarabException("User " + user.getUserName() + 
                " attempted to vote multiple times for issue " + getUniqueId()
                + " which was not allowed in this project.");
        }
        
        // save the user's vote
        issueVote.setVotes(previousVotes+1);
        issueVote.save();

        // update the total votes for the issue
        crit = new Criteria()
            .add(AttributeValuePeer.ATTRIBUTE_ID, 
                 AttributePeer.TOTAL_VOTES__PK);
        List voteValues = getAttributeValues(crit);
        TotalVotesAttribute voteValue = null;
        if ( voteValues.size() == 0 ) 
        {
            voteValue = new TotalVotesAttribute();
            voteValue.setIssue(this);
            voteValue.setAttributeId(AttributePeer.TOTAL_VOTES__PK);
        }
        else 
        {
            voteValue = (TotalVotesAttribute)voteValues.get(0);
        }
        // Updating attribute values requires a activitySet
        ActivitySet activitySet = ActivitySetManager
            .getInstance(ActivitySetTypePeer.RETOTAL_ISSUE_VOTE__PK, user);
        activitySet.save();
        voteValue.startActivitySet(activitySet);
        voteValue.addVote();
        voteValue.save();
    }


    /**
     * Gets a list of non-user AttributeValues which match a given Module.
     * It is used in the MoveIssue2.vm template
     */
    public List getMatchingAttributeValuesList(Module newModule, 
                                               IssueType newIssueType)
          throws Exception
    {
        AttributeValue aval = null;
        List matchingAttributes = new ArrayList();

        HashMap setMap = this.getAttributeValuesMap();
        Iterator iter = setMap.keySet().iterator();
        while ( iter.hasNext() ) 
        {
            aval = (AttributeValue)setMap.get(iter.next());
            List values = getAttributeValues(aval.getAttribute());
            // loop thru the values for this attribute
            for (int i = 0; i<values.size(); i++)
            {
                AttributeValue attVal = (AttributeValue)values.get(i);
                RModuleAttribute modAttr = newModule.
                    getRModuleAttribute(aval.getAttribute(), newIssueType);
                
                // If this attribute is active for the destination module,
                // Add to matching attributes list
                if (modAttr != null && modAttr.getActive())
                {
                    // If attribute is an option attribute,
                    // Check if attribute option is active for destination module.
                    if (aval instanceof OptionAttribute)
                    {
                        Criteria crit2 = new Criteria(1)
                            .add(RModuleOptionPeer.ACTIVE, true);
                        RModuleOption modOpt = (RModuleOption)RModuleOptionPeer
                                                .doSelect(crit2).get(0);
                        if (modOpt.getActive())
                        {
                            matchingAttributes.add(attVal);
                        } 
                    }
                    else if (attVal instanceof UserAttribute)
                    {
                        ScarabUser user = null;
                        try
                        {
                            user = ScarabUserManager.getInstance(attVal.getUserId());
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        Attribute attr = attVal.getAttribute();
                        ScarabUser[] userArray = newModule.getUsers(attr.getPermission());
                        // If user exists in destination module with this permission,
                        // Add as matching value
                        if (Arrays.asList(userArray).contains(user))
                        {
                            matchingAttributes.add(attVal);
                        }
                    }
                    else
                    {
                        matchingAttributes.add(attVal);
                    }
                } 
            } 
        }
        return matchingAttributes;
    }

    public List getMatchingAttributeValuesList(String moduleId, String issueTypeId)
          throws Exception
    {
         Module module = ModuleManager.getInstance(new NumberKey(moduleId)); 
         IssueType issueType = IssueTypeManager.getInstance(new NumberKey(issueTypeId)); 
         return getMatchingAttributeValuesList(module, issueType);
    }

    /**
     * Gets a list AttributeValues which the source module has,
     * But the destination module does not have, when doing a copy.
     * It is used in the MoveIssue2.vm template
     */
    public List getOrphanAttributeValuesList(Module newModule, 
                                             IssueType newIssueType)
          throws Exception
    {
        List orphanAttributes = new ArrayList();
        AttributeValue aval = null;
            
        HashMap setMap = this.getAttributeValuesMap();
        Iterator iter = setMap.keySet().iterator();
        while ( iter.hasNext() ) 
        {
            aval = (AttributeValue)setMap.get(iter.next());
            List values = getAttributeValues(aval.getAttribute());
            // loop thru the values for this attribute
            for (int i = 0; i<values.size(); i++)
            {
                AttributeValue attVal = (AttributeValue)values.get(i);
                RModuleAttribute modAttr = newModule.
                    getRModuleAttribute(aval.getAttribute(), newIssueType);
                
                // If this attribute is not active for the destination module,
                // Add to orphanAttributes list
                if (modAttr == null || !modAttr.getActive())
                {
                    orphanAttributes.add(attVal);
                } 
                else
                {
                    // If attribute is an option attribute, Check if 
                    // attribute option is active for destination module.
                    if (attVal instanceof OptionAttribute) 
                    {
                        Criteria crit2 = new Criteria(1)
                            .add(RModuleOptionPeer.ACTIVE, true);
                        RModuleOption modOpt = (RModuleOption)RModuleOptionPeer
                            .doSelect(crit2).get(0);
                        if (!modOpt.getActive())
                        {
                                orphanAttributes.add(attVal);
                        } 
                    }
                    else if (attVal instanceof UserAttribute)
                    {
                        ScarabUser user = null;
                        try
                        {
                            user = ScarabUserManager.getInstance(attVal.getUserId());
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        Attribute attr = attVal.getAttribute();
                        ScarabUser[] userArray = newModule.getUsers(attr.getPermission());
                        // If user exists in destination module with this permission,
                        // Add as matching value
                        if (!Arrays.asList(userArray).contains(user))
                        {
                            orphanAttributes.add(attVal);
                        }
                    }
                }
            }
        }
        return orphanAttributes;
    }

    public List getOrphanAttributeValuesList(String moduleId, String issueTypeId)
          throws Exception
    {
         Module module = ModuleManager.getInstance(new NumberKey(moduleId)); 
         IssueType issueType = IssueTypeManager.getInstance(new NumberKey(issueTypeId)); 
         return getOrphanAttributeValuesList(module, issueType);
    }

    /**
     * Checks permission and approves or rejects issue template. If template
     * is approved, template type set to "module", else set to "personal".
     */
    public void approve( ScarabUser user, boolean approved )
         throws Exception, ScarabException

    {                
        Module module = getModule();

        if (user.hasPermission(ScarabSecurity.ITEM__APPROVE, module))
        {
            IssueTemplateInfo templateInfo = getTemplateInfo();
            templateInfo.setApproved(true);
            templateInfo.save();
            if (approved)
            {        
                setTypeId(IssueType.MODULE_TEMPLATE__PK);
            }
            save();
        } 
        else
        {
            throw new ScarabException(ScarabConstants.NO_PERMISSION_MESSAGE);
        }            
    }

    /**
     * Checks if user has permission to delete issue template.
     * Only the creating user can delete a personal template.
     * Only project owner or admin can delete a project-wide template.
     */
    public void delete( ScarabUser user )
         throws Exception, ScarabException
    {                
        Module module = getModule();

        if (user.hasPermission(ScarabSecurity.ITEM__APPROVE, module)
          || (user.equals(getCreatedBy()) 
             && getTypeId().equals(IssueType.USER_TEMPLATE__PK)))
        {
            setDeleted(true);
            save();
        } 
        else
        {
            throw new ScarabException(ScarabConstants.NO_PERMISSION_MESSAGE);
        }            
    }


    /**
     * if this RMA is the chosen attribute for email subjects then return
     * true.  if not explicitly chosen, check the other RMA's for this module
     * and if none is chosen as the email attribute, choose the highest
     * ordered text attribute.
     *
     * @return the AttributeValue to use as the email subject, or null
     * or null if no suitable AttributeValue could be found. 
     */
    public AttributeValue getDefaultTextAttributeValue()
        throws Exception
    {
        AttributeValue result = null;
        Object obj = ScarabCache.get(this, GET_DEFAULT_TEXT_ATTRIBUTEVALUE); 
        if ( obj == null ) 
        {        
            Attribute defaultTextAttribute = 
                getModule().getDefaultTextAttribute(getIssueType());
            
            if ( defaultTextAttribute != null ) 
            {
                ObjectKey attributeId = defaultTextAttribute.getAttributeId();
                List avs = getAttributeValues();
                for ( int i=0; i<avs.size(); i++ ) 
                {
                    AttributeValue testAV = (AttributeValue)avs.get(i);
                    if ( attributeId.equals(testAV.getAttributeId()) ) 
                    {
                        result = testAV;
                    }
                }
            }
            ScarabCache.put(result, this, GET_DEFAULT_TEXT_ATTRIBUTEVALUE);
        }
        else 
        {
            result = (AttributeValue)obj;
        }
        return result;
    }


    public String getDefaultText()
        throws Exception
    {
        String result = null;
        Object obj = ScarabCache.get(this, GET_DEFAULT_TEXT); 
        if ( obj == null ) 
        {        
            AttributeValue emailAV = getDefaultTextAttributeValue();
            if ( emailAV != null ) 
            {
                result = emailAV.getValue();
            }        
            result = (result == null ? "" : result);
            ScarabCache.put(result, this, GET_DEFAULT_TEXT);
        }
        else 
        {
            result = (String)obj;
        }
        return result;
    }


    private MethodResultCache getMethodResult()
    {
        return IssueManager.getMethodResult();
    }

    // *******************************************************************
    // Permissions methods - these are deprecated
    // *******************************************************************

    /**
     * Checks if user has permission to enter issue.
     * @deprecated user.hasPermission(ScarabSecurity.ISSUE__ENTER, module)
     */
    public boolean hasEnterPermission( ScarabUser user, Module module)
        throws Exception
    {                
        boolean hasPerm = false;

        if (user.hasPermission(ScarabSecurity.ISSUE__ENTER, module))
        {
             hasPerm = true;
        } 
        return hasPerm;
    }


    /**
     * Checks if user has permission to edit issue.
     * @deprecated user.hasPermission(ScarabSecurity.ISSUE__EDIT, module)
     */
    public boolean hasEditPermission( ScarabUser user, Module module)
        throws Exception
    {                
        boolean hasPerm = false;

        if (user.hasPermission(ScarabSecurity.ISSUE__EDIT, module)
            || user.equals(getCreatedBy()))
        {
            hasPerm = true;
        } 
        return hasPerm;
    }

    /**
     * Checks if user has permission to move issue to destination module.
     * @deprecated user.hasPermission(ScarabSecurity.ISSUE__EDIT, module)
     */
    public boolean hasMovePermission( ScarabUser user, Module module)
        throws Exception
    {                
        boolean hasPerm = false;

        if (user.hasPermission(ScarabSecurity.ISSUE__EDIT, module)
            || user.equals(getCreatedBy()))
        {
            hasPerm = true;
        } 
        return hasPerm;
    }

    /**
     * Assigns user to issue.
     */
    public ActivitySet assignUser(ActivitySet activitySet, 
                                  ScarabUser assignee, ScarabUser assigner,
                                  Attribute attribute, Attachment attachment)
        throws Exception
    {                
        UserAttribute attVal = new UserAttribute();

        // Save activitySet if it has not been already
        if (activitySet.getActivitySetId() == null)
        { 
            activitySet = ActivitySetManager
                .getInstance(ActivitySetTypePeer.EDIT_ISSUE__PK, assigner, 
                             attachment);
            activitySet.save();
            attVal.startActivitySet(activitySet);
        }

        // Save activity record
        String actionString = getAssignUserChangeString(assigner, assignee, 
                                                        attribute);
        ActivityManager
            .createUserActivity(this, attribute, activitySet,
                                actionString, new Attachment(),
                                null, assignee.getUserId());

        // Save user attribute values
        attVal.setIssue(this);
        attVal.setAttributeId(attribute.getAttributeId());
        attVal.setUserId(assignee.getUserId());
        attVal.setValue(assignee.getUserName());
        attVal.save();

        return activitySet;
    }

    /**
     * Get the message that is emailed to associated users,
     * And that is saved in the activity description,
     * When a user is assigned.
     */
    public String getAssignUserChangeString(ScarabUser assigner,
                                            ScarabUser assignee,
                                            Attribute attr)
        throws Exception
    {
        String attrDisplayName = getModule()
              .getRModuleAttribute(attr, getIssueType()).getDisplayValue();
        Object[] args = {
            assigner.getUserName(),
            assignee.getUserName(),
            attrDisplayName
        };
        String actionString = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            Locale.getDefault(),
            "AssignIssueEmailAddedUserAction", args);
        return actionString;
    }


    /**
     * Used to change a user attribute value from one user attribute
     * to a new one. 
     */
    public ActivitySet changeUserAttributeValue(ActivitySet activitySet,
                                                ScarabUser assignee, 
                                                ScarabUser assigner, 
                                                AttributeValue oldAttVal,
                                                Attribute newAttr,
                                                Attachment attachment)
        throws Exception
    {
        Attribute oldAttr = oldAttVal.getAttribute();

        // Save activitySet if it has not been already
        if (activitySet.getActivitySetId() == null)
        { 
            activitySet = ActivitySetManager
                .getInstance(ActivitySetTypePeer.EDIT_ISSUE__PK, assigner, attachment);
            activitySet.save();
            oldAttVal.startActivitySet(activitySet);
        }

        // Save activity record
        String actionString = getUserAttributeChangeString(assignee, assigner,
                                                           oldAttr, newAttr);
        ActivityManager
            .createUserActivity(this, newAttr, activitySet,
                                actionString, new Attachment(),
                                null, assignee.getUserId());

        // Save assignee value
        oldAttVal.setAttributeId(newAttr.getAttributeId());
        oldAttVal.save();
        
        return activitySet;
    }

    /**
     * Get the message that is emailed to associated users,
     * And that is saved in the activity description,
     * When a user is changed from one user attribute to another.
     */
    public String getUserAttributeChangeString(ScarabUser assigner,
                                               ScarabUser assignee, 
                                               Attribute oldAttr,
                                               Attribute newAttr)
        throws Exception
    {
        String oldAttrDisplayName = getModule()
             .getRModuleAttribute(oldAttr, getIssueType()).getDisplayValue();
        String newAttrDisplayName = getModule()
             .getRModuleAttribute(newAttr, getIssueType()).getDisplayValue();
        Object[] args = {
            assignee.getUserName(), assigner.getUserName(),
            oldAttrDisplayName, newAttrDisplayName
        };
        String actionString = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            Locale.getDefault(),
            "AssignIssueEmailChangedUserAttributeAction", args);
        return actionString;
    }


    /**
     * Used to delete a user attribute value.
     */
    public ActivitySet deleteUser(ActivitySet activitySet, ScarabUser assignee, 
                                  ScarabUser assigner,
                                  AttributeValue attVal, Attachment attachment)
        throws Exception
    {
        Attribute attr = attVal.getAttribute();

        // Save activitySet record if it has not been already
        if (activitySet.getActivitySetId() == null)
        { 
            activitySet = ActivitySetManager
                .getInstance(ActivitySetTypePeer.EDIT_ISSUE__PK, assigner, attachment);
            activitySet.save();
            attVal.startActivitySet(activitySet);
        }

        // Save activity record
        String actionString = getUserDeleteString(assigner, assignee, attr);
        ActivityManager
            .createUserActivity(attVal.getIssue(), attVal.getAttribute(), 
                                activitySet,
                                actionString, new Attachment(),
                                assignee.getUserId(), null);

        // Save assignee value
        attVal.setDeleted(true);
        attVal.save();

        return activitySet;
    }

    /**
     * Get the message that is emailed to associated users,
     * And that is saved in the activity description,
     * When a user is removed from a user attribute.
     */
    public String getUserDeleteString(ScarabUser assigner,
                                      ScarabUser assignee, 
                                      Attribute attr)
        throws Exception
    {
        String attrDisplayName = getModule()
             .getRModuleAttribute(attr, getIssueType())
             .getDisplayValue();
        Object[] args = {
            assigner.getUserName(), assignee.getUserName(),
            attrDisplayName
        };
        String actionString = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            Locale.getDefault(),
            "AssignIssueEmailRemovedUserAction", args);
        return actionString;
    }


    /**
     * Deletes a specific dependency on this issue.
     */
    public ActivitySet doDeleteDependency(ActivitySet activitySet, 
                                          Depend depend, ScarabUser user)
        throws Exception
    {
        Issue otherIssue = IssueManager
                        .getInstance(depend.getObserverId(), false);

        String description = new StringBuffer()
            .append("Deleted '")
            .append(depend.getDependType().getName())
            .append("' dependency that ")
            .append(this.getUniqueId())
            .append(" had on ")
            .append(otherIssue.getUniqueId())
            .toString();

        depend.setDeleted(true);
        depend.save();

        if (activitySet == null)
        {
            // deal with user comments
            Attachment comment = depend.getDescriptionAsAttachment(user, this);

            activitySet = getActivitySet(user, comment,
                              ActivitySetTypePeer.EDIT_ISSUE__PK);
            // Save activitySet record
            activitySet.save();
        }

        ActivityManager
            .createDeleteDependencyActivity(this, activitySet, depend,
                                description);
        ActivityManager
            .createDeleteDependencyActivity(otherIssue, activitySet, depend,
                                description);
        return activitySet;
    }

    /**
     * Given a specific attachment object allow us to update
     * the information in it. If the old matches the new, then
     * nothing is modified.
     */
    public ActivitySet doChangeUrlDescription(ActivitySet activitySet, ScarabUser user,
                                              Attachment attachment, String oldDescription)
        throws Exception
    {
        String newDescription = attachment.getName();
        if (!oldDescription.equals(newDescription))
        {
            String changeDescription = new StringBuffer()
                .append("Changed URL description from '").append(oldDescription).append('\'')
                .append(" to '").append(newDescription).append('\'')
                .toString();
            if (changeDescription.length() > 254)
            { 
                changeDescription = changeDescription.substring(0,249) + "...";
            }
            if (activitySet == null)
            {
                // Save activitySet record
                activitySet = getActivitySet(user, ActivitySetTypePeer.EDIT_ISSUE__PK);
                activitySet.save();
            }
            // Save activity record
            ActivityManager
                .createTextActivity(this, activitySet,
                                    changeDescription, attachment,
                                    oldDescription, newDescription);
        }
        return activitySet;
    }

    /**
     * Given a specific attachment object allow us to update
     * the information in it. If the old matches the new, then
     * nothing is modified.
     */
    public ActivitySet doChangeUrlUrl(ActivitySet activitySet, ScarabUser user,
                                              Attachment attachment, String oldUrl)
        throws Exception
    {
        String newUrl = attachment.getData();
        if (!oldUrl.equals(newUrl))
        {
            String changeDescription = new StringBuffer()
                .append("Changed URL from '").append(oldUrl).append('\'')
                .append(" to '").append(newUrl).append('\'')
                .toString();
            if (changeDescription.length() > 254)
            { 
                changeDescription = changeDescription.substring(0,249) + "...";
            }
            if (activitySet == null)
            {
                // Save activitySet record
                activitySet = getActivitySet(user, ActivitySetTypePeer.EDIT_ISSUE__PK);
                activitySet.save();
            }
            // Save activity record
            ActivityManager
                .createTextActivity(this, activitySet,
                                    changeDescription, attachment,
                                    oldUrl, newUrl);
        }
        return activitySet;
    }

    /**
     * changes the dependency type as well as. will not change deptype
     * for deleted deps
     */
    public ActivitySet doChangeDependencyType(ActivitySet activitySet, Depend depend, DependType newDependType,
                                              DependType oldDependType, ScarabUser user)
        throws Exception
    {
        String oldName = oldDependType.getName();
        String newName = newDependType.getName();
        // check to see if something changed
        // only change dependency type for non-deleted deps
        if (!newName.equals(oldName) && depend.getDeleted() == false)
        {
            Issue otherIssue = IssueManager
                            .getInstance(depend.getObserverId(), false);

            depend.save();

            String description = new StringBuffer()
                .append("On issue ")
                .append(this.getUniqueId())
                .append(", the dependency type for issue ")
                .append(otherIssue.getUniqueId())
                .append(" was changed from '")
                .append(oldName)
                .append("' to '")
                .append(newName + '\'')
                .toString();

            if (activitySet == null)
            {
                // deal with user comments
                Attachment comment = depend.getDescriptionAsAttachment(user, this);
    
                activitySet = getActivitySet(user, comment,
                                  ActivitySetTypePeer.EDIT_ISSUE__PK);
                // Save activitySet record
                activitySet.save();
            }
            
            ActivityManager
                .createChangeDependencyActivity(this, activitySet, depend,
                                    description, oldName, newName);
            ActivityManager
                .createChangeDependencyActivity(otherIssue, activitySet, depend,
                                    description, oldName, newName);
        }
        return activitySet;
    }

    /**
     * Sets original AttributeValues for an new issue based on a hashmap of values
     * This is data is saved to the database and the proper ActivitySet is 
     * also recorded.
     *
     * @throws Exception when the workflow has an error to report
     */
    public ActivitySet setInitialAttributeValues(ActivitySet activitySet, 
            Attachment attachment, HashMap newValues, ScarabUser user)
        throws Exception
    {
        // Check new values for workflow
        String msg = doCheckInitialAttributeValueWorkflow(newValues, user);
        if (msg != null)
        {
            throw new Exception(msg);
        }
        
        if (activitySet == null)
        {
            // Save activitySet record
            activitySet = ActivitySetManager
                .getInstance(ActivitySetTypePeer.CREATE_ISSUE__PK, user);
            activitySet.save();
        }

        // enter the values into the activitySet
        SequencedHashMap avMap = getModuleAttributeValuesMap(); 
        Iterator iter = avMap.iterator();
        while (iter.hasNext()) 
        {
            AttributeValue aval = (AttributeValue)avMap.get(iter.next());
            try
            {
                aval.startActivitySet(activitySet);
            }
            catch (ScarabException se)
            {
                throw new Exception("Fatal Error: " + 
                    se.getMessage() + " Please start over.");    
            }
        }

        save();                
        if (attachment.getData() != null 
             && attachment.getData().length() > 0) 
        {
            addComment(activitySet, attachment, user);
        }

        return activitySet;
    }

    /**
     * Sets AttributeValues for an issue based on a hashmap of attribute values
     * This is data is saved to the database and the proper ActivitySet is 
     * also recorded.
     *
     * @throws Exception when the workflow has an error to report
     */
    public ActivitySet setAttributeValues(ActivitySet activitySet, 
                                          HashMap newAttVals, 
                                          Attachment attachment,
                                          ScarabUser user)
        throws Exception
    {
        String msg = doCheckAttributeValueWorkflow(newAttVals, attachment, user);
        if (msg != null)
        {
            throw new Exception(msg);
        }

        // Save explanatory comment
        attachment.setTextFields(user, this, 
                                 Attachment.MODIFICATION__PK);
        attachment.save();

        // Create the ActivitySet
        if (activitySet == null)
        {
            activitySet = getActivitySet(user, attachment,
                                      ActivitySetTypePeer.EDIT_ISSUE__PK);
            activitySet.save();
        }

        SequencedHashMap avMap = getModuleAttributeValuesMap(); 
        AttributeValue oldAttVal = null;
        AttributeValue newAttVal = null;
        Iterator iter = newAttVals.keySet().iterator();
        while (iter.hasNext())
        {
            NumberKey attrId = (NumberKey)iter.next();
            Attribute attr = AttributeManager.getInstance(attrId);
            oldAttVal = (AttributeValue)avMap.get(attr.getName().toUpperCase());
            newAttVal = (AttributeValue)newAttVals.get(attrId);
            String newAttValValue = newAttVal.getValue();
            if (newAttValValue != null && newAttValValue.length() > 0)
            {
                oldAttVal.setProperties(newAttVal);
            }
            else
            {
                oldAttVal.setDeleted(true);
            }
            oldAttVal.startActivitySet(activitySet);
            oldAttVal.save();
        }
        return activitySet;
    }

    /**
     * This method is used with the setInitialAttributeValues() method to 
     * Make sure that workflow is valid for the initial values of a new issue. 
     * It will return a non-null String
     * which is the workflow error message otherwise it will return null.
     */
    public String doCheckInitialAttributeValueWorkflow(HashMap newValues, 
                                                       ScarabUser user)
        throws Exception
    {
        String msg = null;
        Iterator iter = newValues.keySet().iterator();
        while (iter.hasNext())
        {
            NumberKey attrId = (NumberKey)iter.next();
            Attribute attr = AttributeManager.getInstance(new NumberKey(attrId));
            if (attr.isOptionAttribute())
            {
                AttributeOption toOption = AttributeOptionManager
                     .getInstance(new NumberKey((String)newValues.get(attrId)));
                msg = WorkflowFactory.getInstance().checkInitialTransition(
                                                    toOption, this, 
                                                    newValues, user);
            }
            if (msg != null)
            {
                break;
            }
        }
        return msg;
    }

    /**
     * This method is used with the setAttributeValues() method to 
     * Make sure that workflow is valid. It will return a non-null String
     * which is the workflow error message otherwise it will return null.
     */
    public String doCheckAttributeValueWorkflow(HashMap newAttVals, 
                                                Attachment attachment, 
                                                ScarabUser user)
        throws Exception
    {
        SequencedHashMap avMap = getModuleAttributeValuesMap(); 
        AttributeValue oldAttVal = null;
        AttributeValue newAttVal = null;
        String msg = null;
        Iterator iter = newAttVals.keySet().iterator();
        while (iter.hasNext())
        {
            NumberKey attrId = (NumberKey)iter.next();
            Attribute attr = AttributeManager.getInstance(new NumberKey(attrId));
            oldAttVal = (AttributeValue)avMap.get(attr.getName().toUpperCase());
            newAttVal = (AttributeValue)newAttVals.get(attrId);
            AttributeOption fromOption = null;
            AttributeOption toOption = null;

            if (newAttVal.getValue() != null)
            {
                if (oldAttVal.getOptionId() != null 
                    && newAttVal.getAttribute().isOptionAttribute())
                {
                    fromOption = oldAttVal.getAttributeOption();
                    toOption = newAttVal.getAttributeOption();
                    msg = WorkflowFactory.getInstance().checkTransition(
                                                        fromOption, 
                                                        toOption, this, 
                                                        newAttVals, user);
                }
                if (msg != null)
                {
                    break;
                }
            }
        }
        return msg;
    }



    /**
     * If the comment hasn't changed, it will return a valid ActivitySet
     * otherwise it returns null.
     */
    public ActivitySet doEditComment(ActivitySet activitySet, String newComment, 
                                     Attachment attachment, ScarabUser user)
        throws Exception
    {
        String oldComment = attachment.getData();
        if (!newComment.equals(oldComment)) 
        {
            attachment.setData(newComment);
            attachment.save();
           
            // Generate description of modification
            Object[] args = {
                oldComment,
                newComment
            };
            String desc = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME,
                Locale.getDefault(),
                "ChangedComment", args);

            if (activitySet == null)
            {
                 // Save activitySet record
                activitySet = getActivitySet(user,
                                          ActivitySetTypePeer.EDIT_ISSUE__PK);
                activitySet.save();
            }
            // Save activity record
            ActivityManager
                .createTextActivity(this, null, activitySet,
                                    desc, attachment,
                                    oldComment, newComment);
                                    
        }
        return activitySet;
    }

    /**
     * If the URL hasn't changed, it will return a valid ActivitySet
     * otherwise it returns null.
     */
    public ActivitySet doDeleteUrl(ActivitySet activitySet, 
                                     Attachment attachment, ScarabUser user)
        throws Exception
    {
        String oldUrl = attachment.getData();
        attachment.setDeleted(true);
        attachment.save();

        // Generate description of modification
        String name = attachment.getName();
        String desc = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            Locale.getDefault(),
            "UrlDeletedDesc", name);

        if (activitySet == null)
        {
             // Save activitySet record
            activitySet = getActivitySet(user,
                                      ActivitySetTypePeer.EDIT_ISSUE__PK);
            activitySet.save();
        }
        // Save activity record
        ActivityManager
            .createTextActivity(this, null, activitySet,
                                desc, attachment, oldUrl, 
                                Localization.getString(
                                ScarabConstants.DEFAULT_BUNDLE_NAME,
                                Locale.getDefault(),
                                "UrlDeleted"));
        return activitySet;
    }

    /**
     * If the File hasn't changed, it will return a valid ActivitySet
     * otherwise it returns null.
     */
    public ActivitySet doDeleteFile(ActivitySet activitySet, 
                                     Attachment attachment, ScarabUser user)
        throws Exception
    {
        attachment.setDeleted(true);
        attachment.save();

        // Generate description of modification
        String name = attachment.getFileName();
        String path = attachment.getRelativePath();
        Object[] args = {name, path};
        String desc = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            Locale.getDefault(),
            "FileDeletedDesc", args);

        if (activitySet == null)
        {
             // Save activitySet record
            activitySet = getActivitySet(user,
                                      ActivitySetTypePeer.EDIT_ISSUE__PK);
            activitySet.save();
        }
        // Save activity record
        ActivityManager
            .createTextActivity(this, null, activitySet,
                                desc, attachment, name, 
                                Localization.getString(
                                ScarabConstants.DEFAULT_BUNDLE_NAME,
                                Locale.getDefault(),
                                "FileDeleted"));
        return activitySet;
    }


    /**
     * Returns users assigned to all user attributes.
     */
    public List getAssociatedUsers() throws Exception
    {
        List users = null;
        Object obj = ScarabCache.get(this, GET_ASSOCIATED_USERS); 
        if ( obj == null ) 
        {        
            List attributeList = getModule()
                .getUserAttributes(getIssueType(), true);
            List attributeIdList = new ArrayList();
            
            for ( int i=0; i<attributeList.size(); i++ ) 
            {
                Attribute att = (Attribute) attributeList.get(i);
                RModuleAttribute modAttr = getModule().
                    getRModuleAttribute(att, getIssueType());
                if (modAttr.getActive())
                {
                    attributeIdList.add(att.getAttributeId());
                }
            }
            
            if (!attributeIdList.isEmpty())
            {
                users = new ArrayList();
                Criteria crit = new Criteria()
                    .addIn(AttributeValuePeer.ATTRIBUTE_ID, attributeIdList)
                    .add(AttributeValuePeer.DELETED, false);
                crit.setDistinct();
                
                List attValues = getAttributeValues(crit);
                for ( int i=0; i<attValues.size(); i++ ) 
                {
                    List item = new ArrayList(2);
                    AttributeValue attVal = (AttributeValue) attValues.get(i);
                    ScarabUser su = ScarabUserManager.getInstance(attVal.getUserId());
                    Attribute attr = AttributeManager.getInstance(attVal.getAttributeId());
                    item.add(attr);
                    item.add(su);
                    users.add(item);
                }
            }
            ScarabCache.put(users, this, GET_ASSOCIATED_USERS);
        }
        else 
        {
            users = (List)obj;
        }
        return users;
    }
}
