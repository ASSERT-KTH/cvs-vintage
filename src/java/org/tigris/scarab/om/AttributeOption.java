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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Comparator;

// Turbine classes
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.util.Log;

import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.module.ModuleEntity;

/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Wed Feb 28 16:36:26 PST 2001]
  *
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.

  */
public class AttributeOption 
    extends BaseAttributeOption
    implements Persistent
{
    public static NumberKey STATUS__CLOSED__PK = new NumberKey("7");

    private static HashMap parentChildMap;
    private static HashMap childParentMap;

    // need a local reference
    private Attribute aAttribute;                 

    static
    {
        try
        {            
            buildParentChildMaps();
        }
        catch (Exception e)
        {
            Log.error("Unable to setup option relationships", e);
        }
    }

    private static final Comparator comparator = new Comparator()
        {
            public int compare(Object obj1, Object obj2)
            {
                int result = 1;
                AttributeOption opt1 = (AttributeOption)obj1; 
                AttributeOption opt2 = (AttributeOption)obj2;
                if (opt1.getNumericValue() < opt2.getNumericValue()) 
                {
                    result = -1;
                }
                else if (opt1.getNumericValue() == opt2.getNumericValue()) 
                {
                    result = opt1.getName()
                        .compareTo(opt2.getName()); 
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
    
    public Attribute getAttribute() throws Exception
    {
        if ( aAttribute==null && (getAttributeId() != null) )
        {
            aAttribute = Attribute.getInstance(getAttributeId());
            
            // make sure the parent attribute is in synch.
            super.setAttribute(aAttribute);            
        }
        return aAttribute;
    }

    public void setAttribute(Attribute v) throws Exception
    {
        aAttribute = v;
        super.setAttribute(v);
    }

    public String getParentIds()
        throws Exception
    {
        String parentIds = null;
        AttributeOption[] parents = getParents();
        if ( parents == null ) 
        {
            parentIds = "";
        }
        else 
        {
            StringBuffer sb = new StringBuffer(5*parents.length);
            for ( int i=0; i<parents.length; i++ ) 
            {
                if ( i > 0 ) 
                {
                    sb.append(',');
                }
                sb.append(parents[i].getPrimaryKey().toString());
            }
            parentIds = sb.toString();
        }
        
        return parentIds;
    }

    public void setParentIds(String ids)
        throws Exception
    {
        boolean initMaps = false;
        if ( ids == null || ids.length() == 0 ) 
        {
            // remove any parent entries
            if ( childParentMap.containsKey( getPrimaryKey() ) )
            {
                initMaps = true;
                Criteria crit = new Criteria()
                    .add(ROptionOptionPeer.RELATIONSHIP_ID, 
                         OptionRelationship.PARENT_CHILD)
                    .add(ROptionOptionPeer.OPTION2_ID, getPrimaryKey() );
                
                ROptionOptionPeer.doDelete(crit);
            }
        }
        else 
        {
            List addParents = null;
            List currentParents = null;
            // get ids
            StringTokenizer st = new StringTokenizer(ids, ",");
            while ( st.hasMoreTokens() ) 
            {
                String id = st.nextToken();
                AttributeOption parent = getAttribute()
                    .getAttributeOption(new NumberKey(id));
                if ( parent == null ) 
                {
                    throw new ScarabException("Tried to assign option" + id +
                       "as parent of " + getPrimaryKey() + " and they do not "
                       + "share a common Attribute.");
                }
                else 
                {
                    // is this new or restatement of existing relationship
                    if ( isChildOf(parent) ) 
                    {
                        if ( currentParents == null ) 
                        {
                             currentParents = new ArrayList();
                        }
                        currentParents.add(parent);
                    }
                    else 
                    {
                        initMaps = true;
                        if ( addParents == null ) 
                        {
                            addParents = new ArrayList();
                        }
                        addParents.add(parent);
                    }
                }
            }

            // remove any parent entries that are no longer valid
            if ( childParentMap.containsKey( getPrimaryKey() )
                 && currentParents == null )
            {
                // remove all
                initMaps = true;
                Criteria crit = new Criteria()
                    .add(ROptionOptionPeer.RELATIONSHIP_ID, 
                         OptionRelationship.PARENT_CHILD)
                    .add(ROptionOptionPeer.OPTION2_ID, getPrimaryKey() );
                
                ROptionOptionPeer.doDelete(crit);
            }
            else if (childParentMap.containsKey( getPrimaryKey()) ) 
            {
                // remove entries not in the latest set
                initMaps = true; // might avoid initMaps by comparing sets

                NumberKey[] currIds = new NumberKey[currentParents.size()];
                for ( int i=0; i<currIds.length; i++ ) 
                {
                    currIds[i] = ((AttributeOption)currentParents.get(i))
                        .getOptionId();
                }

                Criteria crit = new Criteria()
                    .add(ROptionOptionPeer.RELATIONSHIP_ID, 
                         OptionRelationship.PARENT_CHILD)
                    .add(ROptionOptionPeer.OPTION2_ID, getPrimaryKey() )
                    .addNotIn(ROptionOptionPeer.OPTION2_ID, currIds);
                
                ROptionOptionPeer.doDelete(crit);
            }
            
            // add new entries
            if ( addParents != null ) 
            {
                for ( int i=addParents.size()-1; i>=0; i-- ) 
                {
                    ROptionOption roo = new ROptionOption();
                    roo.setOption1Id( ((AttributeOption)addParents
                                       .get(i)).getOptionId() );
                    roo.setOption2Id( getOptionId() );
                    roo.setOptionRelationshipKey(
                        OptionRelationship.PARENT_CHILD );
                    roo.save();
                }
            }
        }

        if ( initMaps ) 
        {
            buildParentChildMaps();                
        }
    }

    public AttributeOption[] getParents()
        throws Exception
    {
        AttributeOption[] options = null;
        NumberKey[] parentIds = 
            (NumberKey[])childParentMap.get(getPrimaryKey());
        if ( parentIds != null ) 
        {
            options = new AttributeOption[parentIds.length];
            for ( int i=parentIds.length-1; i>=0; i-- ) 
            {
                options[i] = getAttribute().getAttributeOption(parentIds[i]);
            }
        }
        
        return options;
    }


    public AttributeOption[] getChildren()    
        throws Exception
    {
        AttributeOption[] options = null;
        NumberKey[] childIds = 
            (NumberKey[])parentChildMap.get(getPrimaryKey());
        if ( childIds != null ) 
        {
            options = new AttributeOption[childIds.length];
            for ( int i=childIds.length-1; i>=0; i-- ) 
            {
                options[i] = getAttribute().getAttributeOption(childIds[i]);
            }
        }
        
        return options;
    }

    public List getDescendants()
        throws Exception
    {
        List options = new ArrayList();
        addChildren(options);
        return options;
    }

    private void addChildren(List descendants)
        throws Exception
    {
        NumberKey[] childIds = 
            (NumberKey[])parentChildMap.get(getPrimaryKey());
        if ( childIds != null ) 
        {
            for ( int i=childIds.length-1; i>=0; i-- ) 
            {
                AttributeOption child = (AttributeOption)
                    getAttribute().getAttributeOption(childIds[i]);
                descendants.add(child);
                child.addChildren(descendants);
            }
        }
    }

    public List getAncestors()
        throws Exception
    {
        List options = new ArrayList();
        addAncestors(options);
        return options;
    }

    private void addAncestors(List ancestors)
        throws Exception
    {
        NumberKey[] parentIds = 
            (NumberKey[])childParentMap.get(getPrimaryKey());
        if ( parentIds != null ) 
        {
            for ( int i=parentIds.length-1; i>=0; i-- ) 
            {
                AttributeOption parent = (AttributeOption) 
                    getAttribute().getAttributeOption(parentIds[i]);
                ancestors.add(parent);
                parent.addAncestors(ancestors);
            }
        }
    }

    public List descendantsInModule(ModuleEntity module)
        throws Exception
    {
        List moduleOptions = 
            module.getRModuleOptions(this.getAttribute(), false);
        List descendants = getDescendants();
        List descendantsInModule = new ArrayList();
        for ( int i=0; i<moduleOptions.size(); i++ ) 
        {
            AttributeOption moduleOption = 
                ((RModuleOption)moduleOptions.get(i)).getAttributeOption();
            for ( int j=0; j<descendants.size(); j++ ) 
            {
                if ( moduleOption.equals(descendants.get(j)) ) 
                {
                    descendantsInModule.add(moduleOption);
                }
            }
        }

        return descendantsInModule;
    }


    /**
     * Is this an ancestor of the option 
     */
    public boolean isParentOf(AttributeOption option)
    {
        boolean result = false;
        ObjectKey[] children = (ObjectKey[])
            parentChildMap.get(getPrimaryKey());
        if (children != null) 
        {
            for ( int i=children.length-1; i>=0; i-- ) 
            {
                if (children[i].equals(option.getPrimaryKey()))
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    /**
     * Is this an decendent of the option 
     */
    public boolean isChildOf(AttributeOption option)
    {
        boolean result = false;
        ObjectKey[] children = (ObjectKey[])
            parentChildMap.get(option.getPrimaryKey());
        if (children != null) 
        {
            for ( int i=children.length-1; i>=0; i-- ) 
            {
                if (children[i].equals(getPrimaryKey()))
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean hasChildren()
    {
        return parentChildMap.containsKey(getPrimaryKey()); 
    }

    public boolean hasParents()
    {
        return childParentMap.containsKey(getPrimaryKey()); 
    }
        
    public static void buildParentChildMaps()
        throws Exception
    {
        HashMap parentChildMap = new HashMap();
        HashMap childParentMap = new HashMap();

        Criteria crit = new Criteria()
            .add(ROptionOptionPeer.RELATIONSHIP_ID, 
                 OptionRelationship.PARENT_CHILD);

        List relations = ROptionOptionPeer.doSelect(crit);

        for ( int i=relations.size()-1; i>=0; i-- ) 
        {
            ROptionOption relation = (ROptionOption)relations.get(i);
            
            ArrayList children = null; 
            if ( parentChildMap.containsKey(relation.getOption1Id())) 
            {
                children = (ArrayList) 
                    parentChildMap.get(relation.getOption1Id());
            }
            else 
            {
                children = new ArrayList();
                parentChildMap.put(relation.getOption1Id(), children);
            }
            children.add(relation.getOption2Id());

            ArrayList parents = null; 
            if ( childParentMap.containsKey(relation.getOption2Id())) 
            {
                parents = (ArrayList) 
                    childParentMap.get(relation.getOption2Id());
            }
            else 
            {
                parents = new ArrayList();
                childParentMap.put(relation.getOption2Id(), parents);
            }
            parents.add(relation.getOption1Id());
        }

        // clean up, switch to arrays
        Iterator keys = parentChildMap.keySet().iterator();
        while ( keys.hasNext() ) 
        {
            Object key = keys.next();
            ArrayList children = (ArrayList)parentChildMap.get(key);
            Object[] childArray = 
                children.toArray(new NumberKey[children.size()]);
            parentChildMap.put(key, childArray);
        }
        keys = childParentMap.keySet().iterator();
        while ( keys.hasNext() ) 
        {
            Object key = keys.next();
            ArrayList parents = (ArrayList)childParentMap.get(key);
            Object[] parentArray = 
                parents.toArray(new NumberKey[parents.size()]);
            childParentMap.put(key, parentArray);
        }
        
        AttributeOption.parentChildMap = parentChildMap;
        AttributeOption.childParentMap = childParentMap;
    }
}

