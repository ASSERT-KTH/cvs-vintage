package org.tigris.scarab.services.module;

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

import java.util.List;
import java.util.Vector;

import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleIssueType;

import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;

/**
 * This class describes a Module within the Scarab system
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ModuleEntity.java,v 1.38 2001/11/08 02:15:08 elicia Exp $
 */
public interface ModuleEntity
{
    /**
     * The deliminator between parent/child Modules
     * This is used to build up the getName() results.
     * FIXME: define this in a properties file
     */
    public static String NAME_DELIMINATOR = " > ";

    /**
     * Get a list of <code>ScarabUser</code>'s that have the given
     * permission in the given module.
     *
     * @param permission a <code>String</code> value
     * @return ScarabUser[]
     */
    public ScarabUser[] getUsers(String permission);

    /**
     * This method is only used by the Turbine Group interface.
     * The implementation of getName() returns a unique name for
     * this Module that is human readable because our 
     * implementation of Flux needs it as well as the fact that 
     * each Group needs to have a unique name. If you want to get
     * the actual name of the Module, you need to call the getRealName
     * method.
     */
    public String getName();
    public void setName(String name);

    /**
     * This method is only used by the Turbine Group interface.
     * The implementation of getName() returns a unique name for
     * this Module that is human readable because our 
     * implementation of Flux needs it as well as the fact that 
     * each Group needs to have a unique name. If you want to get
     * the actual name of the Module, you need to call the getRealName
     * method.
     */
    public String getRealName();
    public void setRealName(String name);

    public String getCode();
    public void setCode(String code);

    public String getDomain();
    public void setDomain(String domain);

    public String getDescription();
    public void setDescription(String description);

    public String getUrl();
    public void setUrl(String url);

    public void setPrimaryKey(ObjectKey key) throws Exception;
    public NumberKey getModuleId();
    public void setModuleId(NumberKey v) throws Exception;
    
/** @deprecated THESE WILL BE DEPRECATED */
    public NumberKey getQaContactId();
/** @deprecated THESE WILL BE DEPRECATED */
    public void setQaContactId(String v ) throws Exception;
/** @deprecated THESE WILL BE DEPRECATED */
    public void setQaContactId(NumberKey v ) throws Exception;

/** @deprecated THESE WILL BE DEPRECATED */
    public NumberKey getOwnerId();
/** @deprecated THESE WILL BE DEPRECATED */
    public void setOwnerId(String v ) throws Exception;
/** @deprecated THESE WILL BE DEPRECATED */
    public void setOwnerId(NumberKey v ) throws Exception;

    public void save() throws Exception;

    public List getRModuleAttributes(IssueType issueType, boolean activeOnly)
        throws Exception;

    public List getRModuleAttributes(IssueType issueType)
        throws Exception;

    public Vector getRModuleAttributes(Criteria criteria)
        throws Exception;

    public RModuleAttribute getRModuleAttribute(Attribute attribute,
                                                IssueType issueType)
        throws Exception;

    public int getHighestSequence(IssueType issueType)
        throws Exception;

    public String getQueryKey();

    public boolean getDeleted();
    public void setDeleted(boolean b);

    public NumberKey getParentId();
    public void setParentId(NumberKey v) throws Exception;

    public void setModuleRelatedByParentId(ModuleEntity module) 
        throws Exception;

    /**
     * Get this modules direct parent
     */
    public ModuleEntity getModuleRelatedByParentIdCast() throws Exception;

    /**
     * Same as the getModuleRelatedByParentIdCast(), just a better name.
     */
    public ModuleEntity getParent() throws Exception;

    /**
     * Returns this ModuleEntities ancestors in ascending order. 
     * It does not return the 0 parent though.
     */
    public List getAncestors() throws Exception;
    
    public Issue getNewIssue(IssueType issueType)
        throws Exception;

    public Vector getRModuleIssueTypes()
        throws Exception;
        
    public List getRModuleOptions(Attribute attribute, IssueType issueType)
        throws Exception;

    public List getRModuleOptions(Attribute attribute, IssueType issueType,
                                  boolean activeOnly)
        throws Exception;

    public List getLeafRModuleOptions(Attribute attribute, IssueType issueType)
        throws Exception;

    public List getLeafRModuleOptions(Attribute attribute, IssueType issueType,
                                      boolean activeOnly)
        throws Exception;

    public ScarabUser[] getEligibleUsers(Attribute attribute)
        throws Exception;

    public ScarabUser[] getEligibleIssueReporters();

    /**
     * List of saved reports associated with this module and
     * created by the given user.
     */
    public List getSavedReports(ScarabUser user)
        throws Exception;

    /**
     * List of private queries associated with this module and issue type
     * And created by the given user.
     */
    public List getPrivateQueries(ScarabUser user, IssueType issueType)
        throws Exception;

    /**
     * List of global Query objects associated with this module and issuetype.
     */
    public List getGlobalQueries(IssueType issueType)
        throws Exception;

    /**
     * List of all Query objects associated with this module and user.
     */
    public List getAllUserQueries(ScarabUser user, IssueType issueType)
        throws Exception;

    /**
     * List of Issue Template objects associated with this module.
     */
    public List getPrivateTemplates(ScarabUser user, IssueType issueType)
        throws Exception;

    /**
     * List of global Issue Template objects associated with this module.
     */
    public List getGlobalTemplates(IssueType issueType)
        throws Exception;

    /**
     * Array of Attributes used for deduping.
     *
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getDedupeAttributes(IssueType issueType)
        throws Exception;

    /**
     * Array of Attributes used for quick search.
     *
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getQuickSearchAttributes(IssueType issueType)
        throws Exception;

    /**
     * Array of Attributes which are active and required by this module.
     *
     * @param inOrder flag determines whether the attribute order is important
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getRequiredAttributes(IssueType issueType)
        throws Exception;

    /**
     * Array of active Attributes for an Issue Type.
     *
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getActiveAttributes(IssueType issueType)
        throws Exception;

    public RModuleIssueType getRModuleIssueType(IssueType issueType)
        throws Exception;

    public List getTemplateTypes()
        throws Exception;

    /**
     * Determines whether this module allows users to vote many times for
     * the same issue.  This feature needs schema change to allow a
     * configuration screen.  Currently only one vote per issue is supported
     *
     * @return false
     */
    public boolean allowsMultipleVoting();

    /**
     * How many votes does the user have left to cast.  Currently always
     * returns 1, so a user has unlimited voting rights.  Should look to
     * UserVote for the answer when implemented properly.
     */
    public int getUnusedVoteCount(ScarabUser user);

    /**
     * Gets a list of active RModuleOptions which have had their level
     * within the options for this module set.
     *
     * @param attribute an <code>Attribute</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getOptionTree(Attribute attribute, IssueType issueType)
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
    public List getOptionTree(Attribute attribute, IssueType issueType,
                              boolean activeOnly)
        throws Exception;

    public String toString();
}
