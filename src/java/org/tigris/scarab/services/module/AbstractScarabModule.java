package org.tigris.scarab.services.module;

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
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Category;

// Turbine classes
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.BaseObject;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.BasePeer;
import org.apache.torque.oid.IDBroker;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.fulcrum.security.util.TurbineSecurityException;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.impl.db.entity.TurbineUserGroupRole;

// Scarab classes
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.services.user.UserManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.ReportPeer;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.QueryPeer;
import org.tigris.scarab.om.Scope;
import org.tigris.scarab.om.IssueTemplateInfoPeer;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.RModuleUserAttributePeer;
import org.tigris.scarab.om.RModuleIssueTypePeer;
import org.tigris.scarab.om.IssueTypePeer;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.RModuleOptionPeer;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.AttributeTypePeer;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.TransactionPeer;
import org.tigris.scarab.om.ActivityPeer;
import org.tigris.scarab.om.AttributeGroup;
import org.tigris.scarab.om.RAttributeAttributeGroup;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.security.ScarabSecurity;

import org.apache.turbine.Log;

/**
 * The ScarabModule class is the focal point for dealing with
 * Modules. It implements the concept of a ModuleEntity which is a
 * single module and is the base interface for all Modules. In code,
 * one should <strong>never reference ScarabModule directly</strong>
 * -- use its ModuleEntity interface instead.  This allows us to swap
 * out ModuleEntity implementations by modifying the Scarab.properties
 * file.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: AbstractScarabModule.java,v 1.22 2002/02/01 00:46:08 jmcnally Exp $
 */
