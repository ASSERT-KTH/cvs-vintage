
package org.tigris.scarab.om;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import org.apache.torque.om.Persistent;
import org.apache.torque.TorqueException;
import org.apache.torque.TorqueRuntimeException;

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
        
        List rmas = item.getModule()
            .getRModuleAttributes(item.getIssueType());
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
            RModuleAttribute modAttr = compareItem.getModule()
                        .getRModuleAttribute(attribute, 
                                             compareItem.getIssueType());
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
        
        List rmas = item.getModule()
            .getRModuleAttributes(item.getIssueType());
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
        
        List rmas = item.getModule()
            .getRModuleAttributes(item.getIssueType());
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
        List rmas = item.getModule()
            .getRModuleAttributes(item.getIssueType(), true, Module.USER);
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
        List rmos = item.getModule()
            .getLeafRModuleOptions(attribute, item.getIssueType());
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
        List rmos = item.getModule()
            .getOptionTree(attribute, item.getIssueType());
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
            IssueType issueType = item.getIssueType();
            List rmos = item.getModule()
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
            RModuleOption modOpt = compareItem.getModule()
                .getRModuleOption(option, compareItem.getIssueType());
            System.out.println("Comparing " + compareItem.getIssueType().getName() + "; " + modOpt);
            if (modOpt == null || !modOpt.getActive())
            {
                common = false;
                break;
            }
        }
        return common;
    }

    public List getModuleIds()
        throws Exception
    {
        if (size() < 1) 
        {
            throw new IllegalStateException("method should not be called on" +
                " an empty list.");
        }

        List items = getMITListItems();
        ArrayList ids = new ArrayList(items.size());
        Iterator i = items.iterator();
        while (i.hasNext()) 
        {
            MITListItem item = (MITListItem)i.next();
            ids.add(item.getModuleId());
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

        List items = getMITListItems();
        ArrayList ids = new ArrayList(items.size());
        Iterator i = items.iterator();
        while (i.hasNext()) 
        {
            MITListItem item = (MITListItem)i.next();
            ids.add(item.getIssueTypeId());
        }
        return ids;
    }

    private List wrappedGetMITListItems()
    {
        List items = null;
        try
        {
            items = getMITListItems();
        }
        catch (TorqueException e)
        {
            throw new TorqueRuntimeException(e);
        }
        return items;
    }

}
