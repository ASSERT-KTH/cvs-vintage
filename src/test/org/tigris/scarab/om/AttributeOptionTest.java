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

import java.util.List;
import java.util.Iterator;

import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.test.BaseTestCase;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.ROptionOption;

/**
 * A Testing Suite for the om.Attribute class.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: AttributeOptionTest.java,v 1.3 2002/01/18 22:26:17 jon Exp $
 */
public class AttributeOptionTest extends BaseTestCase
{
    /**
     * Creates a new instance.
     *
     */
    public AttributeOptionTest()
    {
        super("AttributeOptionTest");
    }

    public static junit.framework.Test suite()
    {
        return new AttributeOptionTest();
    }

    protected void runTest()
        throws Throwable
    {
        AttributeOption ao = 
            AttributeOption.getInstance((ObjectKey)new NumberKey(83));

/*
        testGetChildren(ao);
        testGetParents(ao);
        testIsChildOf(ao);
        testIsParentOf(ao);
        testHasChildren(ao);
        testHasParents(ao);
        testAddDeleteChild();
        testAddDeleteParent();
*/
        testGetAncestors(ao);
        testGetDescendants(ao);
//        testWalkTree();
    }

    private void testGetChildren(AttributeOption ao)
        throws Exception
    {
        System.out.println (
            "Testing: testGetChildren() with AttributeOption: " + ao.getName());
        List options = ao.getChildren();
        Iterator itr = options.iterator();
        while (itr.hasNext())
        {
            AttributeOption val = ((AttributeOption)itr.next());
            System.out.println (val);
        }
        int size = options.size();
        assertEquals(size, 8);
        assertEquals("BSDI", ((AttributeOption)ao.getChildren().get(0)).getName());
        assertEquals("AIX", ((AttributeOption)ao.getChildren().get(1)).getName());
        assertEquals("BeOS", ((AttributeOption)ao.getChildren().get(2)).getName());
        assertEquals("HP-UX", ((AttributeOption)ao.getChildren().get(3)).getName());
        assertEquals("IRIX", ((AttributeOption)ao.getChildren().get(4)).getName());
        assertEquals("OSF/1", ((AttributeOption)ao.getChildren().get(5)).getName());
        assertEquals("Solaris", ((AttributeOption)ao.getChildren().get(6)).getName());
        assertEquals("SunOS", ((AttributeOption)ao.getChildren().get(7)).getName());
    }

    private void testGetParents(AttributeOption ao)
        throws Exception
    {
        System.out.println (
            "Testing: testGetParents() with AttributeOption: " + ao.getName());
        List options = ao.getParents();
        Iterator itr = options.iterator();
        while (itr.hasNext())
        {
            AttributeOption val = ((AttributeOption)itr.next());
            System.out.println (val);
        }
        int size = options.size();
        assertEquals(size, 1);
        assertEquals(((AttributeOption)ao.getParents().get(0)).getName(), "Unix");
    }

    private void testIsChildOf(AttributeOption ao)
        throws Exception
    {
        System.out.println (
            "Testing: testIsChildOf() with AttributeOption: " + ao.getName());
        AttributeOption parent = 
            AttributeOption.getInstance((ObjectKey)new NumberKey(87));
        assertEquals(true, ao.isChildOf(parent));
        System.out.println (ao.isChildOf(parent));
    }

    private void testIsParentOf(AttributeOption ao)
        throws Exception
    {
        System.out.println (
            "Testing: testIsParentOf() with AttributeOption: " + ao.getName());
        AttributeOption child = 
            AttributeOption.getInstance((ObjectKey)new NumberKey(39));
        assertEquals(true, ao.isParentOf(child));
        System.out.println (ao.isParentOf(child));
    }

    private void testHasChildren(AttributeOption ao)
        throws Exception
    {
        System.out.println (
            "Testing: testHasChildren() with AttributeOption: " + ao.getName());
        assertEquals(true, ao.hasChildren());
        System.out.println (ao.hasChildren());
    }

