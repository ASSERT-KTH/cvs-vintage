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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;
import com.workingdogs.village.Record;

// Turbine classes
import org.apache.torque.NoRowsException;
import org.apache.torque.TorqueException;
import org.apache.torque.om.ComboKey;
import org.apache.torque.om.SimpleKey;
import org.apache.torque.om.BaseObject;
import org.apache.torque.manager.MethodResultCache;
import org.apache.torque.util.Criteria;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.localization.Localization;
import org.apache.turbine.Turbine;

// Scarab classes
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.tools.localization.Localizable;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.ValidationException;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.workflow.WorkflowFactory;
import org.tigris.scarab.reports.ReportBridge;

/**
 * <p>
 * The ScarabModule class is the focal point for dealing with
 * Modules. It implements the concept of a Module which is a
 * single module and is the base interface for all Modules. In code,
 * one should <strong>never reference ScarabModule directly</strong>
 * -- use its Module interface instead.  This allows us to swap
 * out Module implementations by modifying the Scarab.properties
 * file.
 * </p>
 * 
 * <p>This class is the base class for 
 * <code>org.tigris.scarab.om.ScarabModule</code>. BaseScarabModule extends
 * this class and that definition is defined in the scarab-schema.xml
 * which is used by Torque to generated BaseScarabModule.</p>
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: AbstractScarabModule.java,v 1.122 2004/10/11 23:11:55 jorgeuriarte Exp $
 */
