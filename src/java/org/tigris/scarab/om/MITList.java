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

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.sql.Connection;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.SimpleKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.TorqueException;
import org.apache.torque.TorqueRuntimeException;
import org.tigris.scarab.services.security.ScarabSecurity;

/** 
 * A class representing a list (not List) of MITListItems.  MIT stands for
 * Module and IssueType.  This class contains corresponding methods to many
 * in Module which take a single IssueType.  for example
 * module.getAttributes(issueType) is replaced with 
 * mitList.getCommonAttributes() in cases where several modules and issuetypes
 * are involved.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: MITList.java,v 1.21 2003/03/25 16:57:53 jmcnally Exp $
 */
public  class MITList 
    extends org.tigris.scarab.om.BaseMITList
    implements Persistent
{
    /**
     * a local reference to the user.
     */
    private ScarabUser aScarabUser;

    List itemsScheduledForDeletion;

    public int size()
    {
        int size = 0;
        List items = getExpandedMITListItems();
        if (items != null) 
        {
            size = items.size();
        }
        return size;
    }

    public boolean isEmpty()
    {
        boolean empty = true;
        List items = getExpandedMITListItems();
        if (items != null) 
        {
            empty = items.isEmpty();
        }
        return empty;
    }

    public Iterator iterator()
    {
        Iterator i = null;
        List items = getExpandedMITListItems();
        if (items == null)
        {
            Collections.EMPTY_LIST.iterator();
        }
        else 
        {
            i = new ItemsIterator(items.iterator());
        }
        return i;
    }

    public boolean contains(MITListItem item)
    {
        boolean result = false;
        for (Iterator i = iterator(); i.hasNext() && !result;) 
        {
            result = i.next().equals(item);
        }
        return result;
    }

    public class ItemsIterator
        implements Iterator
    {
        Iterator i;
        Object currentObject;
        private ItemsIterator(Iterator i)
        {
            this.i = i;
        }

        public boolean hasNext()
        {
            return i.hasNext();
        }

        public Object next()
        {
            currentObject = i.next();
            return  currentObject;
        }
        
        public void remove()
        {
            List rawList = null;
            try
            {
                rawList = getMITListItems();
            }
            catch (TorqueException e)
            {
                throw new TorqueRuntimeException(e);
            }

            if (rawList.contains(currentObject)) 
            {
                rawList.remove(currentObject);
                i.remove();
            }
            else 
            {
                throw new UnsupportedOperationException("Removing items " +
                    "from a list containing wildcards is not supported.");
            }
        }
    }

    /**
     * Alias for getModifiable()
     *
     * @return a <code>boolean</code> value
     */
    public boolean isModifiable()
    {
        return getModifiable();
    }

    public boolean isAnonymous()
    {
        return !isNew() && getName() == null;
    }

    /**
     * Makes a copy of this object.  
     * It creates a new object filling in the simple attributes.
     * It then fills all the association collections and sets the
     * related objects to isNew=true.
     */
    public MITList copy() throws TorqueException
    {
        MITList copyObj = new MITList();
        copyObj.setName(getName());
        copyObj.setActive(getActive());
        copyObj.setModifiable(getModifiable());
        copyObj.setUserId(getUserId());

        List v = getMITListItems();
        for (int i=0; i<v.size(); i++)
        {
            MITListItem obj = (MITListItem) v.get(i);
            copyObj.addMITListItem(obj.copy());
        }

        return copyObj;
    }


    /**
     * Creates a new MITList containing only those items from this list
     * for which the searcher has the given permission.
     *
     * @param permission a <code>String</code> value
     * @param searcher a <code>ScarabUser</code> value
     * @return a <code>MITList</code> value
     */
    public MITList getPermittedSublist(String[] permissions, ScarabUser user)
        throws Exception
    {
        MITList sublist = new MITList();
        sublist.setScarabUser(getScarabUser());
        List items = getExpandedMITListItems();
        Module[] validModules = user.getModules(permissions);

        Set moduleIds = new HashSet();
        for (int j=0; j<validModules.length; j++) 
        {
            moduleIds.add(validModules[j].getModuleId());
        }
        
        for (Iterator i = items.iterator(); i.hasNext();) 
        {
            MITListItem item = (MITListItem)i.next();
            if (moduleIds.contains(item.getModuleId())) 
            {
                sublist.addMITListItem(item);
            }
        }
        
        return sublist;
    }


    public MITListItem getFirstItem()
    {
        MITListItem i = null;
        List items = getExpandedMITListItems();
        if (items != null)
        {
            i = (MITListItem)items.get(0);
        }
        return i;
    }

    public boolean isSingleModuleIssueType()
    {
        return size() == 1 
            && getFirstItem().isSingleModuleIssueType();
    }

    public boolean isSingleModule()
        throws TorqueException
    {
        List ids = getModuleIds();
        return ids.size() == 1;
    }

    public boolean isSingleIssueType()
        throws TorqueException
    {
        List ids = getIssueTypeIds();
        return ids.size() == 1;
    }

    public Module getModule()
        throws Exception
    {
        if (!isSingleModule()) 
        {
            throw new IllegalStateException("method should not be called on" +
                " a list including more than one module.");
        }
        return getModule(getFirstItem());
    }

    public IssueType getIssueType()
        throws Exception
    {
        if (!isSingleIssueType()) 
        {
            throw new IllegalStateException("method should not be called on" +
                " a list including more than one issue type.");
        }
        return getIssueType(getFirstItem());
    }

    IssueType getIssueType(MITListItem item)
        throws Exception
    {
        IssueType it = null;
        if (item.getIssueTypeId() == null) 
        {
            it = getScarabUser().getCurrentIssueType();
        }
        else 
        {
            it = item.getIssueType();
        }
        return it;
    }

    Module getModule(MITListItem item)
        throws Exception
    {
        Module module = null;
        if (item.getModuleId() == null) 
        {
            module = getScarabUser().getCurrentModule();
        }
        else 
        {
            module = item.getModule();
        }
        return module;
    }

    /**
     * Declares an association between this object and a ScarabUser object
     *
     * @param v
     */
    public void setScarabUser(ScarabUser v) 
        throws TorqueException
    {
        if (v == null) 
        {
            throw new IllegalArgumentException("cannot set user to null.");
        }
        
        super.setScarabUser(v);
        aScarabUser = v;
    }

                 
    public ScarabUser getScarabUser()
        throws TorqueException
    {
        ScarabUser user = null;
        if (aScarabUser == null) 
        {
            user = super.getScarabUser();
        }
        else
        {
            user = aScarabUser;
        }
        return user;
    }


    public List getCommonAttributes()
        throws Exception
    {
        List matchingAttributes = new ArrayList();
        MITListItem item = getFirstItem();
        
        List rmas = getModule(item)
            .getRModuleAttributes(getIssueType(item));
        for (Iterator i = rmas.iterator(); i.hasNext();) 
        {
            RModuleAttribute rma = (RModuleAttribute)i.next();
            Attribute att = rma.getAttribute();
            if (rma.getActive() && (size() == 1 || isCommon(att)))
            {
                matchingAttributes.add(att);
            }
        }             
        
        return matchingAttributes;
    }

    /**
     * Checks all items to see if they contain the attribute.
     *
     * @param attribute an <code>Attribute</code> value
     * @return a <code>boolean</code> value
     */
    public boolean isCommon(Attribute attribute)
        throws Exception
    {
        boolean common = true;
        Iterator items = iterator();
        while (items.hasNext() && common) 
        {
            MITListItem compareItem = (MITListItem)items.next();
            RModuleAttribute modAttr = getModule(compareItem)
                        .getRModuleAttribute(attribute, 
                                             getIssueType(compareItem));
            common = modAttr != null && modAttr.getActive();
        }
        return common;
    }


    public List getCommonNonUserAttributes()
        throws Exception
    {
        if (size() < 1) 
        {
            throw new IllegalStateException("method should not be called on" +
                " an empty list.");
        }
        
        List matchingAttributes = new ArrayList();
        MITListItem item = getFirstItem();
        
        List rmas = getModule(item)
            .getRModuleAttributes(getIssueType(item));
        Iterator i = rmas.iterator();
        while (i.hasNext()) 
        {
            RModuleAttribute rma = (RModuleAttribute)i.next();
            Attribute att = rma.getAttribute();
            if (!att.isUserAttribute() && rma.getActive() && isCommon(att))
            {
                matchingAttributes.add(att);
            }
        }             
        
        return matchingAttributes;
    }

    public List getCommonOptionAttributes()
        throws Exception
    {
        if (size() < 1) 
        {
            throw new IllegalStateException("method should not be called on" +
                " an empty list.");
        }
        
        List matchingAttributes = new ArrayList();
        MITListItem item = getFirstItem();
        
        List rmas = getModule(item)
            .getRModuleAttributes(getIssueType(item));
        Iterator i = rmas.iterator();
        while (i.hasNext()) 
        {
            RModuleAttribute rma = (RModuleAttribute)i.next();
            Attribute att = rma.getAttribute();
            if (att.isOptionAttribute() && rma.getActive() && isCommon(att))
            {
                matchingAttributes.add(att);
            }
        }             
        
        return matchingAttributes;
    }


    /**
     * gets a list of all of the User Attributes common to all modules in 
     * the list.
     */
    public List getCommonUserAttributes()
        throws Exception
    {
        List attributes = null;
        if (isSingleModuleIssueType()) 
        {
            attributes = getModule().getUserAttributes(getIssueType());
        }
        else 
        {
            List matchingAttributes = new ArrayList();
            MITListItem item = getFirstItem();
            List rmas = getModule(item)
                .getRModuleAttributes(getIssueType(item), true, Module.USER);
            Iterator i = rmas.iterator();
            while (i.hasNext()) 
            {
                RModuleAttribute rma = (RModuleAttribute)i.next();
                Attribute att = rma.getAttribute();
                if (rma.getActive() && isCommon(att)) 
                {
                    matchingAttributes.add(att);   
                }            
            }
            attributes = matchingAttributes;
        }
        return attributes;
    }


    /**
     * potential assignee must have at least one of the permissions
     * for the user attributes in all the modules.
     */
    public List getPotentialAssignees()
        throws Exception
    {
        List users = new ArrayList();
        List perms = getUserAttributePermissions();
        if (isSingleModule()) 
        {
            ScarabUser[] userArray = getModule().getUsers(perms);
            for (int i=0;i<userArray.length; i++)
            {
                users.add(userArray[i]);
            }            
        }
        else 
        {
            MITListItem item = getFirstItem();
            ScarabUser[] userArray = getModule(item).getUsers(perms);
            List modules = getModules();
            for (int i=0;i<userArray.length; i++)
            {
                boolean validUser = false;
                ScarabUser user = userArray[i];
                for (Iterator j=perms.iterator(); j.hasNext() && !validUser;) 
                {                    
                    validUser = user.hasPermission((String)j.next(), modules); 
                }
                if (validUser) 
                {
                    users.add(user);       
                }
            }            
        }
        return users;
    }

    /**
     * gets a list of permissions associated with the User Attributes
     * that are active for this Module.
     */
    public List getUserAttributePermissions()
        throws Exception
    {
        List userAttrs = getCommonUserAttributes();
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


    public List getCommonRModuleUserAttributes()
        throws Exception
    {
        List matchingRMUAs = new ArrayList();
        List rmuas = getSavedRMUAs();
        int sizeGoal = rmuas.size();
        if (sizeGoal == 0) 
        {
            sizeGoal = 3;
        }
        
        if (rmuas.isEmpty()) 
        {
            MITListItem item = getFirstItem();
            Module module = getModule(item);
            IssueType issueType = getIssueType(item);
            rmuas = getScarabUser()
                .getRModuleUserAttributes(module, issueType);
            if (rmuas.isEmpty())
            {
                rmuas = module.getDefaultRModuleUserAttributes(issueType);
            }
        }
        
        Iterator i = rmuas.iterator();
        while (i.hasNext()) 
        {
            RModuleUserAttribute rmua = (RModuleUserAttribute)i.next();
            Attribute att = rmua.getAttribute();
            if (isCommon(att)) 
            {
                matchingRMUAs.add(rmua);   
            }            
        }
        // if nothing better, go with random common attributes
        int moreAttributes = sizeGoal - matchingRMUAs.size();
        if (moreAttributes > 0) 
        {
            Iterator attributes = getCommonAttributes().iterator();
            int k=1;
            while (attributes.hasNext() && moreAttributes > 0) 
            {
                Attribute attribute = (Attribute)attributes.next();
                boolean isInList = false;
                i = matchingRMUAs.iterator();
                while (i.hasNext()) 
                {
                    RModuleUserAttribute rmua = (RModuleUserAttribute)i.next();
                    if (rmua.getAttribute().equals(attribute)) 
                    {
                        isInList = true;
                        break;
                    }
                }
                if (!isInList) 
                {
                    RModuleUserAttribute rmua = 
                        getNewRModuleUserAttribute(attribute);
                    rmua.setOrder(k++);
                    rmua.save();
                    matchingRMUAs.add(rmua);
                    moreAttributes--;
                }
            }
        }
        
        return matchingRMUAs;
    }

    protected RModuleUserAttribute getNewRModuleUserAttribute(
        Attribute attribute)
        throws Exception
    {
        RModuleUserAttribute result = RModuleUserAttributeManager.getInstance();
        result.setUserId(getUserId());
        result.setAttributeId(attribute.getAttributeId());
        if (!isNew()) 
        {
            result.setListId(getListId());
        }
        else 
        {
            if (isSingleModule()) 
            {
                result.setModuleId(getModule().getModuleId());
            }
            if (isSingleIssueType()) 
            {
                result.setIssueTypeId(getIssueType().getIssueTypeId());
            }
        }
        return result;
    }


    protected List getSavedRMUAs()
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleUserAttributePeer.USER_ID, getUserId());
        if (!isNew())
        {
            crit.add(RModuleUserAttributePeer.LIST_ID, getListId());
        }
        else if (isSingleModuleIssueType())        
        {
            crit.add(RModuleUserAttributePeer.LIST_ID, null);
            crit.add(RModuleUserAttributePeer.MODULE_ID, 
                     getModule().getModuleId());
            crit.add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                     getIssueType().getIssueTypeId());
        }
        else if (isSingleModule())
        {
            crit.add(RModuleUserAttributePeer.LIST_ID, null);
            crit.add(RModuleUserAttributePeer.MODULE_ID, 
                     getModule().getModuleId());
            crit.add(RModuleUserAttributePeer.ISSUE_TYPE_ID, null);
        }
        else if (isSingleIssueType())
        {
            crit.add(RModuleUserAttributePeer.LIST_ID, null);
            crit.add(RModuleUserAttributePeer.MODULE_ID, null);
            crit.add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                     getIssueType().getIssueTypeId());
        }
        else 
        {
            crit.add(RModuleUserAttributePeer.LIST_ID, null);
            crit.add(RModuleUserAttributePeer.MODULE_ID, null);
            crit.add(RModuleUserAttributePeer.ISSUE_TYPE_ID, null);            
        }
        crit.addAscendingOrderByColumn(RModuleUserAttributePeer.PREFERRED_ORDER);
                
        return RModuleUserAttributePeer.doSelect(crit);
    }

    public List getCommonLeafRModuleOptions(Attribute attribute)
        throws Exception
    {
        if (size() < 1) 
        {
            throw new IllegalStateException("method should not be called on" +
                " an empty list.");
        }
        
        List matchingRMOs = new ArrayList();
        MITListItem item = getFirstItem();
        List rmos = getModule(item)
            .getLeafRModuleOptions(attribute, getIssueType(item));
        Iterator i = rmos.iterator();
        while (i.hasNext()) 
        {
            RModuleOption rmo = (RModuleOption)i.next();
            AttributeOption option = rmo.getAttributeOption();
            if (rmo.getActive() && isCommon(option)) 
            {
                matchingRMOs.add(rmo);   
            }            
        }
        return matchingRMOs;
    }

    public List getCommonRModuleOptionTree(Attribute attribute)
        throws Exception
    {
        if (size() < 1) 
        {
            throw new IllegalStateException("method should not be called on" +
                " an empty list.");
        }
        
        List matchingRMOs = new ArrayList();
        MITListItem item = getFirstItem();
        List rmos = getModule(item)
            .getOptionTree(attribute, getIssueType(item));
        Iterator i = rmos.iterator();
        while (i.hasNext()) 
        {
            RModuleOption rmo = (RModuleOption)i.next();
            AttributeOption option = rmo.getAttributeOption();
            if (rmo.getActive() && isCommon(option)) 
            {
                matchingRMOs.add(rmo);   
            }            
        }
        return matchingRMOs;
    }

    public List getDescendantsUnion(AttributeOption option)
        throws Exception
    {
        if (size() < 1) 
        {
            throw new IllegalStateException("method should not be called on" +
                " an empty list.");
        }
        
        List matchingRMOs = new ArrayList();
        Iterator items = iterator();
        while (items.hasNext()) 
        {
            MITListItem item = (MITListItem)items.next();
            IssueType issueType = getIssueType(item);
            List rmos = getModule(item)
                .getRModuleOption(option, issueType).getDescendants(issueType);
            Iterator i = rmos.iterator();
            while (i.hasNext()) 
            {
                RModuleOption rmo = (RModuleOption)i.next();
                if (!matchingRMOs.contains(rmo)) 
                {
                    matchingRMOs.add(rmo);
                }
            }
        }
        
        return matchingRMOs;
    }


    /**
     * Checks all items after the first to see if they contain the attribute.
     * It is assumed the attribute is included in the first item.
     *
     * @param option an <code>Attribute</code> value
     * @return a <code>boolean</code> value
     */
    public boolean isCommon(AttributeOption option)
        throws Exception
    {
        boolean common = true;
        Iterator items = iterator();
        while (items.hasNext() && common) 
        {
            MITListItem compareItem = (MITListItem)items.next();
            RModuleOption modOpt = getModule(compareItem)
                .getRModuleOption(option, getIssueType(compareItem));
            common = modOpt != null && modOpt.getActive();
        }
        return common;
    }

    public List getModuleIds()
        throws TorqueException
    {
        if (size() < 1) 
        {
            throw new IllegalStateException("method should not be called on" +
                " an empty list.");
        }

        List items = getExpandedMITListItems();
        ArrayList ids = new ArrayList(items.size());
        Iterator i = items.iterator();
        while (i.hasNext()) 
        {
            Integer id = ((MITListItem)i.next()).getModuleId();
            if (!ids.contains(id)) 
            {
                ids.add(id);
            }
        }
        return ids;
    }

    public List getModules()
        throws TorqueException
    {
        if (size() < 1) 
        {
            throw new IllegalStateException("method should not be called on" +
                " an empty list.");
        }

        List items = getExpandedMITListItems();
        ArrayList modules = new ArrayList(items.size());
        Iterator i = items.iterator();
        while (i.hasNext()) 
        {
            Module m = ((MITListItem)i.next()).getModule();
            if (!modules.contains(m)) 
            {
                modules.add(m);
            }
        }
        return modules;
    }

    public List getIssueTypeIds()
        throws TorqueException
    {
        if (size() < 1) 
        {
            throw new IllegalStateException("method should not be called on" +
                " an empty list.");
        }

        List items = getExpandedMITListItems();
        ArrayList ids = new ArrayList(items.size());
        Iterator i = items.iterator();
        while (i.hasNext()) 
        {
            Integer id = ((MITListItem)i.next()).getIssueTypeId();
            if (!ids.contains(id)) 
            {
                ids.add(id);
            }
        }
        return ids;
    }

    public void addToCriteria(Criteria crit)
    {
        if (size() > 0) 
        {
            List items = getExpandedMITListItems();
            Iterator i = items.iterator();
            Criteria.Criterion c = null;
            while (i.hasNext()) 
            {
                MITListItem item = (MITListItem)i.next();
                Criteria.Criterion c1 = 
                    crit.getNewCriterion(IssuePeer.MODULE_ID, 
                        item.getModuleId(), Criteria.EQUAL);
                Criteria.Criterion c2 = 
                    crit.getNewCriterion(IssuePeer.TYPE_ID, 
                        item.getIssueTypeId(), Criteria.EQUAL);
                c1.and(c2);
                if (c == null) 
                {
                    c = c1;
                }
                else 
                {
                    c.or(c1);
                }
            }
            crit.add(c);
        }
    }

    public void addMITListItem(MITListItem item)
        throws TorqueException
    {
        super.addMITListItem(item);
    }

    public List getExpandedMITListItems()
    {
        List items = new ArrayList();
        try
        {
            Iterator rawItems = getMITListItems().iterator();
            while (rawItems.hasNext()) 
            {
                MITListItem item = (MITListItem)rawItems.next();
                if (!item.isSingleModule()) 
                {
                    Module[] modules = getScarabUser()
                        .getModules(ScarabSecurity.ISSUE__SEARCH);
                    for (int i=0; i< modules.length; i++) 
                    {
                        Module module = modules[i];
                        if (item.isSingleIssueType()) 
                        {
                            MITListItem newItem = 
                                MITListItemManager.getInstance();
                            newItem.setModule(module);
                            newItem.setIssueType(getIssueType(item));
                            newItem.setListId(getListId());
                            items.add(newItem);
                        }
                        else 
                        {
                            addIssueTypes(module, items);
                        }
                    }                    
                }
                else if (!item.isSingleIssueType()) 
                {
                    addIssueTypes(getModule(item), items);
                }
                else 
                {
                    items.add(item);
                }
            }            
        }
        catch (Exception e)
        {
            throw new TorqueRuntimeException(e);
        }
        return items;
    }

    private void addIssueTypes(Module module, List items)
        throws Exception
    {
        Iterator rmits = module.getRModuleIssueTypes().iterator();
        while (rmits.hasNext()) 
        {
            MITListItem newItem = MITListItemManager.getInstance();
            newItem.setModuleId(module.getModuleId());
            newItem.setIssueTypeId(
                ((RModuleIssueType)rmits.next()).getIssueTypeId());
            newItem.setListId(getListId());
            items.add(newItem);
        }                            
    }


    public void scheduleItemForDeletion(MITListItem item)
    {
        if (itemsScheduledForDeletion == null) 
        {
            itemsScheduledForDeletion = new ArrayList();
        }
        itemsScheduledForDeletion.add(item);
    }

    public void save(Connection con)
        throws TorqueException
    {
        super.save(con);
        if (itemsScheduledForDeletion != null 
            && !itemsScheduledForDeletion.isEmpty()) 
        {
            List itemIds = new ArrayList(itemsScheduledForDeletion.size());
            for (Iterator iter = itemsScheduledForDeletion.iterator(); 
                 iter.hasNext();) 
            {
                MITListItem item = (MITListItem)iter.next();
                if (!item.isNew()) 
                {
                    itemIds.add(item.getItemId());
                }                
            }
            if (!itemIds.isEmpty()) 
            {
                Criteria crit = new Criteria();
                crit.addIn(MITListItemPeer.ITEM_ID, itemIds);
                MITListItemPeer.doDelete(crit);
            }
        }
    }
}
