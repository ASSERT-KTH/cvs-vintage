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
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

// Turbine classes
import org.apache.torque.om.NumberKey;
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
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.security.ScarabSecurity;
import org.tigris.scarab.security.SecurityFactory;


/**
 * The ScarabModule class is the focal point for dealing
 * with Modules. It implements the concept of a ModuleEntity
 * which is a single module and is the base interface for all
 * Modules. In code, one should never reference a ScarabModule
 * directly, instead everything should be cast to a ModuleEntity
 * and only methods in ModuleEntity should be used.
 * This allows us to swap out ModuleEntity implementations by
 * modifying the Scarab.properties file.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: ScarabModule.java,v 1.58 2001/10/26 00:43:18 elicia Exp $
 */
public class ScarabModule
    extends BaseScarabModule
    implements Persistent, ModuleEntity, Group, Comparable
{
    protected static final NumberKey ROOT_ID = new NumberKey("0");

    private Attribute[] activeAttributes;
    private Attribute[] dedupeAttributes;
    private Attribute[] quicksearchAttributes;
    private Attribute[] requiredAttributes;

    // removing the internal cache until it can be fixed using artifact_types
    // private List allRModuleAttributes;
    // private List activeRModuleAttributes;
    private List parentModules;

    private Map allRModuleOptionsMap = new HashMap();
    private Map activeRModuleOptionsMap = new HashMap();

    /**
     * The 'long' name of the module, includes the parents.
     */
    private String name = null;

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
                return null;
            }
            Iterator itr = parents.iterator();
            boolean firstTime = true;
            while (itr.hasNext())
            {
                ModuleEntity me = (ModuleEntity) itr.next();
                if (!firstTime)
                {
                    sb.append(NAME_DELIMINATOR);
                }
                sb.append(me.getRealName());
                firstTime = false;
            }
            if (parents.size() == 1)
            {
                sb.append(NAME_DELIMINATOR);
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
     * Returns this ModuleEntities ancestors in ascending order. 
     * It does not return the 0 parent though.
     */
    public List getAncestors()
        throws Exception
    {
        if (parentModules == null)
        {
            parentModules = new ArrayList();
            ModuleEntity me = (ModuleEntity) 
                this.getModuleRelatedByParentIdCast();
            addAncestors(me);
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
            addAncestors(module.getModuleRelatedByParentIdCast());
        }
        parentModules.add(module);
    }

    /**
     * Get this modules direct parent
     */
    public ModuleEntity getParent()
        throws Exception
    {
        return (ModuleEntity) 
                this.getModuleRelatedByParentIdCast();
    }

    public ScarabUser[] getEligibleIssueReporters()
    {
        ScarabSecurity security = SecurityFactory.getInstance();
        ScarabUser[] users = 
            security.getUsers(ScarabSecurity.ISSUE__ENTER, this);
        return users;
    }

    /*
    public ScarabUser[] getEligibleAssignees()
        throws Exception
    {
        Attribute attribute = Attribute
            .getInstance(AttributePeer.ASSIGNED_TO__PK);
        return getEligibleUsers(attribute);
    }
    */

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
            ScarabSecurity security = SecurityFactory.getInstance();
            String permission = attribute.getPermission();
            if ( permission == null ) 
            {
                throw new ScarabException("Attribute: " + attribute.getName() + 
                         " has no permission associated with it, so no users"
                         + " can be associated with it." );
            }
            else 
            {
                users = security.getUsers(permission, this);
            }
        }
        return users;
    }

    /**
     * Wrapper method to perform the proper cast to the BaseModule method
     * of the same name. FIXME: find a better way
     */
    public void setModuleRelatedByParentId(ModuleEntity v) throws Exception
    {
        super.setScarabModuleRelatedByParentId((ScarabModule)v);
    }

    /**
     * Cast the getScarabModuleRelatedByParentId() to a ModuleEntity
     */
    public ModuleEntity getModuleRelatedByParentIdCast() throws Exception
    {
        return (ModuleEntity) super.getScarabModuleRelatedByParentId();
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
     * gets a list of all of the Attributes that are not associated with 
     * this module
     */
    public List getAvailableAttributes()
        throws Exception
    {
        List rModuleAttributes = getRModuleAttributes();
        List moduleAttributes = new ArrayList();
        for ( int i=0; i<rModuleAttributes.size(); i++ )
        {
            moduleAttributes.add(
               ((RModuleAttribute) rModuleAttributes.get(i)).getAttribute());
        }

        List allAttributes = AttributePeer.getAllAttributes();
        List availAttributes = new ArrayList();

        for ( int i=0; i<allAttributes.size(); i++ )
        {
            Attribute att = (Attribute)allAttributes.get(i);
            if (!moduleAttributes.contains(att))
            {
                availAttributes.add(att);
            }
        }
        return availAttributes;
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
        ModuleEntity module = this;
        ModuleEntity prevModule = null;
        do
        {
            rModAtts = ((ScarabModule)module).superRModuleAttributes(crit);
            prevModule = module;
            module = (ModuleEntity) prevModule.getModuleRelatedByParentIdCast();
        }
        while ( rModAtts.size() == 0 &&
               !ROOT_ID.equals(prevModule.getModuleId()));

        return (Vector) rModAtts;
    }

    private List superRModuleAttributes(Criteria crit)
        throws Exception
    {
        return super.getRModuleAttributes(crit);
    }

    /**
     * Array of Attributes used for deduping.
     *
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getDedupeAttributes(IssueType issueType)
        throws Exception
    {
        if ( dedupeAttributes == null )
        {
            Criteria crit = new Criteria(3)
                .add(RModuleAttributePeer.DEDUPE, true);
            addActiveAndOrderByClause(crit, issueType);
            dedupeAttributes = getAttributes(crit);
        }

        return dedupeAttributes;
    }


    /**
     * Array of Attributes used for quick search.
     *
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getQuickSearchAttributes(IssueType issueType)
        throws Exception
    {
        if ( quicksearchAttributes == null )
        {
            Criteria crit = new Criteria(3)
                .add(RModuleAttributePeer.QUICK_SEARCH, true);
            addActiveAndOrderByClause(crit, issueType);
            quicksearchAttributes = getAttributes(crit);
        }
        return quicksearchAttributes;
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
        if ( requiredAttributes == null )
        {
            Criteria crit = new Criteria(3)
                .add(RModuleAttributePeer.REQUIRED, true);
            addActiveAndOrderByClause(crit, issueType);
            requiredAttributes = getAttributes(crit);
        }
        return requiredAttributes;
    }

    /**
     * Array of active Attributes for an issue type.
     *
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getActiveAttributes(IssueType issueType)
        throws Exception
    {
        if ( activeAttributes == null )
        {
            Criteria crit = new Criteria(2);
            addActiveAndOrderByClause(crit, issueType);
            activeAttributes = getAttributes(crit);
        }
        return activeAttributes;
    }

    private void addActiveAndOrderByClause(Criteria crit, IssueType issueType)
    {
        crit.add(RModuleAttributePeer.ACTIVE, true);
        crit.addAscendingOrderByColumn(RModuleAttributePeer.PREFERRED_ORDER);
        crit.addAscendingOrderByColumn(RModuleAttributePeer.DISPLAY_VALUE);
        crit.add(RModuleAttributePeer.ISSUE_TYPE_ID, issueType.getIssueTypeId());
    }

    public RModuleAttribute getRModuleAttribute(Attribute attribute, 
                                                IssueType issueType)
        throws Exception
    {
        RModuleAttribute rma = null;
        List rmas = getRModuleAttributes(issueType, false);
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

    public List getRModuleAttributes(IssueType issueType, boolean activeOnly)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleAttributePeer.ISSUE_TYPE_ID, 
                 issueType.getIssueTypeId());
        crit.addAscendingOrderByColumn(RModuleAttributePeer.PREFERRED_ORDER);
        crit.addAscendingOrderByColumn(RModuleAttributePeer.DISPLAY_VALUE);

        if ( activeOnly )
        {
            crit.add(RModuleAttributePeer.ACTIVE, true);
        }

        return getRModuleAttributes(crit);
    }


    /**
     * gets a list of all of the Attributes.
     */
    public Attribute[] getAllAttributes()
        throws Exception
    {
        return getAttributes(new Criteria());
    }

    public List getRModuleOptions(Attribute attribute)
        throws Exception
    {
        return getRModuleOptions(attribute, true);
    }

    public List getRModuleOptions(Attribute attribute, boolean activeOnly)
        throws Exception
    {
        List allRModuleOptions = (List)allRModuleOptionsMap.get(attribute);
        if ( allRModuleOptions == null )
        {
            allRModuleOptions = getAllRModuleOptions(attribute);
            allRModuleOptionsMap.put(attribute, allRModuleOptions);
        }

        if ( activeOnly )
        {
            List activeRModuleOptions =
                (List)activeRModuleOptionsMap.get(attribute);
            if ( activeRModuleOptions == null )
            {
                activeRModuleOptions =
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

                activeRModuleOptionsMap
                    .put(attribute, activeRModuleOptions);
            }

            return activeRModuleOptions;
        }
        else
        {
            return allRModuleOptions;
        }
    }

    private List getAllRModuleOptions(Attribute attribute)
        throws Exception
    {
        List options = attribute.getAttributeOptions(false);
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

        Criteria crit = new Criteria(2);
        crit.addIn(RModuleOptionPeer.OPTION_ID, optIds);
        crit.addAscendingOrderByColumn(RModuleOptionPeer.PREFERRED_ORDER);
        crit.addAscendingOrderByColumn(RModuleOptionPeer.DISPLAY_VALUE);

        List rModOpts = null;
        ScarabModule module = this;
        ScarabModule prevModule = null;
        do
        {
            rModOpts = module.getRModuleOptions(crit);
            prevModule = module;
            module = prevModule.getScarabModuleRelatedByParentId();
        }
        while ( rModOpts.size() == 0 &&
               !ROOT_ID.equals((NumberKey)prevModule.getPrimaryKey()));
        return rModOpts;
    }

    /**
     * Gets the modules list of attribute options. Uses the
     * RModuleOption table to do the join. returns null if there
     * is any error.
     */
    public List getAttributeOptions (Attribute attribute)
        throws Exception
    {
        List attributeOptions = null;
        try
        {
            List rModuleOptions = getOptionTree(attribute, false);
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

    public List getLeafRModuleOptions(Attribute attribute)
        throws Exception
    {
        try
        {
        return getLeafRModuleOptions(attribute, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public List getLeafRModuleOptions(Attribute attribute, boolean activeOnly)
        throws Exception
    {
        List rModOpts = getRModuleOptions(attribute, activeOnly);

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
    public List getOptionTree(Attribute attribute)
        throws Exception
    {
        return getOptionTree(attribute, true);
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
    public List getOptionTree(Attribute attribute, boolean activeOnly)
        throws Exception
    {
        List moduleOptions = null;
try{
        moduleOptions = getRModuleOptions(attribute, activeOnly);
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
     * Saves the module into the database
     */
    public void save() 
        throws TurbineSecurityException
    {
        // if new, make sure the code has a value.
        try
        {
            if ( isNew() )
            {
                Criteria crit = new Criteria();
                crit.add(ScarabModulePeer.MODULE_NAME, getRealName());
                crit.add(ScarabModulePeer.PARENT_ID, getParentId());
                // FIXME: this should be done with a method in ModuleEntity
                // that takes the two criteria values as a argument so that other 
                // implementations can benefit from being able to get the 
                // list of modules.
                List result = (List) ScarabModulePeer.doSelect(crit);
                if (result.size() > 0)
                {
                    throw new Exception("Sorry, a module with that name " + 
                        "and parent already exist.");
                }

                String code = getCode();
                if ( code == null || code.length() == 0 )
                {
                    if ( getParentId().equals(ROOT_ID) )
                    {
                        throw new ScarabException(
                            "A top level module addition was"
                            + " attempted without assigning a Code");
                    }

                    setCode(getModuleRelatedByParentIdCast().getCode());

                    // try to insert a row into the id_table just to be safe.
                    try
                    {
                        // FIXME: UGLY! IDBroker doesn't have a Peer yet.
                        String sql = "insert into " + IDBroker.TABLE_NAME + 
                                     " set " + 
                                     IDBroker.TABLE_NAME + "='" + 
                                                            getCode() + "'," +
                                     IDBroker.NEXT_ID  + "=1," + 
                                     IDBroker.QUANTITY  + "=1";
                        BasePeer.executeStatement(sql);
                    }
                    catch (Exception e)
                    {
                    }
                }

                // need to do this before the relationship save below
                // in order to set the moduleid for the new module.
                super.save();

                if ( getOwnerId() == null ) 
                {
                    throw new ScarabException(
                     "Can't save a project without first assigning an owner.");
                }
                grant (UserManager.getInstance(getOwnerId()), 
                    TurbineSecurity.getRole("Project Owner"));
            }
            else
            {
                super.save();
            }
        }
        catch (Exception e)
        {
            throw new TurbineSecurityException(e.getMessage(), e);
        }
    }

    // *******************************************************************
    // Turbine Group implementation get/setName and save are defined above
    // *******************************************************************


    /**
     * Removes a group from the system.
     *
     * @throws TurbineSecurityException if the Group could not be removed.
     */
    public void remove()
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented");
    }

    /**
     * Renames the group.
     *
     * @param name The new Group name.
     * @throws TurbineSecurityException if the Group could not be renamed.
     */
    public void rename(String name)
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented");
    }

    /**
     * Grants a Role in this Group to an User.
     *
     * @param user An User.
     * @param role A Role.
     * @throws TurbineSecurityException if there is a problem while assigning
     * the Role.
     */
    public void grant(User user, Role role)
        throws TurbineSecurityException
    {
        TurbineSecurity.grant(user,(Group)this,role);
    }

    /**
     * Grants Roles in this Group to an User.
     *
     * @param user An User.
     * @param roleSet A RoleSet.
     * @throws TurbineSecurityException if there is a problem while assigning
     * the Roles.
     */
    public void grant(User user, RoleSet roleSet)
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented");
    }

    /**
     * Revokes a Role in this Group from an User.
     *
     * @param user An User.
     * @param role A Role.
     * @throws TurbineSecurityException if there is a problem while unassigning
     * the Role.
     */
    public void revoke(User user, Role role)
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented");
    }

    /**
     * Revokes Roles in this group from an User.
     *
     * @param user An User.
     * @param roleSet a RoleSet.
     * @throws TurbineSecurityException if there is a problem while unassigning
     * the Roles.
     */
    public void revoke(User user, RoleSet roleSet)
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented");
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
