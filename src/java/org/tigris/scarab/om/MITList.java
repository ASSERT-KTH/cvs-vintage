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
import org.apache.torque.util.Criteria;
import org.apache.torque.TorqueException;
import org.apache.torque.TorqueRuntimeException;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.util.Log;

/** 
 * A class representing a list (not List) of MITListItems.  MIT stands for
 * Module and IssueType.  This class contains corresponding methods to many
 * in Module which take a single IssueType.  for example
 * module.getAttributes(issueType) is replaced with 
 * mitList.getCommonAttributes() in cases where several modules and issuetypes
 * are involved.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: MITList.java,v 1.43 2004/05/01 19:04:23 dabbous Exp $
 */
public class MITList
    extends org.tigris.scarab.om.BaseMITList
    implements Persistent
{
    /**
     * A local reference to the user.
     */
    private ScarabUser aScarabUser;

    private List itemsScheduledForDeletion;

    /**
     * Cache the expanded list because it is widely used and unlikely
     * to change after initialization.
     */
    private List expandedList = null;

    /**
     * Whether this list represents everything the user can query.
     */
    private boolean isAllMITs = false;

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

    /**
     * tests if the list is empty
     * 
     * @return true if the list is empty, otherwise false
     */
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

    /**
     * asserts that the list is not empty
     * 
     * @throws IllegalStateException if the list is empty
     */
    private void assertNotEmpty()
    {
        if (isEmpty())
        {
            throw new IllegalStateException("method should not be called on an empty list."); //EXCEPTION
        }
    }

    public Iterator iterator()
    {
        Iterator i = null;
        List items = getExpandedMITListItems();
        if (items == null)
        {
            i = Collections.EMPTY_LIST.iterator();
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

    public class ItemsIterator implements Iterator
    {
        private Iterator i;
        private Object currentObject;
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
            return currentObject;
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
                throw new TorqueRuntimeException(e); //EXCEPTION
            }

            if (rawList.contains(currentObject))
            {
                rawList.remove(currentObject);
                i.remove();
                expandedList = null;
            }
            else
            {
                throw new UnsupportedOperationException(
                    "Removing items "
                        + "from a list containing wildcards is not supported."); //EXCEPTION
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
        for (int i = 0; i < v.size(); i++)
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
    public MITList getPermittedSublist(String permission, ScarabUser user)
        throws Exception
    {
        String[] perms = { permission };
        return getPermittedSublist(perms, user);
    }

    /**
     * Creates a new MITList containing only those items from this list
     * for which the searcher has at least one of the permission.
     *
     * @param permission a <code>String</code> value
     * @param searcher a <code>ScarabUser</code> value
     * @return a <code>MITList</code> value
     */
    public MITList getPermittedSublist(String[] permissions, ScarabUser user)
        throws Exception
    {
        MITList sublist = new MITList();
        ScarabUser userB = getScarabUser();
        if (userB != null)
        {
            sublist.setScarabUser(userB);
        }
        List items = getExpandedMITListItems();
        sublist.isAllMITs = this.isAllMITs;
        Module[] validModules = user.getModules(permissions);

        Set moduleIds = new HashSet();
        for (int j = 0; j < validModules.length; j++)
        {
            moduleIds.add(validModules[j].getModuleId());
        }

        for (Iterator i = items.iterator(); i.hasNext();)
        {
            MITListItem item = (MITListItem) i.next();
            if (moduleIds.contains(item.getModuleId()))
            {
                // use a copy of the item here to avoid changing the the
                // list_id of the original
                sublist.addMITListItem(item.copy());
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
            i = (MITListItem) items.get(0);
        }
        return i;
    }

    public boolean isSingleModuleIssueType()
    {
        return size() == 1 && getFirstItem().isSingleModuleIssueType();
    }

    public boolean isSingleModule()
    {
        List ids = getModuleIds();
        return ids.size() == 1;
    }

    public boolean isSingleIssueType()
    {
        List ids = getIssueTypeIds();
        return ids.size() == 1;
    }

    public Module getModule() throws Exception
    {
        if (!isSingleModule())
        {
            throw new IllegalStateException(
                "method should not be called on"
                    + " a list including more than one module."); //EXCEPTION
        }
        return getModule(getFirstItem());
    }

    public IssueType getIssueType() throws Exception
    {
        if (!isSingleIssueType())
        {
            throw new IllegalStateException(
                "method should not be called on"
                    + " a list including more than one issue type."); //EXCEPTION
        }
        return getFirstItem().getIssueType();
    }

    Module getModule(MITListItem item) throws Exception
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
    public void setScarabUser(ScarabUser v) throws TorqueException
    {
        if (v == null)
        {
            throw new IllegalArgumentException("cannot set user to null."); //EXCEPTION
        }

        super.setScarabUser(v);
        aScarabUser = v;
        expandedList = null;
    }

    public ScarabUser getScarabUser() throws TorqueException
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

    public List getCommonAttributes(boolean activeOnly) throws Exception
    {
        List matchingAttributes = new ArrayList();
        MITListItem item = getFirstItem();

        List rmas = getModule(item).getRModuleAttributes(item.getIssueType());
        for (Iterator i = rmas.iterator(); i.hasNext();)
        {
            RModuleAttribute rma = (RModuleAttribute) i.next();
            Attribute att = rma.getAttribute();
            if ((!activeOnly || rma.getActive())
                && (size() == 1 || isCommon(att, activeOnly)))
            {
                matchingAttributes.add(att);
            }
        }

        return matchingAttributes;
    }

    public List getCommonAttributes() throws Exception
    {
        return getCommonAttributes(true);
    }

    /**
     * Checks all items to see if they contain the attribute.
     *
     * @param attribute an <code>Attribute</code> value
     * @return a <code>boolean</code> value
     */
    public boolean isCommon(Attribute attribute, boolean activeOnly)
        throws Exception
    {
        Criteria crit = new Criteria();
        addToCriteria(
            crit,
            RModuleAttributePeer.MODULE_ID,
            RModuleAttributePeer.ISSUE_TYPE_ID);
        crit.add(RModuleAttributePeer.ATTRIBUTE_ID, attribute.getAttributeId());
        if (activeOnly)
        {
            crit.add(RModuleAttributePeer.ACTIVE, true);
        }

        return size() == RModuleAttributePeer.count(crit);

        /*
        List rmas = RModuleAttributePeer.doSelect(crit); 
        boolean common = true;
        for (Iterator items = iterator(); items.hasNext() && common;) 
        {
            MITListItem compareItem = (MITListItem)items.next();
            boolean foundRma = false;
            for (Iterator rmaIter = rmas.iterator(); 
                 rmaIter.hasNext() && !foundRma;) 
            {
                RModuleAttribute rma = (RModuleAttribute)rmaIter.next();
                foundRma = (!activeOnly || rma.getActive()) &&
                    rma.getModuleId().equals(compareItem.getModuleId()) &&
                    rma.getIssueTypeId().equals(compareItem.getIssueTypeId());
            }
            common = foundRma;
        }
        return common;
        */
    }

    public boolean isCommon(Attribute attribute) throws Exception
    {
        return isCommon(attribute, true);
    }

    public List getCommonNonUserAttributes() throws Exception
    {
        assertNotEmpty();

        List matchingAttributes = new ArrayList();
        MITListItem item = getFirstItem();

        List rmas = getModule(item).getRModuleAttributes(item.getIssueType());
        Iterator i = rmas.iterator();
        while (i.hasNext())
        {
            RModuleAttribute rma = (RModuleAttribute) i.next();
            Attribute att = rma.getAttribute();
            if (!att.isUserAttribute() && rma.getActive() && isCommon(att))
            {
                matchingAttributes.add(att);
            }
        }

        return matchingAttributes;
    }

    public List getCommonOptionAttributes() throws Exception
    {
        assertNotEmpty();

        List matchingAttributes = new ArrayList();
        MITListItem item = getFirstItem();

        List rmas = getModule(item).getRModuleAttributes(item.getIssueType());
        Iterator i = rmas.iterator();
        while (i.hasNext())
        {
            RModuleAttribute rma = (RModuleAttribute) i.next();
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
    public List getCommonUserAttributes(boolean activeOnly) throws Exception
    {
        List attributes = null;
        if (isSingleModuleIssueType())
        {
            attributes =
                getModule().getUserAttributes(getIssueType(), activeOnly);
        }
        else
        {
            List matchingAttributes = new ArrayList();
            MITListItem item = getFirstItem();
            List rmas =
                getModule(item).getRModuleAttributes(
                    item.getIssueType(),
                    activeOnly,
                    Module.USER);
            Iterator i = rmas.iterator();
            while (i.hasNext())
            {
                RModuleAttribute rma = (RModuleAttribute) i.next();
                Attribute att = rma.getAttribute();
                if ((!activeOnly || rma.getActive())
                    && isCommon(att, activeOnly))
                {
                    matchingAttributes.add(att);
                }
            }
            attributes = matchingAttributes;
        }
        return attributes;
    }

    public List getCommonUserAttributes() throws Exception
    {
        return getCommonUserAttributes(false);
    }

    /**
     * potential assignee must have at least one of the permissions
     * for the user attributes in all the modules.
     */
    public List getPotentialAssignees(boolean includeCommitters)
        throws Exception
    {
        List users = new ArrayList();
        List perms = getUserAttributePermissions();
        if (includeCommitters && !perms.contains(ScarabSecurity.ISSUE__ENTER))
        {
            perms.add(ScarabSecurity.ISSUE__ENTER);
        }
        if (isSingleModule())
        {
            ScarabUser[] userArray = getModule().getUsers(perms);
            for (int i = 0; i < userArray.length; i++)
            {
                users.add(userArray[i]);
            }
        }
        else
        {
            MITListItem item = getFirstItem();
            ScarabUser[] userArray = getModule(item).getUsers(perms);
            List modules = getModules();
            for (int i = 0; i < userArray.length; i++)
            {
                boolean validUser = false;
                ScarabUser user = userArray[i];
                for (Iterator j = perms.iterator(); j.hasNext() && !validUser;)
                {
                    validUser = user.hasPermission((String) j.next(), modules);
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
    public List getUserAttributePermissions() throws Exception
    {
        List userAttrs = getCommonUserAttributes();
        List permissions = new ArrayList();
        for (int i = 0; i < userAttrs.size(); i++)
        {
            String permission = ((Attribute) userAttrs.get(i)).getPermission();
            if (!permissions.contains(permission))
            {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    public List getCommonRModuleUserAttributes() throws Exception
    {
        List matchingRMUAs = new ArrayList();
        List rmuas = getSavedRMUAs();
        Iterator i = rmuas.iterator();
        ScarabUser user = getScarabUser();
        while (i.hasNext())
        {
            RModuleUserAttribute rmua = (RModuleUserAttribute) i.next();
            Attribute att = rmua.getAttribute();
            if (isCommon(att, false))
            {
                matchingRMUAs.add(rmua);
            }
        }

        // None of the saved RMUAs are common for these pairs
        // Delete them and seek new ones.
        if (matchingRMUAs.isEmpty())
        {
            i = rmuas.iterator();
            while (i.hasNext())
            {
                RModuleUserAttribute rmua = (RModuleUserAttribute) i.next();
                rmua.delete(user);
            }
            int sizeGoal = 3;
            int moreAttributes = sizeGoal;

            // First try saved RMUAs for first module-issuetype pair
            MITListItem item = getFirstItem();
            Module module = getModule(item);
            IssueType issueType = item.getIssueType();
            rmuas = user.getRModuleUserAttributes(module, issueType);
            // Next try default RMUAs for first module-issuetype pair
            if (rmuas.isEmpty())
            {
                rmuas = module.getDefaultRModuleUserAttributes(issueType);
            }

            // Loop through these and if find common ones, save the RMUAs
            i = rmuas.iterator();
            while (i.hasNext() && moreAttributes > 0)
            {
                RModuleUserAttribute rmua = (RModuleUserAttribute) i.next();
                Attribute att = rmua.getAttribute();
                if (isCommon(att, false) && !matchingRMUAs.contains(rmua))
                {
                    RModuleUserAttribute newRmua =
                        getNewRModuleUserAttribute(att);
                    newRmua.setOrder(1);
                    newRmua.save();
                    matchingRMUAs.add(rmua);
                    moreAttributes--;
                }
            }

            // if nothing better, go with random common attributes
            moreAttributes = sizeGoal - matchingRMUAs.size();
            if (moreAttributes > 0)
            {
                Iterator attributes = getCommonAttributes(false).iterator();
                int k = 1;
                while (attributes.hasNext() && moreAttributes > 0)
                {
                    Attribute att = (Attribute) attributes.next();
                    boolean isInList = false;
                    i = matchingRMUAs.iterator();
                    while (i.hasNext())
                    {
                        RModuleUserAttribute rmua =
                            (RModuleUserAttribute) i.next();
                        if (rmua.getAttribute().equals(att))
                        {
                            isInList = true;
                            break;
                        }
                    }
                    if (!isInList)
                    {
                        RModuleUserAttribute rmua =
                            getNewRModuleUserAttribute(att);
                        rmua.setOrder(k++);
                        rmua.save();
                        matchingRMUAs.add(rmua);
                        moreAttributes--;
                    }
                }
            }
        }

        return matchingRMUAs;
    }

    protected RModuleUserAttribute getNewRModuleUserAttribute(Attribute attribute)
        throws Exception
    {
        RModuleUserAttribute result = RModuleUserAttributeManager.getInstance();
        result.setUserId(getUserId());
        result.setAttributeId(attribute.getAttributeId());

        if (isSingleModuleIssueType())
        {
            result.setModuleId(getModule().getModuleId());
            result.setIssueTypeId(getIssueType().getIssueTypeId());
        }

        if (!isNew())
        {
            result.setListId(getListId());
        }
        return result;
    }

    /**
    * get common and active RMOs.
    * 
    * @param rmos a list of RModuleOptions
    * @return the sublist of common and active RMOs
    * @throws TorqueException
    * @throws Exception
    * 
    * TODO write a more generic search routine (e.g. for getCommonAttributes,
    * getCommonNonUserAttributes, getCommonOptionAttributes, ...)
    */
    private List getMatchingRMOs(List rmos) throws TorqueException, Exception
    {
        List matchingRMOs = new ArrayList();
        if (rmos != null)
        {
            for (Iterator i = rmos.iterator(); i.hasNext();)
            {
                RModuleOption rmo = (RModuleOption) i.next();
                AttributeOption option = rmo.getAttributeOption();
                if (rmo.getActive() && isCommon(option))
                {
                    matchingRMOs.add(rmo);
                }
            }
        }
        return matchingRMOs;
    }

    protected List getSavedRMUAs() throws Exception
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
            crit.add(
                RModuleUserAttributePeer.MODULE_ID,
                getModule().getModuleId());
            crit.add(
                RModuleUserAttributePeer.ISSUE_TYPE_ID,
                getIssueType().getIssueTypeId());
        }
        else
        {
            crit.add(RModuleUserAttributePeer.LIST_ID, null);
            crit.add(RModuleUserAttributePeer.MODULE_ID, null);
            crit.add(RModuleUserAttributePeer.ISSUE_TYPE_ID, null);
        }
        crit.addAscendingOrderByColumn(
            RModuleUserAttributePeer.PREFERRED_ORDER);

        return RModuleUserAttributePeer.doSelect(crit);
    }

    public List getCommonLeafRModuleOptions(Attribute attribute)
        throws Exception
    {
        assertNotEmpty();
        
        MITListItem item = getFirstItem();
        List rmos =
            getModule(item).getLeafRModuleOptions(
                attribute,
                item.getIssueType());
        return getMatchingRMOs(rmos);
    }

    public List getCommonRModuleOptionTree(Attribute attribute)
        throws Exception
    {
        assertNotEmpty();
        MITListItem item = getFirstItem();
        List rmos =
            getModule(item).getOptionTree(attribute, item.getIssueType());
        return getMatchingRMOs(rmos);
    }

    public List getDescendantsUnion(AttributeOption option) throws Exception
    {
        assertNotEmpty();

        List matchingRMOs = new ArrayList();
        Iterator items = iterator();
        while (items.hasNext())
        {
            MITListItem item = (MITListItem) items.next();
            IssueType issueType = item.getIssueType();
            RModuleOption parent =
                getModule(item).getRModuleOption(option, issueType);
            if (parent != null)
            {
                Iterator i = parent.getDescendants(issueType).iterator();
                while (i.hasNext())
                {
                    RModuleOption rmo = (RModuleOption) i.next();
                    if (!matchingRMOs.contains(rmo))
                    {
                        matchingRMOs.add(rmo);
                    }
                }
            }
        }

        return matchingRMOs;
    }

    public boolean isCommon(AttributeOption option) throws Exception
    {
        return isCommon(option, true);
    }

    /**
     * Checks all items after the first to see if they contain the attribute.
     * It is assumed the attribute is included in the first item.
     *
     * @param option an <code>Attribute</code> value
     * @return a <code>boolean</code> value
     */
    public boolean isCommon(AttributeOption option, boolean activeOnly)
        throws Exception
    {
        Criteria crit = new Criteria();
        addToCriteria(
            crit,
            RModuleOptionPeer.MODULE_ID,
            RModuleOptionPeer.ISSUE_TYPE_ID);
        crit.add(RModuleOptionPeer.OPTION_ID, option.getOptionId());
        if (activeOnly)
        {
            crit.add(RModuleOptionPeer.ACTIVE, true);
        }

        return size() == RModuleOptionPeer.count(crit);
    }

    public List getModuleIds()
    {
        assertNotEmpty();

        List items = getExpandedMITListItems();
        ArrayList ids = new ArrayList(items.size());
        Iterator i = items.iterator();
        while (i.hasNext())
        {
            Integer id = ((MITListItem) i.next()).getModuleId();
            if (!ids.contains(id))
            {
                ids.add(id);
            }
        }
        return ids;
    }

    public List getModules() throws TorqueException
    {
        assertNotEmpty();

        List items = getExpandedMITListItems();
        ArrayList modules = new ArrayList(items.size());
        Iterator i = items.iterator();
        while (i.hasNext())
        {
            Module m = ((MITListItem) i.next()).getModule();
            if (!modules.contains(m))
            {
                modules.add(m);
            }
        }
        return modules;
    }

    public List getIssueTypeIds()
    {
        assertNotEmpty();

        List items = getExpandedMITListItems();
        ArrayList ids = new ArrayList(items.size());
        Iterator i = items.iterator();
        while (i.hasNext())
        {
            Integer id = ((MITListItem) i.next()).getIssueTypeId();
            if (!ids.contains(id))
            {
                ids.add(id);
            }
        }
        return ids;
    }

    public void addToCriteria(Criteria crit) throws Exception
    {
        addToCriteria(crit, IssuePeer.MODULE_ID, IssuePeer.TYPE_ID);
    }

    private void addToCriteria(
        Criteria crit,
        String moduleField,
        String issueTypeField)
        throws Exception
    {
        if (!isSingleModule() && isSingleIssueType())
        {
            crit.addIn(moduleField, getModuleIds());
            crit.add(issueTypeField, getIssueType().getIssueTypeId());
        }
        else if (isSingleModule() && !isSingleIssueType())
        {
            crit.add(moduleField, getModule().getModuleId());
            crit.addIn(issueTypeField, getIssueTypeIds());
        }
        else if (isAllMITs)
        {
            crit.addIn(moduleField, getModuleIds());
            // we do this to avoid including templates in results
            crit.addIn(issueTypeField, getIssueTypeIds());
        }
        else if (size() > 0)
        {
            List items = getExpandedMITListItems();
            Iterator i = items.iterator();
            Criteria.Criterion c = null;
            while (i.hasNext())
            {
                MITListItem item = (MITListItem) i.next();
                Criteria.Criterion c1 =
                    crit.getNewCriterion(
                        moduleField,
                        item.getModuleId(),
                        Criteria.EQUAL);
                Criteria.Criterion c2 =
                    crit.getNewCriterion(
                        issueTypeField,
                        item.getIssueTypeId(),
                        Criteria.EQUAL);
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

    public void addAll(MITList list) throws TorqueException
    {
        List currentList = getExpandedMITListItems();
        for (Iterator i = list.getExpandedMITListItems().iterator();
            i.hasNext();
            )
        {
            MITListItem item = (MITListItem) i.next();
            if (!currentList.contains(item))
            {
                addMITListItem(item);
            }
        }
    }

    public void addMITListItem(MITListItem item) throws TorqueException
    {
        super.addMITListItem(item);
        expandedList = null;
        calculateIsAllMITs(item);
    }

    private void calculateIsAllMITs(MITListItem item)
    {
        isAllMITs
            |= (MITListItem.MULTIPLE_KEY.equals(item.getModuleId())
                && MITListItem.MULTIPLE_KEY.equals(item.getIssueTypeId()));
    }

    public List getExpandedMITListItems()
    {
        if (expandedList == null)
        {
            List items = new ArrayList();
            try
            {
                for (Iterator rawItems = getMITListItems().iterator();
                    rawItems.hasNext();
                    )
                {
                    MITListItem item = (MITListItem) rawItems.next();
                    calculateIsAllMITs(item);
                    if (!item.isSingleModule())
                    {
                        Module[] modules =
                            getScarabUser().getModules(
                                ScarabSecurity.ISSUE__SEARCH);
                        for (int i = 0; i < modules.length; i++)
                        {
                            Module module = modules[i];
                            if (item.isSingleIssueType())
                            {
                                IssueType type = item.getIssueType();
                                if (module.getRModuleIssueType(type) != null)
                                {
                                    MITListItem newItem =
                                        MITListItemManager.getInstance();
                                    newItem.setModule(module);
                                    newItem.setIssueType(type);
                                    newItem.setListId(getListId());
                                    items.add(newItem);
                                }
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
                throw new TorqueRuntimeException(e); //EXCEPTION
            }
            expandedList = items;
        }

        return expandedList;
    }

    /**
     * Adds all the active issue types in module to the items List
     */
    private void addIssueTypes(Module module, List items) throws Exception
    {
        Iterator rmits = module.getRModuleIssueTypes().iterator();
        while (rmits.hasNext())
        {
            MITListItem newItem = MITListItemManager.getInstance();
            newItem.setModuleId(module.getModuleId());
            newItem.setIssueTypeId(
                ((RModuleIssueType) rmits.next()).getIssueTypeId());
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

    public void save(Connection con) throws TorqueException
    {
        super.save(con);
        if (itemsScheduledForDeletion != null
            && !itemsScheduledForDeletion.isEmpty())
        {
            List itemIds = new ArrayList(itemsScheduledForDeletion.size());
            for (Iterator iter = itemsScheduledForDeletion.iterator();
                iter.hasNext();
                )
            {
                MITListItem item = (MITListItem) iter.next();
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

    public String toString()
    {
        StringBuffer sb = new StringBuffer(100);
        sb.append(super.toString()).append(':');
        sb.append((getListId() == null) ? "New" : getListId().toString());
        if (getName() != null)
        {
            sb.append(" name=").append(getName());
        }
        sb.append('[');
        boolean addComma = false;
        try
        {
            for (Iterator rawItems = getMITListItems().iterator();
                rawItems.hasNext();
                )
            {
                if (addComma)
                {
                    sb.append(", ");
                }
                else
                {
                    addComma = true;
                }

                MITListItem item = (MITListItem) rawItems.next();
                sb
                    .append('(')
                    .append(item.getModuleId())
                    .append(',')
                    .append(item.getIssueTypeId())
                    .append(',')
                    .append(item.getListId())
                    .append(')');
            }
        }
        catch (Exception e)
        {
            sb.append("Error retrieving list items. see logs.");
            Log.get().warn("", e);
        }

        return sb.append(']').toString();
    }
}