    private void testHasParents(AttributeOption ao)
        throws Exception
    {
        System.out.println (
            "Testing: testHasParents() with AttributeOption: " + ao.getName());
        assertEquals(true, ao.hasParents());
        System.out.println (ao.hasParents());
    }
/*
    private void testAddDeleteChild()
        throws Exception
    {
        System.out.println (
            "Testing: testAddDeleteChild()");

        // get an Operating System Attribute
        Attribute attribute = Attribute.getInstance((ObjectKey)new NumberKey(6));

        AttributeOption ao1 = AttributeOption.getInstance();
        ao1.setName("TestParent");
        ao1.setAttribute(attribute);
        AttributeOption ao2 = AttributeOption.getInstance();
        ao2.setName("TestChild");
        ao2.setAttribute(attribute);

        ao1.addChild(ao2);

        assertEquals(1, ao1.getChildren().size());
        assertEquals(0, ao2.getParents().size());

        List options = ao1.getChildren();
        Iterator itr = options.iterator();
        while (itr.hasNext())
        {
            AttributeOption val = ((AttributeOption)itr.next());
            System.out.println (val.getWeight() + " : " + val.getName());
        }
        assertEquals(true, ao1.isParentOf(ao2));
        assertEquals(true, ao2.isChildOf(ao1));

        ao1.deleteChild(ao2);
        
        assertEquals(false, ao1.isParentOf(ao2));
        assertEquals(false, ao2.isChildOf(ao1));
        
        AttributeOptionPeer.doDelete(ao1);
        AttributeOptionPeer.doDelete(ao2);
    }

    private void testAddDeleteParent()
        throws Exception
    {
        System.out.println (
            "Testing: testAddDeleteParent()");

        // get an Operating System Attribute
        Attribute attribute = Attribute.getInstance((ObjectKey)new NumberKey(6));

        AttributeOption ao1 = AttributeOption.getInstance();
        ao1.setName("TestChild");
        ao1.setAttribute(attribute);
        AttributeOption ao2 = AttributeOption.getInstance();
        ao2.setName("TestParent");
        ao2.setAttribute(attribute);
        ao2.setPreferredOrder(1);

        ao1.addParent(ao2);

        assertEquals(1, ao1.getParents().size());
        assertEquals(0, ao2.getChildren().size());

        List options = ao1.getParents();
        Iterator itr = options.iterator();
        while (itr.hasNext())
        {
            AttributeOption val = ((AttributeOption)itr.next());
            System.out.println (val.getWeight() + " : " + val.getName());
        }
        assertEquals(true, ao1.isChildOf(ao2));
        assertEquals(true, ao2.isParentOf(ao1));

        ao1.deleteParent(ao2);
        
        assertEquals(false, ao1.isChildOf(ao2));
        assertEquals(false, ao2.isParentOf(ao1));
        
        AttributeOptionPeer.doDelete(ao1);
        AttributeOptionPeer.doDelete(ao2);
    }
*/    
    private void testGetAncestors(AttributeOption ao)
        throws Exception
    {
        System.out.println (
            "Testing: testGetAncestors()");
            
        List ancestors = ao.getAncestors();
        Iterator itr = ancestors.iterator();
        while (itr.hasNext())
        {
            AttributeOption val = ((AttributeOption)itr.next());
            System.out.println (val.getPrimaryKey() + " : " + val.getName());
        }
        assertEquals("Unix", ((AttributeOption)(ancestors.get(0))).getName());
        assertEquals("All", ((AttributeOption)(ancestors.get(1))).getName());
    }

    private void testGetDescendants(AttributeOption ao)
        throws Exception
    {
        System.out.println (
            "Testing: testGetDescendants()");
            
        List descendants = ao.getDescendants();
        Iterator itr = descendants.iterator();
        while (itr.hasNext())
        {
            AttributeOption val = ((AttributeOption)itr.next());
            System.out.println (val.getPrimaryKey() + " : " + val.getName());
        }
        assertEquals("SunOS", ((AttributeOption)(descendants.get(0))).getName());
        assertEquals("Solaris", ((AttributeOption)(descendants.get(1))).getName());
        assertEquals("OSF/1", ((AttributeOption)(descendants.get(2))).getName());
        assertEquals("IRIX", ((AttributeOption)(descendants.get(3))).getName());
        assertEquals("HP-UX", ((AttributeOption)(descendants.get(4))).getName());
        assertEquals("BeOS", ((AttributeOption)(descendants.get(5))).getName());
        assertEquals("AIX", ((AttributeOption)(descendants.get(6))).getName());
        assertEquals("BSDI", ((AttributeOption)(descendants.get(7))).getName());
    }


/*    private void testWalkTree()
        throws Exception
    {
        // 87
        AttributeOption ao = 
            AttributeOption.getInstance((ObjectKey)new NumberKey(24));

//        ROptionOption roo = 
//            ROptionOption.getInstance((ObjectKey)new NumberKey(87));
        Attribute attr = Attribute.getInstance((ObjectKey)new NumberKey(6));

        attr.getOrderedParentChildList();
        
//        ROptionOption roo = new ROptionOption();
//        roo.getAttributeOptionList(ao.getAttribute());
    }
*/
}