public abstract class AbstractScarabModule
    extends BaseObject
    implements Module, Comparable
{
    private static int moduleCodeLength;
	// the following Strings are method names that are used in caching results
    protected static final String GET_R_MODULE_ATTRIBUTES = 
        "getRModuleAttributes";
    protected static final String GET_DEDUPE_GROUPS_WITH_ATTRIBUTES = 
        "getDedupeGroupsWithAttributes";
    protected static final String GET_SAVED_REPORTS = 
        "getSavedReports";
    protected static final String GET_DEFAULT_RMODULE_USERATTRIBUTES = 
        "getDefaultRModuleUserAttributes";
    protected static final String GET_ISSUE_TYPES = 
        "getIssueTypes";
    protected static final String GET_NAV_ISSUE_TYPES = 
        "getNavIssueTypes";
    protected static final String GET_ALL_R_MODULE_OPTIONS = 
        "getAllRModuleOptions";
    protected static final String GET_LEAF_R_MODULE_OPTIONS = 
        "getLeafRModuleOptions";
    protected static final String GET_R_MODULE_ISSUE_TYPES = 
        "getRModuleIssueTypes";
    protected static final String GET_R_MODULE_ISSUE_TYPE = 
        "getRModuleIssueType";
    protected static final String GET_TEMPLATE_TYPES = 
        "getTemplateTypes";
    protected static final String GET_UNAPPROVED_QUERIES = 
        "getUnapprovedQueries";
    protected static final String GET_UNAPPROVED_TEMPLATES = 
        "getUnapprovedTemplates";
    protected static final String GET_AVAILABLE_ISSUE_TYPES =
        "getAvailableIssueTypes";

    private List parentModules = null;

    /** set to true while the setInitialAttributesAndIssueTypes() method is in process */
    private boolean isInitializing = false;

    /**
     * Should be called when the parentage is modified.
     */
    protected void resetAncestors()
    {
        parentModules = null;
    }

    /**
     * The 'long' name of the module, includes the parents.
     */
    private String name = null;

    /**
     * @see org.tigris.scarab.om.Module#getUsers(String)
     */
    public abstract ScarabUser[] getUsers(String permission)
        throws Exception;

    /**
     * @see org.tigris.scarab.om.Module#getUsers(String)
     */
    public abstract ScarabUser[] getUsers(List permissions)
        throws Exception;

    /**
     * @return The unadorned real name of this module; never
     * <code>null</code>.
     * @see #getName()
     */
    public abstract String getRealName();

    public abstract Integer getModuleId();

    /**
     * This method is an implementation of the Group.getName() method
     * and returns a module along with its ancestors
     */
    public String getName()
    {
        if (name == null)
        {
            boolean isRoot = getModuleId().equals(ROOT_ID);
            if (isRoot)
            {
                return getRealName();
            }
            StringBuffer sb = new StringBuffer();
            List parents = null;
            try
            {
                parents = getAncestors();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                getLog().error(e);
                return null;
            }
            Iterator itr = parents.iterator();
            boolean firstTime = true;
            while (itr.hasNext())
            {
                Module me = (Module) itr.next();
                if (!firstTime)
                {
                    sb.append(Module.NAME_DELIMINATOR);
                }
                sb.append(me.getRealName());
                firstTime = false;
            }
            // Make sure we have parents and if we are root, 
            // don't show ourselves again.
            if (parents.size() >= 1 && !isRoot)
            {
                sb.append(Module.NAME_DELIMINATOR);
            }
            // If we are root, don't show ourselves again.
            if (!isRoot)
            {
                sb.append(getRealName());
            }
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
    public synchronized List getAncestors()
        throws Exception
    {
        if (parentModules == null)
        {
            parentModules = new ArrayList();
            Module parent = getParent();
            if (parent != null && !isEndlessLoop(parent))
            {
                addAncestors(parent);
            }
        }
        return parentModules;
    }

    /**
     * recursive helper method for getAncestors()
     */
    private void addAncestors(Module module)
        throws Exception
    {
        if (!module.getParentId().equals(ROOT_ID))
        {
            addAncestors(module.getParent());
        }
        parentModules.add(module);
    }

    /**
     * check for endless loops where Module A > Module B > Module A
     */
    public boolean isEndlessLoop(Module parent)
        throws Exception
    {
        if (parent.getModuleId() != ROOT_ID)
        {
            Module parentParent = parent.getParent();
            if (parentParent != null && parentParent == this)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates new attribute group.
     */
    public AttributeGroup createNewGroup (IssueType issueType)
        throws Exception
    {
        return issueType.createNewGroup(this);
    }

    /**
     * This method is used within Wizard1.vm to get a list of attribute
     * groups which are marked as dedupe and have a list of attributes
     * in them.
     */
    public List getDedupeGroupsWithAttributes(IssueType issueType)
        throws Exception
    {
        List result = null;
        Object obj = getMethodResult()
            .get(this, GET_DEDUPE_GROUPS_WITH_ATTRIBUTES, issueType);
        if (obj == null)
        {
            List attributeGroups = issueType.getAttributeGroups(this, true);
            result = new ArrayList(attributeGroups.size());
            for (Iterator itr = attributeGroups.iterator(); itr.hasNext() ;)
            {
                AttributeGroup ag = (AttributeGroup) itr.next();
                if (ag.getDedupe() && !ag.getAttributes().isEmpty())
                {
                    result.add(ag);
                }
            }
            getMethodResult().put(result, this, GET_DEDUPE_GROUPS_WITH_ATTRIBUTES, 
                                  issueType);
        }
        else
        {
            result = (List)obj;
        }
        return result;
    }


    /**
     * List of active dedupe attribute groups associated with this module.
     */
    public List getDedupeAttributeGroups(IssueType issueType)
        throws Exception
    {
        return getDedupeAttributeGroups(issueType, true);
    }

    /**
     * List of attribute groups associated with this module.
     */
    public List getDedupeAttributeGroups(IssueType issueType,
                                         boolean activeOnly)
        throws Exception
    {
        List groups = issueType.getAttributeGroups(this, activeOnly);
        List dedupeGroups = new ArrayList();
        for (int i =0;i< groups.size(); i++)
        {
            AttributeGroup group = (AttributeGroup)groups.get(i);
            if (group.getDedupe())
            {
                dedupeGroups.add(group);
            }
        }
        return dedupeGroups;
    }

    /**
     * Gets the sequence where the dedupe screen fits between groups.
     */
    public int getDedupeSequence(IssueType issueType)
        throws Exception
    {
        return issueType.getDedupeSequence(this);
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
        if (attribute.isUserAttribute()) 
        {
            String permission = attribute.getPermission();
            if (permission == null) 
            {
                throw new ScarabException(
                        L10NKeySet.ExceptionNoAttributePermission,
                        attribute.getName());
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
    public abstract void setParent(Module v) 
        throws Exception;

    /**
     * Get this module's immediate parent module
     */
    public abstract Module getParent() 
        throws Exception;


    /**
     * List of saved reports associated with this module and
     * created by this user.
     */
    public List getSavedReports(ScarabUser user)
        throws Exception
    {
        List reports = null;
        Object obj = ScarabCache.get(this, GET_SAVED_REPORTS, user); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria()
                .add(ReportPeer.DELETED, 0);
            Criteria.Criterion cc = crit.getNewCriterion(
                ReportPeer.SCOPE_ID, Scope.MODULE__PK, Criteria.EQUAL);
            cc.and(crit.getNewCriterion(
                ReportPeer.MODULE_ID, getModuleId(), Criteria.EQUAL));
            Criteria.Criterion personalcc = crit.getNewCriterion(
                ReportPeer.SCOPE_ID, Scope.PERSONAL__PK, Criteria.EQUAL);
            personalcc.and(crit.getNewCriterion(
                ReportPeer.USER_ID, user.getUserId(), Criteria.EQUAL));
            Criteria.Criterion personalmodulecc = crit.getNewCriterion(
                ReportPeer.MODULE_ID, getModuleId(), Criteria.EQUAL);
            personalmodulecc.or(crit.getNewCriterion(
                ReportPeer.MODULE_ID, null, Criteria.EQUAL));
            personalcc.and(personalmodulecc);
            cc.or(personalcc);
            crit.add(cc);
            crit.addAscendingOrderByColumn(ReportPeer.SCOPE_ID);
            List torqueReports = ReportPeer.doSelect(crit);      
            // create ReportBridge's from torque Reports.
            if (!torqueReports.isEmpty()) 
            {
                reports = new ArrayList(torqueReports.size());
                for (Iterator i = torqueReports.iterator(); i.hasNext();) 
                {
                    Report torqueReport = (Report)i.next();
                    try 
                    {
                        reports.add( new ReportBridge(torqueReport) );
                    }
                    catch (org.xml.sax.SAXException e)
                    {
                        getLog().warn("Could not parse the report id=" +
                                 torqueReport.getReportId() + 
                                 ", so it has been marked as deleted.");
                        torqueReport.setDeleted(true);
                        torqueReport.save();
                    }                    
                }
            }
            else 
            {
                reports = Collections.EMPTY_LIST;
            }

            ScarabCache.put(reports, this, GET_SAVED_REPORTS, user);
        }
        else 
        {
            reports = (List)obj;
        }
        return reports;
    }


    /**
     * Gets a list of attributes for this module with a specific
     * issue type.
     */
    public List getAttributes(IssueType issueType)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleAttributePeer.ISSUE_TYPE_ID, issueType.getIssueTypeId());
        return getAttributes(crit);
    }

    /**
     * gets a list of all of the Attributes in a Module based on the Criteria.
     */
    public List getAttributes(Criteria criteria)
        throws Exception
    {
        List moduleAttributes = getRModuleAttributes(criteria);
        List attributes = new ArrayList(moduleAttributes.size());
        for (int i=0; i<moduleAttributes.size(); i++)
        {
            attributes.add(
               ((RModuleAttribute) moduleAttributes.get(i)).getAttribute());
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

        for (int i=0; i<rModuleAttributes.size(); i++)
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

        for (int i=0; i<moduleAttributes.size(); i++)
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
      for (int i=0; i<moduleOptions.size(); i++)
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

  /*
   * shift all the module options by 1 for all the non-active options with 
   * an order higher or equal than offset
   */
    public void shiftAttributeOption(Attribute attribute, 
                                    IssueType issueType,
                                    int offset)
        throws Exception
    {
      List moduleOptions = getRModuleOptions(attribute, issueType, false);
      RModuleOption rmo;
      for (int i=0; i<moduleOptions.size(); i++)
        {
          rmo = (RModuleOption) moduleOptions.get(i);
          int order = rmo.getOrder();
          if (order >= offset && !rmo.getActive())
            {
              rmo.setOrder(order+1);
              rmo.save();
            }
        }
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
            for (int i=0; i<rModuleAttributes.size(); i++)
            {
                moduleAttributes.add(
                   ((RModuleAttribute) rModuleAttributes.get(i)).getAttribute());
            }


            for (int i=0; i<allAttributes.size(); i++)
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
            for (int i=0; i<rModuleOptions.size(); i++)
            {
                moduleOptions.add(
                   ((RModuleOption) rModuleOptions.get(i)).getAttributeOption());
            }
        }

        List allOptions = attribute.getAttributeOptions(true);
        List availOptions = new ArrayList();

        for (int i=0; i<allOptions.size(); i++)
        {
            AttributeOption option = (AttributeOption)allOptions.get(i);
            if (!moduleOptions.contains(option) && !option.getDeleted())
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
        List result = null;
        Object obj = ScarabCache.get(this, GET_DEFAULT_RMODULE_USERATTRIBUTES, 
                                     issueType); 
        if (obj == null) 
        {        
            result = new LinkedList();
            Attribute[] attributes = new Attribute[3];
            int count = 0;
            attributes[count++] = issueType.getDefaultTextAttribute(this);
            if (attributes[0] == null) 
            {
                count = 0;
            }            
            List rma1s = getRModuleAttributes(issueType, true, NON_USER);
            Iterator i = rma1s.iterator();
            while (i.hasNext())
            {
                Attribute a = ((RModuleAttribute)i.next()).getAttribute();
                if (!a.isTextAttribute() || attributes[0] == null) 
                {
                    attributes[count++] = a;
                    break;
                }
            }

            List rma2s = getRModuleAttributes(issueType, true, USER);
            i = rma2s.iterator();
            while (i.hasNext() && count < 3)            
            {
                Attribute a = ((RModuleAttribute)i.next()).getAttribute();
                attributes[count++] = a;
            }

            // if we still have less than 3 attributes, give the non user
            // attributes another try
            i = rma1s.iterator();
            while (i.hasNext() && count < 3)            
            {
                Attribute a = ((RModuleAttribute)i.next()).getAttribute();
                if (!a.equals(attributes[0]) && !a.equals(attributes[1])) 
                {
                    attributes[count++] = a;
                }                
            }

            for (int j=0; j<attributes.length; j++) 
            {
                if (attributes[j] != null) 
                {
                    RModuleUserAttribute rmua = 
                        RModuleUserAttributeManager.getInstance();
                    rmua.setAttribute(attributes[j]);
                    rmua.setIssueType(issueType);
                    rmua.setOrder(j+1);
                    result.add(rmua);
                }
            }
            ScarabCache.put(result, this, GET_DEFAULT_RMODULE_USERATTRIBUTES, 
                            issueType);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }
    /**
     * This method is useful for getting an issue object
     * by a String id. It has some logic in it for appending
     * the Module Code as well as stripping spaces off the
     * id value using the String.trim() method.
     * @deprecated use IssueManager.getIssueById(String id, String defaultCode)
     */
    public Issue getIssueById(String id)
        throws Exception
    {
        return IssueManager.getIssueById(id, getCode());
    }

    /**
     * gets a list of the Issue Types for this module. only shows
     * active issue types
     */
    public List getIssueTypes()
        throws Exception
    {
        return getIssueTypes(true); 
    }

    /**
     * gets a list of the Issue Types for this module. only shows
     * active issue types
     */
    public List getIssueTypes(boolean activeOnly)
        throws Exception
    {
        List types = null;
        Boolean activeOnlyValue = activeOnly ? Boolean.TRUE : Boolean.FALSE;
        Object obj = ScarabCache.get(this, GET_ISSUE_TYPES, activeOnlyValue);
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID, 
                         IssueTypePeer. ISSUE_TYPE_ID);
            crit.add(RModuleIssueTypePeer.MODULE_ID, getModuleId());
            if (activeOnly)
            {
                crit.add(RModuleIssueTypePeer.ACTIVE, true);
            }
            crit.add(IssueTypePeer.PARENT_ID, 0);
            crit.add(IssueTypePeer.DELETED, 0);
            crit.addAscendingOrderByColumn(RModuleIssueTypePeer.PREFERRED_ORDER);             
            types = IssueTypePeer.doSelect(crit);
            ScarabCache.put(types, this, "getIssueTypes", activeOnlyValue);
        }
        else 
        {
            types = (List)obj;
        }
        return types;
    }

    /**
     * gets a list of the Issue Types for this module.
     * that get listed in the left navigation. only shows active issue types.
     */
    public List getNavIssueTypes()
        throws Exception
    {
        List types = null;
        Object obj = getMethodResult().get(this, GET_NAV_ISSUE_TYPES); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID, 
                         IssueTypePeer. ISSUE_TYPE_ID);
            crit.add(RModuleIssueTypePeer. MODULE_ID, getModuleId());
            crit.add(RModuleIssueTypePeer.ACTIVE, true);
            crit.add(RModuleIssueTypePeer.DISPLAY, true);
            crit.add(IssueTypePeer.PARENT_ID, 0);
            crit.add(IssueTypePeer.DELETED, 0);
            crit.addAscendingOrderByColumn(
                RModuleIssueTypePeer.PREFERRED_ORDER);
            types = IssueTypePeer.doSelect(crit);
            getMethodResult().put(types, this, GET_NAV_ISSUE_TYPES);
        }
        else 
        {
            types = (List)obj;
        }
        return types;
    }

    /**
     * gets a list of all of the issue types that are not associated with 
     * this module
     */
    public List getAvailableIssueTypes()
        throws Exception
    {
        List availIssueTypes = null;
        Object obj = ScarabCache.get(this, GET_AVAILABLE_ISSUE_TYPES); 
        if (obj == null) 
        {
            availIssueTypes = new ArrayList();
            List allIssueTypes = IssueTypePeer.getAllIssueTypes(false);
            List currentIssueTypes = getIssueTypes(false);
            Iterator iter = allIssueTypes.iterator();
            while (iter.hasNext())
            {
                IssueType issueType = (IssueType)iter.next();
                if (IssueTypePeer.getRootKey().equals(issueType.getParentId())
                    && !IssueTypePeer.getRootKey().equals(issueType.getIssueTypeId())
                    && !currentIssueTypes.contains(issueType))
                {
                    availIssueTypes.add(issueType);
                }
            }
            ScarabCache.put(availIssueTypes, this, GET_AVAILABLE_ISSUE_TYPES);
        }
        else 
        {
            availIssueTypes = (List)obj;
        }
        return availIssueTypes;
    }

    /** 
     * Returns RModuleAttributes associated with this module through the
     * foreign key in the schema. This method will return an empty list, if the
     * RModuleAttributes are inherited from its parent.  Will not return an
     * RModuleAttribute if the Attribute is deleted. NOTE: Do not try to add caching
     * to this method as it seems to break things when an attribute is changed on
     * an existing issue. (JSS)
     */
    public List getRModuleAttributes(Criteria crit)
        throws TorqueException
    {
        crit.add(RModuleAttributePeer.MODULE_ID, getModuleId());
        crit.addJoin(RModuleAttributePeer.ATTRIBUTE_ID, 
                     AttributePeer.ATTRIBUTE_ID);
        crit.add(AttributePeer.DELETED, false);
        return RModuleAttributePeer.doSelect(crit);
    }

    /**
     * Overridden method.
     */
    public abstract List getRModuleOptions(Criteria crit)
        throws TorqueException;


    /**
     * Adds module-attribute mapping to module.
     */
    public RModuleAttribute addRModuleAttribute(IssueType issueType,
                                                Attribute attribute)
        throws Exception
    {
        String attributeType = null;
        attributeType = (attribute.isUserAttribute() ? USER : NON_USER);

        RModuleAttribute rma = new RModuleAttribute();
        rma.setModuleId(getModuleId());
        rma.setIssueTypeId(issueType.getIssueTypeId());
        rma.setAttributeId(attribute.getAttributeId());
        rma.setOrder(getLastAttribute(issueType, attributeType) + 1);
        rma.setRequiredOptionId(attribute.getRequiredOptionId());
        rma.save();
        getRModuleAttributes(issueType, false, attributeType).add(rma);

        // Add to template type
        IssueType templateType = IssueTypeManager
            .getInstance(issueType.getTemplateId(), false);
        RModuleAttribute rma2 = new RModuleAttribute();
        rma2.setModuleId(getModuleId());
        rma2.setIssueTypeId(templateType.getIssueTypeId());
        rma2.setAttributeId(attribute.getAttributeId());
        rma2.setOrder(getLastAttribute(templateType, attributeType) + 1);
        rma2.save();
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
        rmo.setOrder(getLastAttributeOption(option.getAttribute(), issueType) + 1);
        return rmo;
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
        while (i.hasNext())
        {
            rma = (RModuleAttribute)i.next();
            if (rma.getAttribute().equals(attribute))
            {
                break;
            }
            else
            {
                rma = null;
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
     * Returns true if module has attributes associated with issue type.
     */
    public boolean hasAttributes(IssueType issueType)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleAttributePeer.ISSUE_TYPE_ID, issueType.getIssueTypeId());
        crit.add(RModuleAttributePeer.MODULE_ID, getModuleId());
        crit.addSelectColumn("count(" + RModuleAttributePeer.ATTRIBUTE_ID + ")");
        return ((Record)IssuePeer.doSelectVillageRecords(crit).get(0))
            .getValue(1).asInt() > 0;
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
        List rmas = null;
        Boolean activeBool = (activeOnly ? Boolean.TRUE : Boolean.FALSE);
        Object obj = getMethodResult().get(this, GET_R_MODULE_ATTRIBUTES,
            issueType,  activeBool, attributeType);
        if (obj == null)
        {
            Criteria crit = new Criteria();
            crit.add(RModuleAttributePeer.ISSUE_TYPE_ID, 
                     issueType.getIssueTypeId());
            crit.add(RModuleAttributePeer.MODULE_ID, getModuleId());
            crit.addAscendingOrderByColumn(
                RModuleAttributePeer.PREFERRED_ORDER);
            crit.addAscendingOrderByColumn(
                RModuleAttributePeer.DISPLAY_VALUE);
            
            if (activeOnly)
            {
                crit.add(RModuleAttributePeer.ACTIVE, true);
            }
            
            crit.addJoin(AttributePeer.ATTRIBUTE_ID,
                     RModuleAttributePeer.ATTRIBUTE_ID); 
            if (USER.equals(attributeType))
            {
                crit.add(AttributePeer.ATTRIBUTE_TYPE_ID, 
                         AttributeTypePeer.USER_TYPE_KEY);
            }
            else if (NON_USER.equals(attributeType))
            {
                crit.add(AttributePeer.ATTRIBUTE_TYPE_ID, 
                         AttributeTypePeer.USER_TYPE_KEY,
                         Criteria.NOT_EQUAL);
            }
            
            rmas = RModuleAttributePeer.doSelect(crit); 
            getMethodResult().put(rmas, this, GET_R_MODULE_ATTRIBUTES, 
                issueType, activeBool, attributeType);
        }
        else
        {
            rmas = (List)obj;
        }
        return rmas;
    }

    /**
     * gets a list of all of the Attributes in this module.
     */
    public List getAllAttributes()
        throws Exception
    {
        return getAttributes(new Criteria());
    }

    /**
     * gets a list of all of the active Attributes.
     * ordered by name
     */
    public List getActiveAttributesByName(IssueType issueType,
                                          String attributeType)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleAttributePeer.MODULE_ID, getModuleId());
        crit.add(RModuleAttributePeer.ISSUE_TYPE_ID, 
                 issueType.getIssueTypeId());
        crit.addJoin(RModuleAttributePeer.ATTRIBUTE_ID, 
                     AttributePeer.ATTRIBUTE_ID);
        crit.add(AttributePeer.DELETED, false);
        crit.add(RModuleAttributePeer.ACTIVE, true);
        if (USER.equals(attributeType))
        {
            crit.add(AttributePeer.ATTRIBUTE_TYPE_ID, 
                     AttributeTypePeer.USER_TYPE_KEY);
            crit.addJoin(AttributePeer.ATTRIBUTE_ID,
                     RModuleAttributePeer.ATTRIBUTE_ID); 
        }
        else if (NON_USER.equals(attributeType))
        {
            crit.add(AttributePeer.ATTRIBUTE_TYPE_ID, 
                     AttributeTypePeer.USER_TYPE_KEY,
                     Criteria.NOT_EQUAL);
        }
        crit.addAscendingOrderByColumn(
                RModuleAttributePeer.DISPLAY_VALUE);
        return AttributePeer.doSelect(crit);
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
            if (activeOnly)
            {
                List activeRModuleOptions =
                    new ArrayList(allRModuleOptions.size());
                for (int i=0; i<allRModuleOptions.size(); i++)
                {
                    RModuleOption rmo =
                        (RModuleOption)allRModuleOptions.get(i);
                    if (rmo.getActive())
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
        Object obj = ScarabCache.get(this, GET_ALL_R_MODULE_OPTIONS, 
                                     attribute, issueType); 
        if (obj == null) 
        {        
            List options = attribute.getAttributeOptions(true);
            Integer[] optIds = null;
            if (options == null)
            {
                optIds = new Integer[0];
            }
            else
            {
                optIds = new Integer[options.size()];
            }
            for (int i=optIds.length-1; i>=0; i--)
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
                rModOpts = getRModuleOptions(crit);
            }
            ScarabCache.put(rModOpts, this, GET_ALL_R_MODULE_OPTIONS, 
                            attribute, issueType); 
        }
        else 
        {
            rModOpts = (List)obj;
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
        
        RModuleOption testRMO = null;
        for (Iterator i = rmos.iterator();i.hasNext();)
        {
            testRMO = (RModuleOption)i.next();
            if (testRMO.getAttributeOption().equals(option))
            {
                rmo = testRMO;
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
            for (int i=0; i<rModuleOptions.size(); i++)
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
        Boolean activeBool = (activeOnly ? Boolean.TRUE : Boolean.FALSE);
        Object obj = getMethodResult().get(this, GET_LEAF_R_MODULE_OPTIONS, 
                                           attribute, issueType, activeBool); 
        if (obj == null) 
        {        
            rModOpts = getRModuleOptions(attribute, issueType, activeOnly);
            if (rModOpts != null)
            {
                
                // put options in a map for searching
                Map optionsMap = new HashMap((int)(rModOpts.size()*1.5));
                for (int i=rModOpts.size()-1; i>=0; i--)
                {
                    RModuleOption rmo = (RModuleOption)rModOpts.get(i);
                    optionsMap.put(rmo.getOptionId(), null);
                }
                
                // remove options with descendants in the list
                for (int i=rModOpts.size()-1; i>=0; i--)
                {
                    AttributeOption option =
                        ((RModuleOption)rModOpts.get(i)).getAttributeOption();
                    List descendants = option.getChildren();
                    if (descendants != null)
                    {
                        for (int j=descendants.size()-1; j>=0; j--)
                        {
                            AttributeOption descendant =
                                (AttributeOption)descendants.get(j);
                            if (optionsMap
                                 .containsKey(descendant.getOptionId()))
                            {
                                rModOpts.remove(i);
                                break;
                            }
                        }
                    }
                }
            }
            getMethodResult().put(rModOpts, this, GET_LEAF_R_MODULE_OPTIONS, 
                                  attribute, issueType, activeBool); 
        }
        else 
        {
            rModOpts = (List)obj;
        }
        
        return rModOpts;
    }

    /**
     * Gets a list of active RModuleOptions which have had their level
     * within the options for this module set.
     *
     * @param attribute an <code>Attribute</code> value
     * @return a <code>List</code> value
     * @exception TorqueException if an error occurs
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
     * @exception TorqueException if an error occurs
     */
    public List getOptionTree(Attribute attribute, IssueType issueType,
                              boolean activeOnly)
        throws Exception
    {
        // I think this code should place an option that had multiple parents -
        // OSX and Mac,BSD is usual example - into the list in multiple places
        // and it should have the level set differently for the two locations.
        // The code is currently only placing the option in the list once.
        // Since the behavior is not well spec'ed, leaving as it is. - jdm

        List moduleOptions = null;
        moduleOptions = getRModuleOptions(attribute, issueType, activeOnly);
        if (moduleOptions == null)
        {
            return moduleOptions;
        }

        int size = moduleOptions.size();
        List[] ancestors = new List[size];

        // find all ancestors
        for (int i=size-1; i>=0; i--)
        {
            AttributeOption option =
                ((RModuleOption)moduleOptions.get(i)).getAttributeOption();
            ancestors[i] = option.getAncestors();
        }

        for (int i=0; i<size; i++)
        {
            RModuleOption moduleOption = (RModuleOption)moduleOptions.get(i);
            int level = 1;
            if (ancestors[i] != null)
            {
                // Set level for first ancestor as the option is only
                // shown once. 
                for (int j=ancestors[i].size()-1; j>=0; j--)
                {
                    AttributeOption ancestor =
                        (AttributeOption)ancestors[i].get(j);

                    for (int k=0; k<i; k++) 
                    {
                        RModuleOption potentialParent = (RModuleOption)
                            moduleOptions.get(k);
                        if (ancestor.getOptionId()
                            .equals(potentialParent.getOptionId()) &&
                            !ancestor.getOptionId()
                            .equals(moduleOption.getOptionId())   ) 
                        {
                            moduleOption.setLevel(level++);
                        }
                    }
                }
            }
        }

        return moduleOptions;
    }
    
    /** 
     * This method is implemented in ScarabModule
     */
    public abstract List getRModuleIssueTypes() throws TorqueException;

    public List getRModuleIssueTypes(String sortColumn, String sortPolarity)
        throws TorqueException
    {
        List types = null;
        Object obj = ScarabCache.get(this, GET_R_MODULE_ISSUE_TYPES); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.add(RModuleIssueTypePeer.MODULE_ID, getModuleId())
                .addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID, 
                         IssueTypePeer.ISSUE_TYPE_ID)
                .add(IssueTypePeer.PARENT_ID, 0)
                .add(IssueTypePeer.DELETED, 0);
            if (sortColumn.equals("name"))
            {
                if (sortPolarity.equals("desc"))
                {
                    crit.addDescendingOrderByColumn(IssueTypePeer.NAME);
                }
                else
                {
                    crit.addAscendingOrderByColumn(IssueTypePeer.NAME);
                }
            }
            else
            {
                // sortColumn defaults to sequence #
                if (sortPolarity.equals("desc"))
                {
                    crit.addDescendingOrderByColumn(RModuleIssueTypePeer
                                                    .PREFERRED_ORDER);
                }
                else
                {
                    crit.addAscendingOrderByColumn(RModuleIssueTypePeer
                                                   .PREFERRED_ORDER);
                }
            }
            types = RModuleIssueTypePeer.doSelect(crit);
            ScarabCache.put(types, this, GET_R_MODULE_ISSUE_TYPES);
        }
        else 
        {
            types = (List) obj;
        }
        return types;
    }

    /**
     * Adds attribute options to a module.
     */
    public void addAttributeOption(IssueType issueType, AttributeOption option)
        throws Exception
    {
        RModuleOption rmo = addRModuleOption(issueType, option);
        rmo.save();
        shiftAttributeOption(option.getAttribute(), issueType, rmo.getOrder());

        // add module-attributeoption mappings to template type
        IssueType templateType = IssueTypeManager
                 .getInstance(issueType.getTemplateId());
        RModuleOption rmo2 = addRModuleOption(templateType, option);
        rmo2.save();
        //FIXME: is it useful to shift options for the templateType?
        //shiftAttributeOption(option.getAttribute(), templateType, rmo.getOrder());
        
        //if the cache is not cleared, when two options are added at the same time, 
        //getLastAttributeOption does not take into account the newest active options.
        ScarabCache.clear();
    }
  

    public void setRmaBasedOnIssueType(RIssueTypeAttribute ria)
        throws Exception
    {
        RModuleAttribute rma = new RModuleAttribute(); 
        rma.setModuleId(getModuleId());
        rma.setIssueTypeId(ria.getIssueTypeId());
        rma.setAttributeId(ria.getAttributeId());
        rma.setActive(ria.getActive());
        rma.setRequired(ria.getRequired());
        rma.setOrder(ria.getOrder());
        rma.setQuickSearch(ria.getQuickSearch());
        rma.setDefaultTextFlag(ria.getDefaultTextFlag());
        rma.save();
        RModuleAttribute rma2 = rma.copy();
        rma2.setModuleId(getModuleId());
        rma2.setIssueTypeId(ria.getIssueType().getTemplateId());
        rma2.setAttributeId(ria.getAttributeId());
        rma2.setActive(ria.getActive());
        rma2.setRequired(ria.getRequired());
        rma2.setOrder(ria.getOrder());
        rma2.setQuickSearch(ria.getQuickSearch());
        rma2.setDefaultTextFlag(ria.getDefaultTextFlag());
        rma2.save();
    }

    public void setRmoBasedOnIssueType(RIssueTypeOption rio)
        throws Exception
    {
        RModuleOption rmo = new RModuleOption(); 
        rmo.setModuleId(getModuleId());
        rmo.setIssueTypeId(rio.getIssueTypeId());
        rmo.setOptionId(rio.getOptionId());
        rmo.setActive(rio.getActive());
        rmo.setOrder(rio.getOrder());
        rmo.setWeight(rio.getWeight());
        rmo.save();
        RModuleOption rmo2 = rmo.copy();
        rmo2.setModuleId(getModuleId());
        rmo2.setIssueTypeId(rio.getIssueType().getTemplateId());
        rmo2.setOptionId(rio.getOptionId());
        rmo2.setActive(rio.getActive());
        rmo2.setOrder(rio.getOrder());
        rmo2.setWeight(rio.getWeight());
        rmo2.save();
    }

    protected String getValidationMessage(String typeName, String detail)
    {
        // TODO: i18n 
        return "The issue type, " + typeName + ", failed its integrity check and " 
            + "has not been added to module, " + getName() + ".  Error message was '" + 
            detail + "'."; 
    }

    public boolean includesIssueType(IssueType issueType)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleIssueTypePeer.MODULE_ID,
                 getModuleId());
        crit.add(RModuleIssueTypePeer.ISSUE_TYPE_ID,
                 issueType.getIssueTypeId());
        return RModuleIssueTypePeer.count(crit) > 0;
    }

    /**
     * Adds an issue type to a module
     * Copies properties from the global issue type's settings
     */
    public void addIssueType(IssueType issueType)
        throws Exception, ValidationException
    {
        // do some validation, refuse to add an issue type that is in a bad
        // state
        if (issueType == null) 
        {
            throw new ValidationException(L10NKeySet.ExceptionIntegrityCheckFailure,
                    "NULL",
                    getName(),
                    "Issue type was null");
        }

        // check that the issueType is not already added.
        if (includesIssueType(issueType)) 
        {
            throw new ValidationException(L10NKeySet.ExceptionDuplicateIssueType,
                    issueType,
                    getName());
        }

        String typeName = issueType.getName();
        // check attribute groups
        List testGroups = issueType.getAttributeGroups(null, false);
        try
        {
            if (testGroups == null) 
            {
                Localizable l10nMessage = new L10NMessage(L10NKeySet.IssueTypeWasNull);
                throw new ValidationException(L10NKeySet.ExceptionIntegrityCheckFailure,
                        typeName,
                        getName(),
                        l10nMessage);
            }
            else 
            {
                for (Iterator i = testGroups.iterator(); i.hasNext();)
                {
                    AttributeGroup group = (AttributeGroup)i.next();
                    // check attributes
                    List attrs = group.getAttributes();
                    if (attrs != null)
                    {
                        for (Iterator j = attrs.iterator(); j.hasNext();)
                        {
                            // check attribute-attribute group maps
                            Attribute attr = (Attribute)j.next();
                            if (attr == null) 
                            {
                                L10NMessage l10nMessage = new L10NMessage(L10NKeySet.AttributesContainsNull);
                                throw new ValidationException(L10NKeySet.ExceptionIntegrityCheckFailure,
                                        typeName,
                                        getName(),
                                        l10nMessage);
                            }                            
                            
                            // TODO: add workflow validation

                            RAttributeAttributeGroup raag = group.getRAttributeAttributeGroup(attr);
                            if (raag == null) 
                            {
                                L10NMessage l10nMessage = new L10NMessage(L10NKeySet.AttributeMappingIsMissing, attr.getName());
                                throw new ValidationException(L10NKeySet.ExceptionIntegrityCheckFailure,
                                        typeName,
                                        getName(),
                                        l10nMessage);
                          
                            }

                            // check attribute-issue type maps
                            RIssueTypeAttribute ria = issueType.getRIssueTypeAttribute(attr);
                            if (ria == null) 
                            {
                                L10NMessage l10nMessage = new L10NMessage(L10NKeySet.AttributeToIssueTypeMappingIsMissing, attr.getName());
                                throw new ValidationException(L10NKeySet.ExceptionIntegrityCheckFailure,
                                        typeName,
                                        getName(),
                                        l10nMessage);
                            }

                            // check options
                            List rios = issueType.getRIssueTypeOptions(attr, false);
                            if (rios != null)
                            {
                                for (Iterator k=rios.iterator(); k.hasNext();)
                                {
                                    if (k.next() == null) 
                                    {
                                        L10NMessage l10nMessage = new L10NMessage(L10NKeySet.ListOfOptionsMissing, attr.getName());
                                                throw new ValidationException(L10NKeySet.ExceptionIntegrityCheckFailure,
                                                typeName,
                                                getName(),
                                                l10nMessage);
                                    }
                                }
                            }
                        }
                    }
                }            
            }
        }
        catch (ValidationException ve)
        {
            throw ve;
        }
        catch (Exception e)
        {
            throw new ValidationException(
                    L10NKeySet.ExceptionGeneral, 
                    e.getMessage(),
                    e);
        }

        // okay we passed, start modifying tables

        // add module-issue type mapping
        RModuleIssueType rmit = new RModuleIssueType();
        rmit.setModuleId(getModuleId());
        rmit.setIssueTypeId(issueType.getIssueTypeId());
        rmit.setActive(true);
        rmit.setDisplay(false);
        rmit.setOrder(getRModuleIssueTypes().size() + 1);
        rmit.setDedupe(issueType.getDedupe());
        rmit.save();

        // add user attributes
        List userRIAs = issueType.getRIssueTypeAttributes(false, "user");
        for (int m=0; m<userRIAs.size(); m++)
        {
            RIssueTypeAttribute userRia = (RIssueTypeAttribute)userRIAs.get(m);
            setRmaBasedOnIssueType(userRia);
        }

        // add workflow 
        WorkflowFactory.getInstance().addIssueTypeWorkflowToModule(this, issueType);

        // add attribute groups
        List groups = issueType.getAttributeGroups(null, false);
        if (groups.isEmpty())
        {
            // Create default groups
            AttributeGroup ag = createNewGroup(issueType);
            ag.setOrder(1);
            ag.setDedupe(true);
            ag.setDescription(null);
            ag.save();
            AttributeGroup ag2 = createNewGroup(issueType);
            ag2.setOrder(3);
            ag2.setDedupe(false);
            ag2.setDescription(null);
            ag2.save();
        }
        else
        {            
            // Inherit attribute groups from issue type
            for (int i=0; i<groups.size(); i++)
            {
                AttributeGroup group = (AttributeGroup)groups.get(i);
                AttributeGroup moduleGroup = group.copyGroup();
                moduleGroup.setModuleId(getModuleId());
                moduleGroup.setIssueTypeId(issueType.getIssueTypeId());
                moduleGroup.save();

                // add attributes
                List attrs = group.getAttributes();
                if (attrs != null)
                {
                    for (int j=0; j<attrs.size(); j++)
                    {
                        // save attribute-attribute group maps
                        Attribute attr = (Attribute)attrs.get(j);
                        RAttributeAttributeGroup raag = group.getRAttributeAttributeGroup(attr);
                        RAttributeAttributeGroup moduleRaag = new RAttributeAttributeGroup();
                        moduleRaag.setAttributeId(raag.getAttributeId());
                        moduleRaag.setOrder(raag.getOrder());
                        moduleRaag.setGroupId(moduleGroup.getAttributeGroupId());
                        moduleRaag.save();

                        // save attribute-module maps
                        RIssueTypeAttribute ria = issueType.getRIssueTypeAttribute(attr);
                        setRmaBasedOnIssueType(ria);

                        // save options
                        List rios = issueType.getRIssueTypeOptions(attr, false);
                        if (rios != null)
                        {
                            for (int k=0; k<rios.size(); k++)
                            {
                                RIssueTypeOption rio = (RIssueTypeOption)rios.get(k);
                                setRmoBasedOnIssueType(rio);
                            }
                        }
                    }
                }
            }
        }
    }

    public RModuleIssueType getRModuleIssueType(IssueType issueType)
        throws Exception
    {
        RModuleIssueType rmit = null;
        try
        {
            SimpleKey[] keys = { SimpleKey.keyFor(getModuleId()), 
                                 SimpleKey.keyFor(issueType.getIssueTypeId())
            };
            rmit = RModuleIssueTypeManager.getInstance(new ComboKey(keys));
        }
        catch (NoRowsException e)
        {
            // ignore and return null, if the rmit does not exist
        }
        return rmit;
    }


    public List getTemplateTypes()
        throws Exception
    {
        List templateTypes = new ArrayList();
        Object obj = ScarabCache.get(this, GET_TEMPLATE_TYPES); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.add(RModuleIssueTypePeer.MODULE_ID, getModuleId())
                .addJoin(RModuleIssueTypePeer.ISSUE_TYPE_ID, 
                     IssueTypePeer.ISSUE_TYPE_ID)
                .add(IssueTypePeer.DELETED, 0);
            List rmits = RModuleIssueTypePeer.doSelect(crit);
            for (int i=0; i<rmits.size(); i++)
            {
                RModuleIssueType rmit = (RModuleIssueType)rmits.get(i);
                IssueType templateType = rmit.getIssueType().getTemplateIssueType();
                templateTypes.add(templateType);
            }
            ScarabCache.put(templateTypes, this, GET_TEMPLATE_TYPES);
        }
        else 
        {
            templateTypes = (List)obj;
        }
        return templateTypes;
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
        List queries = null;
        Object obj = ScarabCache.get(this, GET_UNAPPROVED_QUERIES); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria(3);
            crit.add(QueryPeer.APPROVED, 0)
                .add(QueryPeer.DELETED, 0)
                .add(QueryPeer.MODULE_ID, getModuleId());
            queries = QueryPeer.doSelect(crit);
            ScarabCache.put(queries, this, GET_UNAPPROVED_QUERIES);
        }
        else 
        {
            queries = (List)obj;
        }
        return queries;
    }

    /**
     * Returns list of enter issue templates needing approval.
     */
    public List getUnapprovedTemplates() throws Exception
    {
        List templates = null;
        Object obj = ScarabCache.get(this, GET_UNAPPROVED_TEMPLATES); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria(3);
            crit.add(IssueTemplateInfoPeer.APPROVED, 0)
                .addJoin(IssuePeer.ISSUE_ID, IssueTemplateInfoPeer.ISSUE_ID)
                .add(IssuePeer.DELETED, 0)
                .add(IssuePeer.MODULE_ID, getModuleId());
            templates = IssuePeer.doSelect(crit);
            ScarabCache.put(templates, this, GET_UNAPPROVED_TEMPLATES);
        }
        else 
        {
            templates = (List)obj;
        }
        return templates;
    }

    /**
     * for a new module: inherit issue types from parent module and 
     * from the issue types marked as default
     * parent configuration takes precedence over default
     */
    protected void setInitialAttributesAndIssueTypes()
        throws Exception
    {
        isInitializing = true;
        ValidationException ve = null;
        try
        {
        // Add defaults for issue types and attributes 
        // from parent module
        Module parentModule = ModuleManager.getInstance(getParentId());
        inheritFromParent(parentModule);        

        List defaultIssueTypes = IssueTypePeer.getDefaultIssueTypes();
        for (int i=0; i< defaultIssueTypes.size(); i++)
        {
            IssueType defaultIssueType = (IssueType)defaultIssueTypes.get(i);
            if (!includesIssueType(defaultIssueType))
            {
                try
                {
                    addIssueType(defaultIssueType);
                }
                catch (ValidationException e)
                {
                    // if one issue type is bad, continue with the rest, if 
                    // more than one bad issue type is found, stop.
                    if (ve == null) 
                    {
                        ve = e;
                    }
                    else 
                    {
                        ve = new ValidationException(
                                L10NKeySet.ExceptionMultipleProblems,
                                ve.getMessage(),
                                e);//WORK: what about the stack trace ?
                        isInitializing = false;
                        throw ve;
                    }                    
                }
            }
        }
        }
        finally
        {
            isInitializing = false;
        }
        if (ve != null) 
        {
            throw ve;
        }
    }
    
    /**
     * sets up attributes and issue types for this module based on.
     * the parent module
     */
    protected void inheritFromParent(Module parentModule)
        throws Exception
    {
        Integer newModuleId = getModuleId();
        AttributeGroup ag1;
        AttributeGroup ag2;
        RModuleAttribute rma1 = null;
        RModuleAttribute rma2 = null;
            
        //save RModuleAttributes for template types.
        List templateTypes = parentModule.getTemplateTypes();
        for (int i=0; i<templateTypes.size(); i++)
        {
            IssueType it = (IssueType)templateTypes.get(i);
            List rmas = parentModule.getRModuleAttributes(it);
            for (int j=0; j<rmas.size(); j++)
            {
                rma1 = (RModuleAttribute)rmas.get(j);
                rma2 = rma1.copy();
                rma2.setModuleId(newModuleId);
                rma2.setAttributeId(rma1.getAttributeId());
                rma2.setIssueTypeId(rma1.getIssueTypeId());
                getLog().debug("[ASM] Saving rma for new template type: " + 
                                    rma2.getModuleId()
                                    + "-" + rma2.getIssueTypeId() + "-" +
                                    rma2.getAttributeId());
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
                .getAttributeGroups(parentModule, true);
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
                }
            }

            // set module-attribute defaults
            List rmas = parentModule.getRModuleAttributes(issueType);
            if (rmas != null && rmas.size() >0)
            {
                for (int j=0; j<rmas.size(); j++)
                {
                    rma1 = (RModuleAttribute)rmas.get(j);
                    rma2 = rma1.copy();
                    rma2.setModuleId(newModuleId);
                    rma2.setAttributeId(rma1.getAttributeId());
                    rma2.setIssueTypeId(rma1.getIssueTypeId());
                    rma2.save();

                    // set module-option mappings
                    Attribute attribute = rma1.getAttribute();
                    if (attribute.isOptionAttribute())
                    {
                        List rmos = parentModule.getRModuleOptions(attribute,
                                                                   issueType);
                        if (rmos != null && rmos.size() > 0)
                        {
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
    }

    /**
     * Determines whether this module is accepting new issues.  This default
     * implementation allows new issues if the module has not been deleted.
     */
    public boolean allowsNewIssues()
    {
        return !getDeleted();
    }

    /**
     * Determines whether this module accepts issues.  This default
     * implementation does allow issues.
     */
    public boolean allowsIssues()
    {
        return true;
    }

    /**
     * Returns true if no issue types are associated with this module, or if the module
     * is currently getting its initial values set.
     */
    public boolean isInitializing()
        throws Exception
    {
        return isInitializing;
    }


    /**
     * @see org.tigris.scarab.om.Module#isGlobalModule()
     */
    public boolean isGlobalModule()
    {
        return Module.ROOT_ID.equals(getModuleId());
    }

    // FIXME! should localize
    private static final String REGEX_PREFIX = 
        "([:alpha:]+\\d+)|(issue|bug|artifact";
    private static final String REGEX_SUFFIX = 
        ")\\s*#?([:alpha:]*\\d+)";

    public String getIssueRegexString()
        throws TorqueException
    {
        // regex =  /(issue|bug)\s+#?\d+/i
        List rmitsList = getRModuleIssueTypes();
        StringBuffer regex = new StringBuffer(30 + 10 * rmitsList.size());
        regex.append(REGEX_PREFIX);
        Iterator rmits = rmitsList.iterator();
        while (rmits.hasNext()) 
        {
            regex.append('|')
                .append(((RModuleIssueType)rmits.next()).getDisplayName());
        }
        regex.append(REGEX_SUFFIX);
        return regex.toString();
    }

    /**
     * @see org.tigris.scarab.om.Module#getIssueRegex()
     */
    public REProgram getIssueRegex()
        throws TorqueException
    {
        String regex = getIssueRegexString();
        RECompiler rec = new RECompiler();
        REProgram rep = null;
        try
        {
            rep = rec.compile(regex);
        }
        catch (RESyntaxException e)
        {
            getLog().error("Could not compile regex: " + regex, e);
            try
            {
                rep = rec.compile(REGEX_PREFIX + REGEX_SUFFIX);
            }
            catch (RESyntaxException ee)
            {
                // this should not happen, but it might when we localize
                getLog().error("Could not compile standard regex", ee);
                try
                {
                    rep = rec.compile("[:alpha:]+\\d+");
                }
                catch (RESyntaxException eee)
                {
                    // this will never happen, but log it, just in case 
                    getLog().error("Could not compile simple id regex", eee);
                }
            }
        }
        // FIXME: we should cache the above result
        return rep;
    }

    /**
     * All emails related to this module will have a copy sent to
     * this address.  A system-wide default email address can be specified in 
     * Scarab.properties with the key: scarab.email.archive.toAddress
     */
    public abstract String getArchiveEmail();

    /**
     * Simple implementation returns the single configured default locale
     * from TR.props.  Will be replaced by a way to set this per module.
     *
     * @return a Locale selected for the Fulcrum Localization context
     */
    public Locale getLocale()
    {
        return ScarabConstants.DEFAULT_LOCALE;
    }

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
    public String[] getSystemEmail()
    {
        String name = Turbine.getConfiguration()
            .getString("scarab.email.default.fromName");
        if (name == null || name.length() == 0) 
        {
            name = Localization.format(ScarabConstants.DEFAULT_BUNDLE_NAME,
                getLocale(),
                "DefaultEmailNameForModule", 
                getRealName().toUpperCase());
        }
        
        String email = Turbine.getConfiguration()
            .getString("scarab.email.default.fromAddress"); 

        if (email == null || email.length() == 0) 
        {
            email = getArchiveEmail();
        }
        if (email == null || email.length() == 0) 
        {
            email = "help@localhost";
        }        
        String[] result = {name, email};
        return result;
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
        //TODO [HD] what about using instanceof, or
        //          probably better delete the whole
        //          if-block (see note n ScarabModule.compereTo()
        if (this.getClass() != obj.getClass())
        {
            throw new ClassCastException(); //EXCEPTION 
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

    private MethodResultCache getMethodResult()
    {
        return ModuleManager.getMethodResult();
    }

}

