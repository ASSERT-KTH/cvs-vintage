
package org.tigris.scarab.om;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.TorqueException;
import org.apache.torque.TorqueRuntimeException;
import org.tigris.scarab.services.security.ScarabSecurity;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class MITList 
    extends org.tigris.scarab.om.BaseMITList
    implements Persistent
{
    /**
     * a local reference to the user.
     */
    private ScarabUser aScarabUser;

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
     * @param ScarabUser v
     */
    public void setScarabUser(ScarabUser v) 
        throws TorqueException
    {
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
        if (size() < 2) 
        {
            throw new IllegalStateException("method should not be called on" +
                " a list of one or less items.");
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
            if (rma.getActive() && isCommon(att))
            {
                matchingAttributes.add(att);
            }
        }             
        
        return matchingAttributes;
    }

    /**
     * Checks all items after the first to see if they contain the attribute.
     * It is assumed the attribute is included in the first item.
     *
     * @param attribute an <code>Attribute</code> value
     * @return a <code>boolean</code> value
     */
    private boolean isCommon(Attribute attribute)
        throws Exception
    {
        boolean common = true;
        Iterator items = iterator();
        // skip the first one
        items.next();
        while (items.hasNext()) 
        {
            MITListItem compareItem = (MITListItem)items.next();
            RModuleAttribute modAttr = getModule(compareItem)
                        .getRModuleAttribute(attribute, 
                                             getIssueType(compareItem));
            if (modAttr == null || !modAttr.getActive())
            {
                common = false;
                break;
            }
        }
        return common;
    }


    public List getCommonNonUserAttributes()
        throws Exception
    {
        if (size() < 2) 
        {
            throw new IllegalStateException("method should not be called on" +
                " a list of one or less items.");
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
        if (size() < 2) 
        {
            throw new IllegalStateException("method should not be called on" +
                " a list of one or less items.");
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
        if (size() < 2) 
        {
            throw new IllegalStateException("method should not be called on" +
                " a list of one or less items.");
        }
        
        List matchingAttributes = new ArrayList();
        MITListItem item = getFirstItem();
        List rmas = getModule(item)
            .getRModuleAttributes(getIssueType(item), true, Module.USER);
        Iterator i = rmas.iterator();
        while (i.hasNext()) 
        {
            RModuleAttribute rma = (RModuleAttribute)i.next();
            Attribute att = rma.getAttribute();
            if ( rma.getActive() && isCommon(att)) 
            {
                matchingAttributes.add(att);   
            }            
        }
        return matchingAttributes;
    }

    public List getCommonRModuleUserAttributes()
        throws Exception
    {
        List matchingRMUAs = new ArrayList();
        MITListItem item = getFirstItem();
        Module module = getModule(item);
        IssueType issueType = getIssueType(item);
        List rmuas = getScarabUser()
            .getRModuleUserAttributes(module, issueType);
        if (rmuas.isEmpty())
        {
            rmuas = module.getDefaultRModuleUserAttributes(issueType);
        }
        Iterator i = rmuas.iterator();
        while (i.hasNext()) 
        {
            RModuleUserAttribute rmua = (RModuleUserAttribute)i.next();
            Attribute att = rmua.getAttribute();
            if ( isCommon(att)) 
            {
                matchingRMUAs.add(rmua);   
            }            
        }
        return matchingRMUAs;
    }

    public List getCommonLeafRModuleOptions(Attribute attribute)
        throws Exception
    {
        if (size() < 2) 
        {
            throw new IllegalStateException("method should not be called on" +
                " a list of one or less items.");
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
        if (size() < 2) 
        {
            throw new IllegalStateException("method should not be called on" +
                " a list of one or less items.");
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
        if (size() < 2) 
        {
            throw new IllegalStateException("method should not be called on" +
                " a list of one or less items.");
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
     * @param attribute an <code>Attribute</code> value
     * @return a <code>boolean</code> value
     */
    private boolean isCommon(AttributeOption option)
        throws Exception
    {
        boolean common = true;
        Iterator items = iterator();
        // skip the first one
        items.next();
        while (items.hasNext()) 
        {
            MITListItem compareItem = (MITListItem)items.next();
            RModuleOption modOpt = getModule(compareItem)
                .getRModuleOption(option, getIssueType(compareItem));
            if (modOpt == null || !modOpt.getActive())
            {
                common = false;
                break;
            }
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
            ObjectKey id = ((MITListItem)i.next()).getModuleId();
            if (!ids.contains(id)) 
            {
                ids.add(id);
            }
        }
        return ids;
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
            ObjectKey id = ((MITListItem)i.next()).getIssueTypeId();
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
            ArrayList ids = new ArrayList(items.size());
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
                ((RModuleIssueType)rmits.next()).getIssueTypeId() );
            newItem.setListId(getListId());
            items.add(newItem);
        }                            
    }
}
