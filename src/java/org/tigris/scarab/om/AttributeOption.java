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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Comparator;
import java.util.Collections;

// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;

import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.cache.ScarabCache;

/** 
  * This class deals with AttributeOptions. For more details
  * about the implementation of this class, read the documentation
  * about how Scarab manages Attributes.
  * <p>
  * The implementation of this class is "smart" in that it will only
  * touch the database when it absolutely needs to. For example, if
  * you create a new AttributeOption, it will not query the database
  * for the parent/child relationships until you ask it to. It will then
  * cache the information locally.
  * <p>
  * All instances of AttributeOptions are cached using the 
  * TurbineGlobalCache service.
  *
  * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
  * @version $Id: AttributeOption.java,v 1.28 2003/01/16 22:44:38 jmcnally Exp $
  */
public class AttributeOption 
    extends BaseAttributeOption
    implements Persistent
{
    public static NumberKey STATUS__CLOSED__PK = new NumberKey("7");

    /** the name of this class */
    private static final String className = "AttributeOption";

    /** a local Attribute reference */
    private Attribute aAttribute;                 

    /**
     * Storage for ID's of the parents of this AttributeOption
     */
    private List sortedParents = null;

    /**
     * Storage for ID's of the children of this AttributeOption
     */
    private List sortedChildren = null;

    /**
     * A cached String of parentIds
     */
    private String parentIds = null;

    /**
     * Used in the creation of an 
     */
    private List orderedTree = null;

    /**
     * Must call getInstance()
     */
    protected AttributeOption()
    {
    }

    /**
     * Creates a key for use in caching AttributeOptions
     */
    static String getCacheKey(ObjectKey key)
    {
         String keyString = key.getValue().toString();
         return new StringBuffer(className.length() + keyString.length())
             .append(className).append(keyString).toString();
    }

    /**
     * A comparator for this class. Compares on OPTION_NAME.
     */
    private static final Comparator comparator = new Comparator()
        {
            public int compare(Object obj1, Object obj2)
            {
                int result = 1;
                AttributeOption opt1 = (AttributeOption)obj1; 
                AttributeOption opt2 = (AttributeOption)obj2;
                if (opt1.getName().equals(opt2.getName()))
                {
                    result = 0;
                }
                else
                {
                    result = -1;
                }
                return result;
            }
        };

    /**
     * Compares numeric value and in cases where the numeric value
     * is the same it compares the display values.
     */
    public static Comparator getComparator()
    {
        return comparator;
    }
    
    /**
     * Get the Attribute associated with this Option
     */
    public Attribute getAttribute() throws TorqueException
    {
        if ( aAttribute==null && (getAttributeId() != null) )
        {
            aAttribute = AttributeManager.getInstance(getAttributeId());
            
            // make sure the parent attribute is in synch.
            super.setAttribute(aAttribute);            
        }
        return aAttribute;
    }

    /**
     * Set the Attribute associated with this Option
     */
    public void setAttribute(Attribute v) throws TorqueException
    {
        aAttribute = v;
        super.setAttribute(v);
    }

    /**
     * A new AttributeOption
     */
    public static AttributeOption getInstance() 
    {
        return new AttributeOption();
    }


    /**
     * Get an instance of a particular AttributeOption by
     * Attribute and Name.
     */
    public static AttributeOption getInstance(Attribute attribute, String name)
        throws Exception
    {
        AttributeOption ao = null;
        Criteria crit = new Criteria();
        crit.add (AttributeOptionPeer.OPTION_NAME, name);
        crit.add (AttributeOptionPeer.ATTRIBUTE_ID, attribute.getAttributeId());
        List options = AttributeOptionPeer.doSelect(crit);
        if (options.size() == 1)
        {
            ao =  (AttributeOption) options.get(0);
        }
        return ao;
    }
    
    /**
     * Returns a list of AttributeOptions which are ancestors
     * of this AttributeOption. An Ancestor is the parent tree
     * going up from this AO. The order is bottom up.
     */
    public List getAncestors()
        throws Exception
    {
        List options = new ArrayList();
        addAncestors(options);
        return options;
    }

    /**
     * Recursive method that loops over the ancestors
     */
    private void addAncestors(List ancestors)
        throws Exception
    {
        List parents = getParents();
        for ( int i=parents.size()-1; i>=0; i-- ) 
        {
            AttributeOption parent = (AttributeOption) 
                parents.get(i);
            if (!ancestors.contains(parent)) 
            {
                ancestors.add(parent);    
                parent.addAncestors(ancestors);
            }
        }
    }

    /**
     * Returns a list of AttributeOptions which are descendants
     * of this AttributeOption. The descendants is the child tree
     * going down from this AO. The order is bottom up.
     */
    public List getDescendants()
        throws Exception
    {
        List options = new ArrayList();
        addDescendants(options);
        return options;
    }

    /**
     * Recursive method that loops over the descendants
     */
    private void addDescendants(List descendants)
        throws Exception
    {
        List children = getChildren();
        for ( int i=children.size()-1; i>=0; i-- ) 
        {
            AttributeOption child = (AttributeOption) 
                children.get(i);
            descendants.add(child);
            child.addDescendants(descendants);
        }
    }

    /**
     * Returns a list of AttributeOption's which are children
     * of this AttributeOption.
     */
    public List getChildren()
        throws TorqueException
    {
        if (sortedChildren == null)
        {
            buildChildren();
        }
        return sortedChildren;
    }

    /**
     * Returns a list of AttributeOption's which are parents
     * of this AttributeOption.
     */
    public List getParents()
        throws Exception
    {
        if (sortedParents == null)
        {
            buildParents();
        }
        return sortedParents;
    }

    /**
     * Builds a list of AttributeOption's which are children
     * of this AttributeOption.
     */
    private synchronized void buildChildren()
        throws TorqueException
    {
        Criteria crit = new Criteria()
            .add(ROptionOptionPeer.RELATIONSHIP_ID, 
                 OptionRelationship.PARENT_CHILD)
            .add(ROptionOptionPeer.OPTION1_ID,
                 super.getOptionId());

        List relations = ROptionOptionPeer.doSelect(crit);
        sortedChildren = new ArrayList(relations.size());
        for (int i=0; i < relations.size(); i++)
        {
            ROptionOption relation = (ROptionOption)relations.get(i);
            NumberKey key = relation.getOption2Id();
            if (key != null)
            {
                sortedChildren.add(relation.getOption2Option());
            }
        }
        sortChildren();
    }

    /**
     * Builds a list of AttributeOption's which are parents
     * of this AttributeOption.
     */
    private synchronized void buildParents()
        throws Exception
    {
        Criteria crit = new Criteria()
            .add(ROptionOptionPeer.RELATIONSHIP_ID, 
                 OptionRelationship.PARENT_CHILD)
            .add(ROptionOptionPeer.OPTION2_ID,
                 super.getOptionId());

        List relations = ROptionOptionPeer.doSelect(crit);
        sortedParents = new ArrayList(relations.size());
        for (int i=0; i < relations.size(); i++)
        {
            ROptionOption relation = (ROptionOption)relations.get(i);
            NumberKey key = relation.getOption1Id();
            if (key != null)
            {
                sortedParents.add(relation.getOption1Option());
            }
        }
        sortParents();
    }

    /**
     * re-sorts the Parents
     */
    public void sortParents()
    {
        synchronized (this)
        {
            Collections.sort(sortedParents, comparator);
        }
    }

    /**
     * re-sorts the children
     */
    public void sortChildren()
    {
        synchronized (this)
        {
            Collections.sort(sortedChildren, comparator);
        }
    }

    /**
     * Checks to see if this Attribute option is a child of
     * the passed in AttributeOption parent.
     */
    public boolean isChildOf(AttributeOption parent)
        throws Exception
    {
        return getParents().contains(parent);
    }

    /**
     * Checks to see if this Attribute option is a parent of
     * the passed in AttributeOption child.
     */
    public boolean isParentOf(AttributeOption child)
        throws Exception
    {
        return getChildren().contains(child);
    }
    
    /**
     * Does this AttributeOption have children?
     */
    public boolean hasChildren()
        throws Exception
    {
        return getChildren().size() > 0 ? true : false;
    }

    /**
     * Does this AttributeOption have parents?
     */
    public boolean hasParents()
        throws Exception
    {
        return getParents().size() > 0 ? true : false;
    }

    /**
     * Returns direct parent of this child.
     */
    public AttributeOption getParent()
        throws Exception
    {
        AttributeOption parent = null;
        Criteria crit = new Criteria()
            .add(ROptionOptionPeer.RELATIONSHIP_ID, 
                 OptionRelationship.PARENT_CHILD)
            .add(ROptionOptionPeer.OPTION2_ID,
                 super.getOptionId());
       
        List results = (List)ROptionOptionPeer.doSelect(crit);
        if (results.size() == 1)
        {
           ROptionOption roo = (ROptionOption)results.get(0);
           parent = roo.getOption1Option();
        }
        return parent;
    }

    /**
     * Delete mappings with all modules and issue types.
     */
    public void deleteModuleMappings(ScarabUser user)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleOptionPeer.OPTION_ID, getOptionId());
        RModuleOptionPeer.doDelete(crit);
        ScarabCache.clear();
    }

    /**
     * Add a list of Children to this AttributeOption
     * @throw Exception if child is already a child
    public void addChildren(List children)
        throws Exception
    {
        if (children == null)
        {
            throw new Exception ("AttributeOption.addChildren() -> no children to add");
        }
        else if (children.size() == 0)
        {
            return;
        }
        synchronized (this)
        {
            Iterator itr = children.iterator();
            while (itr.hasNext())
            {
                addChild((AttributeOption)itr.next());
            }
        }
    }
     */

    /**
     * Add a Child to this AttributeOption
     * @throw Exception if child is already a child
    public void addChild(AttributeOption child)
        throws Exception
    {
        if (child.isChildOf(this))
        {
            throw new Exception (
                "The child: " + child.getName() + 
                " is already a child of: " + this.getName());
        }

        // make sure that we exist in the database
        this.save();
        // make sure that the child exists in the database
        child.save();

        // create the mapping
        Criteria crit = new Criteria();
        crit.add (ROptionOptionPeer.OPTION1_ID, this.getOptionId());
        crit.add (ROptionOptionPeer.OPTION2_ID, child.getOptionId());
        ROptionOptionPeer.doInsert(crit);

        synchronized (this)
        {
            getChildren().add(child);
        }
        synchronized (child)
        {
            child.getParents().add(this);
        }
        sortChildren();
    }
     */

    /**
     * Add a list of Parents to this AttributeOption
     * @throw Exception if parents is already a parents
    public void addParents(List parents)
        throws Exception
    {
        if (parents == null)
        {
            throw new Exception ("AttributeOption.addParents() -> no parents to add");
        }
        else if (parents.size() == 0)
        {
            return;
        }
        synchronized (this)
        {
            Iterator itr = parents.iterator();
            int counter = 1;
            while (itr.hasNext())
            {
                addParent((AttributeOption)itr.next(), counter++);
            }
        }
    }
     */

