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

import java.util.LinkedList;

import junit.framework.TestCase;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ListToolsTest extends TestCase {

	LinkedList listFull, listPart1, listPart2;
	/**
	 * Constructor for ListToolsTest.
	 * @param arg0
	 */
	public ListToolsTest(String arg0) {
		super(arg0);
	}

	public void testIntersect() {
		LinkedList testList = new LinkedList(listFull);
		
		ListTools.intersect(testList,listFull);
		assertTrue(testList.equals(listFull));
		
		testList = new LinkedList(listFull);

		ListTools.intersect(testList,new LinkedList());
		assertTrue(testList.size()==0);
		
		testList = new LinkedList(listFull);

		ListTools.intersect(testList,listPart1);
		assertTrue(testList.equals(listPart1));

		testList = new LinkedList(listFull);

		ListTools.intersect(testList,listPart2);
		assertTrue(testList.equals(listPart2));

		testList = new LinkedList(listPart1);

		ListTools.intersect(testList,listPart2);
		assertTrue(testList.size()==0);
	}

	public void testSubstract() {
		LinkedList testList = new LinkedList(listFull);
		
		ListTools.substract(testList,listFull);
		assertTrue(testList.size()==0);
		
		testList = new LinkedList(listFull);

		ListTools.substract(testList,new LinkedList());
		assertTrue(testList.equals(listFull));
		
		testList = new LinkedList(listFull);

		ListTools.substract(testList,listPart1);
		assertTrue(testList.equals(listPart2));
		
		testList = new LinkedList(listFull);

		ListTools.substract(testList,listPart2);
		assertTrue(testList.equals(listPart1));

		testList = new LinkedList(listPart1);

		ListTools.substract(testList,listPart2);
		assertTrue(testList.equals(listPart1));
		
		testList = new LinkedList(listFull);

		ListTools.substract(testList,listPart1);
		ListTools.substract(testList,listPart2);
		assertTrue(testList.size() == 0);
		
	}
	

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		listFull = new LinkedList();
		listFull.add(new Integer(1));
		listFull.add(new Integer(2));
		listFull.add(new Integer(3));
		listFull.add(new Integer(4));
		listFull.add(new Integer(5));

		listPart1 = new LinkedList();
		listPart1.add(new Integer(1));
		listPart1.add(new Integer(3));
		listPart1.add(new Integer(5));
		
		listPart2 = new LinkedList();
		listPart2.add(new Integer(2));
		listPart2.add(new Integer(4));
	}

}
