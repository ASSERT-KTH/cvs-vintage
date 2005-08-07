// $Id: TestActivityGraphsFactory.java,v 1.6 2005/08/07 10:06:55 linus Exp $
// Copyright (c) 2002-2005 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.model.uml;

import junit.framework.TestCase;

import org.argouml.model.CheckUMLModelHelper;
import org.argouml.model.Model;


/**
 * Test the ActivityGraphsFactoryImpl class.
 *
 */
public class TestActivityGraphsFactory extends TestCase {

    /**
     * All the ModelElements we are going to test.
     */
    private static String[] allModelElements = {
        "ActivityGraph",
        "ActionState",
        "CallState",
        "ClassifierInState",
        "ObjectFlowState",
        "Partition",
        "SubactivityState",
    };

    /**
     * The constructor.
     *
     * @param n the name
     */
    public TestActivityGraphsFactory(String n) { super(n); }

    /**
     * Test the singleton pattern for the ActivityGraphsFactoryImpl class.
     */
    public void testSingleton() {
	Object o1 = Model.getActivityGraphsFactory();
	Object o2 = Model.getActivityGraphsFactory();
	assertTrue("Different singletons", o1 == o2);
    }

    /**
     * The test for creation.
     */
    public void testCreates() {
	String [] objs = {
	    "ActionState",
	    "ActivityGraph",
	    "CallState",
	    "ClassifierInState",
	    "ObjectFlowState",
	    "Partition",
	    "SubactivityState",
	    null,
	};

	CheckUMLModelHelper.createAndRelease(Model.getActivityGraphsFactory(),
					     objs);
    }

    /**
     * @return Returns the allModelElements.
     */
    static String[] getAllModelElements() {
        return allModelElements;
    }
}
