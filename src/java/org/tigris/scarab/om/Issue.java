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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.workingdogs.village.Record;

import org.apache.commons.lang.ObjectUtils;
// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.manager.MethodResultCache;
import org.apache.torque.util.Criteria;
import org.apache.commons.collections.SequencedHashMap;
import org.apache.torque.map.DatabaseMap;
import org.apache.torque.oid.IDBroker;
import org.apache.torque.util.BasePeer;
import org.apache.fulcrum.localization.Localization;

// Scarab classes
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.tools.ScarabGlobalTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.Localizable;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.attribute.TotalVotesAttribute;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.workflow.WorkflowFactory;

import org.apache.commons.lang.StringUtils;

/** 
 * This class represents an Issue.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: Issue.java,v 1.337 2004/05/10 21:28:40 thierrylach Exp $
 */
public class Issue 
    extends BaseIssue
    implements Persistent
{
    // the following Strings are method names that are used in caching results
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
    protected static final String GET_ALL_USERS_TO_EMAIL = 
        "getAllUsersToEmail";
    protected static final String GET_USER_ATTRIBUTEVALUE = 
        "getUserAttributeValue";
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
        "getNonMatchingAttributeValuesList";
    protected static final String GET_DEFAULT_TEXT_ATTRIBUTEVALUE = 
        "getDefaultTextAttributeValue";
    protected static final String GET_DEFAULT_TEXT = 
        "getDefaultText";
    protected static final String GET_NULL_END_DATE =
        "getActivitiesWithNullEndDate";
    protected static final String GET_INITIAL_ACTIVITYSET = 
        "getInitialActivitySet";
    protected static final String GET_HISTORY_LIMIT =
        "getHistoryLimit";

    private static final Integer NUMBERKEY_0 = new Integer(0);
    private static final Integer COPIED = new Integer(1);
    private static final Integer MOVED = new Integer(2);

    /** storage for any attachments which have not been saved yet */
    private List unSavedAttachments = null;
    
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
     * @deprecated use IssueManager.getIssueById
     */
    public static Issue getIssueById(String id)
    {
        return IssueManager.getIssueById(id);
    }

    /**
     * @deprecated use IssueManager.getIssueById
     */
    public static Issue getIssueById(Issue.FederatedId fid)
    {
        return IssueManager.getIssueByIdImpl(fid);
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
        if (getIdDomain() != null) 
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
        private String domainId;
        private String prefix;
        private int count;

        public FederatedId(String id)
        {
            int dash = id.indexOf('-');
            if (dash > 0) 
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
            this.domainId = domain;
            setPrefix(prefix);
            this.count = count;
        }

        /**
         * @param id The unique identifier for this issue, generally a
         * combination of code and sequence number (e.g. SCB37).
         */
        public void setUniqueId(String id)
        {
			int codeLength = ScarabGlobalTool.getModuleCodeLength();
			// we could start at 1 here, if the spec says one char is 
			// required, will keep it safe for now.
			StringBuffer code = new StringBuffer(codeLength);
			int max = id.length() < codeLength ? id.length() : codeLength;
            for (int i = 0; i < max; i++)
            {
                char c = id.charAt(i);
                if (c < '0' || c > '9')
                {
                    code.append(c);
                }
            }
            if (code.length() != 0) 
            {
                setPrefix(code.toString());
            }
            count = Integer.parseInt(id.substring(code.length()));
        }
        
        /**
         * @return The domain.
         */
        public String getDomain()
        {
            return domainId;
        }

        /**
         * @return The prefix (upper-cased).
         */
        public String getPrefix()
        {
            return prefix;
        }
                
        /**
         * @return The sequence of this issue within its code.
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
         * @param prefix The module code.
         */
        public void setPrefix(String prefix)
        {
            if (prefix != null)
            {
                this.prefix = prefix.toUpperCase();
            }
        }
        
        /**
         * @param count The sequence of this issue within its code.
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

    /**
     * @param module The current module.
     * @param theList A textual representation of the list of issues
     * to parse.
     * @param The parsed list of issue identifiers.
     */
    public static List parseIssueList(Module module, String theList)
        throws Exception
    {
        String[] issues = StringUtils.split(theList, ",");
        List results = new ArrayList();
        for (int i = 0; i < issues.length; i++)
        {
            if (issues[i].indexOf('*') != -1)
            {
                // Probably better to use more Torque here, but this
                // is definitely going to be faster and more
                // efficient.
                String sql = "SELECT CONCAT(" + IssuePeer.ID_PREFIX + ',' +
                    IssuePeer.ID_COUNT + ") FROM " + IssuePeer.TABLE_NAME +
                    " WHERE " + IssuePeer.ID_PREFIX + " = '" +
                    module.getCode() + '\'';
                List records = BasePeer.executeQuery(sql);
                for (Iterator j = records.iterator(); j.hasNext();)
                {
                    Record rec = (Record)j.next();
                    results.add(rec.getValue(1).asString());
                }
            }
            // check for a -
            else if (issues[i].indexOf('-') == -1)
            {
                // Make sure user is not trying to access issues from another
                // module.
                FederatedId fid = createFederatedId(module, issues[i]);
                if (!fid.getPrefix().equalsIgnoreCase(module.getCode()))
                {
                    String[] args = { fid.getPrefix(), module.getCode() };
                    throw new Exception(Localization.format
                                        (ScarabConstants.DEFAULT_BUNDLE_NAME,
                                         module.getLocale(),
                                         "IssueIDPrefixNotForModule", args)); //EXCEPTION
                }
                results.add(issues[i]);
            }
            else
            {
                String[] issue = StringUtils.split(issues[i], "-");
                if (issue.length != 2)
                {
                    throw new Exception(Localization.format
                                        (ScarabConstants.DEFAULT_BUNDLE_NAME,
                                         module.getLocale(),
                                         "IssueIDRangeNotValid", issues[i])); //EXCEPTION
                }
                FederatedId fidStart = createFederatedId(module, issue[0]);
                FederatedId fidStop = createFederatedId(module, issue[1]);
                if (!fidStart.getPrefix().equalsIgnoreCase(module.getCode()) ||
                    !fidStop.getPrefix().equalsIgnoreCase(module.getCode()))
                {
                    throw new Exception(Localization.format
                                        (ScarabConstants.DEFAULT_BUNDLE_NAME,
                                         module.getLocale(),
                                         "IssueIDPrefixesNotForModule",
                                         module.getCode())); //EXCEPTION
                }
                else if (!fidStart.getPrefix()
                         .equalsIgnoreCase(fidStop.getPrefix()))
                {
                    String[] args = { fidStart.getPrefix(),
                                      fidStop.getPrefix() };
                    throw new Exception(Localization.format
                                        (ScarabConstants.DEFAULT_BUNDLE_NAME,
                                         module.getLocale(),
                                         "IssueIDPrefixesDoNotMatch", args)); //EXCEPTION
                }
                else if (fidStart.getCount() > fidStop.getCount())
                {
                    FederatedId swap = fidStart;
                    fidStart = fidStop;
                    fidStop = swap;
                }

                for (int j = fidStart.getCount(); j <= fidStop.getCount();j++)
                {
                    results.add(fidStart.getPrefix() + j);
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
            throw new Exception("Invalid federated id: " + id); //EXCEPTION
        }
        return fid;
    }

    /**
     * Whether this issue is an enter issue template.
     */
    public boolean isTemplate()
    {
        boolean isTemplate = false;
        try
        {
            isTemplate = !getIssueType().getParentId().equals(NUMBERKEY_0);
        }
        catch (Exception e)
        {
            getLog().error("Problem determining whether issue is template");
        }
        return isTemplate;
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
        // Generate description of modification
        int length = nameFieldString.length() + 12;
        // strip off the end
        if (length > 254)
        {
            nameFieldString = nameFieldString.substring(0, 238) + "...";
        }
        String desc = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            getLocale(),
            "UrlAddedDesc", nameFieldString);

        // Save activitySet record
        if (activitySet == null)
        {
            activitySet = getActivitySet(user, ActivitySetTypePeer.EDIT_ISSUE__PK);
            activitySet.save();            
        }
        // Save activity record
        ActivityManager
            .createTextActivity(this, activitySet, desc, attachment);
        
        return activitySet;
    }

    private Locale getLocale()
        throws TorqueException
    {
        return getModule().getLocale();
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
        String comment = attachment.getData();
        if (comment == null || comment.length() == 0)
        {
            throw new ScarabException(L10NKeySet.NoDataInComment);
        }
        if (activitySet == null)
        {
            activitySet = getActivitySet(user, 
                            ActivitySetTypePeer.EDIT_ISSUE__PK);
        }
        activitySet.save();

        // create the localized string...
        String desc = Localization.getString(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            getLocale(),
            "AddedCommentToIssue");
        int total = 248 - desc.length();
        if (comment.length() > total)
        {
            comment = comment.substring(0,total) + "...";
        }
        comment = desc + " '" + comment + "'";

        // populates the attachment with data to be a comment
        attachment = AttachmentManager
                        .getComment(attachment, this, user);

        ActivityManager
            .createTextActivity(this, activitySet,
                                comment, attachment);

        if (!activitySet.sendEmail(this))
        {   Localizable commentSaved = new L10NMessage(L10NKeySet.CommentSaved);
            Localizable sendFailed   = new L10NMessage(L10NKeySet.CouldNotSendEmail);
            throw new ScarabException(L10NKeySet.ExceptionSavedButErrors,
                    commentSaved,
                    sendFailed);
        }

        return activitySet;
    }

    /**
     * Adds an attachment file to this issue. Does not perform
     * a save because the issue may not have been created yet.
     * use the doSaveFileAttachment() to save the attachment
     * after the issue has been created.
     */
    public synchronized void addFile(Attachment attachment, 
                                     ScarabUser user)
        throws Exception
    {
        attachment.setTypeId(Attachment.FILE__PK);
        attachment.setCreatedBy(user.getUserId());
        if (unSavedAttachments == null)
        {
            unSavedAttachments = new ArrayList();
        }
        unSavedAttachments.add(attachment);
    }

    /**
     * Overrides the super method in order to allow
     * us to return the unSavedAttachments if they exist.
     */
    public synchronized List getAttachments()
        throws TorqueException
    {
        if (unSavedAttachments != null && 
            unSavedAttachments.size() > 0)
        {
            return unSavedAttachments;
        }
        else
        {
            return super.getAttachments();
        }
    }

    /**
     * Adds an attachment file to this issue. Does not perform
     * a save because the issue may not have been created yet.
     * use the doSaveFileAttachment() to save the attachment
     * after the issue has been created.
     */
    public synchronized ActivitySet doSaveFileAttachments(ScarabUser user)
        throws Exception
    {
        return doSaveFileAttachments(null, user);
    }
    
    /**
     * Adds an attachment file to this issue. Does not perform
     * a save because the issue may not have been created yet.
     * use the doSaveFileAttachment() to save the attachment
     * after the issue has been created.
     */
    public synchronized ActivitySet doSaveFileAttachments(ActivitySet activitySet,
                                                          ScarabUser user)
        throws Exception
    {
        if (unSavedAttachments == null)
        {
            return activitySet;
        }
        if (activitySet == null)
        {
            // Save activitySet record
            activitySet = getActivitySet(user, ActivitySetTypePeer.EDIT_ISSUE__PK);
            activitySet.save();
        }
        Iterator itr = unSavedAttachments.iterator();
        while (itr.hasNext())
        {
            Attachment attachment = (Attachment)itr.next();
            // make sure we set the issue to the newly created issue
            attachment.setIssue(this);
            attachment.save();

            // Generate description of modification
            String name = attachment.getFileName();
            Object[] args = {name};
            String description = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME,
                getLocale(),
                "FileAddedDesc", args);

            // Save activity record
            ActivityManager
                .createTextActivity(this, activitySet, description, attachment);

           
        }
        // reset the super method so that the query has to hit the database again
        // so that all of the information is cleaned up and reset.
        super.collAttachments = null;
        // we don't need this one anymore either.
        this.unSavedAttachments = null;
        return activitySet;
    }

    /** 
     * Remove an attachment file
     * @param index starts with 1 because velocityCount start from 1
     * but ArrayList starts from 0
     */
    public void removeFile(String index)
        throws Exception
    {
        int indexInt = Integer.parseInt(index) - 1;
        if (indexInt >= 0)
        {
            if (unSavedAttachments != null && unSavedAttachments.size() > 0)
            {
                unSavedAttachments.remove(indexInt);
            }
            else
            {
                List attachList = getAttachments();
                if (attachList != null && attachList.size() > 0)
                {
                    attachList.remove(indexInt);
                }
            }
        }
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
            "Should use getModule"); //EXCEPTION
    }

    /**
     * Throws UnsupportedOperationException.  Use
     * <code>setModule(Module)</code> instead.
     *
     */
    public void setScarabModule(ScarabModule module)
    {
        throw new UnsupportedOperationException(
            "Should use setModule(Module). Note module cannot be new."); //EXCEPTION
    }

    /**
     * Use this instead of setScarabModule.  Note: module cannot be new.
     */
    public void setModule(Module me)
        throws TorqueException
    {
        Integer id = me.getModuleId();
        if (id == null) 
        {
            throw new TorqueException("Modules must be saved prior to " +
                                      "being associated with other objects."); //EXCEPTION
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
        Integer id = getModuleId();
        if ( id != null ) 
        {
            module = ModuleManager.getInstance(id);
        }
        
        return module;
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
     * Calls the overloaded version by passing 'true' so that only active
     * attributes will be considered.
     * @see #getModuleAttributeValuesMap(boolean)
     */
    public SequencedHashMap getModuleAttributeValuesMap()
        throws Exception
    {
        return getModuleAttributeValuesMap(true);
    }

    /**
     * AttributeValues that are relevant to the issue's current module.
     * Empty AttributeValues that are relevant for the module, but have
     * not been set for the issue are included.  The values are ordered
     * according to the module's preference
     *
     * @param isActive TRUE if only active attributes need to be considered
     * and FALSE if both active and inactive attributes need to be considered
     */
    public SequencedHashMap getModuleAttributeValuesMap(boolean isActive)
        throws Exception
    {
        SequencedHashMap result = null;
        Object obj = getCachedObject(GET_MODULE_ATTRVALUES_MAP, isActive ? Boolean.TRUE : Boolean.FALSE);
        if (obj == null) 
        {        
            List attributes = null;
            if (isActive)
            {
                attributes = getIssueType().getActiveAttributes(getModule());
            }
            else
            {
                attributes = getModule().getAttributes(getIssueType());
            }
            Map siaValuesMap = getAttributeValuesMap();
            result = new SequencedHashMap((int)(1.25*attributes.size() + 1));
            for (int i=0; i<attributes.size(); i++)
            {
                String key = ((Attribute)attributes.get(i)).getName().toUpperCase();
                if (siaValuesMap.containsKey(key))
                {
                    result.put(key, siaValuesMap.get(key));
                }
                else 
                {
                    AttributeValue aval = AttributeValue
                        .getNewInstance(((Attribute)attributes.get(i)), this);
                    addAttributeValue(aval);
                    siaValuesMap.put(
                        aval.getAttribute().getName().toUpperCase(), aval);
                    result.put(key, aval);
                }
            }
            putCachedObject(result, GET_MODULE_ATTRVALUES_MAP, isActive ? Boolean.TRUE : Boolean.FALSE);
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
        if (obj == null) 
        {        
            if (isNew()) 
            {
                List avals = getAttributeValues();
                if (avals != null) 
                {
                    Iterator i = avals.iterator();
                    while (i.hasNext()) 
                    {
                        AttributeValue tempAval = (AttributeValue)i.next();
                        if (tempAval.getAttribute().equals(attribute)) 
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
                else if (avals.size() > 1)
                {
                    throw new Exception("Error in retrieving users."); //EXCEPTION
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
     * Returns the (undeleted) AttributeValues for the Attribute.
     */
    public List getAttributeValues(Attribute attribute)
       throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_ATTRVALUES, attribute); 
        if (obj == null) 
        {        
            if (isNew()) 
            {
                List avals = getAttributeValues();
                result = new ArrayList();
                if (avals != null) 
                {
                    Iterator i = avals.iterator();
                    while (i.hasNext()) 
                    {
                        AttributeValue tempAval = (AttributeValue)i.next();
                        if (tempAval.getAttribute().equals(attribute)) 
                        {
                            result.add(tempAval);
                        }
                    }
                }
            }
            else 
            {            
                Criteria crit = new Criteria(2)
                    .add(AttributeValuePeer.DELETED, false)        
                    .add(AttributeValuePeer.ATTRIBUTE_ID, 
                         attribute.getAttributeId());
                
                result = getAttributeValues(crit);
                ScarabCache.put(result, this, GET_ATTRVALUES, attribute);
            }
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
     * AttributeValues that are set for this Issue
     */
    public Map getAttributeValuesMap() throws Exception
    {
        Map result = null;
        Object obj = ScarabCache.get(this, GET_ATTRIBUTE_VALUES_MAP); 
        if (obj == null) 
        {
            Criteria crit = new Criteria(2)
                .add(AttributeValuePeer.DELETED, false);        
            List siaValues = getAttributeValues(crit);
            result = new HashMap((int)(1.25*siaValues.size() + 1));
            for (Iterator i = siaValues.iterator(); i.hasNext(); )
            {
                AttributeValue att = (AttributeValue) i.next();
                result.put(att.getAttribute().getName().toUpperCase(), att);
            }

            ScarabCache.put(result, this, GET_ATTRIBUTE_VALUES_MAP);
        }
        else
        {
            result = (Map)obj;
        }
        return result;
    }

    /**
     * AttributeValues that are set for this issue and
     * Empty AttributeValues that are relevant for the module, but have 
     * not been set for the issue are included.
     */
    public Map getAllAttributeValuesMap() 
        throws Exception
    {
        Map moduleAtts = getModuleAttributeValuesMap();
        Map issueAtts = getAttributeValuesMap();
        Map allValuesMap = new HashMap((int)(1.25*(moduleAtts.size() + 
                                            issueAtts.size())+1));

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
        List attributes = getIssueType()
            .getRequiredAttributes(getModule());

        boolean result = true;
        SequencedHashMap avMap = getModuleAttributeValuesMap();
        Iterator i = avMap.iterator();
        while (i.hasNext()) 
        {
            AttributeValue aval = (AttributeValue)avMap.get(i.next());
            
            if (aval.getOptionId() == null && aval.getValue() == null) 
            {
                for (int j=attributes.size()-1; j>=0; j--) 
                {
                    if (aval.getAttribute().getPrimaryKey().equals(
                         ((Attribute)attributes.get(j)).getPrimaryKey())) 
                    {
                        result = false;
                        break;
                    }                    
                }
                if (!result) 
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
        if (users != null && assigneeAVs != null) 
        {        
            for (int i=users.length-1; i>=0; i--) 
            {
                for (int j=assigneeAVs.size()-1; j>=0; j--) 
                {
                    AttributeValue av = (AttributeValue)assigneeAVs.get(j);
                    Integer avUserId = av.getUserId();
                    Integer userUserId = users[i].getUserId();
                    if ( av != null && avUserId != null && 
                         userUserId != null && 
                         avUserId.equals(userUserId))
                    {
                        users[i] = null;
                        break;
                    }
                }
            }
        }

        List eligibleUsers = new ArrayList(users.length);
        for (int i=0; i<users.length; i++) 
        {
            if (users[i] != null)
            {
                eligibleUsers.add(users[i]);
            }
        }

        return eligibleUsers;
    }

    /**
     * Returns the users which should be notified when this issue is
     * modified.  The set contains those users associated with user
     * attributes for this issue, plus the creator of the issue.
     *
     * @param action
     * @param issue Usually a reference to this or a dependent issue.
     * @param users The list of users to append to, or
     * <code>null</code> to create a new list.
     */
    protected Set getUsersToEmail(String action, Issue issue, Set users)
        throws Exception
    {
        if (users == null)
        {
            users = new HashSet(1);
        }

        Module module = getModule();

        ScarabUser createdBy = issue.getCreatedBy();
        if (createdBy != null && !users.contains(createdBy) &&
            AttributePeer.EMAIL_TO.equals(action) &&
               createdBy.hasPermission(ScarabSecurity.ISSUE__ENTER, module))
        {
            users.add(createdBy);
        }

        Criteria crit = new Criteria()
            .add(AttributeValuePeer.ISSUE_ID, issue.getIssueId())
            .addJoin(AttributeValuePeer.ATTRIBUTE_ID,
                     AttributePeer.ATTRIBUTE_ID)
            .add(AttributePeer.ACTION, action)
            .add(RModuleAttributePeer.MODULE_ID, getModuleId())
            .add(RModuleAttributePeer.ISSUE_TYPE_ID, getTypeId())
            .add(AttributeValuePeer.DELETED, 0)
            .add(RModuleAttributePeer.ACTIVE, true)
            .addJoin(RModuleAttributePeer.ATTRIBUTE_ID,
                     AttributeValuePeer.ATTRIBUTE_ID);
        List userAttVals = AttributeValuePeer.doSelect(crit);
        for (Iterator i = userAttVals.iterator(); i.hasNext(); )
        {
            AttributeValue attVal = (AttributeValue) i.next();
            try
            {
                ScarabUser su = ScarabUserManager
                    .getInstance(attVal.getUserId());
                if (!users.contains(su)
                    && su.hasPermission(attVal.getAttribute().getPermission(),
                                        module))
                {
                    users.add(su);
                }
            }
            catch (Exception e)
            {
                throw new Exception("Error retrieving users to email"); //EXCEPTION
            }
        }
        return users;
    }

    /**
     * Returns users assigned to user attributes that get emailed 
     * When issue is modified. Plus creating user.
     * Adds users to email for dependant issues as well.
     *
     * @see #getUsersToEmail
     */
    public Set getAllUsersToEmail(String action) throws Exception
    {
        Set result = null;
        Object obj = ScarabCache.get(this, GET_ALL_USERS_TO_EMAIL, action); 
        if (obj == null) 
        {        
            Set users = new HashSet();
            try
            {
                users = getUsersToEmail(action, this, users);
                List children = getChildren();
                for (int i=0;i<children.size();i++)
                {
                    Issue depIssue = IssueManager.getInstance
                        (((Depend) children.get(i)).getObserverId());
                    users = getUsersToEmail(action, depIssue, users);
                }
                result = users;
            }
            catch (Exception e)
            {
                getLog().error("Issue.getUsersToEmail(): ", e);
                throw new Exception("Error in retrieving users."); //EXCEPTION
            }
            ScarabCache.put(result, this, GET_ALL_USERS_TO_EMAIL, action);
        }
        else 
        {
            result = (Set)obj;
        }
        return result;
    }

    /**
     * Returns the specific user's attribute value.
     */
    public AttributeValue getUserAttributeValue(ScarabUser user, Attribute attribute)
        throws Exception
    {
        AttributeValue result = null;
        Object obj = getCachedObject(GET_USER_ATTRIBUTEVALUE,
            attribute.getAttributeId(), user.getUserId()); 
        if (obj == null) 
        {
            Criteria crit = new Criteria()
                .add(AttributeValuePeer.ATTRIBUTE_ID, attribute.getAttributeId())
                .add(AttributeValuePeer.ISSUE_ID, getIssueId())
                .add(AttributeValuePeer.USER_ID, user.getUserId())
                .add(AttributeValuePeer.DELETED, 0);
            List resultList = AttributeValuePeer.doSelect(crit);
            if (resultList != null && resultList.size() == 1)
            {
                result = (AttributeValue)resultList.get(0);
            }
            putCachedObject(result, GET_USER_ATTRIBUTEVALUE, 
                attribute.getAttributeId(), user.getUserId());
        }
        else 
        {
            result = (AttributeValue)obj;
        }
        return result;
    }

    /**
     * Returns attribute values for user attributes.
     */
    public List getUserAttributeValues() throws Exception
    {
        List result = null;
        Object obj = getCachedObject(GET_USER_ATTRIBUTEVALUES);
        if (obj == null)
        {
            List attributeList = getModule().getUserAttributes(getIssueType(), true);
            List attributeIdList = new ArrayList();
            
            for (int i=0; i<attributeList.size(); i++) 
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
            putCachedObject(result, GET_USER_ATTRIBUTEVALUES);
        }
        else
        {
            result = (List)obj;
        }
        return result;
    }

     
    /**
     * The initial activity set from issue creation.
     *
     * @return a <code>ActivitySet</code> value
     * @exception Exception if an error occurs
     */
    public ActivitySet getInitialActivitySet()
        throws Exception
    {
        ActivitySet activitySet = getActivitySet();
        if (activitySet == null) 
        {
            Log.get().warn("Creation ActivitySet is null for " + this);
        }
        
        return activitySet;
    }

    /**
     * The date the issue was created.
     *
     * @return a <code>Date</code> value
     * @exception TorqueException if an error occurs
     */
    public Date getCreatedDate()
        throws TorqueException
    {
        ActivitySet creationSet = getActivitySet();
        Date result = null;
        if (creationSet == null) 
        {
            getLog().warn("Issue " + getUniqueId() + " (pk=" + getIssueId() +
                           ") does not have a creation ActivitySet");
        }
        else 
        {
            result = creationSet.getCreatedDate();
        }
        return result;
    }

    /**
     * The user that created the issue.
     * @return a <code>ScarabUser</code> value
     */
    public ScarabUser getCreatedBy()
        throws TorqueException
    {
        ActivitySet creationSet = getActivitySet();
        ScarabUser result = null;
        if (creationSet == null) 
        {
            getLog().warn("Issue " + getUniqueId() + " (pk=" + getIssueId() +
                           ") does not have a creation ActivitySet");
        }
        else 
        {
            result = creationSet.getScarabUser();
        }
        return result;
    }

    public boolean isCreatingUser(ScarabUser user)
         throws Exception
    {
        ActivitySet creationSet = getActivitySet();
        boolean result = false;
        if (creationSet == null) 
        {
            getLog().warn("Issue " + getUniqueId() + " (pk=" + getIssueId() +
                           ") does not have a creation ActivitySet");
        }
        else 
        {
            result = creationSet.getCreatedBy().equals(user.getUserId());
        }
        return result;
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
        if (!isNew()) 
        {
            Object obj = ScarabCache.get(this, GET_LAST_TRANSACTION); 
            if (obj == null) 
            {        
                Criteria crit = new Criteria();
                crit.addJoin(ActivitySetPeer.TRANSACTION_ID, 
                         ActivityPeer.TRANSACTION_ID);
                crit.add(ActivityPeer.ISSUE_ID, getIssueId());
                Integer[] typeIds = {ActivitySetTypePeer.EDIT_ISSUE__PK, 
                                       ActivitySetTypePeer.MOVE_ISSUE__PK};
                crit.addIn(ActivitySetPeer.TYPE_ID, typeIds);
                // there could be multiple attributes modified during the 
                // creation which will lead to duplicates
                crit.setDistinct();
                crit.addDescendingOrderByColumn(ActivitySetPeer.CREATED_DATE);
                List activitySets = ActivitySetPeer.doSelect(crit);
                if (activitySets.size() > 0) 
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
        if (!isNew()) 
        {
            ActivitySet t = getLastActivitySet();
            if (t == null)
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
        if (!isNew()) 
        {
            ActivitySet t = getLastActivitySet();
            if (t == null)
            {
                result = getCreatedBy();
            }
            else 
            {
                result = ScarabUserManager
                    .getInstance(t.getCreatedBy());
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
        {
            // ignored (return 0 by default)
        }
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
        Object obj = getCachedObject(GET_COMMENTS, fullBool);
        if (obj == null) 
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
            putCachedObject(result, GET_COMMENTS, fullBool);
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
        Object obj = getCachedObject(GET_URLS);
        if (obj == null) 
        {        
            Criteria crit = new Criteria()
                .add(AttachmentPeer.ISSUE_ID, getIssueId())
                .addJoin(AttachmentTypePeer.ATTACHMENT_TYPE_ID,
                         AttachmentPeer.ATTACHMENT_TYPE_ID)
                .add(AttachmentTypePeer.ATTACHMENT_TYPE_ID, 
                     Attachment.URL__PK)
                .add(AttachmentPeer.DELETED, 0);
            result = AttachmentPeer.doSelect(crit);
            putCachedObject(result, GET_URLS);
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
        Object obj = getCachedObject(GET_EXISTING_ATTACHMENTS); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria()
                .add(AttachmentPeer.ISSUE_ID, getIssueId())
                .addJoin(AttachmentTypePeer.ATTACHMENT_TYPE_ID,
                         AttachmentPeer.ATTACHMENT_TYPE_ID)
                .add(AttachmentTypePeer.ATTACHMENT_TYPE_ID, 
                     Attachment.FILE__PK)
                .add(AttachmentPeer.DELETED, 0);
            result = AttachmentPeer.doSelect(crit);
            putCachedObject(result, GET_EXISTING_ATTACHMENTS);
        }
        else 
        {
            result = (List)obj;
        }
        return result;        
    }

    public List getActivitiesWithNullEndDate(Attribute attribute)
        throws TorqueException
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_NULL_END_DATE, attribute);
        if (obj == null)
        {
            Criteria crit = new Criteria();
            crit.add(ActivityPeer.ISSUE_ID, this.getIssueId());
            crit.add(ActivityPeer.ATTRIBUTE_ID, attribute.getAttributeId());
            crit.add(ActivityPeer.END_DATE, null);
            result = ActivityPeer.doSelect(crit);
            ScarabCache.put(result, this, GET_NULL_END_DATE, attribute);
        }
        else
        {
            result = (List)obj;
        }
        return result;
    }

    /**
     * Gets default history limit for this module-issue type.
     * The default is 5.
     */
    public int getHistoryLimit() throws Exception
    {
        RModuleIssueType rmit = getModule().getRModuleIssueType(getIssueType());
        if (rmit != null)
        {
            return rmit.getHistory();
        }
        else
        {
            return 5;
        }
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
        Object obj = getCachedObject(GET_ACTIVITY, fullHistoryObj,
                                     new Integer(limit)); 
        if (obj == null)
        {
            Criteria crit = new Criteria()
                .add(ActivityPeer.ISSUE_ID, getIssueId())
                .addAscendingOrderByColumn(ActivityPeer.TRANSACTION_ID);
            if (!fullHistory)
            {
                crit.setLimit(limit);
            }
            result = ActivityPeer.doSelect(crit);
            putCachedObject(result, GET_ACTIVITY, 
                            fullHistoryObj, new Integer(limit));
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
            throw new TorqueException(e); //EXCEPTION
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
                                      Integer type)
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
    public ActivitySet getActivitySet(ScarabUser user, Integer type)
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
        return getChildren(true);
    }

    /**
     * Returns list of child dependencies
     * i.e., related to this issue through the DEPEND table.
     */
    public List getChildren(boolean hideDeleted) throws Exception  
    {
        List result = null;
        Boolean hide = hideDeleted ? Boolean.TRUE : Boolean.FALSE;
        Object obj = getCachedObject(GET_CHILDREN, hide); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria()
                .add(DependPeer.OBSERVED_ID, getIssueId());
            if (hideDeleted)
            {
                crit.add(DependPeer.DELETED, false);
            }
            result = DependPeer.doSelect(crit);
            putCachedObject(result, GET_CHILDREN, hide);
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
        return getParents(true);
    }

    /**
     * Returns list of parent dependencies
     * i.e., related to this issue through the DEPEND table.
     */
    public List getParents(boolean hideDeleted) throws Exception  
    {
        List result = null;
        Boolean hide = hideDeleted ? Boolean.TRUE : Boolean.FALSE;
        Object obj = getCachedObject(GET_PARENTS, hide); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria()
                .add(DependPeer.OBSERVER_ID, getIssueId());
            if (hideDeleted)
            {
                crit.add(DependPeer.DELETED, false);
            }
            result = DependPeer.doSelect(crit);
            putCachedObject(result, GET_PARENTS, hide);
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
        // Check whether the entered issue is already dependent on this
        // Issue. If so, then throw an exception because we don't want
        // to add it again.
        Depend prevDepend = this.getDependency(childIssue, true);
        if (prevDepend != null)
        {
            throw new ScarabException(L10NKeySet.DependencyExists);
        }

        // we definitely want to do an insert here so force it.
        depend.setNew(true);
        depend.setDeleted(false);
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

        Object[] args = {
            this.getUniqueId(),
            depend.getAction(),
            childIssue.getUniqueId()
        };

        String desc = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            getLocale(),
            "AddDependency", args);

        // Save activity record for the parent issue
        ActivityManager
            .createAddDependencyActivity(this, activitySet, depend, desc);

        // Save activity record for the child issue
        ActivityManager
            .createAddDependencyActivity(childIssue, activitySet, depend, desc);

        return activitySet;
    }
    /**
     * Checks to see if this issue has a dependency on the passed in issue.
     * or if the passed in issue has a dependency on this issue.
     */
    public Depend getDependency(Issue potentialDependency) throws Exception
    {
        return getDependency(potentialDependency, true);
    }

    /**
     * Checks to see if this issue has a dependency on the passed in issue.
     * or if the passed in issue has a dependency on this issue.
     *
     * @param potentialDependency the issue for which we are determining if there is a
     * parent or child dependency to this issue
     * @param hideDeleted true if deleted issues are omitted from the search
     * @returns the dependency object or null
     */
    public Depend getDependency(Issue potentialDependency, boolean hideDeleted) throws Exception
    {
        Depend result = null;
        Object obj = ScarabCache.get(this, GET_DEPENDENCY, potentialDependency);
        if (obj == null)
        {

            // Determine if this issue is a parent to the potentialDependency
            Criteria crit = new Criteria(2)
                .add(DependPeer.OBSERVED_ID, getIssueId())
                .add(DependPeer.OBSERVER_ID, potentialDependency.getIssueId());
            if (hideDeleted)
            {
                crit.add(DependPeer.DELETED, false);
            }

            List childIssues = DependPeer.doSelect(crit);
            // A system invariant is that we will get one and only one
            // record back.
            if (!childIssues.isEmpty())
            {
                result = (Depend)childIssues.get(0);
            }
            else
            {
                // Determine if this issue is a child to the potentialDependency
                Criteria crit2 = new Criteria(2)
                    .add(DependPeer.OBSERVER_ID, getIssueId())
                    .add(DependPeer.OBSERVED_ID, potentialDependency.getIssueId());
                if (hideDeleted)
                {
                    crit2.add(DependPeer.DELETED, false);
                }
                List parentIssues = DependPeer.doSelect(crit2);
                if (!parentIssues.isEmpty())
                {
                    result = (Depend)parentIssues.get(0);
                }
            }

            if (result != null)
            {
                ScarabCache.put(result, this, GET_DEPENDENCY, potentialDependency);
            }
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
                " does not allow issues."); //EXCEPTION
        }
        
        // remove unset AttributeValues before saving
        List attValues = getAttributeValues();
        // reverse order since removing from list
        for (int i=attValues.size()-1; i>=0; i--) 
        {
            AttributeValue attVal = (AttributeValue) attValues.get(i);
            if (!attVal.isSet()) 
            {
                attValues.remove(i);
            }
        }

        if (isNew())
        {
            // set the issue id
            setIdDomain(module.getDomain());
            setIdPrefix(module.getCode());

            // for an enter issue template, do not give issue id
            // set id count to -1 so does not show up as an issue
            if (isTemplate())
            { 
                setIdCount(-1);
            }
            else
            {
                try
                {
                    setIdCount(getNextIssueId(dbCon));
                }
                catch (Exception e)
                {
                    throw new TorqueException(e); //EXCEPTION
                }
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
                        getLog().error("Could not get an id, even after "
                            +"trying to add a module entry into the ID_TABLE", 
                            e);
                        getLog()
                            .error("Error trying to create ID_TABLE entry for "
                                   + getIdTableKey(), badException);
                        // throw the original
                        throw new ScarabException(
                            L10NKeySet.ExceptionRetrievingIssueId,
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
        if (domain != null && domain.length() > 0) 
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
    public List getTemplateTypes() throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_TEMPLATE_TYPES); 
        if (obj == null) 
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
     */


    /**
     * Get IssueTemplateInfo by Issue Id.
     */
    public IssueTemplateInfo getTemplateInfo() 
          throws Exception
    {
        IssueTemplateInfo result = null;
        Object obj = ScarabCache.get(this, GET_TEMPLATEINFO); 
        if (obj == null) 
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
     *  Get Unset required attributes in destination module / issue type.
     */
    public List getUnsetRequiredAttrs(Module newModule, IssueType newIssueType)
        throws Exception
    {
        List attrs = new ArrayList();
        if (!getIssueType().getIssueTypeId()
            .equals(newIssueType.getIssueTypeId())
            || !getModule().getModuleId().equals(newModule.getModuleId()))
        {
            List requiredAttributes = 
                newIssueType.getRequiredAttributes(newModule);
            Map attrValues = getAttributeValuesMap();

            for (Iterator i = requiredAttributes.iterator(); i.hasNext(); )
            {
                Attribute attr = (Attribute)i.next();
                if (!attrValues.containsKey(attr.getName().toUpperCase()))
                {
                    attrs.add(attr);
                }
            }
        }
        return attrs;
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

        Attachment attachment = new Attachment();

        Module oldModule = getModule();

        // If moving to a new issue type, just change the issue type id
        // otherwise, create fresh issue
        if (getModule().getModuleId().equals(newModule.getModuleId())
            && !getIssueType().getIssueTypeId().equals(newIssueType.getIssueTypeId())
            && action.equals("move"))
        {
            newIssue = this;
            newIssue.setIssueType(newIssueType);
        }
        else
        {
            newIssue = newModule.getNewIssue(newIssueType);
        }
        newIssue.save();

        if (newIssue != this)
        {
            // If moving issue to new module, delete original
            if (action.equals("move"))
            {
                setDeleted(true);
                save();
            }

            ActivitySet createActivitySet = ActivitySetManager.getInstance(
                    ActivitySetTypePeer.CREATE_ISSUE__PK, getCreatedBy());
            createActivitySet.setCreatedDate(getCreatedDate());
            createActivitySet.save();
            newIssue.setCreatedTransId(createActivitySet.getActivitySetId());
            newIssue.save();


            // Adjust dependencies if its a new issue id
            // (i.e.. moved to new module)
            List children = getChildren();
            for (Iterator i = children.iterator(); i.hasNext();)
            {
                 Depend depend = (Depend)i.next();
                 if (action.equals("move"))
                 {
                     doDeleteDependency(null, depend, user);
                 }
                 Issue child = IssueManager.getInstance(depend.getObserverId());
                 Depend newDepend = new Depend();
                 newDepend.setObserverId(child.getIssueId());
                 newDepend.setObservedId(newIssue.getIssueId());
                 newDepend.setTypeId(depend.getTypeId());
                 newIssue.doAddDependency(null, newDepend, child, user);
            }
            List parents = getParents();
            for (Iterator j = parents.iterator(); j.hasNext();)
            {
                 Depend depend = (Depend)j.next();
                 if (action.equals("move"))
                 {
                     doDeleteDependency(null, depend, user);
                 }
                 Issue parent = IssueManager.getInstance(depend.getObservedId());
                 Depend newDepend = new Depend();
                 newDepend.setObserverId(newIssue.getIssueId());
                 newDepend.setObservedId(parent.getIssueId());
                 newDepend.setTypeId(depend.getTypeId());
                 parent.doAddDependency(null, newDepend, newIssue, user);
            }

            // copy attachments: comments/files etc.
            Iterator attachments = getAttachments().iterator();
            while (attachments.hasNext())
            {
                Attachment oldA = (Attachment)attachments.next();
                Attachment newA = oldA.copy();
                newA.setIssueId(newIssue.getIssueId());
                newA.save();
                Activity oldAct = oldA.getActivity();
                if (oldAct != null)
                {
                    ActivitySet activitySet = getActivitySet(
                        user, ActivitySetTypePeer.EDIT_ISSUE__PK);
                    activitySet.save();
                    ActivityManager.createTextActivity(newIssue, activitySet,
                        oldA.getActivity().getDescription(), newA);
                }
                if (Attachment.FILE__PK.equals(newA.getTypeId()))
                {
                    oldA.copyFileTo(newA.getFullPath());
                }
            }

            // Copy over activity sets for the source issue's previous
            // Transactions
            List activitySets = getActivitySets();
            List nonMatchingAttributes = getNonMatchingAttributeValuesList
                                               (newModule, newIssueType);
            for (Iterator i = activitySets.iterator(); i.hasNext();)
            {
                ActivitySet as = (ActivitySet)i.next();
                ActivitySet newAS = null;
                Attachment newAtt =  null;
                // If activity set has an attachment, make a copy for new issue
                if (as.getAttachmentId() != null)
                {
                    newAtt = as.getAttachment().copy();
                    newAtt.save();
                }
                // Copy over activities with sets
                List activities = as.getActivityListForIssue(this);
                for (Iterator j = activities.iterator(); j.hasNext();)
                {
                    Activity a = (Activity)j.next();
                    // Only copy transactions that are records of previous move/copies
                    // Or transactions relating to attributes.
                    // Other transactions (attachments, dependencies)
                    // Will be saved when attachments and dependencies are copied
                    if (as.getTypeId().equals((ActivitySetTypePeer.MOVE_ISSUE__PK))
                        || !a.getAttributeId().equals(new Integer("0")))
                    {
                        newAS = new ActivitySet();
                        newAS.setTypeId(as.getTypeId());
                        if (newAtt != null)
                        {
                            newAS.setAttachmentId(newAtt.getAttachmentId());
                        }
                        newAS.setCreatedBy(as.getCreatedBy());
                        newAS.setCreatedDate(as.getCreatedDate());
                        newAS.save();

                        // iterate over and copy transaction's activities
                        Activity newA = a.copy(newIssue, newAS);
                        newIssue.getActivity(true).add(newA);
                      
                        // If this an activity relating to setting an attribute value
                        // And is the final edit of the attribute,
                        // Copy over the attribute value
                        if (a.getEndDate() == null && !a.getAttributeId().equals(new Integer("0")))
                        {
                            AttributeValue attVal = getAttributeValue(a.getAttribute());
                            // Only copy if the target artifact type contains this
                            // Attribute
                            if (attVal != null && !nonMatchingAttributes.contains(attVal))
                            {
                                AttributeValue newAttVal = attVal.copy();
                                newAttVal.setIssueId(newIssue.getIssueId());
                                newAttVal.setActivity(newA);
                                newAttVal.startActivitySet(newAS);
                                newAttVal.save();
                            }
                        }
                    }
                }
            }
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
            attachmentBuf.append(Localization.format(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               getLocale(), "DidNotCopyAttributes", newIssueType.getName()));
            attachmentBuf.append("\n");
            for (int i=0;i<commentAttrs.size();i++)
            {
                List attVals = getAttributeValues((Attribute)commentAttrs.get(i));
                for (int j=0; j<attVals.size(); j++)
                {
                    AttributeValue attVal = (AttributeValue)attVals.get(j);
                    String field = null;
                    delAttrsBuf.append(attVal.getAttribute().getName());
                    field = attVal.getValue();
                    delAttrsBuf.append("=").append(field).append(". ").append("\n");
               }
           }
           String delAttrs = delAttrsBuf.toString();
           attachmentBuf.append(delAttrs);

           // Also create a regular comment with non-matching attribute info
           Attachment comment = new Attachment();
           comment.setTextFields(user, newIssue, Attachment.COMMENT__PK);

           Object[] args = {this.getUniqueId(), newIssueType.getName()};
           StringBuffer commentBuf = new StringBuffer(Localization.format(
              ScarabConstants.DEFAULT_BUNDLE_NAME,
              getLocale(),
              "DidNotCopyAttributesFromArtifact", args));
           commentBuf.append("\n").append(delAttrs);
           comment.setData(commentBuf.toString());
           comment.setName(Localization.getString(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               getLocale(),
               "Comment"));
           comment.save();
        }
        else
        {
            attachmentBuf.append(Localization.getString(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               getLocale(),
               "AllCopied"));
        }
        attachment.setData(attachmentBuf.toString());

        if (action.equals("move"))
        {
            attachment.setName(Localization.getString(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               getLocale(),
               "MovedIssueNote"));
        }
        else
        {
            attachment.setName(Localization.getString(
               ScarabConstants.DEFAULT_BUNDLE_NAME,
               getLocale(),
               "CopiedIssueNote"));
        }
        attachment.setTextFields(user, newIssue, Attachment.MODIFICATION__PK);
        attachment.save();


        // Create activitySet for the MoveIssue activity
        ActivitySet activitySet2 = ActivitySetManager
            .getInstance(ActivitySetTypePeer.MOVE_ISSUE__PK, user, attachment);
        activitySet2.save();

        // Generate comment
        Integer actionChoice = (action.equals("copy")) ? COPIED : MOVED;
        Object[] args = {
            actionChoice,
            getUniqueId(),
            oldModule.getName(),
            getIssueType().getName()
        };
        String desc = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            getLocale(),
            "MovedFromIssueDescription", args);

        // Save activity record
        Attribute zeroAttribute = AttributeManager
            .getInstance(NUMBERKEY_0);
        ActivityManager
            .createTextActivity(newIssue, zeroAttribute, activitySet2,
                                desc, null,
                                getUniqueId(), newIssue.getUniqueId());

        // Save activity record for old issue
        if (newIssue != this)
        {
            args[1] = newIssue.getUniqueId();
            args[2] = newModule.getName();
            args[3] = newIssueType.getName();
            desc = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME,
                getLocale(),
                "MovedToIssueDescription", args);

            ActivityManager
                .createTextActivity(this, zeroAttribute, activitySet2,
                                    desc, null,
                                    getUniqueId(), newIssue.getUniqueId());
        }


        return newIssue;
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
        if (votes != null && votes.size() != 0) 
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
        if (!getModule().allowsMultipleVoting() && previousVotes > 0)
        {
            throw new ScarabException(L10NKeySet.ExceptionMultipleVoteForUnallowed,
                                      user.getUserName(), 
                                      getUniqueId());
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
        if (voteValues.size() == 0) 
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
        List matchingAttributes = new ArrayList();
        Map setMap = this.getAttributeValuesMap();
        for (Iterator iter = setMap.keySet().iterator(); iter.hasNext();)
        {
            AttributeValue aval = (AttributeValue)setMap.get(iter.next());
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
                        // FIXME: Use select count
                        Criteria crit2 = new Criteria(4)
                            .add(RModuleOptionPeer.ACTIVE, true)
                            .add(RModuleOptionPeer.OPTION_ID, attVal.getOptionId())
                            .add(RModuleOptionPeer.MODULE_ID, newModule.getModuleId())
                            .add(RModuleOptionPeer.ISSUE_TYPE_ID, newIssueType.getIssueTypeId());
                        List modOpt = RModuleOptionPeer.doSelect(crit2);

                        if (!modOpt.isEmpty())
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
                            getLog().error(e);
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
         Module module = ModuleManager.getInstance(new Integer(moduleId));
         IssueType issueType = IssueTypeManager.getInstance(new Integer(issueTypeId));
         return getMatchingAttributeValuesList(module, issueType);
    }

    /**
     * Gets a list AttributeValues which the source module has,
     * But the destination module does not have, when doing a copy.
     * It is used in the MoveIssue2.vm template
     */
    public List getNonMatchingAttributeValuesList(Module newModule,
                                             IssueType newIssueType)
          throws Exception
    {
        List nonMatchingAttributes = new ArrayList();
        AttributeValue aval = null;

        Map setMap = this.getAttributeValuesMap();
        for (Iterator iter = setMap.values().iterator(); iter.hasNext();)
        {
            aval = (AttributeValue) iter.next();
            List values = getAttributeValues(aval.getAttribute());
            // loop thru the values for this attribute
            for (Iterator i = values.iterator(); i.hasNext(); )
            {
                AttributeValue attVal = (AttributeValue) i.next();
                RModuleAttribute modAttr = newModule.
                    getRModuleAttribute(aval.getAttribute(), newIssueType);

                // If this attribute is not active for the destination module,
                // Add to nonMatchingAttributes list
                if (modAttr == null || !modAttr.getActive())
                {
                    nonMatchingAttributes.add(attVal);
                }
                else
                {
                    // If attribute is an option attribute, Check if
                    // attribute option is active for destination module.
                    if (attVal instanceof OptionAttribute)
                    {
                        Criteria crit2 = new Criteria(1)
                            .add(RModuleOptionPeer.ACTIVE, true)
                            .add(RModuleOptionPeer.OPTION_ID, attVal.getOptionId())
                            .add(RModuleOptionPeer.MODULE_ID, newModule.getModuleId())
                            .add(RModuleOptionPeer.ISSUE_TYPE_ID, newIssueType.getIssueTypeId());
                        List modOpt = RModuleOptionPeer.doSelect(crit2);

                        if ( modOpt.isEmpty())
                        {
                                nonMatchingAttributes.add(attVal);
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
                            Log.get().error("Unable to retrieve user for "
                                            + "attribute", e);
                        }
                        Attribute attr = attVal.getAttribute();
                        ScarabUser[] userArray =
                            newModule.getUsers(attr.getPermission());
                        // If user exists in destination module with
                        // this permission, add as matching value.
                        if (!Arrays.asList(userArray).contains(user))
                        {
                            nonMatchingAttributes.add(attVal);
                        }
                    }
                }
            }
        }
        return nonMatchingAttributes;
    }


    public List getNonMatchingAttributeValuesList(String moduleId, String issueTypeId)
          throws Exception
    {
         Module module = ModuleManager.getInstance(new Integer(moduleId));
         IssueType issueType = IssueTypeManager.getInstance(new Integer(issueTypeId));
         return getNonMatchingAttributeValuesList(module, issueType);
    }

    /**
     * Checks if user has permission to delete issue template.
     * Only the creating user can delete a personal template.
     * Only project owner or admin can delete a project-wide template.
     */
    public void delete(ScarabUser user)
         throws Exception, ScarabException
    {                
        Module module = getModule();
        if (user.hasPermission(ScarabSecurity.ITEM__DELETE, module)
            || (user.getUserId().equals(getCreatedBy().getUserId()) && isTemplate()))
        {
            setDeleted(true);
            save();
        } 
        else
        {
            throw new ScarabException(L10NKeySet.YouDoNotHavePermissionToAction);
        }            
    }


    /**
     * This method will return the AttributeValue which represents
     * the default text attribute.
     *
     * @return the AttributeValue to use as the email subject, or null
     * or null if no suitable AttributeValue could be found. 
     */
    public AttributeValue getDefaultTextAttributeValue()
        throws Exception
    {
        AttributeValue result = null;
        Object obj = ScarabCache.get(this, GET_DEFAULT_TEXT_ATTRIBUTEVALUE); 
        if (obj == null) 
        {        
            Attribute defaultTextAttribute = 
                getIssueType().getDefaultTextAttribute(getModule());
            if (defaultTextAttribute != null) 
            {
                result = getAttributeValue(defaultTextAttribute);
            }
            ScarabCache.put(result, this, GET_DEFAULT_TEXT_ATTRIBUTEVALUE);
        }
        else 
        {
            result = (AttributeValue)obj;
        }
        return result;
    }

    /**
     * This calls getDefaultTextAttributeValue() and then returns the
     * String value of the Attribute. This method is used to get the
     * subject of an email. if no text attribute value is found it
     * will use the first ActivitySet comment.
     */
    public String getDefaultText()
        throws Exception
    {
        String result = null;
        Object obj = ScarabCache.get(this, GET_DEFAULT_TEXT); 
        if (obj == null) 
        {        
            AttributeValue emailAV = getDefaultTextAttributeValue();
            if (emailAV != null) 
            {
                result = emailAV.getValue();
            }
            if (result == null) 
            {
                ActivitySet activitySet = getInitialActivitySet();
                if (activitySet != null)
                {
                    Attachment reason = activitySet.getAttachment();
                    if (reason != null && reason.getData() != null 
                        && reason.getData().trim().length() > 0) 
                    {
                        result = reason.getData();
                    }        
                }                
            }
            result = (result == null) ? 
                Localization.getString(ScarabConstants.DEFAULT_BUNDLE_NAME,
                                       getLocale(), "NoIssueSummaryAvailable")
                      : result;
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

    /**
     * gets an object from the appropriate cache, based on whether this is
     * a saved issue.  if you know the object should only be in ScarabCache
     * do not use this method.
     */
    private Object getCachedObject(String methodName)
    {
        Object obj = null;
        // Cache Note:
        // we check for issue id, so that we only (JCS) cache for saved issues
        // if we decide to cache results for new issues we should replace
        // this conditional with (this instanceof IssueSearch) because
        // we definitely do not want to cache those.
        if (getIssueId() == null)
        {
            obj = ScarabCache.get(this, methodName);
        }
        else
        {
            obj = getMethodResult().get(this, methodName);
        }        
        return obj;
    }

    /**
     * puts an object into the appropriate cache, based on whether this is
     * a saved issue.  if you know the object should only be in ScarabCache
     * do not use this method.
     */
    private void putCachedObject(Object obj, String methodName)
    {
        // see Cache Note above
        if (getIssueId() == null) 
        {
            ScarabCache.put(obj, this, methodName);
        }
        else
        {
            getMethodResult().put(obj, this, methodName);
        }
    }

    /**
     * gets an object from the appropriate cache, based on whether this is
     * a saved issue.  if you know the object should only be in ScarabCache
     * do not use this method.
     */
    private Object getCachedObject(String methodName, Serializable arg1)
    {
        Object obj = null;
        // Cache Note:
        // we check for issue id, so that we only (JCS) cache for saved issues
        // if we decide to cache results for new issues we should replace
        // this conditional with (this instanceof IssueSearch) because
        // we definitely do not want to cache those.
        if (getIssueId() == null)
        {
            obj = ScarabCache.get(this, methodName, arg1);
        }
        else
        {
            obj = getMethodResult().get(this, methodName, arg1);
        }        
        return obj;
    }

    /**
     * puts an object into the appropriate cache, based on whether this is
     * a saved issue.  if you know the object should only be in ScarabCache
     * do not use this method.
     */
    private void putCachedObject(Object obj, String methodName, 
                                 Serializable arg1)
    {
        // see Cache Note above
        if (getIssueId() == null) 
        {
            ScarabCache.put(obj, this, methodName, arg1);
        }
        else
        {
            getMethodResult().put(obj, this, methodName, arg1);
        }
    }

    /**
     * gets an object from the appropriate cache, based on whether this is
     * a saved issue.  if you know the object should only be in ScarabCache
     * do not use this method.
     */
    private Object getCachedObject(String methodName, 
                                   Serializable arg1, Serializable arg2)
    {
        Object obj = null;
        // Cache Note:
        // we check for issue id, so that we only (JCS) cache for saved issues
        // if we decide to cache results for new issues we should replace
        // this conditional with (this instanceof IssueSearch) because
        // we definitely do not want to cache those.
        if (getIssueId() == null)
        {
            obj = ScarabCache.get(this, methodName, arg1, arg2);
        }
        else
        {
            obj = getMethodResult().get(this, methodName, arg1, arg2);
        }        
        return obj;
    }

    /**
     * puts an object into the appropriate cache, based on whether this is
     * a saved issue.  if you know the object should only be in ScarabCache
     * do not use this method.
     */
    private void putCachedObject(Object obj, String methodName, 
                                 Serializable arg1, Serializable arg2)
    {
        // see Cache Note above
        if (getIssueId() == null) 
        {
            ScarabCache.put(obj, this, methodName, arg1, arg2);
        }
        else
        {
            getMethodResult().put(obj, this, methodName, arg1, arg2);
        }
    }


    // *******************************************************************
    // Permissions methods - these are deprecated
    // *******************************************************************

    /**
     * Checks if user has permission to enter issue.
     * @deprecated user.hasPermission(ScarabSecurity.ISSUE__ENTER, module)
     */
    public boolean hasEnterPermission(ScarabUser user, Module module)
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
    public boolean hasEditPermission(ScarabUser user, Module module)
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
    public boolean hasMovePermission(ScarabUser user, Module module)
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
     * Assigns user to issue. Give description.
     */
    public ActivitySet assignUser(ActivitySet activitySet, String description,
                                  ScarabUser assignee, ScarabUser assigner,
                                  Attribute attribute, Attachment attachment)
        throws Exception
    {                
        UserAttribute attVal = new UserAttribute();

        // Save activitySet if it has not been already
        if (activitySet == null)
        { 
            activitySet = ActivitySetManager
                .getInstance(ActivitySetTypePeer.EDIT_ISSUE__PK, assigner, 
                             attachment);
            activitySet.save();
            attVal.startActivitySet(activitySet);
        }

        if (description == null)
        {
            // Save activity record
            description = getAssignUserChangeString(assigner, assignee, 
                                                            attribute);
        }
        ActivityManager
            .createUserActivity(this, attribute, activitySet,
                                description, null,
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
     * Assigns user to issue.
     */
    public ActivitySet assignUser(ActivitySet activitySet, 
                                  ScarabUser assignee, ScarabUser assigner,
                                  Attribute attribute, Attachment attachment)
        throws Exception
    {                
        return assignUser(activitySet, null, 
                          assignee, assigner,
                          attribute, attachment);
    }

    /**
     * Get the message that is emailed to associated users,
     * And that is saved in the activity description,
     * When a user is assigned.
     */
    private String getAssignUserChangeString(ScarabUser assigner,
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
            getLocale(),
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
        
        String actionString = null;
        // Save activitySet if it has not been already
        if (activitySet == null)
        { 
            activitySet = ActivitySetManager
                .getInstance(ActivitySetTypePeer.EDIT_ISSUE__PK, assigner, attachment);
            activitySet.save();
            oldAttVal.startActivitySet(activitySet);
        }

        // Save activity record for deletion of old assignment
        actionString = getUserDeleteString(assigner, assignee,
                                           oldAttVal.getAttribute());
        if (actionString == null)
        {
            Log.get().debug("User attribute '"+oldAttVal.getAttribute()
                    .getName()+"' removed from the artifact type");
        }
        ActivityManager
            .createUserActivity(this, oldAttVal.getAttribute(), 
                                activitySet,
                                actionString, null,
                                assignee.getUserId(), null);


        // Save activity record for new assignment
        actionString = getAssignUserChangeString(assigner, assignee, 
                                                 newAttr);
        ActivityManager
            .createUserActivity(this, newAttr, activitySet,
                                actionString, null,
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
    private String getUserAttributeChangeString(ScarabUser assigner,
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
            getLocale(),
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
        String actionString = null;
        // Save activitySet record if it has not been already
        if (activitySet == null)
        { 
            activitySet = ActivitySetManager
                .getInstance(ActivitySetTypePeer.EDIT_ISSUE__PK, assigner, attachment);
            activitySet.save();
            attVal.startActivitySet(activitySet);
        }

        // Save activity record
        actionString = getUserDeleteString(assigner, assignee, attr);
        if (actionString == null)
        {
            Log.get().debug("User attribute '"+attr.getName()+
                            "' removed from the artifact type." );
        }

        ActivityManager
            .createUserActivity(this, attVal.getAttribute(), 
                                activitySet,
                                actionString, null,
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
    private String getUserDeleteString(ScarabUser assigner,
                                      ScarabUser assignee, 
                                      Attribute attr)
        throws Exception
    {
        String actionString = null;
        RModuleAttribute rma = getModule()
            .getRModuleAttribute(attr, getIssueType());
        if (rma != null) 
        {
            String attrDisplayName = rma.getDisplayValue();
            Object[] args = {
                assigner.getUserName(), assignee.getUserName(),
                attrDisplayName
            };
            actionString = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                "AssignIssueEmailRemovedUserAction", args);            
        }
        return actionString;
    }

    /**
     * Deletes a specific dependency on this issue.
     */
    public ActivitySet doDeleteDependency(ActivitySet activitySet, 
                                          Depend oldDepend, ScarabUser user)
        throws Exception
    {
        Issue otherIssue = IssueManager
                        .getInstance(oldDepend.getObserverId(), false);
/* Why can a child not delete a dependency??
        if (otherIssue.equals(this))
        {
            throw new ScarabException("CannotDeleteDependency");
        }
*/
        Issue thisIssue = IssueManager
                        .getInstance(oldDepend.getObservedId(), false);

        Object[] args = {
            oldDepend.getDependType().getName(),
            thisIssue.getUniqueId(),
            otherIssue.getUniqueId() 
        };
        String desc = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            getLocale(),
            "DependencyDeletedDesc", args);

        // get the original object so that we do an update
        oldDepend = thisIssue.getDependency(otherIssue);
        oldDepend.setNew(false);
        oldDepend.setDeleted(true);
        oldDepend.save();

        // need to null out the cache entry so that Issue.getDependency()
        // does not try to return the item from the cache
        ScarabCache.put(null, thisIssue, GET_DEPENDENCY, otherIssue);

        if (activitySet == null)
        {
            // deal with user comments
            Attachment comment = oldDepend.getDescriptionAsAttachment(user, thisIssue);

            activitySet = getActivitySet(user, comment,
                              ActivitySetTypePeer.EDIT_ISSUE__PK);
            // Save activitySet record
            activitySet.save();
        }

        ActivityManager
            .createDeleteDependencyActivity(thisIssue, activitySet, oldDepend,
                                desc);
        ActivityManager
            .createDeleteDependencyActivity(otherIssue, activitySet, oldDepend,
                                desc);


        return activitySet;
    }

    /**
     * Given a specific attachment object allow us to update
     * the information in it. If the old matches the new, then
     * nothing is modified.
     */
    public ActivitySet doChangeUrlDescription(ActivitySet activitySet, 
                                              ScarabUser user,
                                              Attachment attachment, 
                                              String oldDescription)
        throws Exception
    {
        String newDescription = attachment.getName();
        if (!oldDescription.equals(newDescription))
        {
            Object[] args = {
                oldDescription,
                newDescription,
            };
            String desc = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME,
                getLocale(),
                "UrlDescChangedDesc", args);

            if (desc.length() > 248)
            { 
                desc = desc.substring(0,248) + "...";
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
                                    desc, attachment,
                                    oldDescription, newDescription);
            if (!activitySet.sendEmail(this))
            {
                Localizable urlDescSaved = new L10NMessage(L10NKeySet.UrlDescChangedDesc);
                Localizable emailError   = new L10NMessage(L10NKeySet.CouldNotSendEmail);
                throw new ScarabException(L10NKeySet.ExceptionSavedButErrors,
                                       urlDescSaved,
                                       emailError);
            }
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
            Object[] args = {
                oldUrl, newUrl
            };
            String desc = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME,
                getLocale(),
                "UrlChangedDesc", args);

            if (desc.length() > 248)
            { 
                desc = desc.substring(0,248) + "...";
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
                                    desc, attachment,
                                    oldUrl, newUrl);
            if (!activitySet.sendEmail(this))
            {
                Localizable urlChanged = new L10NMessage(L10NKeySet.UrlChangedDesc, oldUrl, newUrl);
                Localizable emailError = new L10NMessage(L10NKeySet.CouldNotSendEmail);
                throw new ScarabException(L10NKeySet.ExceptionSavedButErrors,
                                          urlChanged,
                                          emailError);
            }
        }
        return activitySet;
    }

    /**
     * changes the dependency type as well as. will not change deptype
     * for deleted deps
     */
    public ActivitySet doChangeDependencyType(ActivitySet activitySet,
                                              Depend oldDepend,
                                              Depend newDepend, 
                                              ScarabUser user)
        throws Exception
    {
        String oldName = oldDepend.getDependType().getName();
        String newName = newDepend.getDependType().getName();
        // check to see if something changed
        // only change dependency type for non-deleted deps
        if (!newName.equals(oldName) && !newDepend.getDeleted())
        {
            Issue otherIssue = IssueManager
                            .getInstance(newDepend.getObserverId(), false);

            // always delete an old dependency
            oldDepend.setDeleted(true);
            oldDepend.save();
            // always create a new dependency
            newDepend.setNew(true);
            newDepend.save();

            Object[] args = {
                this.getUniqueId(),
                otherIssue.getUniqueId(),
                oldName,
                newName
            };
            String desc = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME,
                getLocale(),
                "DependencyTypeChangedDesc", args);

            // need to null out the cache entry so that Issue.getDependency()
            // does not try to return the item from the cache
            ScarabCache.put(null, this, GET_DEPENDENCY, otherIssue);

            if (activitySet == null)
            {
                // deal with user comments
                Attachment comment = newDepend.getDescriptionAsAttachment(user, this);
    
                activitySet = getActivitySet(user, comment,
                                  ActivitySetTypePeer.EDIT_ISSUE__PK);
                // Save activitySet record
                activitySet.save();
            }
            
            ActivityManager
                .createChangeDependencyActivity(this, activitySet, newDepend,
                                    desc, oldName, newName);
            ActivityManager
                .createChangeDependencyActivity(otherIssue, activitySet, newDepend,
                                    desc, oldName, newName);
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
            throw new Exception(msg); //EXCEPTION
        }
        
        if (activitySet == null)
        {
            // Save activitySet record
            activitySet = ActivitySetManager
                .getInstance(ActivitySetTypePeer.CREATE_ISSUE__PK, user);
            activitySet.save();
        }
        setActivitySet(activitySet);

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
                    se.getMessage() + " Please start over.");     //EXCEPTION
            }
        }        
        this.save();

        // create initial issue creation activity
        ActivityManager.createReportIssueActivity(this, activitySet,  
                Localization.getString(
                    ScarabConstants.DEFAULT_BUNDLE_NAME,
                    getLocale(),
                    "IssueCreated"));

        // this needs to be done after the issue is created.
        // check to make sure the attachment has data before submitting it.
        String attachmentData = attachment.getData();
        if (attachmentData != null &&
            attachmentData.length() > 0)
        {
            attachment = AttachmentManager.getReason(attachment, this, user);
            activitySet.setAttachment(attachment);
        }
        activitySet.save();
        
        // need to clear the cache since this is after the 
        // issue is saved. for some reason, things don't
        // show up properly right away.
        ScarabCache.clear();
        return activitySet;
    }

    /**
     * Sets AttributeValues for an issue based on a hashmap of attribute values
     * This is data is saved to the database and the proper ActivitySet is
     * also recorded.
     * @param activitySet ActivitySet instance
     * @param newAttVals A map of attribute Id's vs new AttributeValues
     * @param attachment Attachment to the issue
     * @param user User responsible for this activity
     * @return ActivitySet object containing the changes made to the issue
     * @throws Exception when the workflow has an error to report
     */
    public ActivitySet setAttributeValues(ActivitySet activitySet,
                                          HashMap newAttVals,
                                          Attachment attachment,
                                          ScarabUser user)
        throws Exception
    {
        if (!isTemplate())
        {
            String msg = doCheckAttributeValueWorkflow(newAttVals, user);
            if (msg != null)
            {
                throw new Exception(msg); //EXCEPTION
            }
        }
        // save the attachment if it exists.
        if (attachment != null)
        {
            attachment.setTextFields(user, this, 
                                     Attachment.MODIFICATION__PK);
            attachment.save();
        }

        // Create the ActivitySet
        if (activitySet == null)
        {
            activitySet = getActivitySet(user, attachment,
                                      ActivitySetTypePeer.EDIT_ISSUE__PK);
            activitySet.save();
            ScarabCache.clear();
        }

        SequencedHashMap avMap = getModuleAttributeValuesMap(); 
        AttributeValue oldAttVal = null;
        AttributeValue newAttVal = null;
        Iterator iter = newAttVals.keySet().iterator();
        boolean attValDeleted = false;
        while (iter.hasNext())
        {
            Integer attrId = (Integer)iter.next();
            Attribute attr = AttributeManager.getInstance(attrId);
            oldAttVal = (AttributeValue)avMap.get(attr.getName().toUpperCase());
            newAttVal = (AttributeValue)newAttVals.get(attrId);
            String newAttValValue = newAttVal.getValue();
            if (Log.get().isDebugEnabled()) 
            {
                Log.get().debug("Attribute: " + attr.getName() + 
                                " has newAttValValue = " + newAttValValue);
            }
            if (newAttValValue != null && newAttValValue.length() > 0)
            {
                oldAttVal.setProperties(newAttVal);
            }
            else
            {
                oldAttVal.setDeleted(true);
                Log.get().debug("setDeleted(true)");
                attValDeleted = true;
            }
            oldAttVal.startActivitySet(activitySet);
            oldAttVal.save();
        }
        if (attValDeleted)
        {
             //Remove attribute value map from cache
             getMethodResult().remove(this, GET_MODULE_ATTRVALUES_MAP,
                                          Boolean.TRUE);
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
            Integer attrId = (Integer)iter.next();
            Attribute attr = AttributeManager.getInstance(attrId);
            if (attr.isOptionAttribute())
            {
                AttributeOption toOption = AttributeOptionManager
                     .getInstance(new Integer((String)newValues.get(attrId)));
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
            Integer attrId = (Integer)iter.next();
            Attribute attr = AttributeManager.getInstance(attrId);
            oldAttVal = (AttributeValue)avMap.get(attr.getName().toUpperCase());
            newAttVal = (AttributeValue)newAttVals.get(attrId);
            AttributeOption fromOption = null;
            AttributeOption toOption = null;

            if (newAttVal.getValue() != null)
            {
                if (newAttVal.getAttribute().isOptionAttribute())
                {
                    if (oldAttVal.getOptionId() == null)
                    {
                        fromOption = AttributeOptionManager.getInstance(ScarabConstants.INTEGER_0);
                    }
                    else
                    {
                        fromOption = oldAttVal.getAttributeOption();
                    }
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
     * This method is used with the setAttributeValues() method to 
     * Make sure that workflow is valid. It will return a non-null String
     * which is the workflow error message otherwise it will return null.
     *
     * @deprecated The attachment doesn't need to be passed into this method.
     */
    public String doCheckAttributeValueWorkflow(HashMap newAttVals, 
                                                Attachment attachment, 
                                                ScarabUser user)
        throws Exception
    {
        return doCheckAttributeValueWorkflow(newAttVals, user);
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
                getLocale(),
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
             
            if (!activitySet.sendEmail(this))
            {
                Localizable commentSaved = new L10NMessage( L10NKeySet.CommentSaved,oldComment,newComment);
                Localizable emailError   = new L10NMessage( L10NKeySet.CouldNotSendEmail);
                throw new ScarabException(L10NKeySet.ExceptionSavedButErrors,
                                          commentSaved,
                                          emailError);
            }
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
            getLocale(),
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
                                desc, attachment, oldUrl, null);
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
        Object[] args = {name};
        String desc = Localization.format(
            ScarabConstants.DEFAULT_BUNDLE_NAME,
            getLocale(),
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
                                desc, attachment, name, null);
        return activitySet;
    }

    /**
     * Returns users assigned to all user attributes.
     */
    public HashSet getAssociatedUsers() throws Exception
    {
        HashSet users = null;
        Object obj = ScarabCache.get(this, GET_ASSOCIATED_USERS); 
        if (obj == null) 
        {        
            List attributeList = getModule()
                .getUserAttributes(getIssueType(), true);
            List attributeIdList = new ArrayList();
            
            for (int i=0; i<attributeList.size(); i++) 
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
                users = new HashSet();
                Criteria crit = new Criteria()
                    .addIn(AttributeValuePeer.ATTRIBUTE_ID, attributeIdList)
                    .add(AttributeValuePeer.DELETED, false);
                crit.setDistinct();
                
                List attValues = getAttributeValues(crit);
                for (int i=0; i<attValues.size(); i++) 
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
            users = (HashSet)obj;
        }
        return users;
    }


    public String toString()
    {
        String id = null;
        try 
        {
            id = isNew() ? "New issue" : getUniqueId();
        }
        catch (Exception e)
        {
            id = "Error in getting unique id";
            Log.get().warn(id, e);
        }
        
        return super.toString() + '{' + id + '}';
    }
}
