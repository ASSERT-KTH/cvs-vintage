// $Id: TestExtensionMechanismsFactory.java,v 1.2 2004/11/21 14:28:10 linus Exp $
// Copyright (c) 2002-2004 The Regents of the University of California. All
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

import org.argouml.util.CheckUMLModelHelper;

/**
 * Test the ExtensionMechanismsFactory class.
 *
 */
public class TestExtensionMechanismsFactory extends TestCase {
    /**
     * List of model elements to test.
     */
    private static String[] allModelElements = {
	"Stereotype",
	"TaggedValue",
    };

    /**
     * The constructor.
     *
     * @param n the name of the test
     */
    public TestExtensionMechanismsFactory(String n) {
	super(n);
    }

    /**
     * Test if the ExtensionMechanismsFactory is really a singleton.
     */
    public void testSingleton() {
	Object o1 = ExtensionMechanismsFactory.getFactory();
	Object o2 = ExtensionMechanismsFactory.getFactory();
	assertTrue("Different singletons", o1 == o2);
    }

    /**
     * Test creation.
     */
    public void testCreates() {

	String[] objs = {
	    "Stereotype", "TaggedValue", null,
	};

	CheckUMLModelHelper.createAndRelease(
		     this,
		     ExtensionMechanismsFactory.getFactory(),
		     objs);

    }

    /**
     * Test complete deletion.
     */
    public void testDeleteComplete() {
	CheckUMLModelHelper.deleteComplete(
		   this,
		   ExtensionMechanismsFactory.getFactory(),
		   allModelElements);
    }

    /**
     * @return Returns the allModelElements.
     */
    static String[] getAllModelElements() {
        return allModelElements;
    }
}
