//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.util;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;


/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ListToolsTest extends TestCase {
    LinkedList listFull_Integer;
    LinkedList listFull_String;
    Random random;

    /**
 * Constructor for ListToolsTest.
 * @param arg0
 */
    public ListToolsTest(String arg0) {
        super(arg0);
    }

    public void testIntersect() {
        /*
LinkedList testList = new LinkedList(listFull_I);

ListTools.intersect(testList,listFull_I);
assertTrue(testList.equals(listFull_I));

testList = new LinkedList(listFull_I);

ListTools.intersect(testList,new LinkedList());
assertTrue(testList.size()==0);

testList = new LinkedList(listFull_I);

ListTools.intersect(testList,listPart1_I);
assertTrue(testList.equals(listPart1_I));

testList = new LinkedList(listFull_I);

ListTools.intersect(testList,listPart2_I);
assertTrue(testList.equals(listPart2_I));

testList = new LinkedList(listPart1_I);

ListTools.intersect(testList,listPart2_I);
assertTrue(testList.size()==0);
*/
    }

    public void testSubstract() {
        testSubstractOnLists(listFull_Integer);
    }

    private void testSubstractOnLists(LinkedList listFull) {
        LinkedList testList = new LinkedList(listFull);
        LinkedList listPart1 = new LinkedList();
        LinkedList listPart2 = new LinkedList();

        Iterator it = listFull.iterator();

        while (it.hasNext()) {
            if (random.nextBoolean()) {
                listPart1.add(it.next());
            } else {
                listPart2.add(it.next());
            }
        }
    
        // test if the size of self substracted list is 0
        ListTools.substract(testList, listFull);
        assertTrue(testList.size() == 0);

        testList = new LinkedList(listFull);

        // tests, if the list substracted with an empty list is equal to itself
        ListTools.substract(testList, new LinkedList());
        assertTrue(testList.equals(listFull));

        testList = new LinkedList(listFull);

        // tests, if the listPart substracted
        ListTools.substract(testList, listPart1);
        Collections.sort(listPart2);
        assertTrue(testList.equals(listPart2));

        testList = new LinkedList(listFull);

        ListTools.substract(testList, listPart2);
        assertTrue(testList.equals(listPart1));

        testList = new LinkedList(listPart1);

        ListTools.substract(testList, listPart2);
        assertTrue(testList.equals(listPart1));

        testList = new LinkedList(listFull);

        ListTools.substract(testList, listPart1);
        ListTools.substract(testList, listPart2);
        assertTrue(testList.size() == 0);
    }

    /**
 * @see junit.framework.TestCase#setUp()
 */
    protected void setUp() throws Exception {
        random = new Random();

        listFull_Integer = new LinkedList();

        for (int i = 0; i < 10; i++) {
            listFull_Integer.add(new Integer(random.nextInt()));
        }

        listFull_String = new LinkedList();
        listFull_String.add("Hello");
        listFull_String.add("World");
        listFull_String.add("it is");
        listFull_String.add("a nice");
        listFull_String.add("day");
    }
}
