package org.tigris.scarab.om;

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
import java.util.List;

import org.apache.regexp.REProgram;

import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeGroup;
import org.tigris.scarab.util.ScarabPaginatedList; 

import org.apache.torque.TorqueException;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.util.Criteria;

/**
 * This class describes a Module within the Scarab system
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: Module.java,v 1.69 2003/04/05 05:59:13 jmcnally Exp $
 */
public interface Module
    extends Serializable
{
    /**
     * The deliminator between parent/child Modules
     * This is used to build up the getName() results.
     * FIXME: define this in a properties file
     */
    String NAME_DELIMINATOR = " > ";

    Integer ROOT_ID = new Integer(0);

    String USER = "user";
    String NON_USER = "non-user";

    /**
     * Get a list of <code>ScarabUser</code>'s that have the given
     * permission in the given module.
     *
     * @param permission a <code>String</code> value
     * @return ScarabUser[]
     */
    ScarabUser[] getUsers(String permission) throws Exception;

    /**
     * Get a list of <code>ScarabUser</code>'s that have any of the given
     * permissions in the given module.
     *
     * @param permissions a <code>List</code> value
     * @return ScarabUser[]
     */
    ScarabUser[] getUsers(List permissions) throws Exception;


    /**
     * Gets users which match all of the given criteria and have at least
     * one permission that is applicable to user attributes active in the
     * given issue type.  The String arguments may be null. Implementations
     * are encouraged to include users where the given Strings are 
     * contained within respective fields.  So firstName=fred would return
     * a user named fredrick.
     */
    List getUsers(String firstName, String lastName, String username,
                         String email, IssueType issueType)
        throws Exception;

    /**
     * Gets users for a given criteria, starting at a particular offset, 
     * for a given length, and have at least one permission that is 
     * applicable to user attributes active in the given issue type. 
     * This method handles the case where limiting the result size
     * is best accomplished by limiting the database transaction 
     * size, and not postfiltering the list. This is extremely helpful
     * with large user datasets. 
     */
    ScarabPaginatedList getUsers(String name, String username,  
                                        MITList mitList,
                                        int offset, int resultSize, 
                                        String sortColumn, String sortPolarity,
                                        boolean includeCommitters)
        throws Exception;

    /**
     * This method is only used by the Turbine Group interface.
     * The implementation of getName() returns a unique name for
     * this Module that is human readable because our 
     * implementation of Flux needs it as well as the fact that 
     * each Group needs to have a unique name. If you want to get
     * the actual name of the Module, you need to call the getRealName
     * method.
     */
    String getName();
    void setName(String name);

    /**
     * This method is only used by the Turbine Group interface.
     * The implementation of getName() returns a unique name for
     * this Module that is human readable because our 
     * implementation of Flux needs it as well as the fact that 
     * each Group needs to have a unique name. If you want to get
     * the actual name of the Module, you need to call the getRealName
     * method.
     */
    String getRealName();
    void setRealName(String name);

    String getCode();
    void setCode(String code);

    String getDomain();
    void setDomain(String domain);

    String getDescription();
    void setDescription(String description);

    String getUrl();
    void setUrl(String url);

    ObjectKey getPrimaryKey();
    void setPrimaryKey(ObjectKey key) throws Exception;
    Integer getModuleId();
    void setModuleId(Integer v) throws TorqueException;
    
/** @deprecated THESE WILL BE DEPRECATED */
    Integer getQaContactId();
/** @deprecated THESE WILL BE DEPRECATED */
    void setQaContactId(Integer v) throws Exception;

    Integer getOwnerId();
    void setOwnerId(Integer v) throws Exception;

    void save() throws Exception;

    /**
     * gets a list of all of the Attributes in a Module based on the Criteria.
     */
    List getAttributes(Criteria criteria)
        throws Exception;

    /**
     * Gets a list of attributes for this module with a specific
     * issue type.
     */
    List getAttributes(IssueType issueType)
        throws Exception;

    /**
     * Gets a list of all of the Attributes in this module.
     */
    List getAllAttributes()
        throws Exception;

    /**
     * Creates new attribute group.
     */
    AttributeGroup createNewGroup (IssueType issueType)
        throws Exception;

    /**
     * Returns the attribute group this attribute is associated with.
     */
    AttributeGroup getAttributeGroup(IssueType issueType, 
                                            Attribute attribute)
        throws Exception;

    /**
     * This method is used within Wizard1.vm to get a list of attribute
     * groups which are marked as dedupe and have a list of attributes
     * in them.
     */
    List getDedupeGroupsWithAttributes(IssueType issueType)
        throws Exception;

    /**
     * List of active attribute groups associated with this module.
     */
    List getAttributeGroups(IssueType issueType)
        throws Exception;

    /**
     * List of attribute groups associated with this module).
     */
    List getAttributeGroups(IssueType issueType, boolean activeOnly)
        throws Exception;

    /**
     * List of active dedupe attribute groups associated with this module.
     */
    List getDedupeAttributeGroups(IssueType issueType)
        throws Exception;

    /**
     * List of attribute groups associated with this module.
     */
    List getDedupeAttributeGroups(IssueType issueType,
                                         boolean activeOnly)
        throws Exception;

    /**
     * Gets the sequence where the dedupe screen fits between groups.
     */
    int getDedupeSequence(IssueType issueType)
        throws Exception;

    List getRModuleAttributes(IssueType issueType, boolean activeOnly,
                                     String attributeType)
        throws Exception;

    List getRModuleAttributes(IssueType issueType, boolean activeOnly)
        throws Exception;

    List getRModuleAttributes(IssueType issueType)
        throws Exception;

    List getRModuleAttributes(Criteria criteria)
        throws Exception;

    /**
     * Returns default issue list attributes for this module.
     */
    List getDefaultRModuleUserAttributes(IssueType issueType)
        throws Exception;

    RModuleAttribute getRModuleAttribute(Attribute attribute,
                                                IssueType issueType)
        throws Exception;
    int getLastAttribute(IssueType issueType, String attributeType)
        throws Exception;

    int getLastAttributeOption(Attribute attribute, 
                                      IssueType issueType)
        throws Exception;

    String getQueryKey();

    boolean getDeleted();
    void setDeleted(boolean b);

    Integer getParentId() throws TorqueException;
    void setParentId(Integer v) throws TorqueException;

    void setParent(Module module) 
        throws Exception;

    /**
     * Same as the getModuleRelatedByParentIdCast(), just a better name.
     */
    Module getParent() throws Exception;

    /**
     * Returns this ModuleEntities ancestors in ascending order. 
     * It does not return the 0 parent though.
     */
    List getAncestors() throws Exception;

    /**
     * check for endless loops where Module A > Module B > Module A
     */
    boolean isEndlessLoop(Module parent)
        throws Exception;
    
    Issue getNewIssue(IssueType issueType)
        throws Exception;

    List getRModuleIssueTypes()
        throws TorqueException;
        
    List getRModuleOptions(Attribute attribute, IssueType issueType)
        throws Exception;

    List getRModuleOptions(Attribute attribute, IssueType issueType,
                                  boolean activeOnly)
        throws Exception;

    List getRModuleOptions(Criteria crit)
        throws Exception;

    List getLeafRModuleOptions(Attribute attribute, IssueType issueType)
        throws Exception;

    List getLeafRModuleOptions(Attribute attribute, IssueType issueType,
                                      boolean activeOnly)
        throws Exception;

    RModuleOption getRModuleOption(AttributeOption option, 
                                          IssueType issueType)
        throws Exception;

    ScarabUser[] getEligibleUsers(Attribute attribute)
        throws Exception;

    ScarabUser[] getEligibleIssueReporters()
        throws Exception;

    /**
     * List of saved reports associated with this module and
     * created by the given user.
     * @param user the user
     * @return a <code>List</code> value
     */
    List getSavedReports(ScarabUser user)
        throws Exception;

    /**
     * Array of Attributes used for quick search given the specified <code>issueType</code>
     *
     * @param issueType
     * @return an <code>List</code> of Attribute objects
     */
    List getQuickSearchAttributes(IssueType issueType)
        throws Exception;

    /**
     * Array of Attributes which are active and required for an Issue Type.
     *
     * @param issueType
     * @return an <code>List</code> of Attribute objects
     */
    List getRequiredAttributes(IssueType issueType)
        throws Exception;

    /**
     * Array of active Attributes for an Issue Type.
     *
     * @return an <code>List</code> of Attribute objects
     */
    List getActiveAttributes(IssueType issueType)
        throws Exception;

    List getUserAttributes(IssueType issueType, boolean activeOnly)
        throws Exception;

    List getUserAttributes(IssueType issueType)
        throws Exception;

    List getUserPermissions(IssueType issueType)
        throws Exception;

    RModuleIssueType getRModuleIssueType(IssueType issueType)
        throws Exception;

    void addIssueType(IssueType issueType)
        throws Exception;

    void addAttributeOption(IssueType issueType, AttributeOption option)
        throws Exception;

    /**
     * if an RMA is the chosen attribute for email subjects then return it.
     * if not explicitly chosen, choose the highest ordered text attribute.
     *
     * @return the Attribute to use as the email subject,
     * or null if no suitable Attribute could be found. 
     */
    Attribute getDefaultTextAttribute(IssueType issueType)
        throws Exception;

    /**
     * Adds module-attribute mapping to module.
     */
    RModuleAttribute addRModuleAttribute(IssueType issueType,
                                                Attribute attribute)
        throws Exception;

    /**
     * Adds module-attribute-option mapping to module.
     */
    RModuleOption addRModuleOption(IssueType issueType, 
                                          AttributeOption option)
        throws Exception;

    List getIssueTypes(boolean activeOnly)
        throws Exception;
    
    List getTemplateTypes()
        throws Exception;

    List getNavIssueTypes()
        throws Exception;

    /**
     * Determines whether this module allows users to vote many times for
     * the same issue.  This feature needs schema change to allow a
     * configuration screen.  Currently only one vote per issue is supported
     *
     * @return false
     */
    boolean allowsMultipleVoting();

    /**
     * How many votes does the user have left to cast.  Currently always
     * returns 1, so a user has unlimited voting rights.  Should look to
     * UserVote for the answer when implemented properly.
     */
    int getUnusedVoteCount(ScarabUser user);

    /**
     * Returns list of queries needing approval.
     */
    List getUnapprovedQueries() throws Exception;

    /**
     * Returns list of enter issue templates needing approval.
     */
    List getUnapprovedTemplates() throws Exception;

    /**
     * Gets a list of active RModuleOptions which have had their level
     * within the options for this module set.
     *
     * @param attribute an <code>Attribute</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    List getOptionTree(Attribute attribute, IssueType issueType)
        throws Exception;

    /**
     * Gets a list of RModuleOptions which have had their level
     * within the options for this module set.
     *
     * @param attribute an <code>Attribute</code> value
     * @param activeOnly a <code>boolean</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    List getOptionTree(Attribute attribute, IssueType issueType,
                              boolean activeOnly)
        throws Exception;

    /**
     * All emails related to this module will have a copy sent to
     * this address.
     */
    String getArchiveEmail();

    /**
     * The default address that is used to fill out either the From or
     * ReplyTo header on emails related to this module.  In many cases
     * the From field is taken as the user who acted that resulted in the 
     * email, but replies should still go to the central location for
     * the module, so in this address would be used in the ReplyTo field.
     *
     * @return a <code>String[]</code> of length=2 where the first element
     * is a name such as "Scarab System" and the second is an email address.
     */
    String[] getSystemEmail();

    /**
     * Determines whether this module is accepting new issues.
     */
    boolean allowsNewIssues();

    /**
     * Determines whether this module accepts issues.
     */
    boolean allowsIssues();

    /**
     * Returns true if no issue types are associated with this module, or if the module
     * is currently getting its initial values set.
     */
    boolean isInitializing()
        throws Exception;

    /**
     * Returns true if this module is the the top level parent module.
     *
     * @return a <code>boolean</code> value
     */
    boolean isGlobalModule();

    /**
     * returns a compiled regex that can used to create a new RE
     * for matching some given text.
     */
    REProgram getIssueRegex()
        throws TorqueException;

    /**
     * This method is useful for getting an issue object
     * by a String id. It has some logic in it for appending
     * the Module Code as well as stripping spaces off the
     * id value using the String.trim() method.
     */
    Issue getIssueById(String id)
        throws Exception;

    String toString();
 
    List getRoles() throws Exception;
}