/*
    public void addParent(AttributeOption parent)
    {
        ROptionOption roo = ROptionOption.getInstance();
        roo.setOption1Id(parent.getOptionId());
        roo.setOption2Id(this.getOptionId());
        roo.setPreferredOrder(this.getPreferredOrder());
        addParent(roo);
    }
*/
    /**
     * Add a Parent to this AttributeOption
     * @throw Exception if parent is already a parent
    public void addParent(ROptionOption parent)
        throws Exception
    {
        if (parent.isParentOf(this))
        {
            throw new Exception (
                "The parent: " + parent.getOption1Option().getName() + 
                " is already a parent of: " + this.getName());
        }

        // make sure that we exist in the database
        this.save();

        // create the mapping
        Criteria crit = new Criteria();
        crit.add (ROptionOptionPeer.OPTION1_ID, parent.getOption1Id());
        crit.add (ROptionOptionPeer.OPTION2_ID, this.getOptionId());
        crit.add (ROptionOptionPeer.RELATIONSHIP_ID, OptionRelationship.PARENT_CHILD);
        crit.add (ROptionOptionPeer.PREFERRED_ORDER, preferredOrder);
        ROptionOptionPeer.doInsert(crit);

        synchronized (this)
        {
            getParents().add(parent.getOption1Option());
        }
        synchronized (parent)
        {
            parent.getChildren().add(this);
        }
        sortParents();
        clearParentIds();
    }
     */
     
    /**
     * Delete all parents. This is usually not a good idea to
     * expose to the general public and is therefore a private 
     * method.
    private void deleteParents()
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add (ROptionOptionPeer.OPTION2_ID, this.getOptionId());
        ROptionOptionPeer.doDelete(crit);

        synchronized (this)
        {
            getParents().clear();
        }
        clearParentIds();
    }
     */

    /**
     * Delete a specific parent
    public void deleteParent(AttributeOption parent)
        throws Exception
    {
        if (!isChildOf(parent))
        {
            throw new Exception (
                parent.getName() + " is not a parent of: " +
                this.getName());
        }
        Criteria crit = new Criteria();
        crit.add (ROptionOptionPeer.OPTION1_ID, parent.getOptionId());
        crit.add (ROptionOptionPeer.OPTION2_ID, this.getOptionId());
        ROptionOptionPeer.doDelete(crit);
        
        synchronized (this)
        {
            getParents().remove(parent);
        }
        synchronized (parent)
        {
            parent.getChildren().remove(this);
        }
        sortParents();
        clearParentIds();
    }
     */

    /**
     * Delete all children
    public void deleteChildren()
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add (ROptionOptionPeer.OPTION1_ID, this.getOptionId());
        ROptionOptionPeer.doDelete(crit);

        synchronized (this)
        {
            getChildren().clear();
        }
    }
     */

    /**
     * Delete a specific child
    public void deleteChild(AttributeOption child)
        throws Exception
    {
        if (!isParentOf(child))
        {
            throw new Exception (
                child.getName() + " is not a child of: " +
                this.getName());
        }
        Criteria crit = new Criteria();
        crit.add (ROptionOptionPeer.OPTION1_ID, this.getOptionId());
        crit.add (ROptionOptionPeer.OPTION2_ID, child.getOptionId());
        ROptionOptionPeer.doDelete(crit);

        synchronized (this)
        {
            getChildren().remove(child);
        }
        synchronized (child)
        {
            child.getParents().remove(this);
        }
        sortChildren();
    }
     */

    /**
     * Get a CSV list of Parent id's associated with this 
     * Attribute Option.
    public String getParentIds()
        throws Exception
    {
        if (parentIds == null)
        {
            // special case of no parents == 0
            if (getParents().size() == 0)
            {
                parentIds = "0";
            }
            else
            {
                StringBuffer sb = new StringBuffer();
                synchronized (this)
                {
                    boolean firstTime = true;
                    Iterator itr = getParents().iterator();
                    while (itr.hasNext())
                    {
                        if (!firstTime)
                        {
                            sb.append (",");
                        }
                        AttributeOption ao = (AttributeOption)itr.next();
                        sb.append(ao.getOptionId());
                        firstTime = false;
                    }
                }
                parentIds = sb.toString();
            }
        }
        return parentIds;
    }
     */

    /**
     * Set a CSV list of Parent id's associated with this 
     * Attribute Option.
    public void setParentIds(String ids)
        throws Exception
    {
        if (ids == null || ids.length() == 0)
        {
            throw new Exception ("Need to specify a list of parent ids!");
        }
        StringTokenizer st = new StringTokenizer(ids, ",");
        int tokenCount = st.countTokens();
        List options = new ArrayList(tokenCount+1);
        if (tokenCount == 0)
        {
            AttributeOption ao = 
                AttributeOption.getInstance((ObjectKey)new NumberKey(0));
            if (!ao.isParentOf(this))
            {
                options.add(ao);
            }
        }
        else
        {
            while ( st.hasMoreTokens() ) 
            {
                String id = st.nextToken();
                AttributeOption ao = 
                    AttributeOption.getInstance((ObjectKey)new NumberKey(id));
                if (!ao.isParentOf(this))
                {
                    options.add(ao);
                }
            }
        }
        deleteParents();
        addParents(options);
    }
     */

    /**
     * Clears out the lists of parent ids
     */
    private void clearParentIds()
    {
        parentIds = null;
    }

    /**
     * A String representation of this object.
     */
    public String toString()
    {
        try
        {
            return "Id: " + getOptionId() + " Name: " + getName();// + " ParentIds: " + getParentIds(); 
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
/*
    public List getOrderedChildTree(AttributeOption option)
        throws Exception
    {
        walkTree(option);
        ArrayList list = new ArrayList();
        for ( int j=orderedTree.size()-1; j>=0; j-- )
        {
            AttributeOption ao = (AttributeOption) orderedTree.get(j);
            System.out.println (
                getTabs(ao.getParents().size()) + 
                ao.getOptionId() + " : '" + 
                ao.getParentIds() + "' : " + 
                ao.getWeight() + " : " + 
                ao.getParents().size() + " : " + 
                ao.getName());
            list.add(ao);
        }
        return list;
    }

    private void walkTree(AttributeOption option)
        throws Exception
    {
        List children = option.getChildren();
        for ( int j=children.size()-1; j>=0; j-- ) 
        {
            AttributeOption ao = (AttributeOption) children.get(j);
            if (ao.hasChildren())
            {
                walkTree(ao);
            }            
            orderedTree.add(ao);
        }
    }
    
    private String getTabs(int level)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i<level; i++)
        {
            sb.append("\t");
        }
        return sb.toString();
    }
*/
}
