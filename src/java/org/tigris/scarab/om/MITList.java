
package org.tigris.scarab.om;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
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
    public int size()
    {
        int size = 0;
        List items = wrappedGetMITListItems();
        if (items != null) 
        {
            size = items.size();
        }
        return size;
    }

    public boolean isEmpty()
    {
        boolean empty = true;
        List items = wrappedGetMITListItems();
        if (items != null) 
        {
            empty = items.isEmpty();
        }
        return empty;
    }

    public Iterator iterator()
    {
        Iterator i = null;
        List items = wrappedGetMITListItems();
        if (items == null)
        {
            Collections.EMPTY_LIST.iterator();
        }
        else 
        {
            i = items.iterator();
        }
        return i;
    }

    public MITListItem getFirstItem()
    {
        MITListItem i = null;
        List items = wrappedGetMITListItems();
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

    private IssueType getIssueType(MITListItem item)
        throws Exception
    {
        IssueType it = null;
        if (item.getIssueTypeId() == null) 
        {
            //it = getScarabUser().getCurrentIssueType();
        }
        else 
        {
            it = item.getIssueType();
        }
        return it;
    }

    private Module getModule(MITListItem item)
        throws Exception
    {
        Module module = null;
        if (item.getModuleId() == null) 
        {
            //module = getScarabUser().getCurrentModule();
        }
        else 
        {
            module = item.getModule();
        }
        return module;
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

        List items = wrappedGetMITListItems();
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
        throws Exception
    {
        if (size() < 1) 
        {
            throw new IllegalStateException("method should not be called on" +
                " an empty list.");
        }

        List items = wrappedGetMITListItems();
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


    public void addMITListItem(MITListItem item)
        throws TorqueException
    {
        super.addMITListItem(item);
    }

    private List wrappedGetMITListItems()
    {
        List items = null;
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
                            newItem.setModuleId(module.getModuleId());
                            newItem.setIssueTypeId(item.getIssueTypeId());
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
                    addIssueTypes(item.getModule(), items);
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