public abstract class AbstractScarabModule
    extends BaseObject
    implements ModuleEntity, Comparable
{

    private static final String USER = "user";
    private static final String NON_USER = "non-user";
    
    /* removing the internal cache until it can be fixed using artifact_types
    private List allRModuleAttributes;
    private List activeRModuleAttributes;
    private Attribute[] activeAttributes;
    private Attribute[] dedupeAttributes;
    private Attribute[] quicksearchAttributes;
    private Attribute[] requiredAttributes;
    private Map allRModuleOptionsMap = new HashMap();
    private Map activeRModuleOptionsMap = new HashMap();
    */
    private List parentModules;


    private String domain;
    
    /**
     * Get the value of domain.
     * @return value of domain.
     */
    public String getDomain() 
    {
        return domain;
    }
    
    /**
     * Set the value of domain.
     * @param v  Value to assign to domain.
     */
    public void setDomain(String  v) 
    {
        this.domain = v;
    }
    
    /**
     * The 'long' name of the module, includes the parents.
     */
    private String name = null;

    /**
     * @see org.tigris.scarab.services.module.ModuleEntity#getUsers(String)
     */
    public abstract ScarabUser[] getUsers(String permission)
        throws Exception;

    /**
     * @see org.tigris.scarab.services.module.ModuleEntity#getUsers(String)
     */
    public abstract ScarabUser[] getUsers(List permissions)
        throws Exception;

    public abstract String getRealName();
    public abstract NumberKey getModuleId();

    /**
     * This method is an implementation of the Group.getName() method
     * and returns a module along with its ancestors
     */
    public String getName()
    {
        if (name == null)
        {
            StringBuffer sb = new StringBuffer();
            List parents = null;
            try
            {
                parents = getAncestors();
            }
            catch (Exception e)
            {
                Category.getInstance(getClass().getName()).error(e);
                return null;
            }
            Iterator itr = parents.iterator();
            boolean firstTime = true;
            while (itr.hasNext())
            {
                ModuleEntity me = (ModuleEntity) itr.next();
                if (!firstTime)
                {
                    sb.append(ModuleEntity.NAME_DELIMINATOR);
                }
                sb.append(me.getRealName());
                firstTime = false;
            }
            if (parents.size() == 1)
            {
                sb.append(ModuleEntity.NAME_DELIMINATOR);
            }
            sb.append(getRealName());
            name = sb.toString();
        }
        return name;
    }

    /**
     * This method is an implementation of the Group.setName() method
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Creates a new Issue.
     */
    public Issue getNewIssue(IssueType issueType)
        throws Exception
    {
        Issue issue = Issue.getNewInstance(this, issueType);
        issue.setDeleted(false);
        return issue;
    }

    /**
     * Returns this ModuleEntities ancestors in ascending order. 
     * It does not return the 0 parent though.
     */
    public List getAncestors()
        throws Exception
    {
        if (parentModules == null)
        {
            parentModules = new ArrayList();
            ModuleEntity parent = getParent();
            addAncestors(parent);
        }
        return parentModules;
    }

    /**
     * recursive helper method for getAncestors()
     */
    private void addAncestors(ModuleEntity module)
        throws Exception
    {
        if (!module.getParentId().equals(ROOT_ID))
        {
            addAncestors(module.getParent());
        }
        parentModules.add(module);
    }

    public ScarabUser[] getEligibleIssueReporters()
        throws Exception
    {
        return getUsers(ScarabSecurity.ISSUE__ENTER);
    }

    /**
     * The users who are possible candidates as values for the given
     * attribute.  An eligible user is determined by checking for users that
     * have the permission associated with the attribute.
     *
     * @param attribute an <code>Attribute</code> value
     * @return a <code>ScarabUser[]</code> value
     * @exception ScarabException if the attribute has no associated permission
     * @exception Exception if an error occurs
     */
    public ScarabUser[] getEligibleUsers(Attribute attribute)
        throws Exception
    {
        ScarabUser[] users = null;
        if ( attribute.isUserAttribute() ) 
        {
            String permission = attribute.getPermission();
            if ( permission == null ) 
            {
                throw new ScarabException("Attribute: " + attribute.getName() + 
                         " has no permission associated with it, so no users"
                         + " can be associated with it." );
            }
            else 
            {
                users = getUsers(permission);
            }
        }
        return users;
    }

    /**
     * Set this module's immediate parent module
     */
    public abstract void setParent(ModuleEntity v) 
        throws Exception;

    /**
     * Get this module's immediate parent module
     */
    public abstract ModuleEntity getParent() 
        throws Exception;

    /**
     * List of saved reports associated with this module and
     * created by this user.
     */
    public List getSavedReports(ScarabUser user)
        throws Exception
    {
        Criteria crit = new Criteria()
            .add(ReportPeer.MODULE_ID, getModuleId())
            .add(ReportPeer.DELETED, 0)
            .add(ReportPeer.USER_ID, user.getUserId());
        return ReportPeer.doSelect(crit);
    }


    /**
     * List of private queries associated with this module.
     * Created by this user.
     */
    public List getPrivateQueries(ScarabUser user, IssueType issueType)
        throws Exception
    {
        List queries = null;
        Criteria crit = new Criteria()
            .add(QueryPeer.MODULE_ID, getModuleId())
            .add(QueryPeer.ISSUE_TYPE_ID, issueType.getIssueTypeId())
            .add(QueryPeer.DELETED, 0)
            .add(QueryPeer.USER_ID, user.getUserId())
            .add(QueryPeer.SCOPE_ID, Scope.PERSONAL__PK);
        queries = QueryPeer.doSelect(crit);
        return queries;
    }

    /**
     * List of global Query objects associated with this module.
     */
    public List getGlobalQueries(IssueType issueType)
        throws Exception
    {
        List queries = null;
        Criteria crit = new Criteria()
            .add(QueryPeer.MODULE_ID, getModuleId())
            .add(QueryPeer.ISSUE_TYPE_ID, issueType.getIssueTypeId())
            .add(QueryPeer.DELETED, 0)
            .add(QueryPeer.SCOPE_ID, Scope.GLOBAL__PK);
        queries = QueryPeer.doSelect(crit);
        return queries;
    }

    /**
     * List of all queries for this user.
     */
    public List getAllUserQueries(ScarabUser user, IssueType issueType)
        throws Exception
    {
        List allQueries = new ArrayList();
        allQueries.addAll(getPrivateQueries(user, issueType));
        allQueries.addAll(getGlobalQueries(issueType));
        return allQueries;
    }

    /**
     * List of Issue Template objects associated with this module.
     * And issue type.
     */
    public List getPrivateTemplates(ScarabUser user, IssueType issueType)
        throws Exception
    {
        List templates = null;
        Criteria crit = new Criteria()
            .add(IssuePeer.MODULE_ID, getModuleId())
            .add(IssuePeer.DELETED, 0)
            .addJoin(TransactionPeer.TRANSACTION_ID, 
                     ActivityPeer.TRANSACTION_ID) 
            .add(TransactionPeer.CREATED_BY, user.getUserId())
            .add(IssuePeer.TYPE_ID, issueType.getTemplateId())
            .addJoin(IssueTemplateInfoPeer.ISSUE_ID,
                     IssuePeer.ISSUE_ID)
            .add(IssueTemplateInfoPeer.SCOPE_ID, Scope.PERSONAL__PK);
        crit.setDistinct();
        templates = IssuePeer.doSelect(crit);
        return templates;
    }

    /**
     * List of global Issue Template objects associated with this module.
     */
    public List getGlobalTemplates(IssueType issueType)
        throws Exception
    {
        List templates = null;
        Criteria crit = new Criteria()
            .add(IssuePeer.MODULE_ID, getModuleId())
            .add(IssuePeer.DELETED, 0)
            .add(IssuePeer.TYPE_ID, issueType.getTemplateId())
            .addJoin(IssueTemplateInfoPeer.ISSUE_ID,
                     IssuePeer.ISSUE_ID)
            .add(IssueTemplateInfoPeer.SCOPE_ID, Scope.GLOBAL__PK);
        templates = IssuePeer.doSelect(crit);
        return templates;
    }


    /**
     * gets a list of all of the Attributes in a Module based on the Criteria.
     */
    public Attribute[] getAttributes(Criteria criteria)
        throws Exception
    {
        List moduleAttributes = getRModuleAttributes(criteria);

        Attribute[] attributes = new Attribute[moduleAttributes.size()];
        for ( int i=0; i<moduleAttributes.size(); i++ )
        {
            attributes[i] =
               ((RModuleAttribute) moduleAttributes.get(i)).getAttribute();
        }
        return attributes;
    }

    /**
     * gets a list of all of the User Attributes in a Module.
     */
    public List getUserAttributes(IssueType issueType)
        throws Exception
    {
        return getUserAttributes(issueType, true);
    }

    /**
     * gets a list of all of the User Attributes in a Module.
     */
    public List getUserAttributes(IssueType issueType, boolean activeOnly)
        throws Exception
    {
        List rModuleAttributes = getRModuleAttributes(issueType, activeOnly, USER);
        List userAttributes = new ArrayList();

        for ( int i=0; i<rModuleAttributes.size(); i++ )
        {
            Attribute att = ((RModuleAttribute)rModuleAttributes.get(i)).getAttribute();
            userAttributes.add(att);
        }
        return userAttributes;
    }


    /**
     * gets a list of permissions associated with the User Attributes
     * that are active for this Module.
     */
    public List getUserPermissions(IssueType issueType)
        throws Exception
    {
        List userAttrs = getUserAttributes(issueType, true);
        List permissions = new ArrayList();
        for (int i = 0; i < userAttrs.size(); i++)
        {
            String permission = ((Attribute)userAttrs.get(i)).getPermission();
            if (!permissions.contains(permission))
            {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    /**
     * FIXME: can this be done more efficently?
     * gets highest sequence number for module-attribute map
     * so that a new RModuleAttribute can be added at the end.
     */
    public int getLastAttribute(IssueType issueType, String attributeType)
        throws Exception
    {
        List moduleAttributes = getRModuleAttributes(issueType, false, attributeType);
        int last = 0;

        for ( int i=0; i<moduleAttributes.size(); i++ )
        {
               int order = ((RModuleAttribute) moduleAttributes.get(i))
                         .getOrder();
               if (order > last)
               {
                   last = order;
               }
        }
        return last;
    }


    /**
     * FIXME: can this be done more efficently?
     * gets highest sequence number for module-attribute map
     * so that a new RModuleAttribute can be added at the end.
     */
    public int getLastAttributeOption(Attribute attribute, 
                                      IssueType issueType)
        throws Exception
    {
        List moduleOptions = getRModuleOptions(attribute, issueType);
        int last = 0;

        for ( int i=0; i<moduleOptions.size(); i++ )
        {
               int order = ((RModuleOption) moduleOptions.get(i))
                         .getOrder();
               if (order > last)
               {
                   last = order;
               }
        }
        return last;
    }

    /**
     * gets a list of all of the global Attributes that are not 
     * associated with this module and issue type
     */
    public List getAvailableAttributes(IssueType issueType, 
                                       String attributeType)
        throws Exception
    {
        List allAttributes = AttributePeer.getAttributes(attributeType);
        List availAttributes = new ArrayList();
        List rModuleAttributes = getRModuleAttributes(issueType, false,
                                                      attributeType);
        List moduleAttributes = new ArrayList();
        if (rModuleAttributes.isEmpty())
        {
             availAttributes = allAttributes;
        }
        else
        {
            for ( int i=0; i<rModuleAttributes.size(); i++ )
            {
                moduleAttributes.add(
                   ((RModuleAttribute) rModuleAttributes.get(i)).getAttribute());
            }


            for ( int i=0; i<allAttributes.size(); i++ )
            {
                Attribute att = (Attribute)allAttributes.get(i);
                if (!moduleAttributes.contains(att))
                {
                    availAttributes.add(att);
                }
            }
         }
        return availAttributes;
    }


    /**
     * gets a list of all of the Attribute options that are not
     * associated with this module and attribute.
     */
    public List getAvailableAttributeOptions(Attribute attribute,
                                             IssueType issueType)
        throws Exception
    {
        List rModuleOptions = getRModuleOptions(attribute, issueType, false);
        List moduleOptions = new ArrayList();
        if (rModuleOptions != null)
        {
            for ( int i=0; i<rModuleOptions.size(); i++ )
            {
                moduleOptions.add(
                   ((RModuleOption) rModuleOptions.get(i)).getAttributeOption());
            }
        }

        List allOptions = attribute.getAttributeOptions(true);
        List availOptions = new ArrayList();

        for ( int i=0; i<allOptions.size(); i++ )
        {
            AttributeOption option = (AttributeOption)allOptions.get(i);
            if (!moduleOptions.contains(option))
            {
                availOptions.add(option);
            }
        }
        return availOptions;
    }

    /**
     * Returns default issue list attributes for this module.
     */
    public List getDefaultRModuleUserAttributes(IssueType issueType)
        throws Exception
    {
        Criteria crit = new Criteria(2)
           .add(RModuleUserAttributePeer.USER_ID, 0)
           .add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                issueType.getIssueTypeId());
        crit.setDistinct();
        return RModuleUserAttributePeer.doSelect(crit);
    }

    /**
     * gets a list of the Issue Types for this module.
     */
    public List getIssueTypes()
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID, 
                     IssueTypePeer. ISSUE_TYPE_ID);
        crit.add(RModuleIssueTypePeer. MODULE_ID, getModuleId());
        crit.add(IssueTypePeer.PARENT_ID, 0);
        crit.add(IssueTypePeer.DELETED, 0);
        return IssueTypePeer.doSelect(crit);
    }

    /**
     * gets a list of the Issue Types for this module.
     * that get listed in the left navigation.
     */
    public List getNavIssueTypes()
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID, 
                     IssueTypePeer. ISSUE_TYPE_ID);
        crit.add(RModuleIssueTypePeer. MODULE_ID, getModuleId());
        crit.add(IssueTypePeer.PARENT_ID, 0);
        crit.add(RModuleIssueTypePeer.ACTIVE, true);
        crit.add(RModuleIssueTypePeer.DISPLAY, true);
        crit.add(IssueTypePeer.DELETED, 0);
        crit.addAscendingOrderByColumn(RModuleIssueTypePeer.PREFERRED_ORDER);
        return IssueTypePeer.doSelect(crit);
    }

    /**
     * gets a list of all of the issue types that are not associated with 
     * this module
     */
    public List getAvailableIssueTypes()
        throws Exception
    {
        List issueTypes = getIssueTypes();
        List allIssueTypes = IssueTypePeer.getAllIssueTypes(false);
        List availIssueTypes = new ArrayList();

        for ( int i=0; i<allIssueTypes.size(); i++ )
        {
            IssueType issueType = (IssueType)allIssueTypes.get(i);
            if (!issueTypes.contains(issueType))
            {
                availIssueTypes.add(issueType);
            }
        }
        return availIssueTypes;
    }

    /**
     * Overridden method.  Calls the super method and if no results are
     * returned the call is passed on to the parent module.
     * FIXME: what the heck is this thing doing? Also, we should expose
     * things as a List instead of as a Vector...ugly ugly ugly...
     */
    public Vector getRModuleAttributes(Criteria crit)
        throws Exception
    {
        List rModAtts = null;
        AbstractScarabModule module = this;
        AbstractScarabModule prevModule = null;
        do
        {
            rModAtts = module.getRModuleAttributesThisModuleOnly(crit);
            prevModule = module;
            module = (AbstractScarabModule)prevModule.getParent();
        }
        while ( rModAtts.size() == 0 &&
               !ROOT_ID.equals(prevModule.getModuleId()));

        return (Vector) rModAtts;
    }

    /**
     * Gets the RModuleAttributes that are related directly to this module.
     * Will return null, if the RModuleAttributes are inherited from its
     * parent.
     *
     * @param crit a <code>Criteria</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    protected abstract List getRModuleAttributesThisModuleOnly(Criteria crit)
        throws Exception;

    /**
     * Overridden method.
     */
    public abstract Vector getRModuleOptions(Criteria crit)
        throws Exception;

    /**
     * Adds module-attribute mapping to module.
     */
    public RModuleAttribute addRModuleAttribute(IssueType issueType)
        throws Exception
    {
        return addRModuleAttribute(issueType, NON_USER);
    }

    /**
     * Adds module-attribute mapping to module.
     */
    public RModuleAttribute addRModuleAttribute(IssueType issueType,
                                                String attributeType)
        throws Exception
    {
        RModuleAttribute rma = new RModuleAttribute();
        rma.setModuleId(getModuleId());
        rma.setIssueTypeId(issueType.getIssueTypeId());
        rma.setOrder(getLastAttribute(issueType, attributeType) + 1);
        return rma;
    }

    /**
     * Adds module-attribute-option mapping to module.
     */
    public RModuleOption addRModuleOption(IssueType issueType, 
                                          AttributeOption option)
        throws Exception
    {
        RModuleOption rmo = new RModuleOption();
        rmo.setModuleId(getModuleId());
        rmo.setIssueTypeId(issueType.getIssueTypeId());
        rmo.setOptionId(option.getOptionId());
        rmo.setDisplayValue(option.getName());
        return rmo;
    }


    /**
     * Array of Attributes used for quick search.
     *
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getQuickSearchAttributes(IssueType issueType)
        throws Exception
    {
        Criteria crit = new Criteria(3)
            .add(RModuleAttributePeer.QUICK_SEARCH, true);
        addActiveAndOrderByClause(crit, issueType);
        return getAttributes(crit);
    }


    /**
     * Array of Attributes which are active and required by this module.
     *
     * @param inOrder flag determines whether the attribute order is important
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getRequiredAttributes(IssueType issueType)
        throws Exception
    {
        Criteria crit = new Criteria(3)
            .add(RModuleAttributePeer.REQUIRED, true);
        addActiveAndOrderByClause(crit, issueType);
        return getAttributes(crit);
    }

    /**
     * Array of active Attributes for an issue type.
     *
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getActiveAttributes(IssueType issueType)
        throws Exception
    {
        Criteria crit = new Criteria(2);
        addActiveAndOrderByClause(crit, issueType);
        return getAttributes(crit);
    }

    private void addActiveAndOrderByClause(Criteria crit, IssueType issueType)
    {
        crit.add(RModuleAttributePeer.ACTIVE, true);
        crit.addAscendingOrderByColumn(RModuleAttributePeer.PREFERRED_ORDER);
        crit.addAscendingOrderByColumn(RModuleAttributePeer.DISPLAY_VALUE);
        crit.add(RModuleAttributePeer.ISSUE_TYPE_ID, 
                 issueType.getIssueTypeId());
    }

    public RModuleAttribute getRModuleAttribute(Attribute attribute, 
                            IssueType issueType)
        throws Exception
    {
        RModuleAttribute rma = null;
        List rmas = null;
        if (attribute.isUserAttribute())
        {
            rmas = getRModuleAttributes(issueType, false, USER);
        }
        else
        {
            rmas = getRModuleAttributes(issueType, false, NON_USER);
        }
        Iterator i = rmas.iterator();
        while ( i.hasNext() )
        {
            rma = (RModuleAttribute)i.next();
            if ( rma.getAttribute().equals(attribute) )
            {
                break;
            }
        }

        return rma;
    }

    /**
     * Overridden method.  Calls the super method and if no results are
     * returned the call is passed on to the parent module.
     */
    public List getRModuleAttributes(IssueType issueType)
        throws Exception
    {
        return getRModuleAttributes(issueType, false);
    }

    /**
     * Overridden method.  Calls the super method and if no results are
     * returned the call is passed on to the parent module.
     */
    public List getRModuleAttributes(IssueType issueType, boolean activeOnly)
        throws Exception
    {
        return getRModuleAttributes(issueType, activeOnly, "all");
    }

    public List getRModuleAttributes(IssueType issueType, boolean activeOnly,
                                     String attributeType)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleAttributePeer.MODULE_ID, 
                 getModuleId());
        crit.add(RModuleAttributePeer.ISSUE_TYPE_ID, 
                 issueType.getIssueTypeId());
        crit.addAscendingOrderByColumn(RModuleAttributePeer.PREFERRED_ORDER);
        crit.addAscendingOrderByColumn(RModuleAttributePeer.DISPLAY_VALUE);
        crit.addJoin(RModuleAttributePeer.ATTRIBUTE_ID, AttributePeer.ATTRIBUTE_ID);

        if ( activeOnly )
        {
            crit.add(RModuleAttributePeer.ACTIVE, true);
        }

        if (attributeType.equals(USER))
        {
           crit.add(AttributePeer.ATTRIBUTE_TYPE_ID, 
                    AttributeTypePeer.USER_TYPE_KEY);
        }
        else if (attributeType.equals(NON_USER))
        {
           crit.add(AttributePeer.ATTRIBUTE_TYPE_ID, 
                    AttributeTypePeer.USER_TYPE_KEY,
                Criteria.NOT_EQUAL);
        }
     

        return RModuleAttributePeer.doSelect(crit);
    }


    /**
     * gets a list of all of the Attributes.
     */
    public Attribute[] getAllAttributes()
        throws Exception
    {
        return getAttributes(new Criteria());
    }

    public List getRModuleOptions(Attribute attribute, IssueType issueType)
        throws Exception
    {
        return getRModuleOptions(attribute, issueType, true);
    }

    public List getRModuleOptions(Attribute attribute, IssueType issueType,
                                  boolean activeOnly)
        throws Exception
    {
        List allRModuleOptions = null;
        allRModuleOptions = getAllRModuleOptions(attribute, issueType);

if (allRModuleOptions != null)
{
        if ( activeOnly )
        {
            List activeRModuleOptions =
                    new ArrayList(allRModuleOptions.size());
            for ( int i=0; i<allRModuleOptions.size(); i++ )
            {
                RModuleOption rmo =
                    (RModuleOption)allRModuleOptions.get(i);
                if ( rmo.getActive() )
                {
                    activeRModuleOptions.add(rmo);
                }
            }
            allRModuleOptions =  activeRModuleOptions;
        }
}
        return allRModuleOptions;
    }

    private List getAllRModuleOptions(Attribute attribute, IssueType issueType)
        throws Exception
    {
        List rModOpts = null;
        List options = attribute.getAttributeOptions(true);
        NumberKey[] optIds = null;
        if (options == null)
        {
            optIds = new NumberKey[0];
        }
        else
        {
            optIds = new NumberKey[options.size()];
        }
        for ( int i=optIds.length-1; i>=0; i-- )
        {
            optIds[i] = ((AttributeOption)options.get(i)).getOptionId();
        }

        if (optIds.length > 0)
        { 
            Criteria crit = new Criteria();
            crit.add(RModuleOptionPeer.ISSUE_TYPE_ID, issueType.getIssueTypeId());
            crit.add(RModuleOptionPeer.MODULE_ID, getModuleId());
            crit.addIn(RModuleOptionPeer.OPTION_ID, optIds);
            crit.addAscendingOrderByColumn(RModuleOptionPeer.PREFERRED_ORDER);
            crit.addAscendingOrderByColumn(RModuleOptionPeer.DISPLAY_VALUE);

            AbstractScarabModule module = this;
            AbstractScarabModule prevModule = null;
            do
            {
                rModOpts = module.getRModuleOptions(crit);
                prevModule = module;
                module = (AbstractScarabModule)prevModule.getParent();
            }
            while ( rModOpts.size() == 0 &&
                   !ROOT_ID.equals(prevModule.getPrimaryKey()));
        }
        return rModOpts;
    }

    public RModuleOption getRModuleOption(AttributeOption option, 
                                          IssueType issueType)
        throws Exception
    {
        RModuleOption rmo = null;
        List rmos = getRModuleOptions(option.getAttribute(),
                                      issueType, false);
        Iterator i = rmos.iterator();
        while ( i.hasNext() )
        {
            rmo = (RModuleOption)i.next();
            if ( rmo.getAttributeOption().equals(option) )
            {
                break;
            }
        }

        return rmo;
    }

    /**
     * Gets the modules list of attribute options. Uses the
     * RModuleOption table to do the join. returns null if there
     * is any error.
     */
    public List getAttributeOptions (Attribute attribute, IssueType issueType)
        throws Exception
    {
        List attributeOptions = null;
        try
        {
            List rModuleOptions = getOptionTree(attribute, issueType, false);
            attributeOptions = new ArrayList(rModuleOptions.size());
            for ( int i=0; i<rModuleOptions.size(); i++ )
            {
                attributeOptions.add(
                    ((RModuleOption)rModuleOptions.get(i)).getAttributeOption());
            }
        }
        catch (Exception e)
        {
        }
        return attributeOptions;
    }

    public List getLeafRModuleOptions(Attribute attribute, IssueType issuetype)
        throws Exception
    {
        try
        {
            return getLeafRModuleOptions(attribute, issuetype, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public List getLeafRModuleOptions(Attribute attribute, 
                                      IssueType issueType,
                                      boolean activeOnly)
        throws Exception
    {
        List rModOpts = null;
        rModOpts = getRModuleOptions(attribute, issueType, activeOnly);
        if (rModOpts != null)
     {

        // put options in a map for searching
        Map optionsMap = new HashMap((int)(rModOpts.size()*1.5));
        for ( int i=rModOpts.size()-1; i>=0; i-- )
        {
            RModuleOption rmo = (RModuleOption)rModOpts.get(i);
            optionsMap.put(rmo.getOptionId(), null);
        }

        // remove options with descendants in the list
        for ( int i=rModOpts.size()-1; i>=0; i-- )
        {
            AttributeOption option =
                ((RModuleOption)rModOpts.get(i)).getAttributeOption();
            List descendants = option.getChildren();
            if ( descendants != null )
            {
                for ( int j=descendants.size()-1; j>=0; j-- )
                {
                    AttributeOption descendant =
                        (AttributeOption)descendants.get(j);
                    if ( optionsMap.containsKey(descendant.getOptionId()) )
                    {
                        rModOpts.remove(i);
                        break;
                    }
                }
            }
        }
}
        return rModOpts;
    }

    /**
     * Gets a list of active RModuleOptions which have had their level
     * within the options for this module set.
     *
     * @param attribute an <code>Attribute</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getOptionTree(Attribute attribute, IssueType issueType)
        throws Exception
    {
        return getOptionTree(attribute, issueType, true);
    }

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
        throws Exception
    {
        List moduleOptions = null;
try{
        moduleOptions = getRModuleOptions(attribute, issueType, activeOnly);
        int size = moduleOptions.size();
        List[] ancestors = new List[size];

        // put option id's in a map for searching and find all ancestors
        Map optionsMap = new HashMap((int)(size*1.5));
        for ( int i=size-1; i>=0; i-- )
        {
            AttributeOption option =
                ((RModuleOption)moduleOptions.get(i)).getAttributeOption();
            optionsMap.put(option.getOptionId(), null);

            List moduleOptionAncestors = option.getAncestors();
            ancestors[i] = moduleOptionAncestors;
        }

        for ( int i=0; i<size; i++ )
        {
            RModuleOption moduleOption = (RModuleOption)moduleOptions.get(i);
            AttributeOption attributeOption =
                moduleOption.getAttributeOption();

            int level = 1;
            if ( ancestors[i] != null )
            {
                for ( int j=ancestors[i].size()-1; j>=0; j-- )
                {
                    AttributeOption option =
                        (AttributeOption)ancestors[i].get(j);

                    if ( optionsMap.containsKey(option.getOptionId()) &&
                         !option.getOptionId()
                         .equals(moduleOption.getOptionId()) )
                    {
                        moduleOption.setLevel(level++);
                    }
                }
            }
        }
}catch (Exception e){e.printStackTrace();}

        return moduleOptions;

    }

    public Vector getRModuleIssueTypes()
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleIssueTypePeer.MODULE_ID, getModuleId())
        .addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID, 
                     IssueTypePeer.ISSUE_TYPE_ID)
        .add(IssueTypePeer.PARENT_ID, 0)
        .add(IssueTypePeer.DELETED, 0)
        .addAscendingOrderByColumn(RModuleIssueTypePeer.PREFERRED_ORDER);
        return RModuleIssueTypePeer.doSelect(crit);
    }

    public void addRModuleIssueType(IssueType issueType)
        throws Exception
    {
        RModuleIssueType rmit = new RModuleIssueType();
        rmit.setModuleId(getModuleId());
        rmit.setIssueTypeId(issueType.getIssueTypeId());
        rmit.setActive(false);
        rmit.setDisplay(false);
        rmit.save();

        // Create default groups
        AttributeGroup ag = issueType.createNewGroup(this);
        ag.setOrder(1);
        ag.save();
        AttributeGroup ag2 = issueType.createNewGroup(this);
        ag2.setOrder(3);
        ag2.save();
         
    }


    public RModuleIssueType getRModuleIssueType(IssueType issueType)
        throws Exception
    {
        RModuleIssueType rmit = null;
        Criteria crit = new Criteria(2);
        crit.add(RModuleIssueTypePeer.MODULE_ID, getModuleId())
           .add(RModuleIssueTypePeer.ISSUE_TYPE_ID, issueType.getIssueTypeId());
        List results = RModuleIssueTypePeer.doSelect(crit);
        if (results.size() > 0)
        {
            rmit = (RModuleIssueType)results.get(0);
        }
        return rmit;
    }

    public List getTemplateTypes()
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleIssueTypePeer.MODULE_ID, getModuleId())
        .addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID, 
                     IssueTypePeer.ISSUE_TYPE_ID)
        .add(IssueTypePeer.PARENT_ID, 0, Criteria.NOT_EQUAL)
        .add(IssueTypePeer.DELETED, 0)
        .addAscendingOrderByColumn(RModuleIssueTypePeer.PREFERRED_ORDER);
        return (List)RModuleIssueTypePeer.doSelect(crit);
    }

    /**
     * Determines whether this module allows users to vote many times for
     * the same issue.  This feature needs schema change to allow a
     * configuration screen.  Currently only one vote per issue is supported
     *
     * @return false
     */
    public boolean allowsMultipleVoting()
    {
        return false;
    }

    /**
     * How many votes does the user have left to cast.  Currently always
     * returns 1, so a user has unlimited voting rights.  Should look to
     * UserVote for the answer when implemented properly.
     */
    public int getUnusedVoteCount(ScarabUser user)
    {
        return 1;
    }

    /**
     * Returns list of queries needing approval.
     */
    public List getUnapprovedQueries() throws Exception
    {
        Criteria crit = new Criteria(3);
        crit.add(QueryPeer.APPROVED, 0)
           .add(QueryPeer.DELETED, 0);
        return (List) QueryPeer.doSelect(crit);
    }

    /**
     * Returns list of enter issue templates needing approval.
     */
    public List getUnapprovedTemplates() throws Exception
    {
        Criteria crit = new Criteria(3);
        crit.add(IssueTemplateInfoPeer.APPROVED, 0)
            .addJoin(IssuePeer.ISSUE_ID, IssueTemplateInfoPeer.ISSUE_ID)
            .add(IssuePeer.DELETED, 0);
        return (List) IssuePeer.doSelect(crit);
    }

    /**
     * sets up attributes and issue types for this module based on.
     * the parent module
     */
    protected void setInitialAttributesAndIssueTypes()
        throws Exception
    {
        // Add defaults for issue types and attributes 
        // from parent module
        NumberKey newModuleId = getModuleId();
        ModuleEntity parentModule = ModuleManager.getInstance(getParentId());
        AttributeGroup ag1;
        AttributeGroup ag2;
        RModuleAttribute rma1;
        RModuleAttribute rma2;
            
        // create enter issue template types
        List templateTypes = parentModule.getTemplateTypes();
        for (int i=0; i<templateTypes.size(); i++)
        {
            RModuleIssueType template1 = 
                (RModuleIssueType)templateTypes.get(i);
            RModuleIssueType template2 = template1.copy();
            template2.setModuleId(newModuleId);
            template2.save();
            
            //save RModuleAttributes for template types.
            IssueType it = template1.getIssueType();
            List rmas = parentModule.getRModuleAttributes(it);
            for (int j=0; j<rmas.size(); j++)
            {
                rma1 = (RModuleAttribute)rmas.get(j);
                rma2 = rma1.copy();
                rma2.setModuleId(newModuleId);
                rma2.setAttributeId(rma1.getAttributeId());
                rma2.setIssueTypeId(rma1.getIssueTypeId());
                rma2.save();
            }
        }

        // set module-issue type mappings
        List rmits = parentModule.getRModuleIssueTypes();
        for (int i=0; i<rmits.size(); i++)
        {
            RModuleIssueType rmit1 = (RModuleIssueType)rmits.get(i);
            RModuleIssueType rmit2 = rmit1.copy();
            rmit2.setModuleId(newModuleId);
            rmit2.save();
            IssueType issueType = rmit1.getIssueType();
                
            // set attribute group defaults
            List attributeGroups = issueType
                .getAttributeGroups(parentModule);
            for (int j=0; j<attributeGroups.size(); j++)
            {
                ag1 = (AttributeGroup)attributeGroups.get(j);
                ag2 = ag1.copy();
                ag2.setModuleId(newModuleId);
                ag2.getRAttributeAttributeGroups().clear();    // are saved later
                ag2.save();
                
                List attributes = ag1.getAttributes();
                for (int k=0; k<attributes.size(); k++)
                {
                    Attribute attribute = (Attribute)attributes.get(k);
                    
                    // set attribute-attribute group defaults
                    RAttributeAttributeGroup raag1 = ag1
                        .getRAttributeAttributeGroup(attribute);
                    RAttributeAttributeGroup raag2 = raag1.copy();
                    raag2.setGroupId(ag2.getAttributeGroupId());
                    raag2.setAttributeId(raag1.getAttributeId());
                    raag2.setOrder(raag1.getOrder());
                    raag2.save();
                    
                    // set module-attribute defaults
                    rma1 = parentModule
                        .getRModuleAttribute(attribute, issueType);
                    rma2 = rma1.copy();
                    rma2.setModuleId(newModuleId);
                    rma2.setAttributeId(rma1.getAttributeId());
                    rma2.setIssueTypeId(rma1.getIssueTypeId());
                    rma2.save();
                        
                    // set module-option mappings
                    if (attribute.isOptionAttribute())
                    {
                        List rmos = parentModule.getRModuleOptions(attribute,
                                                                   issueType);
                        for (int m=0; m<rmos.size(); m++)
                        {
                            RModuleOption rmo1 = (RModuleOption)rmos.get(m);
                            RModuleOption rmo2 = rmo1.copy();
                            rmo2.setOptionId(rmo1.getOptionId());
                            rmo2.setModuleId(newModuleId);
                            rmo2.setIssueTypeId(issueType.getIssueTypeId());
                            rmo2.save();
 
                            // Save module-option mappings for template types
                            RModuleOption rmo3 = rmo1.copy();
                            rmo3.setOptionId(rmo1.getOptionId());
                            rmo3.setModuleId(newModuleId);
                            rmo3.setIssueTypeId(issueType.getTemplateId());
                               rmo3.save();
                        }
                    }
                }
            }
        }
    }

    /**
     * Used for ordering Groups.
     *
     * @param obj The Object to compare to.
     * @return -1 if the name of the other object is lexically greater than 
     * this group, 1 if it is lexically lesser, 0 if they are equal.
     */
    public int compareTo(Object obj)
    {
        if (this.getClass() != obj.getClass())
        {
            throw new ClassCastException();
        }
        String name1 = ((Group)obj).getName();
        String name2 = this.getName();

        return name2.compareTo(name1);
    }

    public String toString()
    {
        String name = getName();
        if (name == null)
        {
            name = getRealName();
        }
        if (name == null)
        {
            name = getClass().getName();
        }
        return name;
    }
}
